
package com.conference.dao;

import com.conference.database.ConnectionFactory;
import com.conference.TestDatabaseInitializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReportDaoTest {

    @BeforeEach
    void setUp() throws Exception {

        ConnectionFactory.URL = "jdbc:sqlite:conference-test.db";
        TestDatabaseInitializer.initialize();
    }

    @AfterEach
    void tearDown() {

        ConnectionFactory.URL = "jdbc:sqlite:conference.db";
    }

    @Test
    void testConferenceOverview() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getConferenceOverview();

        assertEquals(4, rows.size());

        assertArrayEquals(
                new String[]{
                        "Java Programming Workshop",
                        "2",
                        "2",
                        "0"
                },
                rows.get(0)
        );

        assertArrayEquals(
                new String[]{
                        "Cybersecurity Class",
                        "2",
                        "0",
                        "2"
                },
                rows.get(2)
        );
    }


    @Test
    void testConferenceParticipants() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getConferenceParticipants(1);

        assertEquals(2, rows.size());

        assertEquals("Ilsa Rehmat", rows.get(0)[0]);
        assertEquals("Jesse Oliver", rows.get(1)[0]);
    }

    @Test
    void testWaitingListReport() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getWaitingListReport(1);

        assertEquals(2, rows.size());

        assertEquals("#1", rows.get(0)[0]);
        assertEquals("Manal Fayyaz", rows.get(0)[1]);

        assertEquals("#2", rows.get(1)[0]);
        assertEquals("Richard Qing", rows.get(1)[1]);
    }

    @Test
    void testParticipantRegistrations() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getParticipantRegistrations(2);

        assertEquals(2, rows.size());

        assertEquals("Java Programming Workshop", rows.get(0)[0]);
        assertEquals("CONFIRMED", rows.get(0)[1]);

        assertEquals("AI Conference", rows.get(1)[0]);
        assertEquals("CONFIRMED", rows.get(1)[1]);
    }

    @Test
    void testConferenceOccupancy() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getConferenceOccupancy();

        assertEquals(4, rows.size());

        assertEquals("100.0%", rows.get(0)[3]);
        assertEquals("33.3%", rows.get(1)[3]);
        assertEquals("0.0%", rows.get(2)[3]);
        assertEquals("0.0%", rows.get(3)[3]);
    }

    @Test
    void testUnregisteredConferences() throws SQLException {

        ReportDao dao = new ReportDao();

        List<String[]> rows = dao.getUnregisteredConferences();

        assertEquals(2, rows.size());

        assertEquals("Cybersecurity Class", rows.get(0)[0]);
        assertEquals("Software Design Patterns Workshop",
                rows.get(1)[0]);
    }


    @Test
    void testWaitingListReportAfterPromotion() throws SQLException {

        ReportDao dao = new ReportDao();

        com.conference.service.RegistrationService service =
                new com.conference.service.RegistrationService();

        assertTrue(service.cancelRegistration(1));

        List<String[]> rows = dao.getWaitingListReport(1);

        assertEquals(1, rows.size());
        assertEquals("#1", rows.get(0)[0]);
        assertEquals("Richard Qing", rows.get(0)[1]);
    }

    @Test
    void testConferenceParticipantsAfterPromotion() throws SQLException {

        ReportDao dao = new ReportDao();

        com.conference.service.RegistrationService service =
                new com.conference.service.RegistrationService();

        assertTrue(service.cancelRegistration(1));

        List<String[]> rows = dao.getConferenceParticipants(1);

        assertEquals(2, rows.size());
        assertEquals("Jesse Oliver", rows.get(0)[0]);
        assertEquals("Manal Fayyaz", rows.get(1)[0]);
    }

    @Test
    void testParticipantReportExcludesCancelledRegistration()
            throws SQLException {

        ReportDao dao = new ReportDao();

        com.conference.service.RegistrationService service =
                new com.conference.service.RegistrationService();

        assertTrue(service.cancelRegistration(1));

        List<String[]> rows = dao.getParticipantRegistrations(2);

        assertEquals(1, rows.size());
        assertEquals("AI Conference", rows.get(0)[0]);
        assertEquals("CONFIRMED", rows.get(0)[1]);
    }


}
