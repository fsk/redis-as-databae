package org.fsk.redisasdatabase.controllers;

import org.fsk.redisasdatabase.domains.Customer;
import org.fsk.redisasdatabase.services.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        log.debug("Müşteri oluşturma isteği alındı: {}", customer);
        try {
            Customer savedCustomer = customerService.saveCustomer(customer);
            log.info("Müşteri başarıyla oluşturuldu. ID: {}", savedCustomer.getId());
            return ResponseEntity.ok(savedCustomer);
        } catch (Exception e) {
            log.error("Müşteri oluşturulurken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable String id) {
        log.debug("Müşteri getirme isteği alındı. ID: {}", id);
        try {
            Customer customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(customer);
        } catch (RuntimeException e) {
            log.warn("Müşteri bulunamadı. ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Müşteri getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Customer>> getAllCustomers() {
        log.debug("Tüm müşterileri getirme isteği alındı");
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return ResponseEntity.ok(customers);
        } catch (Exception e) {
            log.error("Müşteriler getirilirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String id) {
        log.debug("Müşteri silme isteği alındı. ID: {}", id);
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Müşteri silinirken hata: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
