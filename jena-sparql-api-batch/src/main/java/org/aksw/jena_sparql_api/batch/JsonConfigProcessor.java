package org.aksw.jena_sparql_api.batch;

import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;

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
public class JsonConfigProcessor {
    public void process() {
        MethodInvokingFactoryBean bean = new MethodInvokingFactoryBean();
        //bean.set
    }
    
    
    public static void processContext(Map<String, Object> context) {
        
        for(Entry<String, Object> entry : context.entrySet()) {
            Object beanDef = entry.getValue();
            //processBean(beanDef);
            
            
        }
    }
    
    
    public static void processJob() {
        
    }
    
    public static void processStep() {
        
    }
    
    
    public static void processConstructorArgumentValues() {
        
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
        //cav.
        GenericBeanDefinition beanDef = new GenericBeanDefinition();
        //beanDef.setConstructorArgumentValues(constructorArgumentValues);
        
        
        
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
