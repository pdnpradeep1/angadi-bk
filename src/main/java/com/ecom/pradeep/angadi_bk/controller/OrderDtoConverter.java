package com.ecom.pradeep.angadi_bk.controller;

import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.model.OrderDTO;
import com.ecom.pradeep.angadi_bk.model.OrderItem;
import com.ecom.pradeep.angadi_bk.model.OrderItemDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.stream.Collectors;

public class OrderDtoConverter {

    public OrderDTO convertToDTO(Order order) {
        if (order == null) {
            return null;
        }

        OrderDTO dto = new OrderDTO();

        // Basic order fields
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setStatus(order.getStatus());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());
        dto.setNotes(order.getNotes());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPrepaid(order.isPrepaid());

        // Only set these if they exist in your Order class
        // If these methods don't exist in your Order class, comment them out
        // dto.setQuantity(order.getQuantity());
        // dto.setReminderCount(order.getReminderCount());

        // Financial details
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setTax(order.getTax());
        dto.setDiscount(order.getDiscount());
        dto.setTotalAmount(order.getTotalAmount());

        // Customer details (if customer exists)
        if (order.getCustomer() != null) {
            dto.setCustomerId(order.getCustomer().getId());
            dto.setCustomerName(order.getCustomer().getName());
            dto.setCustomerEmail(order.getCustomer().getEmail());
        }

        // Store details (if store exists)
        if (order.getStore() != null) {
            dto.setStoreId(order.getStore().getId());
            dto.setStoreName(order.getStore().getName());
        }

        // Shipping address details (if address exists)
        if (order.getShippingAddress() != null) {
            dto.setShippingAddressLine1(order.getShippingAddress().getAddressLine1());
            dto.setShippingAddressLine2(order.getShippingAddress().getAddressLine2());
            dto.setShippingCity(order.getShippingAddress().getCity());
            dto.setShippingState(order.getShippingAddress().getState());
            dto.setShippingPostalCode(order.getShippingAddress().getPostalCode());
            dto.setShippingCountry(order.getShippingAddress().getCountry());
        }

        // Order items
        if (order.getOrderItems() != null) {
            List<OrderItemDTO> itemDtos = order.getOrderItems().stream()
                    .map(this::convertToOrderItemDTO)
                    .collect(Collectors.toList());
            dto.setOrderItems(itemDtos);
        }

        return dto;
    }

    public OrderItemDTO convertToOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        OrderItemDTO dto = new OrderItemDTO();

        dto.setId(orderItem.getId());
        dto.setQuantity(orderItem.getQuantity());
        dto.setPrice(orderItem.getPrice());
        dto.setTotal(orderItem.getTotal());

        // Product details (if product exists)
        if (orderItem.getProduct() != null) {
            dto.setProductId(orderItem.getProduct().getId());
            dto.setProductName(orderItem.getProduct().getName());
            dto.setProductImageUrl(orderItem.getProduct().getImageUrl());
        }

        return dto;
    }

    public Page<OrderDTO> convertToOrderDTOPage(Page<Order> orderPage, Pageable pageable) {
        List<OrderDTO> dtos = orderPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, orderPage.getTotalElements());
    }
}