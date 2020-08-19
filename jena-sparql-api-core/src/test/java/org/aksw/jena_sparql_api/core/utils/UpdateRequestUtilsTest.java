package org.aksw.jena_sparql_api.core.utils;

import org.aksw.jena_sparql_api.syntax.UpdateRequestUtils;
import org.apache.jena.query.Syntax;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;
import org.junit.Test;

public class UpdateRequestUtilsTest {

    @Test
    public void testFixVarNames() {
        String str =
                "PREFIX realestate: <http://geophy.io/ontologies/realestate#>\n" +
                "PREFIX wgs84: <http://www.w3.org/2003/01/geo/wgs84_pos#>\n" +
                "PREFIX system: <http://geophy.io/ontologies/system#>\n" +
                "PREFIX f: <geophy:function:>\n" +
                "PREFIX meta: <http://geophy.io/ontologies/rank/meta#>\n" +
                "DELETE {\n" +
                "  ?functional_area ?definition_key ?oldvalue .\n" +
                "}\n" +
                "INSERT {\n" +
                "  ?functional_area ?definition_key ?value .\n" +
                "}" +
                "WHERE\n" +
                "\n" +
                "    {  \n" +
                "      \n" +
                "      SELECT  ?functional_area ?purpose ?renovation_year ?construction_year\n" +
                "      WHERE\n" +
                "        { \n" +
                "          VALUES ?building {   <http://geophy.io/buildings/55881>     }\n" +
                "          ?building  realestate:construction_year  ?con_year\n" +
                "          \n" +
                "        }\n" +
                "}";

        UpdateRequest ur = UpdateFactory.create(str, Syntax.syntaxSPARQL_11);
        UpdateRequestUtils.fixVarNames(ur);

        System.out.println(ur);
    }
}
