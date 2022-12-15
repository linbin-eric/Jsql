package com.jfirer.jsql.mapper;

import com.jfirer.jsql.model.Param;

import java.util.List;

/**
 * 标记注解，该注解的泛型对应了其JPA语句中的实体
 *
 * @param <T>
 */
public interface Repository<T>
{
    T findOne(Param param);

    List<T> findList(Param param);

    int delete(Param param);

    int insert(T entity);

    int save(T entity);

    int update(T entity);
}
