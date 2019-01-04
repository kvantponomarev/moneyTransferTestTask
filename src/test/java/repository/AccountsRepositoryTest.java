package repository;

import model.Account;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountsRepositoryTest {
    private static final long ACCOUNT_ID_OF_EXISTING_ACCOUNT = 1L;
    private static final long ACCOUNT_ID_OF_OTHER_ACCOUNT = 2L;
    private static final Account EXISTING_ACCOUNT = new Account(ACCOUNT_ID_OF_EXISTING_ACCOUNT, 10L);
    private static final Account OTHER_ACCOUNT = new Account(ACCOUNT_ID_OF_OTHER_ACCOUNT, 20L);
    private AccountsRepository accountsRepository = new AccountsRepository();

    @Before
    public void setUp() {
        accountsRepository = new AccountsRepository();
        accountsRepository.add(EXISTING_ACCOUNT);
    }

    @Test
    public void givenIdOfExistingAccount_whenFind_thenReturnsAccount() {
        //WHEN
        Optional<Account> result = accountsRepository.find(ACCOUNT_ID_OF_EXISTING_ACCOUNT);

        //THEN
        assertThat(result).isEqualTo(Optional.of(EXISTING_ACCOUNT));
    }

    @Test
    public void givenIdOfNotExistingAccount_whenFind_thenReturnsEmptyOptional() {
        //WHEN
        Optional<Account> result = accountsRepository.find(2L);

        //THEN
        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    public void givenAddAnAccount_whenAdd_thenAccountIsAdded() {
        //WHEN
        accountsRepository.add(OTHER_ACCOUNT);

        //THEN
        Optional<Account> result = accountsRepository.find(ACCOUNT_ID_OF_OTHER_ACCOUNT);
        assertThat(result).isEqualTo(Optional.of(OTHER_ACCOUNT));
    }
}