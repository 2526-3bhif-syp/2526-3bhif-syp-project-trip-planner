module org.example.tripplanner {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.tripplanner to javafx.fxml;
    exports org.example.tripplanner;
}