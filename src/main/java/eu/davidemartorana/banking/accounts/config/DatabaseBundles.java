package eu.davidemartorana.banking.accounts.config;

import eu.davidemartorana.banking.accounts.domain.internal.Account;
import eu.davidemartorana.banking.accounts.domain.internal.Customer;
import eu.davidemartorana.banking.accounts.domain.internal.Transaction;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.flyway.FlywayBundle;
import io.dropwizard.flyway.FlywayFactory;
import io.dropwizard.hibernate.HibernateBundle;

public class DatabaseBundles {

    public static final HibernateBundle<ApplicationConfig> HIBERNATE_BUNDLE = new HibernateBundle<ApplicationConfig>(Customer.class, Account.class, Transaction.class ) {
        @Override
        public DataSourceFactory getDataSourceFactory(ApplicationConfig configuration) {
            return configuration.getDataSourceFactory();
        }

        @Override
        protected String name() {
            return "hibernate.customer";
        }
    };

    public static final FlywayBundle<ApplicationConfig> FLYWAY_BUNDLE = new FlywayBundle<ApplicationConfig>() {

        @Override
        public DataSourceFactory getDataSourceFactory(ApplicationConfig configuration) {
            return configuration.getDataSourceFactory();
        }

        @Override
        public FlywayFactory getFlywayFactory(ApplicationConfig configuration) {
            return configuration.getFlywayFactory();
        }
    };

//    public static final HibernateBundle<ApplicationConfig> HIBERNATE_BUNDLE_ACCOUNT = new HibernateBundle<ApplicationConfig>(Account.class) {
//        @Override
//        public DataSourceFactory getDataSourceFactory(ApplicationConfig configuration) {
//            return configuration.getDataSourceFactory();
//        }
//
//        @Override
//        protected String name() {
//            return "hibernate.account";
//        }
//    };
//
//    public static final HibernateBundle<ApplicationConfig> HIBERNATE_BUNDLE_TRANSACTION = new HibernateBundle<ApplicationConfig>(Transaction.class) {
//        @Override
//        public DataSourceFactory getDataSourceFactory(ApplicationConfig configuration) {
//            return configuration.getDataSourceFactory();
//        }
//
//        @Override
//        protected String name() {
//            return "hibernate.transaction";
//        }
//    };

}
