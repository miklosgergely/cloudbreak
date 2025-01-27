package com.sequenceiq.cloudbreak.cmtemplate.configproviders.knox;

import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.ConfigUtils.config;
import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.knox.KnoxRoles.IDBROKER;
import static com.sequenceiq.cloudbreak.cmtemplate.configproviders.knox.KnoxRoles.KNOX;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cloudera.api.swagger.model.ApiClusterTemplateConfig;
import com.sequenceiq.cloudbreak.cmtemplate.configproviders.AbstractRoleConfigProvider;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.template.TemplatePreparationObject;
import com.sequenceiq.common.model.FileSystemType;

@Component
public class KnoxIdBrokerConfigProvider extends AbstractRoleConfigProvider {

    private static final Map<String, CloudPlatform> FILE_SYSTEM_TYPE_TO_CLOUD_PLATFORM_MAP = Map.ofEntries(
            Map.entry(FileSystemType.S3.name(), CloudPlatform.AWS),
            Map.entry(FileSystemType.ADLS.name(), CloudPlatform.AZURE),
            Map.entry(FileSystemType.ADLS_GEN_2.name(), CloudPlatform.AZURE),
            Map.entry(FileSystemType.WASB.name(), CloudPlatform.AZURE),
            Map.entry(FileSystemType.WASB_INTEGRATED.name(), CloudPlatform.AZURE),
            Map.entry(FileSystemType.GCS.name(), CloudPlatform.GCP)
    );

    @Override
    protected List<ApiClusterTemplateConfig> getRoleConfigs(String roleType, TemplatePreparationObject source) {
        switch (roleType) {
            case IDBROKER:
                List<ApiClusterTemplateConfig> config;
                String userMapping = getIdentityMapping(source.getIdentityUserMapping());
                String groupMapping = getIdentityMapping(source.getIdentityGroupMapping());
                switch (getCloudPlatform(source)) {
                    case AWS:
                        config = List.of(config("idbroker_aws_user_mapping", userMapping),
                                config("idbroker_aws_group_mapping", groupMapping));
                        break;
                    case AZURE:
                        config = List.of(config("idbroker_azure_user_mapping", userMapping),
                                config("idbroker_azure_group_mapping", groupMapping));
                        break;
                    case GCP:
                        config = List.of(config("idbroker_gcp_user_mapping", userMapping),
                                config("idbroker_gcp_group_mapping", groupMapping));
                        break;
                    default:
                        config = List.of();
                        break;
                }
                return config;
            default:
                return List.of();
        }
    }

    private String getIdentityMapping(Map<String, String> identityMapping) {
        return identityMapping.entrySet().stream()
                .map(e -> String.format("%s=%s", e.getKey(), e.getValue()))
                .collect(Collectors.joining(";"));
    }

    private CloudPlatform getCloudPlatform(TemplatePreparationObject source) {
        CloudPlatform cloudPlatform;
        if (source.getFileSystemConfigurationView().isPresent()) {
            String fileSystemType = source.getFileSystemConfigurationView().get().getType();
            cloudPlatform = FILE_SYSTEM_TYPE_TO_CLOUD_PLATFORM_MAP.get(fileSystemType);
            if (cloudPlatform == null) {
                throw new IllegalStateException("Unknown file system type: " + fileSystemType);
            }
        } else {
            cloudPlatform = source.getCloudPlatform();
        }
        return cloudPlatform;
    }

    @Override
    public String getServiceType() {
        return KNOX;
    }

    @Override
    public List<String> getRoleTypes() {
        return List.of(IDBROKER);
    }

}
