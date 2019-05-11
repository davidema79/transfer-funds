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

/**
 * Service class for Customers related operations.
 *
 *
 * @author Davide Martorana
 */
public class CustomersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomersService.class);

    private final CustomerRepository customerRepository;

    private final AccountRepository accountRepository;

    @Inject
    public CustomersService(final CustomerRepository customerRepository, final AccountRepository accountRepository) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
    }

    /**
     * Retrieves and returns the list of all the customers
     *
     * @return a {@link List} of {@link Customer}
     */
    public List<Customer> getAllCustomers(){
        return customerRepository.findAll();
    }


    /**
     * Retrieve the customer with the given unique {@code uuid}
     *
     * @param uuid - unique identifier for the customer
     *
     * @return the instance of {@link Customer} with the given {@code uuid}.
     *
     * @throws NotFoundException if no customer was found.
     */
    public Customer getByCustomerUUID(final String uuid){
        LOGGER.trace("Retrieving customer by id [{}]", uuid);

        final Customer customer = customerRepository.findByUUID(uuid).orElseThrow(() -> new NotFoundException("Customer with given id was not found"));

        return customer;
    }

    /**
     * Retrieves the list of Accounts given a Customer UUID
     *
     * @param uuid - unique identifier for the customer
     *
     * @return and instance of {@link List<Account>}, empty if no accounts are linked to the customer.
     */
    public List<Account> getAllAccountsByCustomerUUID(final String uuid){
        LOGGER.trace("Retrieving all the accounts of customer with uuid [{}]", uuid);

        final Customer customer = getByCustomerUUID(uuid);

        final Integer id = customer.getId();
        LOGGER.debug("Retrieving all the accounts of customer with id [{}] obtained from uuid [{}]", id, uuid);

        return  accountRepository.findByCustomerId(id);
    }

}
