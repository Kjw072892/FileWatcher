module com.tcss.filewatcher {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.logging;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;
    requires javafx.graphics;
    requires com.google.api.client.json.gson;
    requires com.google.api.services.gmail;
    requires google.api.client;
    requires jakarta.mail;
    requires com.google.auth;
    requires com.google.api.client;
    requires com.google.api.client.extensions.jetty.auth;
    requires com.google.api.client.extensions.java6.auth;
    requires com.google.api.client.auth;
    requires com.opencsv;
    requires jdk.httpserver;
    requires commons.collections;
    requires org.jetbrains.annotations;

    opens com.tcss.filewatcher to javafx.fxml;
    exports com.tcss.filewatcher.Viewer;
    opens com.tcss.filewatcher.Viewer to javafx.fxml;
    exports com.tcss.filewatcher.Common;
    opens com.tcss.filewatcher.Common to javafx.fxml;
    exports com.tcss.filewatcher.Model;
    opens com.tcss.filewatcher.Model to javafx.fxml;
    exports com.tcss.filewatcher.Controller;
    opens com.tcss.filewatcher.Controller to javafx.fxml;
}
