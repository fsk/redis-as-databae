package org.fsk.redisasdatabase.repositories;

import org.fsk.redisasdatabase.domains.Order;
import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, String> {

}
