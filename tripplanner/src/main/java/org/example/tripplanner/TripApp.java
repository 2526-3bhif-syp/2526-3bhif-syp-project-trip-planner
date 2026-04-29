package org.example.tripplanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class TripApp extends Application {

    private ObservableList<Trip> reisen = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Reiseplaner");

        // Übersicht
        ListView<Trip> listView = new ListView<>(reisen);

        Button btnNeueReise = new Button("Neue Reise erstellen");

        btnNeueReise.setOnAction(e -> openCreateDialog());

        VBox root = new VBox(10, btnNeueReise, listView);
        root.setStyle("-fx-padding: 20;");

        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }

    private void openCreateDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Neue Reise");

        DatePicker startPicker = new DatePicker();
        DatePicker endPicker = new DatePicker();
        TextField beschreibungField = new TextField();

        startPicker.setPromptText("Startdatum");
        endPicker.setPromptText("Enddatum");
        beschreibungField.setPromptText("Beschreibung (optional)");

        Button speichernBtn = new Button("Speichern");

        Label errorLabel = new Label();

        speichernBtn.setOnAction(e -> {
            LocalDate start = startPicker.getValue();
            LocalDate end = endPicker.getValue();
            String beschreibung = beschreibungField.getText();

            if (start == null || end == null) {
                errorLabel.setText("Bitte beide Daten auswählen!");
                return;
            }

            if (end.isBefore(start)) {
                errorLabel.setText("Enddatum muss nach Startdatum liegen!");
                return;
            }

            reisen.add(new Trip(start, end, beschreibung));
            dialog.close();
        });

        VBox layout = new VBox(10,
                new Label("Startdatum:"), startPicker,
                new Label("Enddatum:"), endPicker,
                new Label("Beschreibung:"), beschreibungField,
                speichernBtn,
                errorLabel
        );

        layout.setStyle("-fx-padding: 20;");
        dialog.setScene(new Scene(layout, 300, 300));
        dialog.show();
    }

    public static void main(String[] args) {
        launch();
    }
}