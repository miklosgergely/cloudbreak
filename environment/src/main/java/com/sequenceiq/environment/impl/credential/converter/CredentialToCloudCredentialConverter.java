package com.sequenceiq.environment.impl.credential.converter;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;
import com.sequenceiq.environment.impl.credential.Credential;
import com.sequenceiq.environment.impl.credential.Json;

@Component
public class CredentialToCloudCredentialConverter {

    private static final String CREDENTIAL_ID = "id";

    public CloudCredential convert(Credential credential) {
        if (credential == null) {
            return null;
        }
        Map<String, Object> fields;
        if (credential.getId() == null) {
            fields = isEmpty(credential.getAttributes()) ? new HashMap<>() : new Json(credential.getAttributes()).getMap();
        } else {
            fields = new Json(credential.getAttributes()).getMap();
        }
        fields.put(CREDENTIAL_ID, credential.getId());
        return new CloudCredential(credential.getId(), credential.getName(), fields);
    }

}