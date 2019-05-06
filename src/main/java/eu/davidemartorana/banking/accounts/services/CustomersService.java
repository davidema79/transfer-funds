package eu.davidemartorana.banking.accounts.services;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.dao.CustomerRepository;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import java.util.List;

public class CustomersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomersService.class);

    @Inject
    private CustomerRepository customerRepository;

    @Inject
    private AccountRepository accountRepository;

    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }

    public Customer getByCustomerUUID(final String uuid){
        LOGGER.trace("Retrieving customer by id {}", uuid);

        final Customer customer = customerRepository.findByUUID(uuid).orElseThrow(() -> new NotFoundException("Customer with given id was not found"));

        return customer;
    }

    public List<Account> getAllAccountsByCustomerUUID(final String uuid){
        LOGGER.trace("Retrieving all the accounts of customer with id {}", uuid);

        final Customer customer = customerRepository.findByUUID(uuid).orElseThrow(() -> new NotFoundException("Customer with given id was not found"));

        return  accountRepository.findByCustomerId(customer.getId());
    }

}
