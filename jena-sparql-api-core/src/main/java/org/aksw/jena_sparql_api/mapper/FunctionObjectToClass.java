package org.aksw.jena_sparql_api.mapper;

import java.util.function.Function;

import com.google.gson.Gson;

/**
 * Convert a structure composed of nested maps and lists
 * into a java object of a certain class
 * @author raven
 *
 * @param <T>
 */
public class FunctionObjectToClass<V, T>
    implements Function<V, T>
{
    private Gson gson;
    private Class<T> clazz;

    public FunctionObjectToClass(Gson gson, Class<T> clazz) {
        this.gson = gson;
        this.clazz = clazz;
    }

    @Override
    public T apply(V obj) {
        String json = gson.toJson(obj);
        System.out.println(json);
        T result = gson.fromJson(json, clazz);

        return result;
    }
}
