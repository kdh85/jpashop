package jpabook.jpashop.api;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1(){
        List<Order> allByString = orderRepository.findAllByString(new OrderSearch());
        for (Order order : allByString) {
            order.getMember().getName();
            order.getDelivery().getAddress();

            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream()
                    .forEach(o->o.getItem().getName());
        }
        return allByString;
    }

    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2(){
        List<Order> allByString = orderRepository.findAllByString(new OrderSearch());
        return allByString.stream()
                .map(o-> new OrderDto(o))
                .collect(toList());

    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getOrderStatus();
            address = order.getDelivery().getAddress();
            //order.getOrderItems().stream().forEach(o->o.getItem().getName());
            //orderItems = order.getOrderItems();
            //위 라인처럼 orderItem을 entity로 쓰면 또 문제가 생기니 모든 api로 나가는 랩핑 클래스는 Dto로 다 만들어라.
            //단 address클래스 같은 value object는 그대로 사용해도 괜찮다.(변경될 가능성이 없는 기준)
            orderItems = order.getOrderItems()
                    .stream()
                    .map(oi->new OrderItemDto(oi))
                    .collect(toList());
        }
    }

    @Data
    static class OrderItemDto {

        private String itemName;
        private int price;
        private int count;

        public OrderItemDto(OrderItem oi) {
            itemName = oi.getItem().getName();
            price = oi.getItem().getPrice();
            count = oi.getItem().getStockQuantity();
        }
    }

    @GetMapping("/api/v3/orders")
    public List<OrderDto> ordersV3(){
        return orderRepository.findAllWithItem().stream()
                .map(o->new OrderDto(o))
                .collect(toList());
    }

    @GetMapping("/api/v3.1/orders")
    public List<OrderDto> ordersV3_page(@RequestParam(value = "offset", defaultValue = "0")int offset,
                                        @RequestParam(value = "limit",defaultValue = "100")int limit){
        return orderRepository.findAllWithMemberDelivery(offset,limit).stream()
                .map(OrderDto::new)
                .collect(toList());

    }

    @GetMapping("/api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    @GetMapping("/api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    @GetMapping("/api/v6/orders")
    //public List<OrderFlatDto> ordersV6() {
    public List<OrderQueryDto> ordersV6() {
        List<OrderFlatDto> flatDtos = orderQueryRepository.findAllByDto_flat();
        return flatDtos.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(e.getKey().getOrderId(),
                        e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(),
                        e.getKey().getAddress(), e.getValue()))
                .collect(toList());
        //return  orderQueryRepository.findAllByDto_flat();
    }
}
