package com.thoughtapps.droppoint.droppoint.ui;

import com.jfoenix.controls.JFXDecorator;
import io.datafx.controller.flow.Flow;
import io.datafx.controller.flow.container.DefaultFlowContainer;
import io.datafx.controller.flow.context.ViewFlowContext;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AppUI extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        DefaultFlowContainer container = new DefaultFlowContainer();
        ViewFlowContext flowContext = new ViewFlowContext();
        Flow flow = new Flow(AppUIController.class);
        flow.createHandler(flowContext).start(container);

        JFXDecorator decorator = new JFXDecorator(stage, container.getView());
        decorator.setCustomMaximize(true);
        Scene scene = new Scene(decorator, 400, 630);
        final ObservableList<String> stylesheets = scene.getStylesheets();
        stylesheets.addAll(AppUI.class.getResource("/css/jfoenix-fonts.css").toExternalForm(),
                AppUI.class.getResource("/css/jfoenix-design.css").toExternalForm(),
                AppUI.class.getResource("/css/jfoenix-main-demo.css").toExternalForm());
        stage.setMinWidth(400);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }
}