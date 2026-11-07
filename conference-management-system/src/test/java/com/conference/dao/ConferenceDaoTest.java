package com.conference.dao;

import com.conference.exception.DaoException;
import com.conference.model.Conference;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConferenceDaoTest {

    @Test
    void testConferenceModel() {

        Conference conference = new Conference(
                1, "Java Day", "Intro to Java", "Room 101",
                "2026-12-01 09:00:00", "2026-12-01 17:00:00",
                50, "2026-11-03 10:00:00"
        );

        assertEquals(1, conference.getConferenceId());
        assertEquals("Java Day", conference.getTitle());
        assertEquals("Room 101", conference.getLocation());
        assertEquals(50, conference.getCapacity());
    }

    @Test
    void testCreateAndFindById() {

        ConferenceDao dao = new ConferenceDao();

        Conference conference = new Conference(
                0, "Test Conference", "Test description", "Room 101",
                "2026-12-01 09:00:00", "2026-12-01 17:00:00", 50, null
        );

        int newId = dao.createConference(conference);
        Conference saved = dao.findById(newId);

        assertNotNull(saved);
        assertEquals("Test Conference", saved.getTitle());
        assertEquals("Room 101", saved.getLocation());
        assertEquals("2026-12-01 09:00:00", saved.getStartTime());
        assertEquals(50, saved.getCapacity());

        // clean up
        dao.deleteConference(newId);
    }

    @Test
    void testCreateWithoutLocation() {

        ConferenceDao dao = new ConferenceDao();

        // description and location are optional, so null is allowed
        Conference conference = new Conference(
                0, "No Location Conference", null, null,
                "2026-12-02 09:00:00", "2026-12-02 17:00:00", 20, null
        );

        int newId = dao.createConference(conference);
        Conference saved = dao.findById(newId);

        assertNotNull(saved);
        assertNull(saved.getLocation());

        dao.deleteConference(newId);
    }

    @Test
    void testFindByIdNotFound() {

        ConferenceDao dao = new ConferenceDao();

        Conference found = dao.findById(999999);

        assertNull(found);
    }

    @Test
    void testFindAllContainsNewConference() {

        ConferenceDao dao = new ConferenceDao();

        int newId = dao.createConference(new Conference(
                0, "List Test", null, null,
                "2026-12-03 09:00:00", "2026-12-03 17:00:00", 10, null
        ));

        List<Conference> conferences = dao.findAll();

        // stream + lambda: is the new conference in the list?
        boolean found = conferences.stream()
                .anyMatch(c -> c.getConferenceId() == newId);

        assertTrue(found);

        dao.deleteConference(newId);
    }

    @Test
    void testUpdateConference() {

        ConferenceDao dao = new ConferenceDao();

        int newId = dao.createConference(new Conference(
                0, "Old Title", null, null,
                "2026-12-04 09:00:00", "2026-12-04 17:00:00", 10, null
        ));

        Conference conference = dao.findById(newId);
        conference.setTitle("New Title");
        conference.setCapacity(100);

        int rows = dao.updateConference(conference);

        assertEquals(1, rows);
        assertEquals("New Title", dao.findById(newId).getTitle());
        assertEquals(100, dao.findById(newId).getCapacity());

        dao.deleteConference(newId);
    }

    @Test
    void testDeleteConference() {

        ConferenceDao dao = new ConferenceDao();

        int newId = dao.createConference(new Conference(
                0, "Delete Me", null, null,
                "2026-12-05 09:00:00", "2026-12-05 17:00:00", 10, null
        ));

        int rows = dao.deleteConference(newId);

        assertEquals(1, rows);
        assertNull(dao.findById(newId));
    }

    @Test
    void testInvalidCapacityRejected() {

        ConferenceDao dao = new ConferenceDao();

        // capacity 0 breaks the rule "capacity must be positive"
        Conference bad = new Conference(
                0, "Bad Capacity", null, null,
                "2026-12-06 09:00:00", "2026-12-06 17:00:00", 0, null
        );

        assertThrows(DaoException.class, () -> dao.createConference(bad));
    }

    @Test
    void testEndBeforeStartRejected() {

        ConferenceDao dao = new ConferenceDao();

        // end time is earlier than start time
        Conference bad = new Conference(
                0, "Bad Dates", null, null,
                "2026-12-07 17:00:00", "2026-12-07 09:00:00", 10, null
        );

        assertThrows(DaoException.class, () -> dao.createConference(bad));
    }
}