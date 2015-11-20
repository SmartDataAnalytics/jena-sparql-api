package org.aksw.jena_sparql_api.mapper.model;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
import org.aksw.jena_sparql_api.utils.UriUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.ReflectionUtils;

import com.google.common.base.Function;
import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

public class RdfClassFactory {

    private static final Logger logger = LoggerFactory.getLogger(RdfClassFactory.class);

    /*
     * SpEL parser and evaluator
     */
    protected ExpressionParser parser;
    protected EvaluationContext evalContext;
    protected ParserContext parserContext;

    protected Prologue prologue;
    protected SparqlRelationParser relationParser;

    protected Map<Class<?>, RdfClass> classToMapping = new HashMap<Class<?>, RdfClass>();
    protected TypeMapper typeMapper;

    public RdfClassFactory(ExpressionParser parser, ParserContext parserContext, EvaluationContext evalContext, TypeMapper typeMapper, Prologue prologue, SparqlRelationParser relationParser) {
        super();
        this.parser = parser;
        this.evalContext = evalContext;
        this.parserContext = parserContext;
        this.typeMapper = typeMapper;
        this.prologue = prologue;
        this.relationParser = relationParser;
    }

    public Prologue getPrologue() {
        return prologue;
    }


    public RdfClass create(Class<?> clazz) {
        RdfClass result;
        try {
            result = _create(clazz);
        } catch (IntrospectionException e) {
            throw new RuntimeException(e);
        }
        return result;
    }


    public static <A extends Annotation> A getAnnotation(Class<?> clazz, PropertyDescriptor pd, Class<A> annotation) {
        A result;

        String propertyName = pd.getName();
        Field f = ReflectionUtils.findField(clazz, propertyName);
        result = f != null
                ? f.getAnnotation(annotation)
                : null
                ;

        result = result == null && pd.getReadMethod() != null
                ? AnnotationUtils.findAnnotation(pd.getReadMethod(), annotation)
                : result
                ;

        result = result == null && pd.getWriteMethod() != null
                ? AnnotationUtils.findAnnotation(pd.getWriteMethod(), annotation)
                : result
                ;



        return result;
    }


    /**
     * Allocates a new RdfClass object for a given java class or returns an
     * existing one. Does not populate property descriptors.
     *
     * @param clazz
     * @return
     */
    protected RdfClass getOrAllocate(Class<?> clazz) {
        RdfClass result = classToMapping.get(clazz);
        if(result == null) {
            result = allocate(clazz);
            classToMapping.put(clazz, result);
        }
        return result;
    }


    /**
     * Allocates a new RdfClass object for a given java class.
     * Does not populate property descriptors.
     *
     * @param clazz
     * @return
     */
    protected RdfClass allocate(Class<?> clazz) {
        RdfType rdfType = clazz.getAnnotation(RdfType.class);

        DefaultIri defaultIri = clazz.getAnnotation(DefaultIri.class);

        Function<Object, String> defaultIriFn = null;
        if (defaultIri != null) {
            String iriStr = defaultIri.value();
            Expression expression = parser.parseExpression(iriStr,
                    parserContext);
            defaultIriFn = new F_GetValue<String>(String.class, expression,
                    evalContext);
        }

        RdfClass result = new RdfClass(clazz, defaultIriFn, prologue);

        return result;
    }

    protected String resolveIriExpr(String exprStr) {
        Expression expression = parser.parseExpression(exprStr, parserContext);
        String tmp = expression.getValue(evalContext, String.class);
        tmp = tmp.trim();

        PrefixMapping prefixMapping = prologue.getPrefixMapping();
        String result = prefixMapping.expandPrefix(tmp);
        return result;
    }


    protected RdfClass _create(Class<?> clazz) throws IntrospectionException {
        RdfClass result = allocate(clazz);
        populateClasses(result);
        //Map<String, RdfProperty> rdfProperties = processProperties(clazz);

        //RdfClassImpl result = new RdfClassImpl(clazz, defaultIriFn, rdfProperties, prologue);
        return result;
    }

    private void populateClasses(RdfClass rootRdfClass) {
        Set<RdfClass> open = new HashSet<RdfClass>();
        open.add(rootRdfClass);

        while(!open.isEmpty()) {
            RdfClass rdfClass = open.iterator().next();
            open.remove(rdfClass);

            populateProperties(rdfClass, open);
        }
    }


    private void populateProperties(RdfClass rdfClass, Collection<RdfClass> open) {
        if(!rdfClass.isPopulated()) {
            Map<String, RdfProperty> rdfProperties = new LinkedHashMap<String, RdfProperty>();

            Class<?> clazz = rdfClass.getTargetClass();


            BeanWrapper beanInfo = new BeanWrapperImpl(clazz);
            //BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
            for(PropertyDescriptor pd : pds) {
                String propertyName = pd.getName();
                boolean isReadable = beanInfo.isReadableProperty(propertyName);
                boolean isWritable = beanInfo.isWritableProperty(propertyName);

                Class<?> propertyType = pd.getPropertyType();
                RDFDatatype dtype = typeMapper.getTypeByClass(propertyType);
                boolean isLiteral = dtype != null;

                boolean isCandidate = isReadable && isWritable;


                Iri iriAnn = getAnnotation(clazz, pd, Iri.class);
                String iriExprStr = iriAnn == null ? null : iriAnn.value();
                String iriStr = iriExprStr == null ? null : resolveIriExpr(iriExprStr);
                boolean hasIri = iriStr != null && !iriStr.isEmpty();

                if(isCandidate && hasIri) {
                    logger.debug("Annotation on property " + propertyName + " detected: " + iriStr);

                    Node predicate = NodeFactory.createURI(iriStr);

                    RdfProperty rdfProperty = isLiteral
                        ? processDatatypeProperty(beanInfo, pd, predicate, dtype)
                        : processObjectProperty(beanInfo, pd, predicate, open)
                        ;

                    if(rdfProperty != null) {
                        rdfProperties.put(propertyName, rdfProperty);
                    }
                } else {
                    logger.debug("Ignoring property " + propertyName);
                }
            }

            rdfClass.propertyToMapping = rdfProperties;
        }
    }



    /**
     *
     *
     * @param beanInfo
     * @param pd
     * @param dtype
     * @return
     */
    protected RdfProperty processDatatypeProperty(BeanWrapper beanInfo, PropertyDescriptor pd, Node predicate, RDFDatatype dtype) {
        Class<?> beanClass = beanInfo.getWrappedClass();
        Class<?> propertyType = pd.getPropertyType();

        IriType iriType = getAnnotation(beanClass, pd, IriType.class);


        RdfValueMapper rdfValueMapper;
        if(iriType == null) {
            //RDFDatatype dtype = typeMapper.getTypeByClass(propertyType);
            rdfValueMapper = new RdfValueMapperSimple(propertyType, dtype, null);
        } else {
            rdfValueMapper = new RdfValueMapperStringIri();
        }


        RdfProperty result = new RdfPropertyDatatype(beanInfo, pd, null, predicate, rdfValueMapper);
        return result;
    }

    /**
     * Process a property with a complex value
     *
     * @param beanInfo
     * @param pd
     * @param open
     * @return
     */
    protected RdfProperty processObjectProperty(BeanWrapper beanInfo, PropertyDescriptor pd, Node predicate, Collection<RdfClass> open) {
        PrefixMapping prefixMapping = prologue.getPrefixMapping();

        RdfProperty result;

        String propertyName = pd.getName();
        //System.out.println("PropertyName: " + propertyName);


        // If necessary, add the target class to the set of classes that yet
        // need to be populated
        Class<?> targetClass = pd.getPropertyType();
        RdfClass trc = getOrAllocate(targetClass);
        if(!trc.isPopulated()) {
            open.add(trc);
        }

        Relation relation = RelationUtils.createRelation(predicate.getURI(), false, prefixMapping);
        result = new RdfProperyObject(propertyName, relation, trc);


//        Iri iri = getAnnotation(sourceClass, pd, Iri.class);
//        if(iri != null) {
//            String iriStr = iri.value();
//
//            //Relation relation = relationParser.apply(iriStr);
//            Relation relation = RelationUtils.createRelation(iriStr, false, prefixMapping);
//            result = new RdfProperyObject(propertyName, relation, trc);
//
//            logger.debug("Annotation on property " + propertyName + " detected: " + iri.value());
//        } else {
//            result = null;
//            logger.debug("Ignoring property " + propertyName);
//            //throw new RuntimeException("should not happen");
//        }

        return result;
    }

    public static RdfClassFactory createDefault() {
        Prologue prologue = new Prologue();
        RdfClassFactory result = createDefault(prologue);
        return result;
    }

    public static RdfClassFactory createDefault(Prologue prologue) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        TemplateParserContext parserContext = new TemplateParserContext();

        try {
            evalContext.registerFunction("md5", StringUtils.class.getDeclaredMethod("md5Hash", new Class[] { String.class }));
            evalContext.registerFunction("localName", UriUtils.class.getDeclaredMethod("getLocalName", new Class[] { String.class }));
            evalContext.registerFunction("nameSpace", UriUtils.class.getDeclaredMethod("getNameSpace", new Class[] { String.class }));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ExpressionParser parser = new SpelExpressionParser();

        SparqlRelationParser relationParser = SparqlRelationParserImpl.create(Syntax.syntaxARQ, prologue);

        TypeMapper typeMapper = TypeMapper.getInstance();
        RdfClassFactory result = new RdfClassFactory(parser, parserContext, evalContext, typeMapper, prologue, relationParser);
        return result;
    }
}
