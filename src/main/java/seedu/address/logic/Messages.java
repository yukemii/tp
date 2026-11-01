package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX =
            "No engagement at that index in the displayed list.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d engagement(s) listed!";
    public static final String MESSAGE_UNSUPPORTED_PARAMETER = "Unsupported parameter: %1$s.";
    public static final String MESSAGE_SUPPORTED_PARAMETERS = "This command accepts: %1$s";
    public static final String MESSAGE_MISSING_PARAMETER = "Missing required parameter: %1$s.";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";
    public static final String MESSAGE_EXISTING_ENGAGEMENT = "\nExisting engagement: %1$s.";
    public static final String WARNING_PREFIX = "Warning: ";

    /**
     * Returns true if {@code feedback} contains a line starting with {@link #WARNING_PREFIX}.
     */
    public static boolean hasWarning(String feedback) {
        requireNonNull(feedback);
        return feedback.startsWith(WARNING_PREFIX) || feedback.contains("\n" + WARNING_PREFIX);
    }

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Returns a one-line summary of the {@code person} for result messages, in the form
     * {@code NAME | EVENT_TYPE | EVENT_DATE | STAGE}.
     */
    public static String formatSummary(Person person) {
        return person.getName() + " | " + person.getEventType() + " | " + person.getEventDate()
                + " | " + person.getStage();
    }

    /**
     * Returns a line summarizing the engagement in {@code persons} that has the same identity as {@code person},
     * so that the user can find the engagement that blocked an add or edit, or an empty string if there is none.
     */
    public static String formatExistingEngagement(List<Person> persons, Person person) {
        requireNonNull(persons);
        requireNonNull(person);
        return persons.stream()
                .filter(person::isSameEngagement)
                .findFirst()
                .map(existing -> String.format(MESSAGE_EXISTING_ENGAGEMENT, formatSummary(existing)))
                .orElse("");
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Event Type: ")
                .append(person.getEventType())
                .append("; Event Date: ")
                .append(person.getEventDate())
                .append("; Stage: ")
                .append(person.getStage());
        return builder.toString();
    }

}
