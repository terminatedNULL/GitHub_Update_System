module com.example.finance_tracker {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires org.slf4j;

    opens com.example.finance_tracker to javafx.fxml;
    exports com.example.finance_tracker;
}