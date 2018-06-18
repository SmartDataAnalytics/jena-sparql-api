package org.aksw.jena_sparql_api.geo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aksw.jena_sparql_api.lookup.LookupService;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.distance.DistanceOp;

//class

public class LinkGeomizer {
    public static Map<Triple, Geometry> geomize(Iterable<Triple> triples, LookupService<Node, Geometry> lookupServiceSubjects, LookupService<Node, Geometry> lookupServiceObjects) {
        Map<Triple, Geometry> result = new HashMap<Triple, Geometry>();

        Set<Node> subjects = new HashSet<Node>();
        Set<Node> objects = new HashSet<Node>();

        for(Triple triple : triples) {
            subjects.add(triple.getSubject());
            objects.add(triple.getObject());
        }

        Map<Node, Geometry> mapSubjects = lookupServiceSubjects.fetchMap(subjects);
        Map<Node, Geometry> mapObjects = lookupServiceObjects.fetchMap(objects);

        System.out.println("mapSubjects: " + mapSubjects);
        System.out.println("mapObjects: " + mapObjects);

        GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

        for(Triple triple : triples) {
            Geometry gs = mapSubjects.get(triple.getSubject());
            Geometry go = mapObjects.get(triple.getObject());

            if(gs == null || go == null) {
                continue;
            }

            Coordinate[] coordinates = DistanceOp.nearestPoints(gs, go);

//            Point ps = GeometryFactory.createPointFromInternalCoord(points[0], gs);
//            Point po = GeometryFactory.createPointFromInternalCoord(points[1], go);

            LineString lineString = gf.createLineString(coordinates);
            result.put(triple, lineString);
        }

        return result;
    }
}
