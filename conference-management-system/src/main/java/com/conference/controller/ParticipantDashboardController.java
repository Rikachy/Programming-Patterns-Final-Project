package com.conference.controller;

import com.conference.model.User;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class ParticipantDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    public void initialize() {
        User currentUser = WhosLoggedIn.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }
    }

    @FXML
    private void onMyProfile() throws IOException {
        openScreen("MyProfile.FXML");
    }

    @FXML
    private void onBrowseConferences() throws IOException {
        openScreen("BrowseConference.FXML");
    }

    @FXML
    private void onMyRegistrations() throws IOException {
        openScreen("MyRegistration.FXML");
    }

    @FXML
    private void onMyWaitingLists() throws IOException {
        openScreen("WaitingList.FXML");
    }

    @FXML
    private void onLogout() throws IOException {
        WhosLoggedIn.setCurrentUser(null);
        openScreen("LoginIN.FXML");
    }

    private void openScreen(String fxmlFile) throws IOException {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}