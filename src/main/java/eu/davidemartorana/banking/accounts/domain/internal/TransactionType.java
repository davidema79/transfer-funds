package eu.davidemartorana.banking.accounts.domain.internal;

public enum TransactionType {

    DEBIT("DEBIT"),
    CREDIT("CREDIT");

    private String value;

    TransactionType(final String value) {
        this.value = value;
    }

    public String value(){
        return this.value;
    }
}
