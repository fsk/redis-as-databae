package org.fsk.redisasdatabase.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Product;
import org.fsk.redisasdatabase.repositories.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product saveProduct(Product product) {
        log.debug("Ürün kaydetme işlemi başlatıldı: {}", product);
        try {
            product.setId(UUID.randomUUID().toString());
            Product savedProduct = productRepository.save(product);
            log.info("Ürün başarıyla kaydedildi. ID: {}", savedProduct.getId());
            return savedProduct;
        } catch (Exception e) {
            log.error("Ürün kaydedilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Ürün kaydedilemedi", e);
        }
    }

    public Product getProductById(String id) {
        log.debug("Ürün arama işlemi başlatıldı. ID: {}", id);
        try {
            return productRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Ürün bulunamadı. ID: {}", id);
                        return new RuntimeException("Ürün bulunamadı");
                    });
        } catch (Exception e) {
            log.error("Ürün getirilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Ürün getirilemedi", e);
        }
    }

    public Product updateStock(String productId, Integer newStock) {
        log.debug("Ürün stok güncelleme işlemi başlatıldı. ID: {}, Yeni Stok: {}", productId, newStock);
        try {
            Product product = getProductById(productId);
            product.setStock(newStock);
            Product updatedProduct = productRepository.save(product);
            log.info("Ürün stoğu güncellendi. ID: {}, Yeni Stok: {}", productId, newStock);
            return updatedProduct;
        } catch (Exception e) {
            log.error("Ürün stoğu güncellenirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Ürün stoğu güncellenemedi", e);
        }
    }

    public List<Product> getAllProducts() {
        log.debug("Tüm ürünleri getirme işlemi başlatıldı");
        try {
            List<Product> products = new ArrayList<>();
            productRepository.findAll().forEach(products::add);
            log.info("Toplam {} ürün getirildi", products.size());
            return products;
        } catch (Exception e) {
            log.error("Ürünler getirilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Ürünler getirilemedi", e);
        }
    }
}
