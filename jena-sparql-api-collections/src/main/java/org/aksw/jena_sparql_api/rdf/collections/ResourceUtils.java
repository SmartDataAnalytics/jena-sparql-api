package org.aksw.jena_sparql_api.rdf.collections;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.aksw.commons.util.reflect.ClassUtils;
import org.aksw.jena_sparql_api.mapper.proxy.TypeDecider;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.PropertyImpl;
import org.apache.jena.sparql.path.P_Path0;
import org.apache.jena.util.iterator.ExtendedIterator;

import com.google.common.collect.Streams;

/**
 * TODO Move back to jena's iterator API, it allows removing items - which does not work with streams
 * 
 * @author raven Apr 11, 2018
 *
 */
public class ResourceUtils {


//    public static void addLiteral(Resource r, Property p, Object o) {
//        if(o != null) {
//            r.addLiteral(p, o);
//        }
//    }
//
//    public static void addProperty(Resource r, Property p, RDFNode o) {
//        if(o != null) {
//            r.addProperty(p, o);
//        }
//    }

	/*
	 * Misc utils and helper functions
	 */

    public static Resource asResource(Node node, Graph graph) {
        Model model = ModelFactory.createModelForGraph(graph);
        RDFNode tmp = org.apache.jena.sparql.util.ModelUtils.convertGraphNodeToRDFNode(node, model);
        Resource result = tmp.asResource();
        return result;
    }

    // TODO Probably jena already has this util somewhere
	public static <T> Stream<T> asStream(ExtendedIterator<T> it) {
		Stream<T> result = Streams.stream(it).onClose(it::close);
		return result;
	}

	/**
	 * Closing version of .findFirst()
	 * 
	 * @param stream
	 * @return
	 */
	public static <T> Optional<T> findFirst(Stream<T> stream) {
		Optional<T> result;
		try(Stream<?> tmp = stream) {
			result = stream.findFirst();
		}
		return result;
	}

	public static <T> Optional<T> findFirst(ExtendedIterator<T> stream) {
		Optional<T> result = stream.nextOptional();
		stream.close();

		return result;
	}

	public static <T extends RDFNode> boolean canAsProperty(Statement stmt, Class<T> clazz) {
		boolean result = stmt.getObject().canAs(clazz);
		return result;
	}

	public static <T extends RDFNode> boolean canAsPropertyNode(Statement stmt, Predicate<Node> nodeTest) {
		Node node = stmt.getObject().asNode();
		boolean result = nodeTest.test(node);
		return result;
	}

	public static <T extends RDFNode> boolean canAsProperty(Statement stmt, Predicate<RDFNode> nodeTest) {
		RDFNode rdfNode = stmt.getObject();;
		boolean result = nodeTest.test(rdfNode);
		return result;
	}

//	public static <T extends RDFNode> boolean canAsProperty(Statement stmt, Class<T> viewClass, TypeDecider typeDecider) {
//		boolean result = stmt.getObject().canAs(clazz);
//		return result;
//	}

	

	
	
	public static <T extends RDFNode> T getPropertyValue(Statement stmt, Class<T> clazz) {
		T result = stmt.getObject().as(clazz);
		return result;
	}

	public static <T> T getPropertyValue(Statement stmt, NodeMapper<T> nodeMapper) {
		Node node = stmt.getObject().asNode();
		T result = nodeMapper.toJava(node);
		return result;
	}

	
	public static <T> T getPropertyValue(Statement stmt, RDFNodeMapper<T> rdfNodeMapper) {
		RDFNode rdfNode = stmt.getObject();
		T result = rdfNodeMapper.toJava(rdfNode);
		return result;
	}

	
	public static boolean canAsLiteral(Statement stmt, Class<?> clazz) {
		TypeMapper tm = TypeMapper.getInstance();
		RDFDatatype dtype = tm.getTypeByClass(clazz);
		
		RDFNode o = stmt.getObject();
		
		Node node = o.asNode();
		Object obj = node.isLiteral() ? node.getLiteral().getValue() : null;
		Class<?> oClass = obj == null ? null : obj.getClass();
		
		// We check on two levels:
		// (a) Is 'clazz' assignable to the Java type of stmt's object? 
		// (b) Is there an RDFDatatype corresponding to the requested clazz
		boolean result = oClass != null && clazz.isAssignableFrom(oClass)
				|| dtype != null && o.isLiteral() && NodeMapperFromRdfDatatype.canMapCore(node, dtype);
		
		return result;
	}

	public static <T> Literal createTypedLiteral(Model model, Class<T> clazz, T o) {
		TypeMapper tm = TypeMapper.getInstance();
		RDFDatatype dtype = tm.getTypeByClass(clazz);

		Literal result = model.createTypedLiteral((Object)o, dtype);
		return result;
	}

//	public static boolean canAsLiteral(Statement stmt, Class<?> clazz) {
//		boolean result = stmt.getObject().isLiteral() &&
//				Optional.ofNullable(stmt.getObject().asLiteral().getValue())
//				.map(v -> clazz.isAssignableFrom(v.getClass())).isPresent();
//		return result;
//	}

	public static <T> T getLiteralValue(Statement stmt, Class<T> clazz) {
//		TypeMapper tm = TypeMapper.getInstance();
//		RDFDatatype dtype = tm.getTypeByClass(clazz);
		RDFNode o = stmt.getObject();
		Node node = o.asNode();
		T result = NodeMapperFromRdfDatatype.toJavaCore(node, clazz);
		return result;
	}

//	@SuppressWarnings("unchecked")
//	public static <T> T getLiteralValue(Statement stmt, Class<T> clazz) {
//		T result = (T)stmt.getObject().asLiteral().getValue();
//		return result;
//	}

	
	
	public static <T extends RDFNode> ExtendedIterator<Statement> listProperties(Resource s, Property p, Class<T> clazz) {
		ExtendedIterator<Statement> result = listProperties(s, p)
				.filterKeep(stmt -> canAsProperty(stmt, clazz));
		return result;
	}

	
	public static <T extends RDFNode> ExtendedIterator<T> listPropertyValues(Resource s, Property p, Class<T> clazz) {
		ExtendedIterator<T> result = listProperties(s, p, clazz)
				.mapWith(stmt -> getPropertyValue(stmt, clazz));
		return result;
	}

	/*
	 * properties
	 */

//	public static Stream<Statement> listProperties(Resource s, Property p) {
//		Stream<Statement> result = asStream(s.listProperties(p));
//		return result;
//	}

	public static StmtIterator listProperties(Resource s) {
		StmtIterator result = s.listProperties();
		return result;
	}

	public static StmtIterator listProperties(Resource s, Property p) {
		StmtIterator result = s.listProperties(p);
		return result;
	}

	public static ExtendedIterator<RDFNode> listPropertyValues(Resource s, Property p) {
		ExtendedIterator<RDFNode> result = listProperties(s, p)
				.mapWith(Statement::getObject);
		return result;
	}

	/**
	 * Equivalent to s.getProperty(p)
	 * 
	 * This method exists solely for completeness.
	 * 
	 * @param s
	 * @param p
	 * @return
	 */
	public static Optional<Statement> getProperty(Resource s, Property p) {
		Optional<Statement> result = findFirst(listProperties(s, p));
		return result;		
	}

	public static Optional<RDFNode> tryGetPropertyValue(Resource s, Property p) {
		Optional<RDFNode> result = getProperty(s, p)
				.map(Statement::getObject);	
		return result;		
	}

	/**
	 * Get a single value chosen at random from all available values of the given property.
	 * 
	 * @param s
	 * @param p
	 * @return
	 */
	public static RDFNode getPropertyValue(Resource s, Property p) {
		RDFNode result = tryGetPropertyValue(s, p).orElse(null);
		return result;		
	}

//	public static <T extends RDFNode> ExtendedIterator<Statement> listProperties(Resource s, Property p, Class<T> clazz) {
//		ExtendedIterator<Statement> result = listProperties(s, p)
//				.filterKeep(stmt -> canAsProperty(stmt, clazz));
//		return result;
//	}
//
//	
//	public static <T extends RDFNode> ExtendedIterator<T> listPropertyValues(Resource s, Property p, Class<T> clazz) {
//		ExtendedIterator<T> result = listProperties(s, p, clazz)
//				.mapWith(stmt -> getPropertyValue(stmt, clazz));
//		return result;
//	}

// Note: There is not much benefit in a listProperties function that takes a generic predicate
//       - just use filterKeep instead
//	public static <T> ExtendedIterator<Statement> listProperties(Resource s, Property p, Predicate<Statement> predicate) {
//		ExtendedIterator<Statement> result = listProperties(s, p)
//				.filterKeep(predicate);
//		return result;
//	}

	public static <T> ExtendedIterator<Statement> listProperties(Resource s, Property p, NodeMapper<T> nodeMapper) {
		ExtendedIterator<Statement> result = listProperties(s, p)
				.filterKeep(stmt -> canAsPropertyNode(stmt, nodeMapper::canMap));
		return result;
	}

	public static <T> ExtendedIterator<Statement> listProperties(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper) {
		ExtendedIterator<Statement> result = listProperties(s, p)
				.filterKeep(stmt -> canAsProperty(stmt, rdfNodeMapper::canMap));
		return result;
	}

	// Not used yet; this only covers the binary case, but in general we need to consider
	// an arbitrary set of subclasse for whether there is a single set of classes common to all of them
	public static <T> Set<T> lowestCommonAncestors(T a, T b, Function<? super T, ? extends Iterable<? extends T>> successor) {
		Set<T> parentsA = new HashSet<>();
		Set<T> parentsB = new HashSet<>();
		
		parentsA.add(a);
		parentsB.add(b);
		
		Set<T> intersection = Sets.intersection(parentsA, parentsB);
		while(intersection.isEmpty() && !(a == null && b == null)) {
			Iterable<? extends T> pa = a == null ? null : successor.apply(a);
			Iterable<? extends T> pb = b == null ? null : successor.apply(b);

			Iterables.addAll(parentsA, pa);
			if(!intersection.isEmpty()) {
				break;
			}
			
			Iterables.addAll(parentsB, pb);
		}

//		T result = intersection.isEmpty()
//				? null
//				: intersection.iterator().next();
//		
		Set<T> result = new HashSet<>(intersection);
		return result;
	}
	
	public static Iterable<Class<?>> getParentClasses(Class<?> child) {
		return Iterables.concat(
				Collections.singleton(child.getSuperclass()),
				Arrays.asList(child.getInterfaces())
				);
	}
	
	
//	public static lowestCommonClasses(Collection<Class<?>> candidates, Class<?> bound) {
//		lowestCommonAncestors(a, b, successor)
//	}
	
	public static Class<?> getMostSpecificSubclass(Resource s, Class<?> viewClass, TypeDecider typeDecider) {
	    Collection<Class<?>> classes = typeDecider.getApplicableTypes(s);
	
	    Set<Class<?>> mscs = ClassUtils.getMostSpecificSubclasses(viewClass, classes);

	    // TODO If there are multiple specific subclasses, check if there is a lowest common ancestor below viewClass
	    
	    
	    Class<?> result = mscs.size() == 1
	    		? mscs.iterator().next()
	    		: mscs.size() > 1
	    			? viewClass /* TODO LCA */
	    			: null;
	    
//	    if(mscs.isEmpty()) {
//	        throw new RuntimeException("No applicable type found for " + r + " [" + clazz.getName() + "]");
//	    } else if(mscs.size() > 1) {
//	        throw new RuntimeException("Multiple non-subsumed sub-class candidates of " + clazz + " found: " + mscs);
//	    } else {
//	        r = mscs.iterator().next();
//	    }
	    
	    return result;
	}

	
	void setObject(Object o) {
		// Check if the object's class is registered with the TypeMapper
		
		// Otherwise, if the object is a subclass of RDF term, just set it directly
	}
	
	
	/**
	 * Map any RDF term to an appropriate RDFNode by trying to map with the NodeMapper
	 * and the TypeDecider
	 * 
	 * @param <T>
	 * @param it
	 * @param viewClass
	 * @param nodeMapper
	 * @param typeDecider
	 * @return
	 */
//	public static <T extends RDFNode> ExtendedIterator<T> transformIteratorForTypeDecider(Iterator<? extends RDFNode> it, Class<T> viewClass,
//			NodeMapper nodeMapper,
//			TypeDecider typeDecider) {
//		
//		ExtendedIterator<T> result = WrappedIterator.create(it)
//				.mapWith(RDFNode::asResource)
//				.mapWith(o -> Maps.immutableEntry(o, getMostSpecificSubclass(o, viewClass, typeDecider)))
//				// If the type decider did not yield a class, fall back to the requested view class
////				.mapWith(e -> e.getValue() != null ? e : Maps.immutableEntry(e.getKey(), viewClass))
//				.filterKeep(e -> e.getValue() != null)
//				// Only retain items we can cast to
//				.filterKeep(e -> e.getKey().canAs((Class)e.getValue()))
//				.mapWith(e -> (T)e.getKey().as((Class)e.getValue()));
//				
//		return result;
//	}

	
	/**
	 * Extend the given iterator with filtering and transformation of its RDF nodes to
	 * its most specific Java type w.r.t. a type decider.
	 * 
	 * @param <T>
	 * @param it
	 * @param viewClass
	 * @param typeDecider
	 * @return
	 */
//	public static <T extends RDFNode> ExtendedIterator<T> transformIteratorForTypeDecider(Iterator<? extends RDFNode> it, Class<T> viewClass, TypeDecider typeDecider) {
//		
//		ExtendedIterator<T> result = WrappedIterator.create(it)
//				.mapWith(RDFNode::asResource)
//				.mapWith(o -> Maps.immutableEntry(o, getMostSpecificSubclass(o, viewClass, typeDecider)))
//				// If the type decider did not yield a class, fall back to the requested view class
////				.mapWith(e -> e.getValue() != null ? e : Maps.immutableEntry(e.getKey(), viewClass))
//				.filterKeep(e -> e.getValue() != null)
//				// Only retain items we can cast to
//				.filterKeep(e -> e.getKey().canAs((Class)e.getValue()))
//				.mapWith(e -> (T)e.getKey().as((Class)e.getValue()));
//				
//		return result;
//	}
	
//	public static <T extends RDFNode> ExtendedIterator<T> listPropertyValues(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper) {
//		ExtendedIterator<T> result =
//				transformIteratorForTypeDecider(
//					listProperties(s, p, viewClass).mapWith(Statement::getObject),
//					viewClass,
//					typeDecider);
//			
//		return result;
//	}

	
	public static <T> Optional<T> tryGetPropertyValue(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper) {
		Optional<T> result = findFirst(listPropertyValues(s, p, rdfNodeMapper));
		return result;		
	}
	
	
	public static <T> Optional<T> tryGetPropertyValue(Resource s, Property p, NodeMapper<T> nodeMapper) {
		Optional<T> result = findFirst(listPropertyValues(s, p, nodeMapper));
		return result;		
	}
	
	public static <T> T getPropertyValue(Resource s, Property p, NodeMapper<T> nodeMapper) {
		T result = tryGetPropertyValue(s, p, nodeMapper).orElse(null);
		return result;
	}

	public static <T> T getPropertyValue(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper) {
		T result = tryGetPropertyValue(s, p, rdfNodeMapper).orElse(null);
		return result;		
	}


	public static <T> ExtendedIterator<T> listPropertyValues(Resource s, Property p, NodeMapper<T> nodeMapper) {
		ExtendedIterator<T> result = listProperties(s, p, nodeMapper)
				.mapWith(stmt -> getPropertyValue(stmt, nodeMapper));
		return result;
	}

	public static <T> ExtendedIterator<T> listPropertyValues(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper) {
		ExtendedIterator<T> result = listProperties(s, p, rdfNodeMapper)
				.mapWith(stmt -> getPropertyValue(stmt, rdfNodeMapper));
		return result;
	}
	
	public static <T extends RDFNode> Optional<Statement> getProperty(Resource s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listProperties(s, p, clazz));
		return result;		
	}

	public static <T extends RDFNode> Optional<T> tryGetPropertyValue(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = getProperty(s, p, clazz)
				.map(stmt -> getPropertyValue(stmt, clazz));
		return result;		
	}

	public static <T extends RDFNode> T getPropertyValue(Resource s, Property p, Class<T> clazz) {
		T result = tryGetPropertyValue(s, p, clazz).orElse(null);
		return result;		
	}


	public static <T> ExtendedIterator<Statement> listLiteralProperties(Resource s, Property p, Class<T> clazz) {
		ExtendedIterator<Statement> result = listProperties(s, p)
				.filterKeep(stmt -> canAsLiteral(stmt, clazz));
		return result;
	}
	
	public static <T> ExtendedIterator<T> listLiteralPropertyValues(Resource s, Property p, Class<T> clazz) {
		ExtendedIterator<T> result = listLiteralProperties(s, p, clazz)
				.mapWith(stmt -> getLiteralValue(stmt, clazz));

		return result;
	}

	public static <T> Optional<Statement> getLiteralProperty(Resource s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listLiteralProperties(s, p, clazz));
		return result;
	}
	
	public static <T> Optional<T> tryGetLiteralPropertyValue(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = getLiteralProperty(s, p, clazz)
				.map(stmt -> getLiteralValue(stmt, clazz));
		return result;		
	}

	public static <T> T getLiteralPropertyValue(Resource s, Property p, Class<T> clazz) {
		T result = tryGetLiteralPropertyValue(s, p, clazz).orElse(null);
		return result;		
	}

	public static StmtIterator listProperties(RDFNode s, boolean isFwd) {		
		StmtIterator result = isFwd
				? s.asResource().listProperties()
				: listReverseProperties(s);
		return result;
	}

	public static StmtIterator listReverseProperties(RDFNode s) {		
		StmtIterator result = s.getModel().listStatements(null, null, s);
		return result;
	}
	
	// NOTE Inverse properties cannot refer to literals
	public static StmtIterator listReverseProperties(RDFNode s, Property p) {		
		StmtIterator result = s.getModel().listStatements(null, p, s);
		return result;
	}

	public static Statement getReverseProperty(RDFNode s, Property p) {
		Statement result = tryGetReverseProperty(s, p).orElse(null);
		return result;
	}

	public static Optional<Statement> tryGetReverseProperty(RDFNode s, Property p) {
		Optional<Statement> result = findFirst(listReverseProperties(s, p));
		return result;
	}

	public static <T extends Resource> boolean isReverseProperty(Statement stmt, Class<T> clazz) {
		boolean result = stmt.getSubject().canAs(clazz);
		return result;
	}

	public static <T extends Resource> ExtendedIterator<Statement> listReverseProperties(RDFNode s, Property p, Class<T> clazz) {
		ExtendedIterator<Statement> result = listReverseProperties(s, p)
				.filterKeep(stmt -> isReverseProperty(stmt, clazz));
		return result;
	}
	
	public static <T extends Resource> Optional<Statement> getReverseProperty(RDFNode s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listReverseProperties(s, p, clazz));
		
		return result;
	}

	public static Optional<Resource> tryGetReversePropertyValue(Resource s, Property p) {
		Optional<Resource> result = tryGetReverseProperty(s, p)
				.map(Statement::getSubject);	
		return result;		
	}

	public static Resource getReversePropertyValue(Resource s, Property p) {
		Resource result = tryGetReversePropertyValue(s, p).orElse(null);
		return result;		
	}

	public static <T extends Resource> Optional<T> tryGetReversePropertyValue(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = getReverseProperty(s, p, clazz)
				.map(Statement::getSubject)
				.map(r -> r.as(clazz));

		return result;		
	}

	public static <T extends Resource> T getReversePropertyValue(Resource s, Property p, Class<T> clazz) {
		T result = tryGetReversePropertyValue(s, p, clazz).orElse(null);
		return result;		
	}



	public static ExtendedIterator<Resource> listReversePropertyValues(Resource s, Property p) {
		ExtendedIterator<Resource> result = listReverseProperties(s, p)
				.mapWith(Statement::getSubject);
		
		return result;
	}	

	public static <T extends Resource> ExtendedIterator<T> listReversePropertyValues(RDFNode s, Property p, Class<T> clazz) {
		ExtendedIterator<T> result = listReverseProperties(s, p, clazz)
				.mapWith(stmt -> stmt.getSubject().as(clazz));

		return result;
	}

	

//	public static <T extends Resource> Stream<Statement> listInverseObjectPropertyStatements(RDFNode s, Property p, Class<T> clazz) {
//		Stream<Statement> result = listInverseStatements(s, p)
//				.filter(stmt -> isInverseObjectProperty(stmt, clazz));
//		return result;
//	}

//	public static <T extends RDFNode> Optional<T> getObjectAs(Resource s, Property p, Class<T> clazz) {
//		Optional<T> result = Optional.ofNullable(s.getProperty(p))
//				.map(Statement::getObject)
//				.filter(o -> o.canAs(clazz))
//				.map(o -> o.as(clazz));
//		
//		return result;
//	}


//	public static <T> Optional<T> getLiteral(Resource s, Property p, Function<? super Literal, ? extends T> fn) {
//		Optional<T> result = listLiteralStatements(s, p, clazz)
//				.findFirst()
//				.map(stmt -> getObjectAsLiteralType(stmt, clazz));
//	
//		return result;		
//	}

	
	public static boolean addProperty(Resource s, Property p, RDFNode o) {
		boolean result = false;
		if(o != null && !s.hasProperty(p, o)) {
			s.addProperty(p, o);
			result = true;
		}
		return result;
	}

	public static boolean addLiteral(Resource s, Property p, Object o) {
		boolean result = false;
		if(o != null && !s.hasLiteral(p, o)) {
			s.addLiteral(p, o);
			result = true;
		}
		return result;
	}

//	public static boolean addObjectProperty(Resource s, Property p, RDFNode o) {
//		boolean result = false;
//		if(o != null && !s.hasProperty(p, o)) {
//			s.addProperty(p, o);
//			result = true;
//		}
//		return result;
//	}

	public static boolean addReverseProperty(RDFNode s, Property p, Resource o) {
		Resource newS = o.inModel(s.getModel());
		RDFNode newO = s;
		boolean result = addProperty(newS, p, newO);
		return result;
	}

	/**
	 * 
	 * @param m
	 * @param removals
	 * @param stmt The statement to insert after the removal of context statements. May be null.
	 * @return
	 */
	public static boolean replaceProperties(Model m, ExtendedIterator<Statement> removals, Statement stmt) {
		boolean stmtSeen = false;
		while(removals.hasNext()) {
			Statement item = removals.next();
			if(item.equals(stmt)) {
				stmtSeen = true;
			} else {
				removals.remove();
			}
		}
		
		boolean result = stmt != null && !stmtSeen; 
		if(result) {
			m.add(stmt);
		}
		
		return result;

//		List<Statement> stmts = removals
//				.filter(item -> !item.equals(stmt))
//				.collect(Collectors.toList());
//		
//		m.remove(stmts);
//		
//		boolean result = stmt == null ? !stmts.isEmpty() : !m.contains(stmt);
//		
//		if(stmt != null) {
//			m.add(stmt);
//		}
//
//		return result;
	}
//	public static boolean replaceProperties(Model m, Stream<Statement> removals, Statement stmt) {
//		List<Statement> stmts = removals
//				.filter(item -> !item.equals(stmt))
//				.collect(Collectors.toList());
//		
//		m.remove(stmts);
//		
//		boolean result = stmt == null ? !stmts.isEmpty() : !m.contains(stmt);
//		
//		if(stmt != null) {
//			m.add(stmt);
//		}
//
//		return result;
//	}

	/*             
	 * set: replaces all properties resource
	 * update: replaces the set of properties that match the given RDFNode or literal class 
	 * 
	 * TODO This table is outdated
	 * 
	 * |             affects   | literals | non-literals |
	 * |--------------------------------------------------
	 * | setProperty           |      yes |          yes |
	 * | updateProperty        |       no |          yes |
	 * | setLiteralProperty    |      yes |          yes |
	 * | updateLiteralProperty |      yes |           no |
	 * 
	 */

	public static boolean setProperty(Resource s, Property p, RDFNode o) {
		boolean result = replaceProperties(s.getModel(),
				listProperties(s, p),
				o == null ? null : s.getModel().createStatement(s, p, o));
		return result;
	}


	public static <S extends Resource> S setLiteralProperty(S s, Property p, Object o) {
		replaceProperties(s.getModel(),
				listProperties(s, p),
				o == null ? null : s.getModel().createLiteralStatement(s, p, o));

		return s;
	}
	
	public static <T> boolean setProperty(Resource s, Property p, NodeMapper<T> nodeMapper, T value) {
		
		RDFNode o = value == null ? null : s.getModel().asRDFNode(nodeMapper.toNode(value));
		boolean result = replaceProperties(s.getModel(),
				listProperties(s, p, nodeMapper),
				o == null ? null : s.getModel().createStatement(s, p, o));
		return result;
	}

//	public static Predicate<Statement> createPredicateIsObjectOfType(Class<?> clazz) {
//		return stmt -> isObjectOfType()
//	}
		
	public static <T> boolean updateProperty(Resource s, Property p, NodeMapper<T> nodeMapper, T o) {
		Model m = s.getModel();

		boolean result = replaceProperties(m,
				listProperties(s, p, nodeMapper),
				o == null ? null : m.createStatement(s, p, m.asRDFNode(nodeMapper.toNode(o))));
		
		return result;
	}


	public static <T> boolean updateLiteralProperty(Resource s, Property p, Class<T> clazz, T o) {
		Model m = s.getModel();
		
		boolean result = replaceProperties(m,
				listLiteralProperties(s, p, clazz),
				o == null ? null : m.createStatement(s, p, createTypedLiteral(m, clazz, o)));
		
		return result;
	}

	public static <T> boolean updateProperty(Resource s, Property p, RDFNodeMapper<T> rdfNodeMapper, T o) {
		Model m = s.getModel();
		
		boolean result = replaceProperties(m,
				listProperties(s, p, rdfNodeMapper),
				o == null ? null : m.createStatement(s, p, rdfNodeMapper.toNode(o)));
		
		return result;
	}

	
	/**
	 * Replaces all properties that can act as the given class - other properties remain unaffected.
	 * 
	 * @param s
	 * @param p
	 * @param clazz
	 * @param o
	 * @return
	 */
	public static <T extends RDFNode> boolean updateProperty(Resource s, Property p, Class<T> clazz, T o) {		
		boolean result = replaceProperties(s.getModel(),
				listProperties(s, p, clazz),
				o == null ? null : s.getModel().createStatement(s, p, o));
		
		return result;
	}

//	public static <T extends Resource> boolean setInverseProperty(RDFNode s, Property p, Class<T> clazz, T o) {
//		return setInverseObjectProperty(s, p, clazz, o);
//	}

	public static <T extends Resource> boolean setReverseProperty(RDFNode s, Property p, Class<T> clazz, T o) {
		boolean result = replaceProperties(s.getModel(),
				listReverseProperties(s, p, clazz),
				o == null ? null : s.getModel().createStatement(o, p, s));
		
		return result;
	}	
	
	public static <T extends Resource> boolean hasProperty(Resource s, Property p, Class<T> clazz) {
		boolean result = getProperty(s, p, clazz).isPresent();
		return result;
	}
	
	public static boolean hasReverseProperty(RDFNode s, Property p) {
		boolean result = tryGetReverseProperty(s, p).isPresent();
		return result;
	}
	
	public static <T extends Resource> boolean hasReverseProperty(RDFNode s, Property p, Class<T> clazz) {
		boolean result = getReverseProperty(s, p, clazz).isPresent();
		return result;
	}

	// Specific case for object properties should literals be allowed as subjects

	
//	public static <T extends Resource> ExtendedIterator<T> listPropertyValues(Resource s, Property p, boolean isInverse, Class<T> clazz) {
//		ExtendedIterator<T> result = isInverse
//				? listInversePropertyValues(s, p, clazz)
//				: listPropertyValues(s, p, clazz);
//
//		return result;
//	}
//	public static <T> boolean setLiteral(Resource s, Property p, Class<T> clazz, T o) {
//		boolean result = replaceStatements(s.getModel(),
//				listProperties(s, p),
//				o == null ? null : s.getModel().createLiteralStatement(s, p, o));
//		
//		return result;
//	}
	
	/**************************************
	 * Utils with direction flag
	 **************************************/

	public static RDFNode getSource(Statement stmt, boolean isFwd) {
		RDFNode result = isFwd ? stmt.getSubject() : stmt.getObject();
		return result;
	}

	public static RDFNode getTarget(Statement stmt, boolean isFwd) {
		RDFNode result = isFwd ? stmt.getObject() : stmt.getSubject();
		return result;
	}
	
	public static Property getProperty(P_Path0 link) {
		Property result = new PropertyImpl(link.getNode(), null);
		return result;
//		Model m = s.getModel();
//		Node n = step.getNode();
//		RDFNode rn = m.getRDFNode(n);
//		Property p = rn.as(Property.class);
	}
	
	public static ExtendedIterator<RDFNode> listPropertyValues(Resource s, P_Path0 step) {
		boolean isFwd = step.isForward();
		return listProperties(s, step)
				.mapWith(stmt -> getTarget(stmt, isFwd));
	}
	
	public static StmtIterator listProperties(Resource s, P_Path0 step) {
		boolean isFwd = step.isForward();
		Property p = getProperty(step);
		StmtIterator result = listProperties(s, p, isFwd);
		return result;
	}

	public static ExtendedIterator<RDFNode> listPropertyValues(Resource s, Property p, boolean isFwd) {
		return listProperties(s, p, isFwd)
				.mapWith(stmt -> getTarget(stmt, isFwd));		
	}

	public static StmtIterator listProperties(Resource s, Property p, boolean isFwd) {
		StmtIterator result = isFwd
				? ResourceUtils.listProperties(s, p)
				: ResourceUtils.listReverseProperties(s, p);
		return result;
	}


}
