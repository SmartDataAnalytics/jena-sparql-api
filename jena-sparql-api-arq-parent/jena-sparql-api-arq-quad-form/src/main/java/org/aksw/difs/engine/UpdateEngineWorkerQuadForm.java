package org.aksw.difs.engine;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.atlas.data.BagFactory;
import org.apache.jena.atlas.data.DataBag;
import org.apache.jena.atlas.data.ThresholdPolicy;
import org.apache.jena.atlas.data.ThresholdPolicyFactory;
import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.engine.Plan;
import org.apache.jena.sparql.engine.QueryEngineFactory;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingRoot;
import org.apache.jena.sparql.modify.UpdateEngineWorker;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementNamedGraph;
import org.apache.jena.sparql.system.SerializationFactoryFinder;
import org.apache.jena.sparql.util.Context;

public class UpdateEngineWorkerQuadForm
    extends UpdateEngineWorker
{
    protected QueryEngineFactory queryEngineFactory;

    public UpdateEngineWorkerQuadForm(DatasetGraph datasetGraph, Binding inputBinding, Context context) {
        this(datasetGraph, inputBinding, context, QueryEngineMainQuadForm.FACTORY);
    }

    public UpdateEngineWorkerQuadForm(DatasetGraph datasetGraph, Binding inputBinding, Context context, QueryEngineFactory queryEngineFactory) {
        super(datasetGraph, inputBinding, context);
        this.queryEngineFactory = queryEngineFactory;
    }

    protected Iterator<Binding> evalBindings(Element pattern) {
        Query query = elementToQuery(pattern);
        return evalBindings2(query, datasetGraph, inputBinding, context);
    }

    protected Iterator<Binding> evalBindings2(Query query, DatasetGraph dataset, Binding inputBinding, Context context) {

        // The UpdateProcessorBase already copied the context and made it safe
        // ... but that's going to happen again :-(

        Iterator<Binding> toReturn;
        if ( query != null ) {
            Plan plan = queryEngineFactory.create(query, datasetGraph, inputBinding, context);
            toReturn = plan.iterator();
        } else {
            toReturn = Iter.singleton((null != inputBinding) ? inputBinding : BindingRoot.create());
        }

        return toReturn;
    }

    /** The whole method needed to be copied because the base implementation depends on the
     * static evalBindings functions which prevents forcing the specified queryEngineFactory */
    @Override
    public void visit(UpdateModify update) {
        Node withGraph = update.getWithIRI();
        Element elt = update.getWherePattern();

        // null or a dataset for USING clause.
        // USING/USING NAMED
        DatasetGraph dsg = processUsing(update);

        // -------------------
        // WITH
        // USING overrides WITH
        if ( dsg == null && withGraph != null ) {
            // Subtle difference : WITH <uri>... WHERE {}
            // and an empty/unknown graph <uri>
            //   rewrite with GRAPH -> no match.
            //   redo as dataset with different default graph -> match
            // SPARQL is unclear about what happens when the graph does not exist.
            //   but the rewrite with ElementNamedGraph is closer to SPARQL.
            // Better, treat as
            // WHERE { GRAPH <with> { ... } }
            // This is the SPARQL wording (which is a bit loose).
            elt = new ElementNamedGraph(withGraph, elt) ;
        }

        // WITH :
        // The quads from deletion/insertion are altered when streamed
        // into the templates later on.

        // -------------------

        if ( dsg == null )
            dsg = datasetGraph ;

        Query query = elementToQuery(elt) ;
        ThresholdPolicy<Binding> policy = ThresholdPolicyFactory.policyFromContext(datasetGraph.getContext());
        DataBag<Binding> db = BagFactory.newDefaultBag(policy, SerializationFactoryFinder.bindingSerializationFactory()) ;
        try {
            Iterator<Binding> bindings = evalBindings2(query, dsg, inputBinding, context);

            if ( false ) {
                List<Binding> x = Iter.toList(bindings);
                System.out.printf("====>> Bindings (%d)\n", x.size());
                Iter.print(System.out, x.iterator());
                System.out.println("====<<");
                bindings = Iter.iter(x);
            }
            db.addAll(bindings);
            Iter.close(bindings);

            Iterator<Binding> it = db.iterator();
            execDelete(datasetGraph, update.getDeleteQuads(), withGraph, it);
            Iter.close(it);

            Iterator<Binding> it2 = db.iterator();
            execInsert(datasetGraph, update.getInsertQuads(), withGraph, it2);
            Iter.close(it2);
        }
        finally {
            db.close();
        }
    }
}
