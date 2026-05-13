package org.example.tripplanner;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TripApp extends Application {

    private ObservableList<Trip> trips = FXCollections.observableArrayList();
    private StackPane tripListOverlay;
    private StackPane tripFormOverlay;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Trip Planner");

        ListView<Trip> tripListView = new ListView<>(trips);
        VBox.setVgrow(tripListView, Priority.ALWAYS);

        tripListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && tripListView.getSelectionModel().getSelectedItem() != null) {
                openTripForm(tripListView.getSelectionModel().getSelectedItem());
            }
        });

        // --- Navbar ---
        Button navBtn = new Button();
        navBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 4 6 4 6;");
        VBox burgerIcon = new VBox(5);
        burgerIcon.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Region line = new Region();
            line.setPrefSize(22, 3);
            line.setStyle("-fx-background-color: #7F00FF; -fx-background-radius: 2;");
            burgerIcon.getChildren().add(line);
        }
        navBtn.setGraphic(burgerIcon);

        Label navTitle = new Label("Trip Planner");
        navTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #7F00FF;");

        HBox navbar = new HBox(10, navBtn, navTitle);
        navbar.setAlignment(Pos.CENTER_LEFT);
        navbar.setPadding(new Insets(12, 16, 12, 16));
        navbar.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0e0e0;" +
                        "-fx-border-width: 0 0 1 0;"
        );

        // --- Plus Button ---
        Button createTripButton = new Button("+");
        createTripButton.setStyle(
                "-fx-background-radius: 50em;" +
                        "-fx-min-width: 50px; -fx-min-height: 50px;" +
                        "-fx-max-width: 50px; -fx-max-height: 50px;" +
                        "-fx-background-color: #7F00FF;" +
                        "-fx-text-fill: white; -fx-font-size: 22px; -fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(127,0,255,0.4), 10, 0, 0, 3);"
        );

        ScaleTransition hoverIn = new ScaleTransition(Duration.millis(150), createTripButton);
        hoverIn.setToX(1.15); hoverIn.setToY(1.15);
        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(150), createTripButton);
        hoverOut.setToX(1.0); hoverOut.setToY(1.0);

        createTripButton.setOnMouseEntered(e -> { hoverOut.stop(); hoverIn.play(); });
        createTripButton.setOnMouseExited(e -> { hoverIn.stop(); hoverOut.play(); });

        createTripButton.setOnAction(e -> {
            Timeline wobble = new Timeline(
                    new KeyFrame(Duration.millis(0),   new KeyValue(createTripButton.rotateProperty(), 0)),
                    new KeyFrame(Duration.millis(80),  new KeyValue(createTripButton.rotateProperty(), -15)),
                    new KeyFrame(Duration.millis(160), new KeyValue(createTripButton.rotateProperty(), 15)),
                    new KeyFrame(Duration.millis(240), new KeyValue(createTripButton.rotateProperty(), -10)),
                    new KeyFrame(Duration.millis(320), new KeyValue(createTripButton.rotateProperty(), 10)),
                    new KeyFrame(Duration.millis(400), new KeyValue(createTripButton.rotateProperty(), 0))
            );
            wobble.setOnFinished(ev -> openTripForm(null));
            wobble.play();
        });

        HBox buttonBox = new HBox(createTripButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(8, 0, 0, 0));

        Label placeholderLabel = new Label("No content yet.");
        placeholderLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
        StackPane mainArea = new StackPane(placeholderLabel);
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox mainContent = new VBox(0, navbar, mainArea, buttonBox);
        mainContent.setPadding(new Insets(0, 20, 20, 20));

        // --- Overlays ---
        tripListOverlay = buildTripListOverlay(tripListView);
        tripListOverlay.setVisible(false);

        tripFormOverlay = new StackPane();
        tripFormOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        tripFormOverlay.setVisible(false);
        tripFormOverlay.setOnMouseClicked(e -> e.consume());

        navBtn.setOnAction(e -> tripListOverlay.setVisible(true));

        StackPane root = new StackPane(mainContent, tripListOverlay, tripFormOverlay);
        stage.setScene(new Scene(root, 450, 400));
        stage.show();
    }

    private void openTripForm(Trip existing) {
        tripFormOverlay.getChildren().clear();

        VBox card = buildTripFormCard(existing);

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxWidth(380);

        StackPane.setAlignment(scrollPane, Pos.TOP_CENTER);
        StackPane.setMargin(scrollPane, new Insets(20, 0, 20, 0));

        tripFormOverlay.getChildren().add(scrollPane);
        tripFormOverlay.setVisible(true);
    }

    private VBox buildTripFormCard(Trip existing) {
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

        ObservableList<MeetingPoint> meetingPoints = FXCollections.observableArrayList(
                existing != null ? existing.getMeetingPoints() : FXCollections.emptyObservableList()
        );

        ListView<MeetingPoint> meetingPointListView = new ListView<>(meetingPoints);
        meetingPointListView.setPrefHeight(120);

        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextField timeField = new TextField();
        timeField.setPromptText("Time (HH:mm)");

        Button addMeetingPointButton = new Button("Add");
        Label meetingPointErrorLabel = new Label();
        meetingPointErrorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");

        timeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !timeField.getText().isBlank()) {
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
                meetingPointErrorLabel.setText("Invalid time format! Use HH:mm.");
            }
        });

        Button moveUpButton = new Button("▲");
        Button moveDownButton = new Button("▼");
        Button deleteButton = new Button("✕");

        moveUpButton.setOnAction(e -> {
            int i = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (i > 0) {
                MeetingPoint item = meetingPoints.remove(i);
                meetingPoints.add(i - 1, item);
                meetingPointListView.getSelectionModel().select(i - 1);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });
        moveDownButton.setOnAction(e -> {
            int i = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (i >= 0 && i < meetingPoints.size() - 1) {
                MeetingPoint item = meetingPoints.remove(i);
                meetingPoints.add(i + 1, item);
                meetingPointListView.getSelectionModel().select(i + 1);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });
        deleteButton.setOnAction(e -> {
            int i = meetingPointListView.getSelectionModel().getSelectedIndex();
            if (i >= 0) {
                meetingPoints.remove(i);
                updateTravelDurationLabel(meetingPoints, meetingPointListView);
            }
        });

        HBox reorderButtonBox = new HBox(5, moveUpButton, moveDownButton, deleteButton);

        Label travelDurationLabel = new Label();
        travelDurationLabel.setStyle("-fx-text-fill: #1a6fb5; -fx-font-weight: bold; -fx-font-size: 11px;");
        meetingPointListView.setUserData(travelDurationLabel);
        updateTravelDurationLabel(meetingPoints, meetingPointListView);

        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #7F00FF; -fx-text-fill: white;" +
                        "-fx-cursor: hand; -fx-background-radius: 6;"
        );
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle("-fx-cursor: hand;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 11px;");

        saveBtn.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();

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
                Trip updatedTrip = new Trip(startDate, endDate, descriptionField.getText());
                updatedTrip.getMeetingPoints().setAll(meetingPoints);
                trips.set(index, updatedTrip);
            } else {
                Trip newTrip = new Trip(startDate, endDate, descriptionField.getText());
                newTrip.getMeetingPoints().setAll(meetingPoints);
                trips.add(newTrip);
            }
            tripFormOverlay.setVisible(false);
        });

        cancelBtn.setOnAction(e -> tripFormOverlay.setVisible(false));

        HBox timeLocationBox = new HBox(5, timeField, locationField, addMeetingPointButton);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        Label cardTitle = new Label(existing == null ? "New Trip" : "Edit Trip");
        cardTitle.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        VBox card = new VBox(8,
                cardTitle,
                new Separator(),
                new Label("Start date:"), startDatePicker,
                new Label("End date:"), endDatePicker,
                new Label("Description:"), descriptionField,
                new Separator(),
                new Label("Meeting Points:"),
                meetingPointListView,
                travelDurationLabel,
                new Label("New meeting point:"),
                timeLocationBox,
                meetingPointErrorLabel,
                reorderButtonBox,
                new Separator(),
                btnRow,
                errorLabel
        );

        card.setPadding(new Insets(20));
        card.setMaxWidth(380);
        card.setMaxHeight(560);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 4);"
        );

        return card;
    }

    private StackPane buildTripListOverlay(ListView<Trip> tripListView) {
        VBox.setVgrow(tripListView, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-font-size: 16px;" +
                        "-fx-cursor: hand; -fx-text-fill: #555;"
        );

        Label heading = new Label("Your Trips");
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(heading, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(12, header, tripListView);
        card.setPadding(new Insets(20));
        card.setMaxWidth(320);
        card.setMaxHeight(300);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 20, 0, 0, 4);"
        );

        StackPane overlay = new StackPane(card);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.setOnMouseClicked(e -> e.consume());

        closeBtn.setOnAction(e -> tripListOverlay.setVisible(false));

        return overlay;
    }

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
            if (minutes < 0) minutes += 24 * 60;

            long hours = minutes / 60;
            long remainingMinutes = minutes % 60;

            sb.append(meetingPoints.get(i).getLocation())
                    .append(" → ")
                    .append(meetingPoints.get(i + 1).getLocation())
                    .append(": ");
            if (hours > 0) sb.append(hours).append("h ");
            sb.append(remainingMinutes).append("min");
            if (i < meetingPoints.size() - 2) sb.append(" | ");
        }

        label.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch();
    }
}