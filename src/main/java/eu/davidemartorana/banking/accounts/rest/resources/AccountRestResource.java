package eu.davidemartorana.banking.accounts.rest.resources;

import com.codahale.metrics.annotation.Timed;
import eu.davidemartorana.banking.accounts.domain.TransferResult;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import eu.davidemartorana.banking.accounts.rest.representation.EntriesListResponse;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import eu.davidemartorana.banking.accounts.rest.representation.TransferResponse;
import eu.davidemartorana.banking.accounts.services.AccountsService;
import io.dropwizard.hibernate.UnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * REST Service for the endpoint related to the accounts handling.
 *
 * @author Davide Martorana
 */
@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountRestResource.class);


    private final AccountsService accountsService;

    @Inject
    public AccountRestResource(final AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @GET
    @Path("/{accountUUID}")
    @Timed
    @UnitOfWork
    public Account getDetailsByAccountUUID(@PathParam("accountUUID") final String accountUUID) {
        LOGGER.debug("Retrieving account by uuid: [{}]", accountUUID);
        return this.accountsService.getDetailsByAccountUUID(accountUUID);
    }

    @GET
    @Path("/{accountUUID}/transactions")
    @Timed
    @UnitOfWork
    public EntriesListResponse<Transaction> getTransactionByAccountUUID(@PathParam("accountUUID") final String accountUUID){
        LOGGER.debug("Retrieving transactions of account uuid: [{}]", accountUUID);
        final List<Transaction> list = this.accountsService.getAllTransactionsByAccountsUUID(accountUUID);

        return EntriesListResponse.of("transactions", list);
    }


    @POST
    @Path("/{accountUUID}/transfer")
    @Timed
    @UnitOfWork
    public TransferResponse transferFromAccount(@PathParam("accountUUID") final String accountUUID, @NotNull @Valid TransferRequest transferRequest){
        LOGGER.debug("Funds transfer from account uuid [{}] -> [{}]", accountUUID, transferRequest.getBeneficiaryAccountId());

        final TransferResult result = this.accountsService.transferAmount(accountUUID, transferRequest);

        LOGGER.trace("Transfer successfully happened. Result: [{}]", result);
        // Hide some information regarding the beneficiary account, to prevent being seen by the operation initiator
        final Account beneficiaryAccount = new Account();
        beneficiaryAccount.setCurrency(result.getBeneficiaryAccount().getCurrency());
        beneficiaryAccount.setIbanNumber(result.getBeneficiaryAccount().getIbanNumber());
        beneficiaryAccount.setUuid(result.getBeneficiaryAccount().getUuid());

        final TransferResponse response = new TransferResponse();
        response.setDebtorAccount(result.getDebtorAccount());
        response.setBeneficiaryAccount(beneficiaryAccount);
        response.setTransactions(result.getTransactions());

        return response;
    }

}
