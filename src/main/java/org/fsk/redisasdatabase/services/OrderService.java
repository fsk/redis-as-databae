package org.fsk.redisasdatabase.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Customer;
import org.fsk.redisasdatabase.domains.Order;
import org.fsk.redisasdatabase.domains.Product;
import org.fsk.redisasdatabase.repositories.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerService customerService;
    private final ProductService productService;

    public Order createOrder(String customerId, Set<String> productIds) {
        log.debug("Sipariş oluşturma işlemi başlatıldı. Müşteri ID: {}, Ürün IDs: {}",
                customerId, productIds);
        try {
            Customer customer = customerService.getCustomerById(customerId);

            Set<Product> products = new HashSet<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (String productId : productIds) {
                Product product = productService.getProductById(productId);

                if (product.getStock() <= 0) {
                    log.warn("Ürün stokta yok. Ürün ID: {}", productId);
                    throw new RuntimeException("Ürün stokta yok: " + productId);
                }

                productService.updateStock(productId, product.getStock() - 1);
                products.add(product);
                totalAmount = totalAmount.add(product.getPrice());
            }

            Order order = new Order();
            order.setId(UUID.randomUUID().toString());
            order.setOrderDate(LocalDateTime.now());
            order.setTotalAmount(totalAmount);
            order.setCustomer(customer);
            order.setProducts(products);

            Order savedOrder = orderRepository.save(order);

            // Müşterinin siparişlerini güncelle
            Set<Order> customerOrders = customer.getOrders();
            if (customerOrders == null) {
                customerOrders = new HashSet<>();
            }
            customerOrders.add(savedOrder);
            customer.setOrders(customerOrders);
            customerService.saveCustomer(customer);

            log.info("Sipariş başarıyla oluşturuldu. ID: {}", savedOrder.getId());
            return savedOrder;

        } catch (Exception e) {
            log.error("Sipariş oluşturulurken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Sipariş oluşturulamadı", e);
        }
    }

    public Order getOrderById(String id) {
        log.debug("Sipariş arama işlemi başlatıldı. ID: {}", id);
        try {
            return orderRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Sipariş bulunamadı. ID: {}", id);
                        return new RuntimeException("Sipariş bulunamadı");
                    });
        } catch (Exception e) {
            log.error("Sipariş getirilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Sipariş getirilemedi", e);
        }
    }

    public List<Order> getAllOrders() {
        log.debug("Tüm siparişleri getirme işlemi başlatıldı");
        try {
            List<Order> orders = new ArrayList<>();
            orderRepository.findAll().forEach(orders::add);
            log.info("Toplam {} sipariş getirildi", orders.size());
            return orders;
        } catch (Exception e) {
            log.error("Siparişler getirilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Siparişler getirilemedi", e);
        }
    }
}