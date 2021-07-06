package org.aksw.jena_sparql_api.sparql.ext.osrm;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.aksw.jena_sparql_api.sparql.ext.geosparql.F_ParsePolyline;
import org.aksw.jena_sparql_api.sparql.ext.json.RDFDatatypeJson;
import org.aksw.jena_sparql_api.sparql.ext.url.E_UrlText;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.geosparql.implementation.GeometryWrapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.sparql.core.Substitute;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.pfunction.PropFuncArg;
import org.apache.jena.sparql.pfunction.PropertyFunctionBase;
import org.apache.jena.sparql.util.NodeFactoryExtra;
import org.locationtech.jts.geom.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * {
 *    Bind("['foo', 'bar']"^^xsd:json As ?json)
 *    ?json json:array ?items.
 * }
 *
 * @author raven
 *
 */
public class OsrmRoutePF extends PropertyFunctionBase {
    private static Logger log = LoggerFactory.getLogger(OsrmRoutePF.class) ;

    @Override
    public void build(PropFuncArg argSubject, Node predicate, PropFuncArg argObject, ExecutionContext execCxt) {
        if (argSubject.isList()) {
            int size = argSubject.getArgListSize();
            if (size != 3) {
                throw new QueryBuildException("Subject has " + argSubject.getArgList().size() + " elements, must be 3: " + argSubject);
            }
        }

        if (argObject.isList()) {
            List<Node> list = argObject.getArgList() ;

            if (list.size() != 3) {
                throw new QueryBuildException("Too many arguments in list : " + list) ;
            }
        }
    }

    @Override
    public QueryIterator exec(Binding binding,
                              PropFuncArg argSubject,
                              Node node,
                              PropFuncArg argObject,
                              ExecutionContext executionContext) {

        argSubject = Substitute.substitute(argSubject, binding) ;
        argObject = Substitute.substitute(argObject, binding) ;

        Node route = null;
        Node distance = null;
        Node duration = null;

        if (argSubject.isList()) {
            // Length checked in build()
            route = argSubject.getArg(0);
            distance = argSubject.getArg(1);
            duration = argSubject.getArg(2);

        } else {
            route = argSubject.getArg() ;
        }

        Node url = argObject.getArg(0);
        Node src = argObject.getArg(1);
        Node tgt = argObject.getArg(2);

        return query(binding, route, distance, duration, url, src, tgt, executionContext);
    }

    F_ParsePolyline parser = new F_ParsePolyline();

    private QueryIterator query(Binding binding, Node route, Node distance, Node duration, Node url, Node src, Node tgt, ExecutionContext execCxt) {
        Point p1 = (Point)((GeometryWrapper) src.getLiteralValue()).getParsingGeometry();
        Point p2 = (Point)((GeometryWrapper) tgt.getLiteralValue()).getParsingGeometry();
        String request = String.format("%s%,.10f,%,.4f;%,.4f,%,.4f?alternatives=2", url.toString(), p1.getX(), p1.getY(), p2.getX(), p2.getY());
        log.trace("OSRM request: {}", request);
        System.out.println(request);
        Iterator<Binding> bIter;
        try {
            NodeValue result = E_UrlText.resolve(NodeValue.makeNode(NodeFactory.createURI(request)));

            JsonObject json = RDFDatatypeJson.INSTANCE.getGson().fromJson(result.asNode().getLiteral().getLexicalForm(), JsonObject.class);
            JsonArray routes = json.getAsJsonObject().get("routes").getAsJsonArray();

            Function<JsonElement, Binding> converter = (JsonElement elt) -> {
                JsonObject routeObj = elt.getAsJsonObject();
                String routeStr = routeObj.get("geometry").getAsString();
                Node routeNode = parser.exec(NodeValue.makeString(routeStr)).asNode();
                double distanceVal = routeObj.get("distance").getAsDouble();
                double durationVal =  routeObj.get("duration").getAsDouble();
                Node durationNode = NodeValue.makeDuration(Duration.ofSeconds((int) durationVal).toString()).asNode();

                BindingBuilder bmap = Binding.builder(binding);
                bmap.add(Var.alloc(route.getName()), routeNode);
                bmap.add(Var.alloc(distance.getName()), NodeFactoryExtra.doubleToNode(distanceVal));
                bmap.add(Var.alloc(duration.getName()), durationNode);
                return bmap.build();
            };

            bIter = Iter.map(routes.iterator(), converter);
        } catch (Exception e) {
            e.printStackTrace();
            bIter = Iter.empty();
        }
        QueryIterator qIter = QueryIterPlainWrapper.create(bIter, execCxt);
        return qIter ;
    }
}