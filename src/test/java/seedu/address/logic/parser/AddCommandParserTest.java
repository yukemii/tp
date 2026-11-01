package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_MISSING_PARAMETER;
import static seedu.address.logic.Messages.MESSAGE_SUPPORTED_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PARAMETER;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EVENT_DATE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EVENT_DATE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.EVENT_TYPE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EVENT_TYPE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EMAIL_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EVENT_DATE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_EVENT_TYPE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_NAME_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_PHONE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.INVALID_STAGE_DESC;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_NON_EMPTY;
import static seedu.address.logic.commands.CommandTestUtil.PREAMBLE_WHITESPACE;
import static seedu.address.logic.commands.CommandTestUtil.STAGE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STAGE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EVENT_TYPE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalPersons.BOB;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.model.person.Email;
import seedu.address.model.person.EventDate;
import seedu.address.model.person.EventType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.testutil.PersonBuilder;

public class AddCommandParserTest {
    private AddCommandParser parser = new AddCommandParser();

    @Test
    public void parse_allFieldsPresent_success() {
        assertParseSuccess(parser, PREAMBLE_WHITESPACE + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB, new AddCommand(BOB));
    }

    @Test
    public void parse_stagePresent_success() {
        String userInput = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB + STAGE_DESC_AMY;
        assertParseSuccess(parser, userInput, new AddCommand(new PersonBuilder(BOB).withStage("Booked").build()));
    }

    @Test
    public void parse_nameWithRelationMarker_success() {
        String userInput = " " + PREFIX_NAME + "Raj s/o Kumar" + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB + STAGE_DESC_AMY;
        assertParseSuccess(parser, userInput, new AddCommand(new PersonBuilder(BOB)
                .withName("Raj s/o Kumar").withStage("Booked").build()));

        // a name ending with a marker at the end of the command keeps the marker
        userInput = PHONE_DESC_BOB + EMAIL_DESC_BOB + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB
                + " " + PREFIX_NAME + "Ravi s/o";
        assertParseSuccess(parser, userInput,
                new AddCommand(new PersonBuilder(BOB).withName("Ravi s/o").build()));
    }

    @Test
    public void parse_trailingMarkerAfterOtherField_failure() {
        // s/o after the date is still read as the stage "o"
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + EVENT_TYPE_DESC_BOB
                + EVENT_DATE_DESC_BOB + " " + PREFIX_STAGE + "o", Stage.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_repeatedValue_failure() {
        String validPerson = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB;

        assertParseFailure(parser, NAME_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_NAME));
        assertParseFailure(parser, PHONE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_PHONE));
        assertParseFailure(parser, EMAIL_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EMAIL));
        assertParseFailure(parser, EVENT_TYPE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EVENT_TYPE));
        assertParseFailure(parser, EVENT_DATE_DESC_AMY + validPerson,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_EVENT_DATE));
        assertParseFailure(parser, STAGE_DESC_AMY + validPerson + STAGE_DESC_BOB,
                Messages.getErrorMessageForDuplicatePrefixes(PREFIX_STAGE));
    }

    @Test
    public void parse_compulsoryFieldMissing_failure() {
        // missing name prefix: the name becomes the preamble
        assertParseFailure(parser, VALID_NAME_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));

        // missing other prefixes: the missing prefix is named
        assertParseFailure(parser, NAME_DESC_BOB + VALID_PHONE_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB,
                missingParameterMessage(PREFIX_PHONE));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + VALID_EMAIL_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB,
                missingParameterMessage(PREFIX_EMAIL));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + VALID_EVENT_TYPE_BOB + EVENT_DATE_DESC_BOB,
                missingParameterMessage(PREFIX_EVENT_TYPE));
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + EVENT_TYPE_DESC_BOB,
                missingParameterMessage(PREFIX_EVENT_DATE));

        // no arguments at all: the first required prefix is named
        assertParseFailure(parser, "", missingParameterMessage(PREFIX_NAME));
        assertParseFailure(parser, PREAMBLE_WHITESPACE, missingParameterMessage(PREFIX_NAME));

        // several prefixes missing: the first one in command-format order is named
        assertParseFailure(parser, NAME_DESC_BOB + EVENT_TYPE_DESC_BOB,
                missingParameterMessage(PREFIX_PHONE));
    }

    @Test
    public void parse_unsupportedPrefix_failure() {
        String validInput = NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB + EVENT_TYPE_DESC_BOB
                + EVENT_DATE_DESC_BOB;

        // unknown prefix after a value
        assertParseFailure(parser, validInput + " x/oops",
                unsupportedParameterMessage("x/"));

        // prefixes are case-sensitive
        assertParseFailure(parser, " N/Bob" + PHONE_DESC_BOB + EMAIL_DESC_BOB + EVENT_TYPE_DESC_BOB
                + EVENT_DATE_DESC_BOB, unsupportedParameterMessage("N/"));

        // reported before a missing required prefix
        assertParseFailure(parser, NAME_DESC_BOB + " a/address",
                unsupportedParameterMessage("a/"));
    }

    /**
     * Returns the expected error message when {@code prefix} is the first unsupported prefix.
     */
    private static String unsupportedParameterMessage(String prefix) {
        return String.format(MESSAGE_UNSUPPORTED_PARAMETER, prefix) + " "
                + String.format(MESSAGE_SUPPORTED_PARAMETERS, "n/ p/ e/ t/ d/ s/");
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, INVALID_NAME_DESC + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + INVALID_PHONE_DESC + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + INVALID_EMAIL_DESC
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + INVALID_EVENT_TYPE_DESC + EVENT_DATE_DESC_BOB, EventType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + INVALID_EVENT_DATE_DESC, EventDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB + INVALID_STAGE_DESC, Stage.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, PREAMBLE_NON_EMPTY + NAME_DESC_BOB + PHONE_DESC_BOB + EMAIL_DESC_BOB
                        + EVENT_TYPE_DESC_BOB + EVENT_DATE_DESC_BOB,
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
    }

    /**
     * Returns the expected error message when {@code prefix} is the first missing required prefix.
     */
    private static String missingParameterMessage(Prefix prefix) {
        return String.format(MESSAGE_MISSING_PARAMETER, prefix) + "\n" + AddCommand.MESSAGE_USAGE;
    }
}
