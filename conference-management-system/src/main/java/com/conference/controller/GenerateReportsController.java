package com.conference.controller.admin;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.UserDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.model.User;
import com.conference.model.WaitingList;
import com.conference.service.RegistrationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GenerateReportsController {

    @FXML
    private Button backButton;

    @FXML
    private Button conferenceOverviewButton;

    @FXML
    private Button conferenceParticipantsButton;

    @FXML
    private Button waitingListButton;

    @FXML
    private Button participantRegistrationsButton;

    @FXML
    private Button conferenceOccupancyButton;

    @FXML
    private Button unregisteredConferencesButton;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private final UserDao userDao = new UserDao();
    private final RegistrationService registrationService = new RegistrationService();

    @FXML
    private void onConferenceOverview() throws IOException {
        try {
            List<Conference> conferences = conferenceDao.findAll();
            List<Registration> registrations = registrationDao.findAll();
            List<String[]> rows = new ArrayList<>();

            for (Conference conference : conferences) {
                int confirmed = 0;
                for (Registration reg : registrations) {
                    if (reg.getConferenceId() == conference.getConferenceId()
                            && "CONFIRMED".equals(reg.getStatus())) {
                        confirmed++;
                    }
                }
                int available = conference.getCapacity() - confirmed;
                rows.add(new String[]{
                        conference.getTitle(),
                        String.valueOf(conference.getCapacity()),
                        String.valueOf(confirmed),
                        String.valueOf(available)
                });
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No conferences found.", "", "", ""});
            }

            openReportResult("Conference Overview",
                    new String[]{"Conference", "Capacity", "Confirmed", "Available"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Conference Overview report.").showAndWait();
        }
    }

    @FXML
    private void onConferenceParticipants() throws IOException {
        Conference selected = promptForConference("Conference Participants");
        if (selected == null) {
            return;
        }

        try {
            List<String[]> rows = new ArrayList<>();
            for (Registration reg : registrationDao.findAll()) {
                if (reg.getConferenceId() == selected.getConferenceId()
                        && "CONFIRMED".equals(reg.getStatus())) {
                    User participant = userDao.findById(reg.getUserId());
                    if (participant != null) {
                        rows.add(new String[]{
                                participant.getFullName(),
                                participant.getEmail(),
                                String.valueOf(participant.getAge()),
                                reg.getRegisteredAt()
                        });
                    }
                }
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No confirmed participants.", "", "", ""});
            }

            openReportResult("Confirmed Participants - " + selected.getTitle(),
                    new String[]{"Participant", "Email", "Age", "Registered At"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Conference Participants report.").showAndWait();
        }
    }

    @FXML
    private void onWaitingList() throws IOException {
        Conference selected = promptForConference("Waiting List Report");
        if (selected == null) {
            return;
        }

        try {
            List<WaitingList> entries = registrationService.getWaitingList(selected.getConferenceId());
            List<String[]> rows = new ArrayList<>();

            int position = 1;
            for (WaitingList entry : entries) {
                Registration reg = registrationDao.findById(entry.getRegistrationId());
                if (reg != null) {
                    User participant = userDao.findById(reg.getUserId());
                    String name = participant != null ? participant.getFullName() : "User #" + reg.getUserId();
                    String email = participant != null ? participant.getEmail() : "";
                    rows.add(new String[]{
                            "#" + position,
                            name,
                            email,
                            entry.getJoinedAt()
                    });
                    position++;
                }
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No participants on the waiting list.", "", "", ""});
            }

            openReportResult("Waiting List - " + selected.getTitle(),
                    new String[]{"Position", "Participant", "Email", "Joined At"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Waiting List report.").showAndWait();
        }
    }

    @FXML
    private void onParticipantRegistrations() throws IOException {
        User selected = promptForParticipant("Participant Registrations");
        if (selected == null) {
            return;
        }

        try {
            List<String[]> rows = new ArrayList<>();
            for (Registration reg : registrationDao.findAll()) {
                if (reg.getUserId() == selected.getUserId()
                        && ("CONFIRMED".equals(reg.getStatus())
                        || "WAITLISTED".equals(reg.getStatus()))) {
                    Conference conference = conferenceDao.findById(reg.getConferenceId());
                    String title = conference != null ? conference.getTitle() : "Conference #" + reg.getConferenceId();
                    String startTime = conference != null ? conference.getStartTime() : "";
                    rows.add(new String[]{
                            title,
                            reg.getStatus(),
                            startTime,
                            reg.getRegisteredAt()
                    });
                }
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No active registrations found.", "", "", ""});
            }

            openReportResult("Registrations - " + selected.getFullName(),
                    new String[]{"Conference", "Status", "Start Time", "Registered At"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Participant Registrations report.").showAndWait();
        }
    }

    @FXML
    private void onConferenceOccupancy() throws IOException {
        try {
            List<Conference> conferences = conferenceDao.findAll();
            List<Registration> registrations = registrationDao.findAll();
            List<String[]> rows = new ArrayList<>();

            for (Conference conference : conferences) {
                int confirmed = 0;
                for (Registration reg : registrations) {
                    if (reg.getConferenceId() == conference.getConferenceId()
                            && "CONFIRMED".equals(reg.getStatus())) {
                        confirmed++;
                    }
                }
                int capacity = conference.getCapacity();
                double percentage = capacity > 0 ? (confirmed * 100.0) / capacity : 0.0;
                rows.add(new String[]{
                        conference.getTitle(),
                        String.valueOf(capacity),
                        String.valueOf(confirmed),
                        String.format("%.1f%%", percentage)
                });
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No conferences found.", "", "", ""});
            }

            openReportResult("Conference Occupancy",
                    new String[]{"Conference", "Capacity", "Occupied", "Occupancy"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Conference Occupancy report.").showAndWait();
        }
    }

    @FXML
    private void onUnregisteredConferences() throws IOException {
        try {
            List<Conference> conferences = conferenceDao.findAll();
            List<Registration> registrations = registrationDao.findAll();
            List<String[]> rows = new ArrayList<>();

            for (Conference conference : conferences) {
                boolean hasActive = false;
                for (Registration reg : registrations) {
                    if (reg.getConferenceId() == conference.getConferenceId()
                            && ("CONFIRMED".equals(reg.getStatus())
                            || "WAITLISTED".equals(reg.getStatus()))) {
                        hasActive = true;
                        break;
                    }
                }
                if (!hasActive) {
                    String location = conference.getLocation() != null ? conference.getLocation() : "N/A";
                    rows.add(new String[]{
                            conference.getTitle(),
                            location,
                            conference.getStartTime(),
                            String.valueOf(conference.getCapacity())
                    });
                }
            }

            if (rows.isEmpty()) {
                rows.add(new String[]{"No unregistered conferences.", "", "", ""});
            }

            openReportResult("Unregistered Conferences",
                    new String[]{"Conference", "Location", "Start Time", "Capacity"}, rows);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not generate Unregistered Conferences report.").showAndWait();
        }
    }

    private Conference promptForConference(String header) {
        List<Conference> conferences = conferenceDao.findAll();
        if (conferences.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No conferences available.").showAndWait();
            return null;
        }

        Map<String, Conference> options = new LinkedHashMap<>();
        for (Conference conference : conferences) {
            options.put(conference.getConferenceId() + " - " + conference.getTitle(), conference);
        }

        List<String> labels = new ArrayList<>(options.keySet());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.get(0), labels);
        dialog.setTitle("Select Conference");
        dialog.setHeaderText(header);
        dialog.setContentText("Conference:");

        Optional<String> result = dialog.showAndWait();
        return result.map(options::get).orElse(null);
    }

    private User promptForParticipant(String header) {
        List<User> users = userDao.findAll();
        Map<String, User> options = new LinkedHashMap<>();
        for (User user : users) {
            if ("PARTICIPANT".equals(user.getRole())) {
                options.put(user.getUserId() + " - " + user.getFullName() + " (" + user.getEmail() + ")", user);
            }
        }

        if (options.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "No participants available.").showAndWait();
            return null;
        }

        List<String> labels = new ArrayList<>(options.keySet());
        ChoiceDialog<String> dialog = new ChoiceDialog<>(labels.get(0), labels);
        dialog.setTitle("Select Participant");
        dialog.setHeaderText(header);
        dialog.setContentText("Participant:");

        Optional<String> result = dialog.showAndWait();
        return result.map(options::get).orElse(null);
    }

    private void openReportResult(String reportTitle, String[] headers, List<String[]> rows) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ReportResult.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        com.conference.controller.admin.ReportResultController controller = loader.getController();
        controller.setReportData(reportTitle, headers, rows);

        stage.sizeToScene();
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/AdminDashboard.FXML"))));
        stage.sizeToScene();
    }
}