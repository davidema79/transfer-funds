package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.config.ApplicationConfig;
import eu.davidemartorana.banking.accounts.domain.Amount;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Customer;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import eu.davidemartorana.banking.accounts.rest.representation.TransferResponse;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.junit.ClassRule;
import org.junit.Test;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AcceptanceCriteriaFundsTransferTest {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

    @ClassRule
    public static final DropwizardAppRule<ApplicationConfig> RULE = new DropwizardAppRule<>(FundsTransferApplication.class, CONFIG_PATH);

    @Test
    public void test_GET_Customers() throws Exception {

        final List<Customer> customerList = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/api/customers")
                .request()
                .get(List.class);
        assertThat(customerList.size()).isEqualTo(3);
    }

    @Test
    public void test_GET_Accounts() throws Exception {

        final List<Account> accountList = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/api/customers/f88ea81b-e6a9-42e9-ac7a-c52bff551060/accounts")
                .request()
                .get(List.class);
        assertThat(accountList.size()).isEqualTo(1);
    }

    @Test
    public void test_POST_Transfer() throws Exception {
        final TransferRequest request = new TransferRequest();
        final Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("GBP"));
        amount.setValue(new BigDecimal("100.00"));
        request.setAmount(amount);
        request.setBeneficiaryAccountId("85216e45-8dd1-41d7-a890-3cf71acd2630");

        final TransferResponse transferResponse = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/api/accounts/7f9adf98-93fb-4868-8cd8-cfe0f53628c1/transfer")
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE))
                .readEntity(TransferResponse.class);
        assertThat(transferResponse.getTransactions().size()).isEqualTo(2);

        assertThat(transferResponse.getDebtorAccount().getCurrency()).isEqualTo(Currency.getInstance("GBP"));
        assertThat(transferResponse.getDebtorAccount().getTotalAmount()).isEqualByComparingTo("20.00");
        assertThat(transferResponse.getDebtorAccount().getIbanNumber()).isEqualTo("GB40REVO60161331926819");
        assertThat(transferResponse.getDebtorAccount().getUuid()).isEqualTo("7f9adf98-93fb-4868-8cd8-cfe0f53628c1");

        assertThat(transferResponse.getBeneficiaryAccount().getIbanNumber()).isEqualTo("GB40REVO00991232026772");
        assertThat(transferResponse.getBeneficiaryAccount().getCurrency()).isEqualTo(Currency.getInstance("GBP"));
        assertThat(transferResponse.getBeneficiaryAccount().getUuid()).isEqualTo("85216e45-8dd1-41d7-a890-3cf71acd2630");
        assertThat(transferResponse.getBeneficiaryAccount().getTotalAmount()).isNull();

    }

}