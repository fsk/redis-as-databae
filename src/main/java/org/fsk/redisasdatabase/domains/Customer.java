package org.fsk.redisasdatabase.domains;

import java.io.Serializable;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Reference;
import org.springframework.data.redis.core.RedisHash;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@RedisHash("customer")
public class Customer implements Serializable {

    @Id
    private String id;
    private String name;
    private String email;
    private String phone;
    @Reference
    private Set<Order> orders;

}
