package org.fsk.redisasdatabase.domains;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@RedisHash("order")
public class Order implements Serializable {

    @Id
    private String id;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    @Reference
    private Customer customer;
    @Reference
    private Set<Product> products;

}
