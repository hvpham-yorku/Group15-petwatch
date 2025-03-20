module com.petwatch.petwatch {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires java.sql;

    opens com.petwatch.petwatch to javafx.fxml;
    exports com.petwatch.petwatch;
}