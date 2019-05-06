package eu.davidemartorana.banking.accounts.domain.internal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Customers")
@NamedQueries({
        @NamedQuery(name = "customer.byUUID", query = "SELECT c FROM Customer c WHERE c.uuid = :uuid")
})
public class Customer {

    @Id
    @Column(name = "id", nullable = false)
    @JsonIgnore
    private Integer id;

    @Column(name = "name", nullable = false)
    @NotNull
    @JsonProperty
    private String name;

    @Column(name = "uuid", nullable = false)
    @NotNull
    @JsonProperty("id")
    private String uuid;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;

        return new EqualsBuilder()
                .append(id, customer.id)
                .append(name, customer.name)
                .append(uuid, customer.uuid)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .append(name)
                .append(uuid)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("name", name)
                .append("uuid", uuid)
                .toString();
    }
}
