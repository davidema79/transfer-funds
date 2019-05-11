package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.dao.TransactionRepository;
import eu.davidemartorana.banking.accounts.domain.Amount;
import eu.davidemartorana.banking.accounts.domain.TransferResult;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import eu.davidemartorana.banking.accounts.domain.internal.TransactionType;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import eu.davidemartorana.banking.accounts.services.AccountsService;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Test for class {@link AccountsService}
 */
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepositoryMock;

    @Mock
    private TransactionRepository transactionsRepositoryMock;

    private AccountsService accountsService;

    private Account createAccount(final Long id, final String uuid, final Currency currency, final String amount) {
        final Account account = new Account();
        account.setId(id);
        account.setUuid(uuid);
        account.setCurrency(currency);
        account.setTotalAmount(new BigDecimal(amount));

        return account;
    }

    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);
        accountsService = new AccountsService(accountRepositoryMock, transactionsRepositoryMock);
    }

    @After
    public void afterEachTest() {
        Mockito.reset();
    }

    @Test
    public void getDetailsByAccountUUID_No_Account_Test() {
        final String uuid = "not-exist-uuid";

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(uuid))).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> accountsService.getAllTransactionsByAccountsUUID(uuid))
                .withMessage("Account with given id was not found");

        Mockito.verify(accountRepositoryMock, Mockito.only()).findByUUID(Mockito.eq(uuid));
        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(uuid));

    }

    @Test
    public void getDetailsByAccountUUID_Exist_Test(){
        final Long accountId= 1L;
        final Account accountMock = Mockito.mock(Account.class);
        Mockito.when(accountMock.getId()).thenReturn(accountId);

        final String uuid = "exist-uuid";
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(uuid))).thenReturn(Optional.of(accountMock));

        final Account actualValueAccount = accountsService.getDetailsByAccountUUID(uuid);
        Assertions.assertThat(actualValueAccount).isNotNull();
        Assertions.assertThat(actualValueAccount).isSameAs(accountMock);

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(uuid));
    }


    @Test
    public void getAllTransactionsByAccountsUUID_Get_EmptyList_Test() {
        final Long accountId= 1L;
        final Account accountMock = Mockito.mock(Account.class);
        Mockito.when(accountMock.getId()).thenReturn(accountId);

        final String uuid = "exist-uuid";
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(uuid))).thenReturn(Optional.of(accountMock));

        Mockito.when(transactionsRepositoryMock.findByAccountId(Mockito.eq(accountId))).thenReturn(Lists.emptyList());

        Assertions.assertThat(accountsService.getAllTransactionsByAccountsUUID(uuid)).isEmpty();

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(uuid));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(1)).findByAccountId(Mockito.eq(accountId));

    }

    @Test
    public void getAllTransactionsByAccountsUUID_Get_NotEmptyList_Test() {
        final Long accountId= 1L;
        final Account accountMock = Mockito.mock(Account.class);
        Mockito.when(accountMock.getId()).thenReturn(accountId);

        final Transaction transaction1 = new Transaction();
        final Transaction transaction2 = new Transaction();

        final String uuid = "exist-uuid";
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(uuid))).thenReturn(Optional.of(accountMock));

        Mockito.when(transactionsRepositoryMock.findByAccountId(Mockito.eq(accountId))).thenReturn(Lists.newArrayList(transaction2,transaction1));

        final List<Transaction> actualValueList = accountsService.getAllTransactionsByAccountsUUID(uuid);
        Assertions.assertThat(actualValueList).isNotEmpty();
        Assertions.assertThat(actualValueList).containsExactlyInAnyOrder(transaction1, transaction2);

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(uuid));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(1)).findByAccountId(Mockito.eq(accountId));

    }

    @Test
    public void transferAmount_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");

        final Long beneficiaryAccountId = 2L;
        final Account beneficiaryAccount = createAccount(beneficiaryAccountId, beneficiaryAccountUUID, Currency.getInstance("GBP"), "200.00");

    }

    @Test
    public void transferAmount_AccountNotExist_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final TransferRequest transferRequest = new TransferRequest();

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("Debtor Account with given id was not found");

        Mockito.verify(accountRepositoryMock, Mockito.only()).findByUUID(Mockito.eq(debtorAccountUUID));
        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(debtorAccountUUID));
    }


    @Test
    public void transferAmount_DifferentCurrency_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("EUR"), "100.00");

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));

        Assertions.assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("The currency accounts must be the same of the transfer instruction.");

        Mockito.verify(accountRepositoryMock, Mockito.only()).findByUUID(Mockito.eq(debtorAccountUUID));
        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(debtorAccountUUID));

    }

    @Test
    public void transferAmount_BeneficiaryAccountsNotExist_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(beneficiaryAccountUUID))).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("Beneficiary Account with given id was not found.");

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(debtorAccountUUID));
        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(beneficiaryAccountUUID));

    }

    @Test
    public void transferAmount_DifferentCurrencyBetweenAccounts_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));

        final Long beneficiaryAccountId = 2L;
        final Account beneficiaryAccount = createAccount(beneficiaryAccountId, beneficiaryAccountUUID, Currency.getInstance("EUR"), "200.00");
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(beneficiaryAccountUUID))).thenReturn(Optional.of(beneficiaryAccount));


        Assertions.assertThatExceptionOfType(BadRequestException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("Both beneficiary and debtor accounts must have the same currency.");

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(debtorAccountUUID));
        Mockito.verify(accountRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq(beneficiaryAccountUUID));

    }

    @Test
    public void transferAmount_DebtorAccountRecordLocked_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");

        final Long beneficiaryAccountId = 2L;
        final Account beneficiaryAccount = createAccount(beneficiaryAccountId, beneficiaryAccountUUID, Currency.getInstance("GBP"), "200.00");

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(beneficiaryAccountUUID))).thenReturn(Optional.of(beneficiaryAccount));


        final WebApplicationException webApplicationException = new WebApplicationException();
        Mockito.when(accountRepositoryMock.checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId))).thenThrow(webApplicationException);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .isSameAs(webApplicationException);

        Mockito.when(accountRepositoryMock.checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId))).thenThrow(PessimisticLockException.class, LockTimeoutException.class, IllegalArgumentException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("The operation cannot be performed at the present time. Account used by another thread. Please try later.")
                .withRootCauseExactlyInstanceOf(PessimisticLockException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("The operation cannot be performed at the present time. Account used by another thread. Please try later.")
                .withRootCauseExactlyInstanceOf(LockTimeoutException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("Error occurred during the funds transfer. Operation not performed.")
                .withRootCauseExactlyInstanceOf(IllegalArgumentException.class);

        Mockito.verify(accountRepositoryMock, Mockito.times(4)).checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId));
        Mockito.verify(transactionsRepositoryMock, Mockito.never()).addTransaction(Mockito.eq(amount), Mockito.eq(debtorAccount), Mockito.eq(TransactionType.DEBIT));
    }

    @Test
    public void transferAmount_BeneficiaryAccountRecordLocked_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");

        final Long beneficiaryAccountId = 2L;
        final Account beneficiaryAccount = createAccount(beneficiaryAccountId, beneficiaryAccountUUID, Currency.getInstance("GBP"), "200.00");

        final Transaction debtorTransaction =  new Transaction();
        debtorTransaction.setAccountId(debtorAccountId);
        debtorTransaction.setAmount(amount.getValue());
        debtorTransaction.setCurrency(amount.getCurrency());
        debtorTransaction.setType(TransactionType.DEBIT.value());
        debtorTransaction.setUuid(UUID.randomUUID().toString());

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(beneficiaryAccountUUID))).thenReturn(Optional.of(beneficiaryAccount));

        Mockito.when(accountRepositoryMock.checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId))).thenReturn(debtorAccount);

        Mockito.when(transactionsRepositoryMock.addTransaction(Mockito.eq(amount), Mockito.eq(debtorAccount), Mockito.eq(TransactionType.DEBIT))).thenReturn(debtorTransaction);


        final WebApplicationException webApplicationException = new WebApplicationException();
        Mockito.when(accountRepositoryMock.checkAndIncreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(beneficiaryAccountId))).thenThrow(webApplicationException);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .isSameAs(webApplicationException);

        Mockito.when(accountRepositoryMock.checkAndIncreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(beneficiaryAccountId))).thenThrow(PessimisticLockException.class, LockTimeoutException.class, IllegalArgumentException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("The operation cannot be performed at the present time. Account used by another thread. Please try later.")
                .withRootCauseExactlyInstanceOf(PessimisticLockException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("The operation cannot be performed at the present time. Account used by another thread. Please try later.")
                .withRootCauseExactlyInstanceOf(LockTimeoutException.class);

        Assertions.assertThatExceptionOfType(WebApplicationException.class)
                .isThrownBy(() -> accountsService.transferAmount(debtorAccountUUID, transferRequest))
                .withMessage("Error occurred during the funds transfer. Operation not performed.")
                .withRootCauseExactlyInstanceOf(IllegalArgumentException.class);

        Mockito.verify(accountRepositoryMock, Mockito.times(4)).checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(4)).addTransaction(Mockito.eq(amount), Mockito.eq(debtorAccount), Mockito.eq(TransactionType.DEBIT));

        Mockito.verify(accountRepositoryMock, Mockito.times(4)).checkAndIncreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(beneficiaryAccountId));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(0)).addTransaction(Mockito.eq(amount), Mockito.eq(beneficiaryAccount), Mockito.eq(TransactionType.CREDIT));
    }


    @Test
    public void transferAmount_SuccessFull_Test() {
        final String debtorAccountUUID = "debtorAccountUUID";
        final String beneficiaryAccountUUID = "beneficiaryAccountUUID";
        final Amount amount = new Amount(new BigDecimal("120.00"), Currency.getInstance("GBP"));

        final TransferRequest transferRequest = new TransferRequest();
        transferRequest.setBeneficiaryAccountId(beneficiaryAccountUUID);
        transferRequest.setAmount(amount);

        final Long debtorAccountId = 1L;
        final Account debtorAccount = createAccount(debtorAccountId, debtorAccountUUID, Currency.getInstance("GBP"), "100.00");

        final Long beneficiaryAccountId = 2L;
        final Account beneficiaryAccount = createAccount(beneficiaryAccountId, beneficiaryAccountUUID, Currency.getInstance("GBP"), "200.00");

        final Transaction debtorTransaction =  new Transaction();
        debtorTransaction.setAccountId(debtorAccountId);
        debtorTransaction.setAmount(amount.getValue());
        debtorTransaction.setCurrency(amount.getCurrency());
        debtorTransaction.setType(TransactionType.DEBIT.value());
        debtorTransaction.setUuid(UUID.randomUUID().toString());

        final Transaction beneficiaryTransaction =  new Transaction();
        beneficiaryTransaction.setAccountId(beneficiaryAccountId);
        beneficiaryTransaction.setAmount(amount.getValue());
        beneficiaryTransaction.setCurrency(amount.getCurrency());
        beneficiaryTransaction.setType(TransactionType.CREDIT.value());
        beneficiaryTransaction.setUuid(UUID.randomUUID().toString());

        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(debtorAccountUUID))).thenReturn(Optional.of(debtorAccount));
        Mockito.when(accountRepositoryMock.findByUUID(Mockito.eq(beneficiaryAccountUUID))).thenReturn(Optional.of(beneficiaryAccount));

        Mockito.when(accountRepositoryMock.checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId))).thenReturn(debtorAccount);
        Mockito.when(accountRepositoryMock.checkAndIncreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(beneficiaryAccountId))).thenReturn(beneficiaryAccount);

        Mockito.when(transactionsRepositoryMock.addTransaction(Mockito.eq(amount), Mockito.eq(debtorAccount), Mockito.eq(TransactionType.DEBIT))).thenReturn(debtorTransaction);
        Mockito.when(transactionsRepositoryMock.addTransaction(Mockito.eq(amount), Mockito.eq(beneficiaryAccount), Mockito.eq(TransactionType.CREDIT))).thenReturn(beneficiaryTransaction);


        final TransferResult transferResult = accountsService.transferAmount(debtorAccountUUID, transferRequest);
        Assertions.assertThat(transferResult).isNotNull();
        Assertions.assertThat(transferResult.getBeneficiaryAccount()).isEqualTo(beneficiaryAccount);
        Assertions.assertThat(transferResult.getDebtorAccount()).isEqualTo(debtorAccount);
        Assertions.assertThat(transferResult.getTransactions())
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrder(debtorTransaction, beneficiaryTransaction);


        Mockito.verify(accountRepositoryMock, Mockito.times(1)).checkAndDecreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(debtorAccountId));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(1)).addTransaction(Mockito.eq(amount), Mockito.eq(debtorAccount), Mockito.eq(TransactionType.DEBIT));

        Mockito.verify(accountRepositoryMock, Mockito.times(1)).checkAndIncreaseLockingResourceByAccountId(Mockito.eq(amount), Mockito.eq(beneficiaryAccountId));
        Mockito.verify(transactionsRepositoryMock, Mockito.times(1)).addTransaction(Mockito.eq(amount), Mockito.eq(beneficiaryAccount), Mockito.eq(TransactionType.CREDIT));
    }

}
