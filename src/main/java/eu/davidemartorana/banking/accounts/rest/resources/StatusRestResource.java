package eu.davidemartorana.banking.accounts.rest.resources;

import eu.davidemartorana.banking.accounts.rest.representation.Status;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/status")
@Produces(MediaType.APPLICATION_JSON)
public class StatusRestResource  {

    private final Status currentStatus = new Status();


    @GET
    public Status getStatus(){
        return currentStatus;
    }
}
