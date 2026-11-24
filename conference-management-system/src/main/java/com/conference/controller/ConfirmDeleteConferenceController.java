package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ConfirmDeleteConferenceController {

    @FXML
    private Label conferenceNameLabel;

    @FXML
    private Button confirmDeleteButton;

    @FXML
    private Button keepButton;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private Conference currentConference;

    public void setConference(Conference conference) {
        this.currentConference = conference;
        if (conference != null) {
            conferenceNameLabel.setText(conference.getTitle());
        }
    }

    @FXML
    private void onConfirmDelete() throws IOException {
        if (currentConference == null) {
            new Alert(Alert.AlertType.WARNING, "No conference selected to delete.").showAndWait();
            onKeep();
            return;
        }

        //checking if conference has confirmed participants or waiting-list entries
        try {
            for (Registration registration : registrationDao.findAll()) {
                if (registration.getConferenceId() == currentConference.getConferenceId()
                        && ("CONFIRMED".equals(registration.getStatus())
                        || "WAITLISTED".equals(registration.getStatus()))) {
                    new Alert(Alert.AlertType.ERROR,
                            "Cannot delete a conference that has confirmed participants or waiting-list entries.").showAndWait();
                    return;
                }
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not check conference registrations.").showAndWait();
            return;
        }

        conferenceDao.deleteConference(currentConference.getConferenceId());
        new Alert(Alert.AlertType.INFORMATION, "Conference deleted.").showAndWait();
        onKeep();
    }

    @FXML
    private void onKeep() throws IOException {
        Stage stage = (Stage) keepButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/ManageConferences.FXML"))));
        stage.sizeToScene();
    }
}