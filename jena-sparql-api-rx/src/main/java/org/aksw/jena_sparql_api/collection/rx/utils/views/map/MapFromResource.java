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
import org.aksw.jena_sparql_api.utils.views.map.RdfEntry;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.google.common.base.Converter;

/**
 *
 * A map view for over the values of a specific property of a specific resource,
 * modeled in the following way:
 *
 * :subject
 *   :entryProperty ?entry .
 *
 *  ?entry
 *     :keyProperty ?key
 *     :valProperty ?value
 *
 *  The map associates each ?key with ?value.
 *
 *  Use a converter to convert the value to e.g. a property of ?value
 *  (this way, the map will lose its put capability)
 *
 * @author raven
 *
 */
public class MapFromResource
    extends AbstractMap<RDFNode, RDFNode>
{
    protected final Resource subject;
    protected final Property entryProperty;
    protected final Property keyProperty;
    protected final Property valueProperty;

    protected BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry;
    //protected fin
    //protected Function<String, Resource> entryResourceFactory;

    public MapFromResource(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            Property valueProperty)
    {
        this(subject, entryProperty, keyProperty, valueProperty, (s, k) -> s.getModel().createResource());
    }

    public MapFromResource(
            Resource subject,
            Property entryProperty,
            Property keyProperty,
            Property valueProperty,
            BiFunction<Resource, RDFNode, Resource> sAndKeyToEntry) {
        super();
        this.subject = subject;
        this.entryProperty = entryProperty;
        this.keyProperty = keyProperty;
        this.valueProperty = valueProperty;
        this.sAndKeyToEntry = sAndKeyToEntry;
    }

    @Override
    public RDFNode get(Object key) {
        Resource entry = key instanceof RDFNode ? getEntry((RDFNode)key) : null;

        RDFNode result = entry == null ? null : ResourceUtils.getPropertyValue(entry, valueProperty);

        return result;
    }

//	public Resource getEntry( key) {
//		Resource result = key instanceof RDFNode ? getEntry((RDFNode)key) : null;
//		return result;
//	}

    public Resource getEntry(RDFNode key) {
//		Stopwatch sw = Stopwatch.createStarted();
        Resource result = getEntryViaModel(key);
//		System.out.println("Elapsed (s): " + sw.stop().elapsed(TimeUnit.NANOSECONDS) / 1000000000.0);

        return result;
    }


    public Resource getEntryViaModel(RDFNode key) {
        Model model = subject.getModel();
        Resource result = model.listStatements(null, keyProperty, key)
            .mapWith(Statement::getSubject)
            .filterKeep(e -> model.contains(subject, entryProperty, e))
            .nextOptional()
            .orElse(null);

        return result;
    }

    public Resource getEntryViaSparql(RDFNode key) {

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
        RDFNode r = get(key);
        boolean result = r != null;
        return result;
    }

    @Override
    public Resource put(RDFNode key, RDFNode value) {
        Resource entry = getEntry(key);

        if(entry == null) {
            entry = sAndKeyToEntry.apply(subject, key);
            Objects.requireNonNull(entry);
        }

        //Resource e = entry.inModel(subject.getModel());

//		if(!Objects.equals(existing, entry)) {
//			if(existing != null) {
//				subject.getModel().remove(subject, entryProperty, existing);
//			}
//		}

        subject.addProperty(entryProperty, entry);

        ResourceUtils.setProperty(entry, keyProperty, key);
        ResourceUtils.setProperty(entry, valueProperty, value);

        return entry;
    }


    @Override
    public Set<Entry<RDFNode, RDFNode>> entrySet() {
        Converter<Resource, Entry<RDFNode, RDFNode>> converter = Converter.from(
                e -> new RdfEntry(e.asNode(), (EnhGraph)e.getModel(), keyProperty, valueProperty),
                e -> (Resource)e); // TODO Ensure to add the resource and its key to the subject model

        Set<Entry<RDFNode, RDFNode>> result =
            new SetFromCollection<>(
                new ConvertingCollection<>(
                    new SetFromPropertyValues<Resource>(subject, entryProperty, Resource.class) {
                        public Iterator<Resource> iterator() {
                            Iterator<Resource> baseIt = super.iterator();

                            return new SinglePrefetchIterator<Resource>() {
                                @Override
                                protected Resource prefetch() throws Exception {
                                    return baseIt.hasNext() ? baseIt.next() : finish();
                                }

                                protected void doRemove(Resource item) {
                                    item.removeAll(keyProperty);
                                    item.removeAll(valueProperty);
                                    baseIt.remove();
                                };

                            };
                        };
//						@Override
//						public void clear() {
//							System.out.println("here");
//							super.clear();
//						}
//						@Override
//						public boolean remove(Object key) {
//							boolean r = super.remove(key);
//							if(r) {
//								((RdfEntry)key).clear();
//							}
//							return r;
//						}
                    },
                    converter));

        return result;
    }

//	@Override
//	public void clear() {
//		for(Object r : entrySet()) {
//			RdfEntry e = (RdfEntry)r;
//			e.clear();
//		}
//
//		super.clear();
//	}

}
