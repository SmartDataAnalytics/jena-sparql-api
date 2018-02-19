package org.aksw.jena_sparql_api.mapper.impl.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.beans.model.EntityModel;
import org.aksw.jena_sparql_api.beans.model.EntityOps;
import org.aksw.jena_sparql_api.beans.model.PropertyOps;
import org.aksw.jena_sparql_api.mapper.annotation.Datatype;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.IriNs;
import org.aksw.jena_sparql_api.mapper.annotation.IriType;
import org.aksw.jena_sparql_api.mapper.annotation.MappedBy;
import org.aksw.jena_sparql_api.mapper.annotation.MultiValued;
import org.aksw.jena_sparql_api.mapper.model.F_GetValue;
import org.aksw.jena_sparql_api.mapper.model.RdfMapperProperty;
import org.aksw.jena_sparql_api.mapper.model.RdfMapperPropertyMulti;
import org.aksw.jena_sparql_api.mapper.model.RdfMapperPropertySingle;
import org.aksw.jena_sparql_api.mapper.model.RdfType;
import org.aksw.jena_sparql_api.mapper.model.RdfTypeFactory;
import org.aksw.jena_sparql_api.mapper.model.TypeConversionService;
import org.aksw.jena_sparql_api.mapper.model.TypeConversionServiceImpl;
import org.aksw.jena_sparql_api.mapper.model.TypeConverter;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
import org.aksw.jena_sparql_api.util.frontier.Frontier;
import org.aksw.jena_sparql_api.util.frontier.FrontierImpl;
import org.aksw.jena_sparql_api.utils.UriUtils;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.core.Prologue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.convert.ConversionService;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

public class RdfTypeFactoryImpl
    implements RdfTypeFactory
{

    private static final Logger logger = LoggerFactory.getLogger(RdfTypeFactoryImpl.class);

    /*
     * SpEL parser and evaluator
     */
    protected ExpressionParser parser;
    protected EvaluationContext evalContext;
    protected ParserContext parserContext;

    protected Prologue prologue;
    protected SparqlRelationParser relationParser;

    protected Function<Class<?>, EntityOps> entityOpsFactory;
    //protected Map<Class<?>, EntityOps> entityOpsCache;

    protected Map<Class<?>, RdfType> classToRdfType = new HashMap<Class<?>, RdfType>();
    protected TypeMapper typeMapper;
    protected ConversionService conversionService;


    protected TypeConversionService typeConversionService = new TypeConversionServiceImpl();

    public RdfTypeFactoryImpl(
            ExpressionParser parser,
            ParserContext parserContext,
            EvaluationContext evalContext,
            TypeMapper typeMapper,
            Prologue prologue,
            SparqlRelationParser relationParser,
            Function<Class<?>, EntityOps> entityOpsFactory,
            ConversionService conversionService) {
        super();
        this.parser = parser;
        this.evalContext = evalContext;
        this.parserContext = parserContext;
        this.typeMapper = typeMapper;
        this.prologue = prologue;
        this.relationParser = relationParser;
        this.entityOpsFactory = entityOpsFactory;
        this.conversionService = conversionService;
    }

    public Function<Class<?>, EntityOps> getEntityOpsFactory() {
        return entityOpsFactory;
    }

    public Prologue getPrologue() {
        return prologue;
    }

    public Map<Class<?>, RdfType> getClassToRdfType() {
        return classToRdfType;
    }


    public TypeConversionService getTypeConversionService() {
        return typeConversionService;
    }

    @Override
    public RdfType forJavaType(Class<?> clazz) {
        RdfType result = getOrAllocateRdfType(clazz);

        if(result instanceof RdfClass) {
            RdfClass tmp = (RdfClass)result;
            populateClasses(tmp);
        }

        return result;
    }

    /**
     * Allocates a new RdfClass object for a given java class or returns an
     * existing one. Does not populate property descriptors.
     *
     * @param clazz
     * @return
     */
    protected RdfType getOrAllocateRdfType(Class<?> clazz) {
        RdfType result = classToRdfType.get(clazz);
        if(result == null) {
            result = allocateRdfType(clazz);
            classToRdfType.put(clazz, result);
        }
        return result;
    }

    protected RdfType allocateRdfType(Class<?> clazz) {
        RDFDatatype dtype = typeMapper.getTypeByClass(clazz);
        boolean isPrimitive = dtype != null;

        RdfType result;
        if(isPrimitive) {
            result = new RdfTypeLiteralTyped(this, dtype);
        } else {
            result = allocateRdfClass(clazz);
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
    protected RdfClass allocateRdfClass(Class<?> clazz) {
        EntityOps entityOps = entityOpsFactory.apply(clazz);
        RdfClass result = allocateRdfClass(entityOps);
        return result;
    }

    protected RdfClass allocateRdfClass(EntityOps entityOps) {

        //org.aksw.jena_sparql_api.mapper.annotation.RdfType rdfType = AnnotationUtils.findAnnotation(clazz, org.aksw.jena_sparql_api.mapper.annotation.RdfType.class);
        org.aksw.jena_sparql_api.mapper.annotation.RdfType rdfType = entityOps.findAnnotation(org.aksw.jena_sparql_api.mapper.annotation.RdfType.class);

        //DefaultIri defaultIri = AnnotationUtils.findAnnotation(clazz, DefaultIri.class);
        DefaultIri defaultIri = entityOps.findAnnotation(DefaultIri.class);

        Function<Object, String> defaultIriFn = null;
        if (defaultIri != null) {
            String iriStr = defaultIri.value();
//            Expression expression = parser.parseExpression(iriStr,
//                    parserContext);
            defaultIriFn = (o) -> resolveIriExpr(iriStr, o);
                    //new F_GetValue<String>(String.class, expression,
                    //evalContext).andThen(RdfTypeFactory::resolveIriExpr);
        }

        RdfClass result = new RdfClass(entityOps, defaultIriFn);

        return result;
    }

    protected String resolveIriExpr(String exprStr, Object rootObject) {
        Expression expression = parser.parseExpression(exprStr, parserContext);
        String tmp = expression.getValue(evalContext, rootObject, String.class);
        tmp = tmp.trim();

        PrefixMapping prefixMapping = prologue.getPrefixMapping();
        String result = prefixMapping.expandPrefix(tmp);
        return result;
    }

    private void populateClasses(RdfClass rootRdfClass) {
        Frontier<RdfClass> frontier = FrontierImpl.createIdentityFrontier();

        frontier.add(rootRdfClass);
        while(!frontier.isEmpty()) {
            RdfClass rdfClass = frontier.next();
            initProperties(rdfClass, frontier);
        }

    }

    private void initProperties(RdfClass rdfClass, Frontier<RdfClass> frontier) {
        if(!rdfClass.isPopulated()) {
            //Map<String, RdfPopulatorProperty> rdfProperties = new LinkedHashMap<String, RdfPopulatorProperty>();

            //Class<?> clazz = rdfClass.getEntityClass();
            logger.debug("Initializing RdfClass for [" + rdfClass.getEntityOps().getAssociatedClass().getName() + "]");

//            if(Iterable.class.isAssignableFrom(clazz)) {
//                System.out.println("Collection type detected");
//                throw new RuntimeException("Implement me");
//            }


            //BeanWrapper beanInfo = new BeanWrapperImpl(clazz);
            //BeanInfo beanInfo = Introspector.getBeanInfo(clazz);

            EntityOps entityOps = rdfClass.getEntityOps();
            //PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            for(PropertyOps pd : entityOps.getProperties()) {
                //String propertyName = pd.getName();
                processProperty(rdfClass, entityOps, pd, frontier);
//                RdfPopulatorProperty rdfPopulator = processProperty(rdfClass, beanInfo, pd, open);
//
//                if(rdfPopulator != null) {
//                	rdfClass.addPopulator(rdfPopulator);
//                    //rdfProperties.put(propertyName, rdfProperty);
//                }

            }

            //rdfClass.propertyToMapping = rdfProperties;
        }
        rdfClass.setPopulated(true);
    }


    //RdfPopulatorProperty
    protected void processProperty(RdfClass rdfClass, EntityOps entityOps, PropertyOps pd, Frontier<RdfClass> frontier) {
        RdfMapperProperty result = null;

        Class<?> clazz = entityOps.getAssociatedClass();
        String propertyName = pd.getName();
        boolean isReadable = pd.isReadable();
        boolean isWritable = pd.isWritable();


//        RDFDatatype dtype = typeMapper.getTypeByClass(propertyType);
        boolean isCandidate = isReadable && isWritable;


        Iri iriAnn = pd.findAnnotation(Iri.class); //findPropertyAnnotation(clazz, pd, Iri.class);
        String iriExprStr = iriAnn == null ? null : iriAnn.value();
        String iriStr = iriExprStr == null ? null : resolveIriExpr(iriExprStr, null);

        IriNs iriNsAnn = pd.findAnnotation(IriNs.class);
        String iriNsExprStr = iriNsAnn == null ? null : iriNsAnn.value();
        iriNsExprStr = iriNsExprStr == null ? null : iriNsExprStr + (iriNsExprStr.contains(":") ? "" : ":") + propertyName;
        String iriNsStr = iriNsExprStr == null ? null : resolveIriExpr(iriNsExprStr, null);

        if(iriNsStr != null) {
            if(iriStr != null) {
                throw new RuntimeException("@Iri and @IriNs annotations on same element is invalid");
            }

            iriStr = iriNsStr;
        }




        boolean hasIri = iriStr != null && !iriStr.isEmpty();

        String mappedBy = (String)AnnotationUtils.getValue(pd.findAnnotation(MappedBy.class)); //findPropertyAnnotation(clazz, pd, MappedBy.class));
        boolean isMappedBy = mappedBy != null;


        //System.out.println("MappedBy " + mappedBy);
        //logger.debug("MappedBy " + mappedBy)


        if(isCandidate && (hasIri || isMappedBy)) {
            logger.debug("Annotation on property " + propertyName + " detected: " + iriStr);

            Property predicate = iriStr != null ? ResourceFactory.createProperty(iriStr) : null;

            processProperty(rdfClass, entityOps, pd, predicate, frontier);

//            result = isClass
//                ? processDatatypeProperty(beanInfo, pd, predicate, dtype)
//                : processObjectProperty(beanInfo, pd, predicate, open)
//                ;

        } else {
            logger.debug("Ignoring property " + propertyName);
        }

    }

    public static Class<?> extractItemType(Type genericType) {
        Class<?> result = null;
        if(genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)genericType;
            java.lang.reflect.Type[] types = pt.getActualTypeArguments();
            if(types.length == 1) {
                result = (Class<?>)types[0];
            }

        }

        return result;
    }

    protected void processProperty(RdfClass rdfClass, EntityOps beanInfo, PropertyOps pd, Property predicate, Frontier<RdfClass> frontier) {
        Class<?> entityClass = beanInfo.getAssociatedClass();

        String propertyName = pd.getName();

        Class<?> propertyType = pd.getType();


        boolean isCollectionProperty = Collection.class.isAssignableFrom(propertyType);
        boolean isMultiValued = pd.findAnnotation(MultiValued.class) != null; //findPropertyAnnotation(entityClazz, pd, MultiValued.class) != null;

        if(isMultiValued && !isCollectionProperty) {
            throw new RuntimeException("Invalid annotation: " + entityClass + "." + pd.getName() + ": " + " is declared MultiValued however is not a Collection");
        }

        if(isCollectionProperty) {
            Type paramType = pd.getWriteMethod().getGenericParameterTypes()[0];
            //Class<?> itemType = extractItemType(paramType);
            propertyType = extractItemType(paramType);
            //targetRdfType = getOrAllocate(itemType);
        }


        RDFDatatype dtype = typeMapper.getTypeByClass(propertyType);
        boolean isLiteral = dtype != null;

        IriType iriType = pd.findAnnotation(IriType.class);
        RdfType targetRdfType;
        if(iriType == null) {
            if(isLiteral) {
                targetRdfType = new RdfTypeLiteralTyped(this, dtype);
            } else {
              targetRdfType = getOrAllocateRdfType(propertyType);
              if(targetRdfType instanceof RdfClass) {
                  RdfClass tmp = (RdfClass)targetRdfType;
                  if(!tmp.isPopulated()) {
                      frontier.add(tmp);
                  }
              }
            }
        } else {
            Assert.isTrue(String.class.isAssignableFrom(propertyType), "propertyType expected to be (a subclass) of String - but got: " + propertyType);
            targetRdfType = new RdfTypeIriStr();
        }


        DefaultIri defaultIri = pd.findAnnotation(DefaultIri.class);
        BiFunction<Object, Object, Node> defaultIriFn;
        if(defaultIri == null) {
             defaultIriFn = (entity, value) -> targetRdfType.getRootNode(value);
        } else {
            String defaultIriStr = defaultIri.value();
            Expression expression = parser.parseExpression(defaultIriStr,
                    parserContext);

            Function<Object, Node> defaultIriFnTmp = new F_GetValue<String>(String.class, expression,
                    evalContext).andThen(iri -> NodeFactory.createURI(iri));

            defaultIriFn = (entity, value) -> defaultIriFnTmp.apply(entity);

        }



        TypeConverter typeConverter = null;
        Datatype dt = pd.findAnnotation(Datatype.class);
        if(dt != null) {
            String rawDtName = dt.value();
            String dtName = resolveIriExpr(rawDtName, null);
            typeConverter = typeConversionService.getConverter(dtName, propertyType);
            if(typeConverter == null) {
                throw new RuntimeException("Could not find a type converter: " + dtName + " -> " + propertyType);
            }
        }
        //typeConversionService.getConverter(datatypeIri, clazz)

        //Relation relation = RelationUtils.createRelation(predicate.getURI(), false, prefixMapping);
        //typeConversionService.getConverter(overrideRdfType, pd.getType());


        //RdfProperty result = new RdfPropertyDatatypeOld(beanInfo, pd, null, predicate, rdfValueMapper);
        RdfPropertyDescriptor descriptor = new RdfPropertyDescriptor(propertyName, targetRdfType, "");
        RdfMapperProperty populator = isCollectionProperty
                ? new RdfMapperPropertyMulti(pd, predicate, targetRdfType, defaultIriFn, typeConverter)
                : new RdfMapperPropertySingle(pd, predicate, targetRdfType, defaultIriFn, typeConverter)
                ;

        rdfClass.addPropertyDescriptor(descriptor);
        rdfClass.addPropertyMapper(populator);

        //return result;

    }

    public static RdfTypeFactoryImpl createDefault() {
        RdfTypeFactoryImpl result = createDefault(null);
        return result;
    }

    public static RdfTypeFactoryImpl createDefault(Prologue prologue) {
        RdfTypeFactoryImpl result = createDefault(prologue, null, null);
        return result;
    }

    public static RdfTypeFactoryImpl createDefault(Prologue prologue, Function<Class<?>, EntityOps> entityOpsFactory, ConversionService _conversionService) {
        prologue = prologue != null ? prologue : new Prologue();


        ConversionService conversionService;
        if(_conversionService == null) {
            ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
            bean.afterPropertiesSet();
            conversionService = bean.getObject();
        } else {
            conversionService = _conversionService;
        }


        entityOpsFactory = entityOpsFactory != null
                ? entityOpsFactory
                : (clazz) -> EntityModel.createDefaultModel(clazz, conversionService);

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
        RdfTypeFactoryImpl result = new RdfTypeFactoryImpl(parser, parserContext, evalContext, typeMapper, prologue, relationParser, entityOpsFactory, conversionService);

        result.getClassToRdfType().put(Map.class, new RdfTypeMap(x -> (Map)x));
        
        
        result.getClassToRdfType().put(Node.class, new RdfTypeNode());
        return result;
    }
}

//
///**
//*
//*
//* @param beanInfo
//* @param pd
//* @param dtype
//* @return
//*/
//protected RdfProperty processDatatypeProperty(BeanWrapper beanInfo, PropertyDescriptor pd, Node predicate, RDFDatatype dtype) {
//  PrefixMapping prefixMapping = prologue.getPrefixMapping();
//
//  Class<?> beanClass = beanInfo.getWrappedClass();
//  Class<?> propertyType = pd.getPropertyType();
//  String propertyName = pd.getName();
//
//  IriType iriType = getAnnotation(beanClass, pd, IriType.class);
//
//
//  //RdfValueMapper rdfValueMapper;
//  org.aksw.jena_sparql_api.mapper.model.RdfType targetType;
//  if(iriType == null) {
//      //RDFDatatype dtype = typeMapper.getTypeByClass(propertyType);
//      //rdfValueMapper = new RdfValueMapperSimple(propertyType, dtype, null);
//      targetType = new RdfTypeLiteralTyped(dtype);
//  } else {
//      //rdfValueMapper = new RdfValueMapperStringIri();
//      targetType = new RdfTypeIriStr();
//  }
//
//
//  Relation relation = RelationUtils.createRelation(predicate.getURI(), false, prefixMapping);
//
//
//  //RdfProperty result = new RdfPropertyDatatypeOld(beanInfo, pd, null, predicate, rdfValueMapper);
//  RdfProperty result = new RdfPropertyImpl(propertyName, relation, targetType);
//  return result;
//}

/**
* Process a property with a complex value
*
* @param beanInfo
* @param pd
* @param open
* @return
*/
//protected RdfProperty processObjectProperty(BeanWrapper beanInfo, PropertyDescriptor pd, Node predicate, Collection<RdfClass> open) {
//  PrefixMapping prefixMapping = prologue.getPrefixMapping();
//
//  RdfProperty result;
//
//  String propertyName = pd.getName();
//  //System.out.println("PropertyName: " + propertyName);
//
//
//  // If necessary, add the target class to the set of classes that yet
//  // need to be populated
//  Class<?> targetClass = pd.getPropertyType();
//  RdfType trc = getOrAllocate(targetClass);
//  if(trc instanceof RdfClass) {
//      RdfClass tmp = (RdfClass)trc;
//      if(!tmp.isPopulated()) {
//          open.add(tmp);
//      }
//  }
//
//  Relation relation = RelationUtils.createRelation(predicate.getURI(), false, prefixMapping);
//  result = new RdfPropertyImpl(propertyName, relation, trc);
//
//
////  Iri iri = getAnnotation(sourceClass, pd, Iri.class);
////  if(iri != null) {
////      String iriStr = iri.value();
////
////      //Relation relation = relationParser.apply(iriStr);
////      Relation relation = RelationUtils.createRelation(iriStr, false, prefixMapping);
////      result = new RdfProperyObject(propertyName, relation, trc);
////
////      logger.debug("Annotation on property " + propertyName + " detected: " + iri.value());
////  } else {
////      result = null;
////      logger.debug("Ignoring property " + propertyName);
////      //throw new RuntimeException("should not happen");
////  }
//
//  return result;
//}
