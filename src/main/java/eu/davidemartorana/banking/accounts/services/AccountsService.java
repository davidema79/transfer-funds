package eu.davidemartorana.banking.accounts.services;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.dao.TransactionRepository;
import eu.davidemartorana.banking.accounts.domain.TransferResult;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import eu.davidemartorana.banking.accounts.domain.internal.TransactionType;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;
import javax.transaction.TransactionScoped;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsService.class);

    private final AccountRepository accountRepository;

    private final TransactionRepository transactionsRepository;

    @Inject
    public AccountsService(final AccountRepository accountRepository, final TransactionRepository transactionsRepository) {
        this.accountRepository = accountRepository;
        this.transactionsRepository = transactionsRepository;
    }

    public List<Transaction> getAllTransactionsByAccountsUUID(final String uuid) {
        final Account account = getDetailsByAccountUUID(uuid);

        return  transactionsRepository.findByAccountId(account.getId());
    }

    public Account getDetailsByAccountUUID(String accountUUID) {
        LOGGER.trace("Retrieving account by {}", accountUUID);
        final Optional<Account> optionalAccount = accountRepository.findByUUID(accountUUID);
        final Account account = optionalAccount.orElseThrow(()-> new NotFoundException("Account with given id was not found"));

        return  account;
    }

    @TransactionScoped
    public TransferResult transferAmount(final String debtorAccountUUID , final TransferRequest transferRequest) {
        final Optional<Account> optionalDebtorAccount = accountRepository.findByUUID(debtorAccountUUID);
        final Account debtorAccount = optionalDebtorAccount.orElseThrow(()->new NotFoundException("Debtor Account with given id was not found"));

        final Long debtorAccountId = debtorAccount.getId();

        LOGGER.trace("Validation against the account currency");
        if(!debtorAccount.getCurrency().equals(transferRequest.getAmount().getCurrency())) {
            throw new BadRequestException("The currency accounts must be the same of the transfer instruction.");
        }

        final String beneficiaryUUID = transferRequest.getBeneficiaryAccountId();
        final Optional<Account> optionalBeneficiaryAccount = accountRepository.findByUUID(beneficiaryUUID);
        final Account beneficiaryAccount = optionalBeneficiaryAccount.orElseThrow(() -> new BadRequestException("Beneficiary Account with given id was not found."));

        final Long beneficiaryAccountId = beneficiaryAccount.getId();

        LOGGER.trace("Validates that both accounts have the same currency");
        if(!debtorAccount.getCurrency().equals(beneficiaryAccount.getCurrency())){
            throw new BadRequestException("Both beneficiary and debtor accounts must have the same currency.");
        }

        final Account updatedDebtorAccount;
        final Transaction debitTransaction;
        final Account updatedBeneficiaryAccount;
        final Transaction creditTransaction;

        LOGGER.debug("Creating the transactions and updating the account totals. Atomic operation");
        try {
            updatedDebtorAccount = this.accountRepository.checkAndDecreaseLockingResourceByAccountId(transferRequest.getAmount(), debtorAccountId);
            debitTransaction = this.transactionsRepository.addTransaction(transferRequest.getAmount(), debtorAccount, TransactionType.DEBIT);

            updatedBeneficiaryAccount = this.accountRepository.checkAndIncreaseLockingResourceByAccountId(transferRequest.getAmount(), beneficiaryAccountId);
            creditTransaction = this.transactionsRepository.addTransaction(transferRequest.getAmount(), beneficiaryAccount, TransactionType.CREDIT);
        } catch (final WebApplicationException e) {
            throw e;

        } catch (final PessimisticLockException | LockTimeoutException e) {
            throw new WebApplicationException("The operation cannot be performed at the present time. Account used by another thread. Please try later.", e, Response.Status.CONFLICT);
        } catch (final RuntimeException e) {
            LOGGER.error("Error during the funds transfer.", e);
            throw new WebApplicationException("Error occurred during the funds transfer. Operation not performed.", e);
        }

        LOGGER.debug("Transactions created and accounts totals updated. Atomic operation");

        final TransferResult response = new TransferResult();
        response.setBeneficiaryAccount(updatedBeneficiaryAccount);
        response.setDebtorAccount(updatedDebtorAccount);
        response.addTransaction(debitTransaction);
        response.addTransaction(creditTransaction);

        return response;
    }
}
