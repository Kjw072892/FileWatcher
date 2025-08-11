package com.tcss.filewatcher.Viewer;

import com.tcss.filewatcher.Model.DirectoryEntry;
import com.tcss.filewatcher.Model.SceneHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * The query scene controller.
 *
 * @author Kassie Whitney
 * @version 8.6.25
 */
public class QuerySceneController extends SceneHandler implements PropertyChangeListener {

    /**
     * The table of which houses the historical records.
     */
    @FXML
    public TableView<DirectoryEntry> querySceneTable;

    /**
     * The date column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDateColumn;

    /**
     * The Time column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myTimeColumn;

    /**
     * The modification type column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myModificationType;

    /**
     * The directory column.
     */
    @FXML
    public TableColumn<DirectoryEntry, String> myDirectory;

    /**
     * This scenes property change support object. Allows for firing property changes.
     */
    private final PropertyChangeSupport myChanges = new PropertyChangeSupport(this);

    @FXML
    public void initialize() {
        addPropertyChangeListener(this);

    }

        /**
     * Connects the main scene to the filewatcher scene.
     * <P>Ensures bidirectional communication.
     *
     * @param theScene the main scene object.
     */
    protected void setMyMainSceneController(final MainSceneController theScene) {
        theScene.addPropertyChangeListener(this);
        theScene.setAboutSceneController(this);
    }

    /**
     * Adds the mainScene as a listener for this Scene.
     *
     * @param theListener the Main scene listener object.
     */
     public void addPropertyChangeListener(final PropertyChangeListener theListener) {
        myChanges.addPropertyChangeListener(theListener);
    }


    /**
     * Houses actionable events based on what is heard via the listener.
     *
     * @param evt A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }
}
