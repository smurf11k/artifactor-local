package com.renata.presentation.controller.user;

import com.renata.application.contract.SignUpService;
import com.renata.application.dto.UserStoreDto;
import com.renata.domain.entities.User.Role;
import com.renata.presentation.util.MessageManager;
import com.renata.presentation.viewmodel.UserViewModel;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SignUpController {

    @Autowired private SignUpService signUpService;
    @Autowired private MessageManager messageManager;

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

        userViewModel = new UserViewModel(UUID.randomUUID(), "", "", "", Role.GENERAL);

        bindFieldsToViewModel();
    }

    private void bindFieldsToViewModel() {
        usernameField.textProperty().bindBidirectional(userViewModel.usernameProperty());
        emailField.textProperty().bindBidirectional(userViewModel.emailProperty());
        passwordField.textProperty().bindBidirectional(userViewModel.passwordProperty());
        roleComboBox.valueProperty().bindBidirectional(userViewModel.roleProperty());
    }

    @FXML
    private void onSave() {
        if (isSaving) return;
        isSaving = true;

        try {
            UserStoreDto userStoreDto =
                    new UserStoreDto(
                            userViewModel.getUsername().get(),
                            userViewModel.getPassword().get(),
                            userViewModel.getEmail().get(),
                            userViewModel.getRole().get());

            Supplier<String> verificationCodeSupplier = this::askVerificationCode;

            signUpService.signUp(userStoreDto, verificationCodeSupplier);

            messageManager.showInfoAlert(
                    "Інформація користувача",
                    "Користувач успішно збережений",
                    userViewModel.toString());

        } catch (Exception e) {
            messageManager.showErrorAlert(
                    "Реєстрація не вдалась", "Щось пішло не так: ", e.getMessage());
        } finally {
            isSaving = false;
        }
    }

    private String askVerificationCode() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Верифікація пошти");
        dialog.setHeaderText("Введіть код верифікації");
        dialog.setContentText("Будь ласка, введіть код, надісланий на вашу електронну адресу:");

        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

        Optional<String> result = dialog.showAndWait();
        return result.orElseThrow(
                () -> new RuntimeException("Верифікацію скасовано користувачем."));
    }
}
