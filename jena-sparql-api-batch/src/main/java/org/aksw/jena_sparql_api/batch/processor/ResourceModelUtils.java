package org.aksw.jena_sparql_api.batch.processor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

public class ResourceModelUtils {
    public Model filterConnected(Model m, Resource r) {
        Deque<Resource> open = new ArrayDeque<Resource>();
        Set<Resource> seen = new HashSet<Resource>();

        open.add(r);
        seen.add(r);

        Model result = ModelFactory.createDefaultModel();

        while(!open.isEmpty()) {
            Resource s = open.pop();

            Set<Statement> stmts = m.listStatements(s, null, (RDFNode)null).toSet();
            for(Statement stmt : stmts) {

                result.add(stmt);

                RDFNode o = stmt.getObject();
                if(o.isResource()) {
                    Resource x = o.asResource();
                    boolean isVisited = seen.contains(x);
                    if(!isVisited) {
                        seen.add(x);
                        open.push(x);
                    }
                }
            }
        }

        return result;
    }

//    public Graph filter(Graph m, Node n) {
//        Set<Resource> open = new HashSet<Resource>();
//        Set<Resource> visited = new HashSet<Resource>();
//
//        open.add(n);
//
//        while(!open.isEmpty()) {
//
//        }
//
//    }
}
