package com.conference.controller;

import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.service.RegistrationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class ConfirmCancelRegistrationController {

    @FXML
    private Label conferenceNameLabel;

    @FXML
    private Button keepButton;

    private final RegistrationService registrationService = new RegistrationService();

    private Registration registration;

    // called by MyRegistrationController right after this screen loads
    public void setRegistration(Registration registration, Conference conference) {
        this.registration = registration;
        conferenceNameLabel.setText(conference.getTitle());
    }

    @FXML
    private void onConfirmCancel() throws IOException {
        try {
            boolean cancelled = registrationService.cancelRegistration(registration.getRegistrationId());

            if (cancelled) {
                new Alert(Alert.AlertType.INFORMATION, "Registration cancelled.").showAndWait();
            } else {
                new Alert(Alert.AlertType.WARNING, "This registration was already cancelled.").showAndWait();
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not cancel the registration.").showAndWait();
            return;
        }

        onKeep();
    }

    @FXML
    private void onKeep() throws IOException {
        Stage stage = (Stage) keepButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/MyRegistration.FXML"))));
        stage.sizeToScene();
    }
}