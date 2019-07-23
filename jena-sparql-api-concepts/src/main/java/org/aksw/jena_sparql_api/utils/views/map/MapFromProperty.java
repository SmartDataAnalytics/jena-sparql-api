package org.aksw.jena_sparql_api.utils.views.map;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.aksw.commons.accessors.CollectionFromConverter;
import org.aksw.jena_sparql_api.concepts.Concept;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.concepts.UnaryRelation;
import org.aksw.jena_sparql_api.core.utils.ReactiveSparqlUtils;
import org.aksw.jena_sparql_api.utils.ElementUtils;
import org.aksw.jena_sparql_api.utils.Vars;
import org.aksw.jena_sparql_api.utils.model.ResourceUtils;
import org.aksw.jena_sparql_api.utils.model.SetFromPropertyValues;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import com.github.jsonldjava.shaded.com.google.common.base.Stopwatch;
import com.google.common.base.Converter;
import com.google.common.collect.Maps;

/**
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
public class MapFromProperty
	extends AbstractMap<RDFNode, Resource>
{
	protected final Resource subject;
	protected final Property entryProperty;
	protected final Property keyProperty;

	//protected fin
	//protected Function<String, Resource> entryResourceFactory;
	
	public MapFromProperty(Resource subject, Property entryProperty, Property keyProperty) {
		super();
		this.subject = subject;
		this.entryProperty = entryProperty;
		this.keyProperty = keyProperty;
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
			
			Resource result = ReactiveSparqlUtils.execSelectQs(() -> QueryExecutionFactory.create(query, model))
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
	public Resource put(RDFNode key, Resource entry) {
		Resource existing = get(key);
		
		Resource e = entry.inModel(subject.getModel());
		
		if(!Objects.equals(existing, entry)) {
			if(existing != null) {
				subject.getModel().remove(subject, entryProperty, existing);
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
				new CollectionFromConverter<>(
					new SetFromPropertyValues<>(subject, entryProperty, Resource.class),
					converter.reverse()));
		
		return result;
	}
	
	
}
