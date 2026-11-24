package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.model.Conference;
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

public class ManageConferencesController {

    @FXML
    private Button backButton;

    @FXML
    private Button addConferenceButton;

    @FXML
    private ListView<Conference> conferencesListView;

    private final ConferenceDao conferenceDao = new ConferenceDao();

    @FXML
    public void initialize() {
        setupListView();
        loadConferences();
    }

    private void setupListView() {
        conferencesListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Conference conference, boolean empty) {
                super.updateItem(conference, empty);

                if (empty || conference == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label titleLabel = new Label(conference.getTitle());
                titleLabel.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(titleLabel, Priority.ALWAYS);

                Label dateLabel = new Label(conference.getStartTime());
                dateLabel.setPrefWidth(130);

                Label capacityLabel = new Label(String.valueOf(conference.getCapacity()));
                capacityLabel.setPrefWidth(70);

                Button editButton = new Button("Edit");
                editButton.setOnAction(e -> {
                    try {
                        onEditConference(conference);
                    } catch (IOException ex) {
                        new Alert(Alert.AlertType.ERROR, "Could not open edit screen.").showAndWait();
                    }
                });

                Button deleteButton = new Button("Delete");
                deleteButton.setOnAction(e -> {
                    try {
                        onDeleteConference(conference);
                    } catch (IOException ex) {
                        new Alert(Alert.AlertType.ERROR, "Could not open delete confirmation.").showAndWait();
                    }
                });

                HBox actionsBox = new HBox(8, editButton, deleteButton);
                actionsBox.setAlignment(Pos.CENTER_RIGHT);

                HBox row = new HBox(10, titleLabel, dateLabel, capacityLabel, actionsBox);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
    }

    private void loadConferences() {
        conferencesListView.setItems(FXCollections.observableArrayList(conferenceDao.findAll()));
    }

    @FXML
    private void onAddConference() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EditConference.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        EditConferenceController controller = loader.getController();
        controller.setConference(null);

        stage.sizeToScene();
    }

    private void onEditConference(Conference conference) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/EditConference.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        EditConferenceController controller = loader.getController();
        controller.setConference(conference);

        stage.sizeToScene();
    }

    private void onDeleteConference(Conference conference) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/FXML/ConfirmDeleteConference.FXML"));
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(loader.load()));

        ConfirmDeleteConferenceController controller = loader.getController();
        controller.setConference(conference);

        stage.sizeToScene();
    }

    @FXML
    private void onBack() throws IOException {
        openScreen("AdminDashboard.FXML");
    }

    private void openScreen(String fxmlFile) throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}