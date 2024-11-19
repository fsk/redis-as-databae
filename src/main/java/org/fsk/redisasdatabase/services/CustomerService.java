package org.fsk.redisasdatabase.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fsk.redisasdatabase.domains.Customer;
import org.fsk.redisasdatabase.repositories.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomerService {
    
    private final CustomerRepository customerRepository;
    
    public Customer saveCustomer(Customer customer) {
        log.debug("Müşteri kaydetme işlemi başlatıldı: {}", customer);
        try {
            customer.setId(UUID.randomUUID().toString());
            Customer savedCustomer = customerRepository.save(customer);
            log.info("Müşteri başarıyla kaydedildi. ID: {}", savedCustomer.getId());
            return savedCustomer;
        } catch (Exception e) {
            log.error("Müşteri kaydedilirken hata oluştu. Müşteri: {}, Hata: {}", 
                     customer, e.getMessage(), e);
            throw new RuntimeException("Müşteri kaydedilemedi", e);
        }
    }
    
    public Customer getCustomerById(String id) {
        log.debug("Müşteri arama işlemi başlatıldı. ID: {}", id);
        try {
            return customerRepository.findById(id)
                    .orElseThrow(() -> {
                        log.warn("Müşteri bulunamadı. ID: {}", id);
                        return new RuntimeException("Müşteri bulunamadı");
                    });
        } catch (Exception e) {
            log.error("Müşteri getirilirken hata oluştu. ID: {}, Hata: {}", 
                     id, e.getMessage(), e);
            throw new RuntimeException("Müşteri getirilemedi", e);
        }
    }
    
    public List<Customer> getAllCustomers() {
        log.debug("Tüm müşterileri getirme işlemi başlatıldı");
        try {
            List<Customer> customers = new ArrayList<>();
            customerRepository.findAll().forEach(customers::add);
            log.info("Toplam {} müşteri getirildi", customers.size());
            return customers;
        } catch (Exception e) {
            log.error("Müşteriler getirilirken hata oluştu: {}", e.getMessage(), e);
            throw new RuntimeException("Müşteriler getirilemedi", e);
        }
    }
    
    public void deleteCustomer(String id) {
        log.debug("Müşteri silme işlemi başlatıldı. ID: {}", id);
        try {
            customerRepository.deleteById(id);
            log.info("Müşteri başarıyla silindi. ID: {}", id);
        } catch (Exception e) {
            log.error("Müşteri silinirken hata oluştu. ID: {}, Hata: {}", 
                     id, e.getMessage(), e);
            throw new RuntimeException("Müşteri silinemedi", e);
        }
    }
}
