package model;

import static java.lang.String.format;

public class Account {
    private final long accountId;
    private long balance;

    public Account(long accountId) {
        this.accountId = accountId;
    }

    public Account(long accountId, long initialBalance) {
        this.accountId = accountId;
        this.balance = checkInitialBalance(initialBalance);
    }

    public synchronized void add(long extraAmount) {
        this.balance += extraAmount;
    }

    public synchronized void withdraw(long amountToWithdraw) {
        if (balance < amountToWithdraw) {
            throw new IllegalArgumentException("Not enough money!");
        }
        balance -= amountToWithdraw;
    }

    public synchronized long getBalance() {
        return balance;
    }

    public long getAccountId() {
        return accountId;
    }

    public void transfer(final Account accountTo, long amountToTransfer) {
        validate(accountTo, amountToTransfer);
        Account accountWithSmallerAccountId = (this.accountId < accountTo.accountId) ? this : accountTo;
        Account accountWithBiggerAccountId = (this.accountId > accountTo.accountId) ? this : accountTo;
        synchronized (accountWithSmallerAccountId) {
            synchronized (accountWithBiggerAccountId) {
                withdraw(amountToTransfer);
                accountTo.add(amountToTransfer);
            }
        }
    }

    private long checkInitialBalance(long initialBalance) {
        if (initialBalance < 0) {
            throw new IllegalArgumentException(
                    format("Initial Balance must be more than zero, actual was %s", initialBalance));
        }
        return initialBalance;
    }

    private void validate(Account accountTo, long amountToTransfer) {
        if (amountToTransfer < 0) {
            throw new IllegalArgumentException(
                    format(
                            "Amount of money to transfer should be more than zero, amountToTransfer = %s",
                            amountToTransfer));
        }
        if (this.accountId == accountTo.accountId) {
            throw new IllegalArgumentException("Transfer to the same account is not supported");
        }
    }
}
