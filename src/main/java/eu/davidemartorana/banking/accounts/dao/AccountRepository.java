package eu.davidemartorana.banking.accounts.dao;

import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.exceptions.FundsNotEnoughException;
import eu.davidemartorana.banking.accounts.domain.Amount;
import io.dropwizard.hibernate.AbstractDAO;
import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.LockModeType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AccountRepository extends AbstractDAO<Account> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRepository.class);


    @Inject
    public AccountRepository(SessionFactory sessionFactory) {
        super(sessionFactory);
        //sessionFactory.getCurrentSession()
    }

    public List<Account> findByCustomerId(final Integer customerId){
        return  this.currentSession()
                .createNamedQuery("account.byCustomerId", Account.class)
                .setParameter("customerId", customerId)
                .list();
    }

    public Optional<Account> findByUUID(String uuid) {
        return this.currentSession()
                .createNamedQuery("account.byUUID", Account.class)
                .setParameter("uuid", uuid)
                .uniqueResultOptional();
    }

    /**
     * Checks the total amount available of the account with id {@code accountId} if it is greater or equals the given {@code amount}.
     * If this is the case, the total amount is decreased by the the value of {@code amount}, and the new value is persisted.
     *
     * Prior the operation is made a {@link LockModeType#PESSIMISTIC_WRITE} is acquired on the account to guarantee coherence and managing potentials race conditions.
     *
     * @param amount - value to decrease
     * @param accountId - account upon make the changes
     *
     * @return an instance of account containing the new values.
     */
    public Account checkAndDecreaseLockingResourceByAccountId(final Amount amount, final Long accountId) {

        LOGGER.debug("Check the account with id {} and acquires the lock.", accountId);
        final Account account = this.currentSession().find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);
        if(account.getTotalAmount().compareTo(amount.getValue()) < 0 ){
            throw new FundsNotEnoughException("Fund not enough for amount: " + amount.getValue().toPlainString());
        }

        final BigDecimal newTotal = account.getTotalAmount().subtract(amount.getValue());
        account.setTotalAmount(newTotal);

        LOGGER.debug("");
        this.currentSession().saveOrUpdate(account);

        return this.currentSession().find(Account.class, accountId);
    }

    /**
     * Increases the total amount of the account with id {@code accountId} by the the value of {@code amount}, and the new value is persisted.
     *
     * Prior the operation is made a {@link LockModeType#PESSIMISTIC_WRITE} is acquired on the account to guarantee coherence and managing potentials race conditions.
     *
     * @param amount - value to decrease
     * @param accountId - account upon make the changes
     *
     * @return an instance of account containing the new values.
     */
    public Account checkAndIncreaseLockingResourceByAccountId(final Amount amount, final Long accountId) {
        final Account account = this.currentSession().find(Account.class, accountId, LockModeType.PESSIMISTIC_WRITE);

        final BigDecimal newTotal = account.getTotalAmount().add(amount.getValue());
        account.setTotalAmount(newTotal);

        this.currentSession().saveOrUpdate(account);

        return this.currentSession().find(Account.class, accountId);
    }
}
