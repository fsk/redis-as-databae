package org.fsk.redisasdatabase.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Order;
import org.fsk.redisasdatabase.services.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestParam String customerId,
            @RequestParam Set<String> productIds) {
        log.debug("Sipariş oluşturma isteği alındı. Müşteri ID: {}, Ürün IDs: {}",
                customerId, productIds);
        try {
            Order savedOrder = orderService.createOrder(customerId, productIds);
            log.info("Sipariş başarıyla oluşturuldu. ID: {}", savedOrder.getId());
            return ResponseEntity.ok(savedOrder);
        } catch (RuntimeException e) {
            log.warn("Sipariş oluşturulamadı: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Sipariş oluşturulurken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable String id) {
        log.debug("Sipariş getirme isteği alındı. ID: {}", id);
        try {
            Order order = orderService.getOrderById(id);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            log.warn("Sipariş bulunamadı. ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Sipariş getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        log.debug("Tüm siparişleri getirme isteği alındı");
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            log.error("Siparişler getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
