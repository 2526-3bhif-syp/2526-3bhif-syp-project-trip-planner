package org.example.tripplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class Trip {
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private ObservableList<MeetingPoint> meetingPoints = FXCollections.observableArrayList();

    public Trip(LocalDate startDate, LocalDate endDate, String description) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public ObservableList<MeetingPoint> getMeetingPoints() {
        return meetingPoints;
    }

    @Override
    public String toString() {
        return startDate + " - " + endDate +
                (description.isEmpty() ? "" : " | " + description) +
                " (" + meetingPoints.size() + " meeting point(s))";
    }
}