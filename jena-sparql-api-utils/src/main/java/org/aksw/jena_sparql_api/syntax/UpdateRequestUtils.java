package org.aksw.jena_sparql_api.syntax;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.aksw.commons.collections.diff.Diff;
import org.aksw.jena_sparql_api.util.SetFromDatasetGraph;
import org.aksw.jena_sparql_api.util.SetFromGraph;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.PrefixUtils;
import org.aksw.jena_sparql_api.utils.PrologueUtils;
import org.aksw.jena_sparql_api.utils.transform.NodeTransformCollectNodes;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Lists;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.DatasetDescription;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.NodeTransform;
import org.apache.jena.sparql.modify.request.QuadDataAcc;
import org.apache.jena.sparql.modify.request.UpdateData;
import org.apache.jena.sparql.modify.request.UpdateDataDelete;
import org.apache.jena.sparql.modify.request.UpdateDataInsert;
import org.apache.jena.sparql.modify.request.UpdateDeleteInsert;
import org.apache.jena.sparql.modify.request.UpdateModify;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.util.PrefixMapping2;
import org.apache.jena.update.Update;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateRequest;


public class UpdateRequestUtils {


    public static UpdateRequest optimizePrefixes(UpdateRequest updateRequest) {
        optimizePrefixes(updateRequest, null);
        return updateRequest;
    }

    /**
     * In-place optimize an update request's prefixes to only used prefixes
     * The global prefix map may be null.
     *
     * @param query
     * @param pm
     * @return
     */
    public static UpdateRequest optimizePrefixes(UpdateRequest updateRequest, PrefixMapping globalPm) {
        PrefixMapping usedPrefixes = UpdateRequestUtils.usedPrefixes(updateRequest, globalPm);
        updateRequest.setPrefixMapping(usedPrefixes);
        return updateRequest;
    }


    public static PrefixMapping usedPrefixes(UpdateRequest updateRequest, PrefixMapping global) {
        PrefixMapping local = updateRequest.getPrefixMapping();
        PrefixMapping pm = global == null ? local : new PrefixMapping2(global, local);
        PrefixMapping result = usedReferencePrefixes(updateRequest, pm);
        return result;
    }

    /**
     * Determine used prefixes within the given prefix mapping.
     * The update request's own prefixes are ignored.
     *
     * @param query
     * @param pm
     * @return
     */
    public static PrefixMapping usedReferencePrefixes(UpdateRequest updateRequest, PrefixMapping pm) {
        NodeTransformCollectNodes nodeUsageCollector = new NodeTransformCollectNodes();

        applyNodeTransform(updateRequest, nodeUsageCollector);
        Set<Node> nodes = nodeUsageCollector.getNodes();

        PrefixMapping result = PrefixUtils.usedPrefixes(pm, nodes);
        return result;
    }

    public static UpdateRequest applyNodeTransform(UpdateRequest updateRequest, NodeTransform nodeTransform) {
        UpdateRequest result = UpdateRequestUtils.copyTransform(updateRequest, update -> {
            Update r = UpdateUtils.applyNodeTransform(update, nodeTransform);
            return r;
        });

        return result;
    }

//    public static PrefixMapping usedPrefixes(UpdateRequest updateRequest) {
//        NodeTransformCollectNodes nodeUsageCollector = new NodeTransformCollectNodes();
//
//        //applyNodeTransform(updateRequest, nodeUsageCollector);
//        Set<Node> nodes = nodeUsageCollector.getNodes();
//
//        PrefixMapping pm = query.getPrefixMapping();
//        Map<String, String> usedPrefixes = nodes.stream()
//                .filter(Node::isURI)
//                .map(Node::getURI)
//                .map(x -> {
//                    String tmp = pm.shortForm(x);
//                    String r = Objects.equals(x, tmp) ? null : tmp.split(":", 2)[0];
//                    return r;
//                })
//                //.peek(System.out::println)
//                .filter(x -> x != null)
//                .distinct()
//                .collect(Collectors.toMap(x -> x, pm::getNsPrefixURI));
//
//        PrefixMapping result = new PrefixMappingImpl();
//        result.setNsPrefixes(usedPrefixes);
//        return result;
//    }

    public static UpdateRequest applyTransformElt(UpdateRequest updateRequest, Function<? super Element, ? extends Element> transform) {
        UpdateRequest result = UpdateRequestUtils.copyTransform(updateRequest, update -> {
            Update r = UpdateUtils.applyElementTransform(update, transform);
            return r;
        });

        return result;
    }

    public static UpdateRequest applyOpTransform(UpdateRequest updateRequest, Function<? super Op, ? extends Op> transform) {
        UpdateRequest result = UpdateRequestUtils.copyTransform(updateRequest, update -> {
            Update r = UpdateUtils.applyOpTransform(update, transform);
            return r;
        });

        return result;
    }

    /**
     * Append operations from src to tgt
     * @param tgt
     * @param src
     * @return
     */
    public static UpdateRequest append(UpdateRequest tgt, UpdateRequest src) {
        src.getOperations().forEach(tgt::add);
        return tgt;
    }

    public static UpdateRequest clone(UpdateRequest request) {
        UpdateRequest result = copyTransform(request, UpdateUtils::clone);
        return result;
    }

//    	UpdateRequest result = new UpdateRequest();
//        result.setBaseURI(request.getBaseURI());
//        result.setPrefixMapping(request.getPrefixMapping());
//        result.setResolver(request.getResolver());
//
//        for(Update update : request.getOperations()) {
//            Update clone = UpdateUtils.clone(update);
//            result.add(clone);
//        }
//        return result;
//    }

    public static UpdateRequest copyTransform(UpdateRequest request, Function<? super Update, ? extends Update> updateTransform) {
        UpdateRequest result = new UpdateRequest();
        PrologueUtils.copy(result, request);

        for(Update update : request.getOperations()) {
            Update newUpdate = updateTransform.apply(update);
            if(newUpdate != null) {
            //Update clone = UpdateUtils.clone(update);
                result.add(newUpdate);
            }
        }
        return result;
    }

    public static UpdateRequest copyWithIri(UpdateRequest updateRequest, String withIri, boolean substituteDefaultGraph) {
        UpdateRequest result = copyTransform(updateRequest, update -> UpdateUtils.copyWithIri(update, withIri, substituteDefaultGraph));
        return result;
    }

    public static UpdateRequest copyWithIri(UpdateRequest updateRequest, Node withIri, boolean substituteDefaultGraph) {
        UpdateRequest result = copyTransform(updateRequest, update -> UpdateUtils.copyWithIri(update, withIri, substituteDefaultGraph));
        return result;
    }

    public static void applyWithIri(UpdateRequest updateRequest, String withIri) {
        for(Update update : updateRequest.getOperations()) {
            UpdateUtils.applyWithIriIfApplicable(update, withIri);
        }
    }

    public static void applyDatasetDescription(UpdateRequest updateRequest, DatasetDescription dg) {
        for(Update update : updateRequest.getOperations()) {
            UpdateUtils.applyDatasetDescriptionIfApplicable(update, dg);
        }
    }

    public static void fixVarNames(UpdateRequest updateRequest) {
        List<Update> updates = updateRequest.getOperations();

        for(Update update : updates) {
            if(update instanceof UpdateDeleteInsert) {
                UpdateDeleteInsert x = (UpdateDeleteInsert)update;
                Element before = x.getWherePattern();
                Element after = ElementUtils.fixVarNames(before);
                x.setElement(after);
            } else if(update instanceof UpdateModify) {
                UpdateModify x = (UpdateModify)update;
                Element before = x.getWherePattern();
                Element after = ElementUtils.fixVarNames(before);
                x.setElement(after);
            }
        }
    }


    public static UpdateRequest createUpdateRequestDatasetGraph(Diff<? extends DatasetGraph> diff)
    {
        Iterator<Quad> a = diff.getAdded().find();
        Iterator<Quad> b = diff.getRemoved().find();

        UpdateRequest result = createUpdateRequest(a, b);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Diff<? extends Iterable<? extends Quad>> diff)
    {
        UpdateRequest result = createUpdateRequest(diff.getAdded(), diff.getRemoved());
        return result;
    }

    public static UpdateRequest createUpdateRequest(Model added, Model removed)
    {
        Set<Triple> _a = added == null ? Collections.<Triple>emptySet() : SetFromGraph.wrap(added.getGraph());
        Set<Triple> _r = removed == null ? Collections.<Triple>emptySet() :  SetFromGraph.wrap(removed.getGraph());

        Iterable<Quad> a = Iterables.transform(_a, t -> new Quad(Quad.defaultGraphIRI, t));
        Iterable<Quad> r = Iterables.transform(_r, t -> new Quad(Quad.defaultGraphIRI, t));

        UpdateRequest result = createUpdateRequest(a, r);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Dataset added, Dataset removed)
    {
        Iterable<Quad> a = added == null ? Collections.emptySet() : SetFromDatasetGraph.wrap(added.asDatasetGraph());
        Iterable<Quad> r = removed == null ? Collections.emptySet() :  SetFromDatasetGraph.wrap(removed.asDatasetGraph());

        UpdateRequest result = createUpdateRequest(a, r);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Iterable<? extends Quad> added, Iterable<? extends Quad> removed) {
        Iterator<? extends Quad> a = added == null ? null : added.iterator();
        Iterator<? extends Quad> b = removed == null ? null : removed.iterator();

        UpdateRequest result = createUpdateRequest(a, b);
        return result;
    }

    public static UpdateRequest createUpdateRequest(Iterator<? extends Quad> added, Iterator<? extends Quad> removed) {
        UpdateRequest result = new UpdateRequest();

        if(added != null && added.hasNext()) {
            QuadDataAcc insertQuads = new QuadDataAcc(Lists.newArrayList(added));
            UpdateData insertData = new UpdateDataInsert(insertQuads);
            result.add(insertData);
        }

        if(removed != null && removed.hasNext()) {
            QuadDataAcc deleteQuads = new QuadDataAcc(Lists.newArrayList(removed));
            UpdateData deleteData = new UpdateDataDelete(deleteQuads);
            result.add(deleteData);
        }

        return result;
    }

    public static UpdateRequest parse(String requestStr) {
        UpdateRequest result = new UpdateRequest();
        UpdateFactory.parse(result, requestStr);

        return result;
    }

}
