package webprak.DAO.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import webprak.DAO.CommonDAO;
import webprak.models.CommonEntity;
import java.util.Collection;

@Repository
public abstract class CommonDAOImpl<T extends CommonEntity<ID>, ID> implements CommonDAO<T, ID> {

    @PersistenceContext
    protected EntityManager entityManager;

    protected final Class<T> entityClass;

    public CommonDAOImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T getById(ID id) {
        return entityManager.find(entityClass, id);
    }

    @Override
    public Collection<T> getAll() {
        TypedQuery<T> query = entityManager.createQuery("FROM " + entityClass.getName(), entityClass);
        return query.getResultList();
    }

    @Override
    @Transactional
    public void save(T entity) {
        entityManager.persist(entity);
    }

    @Override
    @Transactional
    public void saveCollection(Collection<T> entities) {
        for (T entity : entities) {
            entityManager.persist(entity);
        }
    }

    @Override
    @Transactional
    public void update(T entity) {
        entityManager.merge(entity);
    }

    @Override
    @Transactional
    public void delete(T entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
    }

    @Override
    @Transactional
    public void deleteById(ID id) {
        T entity = getById(id);
        if (entity != null) delete(entity);
    }
}