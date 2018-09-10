package org.aksw.jena_sparql_api.beans.model;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

public class MyAnnotationUtils {
    public static <A extends Annotation> A findPropertyAnnotation(Class<?> clazz, PropertyDescriptor pd, Class<A> annotation) {
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
}
