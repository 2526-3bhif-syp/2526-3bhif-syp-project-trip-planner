package org.example.tripplanner;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TripStorage {

    private static final Path STORAGE_DIR  = Path.of(System.getProperty("user.home"), ".tripplanner");
    private static final Path STORAGE_FILE = STORAGE_DIR.resolve("trips.csv");
    private static final String SEP = "\t";

    public static void save(ObservableList<Trip> trips) {
        try {
            Files.createDirectories(STORAGE_DIR);
            List<String> lines = new ArrayList<>();
            for (Trip trip : trips) {
                lines.add("TRIP" + SEP
                        + trip.getStartDate() + SEP
                        + trip.getEndDate()   + SEP
                        + escape(trip.getDescription()));
                for (MeetingPoint mp : trip.getMeetingPoints()) {
                    lines.add("MP" + SEP
                            + escape(mp.getLocation()) + SEP
                            + mp.getTime());
                }
            }
            Files.write(STORAGE_FILE, lines);
        } catch (IOException e) {
            System.err.println("[TripStorage] save failed: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------ load

    public static ObservableList<Trip> load() {
        ObservableList<Trip> trips = FXCollections.observableArrayList();
        if (!Files.exists(STORAGE_FILE)) return trips;

        try {
            List<String> lines = Files.readAllLines(STORAGE_FILE);
            Trip current = null;
            for (String line : lines) {
                if (line.isBlank()) continue;
                String[] parts = line.split(SEP, -1);
                if (parts[0].equals("TRIP") && parts.length >= 4) {
                    current = new Trip(
                            LocalDate.parse(parts[1]),
                            LocalDate.parse(parts[2]),
                            unescape(parts[3])
                    );
                    trips.add(current);
                } else if (parts[0].equals("MP") && parts.length >= 3 && current != null) {
                    current.getMeetingPoints().add(
                            new MeetingPoint(unescape(parts[1]), LocalTime.parse(parts[2]))
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("[TripStorage] load failed: " + e.getMessage());
        }
        return trips;
    }

    // ---------------------------------------------------------------- helpers

    /** Escapes tab and newline characters so they don't break the TSV format. */
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case 't'  -> { sb.append('\t'); i++; }
                    case 'n'  -> { sb.append('\n'); i++; }
                    case 'r'  -> { sb.append('\r'); i++; }
                    case '\\' -> { sb.append('\\'); i++; }
                    default   -> sb.append(s.charAt(i));
                }
            } else {
                sb.append(s.charAt(i));
            }
        }
        return sb.toString();
    }
}