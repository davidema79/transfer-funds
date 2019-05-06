package eu.davidemartorana.banking.accounts.rest.resources;

import com.codahale.metrics.annotation.Timed;
import eu.davidemartorana.banking.accounts.domain.TransferResult;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import eu.davidemartorana.banking.accounts.rest.representation.TransferRequest;
import eu.davidemartorana.banking.accounts.rest.representation.TransferResponse;
import eu.davidemartorana.banking.accounts.services.AccountsService;
import io.dropwizard.hibernate.UnitOfWork;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestResource {


    @Inject
    private AccountsService accountsService;

    @GET
    @Path("/{accountUUID}")
    @Timed
    @UnitOfWork
    public Account getDetailsByAccountUUID(@PathParam("accountUUID") final String accountUUID){
        return this.accountsService.getDetailsByAccountUUID(accountUUID);
    }

    @GET
    @Path("/{accountUUID}/transactions")
    @Timed
    @UnitOfWork
    public List<Transaction> getTransactionByAccountUUID(@PathParam("accountUUID") final String accountUUID){
        return this.accountsService.getAllTransactionsByAccountsUUID(accountUUID);
    }


    @POST
    @Path("/{accountUUID}/transfer")
    @Timed
    @UnitOfWork
    public TransferResponse transferFromAccount(@PathParam("accountUUID") final String accountUUID, @NotNull @Valid TransferRequest transferRequest){
         final TransferResult result = this.accountsService.transferAmount(accountUUID, transferRequest);

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
