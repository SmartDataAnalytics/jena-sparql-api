package org.aksw.jena_sparql_api.web.utils;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;

import org.aksw.jena_sparql_api.utils.DatasetDescriptionUtils;
import org.springframework.web.bind.ServletRequestUtils;

import org.apache.jena.sparql.core.DatasetDescription;

public class DatasetDescriptionRequestUtils {
    public static DatasetDescription extractDatasetDescriptionAny(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionQuery(req);
        DatasetDescription b = extractDatasetDescriptionUpdate(req);
        DatasetDescriptionUtils.mergeInto(result, b);

        return result;
    }

    public static DatasetDescription extractDatasetDescriptionQuery(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionCommon(req, "default-graph-uri", "named-graph-uri");
        return result;
    }

    public static DatasetDescription extractDatasetDescriptionUpdate(HttpServletRequest req) {
        DatasetDescription result = extractDatasetDescriptionCommon(req, "using-graph-uri", "using-named-graph-uri");
        return result;
    }

    public static DatasetDescription extractDatasetDescriptionCommon(HttpServletRequest req, String dguParamName, String nguParamName) {
        DatasetDescription result = new DatasetDescription();
        result.addAllDefaultGraphURIs(Arrays.asList(ServletRequestUtils.getStringParameters(req, dguParamName)));
        result.addAllNamedGraphURIs(Arrays.asList(ServletRequestUtils.getStringParameters(req, nguParamName)));
        return result;
    }
}
