package com.project.order_service.service;

import com.project.order_service.dto.OrderLineItemsDto;
import com.project.order_service.dto.OrderRequest;
import com.project.order_service.model.Order;
import com.project.order_service.model.OrderLineItem;
import com.project.order_service.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemList(orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::mapToOrderLineItems).toList());
        orderRepository.save(order);
    }

    private OrderLineItem mapToOrderLineItems(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItem.builder()
                .price(orderLineItemsDto.getPrice())
                .skuCode(orderLineItemsDto.getSkuCode())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }
}
