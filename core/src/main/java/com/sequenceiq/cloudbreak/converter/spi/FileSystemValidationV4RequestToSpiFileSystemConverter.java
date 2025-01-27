package com.sequenceiq.cloudbreak.converter.spi;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.endpoint.v4.stacks.request.FileSystemValidationV4Request;
import com.sequenceiq.cloudbreak.cloud.model.SpiFileSystem;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudFileSystemView;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.converter.v4.stacks.cluster.CloudStorageParametersConverter;
import com.sequenceiq.common.model.FileSystemType;

@Component
public class FileSystemValidationV4RequestToSpiFileSystemConverter
        extends AbstractConversionServiceAwareConverter<FileSystemValidationV4Request, SpiFileSystem> {

    @Inject
    private CloudStorageParametersConverter cloudStorageParametersConverter;

    @Override
    public SpiFileSystem convert(FileSystemValidationV4Request source) {
        CloudFileSystemView cloudFileSystemView = null;
        if (source.getAdls() != null) {
            cloudFileSystemView = cloudStorageParametersConverter.adlsToCloudView(source.getAdls());
        } else if (source.getGcs() != null) {
            cloudFileSystemView = cloudStorageParametersConverter.gcsToCloudView(source.getGcs());
        } else if (source.getS3() != null) {
            cloudFileSystemView = cloudStorageParametersConverter.s3ToCloudView(source.getS3());
        } else if (source.getWasb() != null) {
            cloudFileSystemView = cloudStorageParametersConverter.wasbToCloudView(source.getWasb());
        } else if (source.getAdlsGen2() != null) {
            cloudFileSystemView = cloudStorageParametersConverter.adlsGen2ToCloudView(source.getAdlsGen2());
        }
        return new SpiFileSystem(source.getName(), FileSystemType.valueOf(source.getType()), cloudFileSystemView);
    }
}
