package org.aksw.jena_sparql_api.convert;

import org.aksw.jena_sparql_api.geo.vocab.GEO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTWriter;

public class VocabWriterWgs84
    implements VocabWriter<Geometry>
{
    protected boolean ignoreInvalidGeometry = false;
    protected String datatypeUri; // standard compliance is xsd:string, however, numeric formats are encountered in the wild as well

    public VocabWriterWgs84() {
        this.datatypeUri = XSD.xstring.getURI();
        //WKTWriter wktWriter;
    }

    @Override
    public void write(Model model, Resource base, Geometry value) {
        if(value instanceof Point) {
            Point point = (Point)value;
            double x = point.getX();
            double y = point.getY();
            
            RDFNode xlong = model.createTypedLiteral(x, datatypeUri);
            RDFNode lat = model.createTypedLiteral(y, datatypeUri);
            
            model.add(base, GEO.xlong, xlong);
            model.add(base, GEO.lat, lat);            
        } else {
            if(!ignoreInvalidGeometry) {
                throw new RuntimeException("A point geometry was expected, got " + value);
            }
        }
    }

}
