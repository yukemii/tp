package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_PERSONS_LISTED_OVERVIEW;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.FindCommandParser;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.PersonSearchPredicate;
import seedu.address.testutil.PersonBuilder;

/**
 * Contains integration tests (interaction with the Model) for {@code FindCommand}.
 */
public class FindCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
    private Model expectedModel = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_stageSearch_replacesPriorViewAndListRestoresAllRecords() throws Exception {
        Model stageModel = new ModelManager();
        Person booked = new PersonBuilder().withName("Booked Client").withStage("Booked").build();
        Person delivered = new PersonBuilder().withName("Delivered Client").withStage("Delivered").build();
        stageModel.addPerson(booked);
        stageModel.addPerson(delivered);
        stageModel.updateFilteredPersonList(person -> person.equals(delivered));
        FindCommand command = new FindCommandParser().parse(" s/booked");
        assertFalse(command.modifiesAddressBook());
        command.execute(stageModel);
        assertEquals(List.of(booked), stageModel.getFilteredPersonList());
        new FindCommandParser().parse(" s/Awaiting Payment").execute(stageModel);
        assertEquals(List.of(), stageModel.getFilteredPersonList());
        new ListCommand().execute(stageModel);
        assertEquals(List.of(booked, delivered), stageModel.getFilteredPersonList());
    }

    @Test
    public void equals() {
        PersonSearchPredicate firstPredicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Alice Pauline");

        PersonSearchPredicate secondPredicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Benson Meier");

        FindCommand findFirstCommand = new FindCommand(firstPredicate);
        FindCommand findSecondCommand = new FindCommand(secondPredicate);

        assertTrue(findFirstCommand.equals(findFirstCommand));

        FindCommand findFirstCommandCopy = new FindCommand(firstPredicate);
        assertTrue(findFirstCommand.equals(findFirstCommandCopy));

        assertFalse(findFirstCommand.equals(1));
        assertFalse(findFirstCommand.equals(null));
        assertFalse(findFirstCommand.equals(findSecondCommand));
    }

    @Test
    public void execute_noMatch_noPersonFound() {
        PersonSearchPredicate predicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Unknown Person");

        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);

        assertCommandSuccess(
                command,
                model,
                "No matching engagements found.",
                expectedModel);

        assertEquals(List.of(), model.getFilteredPersonList());
    }


    @Test
    public void execute_exactName_onePersonFound() {
        PersonSearchPredicate predicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Carl Kurz");

        FindCommand command = new FindCommand(predicate);
        expectedModel.updateFilteredPersonList(predicate);

        String expectedMessage = String.format(MESSAGE_PERSONS_LISTED_OVERVIEW, 1);

        assertCommandSuccess(command, model, expectedMessage, expectedModel);
        assertEquals(List.of(CARL), model.getFilteredPersonList());
    }

    @Test
    public void execute_partialName_matchesMultipleClients() {
        Person jonathanWong = new PersonBuilder()
                .withName("Jonathan Wong")
                .build();

        Person jonathanTan = new PersonBuilder()
                .withName("Jonathan Tan")
                .build();

        Person alicePauline = new PersonBuilder()
                .withName("Alice Pauline")
                .build();

        AddressBook addressBook = new AddressBook();
        addressBook.addPerson(jonathanWong);
        addressBook.addPerson(jonathanTan);
        addressBook.addPerson(alicePauline);

        Model testModel = new ModelManager(addressBook, new UserPrefs());

        PersonSearchPredicate predicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Jonathan");

        FindCommand command = new FindCommand(predicate);
        command.execute(testModel);

        assertEquals(
                List.of(jonathanWong, jonathanTan),
                testModel.getFilteredPersonList());
    }

    @Test
    public void toStringMethod() {
        PersonSearchPredicate predicate = new PersonSearchPredicate(
                PersonSearchPredicate.Field.NAME,
                "Alice Pauline");

        FindCommand findCommand = new FindCommand(predicate);

        String expected = FindCommand.class.getCanonicalName()
                + "{predicate=" + predicate + "}";

        assertEquals(expected, findCommand.toString());
    }

}
