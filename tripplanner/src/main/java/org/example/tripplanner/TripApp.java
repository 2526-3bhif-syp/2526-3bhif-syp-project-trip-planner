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
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

public class TripApp extends Application {

    private final ObservableList<Trip> trips = TripStorage.load();
    private StackPane tripListOverlay;
    private StackPane tripFormOverlay;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Trip Planner");

        ListView<Trip> tripListView = new ListView<>(trips);
        tripListView.setCellFactory(lv -> new TripListCell());
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

        ScaleTransition hoverIn  = new ScaleTransition(Duration.millis(150), createTripButton);
        hoverIn.setToX(1.15); hoverIn.setToY(1.15);
        ScaleTransition hoverOut = new ScaleTransition(Duration.millis(150), createTripButton);
        hoverOut.setToX(1.0); hoverOut.setToY(1.0);

        createTripButton.setOnMouseEntered(e -> { hoverOut.stop(); hoverIn.play(); });
        createTripButton.setOnMouseExited(e  -> { hoverIn.stop();  hoverOut.play(); });

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

        Label placeholderLabel = new Label("Press + to create a trip.");
        placeholderLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 14px;");
        StackPane mainArea = new StackPane(placeholderLabel);
        VBox.setVgrow(mainArea, Priority.ALWAYS);

        VBox mainContent = new VBox(0, navbar, mainArea, buttonBox);
        mainContent.setPadding(new Insets(0, 20, 20, 20));

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

    // ── Custom ListView Cell ──────────────────────────────────────────────────

    private static class TripListCell extends ListCell<Trip> {
        private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        @Override
        protected void updateItem(Trip trip, boolean empty) {
            super.updateItem(trip, empty);
            if (empty || trip == null) {
                setGraphic(null);
                setText(null);
                setStyle("-fx-background-color: transparent;");
                return;
            }

            // Left accent bar
            Region accent = new Region();
            accent.setPrefWidth(4);
            accent.setPrefHeight(48);
            accent.setStyle("-fx-background-color: #7F00FF; -fx-background-radius: 4 0 0 4;");

            // Date badge
            long days = ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate());
            Label daysLabel = new Label(days + "d");
            daysLabel.setStyle(
                    "-fx-background-color: #f0e6ff;" +
                            "-fx-text-fill: #7F00FF;" +
                            "-fx-font-size: 11px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 2 7 2 7;" +
                            "-fx-background-radius: 20;"
            );

            // Title row
            String title = (trip.getDescription() != null && !trip.getDescription().isBlank())
                    ? trip.getDescription()
                    : "Trip";
            Label nameLabel = new Label(title);
            nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #222;");

            // Date range
            Label dateLabel = new Label(
                    trip.getStartDate().format(fmt) + "  →  " + trip.getEndDate().format(fmt)
            );
            dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888;");

            // Meeting point count badge
            int mpCount = trip.getMeetingPoints().size();
            Label mpLabel = new Label(mpCount + " stop" + (mpCount == 1 ? "" : "s"));
            mpLabel.setStyle(
                    "-fx-background-color: #f5f5f5;" +
                            "-fx-text-fill: #999;" +
                            "-fx-font-size: 10px;" +
                            "-fx-padding: 2 6 2 6;" +
                            "-fx-background-radius: 20;"
            );

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox topRow = new HBox(6, nameLabel, spacer, daysLabel);
            topRow.setAlignment(Pos.CENTER_LEFT);

            HBox bottomRow = new HBox(6, dateLabel, mpLabel);
            bottomRow.setAlignment(Pos.CENTER_LEFT);

            VBox textBox = new VBox(4, topRow, bottomRow);
            textBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            HBox cell = new HBox(0, accent, textBox);
            cell.setAlignment(Pos.CENTER_LEFT);
            cell.setSpacing(12);
            cell.setPadding(new Insets(10, 12, 10, 0));

            setGraphic(cell);
            setText(null);
            setStyle(
                    "-fx-background-color: white;" +
                            "-fx-border-color: #f0f0f0;" +
                            "-fx-border-width: 0 0 1 0;" +
                            "-fx-padding: 0;"
            );
        }
    }

    // ── Trip Form ─────────────────────────────────────────────────────────────

    private void openTripForm(Trip existing) {
        tripFormOverlay.getChildren().clear();

        VBox card = buildTripFormCard(existing);

        ScrollPane scrollPane = new ScrollPane(card);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setMaxWidth(400);

        StackPane.setAlignment(scrollPane, Pos.TOP_CENTER);
        StackPane.setMargin(scrollPane, new Insets(20, 0, 20, 0));

        tripFormOverlay.getChildren().add(scrollPane);
        tripFormOverlay.setVisible(true);
    }

    private VBox buildTripFormCard(Trip existing) {
        // ── Styled inputs ──
        DatePicker startDatePicker = styledDatePicker("Start date");
        DatePicker endDatePicker   = styledDatePicker("End date");
        TextField  descriptionField = styledTextField("Description (optional)");

        if (existing != null) {
            startDatePicker.setValue(existing.getStartDate());
            endDatePicker.setValue(existing.getEndDate());
            descriptionField.setText(existing.getDescription());
        }

        ObservableList<MeetingPoint> meetingPoints = FXCollections.observableArrayList();
        if (existing != null) meetingPoints.addAll(existing.getMeetingPoints());

        ListView<MeetingPoint> mpListView = new ListView<>(meetingPoints);
        mpListView.setPrefHeight(120);
        mpListView.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #e8e8e8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        TextField locationField = styledTextField("Location");
        TextField timeField     = styledTextField("Time (HH:mm)");

        Button addBtn = new Button("Add");
        addBtn.setStyle(
                "-fx-background-color: #f0e6ff; -fx-text-fill: #7F00FF;" +
                        "-fx-font-weight: bold; -fx-cursor: hand;" +
                        "-fx-background-radius: 8; -fx-padding: 7 14 7 14;"
        );

        Label mpErrorLabel = new Label();
        mpErrorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 11px;");

        timeField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !timeField.getText().isBlank()) {
                try {
                    LocalTime.parse(timeField.getText().trim(), DateTimeFormatter.ofPattern("HH:mm"));
                    timeField.setStyle(inputStyle());
                } catch (DateTimeParseException ex) {
                    timeField.setStyle(inputStyle() + "-fx-border-color: #e53935;");
                }
            }
        });

        timeField.setOnAction(e -> addBtn.fire());
        locationField.setOnAction(e -> addBtn.fire());

        addBtn.setOnAction(e -> {
            String location = locationField.getText().trim();
            String timeText = timeField.getText().trim();
            if (location.isEmpty() || timeText.isEmpty()) {
                mpErrorLabel.setText("Please enter both location and time!");
                return;
            }
            try {
                LocalTime time = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                meetingPoints.add(new MeetingPoint(location, time));
                locationField.clear();
                timeField.clear();
                timeField.setStyle(inputStyle());
                mpErrorLabel.setText("");
                updateTravelDurationLabel(meetingPoints, mpListView);
            } catch (DateTimeParseException ex) {
                mpErrorLabel.setText("Invalid time format! Use HH:mm.");
            }
        });

        Button moveUpBtn   = iconBtn("▲");
        Button moveDownBtn = iconBtn("▼");
        Button deleteBtn   = iconBtn("✕");
        deleteBtn.setStyle(deleteBtn.getStyle() + "-fx-text-fill: #e53935;");

        moveUpBtn.setOnAction(e -> {
            int i = mpListView.getSelectionModel().getSelectedIndex();
            if (i > 0) {
                MeetingPoint item = meetingPoints.remove(i);
                meetingPoints.add(i - 1, item);
                mpListView.getSelectionModel().select(i - 1);
                updateTravelDurationLabel(meetingPoints, mpListView);
            }
        });
        moveDownBtn.setOnAction(e -> {
            int i = mpListView.getSelectionModel().getSelectedIndex();
            if (i >= 0 && i < meetingPoints.size() - 1) {
                MeetingPoint item = meetingPoints.remove(i);
                meetingPoints.add(i + 1, item);
                mpListView.getSelectionModel().select(i + 1);
                updateTravelDurationLabel(meetingPoints, mpListView);
            }
        });
        deleteBtn.setOnAction(e -> {
            int i = mpListView.getSelectionModel().getSelectedIndex();
            if (i >= 0) {
                meetingPoints.remove(i);
                updateTravelDurationLabel(meetingPoints, mpListView);
            }
        });

        HBox reorderBox = new HBox(6, moveUpBtn, moveDownBtn, deleteBtn);

        Label travelLabel = new Label();
        travelLabel.setStyle(
                "-fx-text-fill: #7F00FF; -fx-font-size: 11px;" +
                        "-fx-background-color: #f0e6ff;" +
                        "-fx-padding: 4 10 4 10; -fx-background-radius: 20;"
        );
        travelLabel.setWrapText(true);
        mpListView.setUserData(travelLabel);
        updateTravelDurationLabel(meetingPoints, mpListView);

        // ── Save / Cancel ──
        Button saveBtn = new Button("Save Trip");
        saveBtn.setStyle(
                "-fx-background-color: #7F00FF; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-cursor: hand; -fx-background-radius: 10;" +
                        "-fx-padding: 10 24 10 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(127,0,255,0.35), 8, 0, 0, 2);"
        );
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: #f5f5f5; -fx-text-fill: #666;" +
                        "-fx-cursor: hand; -fx-background-radius: 10;" +
                        "-fx-padding: 10 20 10 20;"
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #e53935; -fx-font-size: 11px;");

        saveBtn.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate   = endDatePicker.getValue();

            if (startDate == null || endDate == null) {
                errorLabel.setText("Please select both dates!");
                return;
            }
            if (endDate.isBefore(startDate)) {
                errorLabel.setText("End date must be after start date!");
                return;
            }

            if (existing != null) {
                existing.setStartDate(startDate);
                existing.setEndDate(endDate);
                existing.setDescription(descriptionField.getText());
                existing.getMeetingPoints().setAll(meetingPoints);
                trips.set(trips.indexOf(existing), existing);
            } else {
                Trip newTrip = new Trip(startDate, endDate, descriptionField.getText());
                newTrip.getMeetingPoints().setAll(meetingPoints);
                trips.add(newTrip);
            }

            TripStorage.save(trips);
            tripFormOverlay.setVisible(false);
        });

        cancelBtn.setOnAction(e -> tripFormOverlay.setVisible(false));

        HBox timeLocationBox = new HBox(8, timeField, locationField, addBtn);
        HBox.setHgrow(locationField, Priority.ALWAYS);

        HBox btnRow = new HBox(10, saveBtn, cancelBtn);
        btnRow.setAlignment(Pos.CENTER_RIGHT);

        // ── Section labels ──
        Label cardTitle = new Label(existing == null ? "✈  New Trip" : "✏  Edit Trip");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #222;");

        VBox card = new VBox(12,
                cardTitle,
                styledSeparator(),
                sectionLabel("Dates"),
                startDatePicker,
                endDatePicker,
                sectionLabel("Description"),
                descriptionField,
                styledSeparator(),
                sectionLabel("Meeting Points"),
                mpListView,
                travelLabel,
                sectionLabel("Add stop"),
                timeLocationBox,
                mpErrorLabel,
                reorderBox,
                styledSeparator(),
                btnRow,
                errorLabel
        );

        card.setPadding(new Insets(24));
        card.setMaxWidth(400);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 24, 0, 0, 6);"
        );

        return card;
    }

    // ── Trip List Overlay ─────────────────────────────────────────────────────

    private StackPane buildTripListOverlay(ListView<Trip> tripListView) {
        VBox.setVgrow(tripListView, Priority.ALWAYS);
        tripListView.setStyle(
                "-fx-background-color: #fafafa;" +
                        "-fx-border-color: #eeeeee;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;"
        );

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: #f5f5f5; -fx-font-size: 13px;" +
                        "-fx-cursor: hand; -fx-text-fill: #666; -fx-background-radius: 20;" +
                        "-fx-min-width: 28; -fx-min-height: 28;"
        );

        Label heading = new Label("Your Trips");
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #222;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(heading, spacer, closeBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        Button deleteTrip = new Button("Delete selected");
        deleteTrip.setStyle(
                "-fx-background-color: #fff0f0; -fx-text-fill: #e53935;" +
                        "-fx-cursor: hand; -fx-background-radius: 8;" +
                        "-fx-font-size: 12px; -fx-padding: 6 12 6 12;"
        );
        deleteTrip.setOnAction(e -> {
            Trip selected = tripListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                trips.remove(selected);
                TripStorage.save(trips);
            }
        });

        VBox card = new VBox(12, header, tripListView, deleteTrip);
        card.setPadding(new Insets(20));
        card.setMaxWidth(340);
        card.setMaxHeight(360);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-radius: 14;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 24, 0, 0, 6);"
        );

        StackPane overlay = new StackPane(card);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        overlay.setOnMouseClicked(e -> e.consume());

        closeBtn.setOnAction(e -> tripListOverlay.setVisible(false));

        return overlay;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String inputStyle() {
        return  "-fx-background-color: #fafafa;" +
                "-fx-border-color: #e0e0e0;" +
                "-fx-border-radius: 8; -fx-background-radius: 8;" +
                "-fx-padding: 8 10 8 10; -fx-font-size: 13px;";
    }

    private TextField styledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle(inputStyle());
        return f;
    }

    private DatePicker styledDatePicker(String prompt) {
        DatePicker dp = new DatePicker();
        dp.setPromptText(prompt);
        dp.setMaxWidth(Double.MAX_VALUE);
        dp.setStyle(inputStyle());
        return dp;
    }

    private Button iconBtn(String text) {
        Button b = new Button(text);
        b.setStyle(
                "-fx-background-color: #f5f5f5; -fx-text-fill: #555;" +
                        "-fx-cursor: hand; -fx-background-radius: 8;" +
                        "-fx-padding: 5 12 5 12;"
        );
        return b;
    }

    private Label sectionLabel(String text) {
        Label l = new Label(text.toUpperCase());
        l.setStyle(
                "-fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-text-fill: #aaa; -fx-padding: 4 0 0 0;"
        );
        return l;
    }

    private Separator styledSeparator() {
        Separator s = new Separator();
        s.setStyle("-fx-background-color: #f0f0f0;");
        return s;
    }

    private void updateTravelDurationLabel(ObservableList<MeetingPoint> meetingPoints,
                                           ListView<MeetingPoint> listView) {
        Label label = (Label) listView.getUserData();
        if (label == null) return;

        if (meetingPoints.size() < 2) {
            label.setText("");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < meetingPoints.size() - 1; i++) {
            LocalTime from = meetingPoints.get(i).getTime();
            LocalTime to   = meetingPoints.get(i + 1).getTime();
            long minutes   = ChronoUnit.MINUTES.between(from, to);
            if (minutes < 0) minutes += 24 * 60;

            long hours            = minutes / 60;
            long remainingMinutes = minutes % 60;

            sb.append(meetingPoints.get(i).getLocation())
                    .append(" → ")
                    .append(meetingPoints.get(i + 1).getLocation())
                    .append(": ");
            if (hours > 0) sb.append(hours).append("h ");
            sb.append(remainingMinutes).append("min");
            if (i < meetingPoints.size() - 2) sb.append("  |  ");
        }

        label.setText(sb.toString());
    }

    public static void main(String[] args) {
        launch();
    }
}