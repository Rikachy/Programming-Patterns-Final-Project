package com.conference.controller.admin;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.UserDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageParticipantsController {

    @FXML
    private Button backButton;

    @FXML
    private ListView<User> participantsListView;

    private final UserDao userDao = new UserDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private final ConferenceDao conferenceDao = new ConferenceDao();

    @FXML
    public void initialize() {
        setupListView();
        loadParticipants();
    }

    private void setupListView() {
        participantsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(User participant, boolean empty) {
                super.updateItem(participant, empty);

                if (empty || participant == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label nameLabel = new Label(participant.getFullName());
                nameLabel.setPrefWidth(150);

                Label emailLabel = new Label(participant.getEmail());
                emailLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(emailLabel, Priority.ALWAYS);

                Label ageLabel = new Label(String.valueOf(participant.getAge()));
                ageLabel.setPrefWidth(50);

                Button viewRegistrationsButton = new Button("View Registrations");
                viewRegistrationsButton.setOnAction(e -> onViewRegistrations(participant));

                HBox row = new HBox(10, nameLabel, emailLabel, ageLabel, viewRegistrationsButton);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
    }

    private void loadParticipants() {
        List<User> participants = new ArrayList<>();
        for (User user : userDao.findAll()) {
            if ("PARTICIPANT".equals(user.getRole())) {
                participants.add(user);
            }
        }
        participantsListView.setItems(FXCollections.observableArrayList(participants));
    }

    private void onViewRegistrations(User participant) {
        try {
            StringBuilder details = new StringBuilder();

            for (Registration registration : registrationDao.findAll()) {
                if (registration.getUserId() == participant.getUserId()
                        && ("CONFIRMED".equals(registration.getStatus())
                        || "WAITLISTED".equals(registration.getStatus()))) {

                    Conference conference = conferenceDao.findById(registration.getConferenceId());
                    String title = (conference != null)
                            ? conference.getTitle()
                            : "Conference #" + registration.getConferenceId();

                    details.append("- ")
                            .append(title)
                            .append(" [")
                            .append(registration.getStatus())
                            .append("]\n");
                }
            }

            if (details.isEmpty()) {
                new Alert(Alert.AlertType.INFORMATION,
                        participant.getFullName() + " has no confirmed or wait-listed registrations.").showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Participant Registrations");
            alert.setHeaderText("Registrations for " + participant.getFullName());
            alert.setContentText(details.toString());
            alert.showAndWait();

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load registrations for this participant.").showAndWait();
        }
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/AdminDashboard.FXML"))));
        stage.sizeToScene();
    }
}