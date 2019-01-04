import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import json.MoneyTransferRequest;
import model.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import repository.AccountsRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MoneyTransferServletTest {
    private static final AccountsRepository ACCOUNTS_REPOSITORY = AccountsRepository.getInstance();
    private static final long ACCOUNT_ID_OF_ACCOUNT_FROM = 1L;
    private static final long ACCOUNT_ID_OF_ACCOUNT_TO = 2L;
    private static final long NON_EXISTING_ACCOUNT_ID = 3L;
    private static final long INITIAL_BALANCE_ON_ACCOUNT_FROM = 150L;
    private static final long INITIAL_BALANCE_ON_ACCOUNT_TO = 10L;
    private static final long EXPECTED_BALANCE_AFTER_TRANSFER_ON_ACCOUNT_FROM = 50L; //150 - 100
    private static final long EXPECTED_BALANCE_AFTER_TRANSFER_ON_ACCOUNT_TO = 110L; //10 + 100
    private static final long AMOUNT_OF_MONEY_TO_TRANSFER = 100L;
    private static final long NOT_VALID_AMOUNT_OF_MONEY_TO_TRANSFER = -200L;
    private static final long TOO_BIG_AMOUNT_TO_TRANSFER = 200L;
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST =
            new MoneyTransferRequest(ACCOUNT_ID_OF_ACCOUNT_FROM, ACCOUNT_ID_OF_ACCOUNT_TO, AMOUNT_OF_MONEY_TO_TRANSFER);
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST_WITH_TOO_BIG_AMOUNT_TO_TRANSFER =
            new MoneyTransferRequest(
                    ACCOUNT_ID_OF_ACCOUNT_FROM,
                    ACCOUNT_ID_OF_ACCOUNT_TO,
                    TOO_BIG_AMOUNT_TO_TRANSFER);
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST_WITH_INVALID_AMOUNT_TO_TRANSFER =
            new MoneyTransferRequest(
                    ACCOUNT_ID_OF_ACCOUNT_FROM,
                    ACCOUNT_ID_OF_ACCOUNT_TO,
                    NOT_VALID_AMOUNT_OF_MONEY_TO_TRANSFER);
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST_FROM_NON_EXISTING_ACCOUNT =
            new MoneyTransferRequest(
                    NON_EXISTING_ACCOUNT_ID,
                    ACCOUNT_ID_OF_ACCOUNT_TO,
                    AMOUNT_OF_MONEY_TO_TRANSFER);
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST_TO_NON_EXISTING_ACCOUNT =
            new MoneyTransferRequest(
                    ACCOUNT_ID_OF_ACCOUNT_FROM,
                    NON_EXISTING_ACCOUNT_ID,
                    AMOUNT_OF_MONEY_TO_TRANSFER);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final MoneyTransferServlet moneyTransferServlet = new MoneyTransferServlet();
    @Mock private HttpServletRequest requestMock;
    @Mock private HttpServletResponse responseMock;

    @Before
    public void setUp() {
        initMocks(this);
        ACCOUNTS_REPOSITORY.add(createAccountFrom());
        ACCOUNTS_REPOSITORY.add(createAccountTo());
    }

    @Test
    public void givenValidMoneyTransferRequest_whenDoPost_thenMoneyAreTransferred() throws Exception {
        //GIVEN
        when(requestMock.getReader()).thenReturn(getBufferedReader(MONEY_TRANSFER_REQUEST));

        //WHEN
        moneyTransferServlet.doPost(requestMock, responseMock);

        //THEN
        assertThat(ACCOUNTS_REPOSITORY.find(ACCOUNT_ID_OF_ACCOUNT_FROM).get().getBalance())
                .isEqualTo(EXPECTED_BALANCE_AFTER_TRANSFER_ON_ACCOUNT_FROM);
        assertThat(ACCOUNTS_REPOSITORY.find(ACCOUNT_ID_OF_ACCOUNT_TO).get().getBalance())
                .isEqualTo(EXPECTED_BALANCE_AFTER_TRANSFER_ON_ACCOUNT_TO);
        verify(responseMock, times(1)).setStatus(SC_OK);
    }

    @Test
    public void givenInvalidAmountToTransfer_whenDoPost_thenMoneyWereNotTransferredAndErrorReturned() throws Exception {
        //GIVEN
        when(requestMock.getReader())
                .thenReturn(getBufferedReader(MONEY_TRANSFER_REQUEST_WITH_INVALID_AMOUNT_TO_TRANSFER));

        //WHEN
        moneyTransferServlet.doPost(requestMock, responseMock);

        //THEN
        assertThatMoneyWereNotTransferredAndErrorReturned();
    }

    @Test
    public void givenTooBigAmountToTransfer_whenDoPost_thenMoneyWereNotTransferredAndErrorReturned() throws Exception {
        //GIVEN
        when(requestMock.getReader())
                .thenReturn(getBufferedReader(MONEY_TRANSFER_REQUEST_WITH_TOO_BIG_AMOUNT_TO_TRANSFER));

        //WHEN
        moneyTransferServlet.doPost(requestMock, responseMock);

        //THEN
        assertThatMoneyWereNotTransferredAndErrorReturned();
    }

    @Test
    public void givenNonExistingAccountToTransferFrom_whenDoPost_thenMoneyWereNotTransferredAndErrorReturned() throws Exception {
        //GIVEN
        when(requestMock.getReader())
                .thenReturn(getBufferedReader(MONEY_TRANSFER_REQUEST_FROM_NON_EXISTING_ACCOUNT));

        //WHEN
        moneyTransferServlet.doPost(requestMock, responseMock);

        //THEN
        assertThatMoneyWereNotTransferredAndErrorReturned();
    }

    @Test
    public void givenNonExistingAccountToTransferTo_whenDoPost_thenMoneyWereNotTransferredAndErrorReturned() throws Exception {
        //GIVEN
        when(requestMock.getReader())
                .thenReturn(getBufferedReader(MONEY_TRANSFER_REQUEST_TO_NON_EXISTING_ACCOUNT));

        //WHEN
        moneyTransferServlet.doPost(requestMock, responseMock);

        //THEN
        assertThatMoneyWereNotTransferredAndErrorReturned();
    }

    private static Account createAccountTo() {
        return new Account(ACCOUNT_ID_OF_ACCOUNT_FROM, INITIAL_BALANCE_ON_ACCOUNT_FROM);
    }

    private static Account createAccountFrom() {
        return new Account(ACCOUNT_ID_OF_ACCOUNT_TO, INITIAL_BALANCE_ON_ACCOUNT_TO);
    }

    private static BufferedReader getBufferedReader(MoneyTransferRequest moneyTransferRequest)
            throws JsonProcessingException {
        return new BufferedReader(new StringReader(OBJECT_MAPPER.writeValueAsString(moneyTransferRequest)));
    }

    private void assertThatMoneyWereNotTransferredAndErrorReturned() throws IOException {
        assertThat(ACCOUNTS_REPOSITORY.find(ACCOUNT_ID_OF_ACCOUNT_FROM).get().getBalance())
                .isEqualTo(INITIAL_BALANCE_ON_ACCOUNT_FROM);
        assertThat(ACCOUNTS_REPOSITORY.find(ACCOUNT_ID_OF_ACCOUNT_TO).get().getBalance())
                .isEqualTo(INITIAL_BALANCE_ON_ACCOUNT_TO);
        verify(responseMock, times(1)).sendError(eq(SC_INTERNAL_SERVER_ERROR), any());
    }
}