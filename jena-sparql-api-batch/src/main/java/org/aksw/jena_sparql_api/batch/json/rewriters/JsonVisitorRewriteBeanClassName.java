package org.aksw.jena_sparql_api.batch.json.rewriters;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.aksw.gson.utils.JsonUtils;
import org.aksw.gson.utils.JsonVisitorRewrite;
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
public class JsonVisitorRewriteBeanClassName
    extends JsonVisitorRewrite
{

    public JsonVisitorRewriteBeanClassName() {
    }

    @Override
    public JsonElement visit(JsonObject json) {
        JsonObject result;

        if(json.has("type") && !json.has("beanClassName")) {
            result = new JsonObject();

            for(Entry<String, JsonElement> entry : json.entrySet()) {
                String key = entry.getKey();
                JsonElement val = entry.getValue();

                if("type".equals(key)) {
                    result.add("beanClassName", val);
                } else {
                    result.add(key, val);
                }
            }
        } else {
            result = json;
        }

        return result;
    }
}