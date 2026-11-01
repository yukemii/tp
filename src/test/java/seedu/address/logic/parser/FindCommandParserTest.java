package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_SUPPORTED_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PARAMETER;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.PersonSearchPredicate;
import seedu.address.model.person.Stage;

public class FindCommandParserTest {

    private static final String INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE);
    private final FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_unsupportedPrefix_throwsParseException() {
        assertParseFailure(parser, " n/Alice d/2026-12-20", unsupportedParameterMessage("d/"));
        assertParseFailure(parser, " n/Alice x/oops", unsupportedParameterMessage("x/"));
        assertParseFailure(parser, " x/oops n/Alice", unsupportedParameterMessage("x/"));
        assertParseFailure(parser, " n/Alice\tx/oops", unsupportedParameterMessage("x/"));
    }

    @Test
    public void parse_name_success() throws Exception {
        FindCommand expected = new FindCommand(
                new PersonSearchPredicate(
                        PersonSearchPredicate.Field.NAME,
                        "Alice Pauline"));

        assertParseSuccess(parser, " n/Alice Pauline", expected);
    }

    @Test
    public void parse_nameWithRelationMarker_success() throws Exception {
        assertParseSuccess(parser, " n/Raj s/o Kumar", new FindCommand(
                new PersonSearchPredicate(PersonSearchPredicate.Field.NAME, "Raj s/o Kumar")));
        // a partial search may end with the marker
        assertParseSuccess(parser, " n/Raj s/o", new FindCommand(
                new PersonSearchPredicate(PersonSearchPredicate.Field.NAME, "Raj s/o")));
        assertParseSuccess(parser, " n/Muthu A/L ", new FindCommand(
                new PersonSearchPredicate(PersonSearchPredicate.Field.NAME, "Muthu A/L")));
    }

    @Test
    public void parse_phone_success() throws Exception {
        FindCommand expected = new FindCommand(
                new PersonSearchPredicate(
                        PersonSearchPredicate.Field.PHONE,
                        "+6591234567"));

        assertParseSuccess(parser, " p/+65 9123-4567", expected);

    }

    @Test
    public void parse_email_success() throws Exception {
        FindCommand expected = new FindCommand(
                new PersonSearchPredicate(
                        PersonSearchPredicate.Field.EMAIL,
                        "alice@example.com"));

        assertParseSuccess(parser, " e/alice@example.com", expected);
    }

    @Test
    public void parse_eventType_success() throws Exception {
        FindCommand expected = new FindCommand(
                new PersonSearchPredicate(
                        PersonSearchPredicate.Field.EVENT_TYPE,
                        "Wedding"));

        assertParseSuccess(parser, " t/Wedding", expected);
    }

    @Test
    public void parse_multipleFields_throwsParseException() {
        assertParseFailure(parser, " n/Alice p/91234567", INVALID_FORMAT);
        assertParseFailure(parser, " n/Alice s/Booked", INVALID_FORMAT);
        assertParseFailure(parser, " s/Booked t/Wedding", INVALID_FORMAT);
    }

    @Test
    public void parse_emptyValue_throwsParseException() {
        for (String prefix : new String[]{"n/", "p/", "e/", "t/", "s/"}) {
            for (String value : new String[]{"", " ", "\t"}) {
                assertParseFailure(parser, " " + prefix + value, INVALID_FORMAT);
            }
        }
        assertParseFailure(parser, "p/ - ", INVALID_FORMAT);
        assertParseFailure(parser, " p/---", INVALID_FORMAT);
    }

    @Test
    public void parse_duplicateField_throwsParseException() {
        assertParseFailure(parser, "n/Alice n/Bob", INVALID_FORMAT);
        assertParseFailure(parser, " s/Booked s/Enquiry",
                Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_STAGE));
    }

    @Test
    public void parse_missingPrefix_throwsParseException() {
        assertParseFailure(parser, "Alice", INVALID_FORMAT);
    }

    @Test
    public void parse_completeStageNames_caseInsensitiveSuccess() {
        for (Stage stage : Stage.values()) {
            String value = stage.toString().toLowerCase(Locale.ROOT);
            assertParseSuccess(parser, " s/ " + value + " ", new FindCommand(
                    new PersonSearchPredicate(PersonSearchPredicate.Field.STAGE, stage.toString())));
        }
    }

    @Test
    public void parse_invalidStage_rejectsPartialAndUnknownValues() {
        for (String value : new String[]{"Book", "Completed", "Unknown", "Booked extra"}) {
            assertParseFailure(parser, " s/" + value, Stage.MESSAGE_CONSTRAINTS);
        }
    }

    /**
     * Returns the expected error message when {@code prefix} is the first unsupported prefix.
     */
    private static String unsupportedParameterMessage(String prefix) {
        return String.format(MESSAGE_UNSUPPORTED_PARAMETER, prefix) + " "
                + String.format(MESSAGE_SUPPORTED_PARAMETERS, "n/ p/ e/ t/ s/");
    }
}
