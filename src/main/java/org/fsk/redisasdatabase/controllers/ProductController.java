package org.fsk.redisasdatabase.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Product;
import org.fsk.redisasdatabase.services.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        log.debug("Ürün oluşturma isteği alındı: {}", product);
        try {
            Product savedProduct = productService.saveProduct(product);
            log.info("Ürün başarıyla oluşturuldu. ID: {}", savedProduct.getId());
            return ResponseEntity.ok(savedProduct);
        } catch (Exception e) {
            log.error("Ürün oluşturulurken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        log.debug("Ürün getirme isteği alındı. ID: {}", id);
        try {
            Product product = productService.getProductById(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            log.warn("Ürün bulunamadı. ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ürün getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        log.debug("Tüm ürünleri getirme isteği alındı");
        try {
            List<Product> products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            log.error("Ürünler getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<Product> updateStock(
            @PathVariable String id,
            @RequestParam Integer newStock) {
        log.debug("Ürün stok güncelleme isteği alındı. ID: {}, Yeni Stok: {}", id, newStock);
        try {
            Product updatedProduct = productService.updateStock(id, newStock);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException e) {
            log.warn("Ürün bulunamadı. ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ürün stoku güncellenirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
