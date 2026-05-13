package org.example.tripplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;

public class Trip {

    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private final ObservableList<MeetingPoint> meetingPoints = FXCollections.observableArrayList();

    public Trip(LocalDate startDate, LocalDate endDate, String description) {
        this.startDate   = startDate;
        this.endDate     = endDate;
        this.description = description != null ? description : "";
    }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    public ObservableList<MeetingPoint> getMeetingPoints() { return meetingPoints; }

    @Override
    public String toString() {
        return startDate + " – " + endDate
                + (description.isBlank() ? "" : "  |  " + description)
                + "  (" + meetingPoints.size() + " stop" + (meetingPoints.size() == 1 ? "" : "s") + ")";
    }
}