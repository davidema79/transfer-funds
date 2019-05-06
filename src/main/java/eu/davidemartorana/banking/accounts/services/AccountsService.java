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
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class AccountsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountsService.class);

    @Inject
    private AccountRepository accountRepository;

    @Inject
    private TransactionRepository transactionsRepository;

    public List<Transaction> getAllTransactionsByAccountsUUID(final String uuid){
        LOGGER.trace("Retrieving transaction by account id {}", uuid);
        final Optional<Account> optionalAccount = accountRepository.findByUUID(uuid);
        final Account account = optionalAccount.orElseThrow(()->new NotFoundException("Account with given id was not found"));

        return  transactionsRepository.findByAccountId(account.getId());
    }

    public TransferResult transferAmount(final String debtorAccountUUID , final TransferRequest transferRequest) {
        final Optional<Account> optionalDebtorAccount = accountRepository.findByUUID(debtorAccountUUID);
        final Account debtorAccount = optionalDebtorAccount.orElseThrow(()->new NotFoundException("Debtor Account with given id was not found"));

        final Integer debtorAccountId = debtorAccount.getId();

        LOGGER.trace("Validation against the account currency");
        if(!debtorAccount.getCurrency().equals(transferRequest.getAmount().getCurrency())) {
            throw new BadRequestException("The currency accounts must be the same of the transfer instruction.");
        }

        final String beneficiaryUUID = transferRequest.getBeneficiaryAccountId().toString();
        final Optional<Account> optionalBeneficiaryAccount = accountRepository.findByUUID(beneficiaryUUID);
        final Account beneficiaryAccount = optionalBeneficiaryAccount.orElseThrow(()-> new BadRequestException("Debtor Account with given id was not found"));

        final Integer beneficiaryAccountId = beneficiaryAccount.getId();

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


    public Account getDetailsByAccountUUID(String accountUUID) {
        LOGGER.trace("Retrieving account by {}", accountUUID);
        final Optional<Account> optionalAccount = accountRepository.findByUUID(accountUUID);
        final Account account = optionalAccount.orElseThrow(()->new NotFoundException("Account with given id was not found"));

        return  account;
    }
}
