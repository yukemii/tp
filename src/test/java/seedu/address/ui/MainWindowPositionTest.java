package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/**
 * Checks restored window positions using real JavaFX stages. Set RUN_UI_TESTS=true with a display available.
 */
@EnabledIfEnvironmentVariable(named = "RUN_UI_TESTS", matches = "true")
class MainWindowPositionTest {
    @TempDir
    public Path temporaryFolder;

    @Test
    void command_saveFailure_updatesHeadingAndKeepsErrorVisible() throws Exception {
        FutureTask<Void> checks = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            ModelManager model = new ModelManager();
            model.addPerson(new PersonBuilder().build());
            Path dataPath = temporaryFolder.resolve("blocked");
            Files.createDirectory(dataPath);
            Files.writeString(dataPath.resolve("keep.txt"), "keep");
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                    new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
            MainWindow window = new MainWindow(new Stage(), new LogicManager(model, storage), dataPath);
            window.fillInnerParts();
            TextField input = (TextField) window.getRoot().getScene().lookup("#commandTextField");
            input.setText("delete 1");
            input.fireEvent(new ActionEvent());
            Label heading = (Label) window.getRoot().getScene().lookup("#listHeading");
            TextArea feedback = (TextArea) window.getRoot().getScene().lookup("#resultDisplay");
            assertEquals(0, model.getFilteredPersonList().size());
            assertEquals(model.getPersonListHeading(), heading.getText());
            assertTrue(feedback.getText().startsWith("Could not save data"));
            assertTrue(feedback.getStyleClass().contains("feedback-error"));
            assertEquals("delete 1", input.getText());
            assertEquals("keep", Files.readString(dataPath.resolve("keep.txt")));
            input.setText("list");
            input.fireEvent(new ActionEvent());
            assertEquals(model.getPersonListHeading(), heading.getText());
            assertEquals("", input.getText());
            assertEquals("No engagements to display.", feedback.getText());
            assertEquals("keep", Files.readString(dataPath.resolve("keep.txt")));
            return null;
        });
        try {
            Platform.startup(checks);
        } catch (IllegalStateException e) {
            Platform.runLater(checks);
        }
        checks.get(15, TimeUnit.SECONDS);
    }

    @Test
    void show_savedPositions_keepTitleReachableAndPreferencesUnchanged() throws Exception {
        FutureTask<Void> checks = new FutureTask<>(() -> {
            Platform.setImplicitExit(false);
            for (GuiSettings settings : List.of(new GuiSettings(3072, 1632, Integer.MAX_VALUE, Integer.MAX_VALUE),
                    new GuiSettings(800, 600, 100, 100))) {
                ModelManager model = new ModelManager();
                model.setGuiSettings(settings);
                Path dataPath = temporaryFolder.resolve("data.json");
                StorageManager storage = new StorageManager(new JsonAddressBookStorage(dataPath),
                        new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
                Stage stage = new Stage();
                MainWindow window = new MainWindow(stage, new LogicManager(model, storage), dataPath);
                window.fillInnerParts();
                try {
                    window.show();
                    Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                    assertTrue(screen.contains(stage.getX(), stage.getY(), 160, 40));
                    assertEquals(settings, model.getGuiSettings());
                } finally {
                    stage.hide();
                }
            }
            return null;
        });
        try {
            Platform.startup(checks);
        } catch (IllegalStateException e) {
            Platform.runLater(checks);
        }
        checks.get(15, TimeUnit.SECONDS);
    }
}
