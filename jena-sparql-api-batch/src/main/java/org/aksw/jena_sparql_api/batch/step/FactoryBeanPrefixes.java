package org.aksw.jena_sparql_api.batch.step;

import java.lang.reflect.Type;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import org.apache.jena.sparql.core.Prologue;


public class FactoryBeanPrefixes
    extends AbstractFactoryBean<Object>
{
    //protected static int counter = 0;



    @Autowired
    protected Gson gson;


    @Autowired
    protected Prologue prologue;



    protected JsonElement json;

    @Override
    public Class<?> getObjectType() {
        return String.class;
    }

    public JsonElement getPrefixes() {
        return json;
    }

    public void setPrefixes(JsonElement prefixes) {
        this.json = prefixes;
    }



    @Override
    protected Object createInstance()
        throws Exception
    {
        Type type = new TypeToken<Map<String, String>>(){}.getType();

        Map<String, String> map = gson.fromJson(json, type);
        //JsonObject obj = prefixes.getAsJsonObject();
        prologue.getPrefixMapping().setNsPrefixes(map);

        //return prologue.getPrefixMapping();
        //return "" + ++counter;
        return null;
    }
}
