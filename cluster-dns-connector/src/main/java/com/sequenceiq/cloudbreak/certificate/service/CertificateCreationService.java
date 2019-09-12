package com.sequenceiq.cloudbreak.certificate.service;

import static com.sequenceiq.cloudbreak.certificate.PkiUtil.csr;

import java.io.IOException;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto;
import com.dyngr.Polling;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;
import com.sequenceiq.cloudbreak.certificate.poller.CreateCertificationPoller;
import com.sequenceiq.cloudbreak.client.GrpcClusterDnsClient;
import com.sequenceiq.cloudbreak.logger.LoggerContextKey;
import com.sequenceiq.cloudbreak.logger.MDCBuilder;

@Service
public class CertificateCreationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateCreationService.class);

    private static final Integer MAX_LENGTH_OF_ENVIRONMENT = 8;

    @Value("${gateway.cert.polling.intervall:10}")
    private Long pollingIntervall;

    @Value("${gateway.cert.polling.attempt:20}")
    private Integer pollingAttempt;

    @Value("${gateway.cert.base.domain.name:cloudera.site}")
    private String baseDomainName;

    @Inject
    private GrpcClusterDnsClient grpcClusterDnsClient;

    @Inject
    private GrpcUmsClient grpcUmsClient;

    public List<String> create(String actorCrn, String accountId, String endpoint, String environment, boolean wildcard, KeyPair identity) throws IOException {
        LOGGER.info("Start cert creation");
        Optional<String> requestIdOptional = Optional.ofNullable(MDCBuilder.getMdcContextMap().get(LoggerContextKey.REQUEST_ID.toString()));
        UserManagementProto.Account account = grpcUmsClient.getAccountDetails(actorCrn, actorCrn, requestIdOptional);
        String fullQualifiedDomainName = getFullQualifiedDomainName(endpoint, environment, account.getWorkloadSubdomain());
        LOGGER.info("Create cert for {}", fullQualifiedDomainName);
        PKCS10CertificationRequest csr = csr(identity, fullQualifiedDomainName);
        String pollingRequestId = grpcClusterDnsClient
                .createCertificate(actorCrn, accountId, endpoint, environment, wildcard, csr.getEncoded(), requestIdOptional);
        return polling(actorCrn, pollingRequestId);
    }

    public List<String> polling(String actorCrn, String pollingRequestId) {
        Optional<String> requestIdOptional = Optional.ofNullable(MDCBuilder.getMdcContextMap().get(LoggerContextKey.REQUEST_ID.toString()));
        return Polling.waitPeriodly(pollingIntervall, TimeUnit.SECONDS)
                .stopAfterAttempt(pollingAttempt)
                .stopIfException(true)
                .run(new CreateCertificationPoller(grpcClusterDnsClient, actorCrn, pollingRequestId, requestIdOptional));
    }

    public String getFqdn(String actorCrn, String endpoint, String environment) {
        Optional<String> requestIdOptional = Optional.ofNullable(MDCBuilder.getMdcContextMap().get(LoggerContextKey.REQUEST_ID.toString()));
        UserManagementProto.Account account = grpcUmsClient.getAccountDetails(actorCrn, actorCrn, requestIdOptional);
        return getFullQualifiedDomainName(endpoint, environment, account.getWorkloadSubdomain());
    }

    private String getFullQualifiedDomainName(String endpoint, String environment, String workloadSubdomain) {
        // We need to cap out the environment name to 8 characters. For now, we just truncate to 8
        // and verify that it's still DNS compliant. We assume the original environment name is DNS
        // compliant. Future work for using environment hash tracked in CDPCP-524
        int truncatePoint = environment.length();
        if (truncatePoint > MAX_LENGTH_OF_ENVIRONMENT) {
            truncatePoint = MAX_LENGTH_OF_ENVIRONMENT;
        }
        if (environment.charAt(truncatePoint - 1) == '-') {
            truncatePoint--;
        }
        return String.join(".", Arrays.asList(endpoint, environment.substring(0, truncatePoint), workloadSubdomain, baseDomainName));
    }
}