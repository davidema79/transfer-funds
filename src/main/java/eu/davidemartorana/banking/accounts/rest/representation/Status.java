package eu.davidemartorana.banking.accounts.rest.representation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Status {

    private final String version = "1.0.0";
    private final String name = "Funds-transfer";
    private final String description = "Transfer funds between accounts";

    @JsonProperty
    public String getVersion() {
        return version;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public String getDescription() {
        return description ;
    }
}
