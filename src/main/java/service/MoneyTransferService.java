package service;

import json.MoneyTransferRequest;
import model.Account;
import org.assertj.core.util.VisibleForTesting;
import repository.AccountsRepository;

import static java.lang.String.format;

public class MoneyTransferService {
    private final AccountsRepository accountsRepository;

    private MoneyTransferService() {
        this(AccountsRepository.getInstance());
    }

    @VisibleForTesting
    MoneyTransferService(AccountsRepository accountsRepository) {
        this.accountsRepository = accountsRepository;
    }

    public void transfer(MoneyTransferRequest request) {
        Account accountFrom = getAccount(request.getAccountFromId());
        Account accountTo = getAccount(request.getAccountToId());
        accountFrom.transfer(accountTo, request.getAmount());
    }

    private Account getAccount(long accountId) {
        return accountsRepository
                .find(accountId)
                .orElseThrow(() ->
                        new IllegalArgumentException(format("Can't find an account with id %s", accountId)));
    }

    public static MoneyTransferService getInstance() {
        return AccountsRepositoryInstanceHolder.INSTANCE;
    }

    private static class AccountsRepositoryInstanceHolder {
        private static final MoneyTransferService INSTANCE = new MoneyTransferService();
        private AccountsRepositoryInstanceHolder() {}
    }
}
