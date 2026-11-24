package com.conference.controller.admin;

import com.conference.model.User;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button logoutButton;

    @FXML
    private Button manageConferencesButton;

    @FXML
    private Button manageParticipantsButton;

    @FXML
    private Button viewRegistrationsButton;

    @FXML
    private Button manageWaitingListsButton;

    @FXML
    private Button generateReportsButton;

    @FXML
    public void initialize() {
        User currentUser = WhosLoggedIn.getCurrentUser();
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFullName());
        }
    }

    @FXML
    private void onManageConferences() throws IOException {
        openScreen("ManageConferences.FXML");
    }

    @FXML
    private void onManageParticipants() throws IOException {
        openScreen("ManageParticipants.FXML");
    }

    @FXML
    private void onViewRegistrations() throws IOException {
        openScreen("ViewRegistrations.FXML");
    }

    @FXML
    private void onManageWaitingLists() throws IOException {
        openScreen("ManageWaitingLists.FXML");
    }

    @FXML
    private void onGenerateReports() throws IOException {
        openScreen("GenerateReports.FXML");
    }

    @FXML
    private void onLogout() throws IOException {
        WhosLoggedIn.setCurrentUser(null);
        openScreen("LoginIN.FXML");
    }

    private void openScreen(String fxmlFile) throws IOException {
        Stage stage = (Stage) logoutButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}