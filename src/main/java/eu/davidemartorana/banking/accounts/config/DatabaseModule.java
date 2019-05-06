package eu.davidemartorana.banking.accounts.config;

import com.google.inject.AbstractModule;
import org.hibernate.SessionFactory;

public class DatabaseModule extends AbstractModule {

    private static final DatabaseModule INSTANCE = new DatabaseModule();

    public static final DatabaseModule getInstance(){
        return INSTANCE;
    }

    private DatabaseModule(){}

    @Override
    protected void configure() {
//        super.configure();
        this.bind(SessionFactory.class).toInstance(DatabaseBundles.HIBERNATE_BUNDLE.getSessionFactory());
//        this.bind(SessionFactory.class).toInstance(DatabaseBundles.HIBERNATE_BUNDLE_.getSessionFactory());
//        this.bind(SessionFactory.class).toInstance(DatabaseBundles.HIBERNATE_BUNDLE_ACCOUNT.getSessionFactory());
    }
}
