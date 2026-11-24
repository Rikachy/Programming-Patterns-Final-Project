package com.conference.controller;

import com.conference.dao.CredentialDao;
import com.conference.dao.UserDao;
import com.conference.model.Credential;
import com.conference.model.User;
import com.conference.util.PasswordUtil;
import com.conference.util.WhosLoggedIn;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField emailTxt;

    @FXML
    private PasswordField passwordTxt;

    private final UserDao userDao = new UserDao();
    private final CredentialDao credentialDao = new CredentialDao();

    @FXML
    private void onLogin() throws IOException {
        String email = emailTxt.getText().trim();
        String password = passwordTxt.getText();

        //making sure all textboxes are filled up
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

        User user = userDao.findByEmail(email);

        if (user == null || !passwordMatches(user, password)) {
            new Alert(Alert.AlertType.ERROR, "Incorrect email or password.").showAndWait();
            return;
        }

        WhosLoggedIn.setCurrentUser(user);

        if ("ADMIN".equals(user.getRole())) {
            openScreen("AdminDashboard.FXML");
        } else {
            openScreen("ParticipantDashBoard.FXML");
        }
    }

    @FXML
    private void onCreateAccount() throws IOException {
        openScreen("CreateAccount.FXML");
    }

    private boolean passwordMatches(User user, String password) {
        Credential credential = credentialDao.findByUserId(user.getUserId());
        return credential != null
                && credential.getPasswordHash().equals(PasswordUtil.hash(password));
    }

    private void openScreen(String fxmlFile) throws IOException {
        Stage stage = (Stage) emailTxt.getScene().getWindow();
        stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/FXML/" + fxmlFile))));
        stage.sizeToScene();
    }
}