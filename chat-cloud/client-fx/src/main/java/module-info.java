module com.example.clientfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires lombok;
    requires java.desktop;

    opens com.example.clientfx to javafx.fxml;
    opens com.example.clientfx.test to javafx.fxml;
    exports com.example.clientfx.test;
    exports com.example.clientfx;
}