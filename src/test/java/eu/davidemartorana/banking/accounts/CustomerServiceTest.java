package eu.davidemartorana.banking.accounts;

import eu.davidemartorana.banking.accounts.dao.AccountRepository;
import eu.davidemartorana.banking.accounts.dao.CustomerRepository;
import eu.davidemartorana.banking.accounts.services.CustomersService;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.skyscreamer.jsonassert.JSONAssert;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 *
 * @author Davide Martorana
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepositoryMock;

    @Mock
    private AccountRepository accountRepositoryMock;

    private CustomersService customersService;

    @Before
    public void beforeEachTest() {
        MockitoAnnotations.initMocks(this);
        customersService = new CustomersService(customerRepositoryMock, accountRepositoryMock);


    }

    @After
    public void afterEachTest() {
        Mockito.reset();
    }


    @Test
    public void getCustomers_EmptyList_Test() {
        when(customerRepositoryMock.findAll()).thenReturn(new ArrayList<>());

        assertThat(customersService.getAllCustomers()).isEmpty();

        Mockito.verify(customerRepositoryMock, Mockito.only()).findAll();
        Mockito.verify(customerRepositoryMock, Mockito.times(1)).findAll();
    }


    @Test
    public void getCustomers_NotFoundException_Test() {
        when(customerRepositoryMock.findByUUID(Mockito.eq("uuid-value-not-exits"))).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(NotFoundException.class)
                .isThrownBy(() -> customersService.getByCustomerUUID("uuid-value-not-exits"))
                .withMessage("Customer with given id was not found");

        Mockito.verify(customerRepositoryMock, Mockito.only()).findByUUID(Mockito.eq("uuid-value-not-exits"));
        Mockito.verify(customerRepositoryMock, Mockito.times(1)).findByUUID(Mockito.eq("uuid-value-not-exits"));
    }

}
