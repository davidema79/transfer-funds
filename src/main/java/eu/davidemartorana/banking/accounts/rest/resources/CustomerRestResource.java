package eu.davidemartorana.banking.accounts.rest.resources;

import com.codahale.metrics.annotation.Timed;
import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Customer;
import eu.davidemartorana.banking.accounts.rest.representation.EntriesListResponse;
import eu.davidemartorana.banking.accounts.services.CustomersService;
import io.dropwizard.hibernate.UnitOfWork;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
public class CustomerRestResource {

    private final CustomersService customersService;


    @Inject
    public CustomerRestResource(final CustomersService customersService){
        this.customersService = customersService;
    }

    @GET
    @Timed
    @UnitOfWork
    public EntriesListResponse<Customer> getAll() {
        final List<Customer> list = customersService.getAllCustomers();

        return EntriesListResponse.of("customers", list);
    }

    @GET
    @Path("/{customerUUId}/accounts")
    @Timed
    @UnitOfWork
    public EntriesListResponse<Account> getAllByCustomerId(@PathParam("customerUUId") String customerUUID){
        final List<Account> list =  customersService.getAllAccountsByCustomerUUID(customerUUID);

        return EntriesListResponse.of("accounts", list);
    }

    @GET
    @Path("/{customerUUId}")
    @Timed
    @UnitOfWork
    public Customer getCustomerByUUID(@PathParam("customerUUId") String customerUUID){
        return customersService.getByCustomerUUID(customerUUID);
    }

}
