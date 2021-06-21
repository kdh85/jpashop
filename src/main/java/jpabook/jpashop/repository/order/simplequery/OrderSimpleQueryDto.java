package jpabook.jpashop.repository.order.simplequery;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.OrderStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderSimpleQueryDto {
    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;

    public OrderSimpleQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address){
        this.orderId = orderId;
        this.name = name;//lazy 대상. 영속성 컨텍스트에서 뒤져서 없으면 쿼리 발생.
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;//lazy 대상. 영속성 컨텍스트에서 뒤져서 없으면 쿼리 발생.
    }
}
