package org.example.tripplanner;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TripApp extends Application {

    private ObservableList<Trip> trips = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        stage.setTitle("Trip Planner");

        ListView<Trip> tripListView = new ListView<>(trips);

        Button createTripButton = new Button("Create New Trip");
        createTripButton.setOnAction(e -> openTripDialog(null));

        // Double-click to edit
        tripListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tripListView.getSelectionModel().getSelectedItem() != null) {
                openTripDialog(tripListView.getSelectionModel().getSelectedItem());
            }
        });

        VBox root = new VBox(10, createTripButton, tripListView);
        root.setStyle("-fx-padding: 20;");

        stage.setScene(new Scene(root, 450, 350));
        stage.show();
    }

    /**
     * Opens the create/edit dialog.
     * @param existing null = new trip, otherwise the trip to edit
     */
    private void openTripDialog(Trip existing) {
        Stage dialog = new Stage();
        dialog.setTitle(existing == null ? "New Trip" : "Edit Trip");

        // ── Trip base data ────────────────────────────────────────────────
        DatePicker startDatePicker = new DatePicker();
        DatePicker endDatePicker = new DatePicker();
        TextField descriptionField = new TextField();

        startDatePicker.setPromptText("Start date");
        endDatePicker.setPromptText("End date");
        descriptionField.setPromptText("Description (optional)");

        if (existing != null) {
            startDatePicker.setValue(existing.getStartDate());
            endDatePicker.setValue(existing.getEndDate());
            descriptionField.setText(existing.getDescription());
        }

        // ── Meeting points ────────────────────────────────────────────────
        ObservableList<MeetingPoint> meetingPoints = FXCollections.observableArrayList(
                existing != null ? existing.getMeetingPoints() : FXCollections.emptyObservableList()
        );

        ListView<MeetingPoint> meetingPointListView = new ListView<>(meetingPoints);
        meetingPointListView.setPrefHeight(150);

        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextField timeField = new TextField();
        timeField.setPromptText("Time (HH:mm)");

        Button addMeetingPointButton = new Button("Add");
        Label meetingPointErrorLabel = new Label();
        meetingPointErrorLabel.setStyle("-fx-text-fill: red;");

        // Validate time format on focus lost
        timeField.focusedProperty().addListener((obs, oldFocused, newFocused) -> {
            if (!newFocused && !timeField.getText().isBlank()) {
                try {
                    LocalTime.parse(timeField.getText(), DateTimeFormatter.ofPattern("HH:mm"));
                    timeField.setStyle("");
                } catch (DateTimeParseException ex) {
                    timeField.setStyle("-fx-border-color: red;");
                }
            }
        });

        addMeetingPointButton.setOnAction(e -> {
            String location = locationField.getText().trim();
            String timeText = timeField.getText().trim();

            if (location.isEmpty() || timeText.isEmpty()) {
                meetingPointErrorLabel.setText("Please enter both location and time!");
                return;
            }
            try {
                LocalTime time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                meetingPoints.add(new MeetingPoint(location, time));
                locationField.clear();
                timeField.clear();
                timeField.setStyle("");
                meetingPointErrorLabel.setText("");
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            } catch (DateTimeParseException ex) {
                meetingPointErrorLabel.setText("Invalid time format! Please use HH:mm.");
            }
        });

        // ── Reorder buttons ───────────────────────────────────────────────
        Button moveUpButton = new Button("▲ Up");
        Button moveDownButton = new Button("▼ Down");
        Button deleteButton = new Button("✕ Delete");

        moveUpButton.setOnAction(e -> {
            int index = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (index > 0) {
                MeetingPoint item = meetingPoints.remove(index);
                meetingPoints.add(index - 1, item);
                meetingPointListView.getSelectionModel().select(index - 1);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });

        moveDownButton.setOnAction(e -> {
            int index = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (index >= 0 && index < meetingPoints.size() - 1) {
                MeetingPoint item = meetingPoints.remove(index);
                meetingPoints.add(index + 1, item);
                meetingPointListView.getSelectionModel().select(index + 1);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });

        deleteButton.setOnAction(e -> {
            int index = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (index >= 0) {
                meetingPoints.remove(index);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });

        HBox reorderButtonBox = new HBox(5, moveUpButton, moveDownButton, deleteButton);

        // ── Travel duration label ─────────────────────────────────────────
        Label travelDurationLabel = new Label();
        travelDurationLabel.setStyle("-fx-text-fill: #1a6fb5; -fx-font-weight: bold;");
        meetingPointListView.setUserData(travelDurationLabel);
        updateTravelDurationLabel(meetingPoints, meetingPointListView);

        // ── Save ──────────────────────────────────────────────────────────
        Button saveButton = new Button("Save");
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        saveButton.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            String description = descriptionField.getText();

            if (startDate == null || endDate == null) {
                errorLabel.setText("Please select both dates!");
                return;
            }
            if (endDate.isBefore(startDate)) {
                errorLabel.setText("End date must be after start date!");
                return;
            }

            if (existing != null) {
                int index = trips.indexOf(existing);
                Trip updatedTrip = new Trip(startDate, endDate, description);
                updatedTrip.getMeetingPoints().setAll(meetingPoints);
                trips.set(index, updatedTrip);
            } else {
                Trip newTrip = new Trip(startDate, endDate, description);
                newTrip.getMeetingPoints().setAll(meetingPoints);
                trips.add(newTrip);
            }
            dialog.close();
        });

        // ── Layout ────────────────────────────────────────────────────────
        HBox timeLocationBox = new HBox(5, timeField, locationField, addMeetingPointButton);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        VBox layout = new VBox(8,
                new Label("Start date:"), startDatePicker,
                new Label("End date:"), endDatePicker,
                new Label("Description:"), descriptionField,
                new Separator(),
                new Label("Meeting Points:"),
                meetingPointListView,
                travelDurationLabel,
                new Label("New meeting point (time + location):"),
                timeLocationBox,
                meetingPointErrorLabel,
                reorderButtonBox,
                new Separator(),
                saveButton,
                errorLabel
        );

        layout.setPadding(new Insets(20));
        dialog.setScene(new Scene(layout, 420, 560));
        dialog.show();
    }

    /**
     * Calculates and displays the estimated travel duration between all meeting points.
     * Only shown when at least 2 meeting points exist.
     */
    private void updateTravelDurationLabel(ObservableList<MeetingPoint> meetingPoints,
                                           ListView<MeetingPoint> listView) {
        Label label = (Label) listView.getUserData();
        if (label == null) return;

        if (meetingPoints.size() < 2) {
            label.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder("Estimated travel time: ");
        for (int i = 0; i < meetingPoints.size() - 1; i++) {
            LocalTime from = meetingPoints.get(i).getTime();
            LocalTime to = meetingPoints.get(i + 1).getTime();
            long minutes = ChronoUnit.MINUTES.between(from, to);

            if (minutes < 0) {
                minutes += 24 * 60; // handle past midnight
            }

            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            sb.append(meetingPoints.get(i).getLocation())
                    .append(" → ")
                    .append(meetingPoints.get(i + 1).getLocation())
                    .append(": ");

            if (hours > 0) {
                sb.append(hours).append("h ");
            }
            sb.append(remainingMinutes).append("min");

            if (i < meetingPoints.size() - 2) {
                sb.append(" | ");
            }
        }

        label.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch();
    }
}