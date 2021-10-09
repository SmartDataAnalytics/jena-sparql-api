package org.aksw.jena_sparql_api.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.aksw.jena_sparql_api.rdf.model.ext.dataset.api.ResourceInDataset;
import org.aksw.jena_sparql_api.rdf.model.ext.dataset.impl.ResourceInDatasetImpl;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.E_Conditional;
import org.apache.jena.sparql.expr.E_Function;
import org.apache.jena.sparql.expr.E_IRI;
import org.apache.jena.sparql.expr.E_IsBlank;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprTransformSubstitute;
import org.apache.jena.sparql.expr.ExprTransformer;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.graph.NodeTransformLib;
import org.apache.jena.sparql.util.ExprUtils;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.WrappedIterator;

/** Note transforms not captured by {@link NodeTransformLib} such as Bindings, Graphs, Models, Datsets, ... */
public class NodeTransformLib2 {

    /** Wrap a node transform such the input node is returned whenever otherwise null would be returned */
    public static NodeTransform makeNullSafe(NodeTransform xform) {
        return x -> {
            Node tmp = xform.apply(x);
            Node r = tmp == null ? x : tmp;
            return r;
        };
    }


    public static Binding transformValues(Binding b, NodeTransform transform) {
        BindingBuilder bb = BindingBuilder.create();
        List<Var> vars = Iter.toList(b.vars()) ;
        for (Var v : vars) {
            Node before = b.get(v);
            Node after = transform.apply(before);
            bb.add(v, after);
        }
        Binding result = bb.build();
        return result;
    }

    // The following method is still part of QueryExecutionTransformResult because it needs our version of ResultSetCloseable
//	public static ResultSet applyNodeTransform(NodeTransform nodeTransform, ResultSet rs) {
//	    Closeable closeable = rs instanceof Closeable ? (Closeable)rs : null;
//	    List<String> vars = rs.getResultVars();
//
//	    ExtendedIterator<Binding> it = WrappedIterator.create(new IteratorResultSetBinding(rs))
//	        .mapWith(b -> transformValues(b, nodeTransform));
//
//	    QueryIter queryIter = new QueryIterPlainWrapper(it);
//	    ResultSet core = ResultSetFactory.create(queryIter, vars);
//
//	    ResultSet result = new ResultSetCloseable(core, closeable);
//	    return result;
//	}


    public static Graph copyWithNodeTransform(NodeTransform nodeTransform, Graph graph) {
        Graph result = GraphFactory.createDefaultGraph();
        graph.find().mapWith(t -> NodeTransformLib.transform(nodeTransform, t))
            .forEachRemaining(result::add);
        return result;
    }

    public static Graph copyWithNodeTransform(NodeTransform nodeTransform, Graph in, Graph out) {
        if (in == out) {
            applyNodeTransform(nodeTransform, in);
        } else {
            in.find().mapWith(t -> NodeTransformLib.transform(nodeTransform, t))
                .forEachRemaining(out::add);
        }
        return out;
    }

    public static Graph applyNodeTransform(NodeTransform nodeTransform, Graph graph) {
        List<Triple> inserts = new ArrayList<>();

        ExtendedIterator<Triple> it = graph.find();
        try {
            while(it.hasNext()) {
                Triple before = it.next();
                Triple after = NodeTransformLib.transform(nodeTransform, before);
                if(!after.equals(before)) {
                    it.remove();
                    inserts.add(after);
                }
            }
        } finally {
            it.close();
        }

        for(Triple t : inserts) {
            graph.add(t);
        }
        return graph;
    }

    public static RDFNode applyNodeTransform(NodeTransform nodeTransform, RDFNode rdfNode) {
        Model model = rdfNode.getModel();
        applyNodeTransform(nodeTransform, model);
        Node beforeNode = rdfNode.asNode();
        Node tmp = nodeTransform.apply(beforeNode);
        Node afterNode = tmp == null ? beforeNode : tmp;
        RDFNode result = model.asRDFNode(afterNode);

        return result;
    }

    public static RDFNode copyWithNodeTransform(NodeTransform nodeTransform, RDFNode rdfNode) {
        Model beforeModel = rdfNode.getModel();
        Model afterModel = copyWithNodeTransform(nodeTransform, beforeModel);
        Node beforeNode = rdfNode.asNode();
        Node tmp = nodeTransform.apply(beforeNode);
        Node afterNode = tmp == null ? beforeNode : tmp;
        RDFNode result = afterModel.asRDFNode(afterNode);

        return result;
    }

    public static Model applyNodeTransform(NodeTransform nodeTransform, Model model) {
        Graph oldGraph = model.getGraph();
        applyNodeTransform(nodeTransform, oldGraph);
        return model;
    }

    public static Model copyWithNodeTransform(NodeTransform nodeTransform, Model model) {
        Graph oldGraph = model.getGraph();
        Graph newGraph = copyWithNodeTransform(nodeTransform, oldGraph);
        Model result = ModelFactory.createModelForGraph(newGraph);
        return result;
    }

    // Performs an in-place transform
    public static DatasetGraph applyNodeTransform(NodeTransform nodeTransform, DatasetGraph dg) {

        List<Quad> quads = new ArrayList<>();
        WrappedIterator.create(dg.find())
            .mapWith(q -> NodeTransformLib.transform(nodeTransform, q))
            .forEachRemaining(quads::add);

        dg.clear();
        for(Quad quad : quads) {
            dg.add(quad);
        }

        return dg;
    }


    public static DatasetGraph copyWithNodeTransform(NodeTransform nodeTransform, DatasetGraph graph) {
        DatasetGraph result = DatasetGraphFactory.create();
        copyWithNodeTransform(nodeTransform, graph, result);
        return result;
    }

    public static DatasetGraph copyWithNodeTransform(NodeTransform nodeTransform, DatasetGraph in, DatasetGraph out) {
        if (in == out) {
            applyNodeTransform(nodeTransform, in);
        } else {
            WrappedIterator.create(in.find())
                .mapWith(t -> NodeTransformLib.transform(nodeTransform, t))
                .forEachRemaining(out::add);
        }
        return out;
    }



    public static Dataset applyNodeTransform(NodeTransform nodeTransform, Dataset dataset) {
        DatasetGraph dg = dataset.asDatasetGraph();
        applyNodeTransform(nodeTransform, dg);
        return dataset;
    }

    public static Dataset copyWithNodeTransform(NodeTransform nodeTransform, Dataset graph) {
        Dataset result = DatasetFactory.create();
        copyWithNodeTransform(nodeTransform, graph, result);
        return result;
    }

    public static Dataset copyWithNodeTransform(NodeTransform nodeTransform, Dataset in, Dataset out) {
        if (in == out) {
            applyNodeTransform(nodeTransform, in);
        } else {
            copyWithNodeTransform(nodeTransform, in.asDatasetGraph(), out.asDatasetGraph());
        }
        return out;
    }




    public static <T> ExtendedIterator<T> map(Iterator<T> it, Function<? super T, ? extends T> xform) {
        ExtendedIterator<T> result = WrappedIterator.create(it).mapWith(x -> xform.apply(x));
        return result;
    }


    /**
     * Create a conditional expression checks whether its argument is a blank node,
     * and if so, extracts its bnode label and forwards it to 'bnodeLabelTransform'.
     *
     * IF(isBlank(?x), bnodeLabelTransform(afn:bnode(?x)), ?x)
     *
     * @param bnodeLabelTransform
     * @param v
     * @return
     */
    public static Expr addBnodeCheckAndTransform(Expr bnodeLabelTransform, Var v) {
        ExprVar ev = new ExprVar(v);
        Expr bnodeLabelOf = new E_Function("http://jena.apache.org/ARQ/function#bnode", new ExprList(ev));
        Expr e2 = ExprTransformer.transform(new ExprTransformSubstitute(v, bnodeLabelOf), bnodeLabelTransform);
        Expr result = new E_Conditional(new E_IsBlank(ev), new E_IRI(e2), ev);
        return result;
    }

    /**
     * Create a node transform from an expression that maps bnode label strings
     * to strings that can be used as IRIS in a query.
     *
     * An example for such an expression is CONCAT('_:', ?x )
     *
     * @param bnodeLabelTransform
     * @param v
     * @return
     */
    public static NodeTransform createBnodeLabelTransform(Expr bnodeLabelTransform, Var v) {
        Expr conditionalExpr = addBnodeCheckAndTransform(bnodeLabelTransform, v);
        NodeTransform result = createNodeTransform(conditionalExpr, v);
        return result;
    }

    /**
     * Create a node transformer from an expression
     *
     * @param expr
     * @param v
     * @return
     */
    public static NodeTransform createNodeTransform(Expr expr, Var v) {
        return x -> {
            Binding b = BindingFactory.binding(v, x);
            NodeValue nv = ExprUtils.eval(expr, b);
            Node r = nv.asNode();
            return r;
        };

//		return x -> {
//			Node r;
//			if(x.isBlank()) {
//				String str = x.getBlankNodeLabel();
//				Node node = NodeFactory.createLiteral(str);
//				Binding b = BindingFactory.binding(v, node);
//				NodeValue nv = ExprUtils.eval(expr, b);
//				r = nv.asNode();
//			} else {
//				r = x;
//			}
//			return r;
//		}
    }



    /**
     * Rename multiple RDFterms
     *
     * @param old
     * @param renames
     * @return
     */
    public static ResourceInDataset applyNodeTransform(ResourceInDataset old, NodeTransform nodeTransform) {
        String graphName = old.getGraphName();
        Node graphNode = NodeFactory.createURI(graphName);
        Node newGraphNode = Optional.ofNullable(nodeTransform.apply(graphNode)).orElse(graphNode);

        Node n = old.asNode();
        Node newNode = Optional.ofNullable(nodeTransform.apply(n)).orElse(n);

        String g = newGraphNode.getURI();

        Dataset dataset = old.getDataset();
        NodeTransformLib2.applyNodeTransform(nodeTransform, dataset);

        ResourceInDataset result = new ResourceInDatasetImpl(dataset, g, newNode);
        return result;

    }


    public static ResourceInDataset copyWithNodeTransform(ResourceInDataset old, Dataset target, NodeTransform nodeTransform) {
        String graphName = old.getGraphName();
        Node graphNode = NodeFactory.createURI(graphName);
        Node newGraphNode = Optional.ofNullable(nodeTransform.apply(graphNode)).orElse(graphNode);
        String g = newGraphNode.getURI();

        Node n = old.asNode();
        Node newNode = Optional.ofNullable(nodeTransform.apply(n)).orElse(n);

        NodeTransformLib2.copyWithNodeTransform(nodeTransform, old.getDataset(), target);

        ResourceInDataset result = new ResourceInDatasetImpl(target, g, newNode);
        return result;

    }
}
