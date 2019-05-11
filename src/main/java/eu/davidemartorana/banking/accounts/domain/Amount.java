package eu.davidemartorana.banking.accounts.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nonnegative;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

public class Amount {

    @JsonProperty
    @NotNull
    @Nonnegative
    private BigDecimal value;

    @JsonProperty
    @NotNull
    @org.hibernate.validator.constraints.Currency("GBP")
    private Currency currency;

    public Amount(){}

    public Amount(BigDecimal value, Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
}
