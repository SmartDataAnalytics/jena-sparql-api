package org.aksw.jena_sparql_api.utils.model;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.jena.ext.com.google.common.collect.Streams;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;

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

	public static <T extends RDFNode> boolean canAsProperty(Statement stmt, Class<T> clazz) {
		boolean result = stmt.getObject().canAs(clazz);
		return result;
	}

	public static <T extends RDFNode> T getPropertyValue(Statement stmt, Class<T> clazz) {
		T result = stmt.getObject().as(clazz);
		return result;
	}

	public static boolean canAsLiteral(Statement stmt, Class<?> clazz) {
		boolean result = stmt.getObject().isLiteral() &&
				Optional.ofNullable(stmt.getObject().asLiteral().getValue())
				.map(v -> clazz.isAssignableFrom(v.getClass())).isPresent();
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getLiteralValue(Statement stmt, Class<T> clazz) {
		T result = (T)stmt.getObject().asLiteral().getValue();
		return result;
	}

	
	/*
	 * properties
	 */

	public static Stream<Statement> listProperties(Resource s, Property p) {
		Stream<Statement> result = asStream(s.listProperties(p));
		return result;
	}

	public static Stream<RDFNode> listPropertyValues(Resource s, Property p) {
		Stream<RDFNode> result = listProperties(s, p)
				.map(Statement::getObject);
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
	
	/**
	 * Get a single value chosen at random from all available values of the given property.
	 * 
	 * @param s
	 * @param p
	 * @return
	 */
	public static Optional<RDFNode> getPropertyValue(Resource s, Property p) {
		Optional<RDFNode> result = getProperty(s, p)
				.map(Statement::getObject);	
		return result;		
	}

	public static <T extends RDFNode> Stream<Statement> listProperties(Resource s, Property p, Class<T> clazz) {
		Stream<Statement> result = listProperties(s, p)
				.filter(stmt -> canAsProperty(stmt, clazz));
		return result;
	}

	
	public static <T extends RDFNode> Stream<T> listPropertyValues(Resource s, Property p, Class<T> clazz) {
		Stream<T> result = listProperties(s, p, clazz)
				.map(stmt -> getPropertyValue(stmt, clazz));
		return result;
	}

	
	public static <T extends RDFNode> Optional<Statement> getProperty(Resource s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listProperties(s, p, clazz));
		return result;		
	}

	public static <T extends RDFNode> Optional<T> getPropertyValue(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = getProperty(s, p, clazz)
				.map(stmt -> getPropertyValue(stmt, clazz));
		return result;		
	}
	

	public static <T> Stream<Statement> listLiteralProperties(Resource s, Property p, Class<T> clazz) {
		Stream<Statement> result = listProperties(s, p)
				.filter(stmt -> canAsLiteral(stmt, clazz));
		return result;
	}
	
	public static <T> Stream<T> listLiteralPropertyValues(Resource s, Property p, Class<T> clazz) {
		Stream<T> result = listLiteralProperties(s, p, clazz)
				.map(stmt -> getLiteralValue(stmt, clazz));

		return result;
	}

	public static <T> Optional<Statement> getLiteralProperty(Resource s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listLiteralProperties(s, p, clazz));
		return result;
	}
	
	public static <T> Optional<T> getLiteralPropertyValue(Resource s, Property p, Class<T> clazz) {
		Optional<T> result = getLiteralProperty(s, p, clazz)
				.map(stmt -> getLiteralValue(stmt, clazz));
		return result;		
	}

	
	// NOTE Inverse properties cannot refer to literals
	public static Stream<Statement> listReverseProperties(RDFNode s, Property p) {		
		Stream<Statement> result = asStream(s.getModel().listStatements(null, p, s));
		return result;
	}

	public static Optional<Statement> getReverseProperty(RDFNode s, Property p) {
		Optional<Statement> result = findFirst(listReverseProperties(s, p));
		return result;
	}
	
	public static <T extends Resource> boolean isReverseProperty(Statement stmt, Class<T> clazz) {
		boolean result = stmt.getSubject().canAs(clazz);
		return result;
	}

	public static <T extends Resource> Stream<Statement> listReverseProperties(RDFNode s, Property p, Class<T> clazz) {
		Stream<Statement> result = listReverseProperties(s, p)
				.filter(stmt -> isReverseProperty(stmt, clazz));
		return result;
	}
	
	public static <T extends Resource> Optional<Statement> getReverseProperty(RDFNode s, Property p, Class<T> clazz) {
		Optional<Statement> result = findFirst(listReverseProperties(s, p, clazz));
		
		return result;
	}


	public static Stream<Resource> listReversePropertyValues(Resource s, Property p) {
		Stream<Resource> result = listReverseProperties(s, p)
				.map(Statement::getSubject);
		
		return result;
	}	

	public static <T extends Resource> Stream<T> listReversePropertyValues(RDFNode s, Property p, Class<T> clazz) {
		Stream<T> result = listReverseProperties(s, p, clazz)
				.map(stmt -> stmt.getSubject().as(clazz));

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
	public static boolean replaceProperties(Model m, Stream<Statement> removals, Statement stmt) {
		List<Statement> stmts = removals
				.filter(item -> !item.equals(stmt))
				.collect(Collectors.toList());
		
		m.remove(stmts);
		
		boolean result = stmt == null ? !stmts.isEmpty() : !m.contains(stmt);
		
		if(stmt != null) {
			m.add(stmt);
		}

		return result;
	}

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
	
//	public static Predicate<Statement> createPredicateIsObjectOfType(Class<?> clazz) {
//		return stmt -> isObjectOfType()
//	}
		

	public static <T> boolean updateLiteralProperty(Resource s, Property p, Class<T> clazz, T o) {
		boolean result = replaceProperties(s.getModel(),
				listLiteralProperties(s, p, clazz),
				o == null ? null : s.getModel().createLiteralStatement(s, p, o));
		
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
		boolean result = getReverseProperty(s, p).isPresent();
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

}
