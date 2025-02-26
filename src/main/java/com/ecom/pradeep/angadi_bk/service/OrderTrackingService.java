package com.ecom.pradeep.angadi_bk.service;
import com.ecom.pradeep.angadi_bk.model.Order;
import com.ecom.pradeep.angadi_bk.repo.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class OrderTrackingService {
    private final OrderRepository orderRepository;

    public OrderTrackingService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order updateOrderStatus(Long orderId, String status) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            order.updateStatus(status);
            return orderRepository.save(order);
        } else {
            throw new RuntimeException("Order not found!");
        }
    }

    public Order getOrderStatus(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found!"));
    }
}
