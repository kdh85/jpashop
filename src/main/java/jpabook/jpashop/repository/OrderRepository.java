package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {

    private final EntityManager entityManager;

    public void save(Order order){
        entityManager.persist(order);
    }

    public Order findOne(Long id){
        return entityManager.find(Order.class,id);
    }

    public List<Order> findAllByString(OrderSearch orderSearch){

        String jpql = "select o from Order o join o.member m";
        boolean isFirstcondition = true;

        if(orderSearch.getOrderStatus() != null){
            if(isFirstcondition){
                jpql += "where";
                isFirstcondition = false;
            }else{
                jpql += " and";
            }
            jpql += "o.orderStatus = : orderStatus";
        }

        if(StringUtils.hasText(orderSearch.getMemberName())){
            if(isFirstcondition){
                jpql += "where";
                isFirstcondition = false;
            }else{
                jpql += " and";
            }
            jpql += "m.name like : name";
        }


        TypedQuery<Order> query = entityManager.createQuery(jpql, Order.class)
                .setMaxResults(1000);

        if(orderSearch.getOrderStatus() != null){
            query = query.setParameter("orderStatus",orderSearch.getOrderStatus());
        }

        if(StringUtils.hasText(orderSearch.getMemberName())){
            query = query.setParameter("name", orderSearch.getMemberName());
        }
        return query.getResultList();
    }

    public List<Order> findAllByCriteria(OrderSearch orderSearch){
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Order> query = criteriaBuilder.createQuery(Order.class);
        Root<Order> orderRoot = query.from(Order.class);
        Join<Object, Object> objectJoin = orderRoot.join("member", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        if(orderSearch.getOrderStatus() != null){
            Predicate status = criteriaBuilder.equal(orderRoot.get("orderStatus"),orderSearch.getOrderStatus());
            predicates.add(status);
        }

        if(StringUtils.hasText(orderSearch.getMemberName())){
            Predicate name = criteriaBuilder.like(objectJoin.<String>get("name"),"%"+orderSearch.getMemberName()+"%");
            predicates.add(name);
        }

        query.where(criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()])));
        TypedQuery<Order> orderTypedQuery = entityManager.createQuery(query).setMaxResults(1000);
        return orderTypedQuery.getResultList();
    }

    public List<Order> findAllWithMemberDelivery(OrderSearch orderSearch) {
        return entityManager.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        ).getResultList();
    }

    public List<Order> findAllWithItem() {
        return entityManager.createQuery(
                "select distinct o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItems oi" +
                        " join fetch  oi.item i", Order.class)
                .getResultList();
    }

    public List<Order> findAllWithMemberDelivery(int offset, int limit) {
        return entityManager.createQuery(
                "select o from Order o" +
                        " join fetch o.member m" +
                        " join fetch o.delivery d", Order.class
        )
                .setFirstResult(offset)
                .setMaxResults(limit)
                .getResultList();
    }
}
