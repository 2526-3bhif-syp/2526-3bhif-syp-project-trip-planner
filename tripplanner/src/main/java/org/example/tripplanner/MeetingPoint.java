package org.example.tripplanner;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MeetingPoint {

    private String location;
    private LocalTime time;

    public MeetingPoint(String location, LocalTime time) {
        this.location = location;
        this.time = time;
    }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    @Override
    public String toString() {
        return time.format(DateTimeFormatter.ofPattern("HH:mm")) + " – " + location;
    }
}