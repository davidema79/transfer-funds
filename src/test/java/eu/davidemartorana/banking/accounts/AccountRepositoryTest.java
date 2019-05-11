package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.domain.Amount;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.exceptions.FundsNotEnoughException;
import io.dropwizard.testing.junit.DAOTestRule;
import org.assertj.core.api.Assertions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.PessimisticLockException;
import javax.security.auth.login.AccountLockedException;
import javax.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 *
 */
public class AccountRepositoryTest {

    @Rule
    public DAOTestRule database = DAOTestRule.newBuilder()
            .addEntityClass(Account.class)
            .setProperty("hibernate.current_session_context_class", "thread")
            .build();

    private AccountRepository accountRepository;

    private DAOHelper<Account> accountDAOHelper;

    private Account createAccount(final Long id, final String uuid, final Currency currency, final String amount) {
        final Account account = new Account();
        account.setId(id);
        account.setUuid(uuid);
        account.setCurrency(currency);
        account.setTotalAmount(new BigDecimal(amount));
        account.setIbanNumber("GB40REVO60161331926819");
        account.setCustomerId(12345);

        return account;
    }


    @Before
    public void setUp() {
        accountRepository = new AccountRepository(database.getSessionFactory());

        accountDAOHelper = new DAOHelper<>(database.getSessionFactory());
    }


    @Test
    public void testCheckAndDecreaseLockingResourceByAccountId() throws Exception {
        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;
        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), "150.00");

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final Account account = database.inTransaction( () -> accountRepository.checkAndDecreaseLockingResourceByAccountId(amount, accountId));

        Assertions.assertThat(account).isNotNull();
        Assertions.assertThat(account.getTotalAmount()).isEqualByComparingTo("50.00");

    }


    @Test
    public void testCheckAndDecreaseLockingResourceByAccountId_NotEnoughFundsException() throws Exception {
        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;
        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), "50.00");

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        Assertions.assertThatExceptionOfType(FundsNotEnoughException.class)
                .isThrownBy(
                        () -> database.inTransaction( () -> accountRepository.checkAndDecreaseLockingResourceByAccountId(amount, accountId))
                ).withMessage("Fund not enough for amount: 100.00");


    }



    @Test
    public void testCheckAndIncreaseLockingResourceByAccountId() throws Exception {
        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;
        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), "150.00");

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final Account account = database.inTransaction( () -> accountRepository.checkAndIncreaseLockingResourceByAccountId(amount, accountId));

        Assertions.assertThat(account).isNotNull();
        Assertions.assertThat(account.getTotalAmount()).isEqualByComparingTo("250.00");

    }

    @Test
    public void testCheckAndIncreaseLockingResourceByAccountId_Locking_Test() throws Exception {

        final int threadPoolSize = 3;
        final int threadsNumber = threadPoolSize -1;
        final CountDownLatch countDownLatch = new CountDownLatch(threadsNumber);

        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;
        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), "150.00");

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        final List<Future<Account>> futureList = new ArrayList<>(threadPoolSize);
        for(int index = 0; index < threadsNumber ; index++) {
            final Future<Account> future = executor.submit(() -> {
                    return database.inTransaction(() -> {
                        final Account account;
                        try {
                            account= accountRepository.checkAndIncreaseLockingResourceByAccountId(amount, accountId);
                        } finally {
                            countDownLatch.countDown();
                            System.out.printf("countDownLatch: %s\n", countDownLatch.getCount());
                            countDownLatch.await();
                        }
                        return account;
                    });
            });
            futureList.add(future);
        }

        Assertions.assertThat(futureList).isNotEmpty().hasSize(threadsNumber);



        int counter =0;
        try {
            final Account first = futureList.get(0).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(first).isNotNull();
            Assertions.assertThat(first.getTotalAmount()).isEqualByComparingTo("250.00");
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
            counter++;
        }


         try {
            final Account second = futureList.get(1).get(100, TimeUnit.SECONDS);
             Assertions.assertThat(second).isNotNull();
             Assertions.assertThat(second.getTotalAmount()).isEqualByComparingTo("250.00");
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
             counter++;
        }

         // Asserting that only one transaction has failed.
         Assertions.assertThat(counter).isEqualTo(1);
    }

    @Test
    public void testCheckAndDecreaseLockingResourceByAccountId_Locking_Test() throws Exception {

        final int threadPoolSize = 3;
        final int threadsNumber = threadPoolSize -1;
        final CountDownLatch countDownLatch = new CountDownLatch(threadsNumber);

        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;
        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), "150.00");

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        final List<Future<Account>> futureList = new ArrayList<>(threadPoolSize);
        for(int index = 0; index < threadsNumber ; index++) {
            final Future<Account> future = executor.submit(() -> {
                return database.inTransaction(() -> {
                    final Account account;
                    try {
                        account= accountRepository.checkAndDecreaseLockingResourceByAccountId(amount, accountId);
                    } finally {
                        countDownLatch.countDown();
                        System.out.printf("countDownLatch: %s\n", countDownLatch.getCount());
                        countDownLatch.await();
                    }
                    return account;
                });
            });
            futureList.add(future);
        }

        Assertions.assertThat(futureList).isNotEmpty().hasSize(threadsNumber);



        int counter =0;
        try {
            final Account first = futureList.get(0).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(first).isNotNull();
            Assertions.assertThat(first.getTotalAmount()).isEqualByComparingTo("50.00");
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
            counter++;
        }


        try {
            final Account second = futureList.get(1).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(second).isNotNull();
            Assertions.assertThat(second.getTotalAmount()).isEqualByComparingTo("50.00");
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
            counter++;
        }

        // Asserting that only one transaction has failed.
        Assertions.assertThat(counter).isEqualTo(1);
    }

}
