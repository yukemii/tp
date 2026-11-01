package seedu.address;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonUserPrefsStorage;

/**
 * Tests recovery from invalid application preferences.
 */
public class MainAppTest {
    @TempDir
    public Path testFolder;

    @Test
    public void initPrefs_nullGuiSettings_restoresAndSavesDefaults() throws Exception {
        Path prefsPath = testFolder.resolve("preferences.json");
        Files.writeString(prefsPath, "{\"guiSettings\":null}");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsPath);

        UserPrefs prefs = new MainApp().initPrefs(storage);

        assertEquals(new UserPrefs(), prefs);
        assertEquals(prefs, storage.readUserPrefs().orElseThrow());
    }

    @Test
    public void initPrefs_invalidWindowDimensions_restoresAndSavesDefaults() throws Exception {
        Path prefsPath = testFolder.resolve("preferences.json");
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(prefsPath);
        for (String invalid : List.of("0", "-1", "null", "\"NaN\"", "\"Infinity\"", "1e309")) {
            for (String dimension : List.of("windowWidth", "windowHeight")) {
                Files.writeString(prefsPath, "{\"guiSettings\":{\"" + dimension + "\":" + invalid + "}}");
                UserPrefs prefs = new MainApp().initPrefs(storage);
                assertEquals(new UserPrefs(), prefs, dimension + "=" + invalid);
                assertEquals(prefs, storage.readUserPrefs().orElseThrow());
            }
        }
    }

    @Test
    public void initPrefs_validWindowDimensions_preservesSizeAndNegativeCoordinates() throws Exception {
        JsonUserPrefsStorage storage = new JsonUserPrefsStorage(testFolder.resolve("preferences.json"));
        for (GuiSettings settings : List.of(new GuiSettings(800.5, 600.25, -1500, -200),
                new GuiSettings(3072, 1632, 4000, 2000), new GuiSettings(5, 5, 0, 0))) {
            UserPrefs expected = new UserPrefs();
            expected.setGuiSettings(settings);
            storage.saveUserPrefs(expected);
            assertEquals(expected, new MainApp().initPrefs(storage));
            assertEquals(expected, storage.readUserPrefs().orElseThrow());
        }
    }
}
