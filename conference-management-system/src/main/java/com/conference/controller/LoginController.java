package com.conference.controller;

import com.conference.dao.CredentialDao;
import com.conference.dao.UserDao;
import com.conference.model.Credential;
import com.conference.model.User;
import com.conference.util.PasswordUtil;
import com.conference.util.WhosLoggedIn;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private void onLogin(ActionEvent event) throws IOException {
        User user = new UserDao().findByEmail(emailField.getText().trim());

        if (user == null || !passwordMatches(user, passwordField.getText())) {
            new Alert(Alert.AlertType.ERROR, "Incorrect email or password.").showAndWait();
            return;
        }

        WhosLoggedIn.setCurrentUser(user);

        if ("ADMIN".equals(user.getRole())) {
            openScreen(event, "AdminDashboard.FXML");
        } else {
            openScreen(event, "ParticipantDashBoard.FXML");
        }
    }

    @FXML
    private void onCreateAccount(ActionEvent event) throws IOException {
        openScreen(event, "CreateAccount.FXML");
    }

    private boolean passwordMatches(User user, String password) {
        Credential credential = new CredentialDao().findByUserId(user.getUserId());
        return credential != null
                && credential.getPasswordHash().equals(PasswordUtil.hash(password));
    }

    private void openScreen(ActionEvent event, String fxmlFile) throws IOException {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}