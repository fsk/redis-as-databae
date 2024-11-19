package org.fsk.redisasdatabase.repositories;

import org.fsk.redisasdatabase.domains.Customer;
import org.springframework.data.repository.CrudRepository;

public interface CustomerRepository extends CrudRepository<Customer, String> {

}
