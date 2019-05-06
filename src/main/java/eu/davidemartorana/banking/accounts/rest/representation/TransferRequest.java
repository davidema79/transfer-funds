package eu.davidemartorana.banking.accounts.rest.representation;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.davidemartorana.banking.accounts.domain.Amount;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.UUID;

public class TransferRequest {

    @JsonProperty
    @NotNull
    private Amount amount;

    @JsonProperty
    @Length(min = 36, max = 36)
    private String beneficiaryAccountId;

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public String getBeneficiaryAccountId() {
        return beneficiaryAccountId;
    }

    public void setBeneficiaryAccountId(String beneficiaryAccountId) {
        this.beneficiaryAccountId = beneficiaryAccountId;
    }
}
