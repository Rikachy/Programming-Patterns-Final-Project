package com.conference.controller;

import com.conference.dao.UserDao;
import com.conference.model.User;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class MyProfileController {

    @FXML
    private Button backButton;

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField ageField;

    @FXML
    private Label messageLabel;

    private final UserDao userDao = new UserDao();

    @FXML
    public void initialize() {
        User currentUser = WhosLoggedIn.getCurrentUser();

        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        ageField.setText(String.valueOf(currentUser.getAge()));
        messageLabel.setText("");
    }

    @FXML
    private void onSave() {
        User currentUser = WhosLoggedIn.getCurrentUser();

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();

        if (fullName.isEmpty()) {
            messageLabel.setText("Please enter your name.");
            return;
        }
        if (email.isEmpty()) {
            messageLabel.setText("Please enter your email.");
            return;
        }
        if (!email.contains("@")) {
            messageLabel.setText("Please enter a valid email (includes '@').");
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            messageLabel.setText("Age must be a number.");
            return;
        }

        if (age <= 0 || age >= 125) {
            messageLabel.setText("Age has to be between 1 and 124.");
            return;
        }

        // the email can't belong to a different account
        User existing = userDao.findByEmail(email);
        if (existing != null && existing.getUserId() != currentUser.getUserId()) {
            messageLabel.setText("That email is already used by another account.");
            return;
        }

        User updatedUser = new User(currentUser.getUserId(), fullName, email, age,
                currentUser.getRole(), currentUser.getCreatedAt());

        userDao.updateUser(updatedUser);
        WhosLoggedIn.setCurrentUser(updatedUser);

        messageLabel.setText("Profile updated.");
    }

    @FXML
    private void onBack() throws IOException {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/ParticipantDashBoard.FXML"))));
        stage.sizeToScene();
    }
}