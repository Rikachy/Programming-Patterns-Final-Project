package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyRegistrationController {

    @FXML
    private Button backButton;

    @FXML
    private ListView<String> registrationsListView;

    private final RegistrationDao registrationDao = new RegistrationDao();
    private final ConferenceDao conferenceDao = new ConferenceDao();

    // the registrations shown in the list, in the same order as the list rows
    private final List<Registration> shownRegistrations = new ArrayList<>();

    @FXML
    public void initialize() {
        registrationsListView.setPlaceholder(new Label("You have no registrations yet."));
        loadRegistrations();
    }

    private void loadRegistrations() {
        int userId = WhosLoggedIn.getCurrentUser().getUserId();

        shownRegistrations.clear();
        registrationsListView.getItems().clear();

        try {
            for (Registration registration : registrationDao.findAll()) {

                boolean isMine = registration.getUserId() == userId;
                boolean isActive = "CONFIRMED".equals(registration.getStatus())
                        || "WAITLISTED".equals(registration.getStatus());

                if (isMine && isActive) {
                    Conference conference = conferenceDao.findById(registration.getConferenceId());

                    String text = conference.getTitle()
                            + "  -  " + registration.getStatus()
                            + "  -  " + conference.getStartTime().substring(0, 10);

                    shownRegistrations.add(registration);
                    registrationsListView.getItems().add(text);
                }
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load your registrations.").showAndWait();
        }
    }

    @FXML
    private void onCancelSelected() throws IOException {
        int index = registrationsListView.getSelectionModel().getSelectedIndex();

        if (index < 0) {
            new Alert(Alert.AlertType.WARNING, "Please select a registration first.").showAndWait();
            return;
        }

        Registration registration = shownRegistrations.get(index);
        Conference conference = conferenceDao.findById(registration.getConferenceId());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ConfirmCancelRegistration.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        ConfirmCancelRegistrationController controller = loader.getController();
        controller.setRegistration(registration, conference);

        stage.sizeToScene();
    }

    @FXML
    private void onBrowse() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/BrowseConference.FXML"))));
        stage.sizeToScene();
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/ParticipantDashBoard.FXML"))));
        stage.sizeToScene();
    }
}