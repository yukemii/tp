package seedu.address.logic.commands;

import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code AddCommand}.
 */
public class AddCommandIntegrationTest {

    private Model model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    }

    @Test
    public void execute_newPerson_success() {
        Person validPerson = new PersonBuilder().build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        assertCommandSuccess(new AddCommand(validPerson), model,
                String.format(AddCommand.MESSAGE_SUCCESS, Messages.formatSummary(validPerson)),
                expectedModel);
    }

    @Test
    public void execute_personWithSimilarContactDetails_successWithWarning() {
        Person firstPerson = model.getFilteredPersonList().get(0);
        Person validPerson = new PersonBuilder().withPhone(firstPerson.getPhone().value).build();

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.addPerson(validPerson);

        String expectedMessage = String.format(AddCommand.MESSAGE_SUCCESS,
                Messages.formatSummary(validPerson))
                + String.format(SimilarContactWarning.MESSAGE_WARNING,
                        "#1 " + firstPerson.getName() + " (same phone)");
        assertCommandSuccess(new AddCommand(validPerson), model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicatePerson_throwsCommandException() {
        Person personInList = model.getAddressBook().getPersonList().get(0);
        assertCommandFailure(new AddCommand(personInList), model, AddCommand.MESSAGE_DUPLICATE_PERSON
                + String.format(Messages.MESSAGE_EXISTING_ENGAGEMENT, Messages.formatSummary(personInList)));
    }

}
