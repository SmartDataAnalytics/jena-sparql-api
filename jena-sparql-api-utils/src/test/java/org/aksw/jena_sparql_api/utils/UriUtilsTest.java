package org.aksw.jena_sparql_api.utils;

import org.junit.Assert;
import org.junit.Test;


public class UriUtilsTest {

    @Test
    public void test() {
        String r = UriUtils.replaceNamespace("http://example.org/resource/node123", "geometry");
        Assert.assertEquals("http://example.org/geometry/node123", r);
    }

}
