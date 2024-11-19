package org.fsk.redisasdatabase.repositories;

import org.fsk.redisasdatabase.domains.Product;
import org.springframework.data.repository.CrudRepository;

public interface ProductRepository extends CrudRepository<Product, String> {

}
