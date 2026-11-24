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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ViewRegistrationsController {

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<Conference> conferenceComboBox;

    @FXML
    private ListView<Registration> registrationsListView;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private final UserDao userDao = new UserDao();

    @FXML
    public void initialize() {
        setupComboBox();
        setupListView();
        loadConferences();
    }

    private void setupComboBox() {
        conferenceComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Conference conference) {
                return conference == null ? "" : conference.getTitle();
            }

            @Override
            public Conference fromString(String string) {
                return null;
            }
        });
    }

    private void setupListView() {
        registrationsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Registration registration, boolean empty) {
                super.updateItem(registration, empty);

                if (empty || registration == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                User participant = userDao.findById(registration.getUserId());
                String name = participant != null ? participant.getFullName() : "User #" + registration.getUserId();
                String email = participant != null ? participant.getEmail() : "";

                Label participantLabel = new Label(name);
                participantLabel.setPrefWidth(105);

                Label emailLabel = new Label(email);
                emailLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(emailLabel, Priority.ALWAYS);

                Label registeredAtLabel = new Label(registration.getRegisteredAt());
                registeredAtLabel.setPrefWidth(110);

                HBox row = new HBox(10, participantLabel, emailLabel, registeredAtLabel);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
    }

    private void loadConferences() {
        List<Conference> conferences = conferenceDao.findAll();
        conferenceComboBox.setItems(FXCollections.observableArrayList(conferences));

        if (!conferences.isEmpty()) {
            conferenceComboBox.getSelectionModel().selectFirst();
            onSelectConference();
        }
    }

    @FXML
    private void onSelectConference() {
        Conference selected = conferenceComboBox.getValue();
        if (selected == null) {
            registrationsListView.getItems().clear();
            return;
        }

        try {
            List<Registration> confirmed = new ArrayList<>();
            for (Registration registration : registrationDao.findAll()) {
                if (registration.getConferenceId() == selected.getConferenceId()
                        && "CONFIRMED".equals(registration.getStatus())) {
                    confirmed.add(registration);
                }
            }
            registrationsListView.setItems(FXCollections.observableArrayList(confirmed));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load confirmed registrations.").showAndWait();
        }
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/AdminDashboard.FXML"))));
        stage.sizeToScene();
    }
}