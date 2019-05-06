package eu.davidemartorana.banking.accounts.rest.resources;

import com.codahale.metrics.health.HealthCheck;

public class HealthCheckFundsTransfer extends HealthCheck {

    @Override
    protected Result check() throws Exception {

        return Result.healthy("FundsTransfer Application is OK at '%1$tFT%1$tT'", System.currentTimeMillis());
    }
}
