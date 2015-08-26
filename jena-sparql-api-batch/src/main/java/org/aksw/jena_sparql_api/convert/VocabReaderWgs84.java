package org.aksw.jena_sparql_api.convert;

import java.util.List;

import org.aksw.jena_sparql_api.geo.vocab.GEO;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class VocabReaderWgs84
    implements VocabReader<Geometry>
{
    private GeometryFactory geometryFactory;
    
    
    public VocabReaderWgs84() {
        this(new GeometryFactory());
    }
    
    public VocabReaderWgs84(GeometryFactory geometryFactory) {
        this.geometryFactory = geometryFactory;
    }

    /**
     * Fore a double value either from a number or a string
     * 
     * @param o
     * @return
     */
    public static Double extractDouble(Object o) {
        Double result;

        if(o == null) {
            result = null;
        } else if(o instanceof Number) {
            Number n = (Number)o;
            result = n.doubleValue();
        } else {
            String str = o.toString();
            result = Double.parseDouble(str);
        }
        
        return result;
    }
    
    @Override
    public Geometry read(Model model, Resource base) {
        List<RDFNode> xlongs = model.listObjectsOfProperty(base, GEO.xlong).toList();
        List<RDFNode> lats = model.listObjectsOfProperty(base, GEO.lat).toList();

        Point result;
        
        if(xlongs.isEmpty() && lats.isEmpty()) {
            result = null;
        } else if(xlongs.size() == 1 && lats.size() == 1) {
            
            RDFNode xlong = xlongs.get(0);
            RDFNode lat = lats.get(0);
         
            Object ox = xlong.asLiteral().getValue();
            Object oy = lat.asLiteral().getValue();
            
            double x = extractDouble(ox);
            double y = extractDouble(oy);
    
            result = geometryFactory.createPoint(new Coordinate(x, y));
        } else {
            // Invalid geometry
            // TODO Add support for an error callback
            result = null;
        }

        return result;
    }
}
