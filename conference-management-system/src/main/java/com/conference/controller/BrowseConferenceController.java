package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

public class BrowseConferenceController implements EventHandler<ActionEvent> {

    @FXML
    private Button backButton;

    @FXML
    private ListView<GridPane> conferencesListView;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();

    @FXML
    public void initialize() {
        loadConferences();
    }

    private void loadConferences() {
        try {
            List<Conference> conferences = conferenceDao.findAll();

            // conference id -> number of confirmed registrations
            HashMap<Integer, Integer> confirmedCounts = new HashMap<>();

            for (Registration registration : registrationDao.findAll()) {
                if ("CONFIRMED".equals(registration.getStatus())) {
                    int id = registration.getConferenceId();
                    confirmedCounts.put(id, confirmedCounts.getOrDefault(id, 0) + 1);
                }
            }

            conferencesListView.getItems().clear();

            for (Conference conference : conferences) {
                int confirmed = confirmedCounts.getOrDefault(conference.getConferenceId(), 0);
                int seatsLeft = conference.getCapacity() - confirmed;
                conferencesListView.getItems().add(createRow(conference, seatsLeft));
            }

        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load conferences.").showAndWait();
        }
    }

    private GridPane createRow(Conference conference, int seatsLeft) {
        Label titleLabel = new Label(conference.getTitle());
        Label dateLabel = new Label(conference.getStartTime().substring(0, 10));

        Label seatsLabel = new Label();
        if (seatsLeft > 0) {
            seatsLabel.setText(String.valueOf(seatsLeft));
        } else {
            seatsLabel.setText("FULL");
        }

        Button viewButton = new Button("View");
        viewButton.setUserData(conference);   // remembers which conference this button is for
        viewButton.setOnAction(this);         // clicks go to the handle() method below

        GridPane row = new GridPane();
        row.getColumnConstraints().addAll(column(35), column(25), column(20), column(20));
        row.add(titleLabel, 0, 0);
        row.add(dateLabel, 1, 0);
        row.add(seatsLabel, 2, 0);
        row.add(viewButton, 3, 0);

        GridPane.setHalignment(titleLabel, HPos.CENTER);
        GridPane.setHalignment(dateLabel, HPos.CENTER);
        GridPane.setHalignment(seatsLabel, HPos.CENTER);
        GridPane.setHalignment(viewButton, HPos.CENTER);

        // makes each row as wide as the list (minus room for the scrollbar)
        row.prefWidthProperty().bind(conferencesListView.widthProperty().subtract(30));
        row.getStyleClass().add("row");

        return row;
    }

    private ColumnConstraints column(double percent) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(percent);
        return constraints;
    }

    // runs when any View button is clicked
    @Override
    public void handle(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Conference conference = (Conference) clickedButton.getUserData();

        try {
            openDetail(conference);
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Could not open the conference.").showAndWait();
        }
    }

    private void openDetail(Conference conference) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ConferenceDetail.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        ConferenceDetailController controller = loader.getController();
        controller.setConference(conference);

        stage.sizeToScene();
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/ParticipantDashBoard.FXML"))));
        stage.sizeToScene();
    }
}