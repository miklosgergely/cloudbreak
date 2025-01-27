package com.sequenceiq.distrox.api.v1.distrox.model.cluster.storage.parameter;

import java.util.Objects;

import javax.validation.constraints.NotNull;

import com.sequenceiq.common.api.cloudstorage.old.validation.ValidS3CloudStorageParameters;
import com.sequenceiq.common.model.FileSystemType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@ValidS3CloudStorageParameters
public class S3CloudStorageV1Parameters implements CloudStorageV1Parameters {

    @ApiModelProperty
    @NotNull
    private String instanceProfile;

    @ApiModelProperty(hidden = true)
    @Override
    public FileSystemType getType() {
        return FileSystemType.S3;
    }

    public String getInstanceProfile() {
        return instanceProfile;
    }

    public void setInstanceProfile(String instanceProfile) {
        this.instanceProfile = instanceProfile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof S3CloudStorageV1Parameters)) {
            return false;
        }
        S3CloudStorageV1Parameters that = (S3CloudStorageV1Parameters) o;
        return Objects.equals(instanceProfile, that.instanceProfile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instanceProfile);
    }

}
