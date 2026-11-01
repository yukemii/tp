package seedu.address.model.person;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests whether a person's selected field contains the search value.
 * Matching is case-insensitive; stages match the complete value.
 */
public class PersonSearchPredicate implements Predicate<Person> {

    /** Represents the person field to search. */
    public enum Field {
        NAME,
        PHONE,
        EMAIL,
        EVENT_TYPE,
        STAGE
    }

    private final Field field;
    private final String searchValue;

    /**
     * Creates a predicate for a selected person field.
     *
     * @param field the person field to search
     * @param searchValue the case-insensitive value to search for
     */
    public PersonSearchPredicate(Field field, String searchValue) {
        this.field = requireNonNull(field);
        this.searchValue = requireNonNull(searchValue).trim();
    }

    @Override
    public boolean test(Person person) {
        String actualValue = switch (field) {
            case NAME -> person.getName().fullName;
            case PHONE -> person.getPhone().value;
            case EMAIL -> person.getEmail().value;
            case EVENT_TYPE -> person.getEventType().value;
            case STAGE -> person.getStage().toString();
        };

        if (field == Field.STAGE) {
            return actualValue.equalsIgnoreCase(searchValue);
        }

        // Compare regions directly to avoid context-sensitive lowercase conversions changing a substring.
        for (int i = 0; i <= actualValue.length() - searchValue.length(); i++) {
            if (actualValue.regionMatches(true, i, searchValue, 0, searchValue.length())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof PersonSearchPredicate otherPredicate)) {
            return false;
        }

        return field == otherPredicate.field
                && searchValue.equalsIgnoreCase(otherPredicate.searchValue);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("field", field)
                .add("searchValue", searchValue)
                .toString();
    }
}
