package eu.davidemartorana.banking.accounts.dao;

import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import eu.davidemartorana.banking.accounts.domain.internal.TransactionType;
import eu.davidemartorana.banking.accounts.domain.Amount;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class TransactionRepository  extends AbstractDAO<Transaction> {

    @Inject
    public TransactionRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Transaction> findByAccountId(Long id) {
        return this.currentSession()
                .createNamedQuery("transactions.findByAccountId", Transaction.class)
                .setParameter("accountId", id)
                .list();
    }

    public Transaction addTransaction(final Amount amount, final Account account, final TransactionType transactionType) {
        final Transaction transaction = new Transaction();
        transaction.setAccountId(account.getId());
        transaction.setAmount(amount.getValue());
        transaction.setCurrency(amount.getCurrency());
        transaction.setType(transactionType.value());
        transaction.setDateTime(DateTime.now());
        transaction.setUuid(UUID.randomUUID().toString());

        final Long id = (Long) this.currentSession().save(transaction);

        return this.currentSession().find(Transaction.class, id);
    }
}
