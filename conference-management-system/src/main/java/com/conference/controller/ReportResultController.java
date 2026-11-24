package com.conference.controller.admin;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class ReportResultController {

    @FXML
    private Label reportTitleLabel;

    @FXML
    private Label column1Label;

    @FXML
    private Label column2Label;

    @FXML
    private Label column3Label;

    @FXML
    private Label column4Label;

    @FXML
    private ListView<String[]> reportListView;

    @FXML
    private Button topBackButton;

    @FXML
    private Button bottomBackButton;

    @FXML
    public void initialize() {
        setupListView();
    }

    private void setupListView() {
        reportListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String[] rowValues, boolean empty) {
                super.updateItem(rowValues, empty);

                if (empty || rowValues == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                Label col1 = new Label(rowValues.length > 0 ? rowValues[0] : "");
                col1.setMaxWidth(Double.MAX_VALUE);
                HBox.setHgrow(col1, Priority.ALWAYS);

                Label col2 = new Label(rowValues.length > 1 ? rowValues[1] : "");
                col2.setPrefWidth(115);

                Label col3 = new Label(rowValues.length > 2 ? rowValues[2] : "");
                col3.setPrefWidth(115);

                Label col4 = new Label(rowValues.length > 3 ? rowValues[3] : "");
                col4.setPrefWidth(115);

                HBox row = new HBox(10, col1, col2, col3, col4);
                row.setAlignment(Pos.CENTER_LEFT);
                setGraphic(row);
            }
        });
    }

    public void setReportData(String title, String[] headers, List<String[]> rows) {
        reportTitleLabel.setText(title);
        column1Label.setText(headers.length > 0 ? headers[0] : "");
        column2Label.setText(headers.length > 1 ? headers[1] : "");
        column3Label.setText(headers.length > 2 ? headers[2] : "");
        column4Label.setText(headers.length > 3 ? headers[3] : "");
        reportListView.setItems(FXCollections.observableArrayList(rows));
    }

    @FXML
    private void onBackToReports() throws IOException {
        Stage stage = (Stage) topBackButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/GenerateReports.FXML"))));
        stage.sizeToScene();
    }
}