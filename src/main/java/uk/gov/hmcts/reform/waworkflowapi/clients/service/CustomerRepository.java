package uk.gov.hmcts.reform.waworkflowapi.clients.service;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.waworkflowapi.clients.model.Customer;

import java.util.List;

public interface CustomerRepository extends CrudRepository<Customer, Long> {

    List<Customer> findByLastName(String lastName);

    Customer findById(long id);

}
