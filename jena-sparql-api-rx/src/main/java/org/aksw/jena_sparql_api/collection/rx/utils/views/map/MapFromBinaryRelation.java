package org.aksw.jena_sparql_api.collection.rx.utils.views.map;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.aksw.jena_sparql_api.concepts.BinaryRelation;
import org.aksw.jena_sparql_api.concepts.ConceptUtils;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;

import com.google.common.collect.Maps;

import io.reactivex.rxjava3.core.Flowable;

public class MapFromBinaryRelation
    extends AbstractMap<RDFNode, Collection<RDFNode>>
{
    protected Model model;
    protected BinaryRelation relation;

    // TODD Maybe a Plan object would be a better basis for the map - the plan would
    // already be precomputed and consider all optimizations


    public MapFromBinaryRelation(Model model, BinaryRelation relation) {
        super();
        this.model = model;
        this.relation = relation;
    }

//	public <K, V> Map<K, V> transform(Function<Model, Converter<RDFNode, K>> keyMapper) {
//
//	}

    @Override
    public Collection<RDFNode> get(Object key) {
        Collection<RDFNode> result = null;
        if(key instanceof RDFNode) {
            RDFNode k = (RDFNode)key;
            BinaryRelation br = relation.joinOn(relation.getSourceVar()).with(ConceptUtils.createFilterConcept(k.asNode())).toBinaryRelation();

            result = Optional.ofNullable(fetch(model, br)).map(f -> f.map(Entry::getValue).toList().blockingGet()).orElse(null);
        }

        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        Collection<RDFNode> item = get(key);
        boolean result = item != null;
        return result;
    }

    @Override
    public Set<Entry<RDFNode, Collection<RDFNode>>> entrySet() {

        Map<RDFNode, Collection<RDFNode>> map = fetch(model, relation)
            .toMultimap(Entry::getKey, Entry::getValue) //, LinkedHashMap::new, LinkedHashSet::new)
            .blockingGet();

        return map.entrySet();
    }

    public static Flowable<Entry<RDFNode, RDFNode>> fetch(Model model, BinaryRelation relation) {
        Query query = RelationUtils.createQuery(relation);

        Flowable<Entry<RDFNode, RDFNode>> result = SparqlRx.execSelect(() -> QueryExecutionFactory.create(query, model))
            .map(qs -> Maps.immutableEntry(
                    qs.get(relation.getSourceVar().getName()),
                    qs.get(relation.getTargetVar().getName())));

        return result;
    }
}
