package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.StringUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.Email;
import seedu.address.model.person.EventDate;
import seedu.address.model.person.EventType;
import seedu.address.model.person.Name;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Stage;

/**
 * Contains utility methods used for parsing strings in the various *Parser classes.
 */
public class ParserUtil {

    public static final String MESSAGE_INVALID_INDEX = "Index must be a positive integer.";

    private static final Pattern PREFIX_PATTERN = Pattern.compile("(?:^|[ \\t])([a-zA-Z]+/)");

    /**
     * Throws a {@code ParseException} naming the first prefix in {@code args} that is not one of
     * {@code supportedPrefixes}, followed by the supported prefixes. A prefix is a sequence of letters followed by
     * {@code /} at the start of {@code args} or after a space or tab. Name relation markers such as {@code s/o}
     * are not prefixes.
     */
    public static void verifyNoUnsupportedPrefixes(String args, Prefix... supportedPrefixes)
            throws ParseException {
        requireNonNull(args);
        Set<String> supported = Stream.of(supportedPrefixes)
                .map(Prefix::getPrefix)
                .collect(Collectors.toSet());
        Matcher matcher = PREFIX_PATTERN.matcher(args);
        while (matcher.find()) {
            String prefix = matcher.group(1);
            if (CliSyntax.isNameRelationMarkerAt(args, matcher.start(1))
                    || CliSyntax.isTrailingNameRelationMarkerAt(args, matcher.start(1))) {
                continue;
            }
            if (!supported.contains(prefix)) {
                String supportedList = Stream.of(supportedPrefixes)
                        .map(Prefix::getPrefix)
                        .collect(Collectors.joining(" "));
                throw new ParseException(String.format(Messages.MESSAGE_UNSUPPORTED_PARAMETER, prefix) + " "
                        + String.format(Messages.MESSAGE_SUPPORTED_PARAMETERS, supportedList));
            }
        }
    }

    /**
     * Throws a {@code ParseException} naming the first of {@code requiredPrefixes} that is absent from
     * {@code argMultimap}, checked in the given order.
     */
    public static void verifyRequiredPrefixesPresent(ArgumentMultimap argMultimap, Prefix... requiredPrefixes)
            throws ParseException {
        requireNonNull(argMultimap);
        for (Prefix prefix : requiredPrefixes) {
            if (argMultimap.getValue(prefix).isEmpty()) {
                throw new ParseException(String.format(Messages.MESSAGE_MISSING_PARAMETER, prefix));
            }
        }
    }

    /**
     * Parses {@code oneBasedIndex} into an {@code Index} and returns it. Leading and trailing whitespaces will be
     * trimmed.
     * @throws ParseException if the specified index is invalid (not a non-zero unsigned integer).
     */
    public static Index parseIndex(String oneBasedIndex) throws ParseException {
        String trimmedIndex = oneBasedIndex.trim();
        if (!StringUtil.isNonZeroUnsignedInteger(trimmedIndex)) {
            throw new ParseException(MESSAGE_INVALID_INDEX);
        }
        return Index.fromOneBased(Integer.parseInt(trimmedIndex));
    }

    /**
     * Parses a {@code String name} into a {@code Name}.
     * Leading and trailing whitespaces will be trimmed, and consecutive spaces or tabs will be replaced by
     * one space.
     *
     * @throws ParseException if the given {@code name} is invalid.
     */
    public static Name parseName(String name) throws ParseException {
        requireNonNull(name);
        String normalizedName = StringUtil.normalizeWhitespace(name);
        if (!Name.isValidName(normalizedName)) {
            throw new ParseException(Name.MESSAGE_CONSTRAINTS);
        }
        return new Name(normalizedName);
    }

    /**
     * Parses a {@code String phone} into a {@code Phone}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code phone} is invalid.
     */
    public static Phone parsePhone(String phone) throws ParseException {
        requireNonNull(phone);
        String trimmedPhone = phone.trim();
        if (!Phone.isValidPhone(trimmedPhone)) {
            throw new ParseException(Phone.MESSAGE_CONSTRAINTS);
        }
        return new Phone(trimmedPhone);
    }

    /**
     * Parses a {@code String eventType} into an {@code EventType}.
     * Leading and trailing whitespaces will be trimmed, and consecutive spaces or tabs will be replaced by one space.
     *
     * @throws ParseException if the given {@code eventType} is invalid.
     */
    public static EventType parseEventType(String eventType) throws ParseException {
        requireNonNull(eventType);
        String normalizedEventType = StringUtil.normalizeWhitespace(eventType);
        if (!EventType.isValidEventType(normalizedEventType)) {
            throw new ParseException(EventType.MESSAGE_CONSTRAINTS);
        }
        return new EventType(normalizedEventType);
    }

    /**
     * Parses a {@code String email} into an {@code Email}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code email} is invalid.
     */
    public static Email parseEmail(String email) throws ParseException {
        requireNonNull(email);
        String trimmedEmail = email.trim();
        if (!Email.isValidEmail(trimmedEmail)) {
            throw new ParseException(Email.MESSAGE_CONSTRAINTS);
        }
        return new Email(trimmedEmail);
    }

    /**
     * Parses a {@code String eventDate} into an {@code EventDate}.
     * Leading and trailing whitespaces will be trimmed.
     *
     * @throws ParseException if the given {@code eventDate} is invalid.
     */
    public static EventDate parseEventDate(String eventDate) throws ParseException {
        requireNonNull(eventDate);
        String trimmedEventDate = eventDate.trim();
        if (!EventDate.isValidEventDate(trimmedEventDate)) {
            throw new ParseException(EventDate.MESSAGE_CONSTRAINTS);
        }
        return new EventDate(trimmedEventDate);
    }

    /**
     * Parses a {@code String stage} into a {@code Stage}.
     * Leading and trailing whitespaces will be trimmed, and matching is case-insensitive.
     *
     * @throws ParseException if the given {@code stage} is invalid.
     */
    public static Stage parseStage(String stage) throws ParseException {
        requireNonNull(stage);
        try {
            return Stage.fromString(stage);
        } catch (IllegalArgumentException exception) {
            throw new ParseException(Stage.MESSAGE_CONSTRAINTS);
        }
    }
}
