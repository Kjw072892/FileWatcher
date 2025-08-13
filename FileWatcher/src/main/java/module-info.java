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
    requires com.opencsv;

    opens com.tcss.filewatcher to javafx.fxml;
    exports com.tcss.filewatcher.Viewer;
    opens com.tcss.filewatcher.Viewer to javafx.fxml;
    exports com.tcss.filewatcher.Common;
    opens com.tcss.filewatcher.Common to javafx.fxml;
    exports com.tcss.filewatcher.Model;
    opens com.tcss.filewatcher.Model to javafx.fxml;
}
