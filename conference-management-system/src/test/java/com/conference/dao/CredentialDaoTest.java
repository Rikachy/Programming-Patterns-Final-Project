package com.conference.dao;

import com.conference.exception.DaoException;
import com.conference.model.Credential;
import com.conference.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.junit.jupiter.api.Assertions.*;

public class CredentialDaoTest {
    @BeforeAll
    static void setUp() {
        TestDatabase.create();
    }

    @Test
    void testCredentialModel() {

        Credential credential = new Credential(1, "some_hash");

        assertEquals(1, credential.getUserId());
        assertEquals("some_hash", credential.getPasswordHash());
    }

    @Test
    void testCreateAndFindByUserId() {

        UserDao userDao = new UserDao();
        CredentialDao dao = new CredentialDao();

        // a credential needs a user first (foreign key)
        String email = "f" + System.nanoTime() + "@mail.com";
        int userId = userDao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));

        int rows = dao.createCredential(new Credential(userId, "hash_abc"));
        Credential saved = dao.findByUserId(userId);

        assertEquals(1, rows);
        assertNotNull(saved);
        assertEquals(userId, saved.getUserId());
        assertEquals("hash_abc", saved.getPasswordHash());

        userDao.deleteUser(userId);
    }

    @Test
    void testFindByUserIdNotFound() {

        CredentialDao dao = new CredentialDao();

        Credential found = dao.findByUserId(999999);

        assertNull(found);
    }

    @Test
    void testCreateWithoutUserRejected() {

        CredentialDao dao = new CredentialDao();

        Credential bad = new Credential(999999, "hash_abc");

        assertThrows(DaoException.class, () -> dao.createCredential(bad));
    }

    @Test
    void testUpdatePasswordHash() {

        UserDao userDao = new UserDao();
        CredentialDao dao = new CredentialDao();

        String email = "g" + System.nanoTime() + "@mail.com";
        int userId = userDao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));
        dao.createCredential(new Credential(userId, "old_hash"));

        int rows = dao.updatePasswordHash(userId, "new_hash");

        assertEquals(1, rows);
        assertEquals("new_hash", dao.findByUserId(userId).getPasswordHash());

        userDao.deleteUser(userId);
    }

    @Test
    void testDeleteCredential() {

        UserDao userDao = new UserDao();
        CredentialDao dao = new CredentialDao();

        String email = "h" + System.nanoTime() + "@mail.com";
        int userId = userDao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));
        dao.createCredential(new Credential(userId, "hash_abc"));

        int rows = dao.deleteCredential(userId);

        assertEquals(1, rows);
        assertNull(dao.findByUserId(userId));

        userDao.deleteUser(userId);
    }

    @Test
    void testDeletingUserDeletesCredential() {

        UserDao userDao = new UserDao();
        CredentialDao dao = new CredentialDao();

        String email = "i" + System.nanoTime() + "@mail.com";
        int userId = userDao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));
        dao.createCredential(new Credential(userId, "hash_abc"));

        userDao.deleteUser(userId);

        assertNull(dao.findByUserId(userId));
    }
}