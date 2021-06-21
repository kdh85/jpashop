package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderSearch;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimepleQueryRepository;
import jpabook.jpashop.repository.order.simplequery.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/*
    NtoOne 관계
    Order
    Order -> Member
    Order -> Delivery
*/

@RestController
@RequiredArgsConstructor
public class OrderSampleApiController {

    private final OrderRepository orderRepository;
    private final OrderSimepleQueryRepository orderSimepleQueryRepository;

    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1(){
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        //Hibernate5Module 이 필요해진다. 이유는 order entity에 선언된 각종 관계에 대한 DB를 다 조회하는데
        //lazy 설정때문에 proxy로 new entity를 만들면서 에러가 발생하게 되고,
        //불필요한 부분까지 전부 조회가 되는 문제가 발생함.
        //Hibernate5Module을 bean 등록한 부분에서 FORCE_LAZY_LOADING를 사용하지 말아야하며,
        //이를 사용하지 않을 경우 아래와 같은 방식으로 코드를 변경해야 한다.
        for (Order order : all) {
            //order entity안에 선언되있는 NtoOne lazy로딩 관계인 entity를 조회하도록 가져오게 시킨다.
            order.getMember().getName();
            order.getDelivery().getAddress();
        }

        return all;
    }

    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2(){
//        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
//        return orders.stream()
//                .map(o -> new SimpleOrderDto(o))
//                .collect(Collectors.toList());
        return orderRepository.findAllByCriteria(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3(){
        return orderRepository.findAllWithMemberDelivery(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());
    }

    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4(){
        return orderSimepleQueryRepository.findOrderDtos();
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order){
            orderId = order.getId();
            name = order.getMember().getName();//lazy 대상. 영속성 컨텍스트에서 뒤져서 없으면 쿼리 발생.
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getMember().getAddress();//lazy 대상. 영속성 컨텍스트에서 뒤져서 없으면 쿼리 발생.
        }
    }
}
