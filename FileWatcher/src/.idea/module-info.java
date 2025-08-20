module com.tcss.filewatcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.web;

    // JDK / JDBC
    requires java.logging;
    requires java.sql;
    requires java.desktop;

    // SQLite JDBC
    requires org.xerial.sqlitejdbc;

    // 3rd-party JavaFX libs (you use them)
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    // Google client libs (match your POM)
    requires com.google.api.client;               // google-api-client
    requires com.google.api.client.json.gson;     // gson binding
    requires com.google.api.services.gmail;       // gmail v1

    // Mail
    requires jakarta.mail;

    // CSV
    requires com.opencsv;

    // Compile-time only
    requires static org.jetbrains.annotations;
    requires google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.auth;

    // FXML reflection access
    opens com.tcss.filewatcher.Viewer to javafx.fxml;
    opens com.tcss.filewatcher.Controller to javafx.fxml;
    opens com.tcss.filewatcher.Model to javafx.fxml;
    opens com.tcss.filewatcher.Common to javafx.fxml;

    // Public API (if other modules depend on these)
    exports com.tcss.filewatcher.Viewer;
    exports com.tcss.filewatcher.Controller;
    exports com.tcss.filewatcher.Model;
    exports com.tcss.filewatcher.Common;
}
