
package com.conference.service;

import com.conference.dao.ReportDao;

import java.sql.SQLException;
import java.util.List;

public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    public List<String[]> getConferenceOverview()
            throws SQLException {

        return reportDao.getConferenceOverview();
    }

    public List<String[]> getConferenceParticipants(int conferenceId)
            throws SQLException {

        return reportDao.getConferenceParticipants(conferenceId);
    }

    public List<String[]> getWaitingListReport(int conferenceId)
            throws SQLException {

        return reportDao.getWaitingListReport(conferenceId);
    }

    public List<String[]> getParticipantRegistrations(int userId)
            throws SQLException {

        return reportDao.getParticipantRegistrations(userId);
    }

    public List<String[]> getConferenceOccupancy()
            throws SQLException {

        return reportDao.getConferenceOccupancy();
    }

    public List<String[]> getUnregisteredConferences()
            throws SQLException {

        return reportDao.getUnregisteredConferences();
    }
}
