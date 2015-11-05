package org.aksw.jena_sparql_api.mapper.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import org.aksw.commons.util.strings.StringUtils;
import org.aksw.jena_sparql_api.concepts.Relation;
import org.aksw.jena_sparql_api.concepts.RelationUtils;
import org.aksw.jena_sparql_api.mapper.annotation.DefaultIri;
import org.aksw.jena_sparql_api.mapper.annotation.Iri;
import org.aksw.jena_sparql_api.mapper.annotation.RdfType;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParser;
import org.aksw.jena_sparql_api.stmt.SparqlRelationParserImpl;
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
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.core.Prologue;

public class RdfClassFactory {

    /*
     * SpEL parser and evaluator
     */
    protected ExpressionParser parser;
    protected EvaluationContext evalContext;
    protected ParserContext parserContext;

    protected Prologue prologue;
    protected SparqlRelationParser relationParser;



    public RdfClassFactory(ExpressionParser parser, ParserContext parserContext, EvaluationContext evalContext, Prologue prologue, SparqlRelationParser relationParser) {
        super();
        this.parser = parser;
        this.evalContext = evalContext;
        this.parserContext = parserContext;
        this.prologue = prologue;
        this.relationParser = relationParser;
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



    protected RdfClass _create(Class<?> clazz) throws IntrospectionException {
        PrefixMapping prefixMapping = prologue.getPrefixMapping();

        RdfType x = clazz.getAnnotation(RdfType.class);
        DefaultIri defaultIri = clazz.getAnnotation(DefaultIri.class);

        Function<Object, String> defaultIriFn = null;
        if(defaultIri != null) {
            String iriStr = defaultIri.value();
            Expression expression = parser.parseExpression(iriStr, parserContext);
             defaultIriFn = new F_GetValue<String>(String.class, expression, evalContext);
        }

        Map<String, RdfProperty> rdfProperties = new LinkedHashMap<String, RdfProperty>();

        BeanInfo bi = Introspector.getBeanInfo(clazz);
        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
        for(PropertyDescriptor pd : pds) {
            String propertyName = pd.getName();
            System.out.println("PropertyName: " + propertyName);

            Iri iri = getAnnotation(clazz, pd, Iri.class);
            if(iri != null) {
                String iriStr = iri.value();

                //Relation relation = relationParser.apply(iriStr);


                Relation relation = RelationUtils.createRelation(iriStr, false, prefixMapping);

                RdfProperty rdfProperty = new RdfProperty(propertyName, relation);
                rdfProperties.put(propertyName, rdfProperty);

                System.out.println("--- Found anno: " + iri.value());
            }
        }

        RdfClass result = new RdfClass(clazz, defaultIriFn, rdfProperties, prologue);
        return result;



//        Expression expr = parser.parseExpression(exprStr, parserContext);
//        String rawIri = expr.getValue(evalContext, w, String.class);
//        System.out.println(rawIri);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ExpressionParser parser = new SpelExpressionParser();

        SparqlRelationParser relationParser = SparqlRelationParserImpl.create(Syntax.syntaxARQ, prologue);

        RdfClassFactory result = new RdfClassFactory(parser, parserContext, evalContext, prologue, relationParser);
        return result;
    }
}
