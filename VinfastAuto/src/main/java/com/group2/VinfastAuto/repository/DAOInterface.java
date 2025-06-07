package com.group2.VinfastAuto.repository;

import java.util.List;
import java.util.Optional;

public interface DAOInterface<T, K> {
    public List<T> findAll();

    public Optional<T> findById(K id);

    public T add(T t);

    public T update(T t);

    public void delete(T t);
}
