package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_SUPPORTED_PARAMETERS;
import static seedu.address.logic.Messages.MESSAGE_UNSUPPORTED_PARAMETER;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
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
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.STAGE_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.STAGE_DESC_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EVENT_DATE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EVENT_TYPE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_THIRD_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.EditCommand.EditPersonDescriptor;
import seedu.address.model.person.Email;
import seedu.address.model.person.EventDate;
import seedu.address.model.person.EventType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;
import seedu.address.testutil.EditPersonDescriptorBuilder;

public class EditCommandParserTest {
    private static final String MESSAGE_INVALID_FORMAT =
            String.format(MESSAGE_INVALID_COMMAND_FORMAT, EditCommand.MESSAGE_USAGE);

    private EditCommandParser parser = new EditCommandParser();

    @Test
    public void parse_missingParts_failure() {
        assertParseFailure(parser, VALID_NAME_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1", EditCommand.MESSAGE_NOT_EDITED);
        assertParseFailure(parser, "", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_invalidPreamble_failure() {
        assertParseFailure(parser, "-5" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "0" + NAME_DESC_AMY, MESSAGE_INVALID_FORMAT);
        assertParseFailure(parser, "1 some random string", MESSAGE_INVALID_FORMAT);
    }

    @Test
    public void parse_unsupportedPrefix_failure() {
        // unknown prefix instead of a field
        assertParseFailure(parser, "1 i/ string", unsupportedParameterMessage("i/"));

        // unknown prefix after a valid field
        assertParseFailure(parser, "1" + NAME_DESC_AMY + " x/abc",
                unsupportedParameterMessage("x/"));
    }

    @Test
    public void parse_invalidValue_failure() {
        assertParseFailure(parser, "1" + INVALID_NAME_DESC, Name.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC, Phone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_EMAIL_DESC, Email.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_EVENT_TYPE_DESC, EventType.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_EVENT_DATE_DESC, EventDate.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_STAGE_DESC, Stage.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "1" + INVALID_PHONE_DESC + EMAIL_DESC_AMY, Phone.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_allFieldsSpecified_success() {
        Index targetIndex = INDEX_SECOND_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY
                + EVENT_TYPE_DESC_AMY + EVENT_DATE_DESC_AMY + STAGE_DESC_AMY + NAME_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY)
                .withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_AMY).withEventType(VALID_EVENT_TYPE_AMY)
                .withEventDate(VALID_EVENT_DATE_AMY).withStage("Booked").build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_someFieldsSpecified_success() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_BOB + EMAIL_DESC_AMY;
        EditPersonDescriptor descriptor = new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_BOB)
                .withEmail(VALID_EMAIL_AMY).build();

        assertParseSuccess(parser, userInput, new EditCommand(targetIndex, descriptor));
    }

    @Test
    public void parse_oneFieldSpecified_success() {
        Index targetIndex = INDEX_THIRD_PERSON;

        assertParseSuccess(parser, targetIndex.getOneBased() + NAME_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withName(VALID_NAME_AMY).build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + PHONE_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withPhone(VALID_PHONE_AMY).build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + EMAIL_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withEmail(VALID_EMAIL_AMY).build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + EVENT_TYPE_DESC_AMY,
                new EditCommand(targetIndex,
                        new EditPersonDescriptorBuilder().withEventType(VALID_EVENT_TYPE_AMY).build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + EVENT_DATE_DESC_AMY,
                new EditCommand(targetIndex,
                        new EditPersonDescriptorBuilder().withEventDate(VALID_EVENT_DATE_AMY).build()));
        assertParseSuccess(parser, targetIndex.getOneBased() + STAGE_DESC_AMY,
                new EditCommand(targetIndex, new EditPersonDescriptorBuilder().withStage("Booked").build()));
    }

    @Test
    public void parse_multipleRepeatedFields_failure() {
        Index targetIndex = INDEX_FIRST_PERSON;
        String userInput = targetIndex.getOneBased() + PHONE_DESC_AMY + EVENT_TYPE_DESC_AMY + EMAIL_DESC_AMY
                + PHONE_DESC_BOB + EVENT_TYPE_DESC_BOB + EMAIL_DESC_AMY + EVENT_DATE_DESC_AMY + EVENT_DATE_DESC_BOB
                + STAGE_DESC_AMY + STAGE_DESC_BOB;

        assertParseFailure(parser, userInput, Messages.getErrorMessageForDuplicatePrefixes(
                PREFIX_PHONE, PREFIX_EMAIL, PREFIX_EVENT_TYPE, PREFIX_EVENT_DATE, PREFIX_STAGE));
    }

    /**
     * Returns the expected error message when {@code prefix} is the first unsupported prefix.
     */
    private static String unsupportedParameterMessage(String prefix) {
        return String.format(MESSAGE_UNSUPPORTED_PARAMETER, prefix) + " "
                + String.format(MESSAGE_SUPPORTED_PARAMETERS, "n/ p/ e/ t/ d/ s/");
    }
}
