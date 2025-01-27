package com.sequenceiq.environment.environment.validation.validators;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.sequenceiq.cloudbreak.cloud.model.CloudRegions;
import com.sequenceiq.cloudbreak.util.ValidationResult;
import com.sequenceiq.cloudbreak.util.ValidationResult.ValidationResultBuilder;
import com.sequenceiq.environment.CloudPlatform;
import com.sequenceiq.environment.environment.domain.Environment;
import com.sequenceiq.environment.environment.dto.EnvironmentCreationDto;
import com.sequenceiq.environment.environment.dto.SecurityAccessDto;
import com.sequenceiq.environment.environment.validation.network.EnvironmentNetworkValidator;
import com.sequenceiq.environment.environment.validation.securitygroup.EnvironmentSecurityGroupValidator;
import com.sequenceiq.environment.network.dto.NetworkDto;

@Component
public class EnvironmentCreationValidator {

    private final EnvironmentRegionValidator environmentRegionValidator;

    private final Map<CloudPlatform, EnvironmentNetworkValidator> environmentNetworkValidatorsByCloudPlatform;

    private final Map<CloudPlatform, EnvironmentSecurityGroupValidator> environmentSecurityGroupValidatorsByCloudPlatform;

    public EnvironmentCreationValidator(EnvironmentRegionValidator environmentRegionValidator,
                                        Map<CloudPlatform, EnvironmentNetworkValidator> environmentNetworkValidatorsByCloudPlatform,
                                        Map<CloudPlatform, EnvironmentSecurityGroupValidator> environmentSecurityGroupValidatorsByCloudPlatform) {
        this.environmentRegionValidator = environmentRegionValidator;
        this.environmentNetworkValidatorsByCloudPlatform = environmentNetworkValidatorsByCloudPlatform;
        this.environmentSecurityGroupValidatorsByCloudPlatform = environmentSecurityGroupValidatorsByCloudPlatform;
    }

    public ValidationResult validate(Environment environment, EnvironmentCreationDto creationDto, CloudRegions cloudRegions) {
        String cloudPlatform = environment.getCloudPlatform();
        ValidationResultBuilder resultBuilder = ValidationResult.builder();
        environmentRegionValidator.validateRegions(creationDto.getRegions(), cloudRegions, cloudPlatform, resultBuilder);
        environmentRegionValidator.validateLocation(creationDto.getLocation(), creationDto.getRegions(), environment, resultBuilder);
        validateNetwork(creationDto, cloudPlatform, resultBuilder);
        validateSecurityGroup(creationDto, cloudPlatform, resultBuilder);
        return resultBuilder.build();
    }

    private void validateNetwork(EnvironmentCreationDto request, String cloudPlatform, ValidationResultBuilder resultBuilder) {
        NetworkDto networkDto = request.getNetwork();
        if (networkDto != null && Strings.isNullOrEmpty(networkDto.getNetworkCidr())) {
            EnvironmentNetworkValidator environmentNetworkValidator = environmentNetworkValidatorsByCloudPlatform.get(CloudPlatform.valueOf(cloudPlatform));
            if (environmentNetworkValidator != null) {
                environmentNetworkValidator.validate(networkDto, resultBuilder);
            } else {
                resultBuilder.error(String.format("Environment specific network is not supported for cloud platform: '%s'!", cloudPlatform));
            }
        }
    }

    private void validateSecurityGroup(EnvironmentCreationDto request, String cloudPlatform, ValidationResultBuilder resultBuilder) {
        SecurityAccessDto securityAccess = request.getSecurityAccess();
        NetworkDto networkDto = request.getNetwork();
        if (securityAccess != null && networkDto != null && Strings.isNullOrEmpty(securityAccess.getCidr())) {
            EnvironmentSecurityGroupValidator environmentSecurityGroupValidator =
                    environmentSecurityGroupValidatorsByCloudPlatform.get(CloudPlatform.valueOf(cloudPlatform));
            if (environmentSecurityGroupValidator != null) {
                environmentSecurityGroupValidator.validate(request, resultBuilder);
            } else {
                resultBuilder.error(String.format("Environment specific security group is not supported for cloud platform: '%s'!", cloudPlatform));
            }
        }
    }
}