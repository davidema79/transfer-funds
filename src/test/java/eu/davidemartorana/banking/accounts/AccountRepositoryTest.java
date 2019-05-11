package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.domain.Amount;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.exceptions.FundsNotEnoughException;
import io.dropwizard.testing.junit.DAOTestRule;
import org.assertj.core.api.Assertions;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.persistence.PessimisticLockException;
import javax.security.auth.login.AccountLockedException;
import javax.ws.rs.NotFoundException;
import java.math.BigDecimal;
import java.util.*;
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



    private void commonLockingTests(final Amount amount, final Long accountId, final String initialAmount, final String expectedFinalAmount, final int threadsNumber ,final Callable<Account> callable) throws Exception {
        final int threadPoolSize = threadsNumber+1;

        final Account accountToSave = createAccount(accountId, "any-uuid", Currency.getInstance("GBP"), initialAmount);

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(accountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        final List<Future<Account>> futureList = new ArrayList<>(threadPoolSize);
        for(int index = 0; index < threadsNumber ; index++) {
            final Future<Account> future = executor.submit(() -> {
                return database.inTransaction(callable);
            });
            futureList.add(future);
        }

        Assertions.assertThat(futureList).isNotEmpty().hasSize(threadsNumber);

        int counter =0;
        try {
            final Account first = futureList.get(0).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(first).isNotNull();
            Assertions.assertThat(first.getTotalAmount()).isEqualByComparingTo(expectedFinalAmount);
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
            counter++;
        }


        try {
            final Account second = futureList.get(1).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(second).isNotNull();
            Assertions.assertThat(second.getTotalAmount()).isEqualByComparingTo(expectedFinalAmount);
        } catch (final Exception e) {
            Assertions.assertThat(e).hasCauseExactlyInstanceOf(PessimisticLockException.class);
            counter++;
        }

        // Asserting that only one transaction has failed.
        Assertions.assertThat(counter).isEqualTo(1);
    }

    @Test
    public void testCheckAndIncreaseLockingResourceByAccountId_Locking_Test() throws Exception {

        final int threadsNumber = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(threadsNumber);

        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;

        commonLockingTests(amount, accountId, "150.00", "250.00", threadsNumber,() -> {
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
    }

    @Test
    public void testCheckAndDecreaseLockingResourceByAccountId_Locking_Test() throws Exception {

        final int threadsNumber = 2;
        final CountDownLatch countDownLatch = new CountDownLatch(threadsNumber);

        final Amount amount = new Amount(new BigDecimal("100.00"), Currency.getInstance("GBP"));
        final Long accountId = 1L;

        commonLockingTests(amount, accountId, "150.00", "50.00", threadsNumber,() -> {
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
    }


    /**
     * This test case tests that in case of a race condition occurs the micro-service handles it gracefully: either no
     * transaction changes the database internal status or just one of them does it. Never both.
     *
     *
     */
    @Test
    public void testNoDeadLock_On_FundsTransfer() throws Exception{

        final int threadsNumber = 2;
        final CountDownLatch countDownLatchFirstThread = new CountDownLatch(2);
        final CountDownLatch countDownLatchSecondThread = new CountDownLatch(2);

        final Amount amount = new Amount(new BigDecimal("150.00"), Currency.getInstance("GBP"));
        final Long firstAccountId = 12345L;
        final Long secondAccountId = 6789L;

        final String firstInitialAmount = "200.00";
        final String secondInitialAmount = "300.00";

        final Account firstAccountToSave = createAccount(firstAccountId, "any-uuid-1", Currency.getInstance("GBP"), firstInitialAmount);
        final Account secondAccountToSave = createAccount(secondAccountId, "any-uuid-2", Currency.getInstance("GBP"), secondInitialAmount);

        database.inTransaction( () -> {
                    accountDAOHelper.getSession().save(firstAccountToSave);
                    accountDAOHelper.getSession().save(secondAccountToSave);
                    accountDAOHelper.getSession().flush();
                }
        );

        final ExecutorService executor = Executors.newFixedThreadPool(threadsNumber);

        final Callable<Account> firstThread = () -> {
            final Account account1;
            final Account account2;
            try {
                account2= accountRepository.checkAndDecreaseLockingResourceByAccountId(amount, secondAccountId);

                countDownLatchFirstThread.countDown();
                countDownLatchFirstThread.await();

                account1= accountRepository.checkAndIncreaseLockingResourceByAccountId(amount, firstAccountId);
            } finally {
                countDownLatchSecondThread.countDown();
            }
            countDownLatchSecondThread.await();
            return account1;
        };

        final Callable<Account> secondThread = () -> {
            final Account account1;
            final Account account2;
            try {
                account1= accountRepository.checkAndIncreaseLockingResourceByAccountId(amount, firstAccountId);

                countDownLatchFirstThread.countDown();
                countDownLatchFirstThread.await();

                account2= accountRepository.checkAndDecreaseLockingResourceByAccountId(amount, secondAccountId);
            } finally {
                countDownLatchSecondThread.countDown();
            }
            countDownLatchSecondThread.await();
            return account2;
        };

        final Map<String, Future<Account>> futureMap = new HashMap<>(threadsNumber);
        futureMap.put(firstAccountToSave.getUuid(), executor.submit( () -> database.inTransaction(firstThread)));
        futureMap.put(secondAccountToSave.getUuid(), executor.submit( () -> database.inTransaction(secondThread)));

        int counter = 0;
        try {
            final Account account = futureMap.get(secondAccountToSave.getUuid()).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(account).isNotNull();
            Assertions.assertThat(account.getTotalAmount()).isEqualByComparingTo("150.00");
        } catch (final Exception e) {
            Assertions.assertThat(e)
                    .hasRootCauseExactlyInstanceOf(IllegalStateException.class)
                    .hasStackTraceContaining("Caused by: java.lang.IllegalStateException: Transaction")
                    .hasStackTraceContaining("has been chosen as a deadlock victim");
            counter++;
        }

        try {
            final Account account = futureMap.get(firstAccountToSave.getUuid()).get(100, TimeUnit.SECONDS);
            Assertions.assertThat(account).isNotNull();
            Assertions.assertThat(account.getTotalAmount()).isEqualByComparingTo("350.00");
        } catch (final Exception e) {
            Assertions.assertThat(e)
                    .hasRootCauseExactlyInstanceOf(IllegalStateException.class)
                    .hasStackTraceContaining("Caused by: java.lang.IllegalStateException: Transaction")
                    .hasStackTraceContaining("has been chosen as a deadlock victim");
            counter++;
        }

        // Asserting that at least one transaction has failed.
        Assertions.assertThat(counter).isBetween(1,2);




    }

}
