package com.sequenceiq.it.cloudbreak.dto.sdx;

import com.sequenceiq.common.api.cloudstorage.old.S3CloudStorageV1Parameters;
import com.sequenceiq.common.model.FileSystemType;
import com.sequenceiq.it.cloudbreak.Prototype;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.AbstractSdxTestDto;
import com.sequenceiq.sdx.api.model.SdxCloudStorageRequest;
import com.sequenceiq.sdx.api.model.SdxClusterResponse;

@Prototype
public class SdxCloudStorageTestDto extends AbstractSdxTestDto<SdxCloudStorageRequest, SdxClusterResponse, SdxCloudStorageTestDto> {

    protected SdxCloudStorageTestDto(TestContext testContext) {
        super(new SdxCloudStorageRequest(), testContext);
    }

    @Override
    public SdxCloudStorageTestDto valid() {
        return getCloudProvider().cloudStorage(this);
    }

    public SdxCloudStorageTestDto withS3(S3CloudStorageV1Parameters s3Parameters) {
        getRequest().setS3(s3Parameters);
        return this;
    }

    public SdxCloudStorageTestDto withFileSystemType(FileSystemType fileSystemType) {
        getRequest().setFileSystemType(fileSystemType);
        return this;
    }

    public SdxCloudStorageTestDto withBaseLocation(String baseLocation) {
        getRequest().setBaseLocation(baseLocation);
        return this;
    }
}
