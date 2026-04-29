package org.example.tripplanner;

import java.time.LocalDate;

public class Trip {
    private LocalDate startDatum;
    private LocalDate endDatum;
    private String beschreibung;

    public Trip(LocalDate startDatum, LocalDate endDatum, String beschreibung) {
        this.startDatum = startDatum;
        this.endDatum = endDatum;
        this.beschreibung = beschreibung;
    }

    public LocalDate getStartDatum() {
        return startDatum;
    }

    public LocalDate getEndDatum() {
        return endDatum;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    @Override
    public String toString() {
        return startDatum + " - " + endDatum +
                (beschreibung.isEmpty() ? "" : " | " + beschreibung);
    }
}