package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.dao.WaitingListDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import com.conference.service.RegistrationService;
import com.conference.util.WhosLoggedIn;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class WaitingListController implements EventHandler<ActionEvent> {

    @FXML
    private Button backButton;

    @FXML
    private ListView<GridPane> waitingListView;

    private final RegistrationDao registrationDao = new RegistrationDao();
    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final WaitingListDao waitingListDao = new WaitingListDao();
    private final RegistrationService registrationService = new RegistrationService();

    @FXML
    public void initialize() {
        waitingListView.setPlaceholder(new Label("You are not on any waiting list."));
        loadWaitingLists();
    }

    private void loadWaitingLists() {
        int userId = WhosLoggedIn.getCurrentUser().getUserId();
        waitingListView.getItems().clear();

        try {
            for (Registration registration : registrationDao.findAll()) {

                boolean isMine = registration.getUserId() == userId;
                boolean isWaiting = "WAITLISTED".equals(registration.getStatus());

                if (isMine && isWaiting) {
                    Conference conference = conferenceDao.findById(registration.getConferenceId());
                    int position = waitingListDao.getWaitingListPosition(
                            conference.getConferenceId(), registration.getRegistrationId());

                    waitingListView.getItems().add(createRow(registration, conference, position));
                }
            }
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not load your waiting lists.").showAndWait();
        }
    }

    private GridPane createRow(Registration registration, Conference conference, int position) {
        Label titleLabel = new Label(conference.getTitle());

        Label positionLabel = new Label("-");
        if (position > 0) {
            positionLabel.setText("#" + position);
        }

        Button leaveButton = new Button("Leave");
        leaveButton.setUserData(registration);   // remembers which registration this button is for
        leaveButton.setOnAction(this);           // clicks go to the handle() method below

        GridPane row = new GridPane();
        row.getColumnConstraints().addAll(column(33.33), column(33.33), column(33.33));
        row.add(titleLabel, 0, 0);
        row.add(positionLabel, 1, 0);
        row.add(leaveButton, 2, 0);

        GridPane.setHalignment(titleLabel, HPos.CENTER);
        GridPane.setHalignment(positionLabel, HPos.CENTER);
        GridPane.setHalignment(leaveButton, HPos.CENTER);

        // makes each row as wide as the list (minus room for the scrollbar)
        row.prefWidthProperty().bind(waitingListView.widthProperty().subtract(30));
        row.getStyleClass().add("row");

        return row;
    }

    private ColumnConstraints column(double percent) {
        ColumnConstraints constraints = new ColumnConstraints();
        constraints.setPercentWidth(percent);
        return constraints;
    }

    // runs when any Leave button is clicked
    @Override
    public void handle(ActionEvent event) {
        Button clickedButton = (Button) event.getSource();
        Registration registration = (Registration) clickedButton.getUserData();
        Conference conference = conferenceDao.findById(registration.getConferenceId());

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Leave the waiting list for " + conference.getTitle() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();

        if (confirm.getResult() != ButtonType.YES) {
            return;
        }

        try {
            registrationService.cancelRegistration(registration.getRegistrationId());
            new Alert(Alert.AlertType.INFORMATION, "You left the waiting list.").showAndWait();
            loadWaitingLists();   // refresh the list
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR, "Could not leave the waiting list.").showAndWait();
        }
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