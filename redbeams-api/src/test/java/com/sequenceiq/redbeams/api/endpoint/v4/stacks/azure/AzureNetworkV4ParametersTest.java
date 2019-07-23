package com.sequenceiq.redbeams.api.endpoint.v4.stacks.azure;

import static org.junit.Assert.assertEquals;

import com.sequenceiq.cloudbreak.common.mappable.CloudPlatform;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class AzureNetworkV4ParametersTest {

    private AzureNetworkV4Parameters underTest;

    @Before
    public void setUp() {
        underTest = new AzureNetworkV4Parameters();
    }

    @Test
    public void testGettersAndSetters() {
        underTest.setVirtualNetwork("someVirtualNetwork");
        assertEquals("someVirtualNetwork", underTest.getVirtualNetwork());
    }

    @Test
    public void testAsMap() {
        underTest.setVirtualNetwork("someVirtualNetwork");

        Map<String, Object> map = underTest.asMap();

        assertEquals("someVirtualNetwork", map.get("virtualNetwork"));
    }

    @Test
    public void testGetCloudPlatform() {
        assertEquals(CloudPlatform.AZURE, underTest.getCloudPlatform());
    }

    @Test
    public void testParse() {
        Map<String, Object> parameters = Map.of("virtualNetwork", "someVirtualNetwork");

        underTest.parse(parameters);

        assertEquals("someVirtualNetwork", underTest.getVirtualNetwork());
    }

}
