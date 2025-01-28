package com.project.order_service.service;

import com.project.order_service.dto.InventoryResponse;
import com.project.order_service.dto.OrderLineItemsDto;
import com.project.order_service.dto.OrderRequest;
import com.project.order_service.model.Order;
import com.project.order_service.model.OrderLineItem;
import com.project.order_service.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
@AllArgsConstructor
public class OrderService {

    private OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        order.setOrderLineItemList(orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::mapToOrderLineItems).toList());

        List<String> skuCodes = orderRequest.getOrderLineItemsDtoList().stream()
                .map(OrderLineItemsDto::getSkuCode).toList();

        InventoryResponse[] inventoryResponse = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        assert inventoryResponse != null;

        boolean isAllInStock = Arrays.stream(inventoryResponse).allMatch(InventoryResponse::isInStock);

        if(isAllInStock){
            orderRepository.save(order);
        }
        else{
            throw new IllegalArgumentException("Product is not in stock, please try again later");
        }

    }

    private OrderLineItem mapToOrderLineItems(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItem.builder()
                .price(orderLineItemsDto.getPrice())
                .skuCode(orderLineItemsDto.getSkuCode())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }
}
