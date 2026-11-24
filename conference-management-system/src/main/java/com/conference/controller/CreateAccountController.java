package com.conference.controller;
import com.conference.dao.CredentialDao;
import com.conference.dao.UserDao;
import com.conference.model.Credential;
import com.conference.model.User;
import com.conference.util.PasswordUtil;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class CreateAccountController {

    @FXML
    private TextField fullNameTxt;

    @FXML
    private TextField emailTxt;

    @FXML
    private TextField ageTxt;

    @FXML
    private PasswordField passwordTxt;

    private final UserDao userDao = new UserDao();
    private final CredentialDao credentialDao = new CredentialDao();

    @FXML
    private void onCreateAccount() throws IOException {
        String fullName = fullNameTxt.getText().trim();
        String email = emailTxt.getText().trim();
        String password = passwordTxt.getText();

        //making sure all textboxes are filled up
        if (fullName.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please make sure your name is entered").showAndWait();
            return;
        }
        if (email.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please make sure your email is entered").showAndWait();
            return;
        }
        if (password.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Please make sure your password is entered").showAndWait();
            return;
        }
        //email check
        if (!email.contains("@")) {
            new Alert(Alert.AlertType.WARNING, "Please make sure your email is valid (includes '@')").showAndWait();
            return;
        }
        //age checks
        int age;
        try {
            age = Integer.parseInt(ageTxt.getText().trim());
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.WARNING, "Age must be a number").showAndWait();
            return;
        }

        if (age <= 0 || age >= 125) {
            new Alert(Alert.AlertType.WARNING, "Age has to be between 1 and 124").showAndWait();
            return;
        }

        if (userDao.findByEmail(email) != null) {
            new Alert(Alert.AlertType.WARNING, "That email is already registered.").showAndWait();
            return;
        }

        User user = new User(0, fullName, email, age, "PARTICIPANT", null);
        int userId = userDao.createUser(user);
        credentialDao.createCredential(new Credential(userId, PasswordUtil.hash(password)));

        new Alert(Alert.AlertType.INFORMATION, "Account created. You can log in now.").showAndWait();
        onBackToLogin();
    }

    @FXML
    private void onBackToLogin() throws IOException {
        Stage stage = (Stage) fullNameTxt.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/LoginIN.FXML"))));
        stage.sizeToScene();
    }
}