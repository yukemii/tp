package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.SimilarContactWarning;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class MessagesTest {

    @Test
    public void hasWarning() {
        assertThrows(NullPointerException.class, () -> Messages.hasWarning(null));

        // a warning on its own line -> returns true
        assertTrue(Messages.hasWarning("New engagement added.\nWarning: possible typo."));
        assertTrue(Messages.hasWarning(String.format(SimilarContactWarning.MESSAGE_WARNING, "#1 Alice (same phone)")));

        // feedback that starts with a warning -> returns true
        assertTrue(Messages.hasWarning("Warning: something"));

        // no warning, or "Warning" only inside a line -> returns false
        assertFalse(Messages.hasWarning(""));
        assertFalse(Messages.hasWarning("New engagement added: Alice | Wedding | 2026-12-20 | Enquiry."));
        assertFalse(Messages.hasWarning("Name: Warning: Tan"));
    }

    @Test
    public void formatExistingEngagement() {
        assertThrows(NullPointerException.class, () -> Messages.formatExistingEngagement(null, ALICE));
        assertThrows(NullPointerException.class, () -> Messages.formatExistingEngagement(List.of(ALICE), null));

        // same engagement in a different stage -> the stored engagement is described
        Person aliceDelivered = new PersonBuilder(ALICE).withStage("Delivered").build();
        assertEquals(String.format(Messages.MESSAGE_EXISTING_ENGAGEMENT, Messages.formatSummary(ALICE)),
                Messages.formatExistingEngagement(List.of(BENSON, ALICE), aliceDelivered));

        // no engagement with the same identity -> empty string
        assertEquals("", Messages.formatExistingEngagement(List.of(BENSON), ALICE));
    }
}
