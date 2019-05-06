package eu.davidemartorana.banking.accounts.domain.internal;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Date;

@Entity
@Table(name = "account_transactions")
@NamedQueries({
        @NamedQuery(name="transactions.findByAccountId", query = "SELECT t FROM Transaction t WHERE t.accountId = :accountId")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    @JsonIgnore
    private Integer id;

    @Column(name = "uuid", nullable = false)
    @NotNull
    @JsonProperty("id")
    private String uuid;

    @Column(name = "amount", nullable = false)
    @NotNull
    @JsonProperty
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    @NotNull
    @JsonProperty
    private Currency currency;

    @Column(name = "date_time", nullable = false)
    @NotNull
    @JsonProperty
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss ZZZ")
    private DateTime dateTime;

    @Column(name = "type", nullable = false)
    @NotNull
    @JsonProperty
    private String type;

    @Column(name = "account_id", nullable = false)
    @NotNull
    @JsonIgnore
    private Integer accountId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Transaction that = (Transaction) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .append(uuid, that.uuid)
                .append(amount, that.amount)
                .append(currency, that.currency)
                .append(dateTime, that.dateTime)
                .append(type, that.type)
                .append(accountId, that.accountId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(uuid)
                .append(amount)
                .append(currency)
                .append(dateTime)
                .append(type)
                .append(accountId)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("uuid", uuid)
                .append("amount", amount)
                .append("currency", currency)
                .append("dateTime", dateTime)
                .append("type", type)
                .append("accountId", accountId)
                .toString();
    }
}