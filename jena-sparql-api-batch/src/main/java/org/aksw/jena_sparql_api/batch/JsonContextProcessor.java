package org.aksw.jena_sparql_api.batch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

/**
 * 
 * Context := {
 *     context: [
 *         myBean1: 'MyClass',
 *         myBean2: { class: 'MyClass', args: [] }
 *     ],
 * }
 * 
 * BatchProcess := {
 *     context: { // jobContext
 *     },
 *     steps: [{
 *         context: // stepContext
 *         
 *     }]
 *     
 * }
 * 
 * 
 */
public class JsonContextProcessor {
    
    private static final Logger logger = LoggerFactory.getLogger(JsonContextProcessor.class);
    
    public static final String ATTR_REF = "ref";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_CTOR_ARGS = "ctor";
    
    public void process() {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
    }
    
    
    public static void processContext(AnnotationConfigApplicationContext c, Object context, Map<String, String> classAliasMap) throws Exception {
        processContext(c, (Map<String, Object>)context, classAliasMap);
    }

    public static void processContext(AnnotationConfigApplicationContext c, Map<String, Object> context, Map<String, String> classAliasMap) throws Exception {
        for(Entry<String, Object> entry : context.entrySet()) {
            String beanName = entry.getKey();
            Object value = entry.getValue();
            
            logger.debug("Processing [" + beanName + "]");
            
            
            BeanDefinition beanDefinition = processBean(value);
            c.registerBeanDefinition(beanName, beanDefinition);
        }
    }
    
    
    public static void processJob() {
        
    }
    
    public static void processStep() {
        
    }
    
    
    public static BeanDefinition processBean(Object data) throws Exception {
        BeanDefinition result;
        if(data == null) {
            result = new GenericBeanDefinition();
        } else if(data instanceof String) {
            result = processBean((String)data);
        } else if(data instanceof List) {
            
            // List<?> args = (List<?>)data;

            BeanDefinition beanDef = new GenericBeanDefinition();
            beanDef.setBeanClassName(ArrayList.class.getCanonicalName());
            ConstructorArgumentValues cav = beanDef.getConstructorArgumentValues();
            
            List<BeanDefinition> args = processBeans((List<?>)data);
            cav.addGenericArgumentValue(args);
            
            result = beanDef;
            //result = processBeans((List<Object>)data);
        } else if(data instanceof Map) {
            result = processBean((Map<String, Object>)data);
        } else {
            throw new RuntimeException("Unexpected type: " + data);
        }
        
        return result;
    }

    public static BeanDefinition processBean(String value) throws Exception {
        BeanDefinition result = new GenericBeanDefinition();
        result.setBeanClassName(String.class.getCanonicalName());
        result.getConstructorArgumentValues().addGenericArgumentValue(value);
        //Class<?> clazz = Class.forName(className);
        //Object result = clazz.newInstance();
        return result;
    }

//    public static BeanDefinition processBean(String className) throws Exception {
//        BeanDefinition result = new GenericBeanDefinition();
//        result.setBeanClassName(className);
//        //Class<?> clazz = Class.forName(className);
//        //Object result = clazz.newInstance();
//        return result;
//    }
    
    public static List<BeanDefinition> processBeans(List<?> items) throws Exception {
        List<BeanDefinition> result = new ArrayList<BeanDefinition>();
        for(Object item : items) {
            BeanDefinition bean = processBean(item);
            result.add(bean);
        }
        
        return result;
    }
    
    
    public static Object processAttr(Object data) throws Exception {
        Object result;

        if(data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>)data;
            result = processAttrMap(map);
        //} else if(data instanceof List) {
        } else {
            result = data;
            //Class<?> type = data == null ? null : data.getClass();
            //throw new RuntimeException("Unknown attribute type: " + data + " [" + type + "]");
        }
        
        return result;
    }

    public static Object processAttrMap(Map<String, Object> data) {
        Object result;
        Object _ref = data.get(ATTR_REF);
        if(_ref != null) {
            Assert.isInstanceOf(String.class, _ref);
            String ref = (String)_ref;
            result = new RuntimeBeanReference(ref);
        } else {
            result = null;
        }
        
        return result;
    }

    public static BeanDefinition processBean(Map<String, Object> data, String key) throws Exception {
        Object beanSpec = data.get(key);
        BeanDefinition result = processBean(beanSpec);
        return result;
    }
    
    public static BeanDefinition processBean(Map<String, Object> data) throws Exception {
        
        BeanDefinition result = new GenericBeanDefinition();

        // Process special attributes
        Object _clazz = data.get(ATTR_TYPE);
        if(_clazz != null) {
            Assert.isInstanceOf(String.class, _clazz);
            String clazz = (String)_clazz;
            result.setBeanClassName(clazz);
        }

        // check for ctor args
        Object _ctorArgs = data.get(ATTR_CTOR_ARGS);
        if(_ctorArgs != null) {
            Assert.isInstanceOf(List.class, _ctorArgs);
            List<?> ctorArgs = (List<?>)_ctorArgs;
            List<BeanDefinition> args = processBeans(ctorArgs);
            
            ConstructorArgumentValues cav = result.getConstructorArgumentValues();
            for(BeanDefinition arg : args) {
                //cav.add
                //ValueHolder holder = new ValueHolder(null);
                //holder.get
                cav.addGenericArgumentValue(arg);
            }
        }
        
        // Create a new map with special attributes removed
        Map<String, Object> tmp = data;
        data = new HashMap<String, Object>(tmp);
        data.remove(ATTR_TYPE);
        data.remove(ATTR_CTOR_ARGS);
        
        
        
        // Default handling of attributes
        for(Entry<String, Object> entry : data.entrySet()) {

            String key = entry.getKey();
            Object value = entry.getValue();
            
            Object obj = processAttr(value);
            //result.setAttribute(key, obj);
            result.getPropertyValues().add(key, obj);
            //result.getPropertyValues()
        }
        
               
        return result;
        
        
        //values.addPropertyValue("beanProperty", new RuntimeBeanReference("beanName"));
    }
    
    
    public static void processConstructorArgumentValues() {
        
    }
    
    public static void processCtorArgs() {
        ConstructorArgumentValues cav = new ConstructorArgumentValues();
        //<constructor-arg type="java.lang.String" value="Zara"/>
        //cav.addGenericArgumentValue(value);
        //cav.addGenericArgumentValue(value, type);
    }
    
    
    /**
     * Resolves the value of a given attribute
     * Can be:
     * - Primitive value, such as: "a string", 10 (integer)
     * - Lazy Reference: An object with only attribute {ref: "ref target"}.
     * - Object { class: someClass } or HashMap (an object without class attribute)
     * - ArrayList ([item1, ..., itemN])
     * - 
     * 
     * @param map
     */
//    public static <T> resolveAttributeValue(Map<String, Object> map) {
//        ValueHolder vh;
//        return null;
//    }
    
    public static void processBeanDefinition(BeanDefinitionRegistry registry, String beanName, Map<String, Object> map) {
        ConstructorArgumentValues cav;
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        
        MutablePropertyValues propertyValues = new MutablePropertyValues();
        propertyValues.addPropertyValues(map);
        beanDef.setPropertyValues(propertyValues);

        @SuppressWarnings("unchecked")
        Map<String, Object> attrs = (Map<String, Object>)map.get("attrs");
        if(attrs != null) {
//            Map<String, Object> attrs = (Map<String, Object>)attrs;
            for(Entry<String, Object> attr : attrs.entrySet()) {
                beanDef.setAttribute(attr.getKey(), attr.getValue());
            }
        }
        
        //BeanUtils.
        String beanClassName = (String)map.get("class");
        beanDef.setBeanClassName(beanClassName);
        
        //beanDef.setAttribute(name, value);
        //registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
