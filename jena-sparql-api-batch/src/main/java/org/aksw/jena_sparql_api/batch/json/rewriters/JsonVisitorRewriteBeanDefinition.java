package org.aksw.jena_sparql_api.batch.json.rewriters;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.gson.utils.JsonVisitorRewrite;
import org.aksw.spring.json.ContextProcessorJsonUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * If a json document which does not have a properties key, all keys which are not part of Generic Bean definition will be moved to
 * the properties section
 *
 * {
 *   type: 'some.java.class.name',
 *   nonBeanDefinitionKey: 'foo'
 * }
 *
 * {
 *   type: 'some.java.class.name',
 *   properties: {
 *     nonBeanDefinitionKey: 'foo'
 *   }
 * }
 *
 * @author raven
 *
 */
public class JsonVisitorRewriteBeanDefinition
    extends JsonVisitorRewrite
{
    protected String subDocKey;
    protected Predicate<String> isTransferNeeded;

    public static Set<String> getBeanPropertyNames(Class<?> clazz) {
        Set<String> result = new HashSet<String>();
        BeanWrapper beanWrapper = new BeanWrapperImpl(GenericBeanDefinition.class);
        for(PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
            result.add(pd.getName());
        }
        return result;
    }

    public JsonVisitorRewriteBeanDefinition() {
        subDocKey = "properties";

        Set<String> propertyNames = getBeanPropertyNames(GenericBeanDefinition.class);
        propertyNames.addAll(ContextProcessorJsonUtils.specialAttributes);
        propertyNames.remove("source");

        isTransferNeeded = Predicates.not(Predicates.in(propertyNames));
    }

    @Override
    public JsonElement visit(JsonObject json) {
        JsonObject result;

        if(json.has("beanClassName") && !json.has(subDocKey)) {
            result = new JsonObject();
            JsonObject tmp = new JsonObject();

            for(Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                JsonElement val = entry.getValue();

                boolean move = isTransferNeeded.apply(key);
                if(move) {
                    tmp.add(key, val);
                } else {
                    result.add(key, val);
                }
            }

            result.add(subDocKey, tmp);
        } else {
            result = json;
        }

        return result;
    }
}