package com.conference.dao;

import com.conference.exception.DaoException;
import com.conference.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserDaoTest {

    @Test
    void testUserModel() {

        User user = new User(
                1, "Test User", "test@mail.com", 25,
                "PARTICIPANT", "2026-11-03 10:00:00"
        );

        assertEquals(1, user.getUserId());
        assertEquals("Test User", user.getFullName());
        assertEquals("test@mail.com", user.getEmail());
        assertEquals(25, user.getAge());
        assertEquals("PARTICIPANT", user.getRole());
    }

    @Test
    void testCreateAndFindById() {

        UserDao dao = new UserDao();

        // unique email so the test can run many times
        String email = "a" + System.nanoTime() + "@mail.com";
        User user = new User(0, "Test Person", email, 25, "PARTICIPANT", null);

        int newId = dao.createUser(user);
        User saved = dao.findById(newId);

        assertNotNull(saved);
        assertEquals("Test Person", saved.getFullName());
        assertEquals(email, saved.getEmail());
        assertEquals(25, saved.getAge());

        // clean up
        dao.deleteUser(newId);
    }

    @Test
    void testFindByEmail() {

        UserDao dao = new UserDao();

        String email = "b" + System.nanoTime() + "@mail.com";
        int newId = dao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));

        User found = dao.findByEmail(email);

        assertNotNull(found);
        assertEquals(newId, found.getUserId());

        dao.deleteUser(newId);
    }

    @Test
    void testFindByEmailNotFound() {

        UserDao dao = new UserDao();

        User found = dao.findByEmail("nobody@nowhere.com");

        assertNull(found);
    }

    @Test
    void testDuplicateEmail() {

        UserDao dao = new UserDao();

        String email = "c" + System.nanoTime() + "@mail.com";
        User user = new User(0, "Test Person", email, 25, "PARTICIPANT", null);

        int newId = dao.createUser(user);

        // same email again must fail
        assertThrows(DaoException.class, () -> dao.createUser(user));

        dao.deleteUser(newId);
    }

    @Test
    void testUpdateUser() {

        UserDao dao = new UserDao();

        String email = "d" + System.nanoTime() + "@mail.com";
        int newId = dao.createUser(
                new User(0, "Old Name", email, 25, "PARTICIPANT", null));

        User user = dao.findById(newId);
        user.setFullName("New Name");

        int rows = dao.updateUser(user);

        assertEquals(1, rows);
        assertEquals("New Name", dao.findById(newId).getFullName());

        dao.deleteUser(newId);
    }

    @Test
    void testDeleteUser() {

        UserDao dao = new UserDao();

        String email = "e" + System.nanoTime() + "@mail.com";
        int newId = dao.createUser(
                new User(0, "Test Person", email, 25, "PARTICIPANT", null));

        int rows = dao.deleteUser(newId);

        assertEquals(1, rows);
        assertNull(dao.findById(newId));
    }
}