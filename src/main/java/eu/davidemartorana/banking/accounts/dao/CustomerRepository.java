package eu.davidemartorana.banking.accounts.dao;


import eu.davidemartorana.banking.accounts.domain.internal.Customer;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

public class CustomerRepository extends AbstractDAO<Customer> {

    @Inject
    public CustomerRepository(final SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Customer> findAll() {
        return (List<Customer>) currentSession().createCriteria(Customer.class).list();
    }

    public Optional<Customer> findByUUID(final String uuid) {
        return this.currentSession()
                .createNamedQuery("customer.byUUID", Customer.class)
                .setParameter("uuid", uuid)
                .uniqueResultOptional();
    }
}
