package seedu.address.ui;

import java.nio.file.Path;
import java.time.LocalDate;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Person;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final int UPCOMING_WINDOW_DAYS = 6;

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;
    private CommandBox commandBox;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private PieChart eventScheduleChart;

    @FXML
    private Label pastLegend;

    @FXML
    private Label upcomingLegend;

    @FXML
    private Label laterLegend;

    @FXML
    private StackPane personListPanelPlaceholder;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());
        setHelpShortcut();

        helpWindow = new HelpWindow(new Stage(), primaryStage, this::focusCommandBox);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setHelpShortcut() {
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F1) {
                handleHelp();
                event.consume();
            }
        });
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        ObservableList<Person> persons = logic.getFilteredPersonList();
        personListPanel = new PersonListPanel(persons);
        personListPanel.setHeading(logic.getPersonListHeading());
        personListPanelPlaceholder.getChildren().add(personListPanel.getRoot());

        eventScheduleChart.setAnimated(false);
        eventScheduleChart.setLabelsVisible(false);
        persons.addListener((ListChangeListener<Person>) change -> updateEventScheduleChart(persons));
        updateEventScheduleChart(persons);

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());
    }

    /**
     * Updates the chart showing when engagements in the current view will take place.
     */
    private void updateEventScheduleChart(ObservableList<Person> persons) {
        LocalDate today = LocalDate.now();
        LocalDate upcomingEndDate = today.plusDays(UPCOMING_WINDOW_DAYS);
        int pastCount = 0;
        int upcomingCount = 0;
        int laterCount = 0;

        for (Person person : persons) {
            LocalDate eventDate = person.getEventDate().value;
            if (eventDate.isBefore(today)) {
                pastCount++;
            } else if (!eventDate.isAfter(upcomingEndDate)) {
                upcomingCount++;
            } else {
                laterCount++;
            }
        }

        eventScheduleChart.getData().setAll(
                new PieChart.Data("Past (" + pastCount + ")", pastCount),
                new PieChart.Data("Upcoming (" + upcomingCount + ")", upcomingCount),
                new PieChart.Data("Later (" + laterCount + ")", laterCount));
        pastLegend.setText("Past (" + pastCount + ")");
        upcomingLegend.setText("Upcoming (" + upcomingCount + ")");
        laterLegend.setText("Later (" + laterCount + ")");
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.hasVisibleWindowTitle(Screen.getScreens().stream().map(Screen::getVisualBounds).toList())) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        } else {
            Point2D fallback = guiSettings.getFallbackWindowPosition(Screen.getPrimary().getVisualBounds());
            primaryStage.setX(fallback.getX());
            primaryStage.setY(fallback.getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    /**
     * Returns keyboard focus to the command input field after Help is closed.
     */
    @FXML
    private void focusCommandBox() {
        Platform.runLater(() -> {
            if (!primaryStage.isShowing()) {
                return;
            }

            primaryStage.toFront();
            primaryStage.requestFocus();
            if (commandBox != null) {
                commandBox.requestFocus();
            }
        });
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            resultDisplay.setErrorToUser(e.getMessage());
            throw e;
        } finally {
            personListPanel.setHeading(logic.getPersonListHeading());
        }
    }
}
