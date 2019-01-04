package repository;

import model.Account;
import org.assertj.core.util.VisibleForTesting;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AccountsRepository {
    private final Map<Long, Account> accounts;

    @VisibleForTesting
    AccountsRepository() {
        this.accounts = new ConcurrentHashMap<>();
    }

    public void add(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Optional<Account> find(Long accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }

    public static AccountsRepository getInstance() {
        return AccountsRepositoryInstanceHolder.INSTANCE;
    }

    private static class AccountsRepositoryInstanceHolder {
        private static final AccountsRepository INSTANCE = new AccountsRepository();
        private AccountsRepositoryInstanceHolder() {}
    }
}
