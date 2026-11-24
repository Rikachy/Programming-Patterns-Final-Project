package com.conference.controller.admin;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.UserDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.model.User;
import com.conference.model.WaitingList;
import com.conference.service.RegistrationService;
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
import java.util.List;

public class ManageWaitingListsController {

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<Conference> conferenceComboBox;

    @FXML
    private ListView<WaitingList> waitingListView;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private final UserDao userDao = new UserDao();
    private final RegistrationService registrationService = new RegistrationService();

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
        waitingListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(WaitingList entry, boolean empty) {
                super.updateItem(entry, empty);

                if (empty || entry == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                String name = "Unknown";
                String email = "";
                try {
                    Registration registration = registrationDao.findById(entry.getRegistrationId());
                    if (registration != null) {
                        User participant = userDao.findById(registration.getUserId());
                        if (participant != null) {
                            name = participant.getFullName();
                            email = participant.getEmail();
                        }
                    }
                } catch (SQLException e) {
                    name = "Error";
                }

                Label positionLabel = new Label("#" + (getIndex() + 1));
                positionLabel.setPrefWidth(55);

                Label nameLabel = new Label(name);
                nameLabel.setPrefWidth(80);

                Label emailLabel = new Label(email);
                emailLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(emailLabel, Priority.ALWAYS);

                Label joinedAtLabel = new Label(entry.getJoinedAt());
                joinedAtLabel.setPrefWidth(95);

                HBox row = new HBox(8, positionLabel, nameLabel, emailLabel, joinedAtLabel);
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
            waitingListView.getItems().clear();
            return;
        }

        try {
            List<WaitingList> entries = registrationService.getWaitingList(selected.getConferenceId());
            waitingListView.setItems(FXCollections.observableArrayList(entries));
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load waiting list.").showAndWait();
        }
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/AdminDashboard.FXML"))));
        stage.sizeToScene();
    }
}