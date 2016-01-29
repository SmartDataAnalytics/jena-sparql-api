package org.aksw.jena_sparql_api.convert;

import org.aksw.jena_sparql_api.geo.vocab.GEOSPARQL;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTWriter;

public class VocabWriterGeosparql
    implements VocabWriter<Geometry>
{
    protected WKTWriter wktWriter;
    protected boolean ignoreInvalidGeometry = false;
    protected Property property;
    protected Resource datatype;

    public VocabWriterGeosparql() {
        this.wktWriter = new WKTWriter();
        this.property = GEOSPARQL.asWKT;//.getURI();
        this.datatype = GEOSPARQL.Geometry;//.getURI();
    }

    @Override
    public void write(Model model, Resource base, Geometry value) {
        String wkt = wktWriter.write(value);
        //Property p = model.createProperty(property);
        RDFNode rdfNode = model.createTypedLiteral(wkt, datatype.getURI());
        model.add(base, property, rdfNode);
    }

}
