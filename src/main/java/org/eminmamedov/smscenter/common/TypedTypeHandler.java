package org.eminmamedov.smscenter.common;

import org.apache.ibatis.type.BaseTypeHandler;

/**
 * Abstract type handler for all typed type handlers
 * 
 * @author Emin Mamedov
 * 
 */
public abstract class TypedTypeHandler<T> extends BaseTypeHandler<T> {

    private Class<T> javaType;

    public TypedTypeHandler(Class<T> javaType) {
        this.javaType = javaType;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

}
