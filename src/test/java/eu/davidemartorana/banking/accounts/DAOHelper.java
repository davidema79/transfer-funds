package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.domain.internal.Account;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

public class DAOHelper<T> extends AbstractDAO<T> {

    public DAOHelper(SessionFactory sessionFactory){
        super(sessionFactory);
    }

    public Session getSession() {
        // this.getSession().getEntityManagerFactory().createE
        return this.currentSession();
    }
}
