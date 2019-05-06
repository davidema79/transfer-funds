package eu.davidemartorana.banking.accounts.rest.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;

import java.util.List;

public class TransferResponse {

    @JsonProperty
    private Account debtorAccount;

    @JsonProperty
    private Account beneficiaryAccount;

    @JsonProperty
    private List<Transaction> transactions;

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

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
}
