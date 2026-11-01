package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.PersonSearchPredicate;
import seedu.address.model.person.Phone;

/**
 * Parses input arguments and creates a new {@link FindCommand} object.
 */
public class FindCommandParser implements Parser<FindCommand> {
    private static final List<Prefix> SEARCH_PREFIXES = List.of(
            PREFIX_NAME,
            PREFIX_PHONE,
            PREFIX_EMAIL,
            PREFIX_EVENT_TYPE,
            PREFIX_STAGE);

    @Override
    public FindCommand parse(String args) throws ParseException {
        ParserUtil.verifyNoUnsupportedPrefixes(args,
                PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_EVENT_TYPE, PREFIX_STAGE);
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(
                args,
                PREFIX_NAME,
                PREFIX_PHONE,
                PREFIX_EMAIL,
                PREFIX_EVENT_TYPE,
                PREFIX_STAGE);

        argMultimap.verifyNoDuplicatePrefixesFor(
                PREFIX_NAME,
                PREFIX_PHONE,
                PREFIX_EMAIL,
                PREFIX_EVENT_TYPE,
                PREFIX_STAGE);

        List<Prefix> suppliedPrefixes = SEARCH_PREFIXES.stream()
                .filter(prefix -> argMultimap.getValue(prefix).isPresent())
                .toList();

        boolean hasInvalidPreamble = !argMultimap.getPreamble().trim().isEmpty();

        if (suppliedPrefixes.size() != 1 || hasInvalidPreamble) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        Prefix suppliedPrefix = suppliedPrefixes.get(0);
        String searchValue = argMultimap.getValue(suppliedPrefix)
                .orElse("")
                .trim();

        if (suppliedPrefix.equals(PREFIX_PHONE)) {
            searchValue = Phone.removeSeparators(searchValue);
        }
        if (searchValue.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        PersonSearchPredicate.Field field;

        if (suppliedPrefix.equals(PREFIX_NAME)) {
            field = PersonSearchPredicate.Field.NAME;
        } else if (suppliedPrefix.equals(PREFIX_PHONE)) {
            field = PersonSearchPredicate.Field.PHONE;
        } else if (suppliedPrefix.equals(PREFIX_EMAIL)) {
            field = PersonSearchPredicate.Field.EMAIL;
        } else if (suppliedPrefix.equals(PREFIX_STAGE)) {
            field = PersonSearchPredicate.Field.STAGE;
            searchValue = ParserUtil.parseStage(searchValue).toString();
        } else {
            field = PersonSearchPredicate.Field.EVENT_TYPE;
        }

        return new FindCommand(new PersonSearchPredicate(field, searchValue));
    }
}
