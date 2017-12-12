package com.thoughtapps.droppoint.droppoint.ui;

import com.jfoenix.controls.JFXButton;
import com.thoughtapps.droppoint.droppoint.DropPoint;
import com.thoughtapps.droppoint.droppoint.schedule.SshConnectionHolder;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import com.thoughtapps.droppoint.droppoint.ui.components.CustomJFXPasswordField;
import com.thoughtapps.droppoint.droppoint.ui.components.CustomJFXTextField;
import com.thoughtapps.droppoint.droppoint.ui.components.HasPropertyName;
import com.thoughtapps.droppoint.droppoint.ui.components.ValidationSupported;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import io.datafx.controller.ViewController;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import javax.annotation.PostConstruct;

@ViewController(value = "/fxml/AppUI.fxml", title = "Drop point")
public final class AppUIController {

    @FXML
    private JFXButton startButton;

    @FXML
    private JFXButton stopButton;

    @FXML
    private CustomJFXTextField dropPointId;

    @FXML
    private CustomJFXTextField allowedProcessorIds;

    @FXML
    private CustomJFXTextField dropPointPort;

    @FXML
    private CustomJFXTextField dropPointSFTPRootDir;

    @FXML
    private CustomJFXTextField dropPointPingIntervalSec;

    @FXML
    private CustomJFXTextField serverHostname;

    @FXML
    private CustomJFXTextField serverPort;

    @FXML
    private CustomJFXTextField serverUsername;

    @FXML
    private CustomJFXPasswordField serverPassword;

    @FXML
    private JFXButton editButton;

    @FXML
    private JFXButton saveButton;

    @FXML
    private FontAwesomeIconView conStatusIcon;

    @FXML
    private FontAwesomeIconView copyDropPointIdBtn;

    @PostConstruct
    public void init() throws Exception {
        enableFocusValidate(dropPointId);
        setFieldPropertyName(dropPointId, ConfigurationService.DROP_POINT_ID);

        enableFocusValidate(allowedProcessorIds);
        setFieldPropertyName(allowedProcessorIds, ConfigurationService.DROP_POINT_ALLOWED_PROCESSOR_IDS);

        enableFocusValidate(dropPointPort);
        setFieldPropertyName(dropPointPort, ConfigurationService.DROP_POINT_PORT);

        enableFocusValidate(dropPointSFTPRootDir);
        setFieldPropertyName(dropPointSFTPRootDir, ConfigurationService.DROP_POINT_ROOT_DIR);

        enableFocusValidate(dropPointPingIntervalSec);
        setFieldPropertyName(dropPointPingIntervalSec, ConfigurationService.DROP_POINT_PING_INTERVAL);

        enableFocusValidate(serverHostname);
        setFieldPropertyName(serverHostname, ConfigurationService.NODE_HOSTNAME);

        enableFocusValidate(serverPort);
        setFieldPropertyName(serverPort, ConfigurationService.NODE_PORT);

        enableFocusValidate(serverUsername);
        setFieldPropertyName(serverUsername, ConfigurationService.NODE_USERNAME);

        enableFocusValidate(serverPassword);
        setFieldPropertyName(serverPassword, ConfigurationService.NODE_PASSWORD);

        editButton.setOnMouseClicked((e) -> {
            setFormEditable(true);
            startButton.setDisable(true);
            stopButton.setDisable(true);
        });

        saveButton.setOnMouseClicked((e) -> {
            if (isFromValid()) {
                setFormEditable(false);
                saveFieldValues();
                startButton.setDisable(false);
                stopButton.setDisable(false);
            }
        });

        startButton.managedProperty().bind(startButton.visibleProperty());
        stopButton.managedProperty().bind(stopButton.visibleProperty());

        startButton.setOnMouseClicked((e) -> {
            startButton.setVisible(false);
            stopButton.setVisible(true);
            editButton.setDisable(true);
            saveButton.setDisable(true);
            DropPoint.getSpringContext().getBean(SshConnectionHolder.class).setIsConnectionAllowed(true);
        });

        stopButton.setOnMouseClicked((e) -> {
            startButton.setVisible(true);
            stopButton.setVisible(false);
            editButton.setDisable(false);
            saveButton.setDisable(true);
            DropPoint.getSpringContext().getBean(SshConnectionHolder.class).setIsConnectionAllowed(false);
            DropPoint.getSpringContext().getBean(SshConnectionHolder.class).destroy();
        });

        copyDropPointIdBtn.setOnMouseClicked((e) -> {
            final ClipboardContent content = new ClipboardContent();
            content.putString(dropPointId.getText());
            Clipboard.getSystemClipboard().setContent(content);
        });

        initFieldValues();

        //Periodically check connection to drop point processor
        Thread thread = new Thread() {
            @Override
            public void run() {
                SshConnectionHolder connectionHolder = DropPoint.getSpringContext().getBean(SshConnectionHolder.class);

                while (true) {
                    ObservableList<String> classes = conStatusIcon.getStyleClass();
                    if (connectionHolder.isConnectionReady()) {
                        classes.clear();
                        classes.add("status-icon-connected");
                    } else {
                        classes.clear();
                        classes.add("status-icon-disconnected");
                    }

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    //Load configuration from DB
    private void initFieldValues() {
        ConfigurationService confService = DropPoint.getSpringContext().getBean(ConfigurationService.class);
        dropPointId.setText(confService.getPropertyValue(dropPointId.getPropertyName()));
        allowedProcessorIds.setText(confService.getPropertyValue(allowedProcessorIds.getPropertyName()));
        dropPointPort.setText(confService.getPropertyValue(dropPointPort.getPropertyName()));
        dropPointSFTPRootDir.setText(confService.getPropertyValue(dropPointSFTPRootDir.getPropertyName()));
        dropPointPingIntervalSec.setText(confService.getPropertyValue(dropPointPingIntervalSec.getPropertyName()));
        serverHostname.setText(confService.getPropertyValue(serverHostname.getPropertyName()));
        serverPort.setText(confService.getPropertyValue(serverPort.getPropertyName()));
        serverUsername.setText(confService.getPropertyValue(serverUsername.getPropertyName()));
        serverPassword.setText(confService.getPropertyValue(serverPassword.getPropertyName()));
    }

    //Save configuration to DB
    private void saveFieldValues() {
        ConfigurationService confService = DropPoint.getSpringContext().getBean(ConfigurationService.class);
        confService.setPropertyValue(allowedProcessorIds.getPropertyName(), allowedProcessorIds.getText());
        confService.setPropertyValue(dropPointPort.getPropertyName(), dropPointPort.getText());
        confService.setPropertyValue(dropPointSFTPRootDir.getPropertyName(), dropPointSFTPRootDir.getText());
        confService.setPropertyValue(dropPointPingIntervalSec.getPropertyName(), dropPointPingIntervalSec.getText());
        confService.setPropertyValue(serverHostname.getPropertyName(), serverHostname.getText());
        confService.setPropertyValue(serverPort.getPropertyName(), serverPort.getText());
        confService.setPropertyValue(serverUsername.getPropertyName(), serverUsername.getText());
        confService.setPropertyValue(serverPassword.getPropertyName(), serverPassword.getText());
    }

    private void setFieldPropertyName(HasPropertyName hasPropertyName, String propertyName) {
        hasPropertyName.setPropertyName(propertyName);
    }

    private void enableFocusValidate(ValidationSupported field) {
        field.getFocusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                field.validate();
            }
        });
    }

    private boolean isFromValid() {
        return allowedProcessorIds.validate() && dropPointPort.validate() & dropPointSFTPRootDir.validate()
                & dropPointPingIntervalSec.validate() & serverHostname.validate()
                & serverPort.validate() & serverUsername.validate() & serverPassword.validate();
    }

    private void setFormEditable(boolean isEditable) {
        editButton.setDisable(isEditable);
        saveButton.setDisable(!isEditable);

        allowedProcessorIds.setDisable(!isEditable);
        dropPointPort.setDisable(!isEditable);
        dropPointSFTPRootDir.setDisable(!isEditable);
        dropPointPingIntervalSec.setDisable(!isEditable);
        serverHostname.setDisable(!isEditable);
        serverPort.setDisable(!isEditable);
        serverUsername.setDisable(!isEditable);
        serverPassword.setDisable(!isEditable);
    }
}
