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
        openScreen("MyProfile.fxml");
    }

    @FXML
    private void onBrowseConferences() throws IOException {
        openScreen("BrowseConferences.fxml");
    }

    @FXML
    private void onMyRegistrations() throws IOException {
        openScreen("MyRegistrations.fxml");
    }

    @FXML
    private void onMyWaitingLists() throws IOException {
        openScreen("MyWaitingLists.fxml");
    }

    @FXML
    private void onLogout() throws IOException {
        WhosLoggedIn.setCurrentUser(null);
        openScreen("LoginIN.fxml");
    }

    private void openScreen(String fxmlFile) throws IOException {
        Stage stage = (Stage) welcomeLabel.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}