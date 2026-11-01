package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_DATE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EVENT_TYPE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STAGE;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;

/**
 * Adds a contact (one client engagement) to ShutterLink.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a contact for one client engagement. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_EVENT_TYPE + "EVENT_TYPE "
            + PREFIX_EVENT_DATE + "EVENT_DATE "
            + "[" + PREFIX_STAGE + "STAGE]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_EVENT_TYPE + "Wedding "
            + PREFIX_EVENT_DATE + "2026-12-20 "
            + PREFIX_STAGE + "Booked";

    public static final String MESSAGE_SUCCESS = "New engagement added: %1$s.";
    public static final String MESSAGE_DUPLICATE_PERSON = "An engagement with the same name, phone, email, "
            + "event type and event date already exists. No engagement was added.";

    private final Person toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON
                    + Messages.formatExistingEngagement(model.getAddressBook().getPersonList(), toAdd));
        }

        model.addPerson(toAdd);
        // Show all contacts so that the displayed list contains every contact, and the indexes in the warning
        // match what the user sees.
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        String warning = SimilarContactWarning.generate(model.getFilteredPersonList(), toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.formatSummary(toAdd)) + warning);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
