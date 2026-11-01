package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.io.TempDir;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.LogicManager;
import seedu.address.model.ModelManager;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/**
 * Checks restored window positions using real JavaFX stages. Set RUN_UI_TESTS=true with a display available.
 */
@EnabledIfEnvironmentVariable(named = "RUN_UI_TESTS", matches = "true")
class MainWindowPositionTest {
    @TempDir
    public Path temporaryFolder;

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
