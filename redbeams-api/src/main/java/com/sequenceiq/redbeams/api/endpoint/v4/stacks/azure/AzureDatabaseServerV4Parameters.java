package com.sequenceiq.redbeams.api.endpoint.v4.stacks.azure;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;
import com.sequenceiq.cloudbreak.common.mappable.MappableBase;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.Map;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class AzureDatabaseServerV4Parameters extends MappableBase {

    @ApiModelProperty
    private Integer backupRetentionDays;

    @ApiModelProperty
    private boolean geoRedundantBackup;

    @ApiModelProperty
    private Integer skuCapacity;

    @ApiModelProperty
    private String skuFamily;

    @ApiModelProperty
    private String skuTier;

    @ApiModelProperty
    private boolean storageAutoGrow;

    @ApiModelProperty
    private String dbVersion;

    public Integer getBackupRetentionDays() {
        return backupRetentionDays;
    }

    public void setBackupRetentionDays(Integer backupRetentionDays) {
        this.backupRetentionDays = backupRetentionDays;
    }

    public boolean isGeoRedundantBackup() {
        return geoRedundantBackup;
    }

    public void setGeoRedundantBackup(boolean geoRedundantBackup) {
        this.geoRedundantBackup = geoRedundantBackup;
    }

    public Integer getSkuCapacity() {
        return skuCapacity;
    }

    public void setSkuCapacity(Integer skuCapacity) {
        this.skuCapacity = skuCapacity;
    }

    public String getSkuFamily() {
        return skuFamily;
    }

    public void setSkuFamily(String skuFamily) {
        this.skuFamily = skuFamily;
    }

    public String getSkuTier() {
        return skuTier;
    }

    public void setSkuTier(String skuTier) {
        this.skuTier = skuTier;
    }

    public boolean isStorageAutoGrow() {
        return storageAutoGrow;
    }

    public void setStorageAutoGrow(boolean storageAutoGrow) {
        this.storageAutoGrow = storageAutoGrow;
    }

    public String getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(String dbVersion) {
        this.dbVersion = dbVersion;
    }

    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> map = super.asMap();
        putIfValueNotNull(map, "backupRetentionDays", backupRetentionDays);
        putIfValueNotNull(map, "geoRedundantBackup", toEnabledDisabled(geoRedundantBackup));
        putIfValueNotNull(map, "skuCapacity", skuCapacity);
        putIfValueNotNull(map, "skuFamily", skuFamily);
        putIfValueNotNull(map, "skuTier", skuTier);
        putIfValueNotNull(map, "storageAutoGrow", toEnabledDisabled(storageAutoGrow));
        putIfValueNotNull(map, "dbVersion", dbVersion);
        return map;
    }

    @Override
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public CloudPlatform getCloudPlatform() {
        return CloudPlatform.AZURE;
    }

    @Override
    public void parse(Map<String, Object> parameters) {
        backupRetentionDays = getInt(parameters, "backupRetentionDays");
        geoRedundantBackup = fromEnabledDisabled(getParameterOrNull(parameters, "geoRedundantBackup"));
        skuCapacity = getInt(parameters, "skuCapacity");
        skuFamily = getParameterOrNull(parameters, "skuFamily");
        skuTier = getParameterOrNull(parameters, "skuTier");
        storageAutoGrow = fromEnabledDisabled(getParameterOrNull(parameters, "storageAutoGrow"));
        dbVersion = getParameterOrNull(parameters, "dbVersion");
    }

    private String toEnabledDisabled(boolean booleanToConvert) {
        return booleanToConvert ? "Enabled" : "Disabled";
    }

    private boolean fromEnabledDisabled(String stringToConvert) {
        return "Enabled".equalsIgnoreCase(stringToConvert);
    }
}
