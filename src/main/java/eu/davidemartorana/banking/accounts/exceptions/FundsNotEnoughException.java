package eu.davidemartorana.banking.accounts.exceptions;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 * A runtime exception indicating the resource account has not got enough funds to perform the request.
 * There is a {@link javax.ws.rs.core.Response.Status#CONFLICT conflict} between the request and the
 * internal status of the resource.
 *
 * @author Davide Martorana
 */
public class FundsNotEnoughException extends WebApplicationException {

    public FundsNotEnoughException() {
        super(Response.Status.CONFLICT);
    }

    public FundsNotEnoughException(String message) {
        super(message, Response.Status.CONFLICT);
    }

    public FundsNotEnoughException(String message, Throwable cause) {
        super(message, cause, Response.Status.CONFLICT);
    }

    public FundsNotEnoughException(Throwable cause) {
        super(cause, Response.Status.CONFLICT);
    }
}
