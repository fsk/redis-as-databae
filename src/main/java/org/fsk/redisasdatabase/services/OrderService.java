package org.fsk.redisasdatabase.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Customer;
import org.fsk.redisasdatabase.domains.Order;
import org.fsk.redisasdatabase.domains.Product;
import org.fsk.redisasdatabase.repositories.CustomerRepository;
import org.fsk.redisasdatabase.repositories.OrderRepository;
import org.fsk.redisasdatabase.repositories.ProductRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public Order createOrder(String customerId, Set<String> productIds) {
        log.debug("Sipariş oluşturma işlemi başlatıldı. Müşteri ID: {}, Ürün IDs: {}",
                customerId, productIds);

        return redisTemplate.execute(new SessionCallback<Order>() {
            @Override
            public Order execute(RedisOperations operations) throws DataAccessException {
                try {
                    // Transaction başlat
                    operations.multi();

                    // Müşteriyi kontrol et
                    Customer customer = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Müşteri bulunamadı"));

                    Set<Product> products = new HashSet<>();
                    BigDecimal totalAmount = BigDecimal.ZERO;

                    // Ürünleri kontrol et ve stok düş
                    for (String productId : productIds) {
                        Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new RuntimeException("Ürün bulunamadı: " + productId));

                        // Stok kontrolü
                        if (product.getStock() <= 0) {
                            operations.discard(); // Transaction'ı iptal et
                            throw new RuntimeException("Ürün stokta yok: " + productId);
                        }

                        // Stok güncelle
                        product.setStock(product.getStock() - 1);
                        operations.opsForHash().putAll(
                            "product:" + product.getId(),
                            objectToHash(product)
                        );

                        products.add(product);
                        totalAmount = totalAmount.add(product.getPrice());
                    }

                    // Sipariş oluştur
                    Order order = new Order();
                    order.setId(UUID.randomUUID().toString());
                    order.setOrderDate(LocalDateTime.now());
                    order.setTotalAmount(totalAmount);
                    order.setCustomer(customer);
                    order.setProducts(products);

                    // Siparişi kaydet
                    operations.opsForHash().putAll(
                        "order:" + order.getId(),
                        objectToHash(order)
                    );

                    // Müşterinin siparişlerini güncelle
                    Set<Order> customerOrders = customer.getOrders();
                    if (customerOrders == null) {
                        customerOrders = new HashSet<>();
                    }
                    customerOrders.add(order);
                    customer.setOrders(customerOrders);

                    operations.opsForHash().putAll(
                        "customer:" + customer.getId(),
                        objectToHash(customer)
                    );

                    // Transaction'ı commit et
                    operations.exec();

                    log.info("Sipariş başarıyla oluşturuldu. ID: {}", order.getId());
                    return order;

                } catch (Exception e) {
                    // Hata durumunda transaction'ı geri al
                    operations.discard();
                    log.error("Sipariş oluşturulurken hata: {}", e.getMessage());
                    throw new RuntimeException("Sipariş oluşturulamadı", e);
                }
            }
        });
    }

    // Nesneyi Hash'e çeviren yardımcı metod
    private Map<String, String> objectToHash(Object obj) {
        Map<String, String> hash = new HashMap<>();
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(obj.getClass());
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                if (pd.getReadMethod() != null && !"class".equals(pd.getName())) {
                    Object value = pd.getReadMethod().invoke(obj);
                    if (value != null) {
                        hash.put(pd.getName(), value.toString());
                    }
                }
            }
            hash.put("_class", obj.getClass().getName());
        } catch (Exception e) {
            log.error("Nesne Hash'e çevrilirken hata: {}", e.getMessage());
        }
        return hash;
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

    public void demonstrateTransactionIssues() {
        String productKey = "product:test";
        
        // İlk durumu ayarla
        redisTemplate.opsForValue().set(productKey, 100); // Stok: 100
        
        // Parallel işlemleri simüle et
        ExecutorService executor = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(10);
        
        for (int i = 0; i < 10; i++) {
            executor.submit(() -> {
                try {
                    redisTemplate.execute(new SessionCallback<Object>() {
                        @Override
                        public Object execute(RedisOperations operations) {
                            try {
                                // WATCH komutu ile key'i izle
                                operations.watch(productKey);
                                
                                // Mevcut stoku oku
                                Integer currentStock = (Integer) operations.opsForValue().get(productKey);
                                
                                // İşlemi simüle et (100ms bekle)
                                Thread.sleep(100);
                                
                                // Transaction başlat
                                operations.multi();
                                
                                // Stok güncelle
                                operations.opsForValue().set(productKey, currentStock - 1);
                                
                                // Transaction'ı commit et
                                List<Object> result = operations.exec();
                                
                                if (result == null) {
                                    log.error("Transaction başarısız - Optimistic Lock hatası");
                                } else {
                                    log.info("Stok güncellendi: {}", currentStock - 1);
                                }
                                
                                return result;
                                
                            } catch (Exception e) {
                                operations.discard();
                                log.error("İşlem hatası: {}", e.getMessage());
                                return null;
                            }
                        }
                    });
                } finally {
                    latch.countDown();
                }
            });
        }
        
        try {
            latch.await();
            Integer finalStock = (Integer) redisTemplate.opsForValue().get(productKey);
            log.info("Beklenen son stok: 90");
            log.info("Gerçek son stok: {}", finalStock);
            log.info("Tutarsızlık: {}", Math.abs(90 - finalStock));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
    }
}