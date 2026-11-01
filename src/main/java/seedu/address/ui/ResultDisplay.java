package seedu.address.ui;

import static java.util.Objects.requireNonNull;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import seedu.address.logic.Messages;

/**
 * A UI component that displays the result of a command execution.
 */
public class ResultDisplay extends UiPart<Region> {

    private static final String FXML = "ResultDisplay.fxml";
    private static final String WARNING_STYLE_CLASS = "warning";
    private static final String ERROR_STYLE_CLASS = "feedback-error";

    @FXML
    private TextArea resultDisplay;

    public ResultDisplay() {
        super(FXML);
    }

    /**
     * Displays {@code feedbackToUser}, highlighting it if it contains a warning so that the warning is not missed.
     */
    public void setFeedbackToUser(String feedbackToUser) {
        requireNonNull(feedbackToUser);
        if (Messages.hasWarning(feedbackToUser)) {
            showFeedback(feedbackToUser, WARNING_STYLE_CLASS);
        } else {
            showFeedback(feedbackToUser);
        }
    }

    /**
     * Displays {@code errorMessage} highlighted as an error.
     */
    public void setErrorToUser(String errorMessage) {
        showFeedback(requireNonNull(errorMessage), ERROR_STYLE_CLASS);
    }

    private void showFeedback(String text) {
        resultDisplay.setText(text);
        resultDisplay.getStyleClass().removeAll(WARNING_STYLE_CLASS, ERROR_STYLE_CLASS);
    }

    private void showFeedback(String text, String highlightStyleClass) {
        showFeedback(text);
        resultDisplay.getStyleClass().add(highlightStyleClass);
    }

}
