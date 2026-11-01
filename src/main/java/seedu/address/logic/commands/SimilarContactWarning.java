package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.model.person.Person;

/**
 * Generates a warning about existing contacts whose phone number or email matches a given contact,
 * which may indicate a typo.
 */
public class SimilarContactWarning {

    public static final int MAX_CONTACTS_LISTED = 3;
    public static final String MESSAGE_WARNING = "\n" + Messages.WARNING_PREFIX
            + "possible typo. Shares details with %1$s."
            + "\nIf this is a mistake, fix the wrong engagement with edit INDEX or delete INDEX.";
    public static final String MESSAGE_MORE_CONTACTS = " and %1$d more";

    private SimilarContactWarning() {}

    /**
     * Returns a warning that lists the contacts in {@code displayedPersons} with similar contact details to
     * {@code target}, using their displayed indexes, or an empty string if there are none.
     * A client with several records is listed once, at the index of its first record.
     */
    public static String generate(List<Person> displayedPersons, Person target) {
        requireNonNull(displayedPersons);
        requireNonNull(target);

        List<Person> listedClients = new ArrayList<>();
        List<String> similarContacts = new ArrayList<>();
        for (int i = 0; i < displayedPersons.size(); i++) {
            Person person = displayedPersons.get(i);
            boolean isListedClient = listedClients.stream().anyMatch(person::isSameClient);
            if (person.hasSimilarContactDetails(target) && !isListedClient) {
                listedClients.add(person);
                similarContacts.add(describe(Index.fromZeroBased(i), person, target));
            }
        }

        if (similarContacts.isEmpty()) {
            return "";
        }

        String listedContacts = String.join(", ",
                similarContacts.subList(0, Math.min(MAX_CONTACTS_LISTED, similarContacts.size())));
        if (similarContacts.size() > MAX_CONTACTS_LISTED) {
            int unlistedCount = similarContacts.size() - MAX_CONTACTS_LISTED;
            listedContacts += String.format(MESSAGE_MORE_CONTACTS, unlistedCount);
        }
        return String.format(MESSAGE_WARNING, listedContacts);
    }

    /**
     * Returns a description of {@code person} at {@code index}, stating which contact details it shares with
     * {@code target}, e.g. {@code #3 John Tan (same phone)}.
     */
    private static String describe(Index index, Person person, Person target) {
        String sharedDetails;
        if (person.hasSamePhone(target) && person.hasSameEmail(target)) {
            sharedDetails = "same phone and email";
        } else if (person.hasSamePhone(target)) {
            sharedDetails = "same phone";
        } else {
            sharedDetails = "same email";
        }
        return "#" + index.getOneBased() + " " + person.getName() + " (" + sharedDetails + ")";
    }
}
