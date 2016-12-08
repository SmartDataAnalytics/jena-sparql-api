package org.aksw.jena_sparql_api.mapper.util;

import java.beans.PropertyDescriptor;
import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

public class BeanUtils {
    
    public static Set<String> getPropertyNames(Class<?> clazz) {
        BeanWrapper beanInfo = new BeanWrapperImpl(clazz);
        Set<String> result = getPropertyNames(beanInfo);
        return result;
    }

    public static Set<String> getPropertyNames(Object o) {
        BeanWrapper beanInfo = new BeanWrapperImpl(o);
        Set<String> result = getPropertyNames(beanInfo);
        return result;
    }

    public static Set<String> getPropertyNames(BeanWrapper beanInfo) {
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        
        Set<String> result = new LinkedHashSet<>(pds.length);
        for(PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            result.add(pd.getName());
        }
        
        return result;
    }
}
