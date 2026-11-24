package com.conference.controller;

import com.conference.dao.ConferenceDao;
import com.conference.dao.RegistrationDao;
import com.conference.model.Conference;
import com.conference.model.Registration;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EditConferenceController {

    @FXML
    private Label screenTitleLabel;

    @FXML
    private TextField titleTextField;

    @FXML
    private TextArea descriptionTextArea;

    @FXML
    private TextField locationTextField;

    @FXML
    private DatePicker startDatePicker;

    @FXML
    private TextField startTimeTextField;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private TextField endTimeTextField;

    @FXML
    private TextField capacityTextField;

    @FXML
    private Button cancelButton;

    @FXML
    private Button saveButton;

    private final ConferenceDao conferenceDao = new ConferenceDao();
    private final RegistrationDao registrationDao = new RegistrationDao();
    private Conference currentConference;

    private static final DateTimeFormatter DB_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public void setConference(Conference conference) {
        this.currentConference = conference;

        if (conference == null) {
            screenTitleLabel.setText("Add Conference");
            return;
        }

        screenTitleLabel.setText("Edit Conference");
        titleTextField.setText(conference.getTitle());
        descriptionTextArea.setText(conference.getDescription());
        locationTextField.setText(conference.getLocation() != null ? conference.getLocation() : "");
        capacityTextField.setText(String.valueOf(conference.getCapacity()));

        if (conference.getStartTime() != null && !conference.getStartTime().isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(conference.getStartTime(), DB_FORMATTER);
            startDatePicker.setValue(start.toLocalDate());
            startTimeTextField.setText(start.toLocalTime().format(TIME_FORMATTER));
        }

        if (conference.getEndTime() != null && !conference.getEndTime().isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(conference.getEndTime(), DB_FORMATTER);
            endDatePicker.setValue(end.toLocalDate());
            endTimeTextField.setText(end.toLocalTime().format(TIME_FORMATTER));
        }
    }

    @FXML
    private void onSave() throws IOException {
        String title = titleTextField.getText().trim();
        String description = descriptionTextArea.getText().trim();
        String location = locationTextField.getText().trim();
        LocalDate startDate = startDatePicker.getValue();
        String startTimeText = startTimeTextField.getText().trim();
        LocalDate endDate = endDatePicker.getValue();
        String endTimeText = endTimeTextField.getText().trim();

        //making sure required fields are filled up
        if (title.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please make sure the title is entered").showAndWait();
            return;
        }
        if (description.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please make sure the description is entered").showAndWait();
            return;
        }
        if (startDate == null || startTimeText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter both a start date and start time (HH:mm)").showAndWait();
            return;
        }
        if (endDate == null || endTimeText.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please enter both an end date and end time (HH:mm)").showAndWait();
            return;
        }

        //time format and order checks
        LocalTime startTime;
        LocalTime endTime;
        try {
            startTime = LocalTime.parse(startTimeText, TIME_FORMATTER);
            endTime = LocalTime.parse(endTimeText, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            new Alert(Alert.AlertType.WARNING, "Time must be in HH:mm format (e.g. 09:30 or 14:00)").showAndWait();
            return;
        }

        LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

        if (!endDateTime.isAfter(startDateTime)) {
            new Alert(Alert.AlertType.WARNING, "End date/time must be after start date/time").showAndWait();
            return;
        }

        //capacity checks
        int capacity;
        try {
            capacity = Integer.parseInt(capacityTextField.getText().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Capacity must be a number").showAndWait();
            return;
        }

        if (capacity <= 0) {
            new Alert(Alert.AlertType.WARNING, "Capacity must be greater than 0").showAndWait();
            return;
        }

        String formattedStart = startDateTime.format(DB_FORMATTER);
        String formattedEnd = endDateTime.format(DB_FORMATTER);
        String finalLocation = location.isEmpty() ? null : location;

        if (currentConference == null) {
            Conference newConference = new Conference(
                    0, title, description, finalLocation, formattedStart, formattedEnd, capacity, null
            );
            conferenceDao.createConference(newConference);
            new Alert(Alert.AlertType.INFORMATION, "Conference created.").showAndWait();
        } else {
            //making sure capacity is not below confirmed participants
            try {
                int confirmedCount = 0;
                for (Registration registration : registrationDao.findAll()) {
                    if (registration.getConferenceId() == currentConference.getConferenceId()
                            && "CONFIRMED".equals(registration.getStatus())) {
                        confirmedCount++;
                    }
                }
                if (capacity < confirmedCount) {
                    new Alert(Alert.AlertType.WARNING,
                            "Capacity cannot be below the number of confirmed participants (" + confirmedCount + ").").showAndWait();
                    return;
                }
            } catch (SQLException e) {
                new Alert(Alert.AlertType.ERROR, "Could not verify confirmed registrations.").showAndWait();
                return;
            }

            currentConference.setTitle(title);
            currentConference.setDescription(description);
            currentConference.setLocation(finalLocation);
            currentConference.setStartTime(formattedStart);
            currentConference.setEndTime(formattedEnd);
            currentConference.setCapacity(capacity);
            conferenceDao.updateConference(currentConference);
            new Alert(Alert.AlertType.INFORMATION, "Conference updated.").showAndWait();
        }

        onCancel();
    }

    @FXML
    private void onCancel() throws IOException {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/ManageConferences.FXML"))));
        stage.sizeToScene();
    }
}