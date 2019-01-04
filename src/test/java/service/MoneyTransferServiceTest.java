package service;

import json.MoneyTransferRequest;
import model.Account;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import repository.AccountsRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MoneyTransferServiceTest {
    private static final long ACCOUNT_ID_OF_ACCOUNT_FROM = 1L;
    private static final long ACCOUNT_ID_OF_ACCOUNT_TO = 2L;
    private static final long UNKNOWN_ACCOUNT_ID = 3L;
    private static final long AMOUNT_TO_TRANSFER = 100L;
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST =
            new MoneyTransferRequest(ACCOUNT_ID_OF_ACCOUNT_FROM, ACCOUNT_ID_OF_ACCOUNT_TO, AMOUNT_TO_TRANSFER);
    private static final MoneyTransferRequest MONEY_TRANSFER_REQUEST_WITH_UNKNOWN_ACCOUNT_ID =
            new MoneyTransferRequest(UNKNOWN_ACCOUNT_ID, ACCOUNT_ID_OF_ACCOUNT_TO, AMOUNT_TO_TRANSFER);
    @Mock private AccountsRepository accountsRepositoryMock;
    @Mock private Account accountFromMock;
    @Mock private Account accountToMock;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        when(accountsRepositoryMock.find(ACCOUNT_ID_OF_ACCOUNT_FROM)).thenReturn(Optional.of(accountFromMock));
        when(accountsRepositoryMock.find(ACCOUNT_ID_OF_ACCOUNT_TO)).thenReturn(Optional.of(accountToMock));
    }

    @Test
    public void givenTwoExistingAccounts_whenTransfer_thenTransferMethodOfAccountFromIsInvoked() {
        //WHEN
        MoneyTransferService moneyTransferService = new MoneyTransferService(accountsRepositoryMock);
        moneyTransferService.transfer(MONEY_TRANSFER_REQUEST);

        //THEN
        verify(accountFromMock, times(1)).transfer(accountToMock, AMOUNT_TO_TRANSFER);
        verify(accountsRepositoryMock, times(1)).find(ACCOUNT_ID_OF_ACCOUNT_FROM);
        verify(accountsRepositoryMock, times(1)).find(ACCOUNT_ID_OF_ACCOUNT_TO);
    }

    @Test
    public void givenNotExistingAccount_whenTransfer_thenThrowsAnException() {
        //WHEN
        MoneyTransferService moneyTransferService = new MoneyTransferService(accountsRepositoryMock);
        assertThatThrownBy(() -> moneyTransferService.transfer(MONEY_TRANSFER_REQUEST_WITH_UNKNOWN_ACCOUNT_ID))
                .isInstanceOf(IllegalArgumentException.class);
    }
}