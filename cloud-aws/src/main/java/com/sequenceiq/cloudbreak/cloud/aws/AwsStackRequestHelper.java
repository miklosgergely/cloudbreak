package com.sequenceiq.cloudbreak.cloud.aws;

import static com.amazonaws.services.cloudformation.model.Capability.CAPABILITY_IAM;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.amazonaws.services.cloudformation.model.CreateStackRequest;
import com.amazonaws.services.cloudformation.model.DeleteStackRequest;
import com.amazonaws.services.cloudformation.model.OnFailure;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.annotations.VisibleForTesting;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsCredentialView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsInstanceProfileView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsNetworkView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsRdsDbSubnetGroupView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsRdsInstanceView;
import com.sequenceiq.cloudbreak.cloud.aws.view.AwsRdsVpcSecurityGroupView;
import com.sequenceiq.cloudbreak.cloud.context.AuthenticatedContext;
import com.sequenceiq.cloudbreak.cloud.exception.CloudConnectorException;
import com.sequenceiq.cloudbreak.cloud.model.CloudStack;
import com.sequenceiq.cloudbreak.cloud.model.DatabaseStack;
import com.sequenceiq.common.api.type.InstanceGroupType;

@Component
public class AwsStackRequestHelper {

    @Inject
    private AwsTagPreparationService awsTagPreparationService;

    @Inject
    private AwsClient awsClient;

    public CreateStackRequest createCreateStackRequest(AuthenticatedContext ac, CloudStack stack, String cFStackName, String subnet, String cfTemplate) {
        return new CreateStackRequest()
                .withStackName(cFStackName)
                .withOnFailure(OnFailure.DO_NOTHING)
                .withTemplateBody(cfTemplate)
                .withTags(awsTagPreparationService.prepareCloudformationTags(ac, stack.getTags()))
                .withCapabilities(CAPABILITY_IAM)
                .withParameters(getStackParameters(ac, stack, cFStackName, subnet));
    }

    public CreateStackRequest createCreateStackRequest(AuthenticatedContext ac, DatabaseStack stack, String cFStackName, String cfTemplate) {
        return new CreateStackRequest()
                .withStackName(cFStackName)
                .withOnFailure(OnFailure.DO_NOTHING)
                .withTemplateBody(cfTemplate)
                .withTags(awsTagPreparationService.prepareCloudformationTags(ac, stack.getTags()))
                .withCapabilities(CAPABILITY_IAM)
                .withParameters(getStackParameters(ac, stack));
    }

    public DeleteStackRequest createDeleteStackRequest(String cFStackName) {
        return new DeleteStackRequest()
                .withStackName(cFStackName);
    }

    private Collection<Parameter> getStackParameters(AuthenticatedContext ac, CloudStack stack, String stackName, String newSubnetCidr) {
        AwsNetworkView awsNetworkView = new AwsNetworkView(stack.getNetwork());
        AwsInstanceProfileView awsInstanceProfileView = new AwsInstanceProfileView(stack);
        String keyPairName = awsClient.getKeyPairName(ac);
        if (awsClient.existingKeyPairNameSpecified(stack.getInstanceAuthentication())) {
            keyPairName = awsClient.getExistingKeyPairName(stack.getInstanceAuthentication());
        }

        Collection<Parameter> parameters = new ArrayList<>(asList(
                new Parameter().withParameterKey("CBUserData").withParameterValue(stack.getImage().getUserDataByType(InstanceGroupType.CORE)),
                new Parameter().withParameterKey("CBGateWayUserData").withParameterValue(stack.getImage().getUserDataByType(InstanceGroupType.GATEWAY)),
                new Parameter().withParameterKey("StackName").withParameterValue(stackName),
                new Parameter().withParameterKey("StackOwner").withParameterValue(String.valueOf(ac.getCloudContext().getUserName())),
                new Parameter().withParameterKey("KeyName").withParameterValue(keyPairName),
                new Parameter().withParameterKey("AMI").withParameterValue(stack.getImage().getImageName()),
                new Parameter().withParameterKey("RootDeviceName").withParameterValue(getRootDeviceName(ac, stack))
        ));
        if (awsInstanceProfileView.isInstanceProfileAvailable()) {
            parameters.add(new Parameter().withParameterKey("InstanceProfile").withParameterValue(awsInstanceProfileView.getInstanceProfile()));
        }
        if (ac.getCloudContext().getLocation().getAvailabilityZone().value() != null) {
            parameters.add(new Parameter().withParameterKey("AvailabilitySet")
                    .withParameterValue(ac.getCloudContext().getLocation().getAvailabilityZone().value()));
        }
        if (awsNetworkView.isExistingVPC()) {
            parameters.add(new Parameter().withParameterKey("VPCId").withParameterValue(awsNetworkView.getExistingVpc()));
            if (awsNetworkView.isExistingIGW()) {
                parameters.add(new Parameter().withParameterKey("InternetGatewayId").withParameterValue(awsNetworkView.getExistingIgw()));
            }
            if (awsNetworkView.isExistingSubnet()) {
                parameters.add(new Parameter().withParameterKey("SubnetId").withParameterValue(awsNetworkView.getExistingSubnet()));
            } else {
                parameters.add(new Parameter().withParameterKey("SubnetCIDR").withParameterValue(newSubnetCidr));
            }
        }
        return parameters;
    }

    private String getRootDeviceName(AuthenticatedContext ac, CloudStack cloudStack) {
        AmazonEC2Client ec2Client = awsClient.createAccess(new AwsCredentialView(ac.getCloudCredential()),
                ac.getCloudContext().getLocation().getRegion().value());
        DescribeImagesResult images = ec2Client.describeImages(new DescribeImagesRequest().withImageIds(cloudStack.getImage().getImageName()));
        if (images.getImages().isEmpty()) {
            throw new CloudConnectorException(String.format("AMI is not available: '%s'.", cloudStack.getImage().getImageName()));
        }
        Image image = images.getImages().get(0);
        if (image == null) {
            throw new CloudConnectorException(String.format("Couldn't describe AMI '%s'.", cloudStack.getImage().getImageName()));
        }
        return image.getRootDeviceName();
    }

    @VisibleForTesting
    Collection<Parameter> getStackParameters(AuthenticatedContext ac, DatabaseStack stack) {
        AwsNetworkView awsNetworkView = new AwsNetworkView(stack.getNetwork());
        AwsRdsInstanceView awsRdsInstanceView = new AwsRdsInstanceView(stack.getDatabaseServer());
        AwsRdsDbSubnetGroupView awsRdsDbSubnetGroupView = new AwsRdsDbSubnetGroupView(stack.getDatabaseServer());
        AwsRdsVpcSecurityGroupView awsRdsVpcSecurityGroupView = new AwsRdsVpcSecurityGroupView(stack.getDatabaseServer());
        List<Parameter> parameters = new ArrayList<>(asList(
                new Parameter().withParameterKey("DBInstanceClassParameter").withParameterValue(awsRdsInstanceView.getDBInstanceClass()),
                new Parameter().withParameterKey("DBInstanceIdentifierParameter").withParameterValue(awsRdsInstanceView.getDBInstanceIdentifier()),
                new Parameter().withParameterKey("DBSubnetGroupNameParameter").withParameterValue(awsRdsDbSubnetGroupView.getDBSubnetGroupName()),
                new Parameter().withParameterKey("DBSubnetGroupSubnetIdsParameter").withParameterValue(String.join(",", awsNetworkView.getSubnetList())),
                new Parameter().withParameterKey("EngineParameter").withParameterValue(awsRdsInstanceView.getEngine()),
                new Parameter().withParameterKey("MasterUsernameParameter").withParameterValue(awsRdsInstanceView.getMasterUsername()),
                new Parameter().withParameterKey("MasterUserPasswordParameter").withParameterValue(awsRdsInstanceView.getMasterUserPassword()),
                new Parameter().withParameterKey("StackOwner").withParameterValue(ac.getCloudContext().getUserName()))
        );

        addParameterIfNotNull(parameters, "AllocatedStorageParameter", awsRdsInstanceView.getAllocatedStorage());
        addParameterIfNotNull(parameters, "BackupRetentionPeriodParameter", awsRdsInstanceView.getBackupRetentionPeriod());
        addParameterIfNotNull(parameters, "EngineVersionParameter", awsRdsInstanceView.getEngineVersion());
        addParameterIfNotNull(parameters, "PortParameter", stack.getDatabaseServer().getPort());

        if (awsRdsInstanceView.getVPCSecurityGroups().isEmpty()) {
            // VPC-id and VPC cidr should be filled in
            parameters.addAll(
                    asList(
                            new Parameter().withParameterKey("VPCIdParameter").withParameterValue(String.valueOf(awsNetworkView.getExistingVpc())),
                            new Parameter().withParameterKey("VPCCidrParameter").withParameterValue(String.valueOf(awsNetworkView.getExistingVpcCidr())),
                            new Parameter().withParameterKey("DBSecurityGroupNameParameter")
                                    .withParameterValue(awsRdsVpcSecurityGroupView.getDBSecurityGroupName())
                    )
            );
        } else {
            parameters.add(
                    new Parameter().withParameterKey("VPCSecurityGroupsParameter")
                            .withParameterValue(String.join(",", awsRdsInstanceView.getVPCSecurityGroups()))
            );
        }

        return parameters;
    }

    private void addParameterIfNotNull(List<Parameter> parameters, String key, Object value) {
        if (value != null) {
            parameters.add(new Parameter().withParameterKey(key).withParameterValue(Objects.toString(value)));
        }
    }
}
