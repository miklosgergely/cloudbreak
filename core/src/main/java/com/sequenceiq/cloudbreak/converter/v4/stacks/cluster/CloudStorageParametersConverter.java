package com.sequenceiq.cloudbreak.converter.v4.stacks.cluster;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsGen2View;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudAdlsView;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudGcsView;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudS3View;
import com.sequenceiq.cloudbreak.cloud.model.filesystem.CloudWasbView;
import com.sequenceiq.common.api.cloudstorage.old.AdlsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.AdlsGen2CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.GcsCloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.S3CloudStorageV1Parameters;
import com.sequenceiq.common.api.cloudstorage.old.WasbCloudStorageV1Parameters;
import com.sequenceiq.common.api.filesystem.AdlsFileSystem;
import com.sequenceiq.common.api.filesystem.AdlsGen2FileSystem;
import com.sequenceiq.common.api.filesystem.GcsFileSystem;
import com.sequenceiq.common.api.filesystem.S3FileSystem;
import com.sequenceiq.common.api.filesystem.WasbFileSystem;

@Component
public class CloudStorageParametersConverter {

    public AdlsFileSystem adlsToFileSystem(AdlsCloudStorageV1Parameters source) {
        AdlsFileSystem fileSystemConfigurations = new AdlsFileSystem();
        fileSystemConfigurations.setClientId(source.getClientId());
        fileSystemConfigurations.setAccountName(source.getAccountName());
        fileSystemConfigurations.setCredential(source.getCredential());
        fileSystemConfigurations.setTenantId(source.getTenantId());
        return fileSystemConfigurations;
    }

    public AdlsFileSystem adlsParametersToFileSystem(AdlsCloudStorageV1Parameters source) {
        AdlsFileSystem fileSystemConfigurations = new AdlsFileSystem();
        fileSystemConfigurations.setClientId(source.getClientId());
        fileSystemConfigurations.setAccountName(source.getAccountName());
        fileSystemConfigurations.setCredential(source.getCredential());
        fileSystemConfigurations.setTenantId(source.getTenantId());
        return fileSystemConfigurations;
    }

    public CloudAdlsView adlsToCloudView(AdlsCloudStorageV1Parameters source) {
        CloudAdlsView cloudAdlsView = new CloudAdlsView();
        cloudAdlsView.setAccountName(source.getAccountName());
        cloudAdlsView.setClientId(source.getClientId());
        cloudAdlsView.setCredential(source.getCredential());
        cloudAdlsView.setTenantId(source.getTenantId());
        return cloudAdlsView;
    }

    public AdlsCloudStorageV1Parameters adlsFileSystemToParameters(AdlsFileSystem source) {
        AdlsCloudStorageV1Parameters adlsCloudStorageV1Parameters = new AdlsCloudStorageV1Parameters();
        adlsCloudStorageV1Parameters.setClientId(source.getClientId());
        adlsCloudStorageV1Parameters.setAccountName(source.getAccountName());
        adlsCloudStorageV1Parameters.setCredential(source.getCredential());
        adlsCloudStorageV1Parameters.setTenantId(source.getTenantId());
        return adlsCloudStorageV1Parameters;
    }

    public GcsFileSystem gcsToFileSystem(GcsCloudStorageV1Parameters source) {
        GcsFileSystem fileSystemConfigurations = new GcsFileSystem();
        fileSystemConfigurations.setServiceAccountEmail(source.getServiceAccountEmail());
        return fileSystemConfigurations;
    }

    public CloudGcsView gcsToCloudView(GcsCloudStorageV1Parameters source) {
        CloudGcsView cloudGcsView = new CloudGcsView();
        cloudGcsView.setServiceAccountEmail(source.getServiceAccountEmail());
        return cloudGcsView;
    }

    public S3FileSystem s3ToFileSystem(S3CloudStorageV1Parameters source) {
        S3FileSystem fileSystemConfigurations = new S3FileSystem();
        fileSystemConfigurations.setInstanceProfile(source.getInstanceProfile());
        return fileSystemConfigurations;
    }

    public CloudS3View s3ToCloudView(S3CloudStorageV1Parameters source) {
        CloudS3View cloudS3View = new CloudS3View();
        cloudS3View.setInstanceProfile(source.getInstanceProfile());
        return cloudS3View;
    }

    public WasbFileSystem wasbToFileSystem(WasbCloudStorageV1Parameters source) {
        WasbFileSystem wasbFileSystem = new WasbFileSystem();
        wasbFileSystem.setSecure(source.isSecure());
        wasbFileSystem.setAccountName(source.getAccountName());
        wasbFileSystem.setAccountKey(source.getAccountKey());
        return wasbFileSystem;
    }

    public CloudWasbView wasbToCloudView(WasbCloudStorageV1Parameters source) {
        CloudWasbView cloudWasbView = new CloudWasbView();
        cloudWasbView.setAccountKey(source.getAccountKey());
        cloudWasbView.setAccountName(source.getAccountName());
        cloudWasbView.setSecure(source.isSecure());
        return cloudWasbView;
    }

    public AdlsGen2FileSystem adlsGen2ToFileSystem(AdlsGen2CloudStorageV1Parameters source) {
        AdlsGen2FileSystem adlsGen2FileSystem = new AdlsGen2FileSystem();
        adlsGen2FileSystem.setAccountName(source.getAccountName());
        adlsGen2FileSystem.setAccountKey(source.getAccountKey());
        return adlsGen2FileSystem;
    }

    public CloudAdlsGen2View adlsGen2ToCloudView(AdlsGen2CloudStorageV1Parameters source) {
        CloudAdlsGen2View cloudAdlsGen2View = new CloudAdlsGen2View();
        cloudAdlsGen2View.setAccountKey(source.getAccountKey());
        cloudAdlsGen2View.setAccountName(source.getAccountName());
        cloudAdlsGen2View.setSecure(source.isSecure());
        return cloudAdlsGen2View;
    }
}
