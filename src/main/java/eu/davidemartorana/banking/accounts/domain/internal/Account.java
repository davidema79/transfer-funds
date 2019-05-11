package eu.davidemartorana.banking.accounts.domain.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;

@Entity
@Table(name = "Accounts")
@NamedQueries({
        @NamedQuery(name = "account.byIbanNumber", query = "SELECT a FROM Account a WHERE a.ibanNumber = :iban"),
        @NamedQuery(name = "account.byCustomerId", query = "SELECT a FROM Account a WHERE a.customerId = :customerId"),
        @NamedQuery(name = "account.byUUID", query = "SELECT a FROM Account a WHERE a.uuid = :uuid")
})
public class Account {

    @Id
    @Column(name = "id", nullable = false)
    @JsonIgnore
    private Long id;

    @Column(name = "iban_number", nullable = false)
    @NotNull
    @JsonProperty
    private String ibanNumber;

    @Column(name = "uuid", nullable = false)
    @NotNull
    @JsonProperty("id")
    private String uuid;

    @Column(name = "total_amount", nullable = false)
    @NotNull
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonFormat
    private BigDecimal totalAmount;

    @Column(name = "currency", nullable = false)
    @NotNull
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Currency currency;

    @Column(name = "customer_id", nullable = false)
    @NotNull
    @JsonIgnore
    private Integer customerId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIbanNumber() {
        return ibanNumber;
    }

    public void setIbanNumber(String ibanNumber) {
        this.ibanNumber = ibanNumber;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return new EqualsBuilder()
                .append(id, account.id)
                .append(ibanNumber, account.ibanNumber)
                .append(uuid, account.uuid)
                .append(totalAmount, account.totalAmount)
                .append(currency, account.currency)
                .append(customerId, account.customerId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(ibanNumber)
                .append(uuid)
                .append(totalAmount)
                .append(currency)
                .append(customerId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("ibanNumber", ibanNumber)
                .append("uuid", uuid)
                .append("totalAmount", totalAmount)
                .append("currency", currency)
                .append("customerId", customerId)
                .toString();
    }
}
