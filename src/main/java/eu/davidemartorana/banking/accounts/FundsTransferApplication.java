package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.config.ApplicationConfig;
import eu.davidemartorana.banking.accounts.config.DatabaseBundles;
import eu.davidemartorana.banking.accounts.config.DatabaseModule;
import eu.davidemartorana.banking.accounts.rest.resources.AccountRestResource;
import eu.davidemartorana.banking.accounts.rest.resources.CustomerRestResource;
import eu.davidemartorana.banking.accounts.rest.resources.HealthCheckFundsTransfer;
import eu.davidemartorana.banking.accounts.rest.resources.StatusRestResource;
import io.dropwizard.Application;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.vyarus.dropwizard.guice.GuiceBundle;

/**
 * Main Class / Application Starter.
 *
 * @author Davide Martorana
 */
public class FundsTransferApplication extends Application<ApplicationConfig> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FundsTransferApplication.class);

    public static void main(String[] args) throws Exception {
        LOGGER.debug("/*****************************************/");
        LOGGER.debug("/****     Application starting...     ****/");
        LOGGER.debug("/*****************************************/");

        new FundsTransferApplication().run(args);

        LOGGER.debug("/****************************************/");
        LOGGER.debug("/****     ...application started     ****/");
        LOGGER.debug("/****************************************/");
    }

    @Override
    public String getName() {
        return "funds-transfer";
    }

    @Override
    public void initialize(final Bootstrap<ApplicationConfig> bootstrap) {
        bootstrap.addBundle(DatabaseBundles.HIBERNATE_BUNDLE);

        LOGGER.info("Scanning the package to find all the needed Beans, including the resources in Jersey.");
        bootstrap.addBundle(GuiceBundle.builder()
                .enableAutoConfig(FundsTransferApplication.class.getPackage().getName())
                .modules(DatabaseModule.getInstance())
                .build());

    }

    @Override
    public void run(final ApplicationConfig applicationConfig, final Environment environment) throws Exception {
        LOGGER.info("Database initialisation");
        final ManagedDataSource dataSource = applicationConfig.getDataSourceFactory().build(environment.metrics(), "flyway-migration");
        final Flyway flyway = applicationConfig.getFlywayFactory().build(dataSource);
        flyway.migrate();

        LOGGER.info("Registration: healthCheck");
        environment.healthChecks().register("simpleHealthCheck", new HealthCheckFundsTransfer());



    }
}
