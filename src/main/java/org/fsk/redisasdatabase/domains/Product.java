package org.fsk.redisasdatabase.domains;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@RedisHash("product")
public class Product implements Serializable {

    @Id
    private String id;
    private String name;
    private BigDecimal price;
    private Integer stock;
    @Reference
    private Set<Order> orders;

}
