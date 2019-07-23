package com.sequenceiq.redbeams.api.endpoint.v4.stacks.azure;

import static org.junit.Assert.assertEquals;

import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AzureDatabaseServerV4ParametersTest {

    private AzureDatabaseServerV4Parameters underTest;

    @Before
    public void setUp() {
        underTest = new AzureDatabaseServerV4Parameters();
    }

    @Test
    public void testGettersAndSetters() {
        underTest.setBackupRetentionDays(3);
        assertEquals(3, underTest.getBackupRetentionDays().intValue());

        underTest.setDbVersion("1.2.3");
        assertEquals("1.2.3", underTest.getDbVersion());
    }

    @Test
    public void testAsMap() {
        underTest.setBackupRetentionDays(3);
        underTest.setDbVersion("1.2.3");

        Map<String, Object> map = underTest.asMap();

        assertEquals(3, ((Integer) map.get("backupRetentionDays")).intValue());
        assertEquals("1.2.3", map.get("dbVersion"));
    }

    @Test
    public void testGetCloudPlatform() {
        assertEquals(CloudPlatform.AZURE, underTest.getCloudPlatform());
    }

    @Test
    public void testParse() {
        Map<String, Object> parameters = Map.of("backupRetentionDays", 3, "dbVersion", "1.2.3");

        underTest.parse(parameters);

        assertEquals(3, underTest.getBackupRetentionDays().intValue());
        assertEquals("1.2.3", underTest.getDbVersion());
    }

}
