package eu.davidemartorana.banking.accounts.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;

import java.util.ArrayList;
import java.util.List;

public class TransferResult {

    @JsonProperty
    private Account debtorAccount;

    private Account beneficiaryAccount;

    private final List<Transaction> transactions = new ArrayList<>(2);

    public Account getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(Account debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public Account getBeneficiaryAccount() {
        return beneficiaryAccount;
    }

    public void setBeneficiaryAccount(Account beneficiaryAccount) {
        this.beneficiaryAccount = beneficiaryAccount;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions.clear();
        this.transactions.addAll(transactions);
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
    }
}
