package org.aksw.jena_sparql_api.collection.rx.utils.views.map;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;

import org.aksw.commons.collections.ConvertingCollection;
import org.aksw.commons.collections.SinglePrefetchIterator;
import org.aksw.commons.collections.sets.SetFromCollection;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.rdf.collections.ResourceUtils;
import org.aksw.jena_sparql_api.rdf.collections.SetFromPropertyValues;
import org.aksw.jena_sparql_api.rx.SparqlRx;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.views.map.RdfMap;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.common.base.Converter;
import com.google.common.collect.Maps;

/**
 * Entry resources are not managed in this map - i.e. the put method requires a resource
 * with appropriate entry information which is probably not what you want!
 * Use {@link MapFromResource} instead which creates anonymous resources for map entries.
 *
 * A map view for over the values of a specific property of a specific resource,
 * modeled in the following way:
 *
 * :subject
 *   :entryProperty ?value .
 *
 *  ?value
 *     :keyProperty ?key .
 *
 *  The map associates each ?key with ?value.
 *
 *  Use a converter to convert the value to e.g. a property of ?value
 *  (this way, the map will lose its put capability)
 *
 * @author raven
 *
 */
public class MapFromResourceUnmanaged
    extends AbstractMap<RDFNode, Resource>
    implements RdfMap<RDFNode, Resource>
{
    protected final Resource subject;
    protected final Property entryProperty;
    //protected final boolean isReverseEntryProperty;
    protected final Property keyProperty;

    protected BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry;

    /** Whether to remove the key properties from entries upon unlinking them
    /* Unlink occurs when an entry is deleted or replaced with a new one */
    protected final boolean removeKeyFromEntryUponUnlinking = true;

    public MapFromResourceUnmanaged(
            Resource subject,
            Property entryProperty,
            Property keyProperty) {
        this(subject, entryProperty, keyProperty, (s, k) -> s.getModel().createResource());
    }

    public MapFromResourceUnmanaged(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry) {
        super();
        this.subject = subject;
        this.entryProperty = entryProperty;
        //this.isReverseEntryProperty = false;
        this.keyProperty = keyProperty;
        this.sAndKeyToEntry = sAndKeyToEntry;
    }

    @Override
    public Resource get(Object key) {
        Resource result = key instanceof RDFNode ? get((RDFNode)key) : null;
        return result;
    }

    public Resource get(RDFNode key) {
//		Stopwatch sw = Stopwatch.createStarted();
        Resource result = getViaModel(key);
//		System.out.println("Elapsed (s): " + sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0);

        return result;
    }


    public Resource getViaModel(RDFNode key) {
        Model model = subject.getModel();
        Resource result = model.listStatements(null, keyProperty, key)
            .mapWith(Statement::getSubject)
            .filterKeep(e -> model.contains(subject, entryProperty, e))
            .nextOptional()
            .orElse(null);

        return result;
    }

    public Resource getViaSparql(RDFNode key) {

        UnaryRelation e = new Concept(
                ElementUtils.createElementTriple(
                        new Triple(Vars.e, keyProperty.asNode(), key.asNode()),
                        new Triple(subject.asNode(), entryProperty.asNode(), Vars.e))
                , Vars.e);

            Query query = RelationUtils.createQuery(e);

            Model model = subject.getModel();

            Resource result = SparqlRx.execSelect(() -> QueryExecutionFactory.create(query, model))
                .map(qs -> qs.get(e.getVar().getName()).asResource())
                .singleElement()
                .blockingGet();

        return result;
    }

    @Override
    public boolean containsKey(Object key) {
        Resource r = get(key);
        boolean result = r != null;
        return result;
    }

    @Override
    public Resource allocate(RDFNode key) {
        Resource result = get(key);
        if(result == null) {
            result = sAndKeyToEntry.apply(subject, key);
            put(key, result);
        }
        return result;
    }

    @Override
    public Resource put(RDFNode key, Resource entry) {
        Resource existing = get(key);

        Resource e = entry.inModel(subject.getModel());

        // If the put entry differs from a prior one then unlink the prior one
        if (!Objects.equals(existing, entry)) {
            if(existing != null) {
                subject.getModel().remove(subject, entryProperty, existing);

                if (removeKeyFromEntryUponUnlinking) {
                    existing.removeAll(keyProperty);
                }
            }
        }

        subject.addProperty(entryProperty, e);

        ResourceUtils.setProperty(e, keyProperty, key);

        return entry;
    }


    @Override
    public Set<Entry<RDFNode, Resource>> entrySet() {
        Converter<Resource, Entry<RDFNode, Resource>> converter = Converter.from(
                e -> Maps.immutableEntry(ResourceUtils.getPropertyValue(e, keyProperty), e),
                e -> e.getValue()); // TODO Ensure to add the resource and its key to the subject model

        Set<Entry<RDFNode, Resource>> result =
            new SetFromCollection<>(
                new ConvertingCollection<>(
                    //new SetFromPropertyValues<>(subject, entryProperty, Resource.class),
                    new SetFromPropertyValues<Resource>(subject, entryProperty, Resource.class) {
                        // Override the iterator to support removal of key properties
                        public Iterator<Resource> iterator() {
                            Iterator<Resource> baseIt = super.iterator();

                            return new SinglePrefetchIterator<Resource>() {
                                @Override
                                protected Resource prefetch() throws Exception {
                                    return baseIt.hasNext() ? baseIt.next() : finish();
                                }

                                protected void doRemove(Resource item) {
                                    if (removeKeyFromEntryUponUnlinking) {
                                        item.removeAll(keyProperty);
                                    }
                                    baseIt.remove();
                                };

                            };
                        };
                    },
                    converter));

        return result;
    }


}
