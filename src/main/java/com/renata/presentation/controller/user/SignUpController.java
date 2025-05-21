package com.renata.presentation.controller.user;

import com.renata.application.contract.UserService;
import com.renata.application.dto.UserStoreDto;
import com.renata.application.exception.SignUpException;
import com.renata.application.exception.ValidationException;
import com.renata.domain.entities.User.Role;
import com.renata.infrastructure.api.EmailSender;
import com.renata.infrastructure.api.exception.VerificationException;
import com.renata.presentation.viewmodel.UserViewModel;
import java.util.Optional;
import java.util.UUID;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Контролер реєстрації нового користувача. Керує: валідацією даних, верифікацією email, створенням
 * акаунта через UserService.
 */
@Component
public class SignUpController {

    @Autowired private UserService userService;
    @Autowired private EmailSender emailSender;

    @FXML private Label idLabel;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private ComboBox<Role> roleComboBox;

    private UserViewModel userViewModel;
    private boolean isSaving = false;

    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll(Role.values());
        roleComboBox.setValue(Role.GENERAL);

        userViewModel =
                new UserViewModel(
                        UUID.randomUUID(),
                        "JohnDoe",
                        "john.doe@example.com",
                        "password123",
                        Role.GENERAL);

        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        idLabel.setText(userViewModel.getId().toString());
        usernameField.textProperty().bindBidirectional(userViewModel.usernameProperty());
        emailField.textProperty().bindBidirectional(userViewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(userViewModel.passwordProperty());
        roleComboBox.valueProperty().bindBidirectional(userViewModel.roleProperty());
    }

    @FXML
    private void onSave() {
        if (isSaving) {
            return;
        }
        isSaving = true;

        try {
            String email = userViewModel.getEmail().get();
            emailSender.initiateVerification(email);

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Email Verification");
            dialog.setHeaderText("Enter Verification Code");
            dialog.setContentText("Please enter the verification code sent to " + email + ":");

            Optional<String> result = dialog.showAndWait();
            if (!result.isPresent()) {
                throw new VerificationException("Verification cancelled by user.");
            }

            emailSender.verifyCodeFromInput(email, result::get);

            UserStoreDto userStoreDto =
                    new UserStoreDto(
                            userViewModel.getUsername().get(),
                            userViewModel.getPassword().get(),
                            userViewModel.getEmail().get(),
                            userViewModel.getRole().get());

            userService.create(userStoreDto);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("User Information");
            alert.setHeaderText("User Data Saved Successfully");
            alert.setContentText(userViewModel.toString());
            alert.showAndWait();

        } catch (SignUpException | ValidationException | VerificationException e) {
            showErrorAlert(e.getMessage());
        } catch (Exception e) {
            showErrorAlert("An unexpected error occurred: " + e.getMessage());
        } finally {
            isSaving = false;
        }
    }

    @FXML
    private void onCancel() {
        System.out.println("Operation Cancelled");
    }

    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Registration Failed");
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
