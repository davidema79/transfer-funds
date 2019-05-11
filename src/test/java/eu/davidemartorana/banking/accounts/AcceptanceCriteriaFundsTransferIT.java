package eu.davidemartorana.banking.accounts;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.davidemartorana.banking.accounts.config.ApplicationConfig;
import eu.davidemartorana.banking.accounts.domain.Amount;
import eu.davidemartorana.banking.accounts.rest.representation.EntriesListResponse;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import eu.davidemartorana.banking.accounts.rest.representation.TransferResponse;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.FixtureHelpers;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.ClassRule;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 */
public class AcceptanceCriteriaFundsTransferIT {

    private static final String CONFIG_PATH = ResourceHelpers.resourceFilePath("test-config.yml");

    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();

    @ClassRule
    public static final DropwizardAppRule<ApplicationConfig> RULE = new DropwizardAppRule<>(FundsTransferApplication.class, CONFIG_PATH);

    private void assertTransactions(final String accountUUID, final String fileNameFixturePath, final boolean removeRandomValues) throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/accounts/%s/transactions", RULE.getLocalPort(), accountUUID))
                .request()
                .get();

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        final String actualJSON;
        final String expectedJSON;
        final String fileContent = FixtureHelpers.fixture("fixtures/integration/" + fileNameFixturePath);

        final EntriesListResponse<LinkedHashMap> transactionListActual = response.readEntity(EntriesListResponse.class);
        final EntriesListResponse<LinkedHashMap> transactionListExpected = RULE.getObjectMapper().readValue(fileContent, EntriesListResponse.class);

        if(removeRandomValues) {
            //Removing variable parts
            transactionListActual.getContentList().forEach(itemMap -> {
                itemMap.put("dateTime","2019-05-10T23:38:42+00:00");
                itemMap.put("id","");
            });

            transactionListExpected.getContentList().forEach(itemMap -> {
                itemMap.put("dateTime","2019-05-10T23:38:42+00:00");
                itemMap.put("id","");
            });
        }

        Assertions.assertThat(transactionListActual.getContentList()).containsExactlyElementsOf(transactionListExpected.getContentList());
    }

    @Test
    public void test_GET_APIIdentifier() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/identifier", RULE.getLocalPort()))
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-api-identifier.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }

    @Test
    public void test_GET_allCustomers() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/customers", RULE.getLocalPort()))
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-all-customers.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }

    @Test
    public void test_GET_customerByUUID() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/customers/52bc1e5d-af14-4fa0-8641-a1a3622e69e1",RULE.getLocalPort()))
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-customer-by-id-XXX-a1a3622e69e1.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }

    @Test
    public void test_GET_AccountsByCustomerUUID() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/customers/f88ea81b-e6a9-42e9-ac7a-c52bff551060/accounts",RULE.getLocalPort()))
                .request()
                .get();

        assertThat(response.getStatus()).isEqualTo(200);
        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-accounts-by-customer-id-XXX-c52bff551060.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }


    @Test
    public void test_GET_accountDetails() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/accounts/85216e45-8dd1-41d7-a890-3cf71acd2630", RULE.getLocalPort()))
                .request()
                .get();

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-account-by-id-3cf71acd2630.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }


    @Test
    public void test_GET_Transactions() throws Exception {
        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/accounts/85216e45-8dd1-41d7-a890-3cf71acd2630/transactions", RULE.getLocalPort()))
                .request()
                .get();

        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        final String actualJSON = response.readEntity(String.class);
        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/get-transactions-by-account-XXX-3cf71acd2630.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);
    }

    @Test
    public void test_POST_Transfer() throws Exception {
        // Assertion of transaction prior transfer funds
        assertTransactions("85216e45-8dd1-41d7-a890-3cf71acd2630", "get-transactions-by-account-XXX-3cf71acd2630.json", false);
        assertTransactions("7f9adf98-93fb-4868-8cd8-cfe0f53628c1", "get-transactions-by-account-XXX-cfe0f53628c1.json", false);

        final TransferRequest request = new TransferRequest();
        final Amount amount = new Amount();
        amount.setCurrency(Currency.getInstance("GBP"));
        amount.setValue(new BigDecimal("100.00"));
        request.setAmount(amount);
        request.setBeneficiaryAccountId("85216e45-8dd1-41d7-a890-3cf71acd2630");

        final Response response = RULE.client()
                .target(String.format("http://localhost:%s/api/accounts/7f9adf98-93fb-4868-8cd8-cfe0f53628c1/transfer", RULE.getLocalPort()) )
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE));


        Assertions.assertThat(response.getStatus()).isEqualTo(200);

        // Asserting Transfer Response Body - removing random and datetime values from response
        final TransferResponse actualResponse = response.readEntity(TransferResponse.class);
        actualResponse.getTransactions().get(0).setUuid("");
        actualResponse.getTransactions().get(0).setDateTime(DateTime.parse("2019-05-10T23:38:42+00:00"));
        actualResponse.getTransactions().get(1).setUuid("");
        actualResponse.getTransactions().get(1).setDateTime(DateTime.parse("2019-05-10T23:38:42+00:00"));

        final String actualJSON = MAPPER.writeValueAsString(actualResponse);

        final String expectedJSON = FixtureHelpers.fixture("fixtures/integration/transfer-from-XXX-cfe0f53628c1-TO-XXX-3cf71acd2630.json");

        JSONAssert.assertEquals(expectedJSON, actualJSON, false);

        // Asserting Transfer Response Body
        assertTransactions("85216e45-8dd1-41d7-a890-3cf71acd2630", "get-transactions-after-transfer-by-account-XXX-3cf71acd2630.json", true);
        assertTransactions("7f9adf98-93fb-4868-8cd8-cfe0f53628c1", "get-transactions-after-transfer-by-account-XXX-cfe0f53628c1.json", true);
    }

}