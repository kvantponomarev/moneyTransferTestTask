package model;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AccountTest {
    private static final long ACCOUNT_ID_OF_ACCOUNT_FROM = 1L;
    private static final long ACCOUNT_ID_OF_ACCOUNT_TO = 2L;
    private static final long VALID_INITIAL_BALANCE_ON_ACCOUNT_FROM = 150L;
    private static final long VALID_INITIAL_BALANCE_ON_ACCOUNT_TO = 10L;
    private static final long VALID_AMOUNT_OF_MONEY_TO_TRANSFER = 100L;
    private static final long INVALID_INITIAL_BALANCE = -10L;
    private static final long EXPECTED_AMOUNT_AFTER_TRANSFER_ON_ACCOUNT_FROM = 50;
    private static final long EXPECTED_AMOUNT_AFTER_TRANSFER_ON_ACCOUNT_TO = 110;
    private static final long NOT_VALID_AMOUNT_OF_MONEY_TO_TRANSFER = -200L;
    private static final long TOO_BIG_AMOUNT_TO_TRANSFER = 200L;

    @Test
    public void givenAccountFromAccountToAndValidAmountToTransfer_whenTransfer_thenMoneyAreTransferred() {
        //GIVEN
        Account accountFrom = createTestAccountFrom();
        Account accountTo = createTestAccountTo();

        //WHEN
        accountFrom.transfer(accountTo, VALID_AMOUNT_OF_MONEY_TO_TRANSFER);

        //THEN
        assertThat(accountFrom.getBalance()).isEqualTo(EXPECTED_AMOUNT_AFTER_TRANSFER_ON_ACCOUNT_FROM);
        assertThat(accountTo.getBalance()).isEqualTo(EXPECTED_AMOUNT_AFTER_TRANSFER_ON_ACCOUNT_TO);
    }

    @Test
    public void givenInvalidAmountToTransfer_whenTransfer_thenExceptionIsThrownAndBalancesRemainTheSame() {
        //GIVEN
        Account accountFrom = createTestAccountFrom();
        Account accountTo = createTestAccountTo();

        //WHEN + THEN
        assertThatThrownBy(() -> accountFrom.transfer(accountTo, NOT_VALID_AMOUNT_OF_MONEY_TO_TRANSFER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatMoneyAreNotTransferred(accountFrom, accountTo);
    }

    @Test
    public void givenToBigAmountToTransfer_whenTransfer_thenExceptionIsThrownAndBalancesRemainTheSame() {
        //GIVEN
        Account accountFrom = createTestAccountFrom();
        Account accountTo = createTestAccountTo();

        //WHEN + THEN
        assertThatThrownBy(() -> accountFrom.transfer(accountTo, TOO_BIG_AMOUNT_TO_TRANSFER))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatMoneyAreNotTransferred(accountFrom, accountTo);
    }

    @Test
    public void givenTwoTransfersInOppositeDirectionAtTheSameTime_whenTransfer_thenBothTransfersArePerformed() throws InterruptedException {
        //GIVEN
        Account accountFrom = createTestAccountFrom();
        Account accountTo = createTestAccountTo();

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(2);

        runTransferAsync(startLatch, endLatch, accountFrom, accountTo, 50L);
        runTransferAsync(startLatch, endLatch, accountTo, accountFrom, 10L);

        //WHEN
        startLatch.countDown();
        endLatch.await();

        //THEN
        assertThat(accountFrom.getBalance()).isEqualTo(110L);
        assertThat(accountTo.getBalance()).isEqualTo(50L);
    }

    @Test
    public void givenInvalidInitialBalance_whenCreatingAccount_thenThrowAnException() {
        assertThatThrownBy(() -> new Account(VALID_INITIAL_BALANCE_ON_ACCOUNT_FROM, INVALID_INITIAL_BALANCE))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void givenAccountAndMoneyToAdd_whenAdd_thenMoneyAreAdded() {
        //GIVEN
        Account account = new Account(1L, 150L);

        //WHEN
        account.add(30L);

        //THEN
        assertThat(account.getBalance()).isEqualTo(180L);
    }

    @Test
    public void givenAccountAndValidAmountOfMoneyToWithdraw_whenWithdraw_thenMoneyAreWithdrawn() {
        //GIVEN
        Account account = new Account(1L, 150L);

        //WHEN
        account.withdraw(30L);

        //THEN
        assertThat(account.getBalance()).isEqualTo(120L);
    }

    @Test
    public void givenAccountAndInvalidValidAmountOfMoneyToWithdraw_whenWithdraw_thenAnExceptionIsThrown() {
        //GIVEN
        Account account = new Account(1L, 150L);

        //WHEN + THEN
        assertThatThrownBy(() -> account.withdraw(200L)).isInstanceOf(IllegalArgumentException.class);
    }

    private static Account createTestAccountFrom() {
        return new Account(ACCOUNT_ID_OF_ACCOUNT_FROM, VALID_INITIAL_BALANCE_ON_ACCOUNT_FROM);
    }

    private static Account createTestAccountTo() {
        return new Account(ACCOUNT_ID_OF_ACCOUNT_TO, VALID_INITIAL_BALANCE_ON_ACCOUNT_TO);
    }

    private static void assertThatMoneyAreNotTransferred(Account accountFrom, Account accountTo) {
        assertThat(accountFrom.getBalance()).isEqualTo(VALID_INITIAL_BALANCE_ON_ACCOUNT_FROM);
        assertThat(accountTo.getBalance()).isEqualTo(VALID_INITIAL_BALANCE_ON_ACCOUNT_TO);
    }

    private static void runTransferAsync(
            CountDownLatch startLatch,
            CountDownLatch endLatch,
            Account accountFrom,
            Account accountTo,
            long amount) {
        new Thread(()-> {
            try {
                startLatch.await();
                try {
                    accountFrom.transfer(accountTo, amount);
                } finally {
                    endLatch.countDown();
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }
}