package com.conference.controller;

import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.service.RegistrationService;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ConferenceDetailController {

    @FXML
    private Label conferenceTitleLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label locationLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private Label capacityLabel;

    @FXML
    private Label seatsLabel;

    @FXML
    private Button registerButton;

    @FXML
    private Button joinWaitingListButton;

    @FXML
    private Button backButton;

    private final RegistrationDao registrationDao = new RegistrationDao();
    private final RegistrationService registrationService = new RegistrationService();

    private Conference conference;

    // called by BrowseConferenceController right after this screen loads
    public void setConference(Conference conference) {
        this.conference = conference;
        showConference();
    }

    private void showConference() {
        String description = conference.getDescription();
        if (description == null || description.isBlank()) {
            description = "No description";
        }

        String location = conference.getLocation();
        if (location == null || location.isBlank()) {
            location = "Not specified";
        }

        conferenceTitleLabel.setText(conference.getTitle());
        descriptionLabel.setText(description);
        locationLabel.setText(location);
        dateLabel.setText(conference.getStartTime().substring(0, 10));
        timeLabel.setText(conference.getStartTime().substring(11, 16)
                + " - " + conference.getEndTime().substring(11, 16));
        capacityLabel.setText(String.valueOf(conference.getCapacity()));

        int seatsLeft = 0;
        try {
            seatsLeft = conference.getCapacity() - countConfirmed();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load the available seats.").showAndWait();
        }

        if (seatsLeft > 0) {
            seatsLabel.setText(String.valueOf(seatsLeft));
        } else {
            seatsLabel.setText("FULL");
        }

        // seats open -> Register, full -> Join Waiting List
        registerButton.setVisible(seatsLeft > 0);
        joinWaitingListButton.setVisible(seatsLeft <= 0);
    }

    private int countConfirmed() throws SQLException {
        int confirmed = 0;

        for (Registration registration : registrationDao.findAll()) {
            if (registration.getConferenceId() == conference.getConferenceId()
                    && "CONFIRMED".equals(registration.getStatus())) {
                confirmed++;
            }
        }
        return confirmed;
    }

    @FXML
    private void onRegister() {
        int userId = WhosLoggedIn.getCurrentUser().getUserId();

        try {
            int registrationId = registrationService.registerParticipant(
                    userId, conference.getConferenceId());

            Registration registration = registrationDao.findById(registrationId);

            if ("CONFIRMED".equals(registration.getStatus())) {
                new Alert(Alert.AlertType.INFORMATION,
                        "You are registered for " + conference.getTitle() + ".").showAndWait();
            } else {
                new Alert(Alert.AlertType.INFORMATION,
                        "The conference is full. You were added to the waiting list.").showAndWait();
            }

            showConference();   // refresh the seats

        } catch (IllegalArgumentException e) {
            // "Already registered", "Scheduling conflict", ...
            new Alert(Alert.AlertType.WARNING, e.getMessage()).showAndWait();
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not complete the registration.").showAndWait();
        }
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/BrowseConference.FXML"))));
        stage.sizeToScene();
    }
}