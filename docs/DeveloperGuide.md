---
layout: page
title: Developer Guide
---
* Table of Contents
{:toc}

--------------------------------------------------------------------------------------------------------------------

## **Acknowledgements**

This project (ShutterLink) is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org).
It uses JavaFX for the UI, Jackson for JSON serialization, JUnit 5 for testing, and Gradle for builds.

--------------------------------------------------------------------------------------------------------------------

## **Setting up, getting started**

Refer to the guide [_Setting up and getting started_](SettingUp.md).

--------------------------------------------------------------------------------------------------------------------

## **Design**

<div markdown="span" class="alert alert-primary">

:bulb: **Tip:** The `.puml` files used to create diagrams are in `docs/diagrams`. Refer to the [_PlantUML Tutorial_ at se-edu/guides](https://se-education.org/guides/tutorials/plantUml.html) to learn how to create and edit diagrams.
</div>

### Architecture

The application is called **ShutterLink**. It helps photographers manage client contacts and their associated photography events.

<img src="images/ArchitectureDiagram.png" width="280" />

The ***Architecture Diagram*** above explains the high-level design of ShutterLink. Solid arrows show runtime
collaboration, while dotted arrows show dependencies on shared types or external actors and files.

* [**`UI`**](#ui-component): Displays the application window and engagement information.
* [**`Logic`**](#logic-component): Parses and executes user commands.
* [**`Model`**](#model-component): Stores engagements and their event details in memory.
* [**`Storage`**](#storage-component): Saves and loads engagement data from the local JSON file.

[**`Commons`**](#common-classes) contains utility classes shared by multiple components.


**Main components of the architecture**

**`Main`** and **`MainApp`** initialise the application, connect the components, and handle application shutdown.

The `Logic` component receives commands from the UI. It parses commands such as adding, editing, deleting, listing,
and finding engagements, then executes them using the `Model`. The `Model` maintains a filtered list wrapped by a
sorted list, which determines the engagements displayed by the UI.

Each engagement is represented by a `Person` object containing the client's name, phone number, email address,
event type, event date, and engagement stage.


**How the architecture components interact with each other**

The *Sequence Diagram* below shows how the components interact when the user issues `delete 1`. It includes the
successful JSON save and the subsequent refresh of the list heading. The UI observes the model's displayed list, so
individual list-change notifications are omitted.

<img src="images/ArchitectureSequenceDiagram.png" width="574" />

Each of the four main components (also shown in the diagram above),

* defines its *API* in an `interface` with the same name as the Component.
* provides its functionality through a concrete `{Component Name}Manager` class that implements the corresponding API interface.

For example, the `Logic` component defines its API in `Logic.java` and implements it in `LogicManager.java`. Other components interact with a component through its interface rather than its concrete class, preventing them from coupling to that component's implementation, as illustrated in the following partial class diagram.

<img src="images/ComponentManagers.png" width="300" />

The sections below give more details of each component.

### UI component

The **API** of this component is specified in
[`Ui.java`](https://github.com/AY2627S1-CS2103T-T08-3/tp/blob/master/src/main/java/seedu/address/ui/Ui.java).

![Structure of the UI Component](images/UiClassDiagram.png)

The UI keeps a reference to the `Logic` interface, which it uses to execute user commands. It also depends on selected
`Model` classes, such as `Person`, when displaying engagement data.

The UI is composed of multiple UI parts. All these parts, including `MainWindow`, inherit from the abstract `UiPart`
class, which provides common functionality for loading and managing their corresponding FXML files.

The UI is implemented using JavaFX. Its main parts include:

* `MainWindow`, which contains the main application layout and event schedule chart.
* `CommandBox`, which accepts user commands.
* `ResultDisplay`, which displays feedback messages.
* `PersonListPanel`, which displays the filtered and sorted list of engagements.
* `PersonCard`, which displays an individual engagement.
* `StatusBarFooter`, which displays the data file location.

The layouts of these UI parts are defined in FXML files under `src/main/resources/view`. For example,
`PersonListCard.fxml` defines the layout for an individual engagement card.

The `UI` component,

* keeps a reference to `Logic` and sends user commands through it;
* displays command results returned by `Logic`;
* observes the filtered and sorted engagement list exposed through `Logic`;
* depends on selected `Model` classes when displaying engagement data;
* updates the displayed engagements and event schedule chart whenever the observed list changes.

`MainWindow` rebuilds the chart's Past, Upcoming (today through today + 6 days), and Later slices when the displayed
`ObservableList<Person>` changes. The chart summarizes the current view and does not modify or save model data.

`ResultDisplay` colours its feedback by outcome: results are green, results containing a warning are amber (see
[Detecting duplicate engagements and possible typos](#detecting-duplicate-engagements-and-possible-typos)), and
errors are red. `MainWindow` passes command errors to `ResultDisplay#setErrorToUser`, which adds a `feedback-error`
style class using the same `-error-colour` as the command field's error border. Each message replaces the previous
highlight. The class is not named `error` because the global `.error` rule in `Extensions.css` sets an `!important`
text colour that would override it.

### Logic component

**API**:
[`Logic.java`](https://github.com/AY2627S1-CS2103T-T08-3/tp/blob/master/src/main/java/seedu/address/logic/Logic.java)

Here's a (partial) class diagram of the `Logic` component:

<img src="images/LogicClassDiagram.png" width="550"/>

The sequence diagram below illustrates the parsing and command-execution portion of the `Logic` component, taking
the `execute("delete 1")` API call as an example. The subsequent storage write is shown in the architecture sequence
diagram above.

![Interactions Inside the Logic Component for the `delete 1` Command](images/DeleteSequenceDiagram.png)

<div markdown="span" class="alert alert-info">:information_source: **Note:** The lifeline for `DeleteCommandParser` should end at the destroy marker (X), but due to a limitation of PlantUML, it continues to the end of the diagram.
</div>

How the `Logic` component works:

1. When `Logic` is called upon to execute a command, the command is passed to an `AddressBookParser` object, which in turn creates a parser that matches the command (e.g., `DeleteCommandParser`) and uses it to parse the command.
1. This results in a `Command` object (more precisely, an object of one of its subclasses e.g., `DeleteCommand`) which is executed by the `LogicManager`.
1. The command can communicate with the `Model` when it is executed (e.g. to delete an engagement).<br>
   Note that although this is shown as a single step in the diagram above for simplicity, the code can require several interactions between the command object and the `Model` to complete the operation.
1. The result of the command execution is encapsulated as a `CommandResult` object.
1. If `Command#modifiesAddressBook` reports that the command can change persistent data, `LogicManager` saves the
   address book through `Storage` (see [Persistence after commands](#persistence-after-commands)). If saving fails, it
   throws a `CommandException` instead of returning the result.
1. Finally, the `CommandResult` is returned from `Logic`.

The sequence diagram below shows the execution portion for a command that modifies data, using `add` as an example.
Parsing works the same way as in the `delete 1` example above, so it is omitted.

![Interactions Inside the Logic Component for an `add` Command](images/AddSequenceDiagram.png)

`AddCommand` first asks the `Model` whether the new engagement duplicates an existing one, and fails with a
`CommandException` if so. Otherwise, it adds the engagement, shows all engagements so that the displayed indexes cover
every engagement, and asks `SimilarContactWarning` for a possible-typo warning about engagements of other clients that
share the same phone number or email. The warning, if any, is appended to the result message.

Here are the other classes in `Logic` (omitted from the class diagram above) that are used for parsing a user command:

<img src="images/ParserClasses.png" width="600"/>

How the parsing works:
* When called upon to parse a user command, the `AddressBookParser` class creates an `XYZCommandParser` (`XYZ` is a placeholder for the specific command name, e.g., `AddCommandParser`). The parser uses the other classes shown above to parse the user command and create an `XYZCommand` object (e.g., `AddCommand`). The `AddressBookParser` returns that object as a `Command` object.
* All `XYZCommandParser` classes, such as `AddCommandParser` and `DeleteCommandParser`, implement the `Parser` interface so they can be treated similarly where appropriate, for example during testing.
* Commands that take no arguments (e.g., `clear`, `help`, `exit`) are created by `AddressBookParser` directly, without
  an `XYZCommandParser`.

### Model component
**API**:
[`Model.java`](https://github.com/AY2627S1-CS2103T-T08-3/tp/blob/master/src/main/java/seedu/address/model/Model.java)

<img src="images/ModelClassDiagram.png" width="900" alt="Model classes and the JavaFX filtered and sorted display pipeline" />


The `Model` component,

* stores the address book data i.e., all `Person` objects (which are contained in a `UniquePersonList` object).
* represents each `Person` as one photography engagement containing a client's `Name`, `Phone`, `Email`,
  `EventType`, `EventDate`, and `Stage`.
* represents the engagement stage as the `Stage` enum. `advance` uses `Stage#getNextStage()`, which maps each stage to
  the next one explicitly, so adding a stage requires updating that mapping (changing the enum order alone does not
  change the behaviour of `advance`).
* treats two `Person` objects as the same engagement (`Person#isSameEngagement`) when their name, phone, email, event
  type and event date all match, ignoring case for the name, email and event type. The stage is not part of this
  identity. `UniquePersonList` uses this check to reject duplicates.
* maintains the _displayed_ list of `Person` objects in two layers: a `FilteredList<Person>` over the saved,
  insertion-ordered list selects which engagements are shown (e.g., search results or the upcoming-events window), and a
  `SortedList<Person>` over that filter optionally orders them by event date (e.g., `list sort/date`) without
  reordering the saved records. `getFilteredPersonList()` returns this displayed list as an unmodifiable
  `ObservableList<Person>` that the UI observes, so the UI updates when the list changes. The UI and index-based
  commands such as `edit 2` use this displayed order.
* stores a `UserPrefs` object that represents the user’s preferences (currently, just the GUI settings). This is exposed to the outside as a `ReadOnlyUserPrefs` object.
* does not depend on any of the other three components (as the `Model` represents data entities of the domain, they should make sense on their own without depending on other components)

### Storage component

**API**:
[`Storage.java`](https://github.com/AY2627S1-CS2103T-T08-3/tp/blob/master/src/main/java/seedu/address/storage/Storage.java)

<img src="images/StorageClassDiagram.png" width="550" />

The `Storage` component,
* can save both address book data and user preference data in JSON format, and read them back into corresponding objects.
* is implemented by `StorageManager`, which delegates the actual JSON file access to `JsonAddressBookStorage` and `JsonUserPrefsStorage` (one class per data file).
* converts each `Person` to and from a `JsonAdaptedPerson`, including its event type, ISO-8601 event date,
  and engagement stage.
* depends on some classes in the `Model` component (because the `Storage` component's job is to save/retrieve objects that belong to the `Model`)

### Common classes

Classes used by multiple components are in the `seedu.address.commons` package.

`MainWindow` handles Ctrl+L (Command+L on macOS) through its scene key filter, focusing the command input
and consuming the shortcut without executing a command or changing the input text.

--------------------------------------------------------------------------------------------------------------------

## **Implementation**

This section describes some noteworthy details on how certain features are implemented.

### Viewing upcoming events

`UpcomingCommandParser` accepts the case-insensitive, argument-free `upcoming` command. Invalid arguments,
including line breaks, are rejected before execution. `UpcomingCommand` obtains the current local date from a
`Clock` at execution time and calculates the inclusive end date with `plusDays(6)`. Tests supply fixed or mutable
clocks to cover timezone differences, daylight-saving transitions, leap years and rerunning after midnight.
Both boundaries are calculated before changing the model, so a date failure leaves the previous view intact.

`Model#showUpcomingPersons` replaces the existing filter with an inclusive date-range predicate over all saved
engagements, then reuses the list feature's date sorter with insertion-order ties. The model retains the date window
for the `Upcoming: START_DATE to END_DATE (N)` heading. Other view updates clear that window. The existing list
panel displays the heading and renumbers cards; index-based commands therefore use the upcoming view.
No stage restriction is applied. The selected window stays fixed until the command is executed again.

`LogicManager` skips storage writes for `UpcomingCommand`, just as it does for `ListCommand`. Tests verify that
the command works without writable storage, neither changes existing files nor creates missing files, and
preserves the previous view on invalid input. The persistent insertion order is unchanged.

The sequence diagram below summarizes the successful command path. It focuses on parsing, calculating the inclusive
date range, filtering and sorting the displayed list, and returning feedback. Calls used only to construct the date
predicate or format the feedback are omitted.

![Interactions for the `upcoming` Command](images/UpcomingSequenceDiagram.png)

### Listing engagements and sorting by event date

The sequence below shows `list` and `list sort/date`, including the display's `FilteredList` and `SortedList` interactions. Parsing is summarized in a reference frame; invalid arguments are rejected before the model calls shown here. `allPersons` denotes `PREDICATE_SHOW_ALL_PERSONS`. The comparator expression is abbreviated: its secondary key is the engagement's saved insertion position.

![List command execution, optional date sorting, and the filtered-to-sorted display pipeline.](images/ListSortSequenceDiagram.png)

`ListCommandParser` accepts `list` and `list sort/date`, rejecting extra positional arguments,
unsupported/repeated prefixes, empty values and invalid sort options before changing the model.
List-specific parsing is case-insensitive and preserves raw arguments to reject embedded control characters.
Stage filtering is not supported; `stage/` is rejected as an unsupported list parameter.

`ListCommand` calls `Model#showAllPersons` to clear the current filter and select an ordering.
`ModelManager` exposes a `SortedList<Person>` over its `FilteredList<Person>` to the UI and index-based commands.
For date sorting, it compares the existing `EventDate.value` (`LocalDate`) and breaks ties by the record's
position in the underlying address book. Plain listing removes the comparator, restoring insertion order.
The stored list itself is never sorted. Updating a filter removes date sorting so a later search is in
insertion order. Deleting from a date-sorted view resets that view after selecting the displayed engagement.

`LogicManager` skips storage writes for `ListCommand`: viewing engagements does not require write access to the
JSON file. Other commands retain their existing saving behaviour. `Model#getPersonListHeading` supplies the
heading and count through `Logic`; `MainWindow` refreshes a simple label in `PersonListPanel` after successful
commands. A filtered view uses the neutral heading `Engagements (N)`.

Tests cover empty and single-record lists, past/future dates, ties, filter/sort resets, displayed-index
edit/advance/delete integration, failed-input view preservation and unchanged persisted order across restarts.

### Managing engagement stages

`Stage` is a required enum field of every `Person`. `Stage#fromString` accepts the five display values
case-insensitively after trimming surrounding whitespace: `Enquiry`, `Booked`, `Shot Completed`,
`Awaiting Payment`, and `Delivered`. JSON loading is strict: a missing or invalid `stage` makes the entire data file
invalid rather than assigning a default.

`AddCommandParser` accepts an optional `s/STAGE`; when it is absent, the parser constructs the engagement with
`Stage.ENQUIRY`. `EditCommandParser` stores a supplied stage in `EditPersonDescriptor`. The descriptor uses
`Optional<Stage>` to distinguish “stage not supplied in this edit” from a stage value; `Person#getStage` itself
returns a non-null `Stage`.

`AdvanceStageCommand` takes the engagement at the displayed index and asks `Stage#getNextStage` for its successor.
It constructs a new immutable `Person` with the same client and event fields, replaces the old object through
`Model#setPerson`, and returns to the complete insertion-ordered list. `Delivered` has no successor and produces a
command error without modifying the model or view. Direct editing can move a stage either forward or backward.

The following sequence diagram shows the parsing and execution of a successful `advance 1` command up to the model
update. As with the delete-command sequence diagram, the subsequent storage write is covered by the architecture
sequence diagram.

![Interactions for the `advance 1` Command](images/AdvanceStageSequenceDiagram.png)

Stage is deliberately excluded from `Person#isSameEngagement`, so changing only the stage cannot create a distinct
duplicate. Engagement identity uses name, normalized phone, normalized email, event type, and event date; name,
email, and event type comparisons are case-insensitive.

### Finding engagements

`FindCommandParser` requires exactly one of `n/`, `p/`, `e/`, `t/`, or `s/` with a non-blank search value. For `s/`,
it reuses `ParserUtil#parseStage` and `PersonSearchPredicate` matches the complete canonical stage name, ignoring
capitalization. The other search fields do not require complete field values and use a case-insensitive substring
comparison against the selected stored field. Consequently, `find e/alice@` is valid and can match
`alice@example.com`. Phone values and phone search text are normalized by removing spaces and hyphens, so formatted
searches such as `find p/9123-4567` can match a stored `91234567`.

Executing `find` removes date sorting and assigns the search predicate to the model's `FilteredList`. The
`FilteredList` invokes `PersonSearchPredicate` for the saved engagements, while the wrapping `SortedList` supplies
the displayed view and its match count. Index-based commands operate on that view. A successful add, edit, or
advance restores the complete list; deleting from a search leaves the remaining search matches visible.

The following sequence diagram shows how a search command is parsed, converted into a
`PersonSearchPredicate`, and used to filter the model.

![Find command sequence diagram](images/FindCommandSequenceDiagram.png)

### Detecting duplicate engagements and possible typos

A photographer often records several engagements for the same client, and contact details are typed by hand. ShutterLink
therefore distinguishes three levels of "sameness" between two `Person` objects, all defined in `Person`:

| Check | Fields compared | Used for |
| --- | --- | --- |
| `isSameClient` | name (ignoring case), phone, email | Deciding whether two engagements belong to the same client |
| `isSameEngagement` | client identity, event type (ignoring case), event date | Rejecting duplicate engagements in `UniquePersonList` |
| `hasSimilarContactDetails` | phone or email shared, but not the same client | Producing a possible-typo warning |

The stage is deliberately not part of any check, so changing only the stage can neither create a duplicate nor hide one.

**Normalization before comparison.** The checks compare stored values, so every value is normalized once, when it is
parsed (`ParserUtil`) or loaded from the data file (`JsonAdaptedPerson`), rather than at every comparison:

* `Phone` removes spaces and hyphens, so `9123 4567`, `9123-4567` and `91234567` are equal. A leading `+` is kept,
  because inferring country codes would be guesswork.
* `Email` is stored in lowercase.
* `Name` and `EventType` values have leading and trailing whitespace removed and repeated spaces or tabs collapsed
  into one space by `StringUtil#normalizeWhitespace`.

**Duplicates are rejected.** `AddCommand` calls `Model#hasPerson` before adding, and `EditCommand` rejects an edit whose
result has the identity of a *different* engagement. The error ends with `Messages#formatExistingEngagement`, which
names the stored engagement with that identity, so the user can find and edit it instead. `UniquePersonList` enforces
the same rule as a last line of defence and throws `DuplicatePersonException` if a command misses it. An edit that leaves every field unchanged returns
`No changes were made.` before either check and keeps the current view.

**Similar contacts produce a warning, not an error.** After a successful add, or an edit that changes the client's name,
phone or email, the command shows the full list and calls `SimilarContactWarning#generate(displayedPersons, target)`.
The activity diagram below summarizes how the warning is built.

![Building the possible-typo warning](images/SimilarContactWarningActivityDiagram.png)

* The warning is computed on the displayed list after it has been reset to all engagements, so the `#INDEX` values it
  prints are exactly the indexes the user can pass to `edit` or `delete`.
* A client with several engagements is listed once, at the index of its first engagement, by remembering the clients
  already listed and comparing new matches with `isSameClient`.
* At most `MAX_CONTACTS_LISTED` (3) clients are named; any others are summarized as `and N more`, keeping the feedback
  short enough for the result display.
* Each entry states what is shared (`same phone`, `same email` or `same phone and email`), so the user can tell which
  record is likely mistyped.
* `ResultDisplay` adds a `warning` style class whenever `Messages#hasWarning` finds a line starting with `Warning: `,
  so the feedback is highlighted in amber and the warning is not mistaken for an ordinary success message.
* An edit that changes only the event, the stage or the capitalization of the name does not warn again, because the
  client details are unchanged.

**Parsing names that contain slashes.** Legal names in Singapore and Malaysia can contain relation markers such as
`s/o`, `d/o`, `a/l`, `a/p` and `w/o` (e.g. `Raj s/o Kumar`), which clash with the `s/` (stage) and `d/` (date) prefixes.
`CliSyntax` defines these markers. `ArgumentTokenizer` skips a prefix match that starts a marker followed by
whitespace, and also keeps a marker that ends the input directly after `n/` (as in `find n/Raj s/o`), so that the
marker stays in the name. `ParserUtil#verifyNoUnsupportedPrefixes` also skips relation markers, so a marker is not reported as an
unsupported parameter. A marker after any other prefix, as in `edit 1 s/o`, is still read as a prefix
and fails stage validation.

**Design considerations.**

* **Aspect: how to treat engagements that share a phone number or email.**
  * **Alternative 1 (current choice):** Accept the command and show a warning that names the similar engagements.
    * Pros: Family members and businesses legitimately share phones and emails, and the user stays in control.
    * Cons: A real typo is saved until the user corrects it.
  * **Alternative 2:** Reject the command until the details are unique.
    * Pros: Typos cannot be saved.
    * Cons: Blocks legitimate records, and the user has no way to override it.
* **Aspect: where client identity lives.**
  * **Alternative 1 (current choice):** Each engagement stores its own copy of the client's details, and client
    identity is derived by `isSameClient`.
    * Pros: Keeps AB3's single-entity model and storage format, so commands, storage and UI need no new entity.
    * Cons: Editing one engagement does not update the client's other engagements.
  * **Alternative 2:** A separate `Client` entity referenced by engagements.
    * Pros: Client details are stored once.
    * Cons: Needs referential integrity in storage, new commands to manage clients, and a two-level UI.

### Persistence after commands

The sequence diagram below shows how `LogicManager` saves the data after a command that may change it.

![Saving the address book after a command](images/PersistenceSequenceDiagram.png)

`StorageManager` delegates to `JsonAddressBookStorage`, which converts the address book into a
`JsonSerializableAddressBook` (one `JsonAdaptedPerson` per engagement) and writes it to the data file using `JsonUtil`.
The whole address book is rewritten on every save.

`LogicManager#execute` parses and executes a command before deciding whether to save. Each `Command` reports through
`Command#modifiesAddressBook` whether it can change persistent data. Data-changing commands trigger
`Storage#saveAddressBook`; `find`, `list`, `upcoming`, `help`, and `exit` skip that write.

If a storage write fails, `LogicManager` throws a `CommandException`. The command has already executed, however, so
an add, edit, advance, delete, or clear remains applied to the in-memory model for the current session. There is no
rollback. The data file is also not guaranteed to keep its previous contents, because `FileUtil#writeToFile`
overwrites it directly rather than writing to a temporary file first, so a failure during writing can leave it
incomplete. On startup, a missing file loads sample data,
whereas a malformed or otherwise unreadable file loads an empty address book and leaves the bad file untouched until
a later command successfully writes data.

--------------------------------------------------------------------------------------------------------------------

## **Documentation, logging, testing, dev-ops**

* [Documentation guide](Documentation.md)
* [Testing guide](Testing.md)
* [Logging guide](Logging.md)
* [DevOps guide](DevOps.md)

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Requirements**

### Product scope

**Target user profile**:

* is a freelance photographer who handles event-based jobs
* has a need to manage a significant number of clients and photography jobs
* needs to track client contact details, event details, and job progress from enquiry to delivery
* prefers desktop apps over other types of applications
* can type fast
* prefers typing to mouse interactions
* is reasonably comfortable using CLI apps

**Value proposition**: Manage photography engagements from enquiry to delivery faster than with a typical
mouse-driven GUI application.


### User stories

Priorities: High (must have) - `* * *`, Medium (nice to have) - `* *`, Low (unlikely to have) - `*`

In these stories, an **engagement** is the record being added, edited, searched, displayed, and deleted. A **client**
is the person associated with that engagement, identified in the current model by the combination of name, normalized
phone number, and normalized email address. Clients are not stored as separate objects, so multiple engagements for
the same client repeat those three fields.

| Priority | User story |
| -------- | ---------- |
| `***` | As a photographer, I can add an engagement with client contact details, event details, and an optional initial stage, so that I can record work at any point in its lifecycle. |
| `***` | As a photographer, I can set an engagement's stage, such as enquiry, booked, shot completed, awaiting payment, or delivered, so that I always know what needs to happen next. |
| `***` | As a photographer, I can edit an engagement's client, event, or stage details, so that I can keep its information accurate as things change. |
| `***` | As a photographer, I can delete an engagement, so that my engagement list does not fill up with irrelevant work. |
| `***` | As a photographer, I can search engagements by client phone number or email address, so that I can find a client's work even when I do not remember their name. |
| `***` | As a photographer, I can search engagements by event type, so that I can quickly locate specific types of shoots. |
| `***` | As a photographer, I can view engagements sorted by event date, including past events, so that I can review them chronologically. |
| `***` | As a photographer, I can view all engagements with events happening within the next seven days, so that I can prepare logistics in advance. |
| `***` | As a fast typist, I can add, edit, and find engagements by typing commands, so that I can manage work faster than with a form. |
| `***` | As a photographer, I can type `help` to open a concise command reference, so that I can recall common syntax without leaving the application. |
| `**` | As a photographer, I can record multiple engagements for the same repeat client, so that their separate events remain available. |
| `*` | As a photographer, I receive a possible-typo warning when adding an engagement or editing its client contact details causes it to share a phone number or email address with a different client identity. |


### Use cases

(For all use cases below, the **System** is `ShutterLink` and the **Actor** is the `user`, a freelance event photographer, unless specified otherwise)

**Use case: UC01 - Add an engagement**

**MSS**

1.  User requests to add an engagement, giving the client's name, phone number, email address, event type and event
    date, and optionally an engagement stage.
2.  ShutterLink adds the engagement at the supplied stage, or at _Enquiry_ when no stage is supplied, and shows the
    full engagement list in insertion order.

    Use case ends.

**Extensions**

* 1a. A required detail (name, phone number, email address, event type or event date) is missing, or a supplied detail
  (including stage) is invalid.

    * 1a1. ShutterLink shows an error message describing the first problem found.
    * 1a2. User requests to add the engagement again with corrected details.

      Steps 1a1-1a2 are repeated until the details are valid.<br>
      Use case resumes from step 2.

* 1b. An engagement with the same name, normalized phone number, email address, event type, and event date already
  exists, using case-insensitive comparison for name, email, and event type. Stage is not part of duplicate identity.

    * 1b1. ShutterLink informs the user that the engagement already exists, shows the existing engagement, and does
      not add it.

      Use case ends.

* 1c. ShutterLink is unable to save the data.

    * 1c1. ShutterLink shows an error message. The engagement remains in memory for the current session. A failure
      detected before writing, such as insufficient permissions, leaves the data file unchanged; an interrupted write
      is not guaranteed to be recoverable.

      Use case ends.

**Use case: UC02 - Find engagements by client or event details**

**MSS**

1.  User requests to find engagements, giving one client name, phone number, email address, or event type.
2.  ShutterLink shows all engagements whose selected field contains the given value, ignoring capitalization.

    Use case ends.

**Extensions**

* 1a. User gives no search value, or more than one.

    * 1a1. ShutterLink shows an error message asking for exactly one search value.

      Use case resumes at step 1.

* 2a. No engagement matches the given value.

    * 2a1. ShutterLink informs the user that no matching engagements were found.

      Use case ends.

**Use case: UC03 - Update an engagement stage**

**MSS**

1.  User requests to list engagements, or finds the relevant engagement (UC02).
2.  ShutterLink shows a list of engagements.
3.  User either edits the stage directly or advances the engagement to the next stage.
4.  ShutterLink updates the engagement's stage and shows the full engagement list.

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. ShutterLink shows an error message.

      Use case resumes at step 2.

* 3b. The given stage is not one of the supported stages.

    * 3b1. ShutterLink shows an error message listing the supported stages.

      Use case resumes at step 2.

* 3c. User requests to advance an engagement that is already at _Delivered_.

    * 3c1. ShutterLink informs the user that the final stage cannot be advanced and keeps the current list.

      Use case ends.

* 3d. ShutterLink is unable to save the data.

    * 3d1. ShutterLink shows an error message. The stage remains changed in memory for the current session. A failure
      detected before writing, such as insufficient permissions, leaves the data file unchanged; an interrupted write
      is not guaranteed to be recoverable.

      Use case ends.

**Use case: UC04 - Edit an engagement**

**MSS**

1.  User requests to list engagements, or finds the relevant engagement (UC02).
2.  ShutterLink shows a list of engagements.
3.  User requests to edit a specific engagement in the list, giving new values for one or more client, event, or stage
    fields.
4.  ShutterLink updates the engagement and shows the full engagement list.

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. ShutterLink shows an error message.

      Use case resumes at step 2.

* 3b. User does not give any detail to edit, or a given detail is invalid.

    * 3b1. ShutterLink shows an error message and does not change the engagement.

      Use case resumes at step 2.

* 3c. The edit supplies an empty value for a required detail (name, phone number, email address, event type, event date,
  or stage).

    * 3c1. ShutterLink shows an error message and does not change the engagement.

      Use case resumes at step 2.

* 3d. The edit would give the engagement the same identity as another existing engagement.

    * 3d1. ShutterLink informs the user that the edit would create a duplicate and does not change the engagement.

      Use case resumes at step 2.

* 3e. All given values are the same as the engagement's current details.

    * 3e1. ShutterLink informs the user that no changes were made and keeps the current list.

      Use case ends.

* 3f. ShutterLink is unable to save the data.

    * 3f1. ShutterLink shows an error message. The edit remains applied in memory for the current session. A failure
      detected before writing, such as insufficient permissions, leaves the data file unchanged; an interrupted write
      is not guaranteed to be recoverable.

      Use case ends.

**Use case: UC05 - Delete an engagement**

**MSS**

1.  User requests to list engagements, or finds the relevant engagement (UC02).
2.  ShutterLink shows a list of engagements.
3.  User requests to delete a specific engagement in the list.
4.  ShutterLink deletes the engagement immediately and shows the remaining engagements.

    Use case ends.

**Extensions**

* 2a. The list is empty.

  Use case ends.

* 3a. The given index is invalid.

    * 3a1. ShutterLink shows an error message.

      Use case resumes at step 2.

* 3b. ShutterLink is unable to save the data.

    * 3b1. ShutterLink shows an error message. The deletion remains applied in memory for the current session. A
      failure detected before writing, such as insufficient permissions, leaves the data file unchanged; an interrupted
      write is not guaranteed to be recoverable.

      Use case ends.

**Use case: UC06 - Prepare for upcoming events**

**MSS**

1.  User requests to view upcoming events.
2.  ShutterLink shows the engagements whose event dates fall within today and the next six days, sorted by event date.
3.  User reviews the events and edits an engagement if needed (UC04).

    Use case ends.

**Extensions**

* 1a. ShutterLink is unable to determine today's date.

    * 1a1. ShutterLink shows an error message and keeps the current list.

      Use case ends.

* 2a. No event falls within the next seven days.

    * 2a1. ShutterLink informs the user that no events are scheduled in that period.

      Use case ends.

### Non-Functional Requirements

These requirements describe the intended quality of the current ShutterLink release for an independent freelance
event photographer.
An **engagement** is the primary stored record. It contains one client's contact details, one event, and one workflow
stage, so a repeat client may appear in multiple engagement records.

1. **Platform compatibility (NFR-01).** ShutterLink shall run on Windows, Linux and macOS with Java 25 installed,
   without requiring another Java version. Supported operations shall have the same behaviour across these platforms.
2. **Portable distribution (NFR-02).** Users shall be able to run ShutterLink from a single downloadable JAR
   without an application installer or separately installed dependencies other than the required Java runtime.
3. **Independent, offline operation (NFR-03).** Engagement management, searching, sorting, upcoming-event views,
   command help, and saving/loading shall remain available without an Internet connection.
   Normal use shall not require an account, a shared data service, or a remote server.
   Each installation is intended for one photographer managing their own records; concurrent users and shared-file
   collaboration are outside the supported operating environment.
4. **Local data ownership and privacy (NFR-04).** Engagement records shall be stored locally in a human-editable UTF-8
   text format, without a database server. Valid manual edits made while the application is closed shall be loaded
   on the next launch. Normal operation shall not transmit engagement or client contact data to external services.
   Local files are not encrypted by this requirement; restricting access to the device and files remains the user's
   responsibility.
5. **Persistence (NFR-05).** After a data-changing command reports success, its changes shall be saved locally
   and restored after a normal shutdown and restart, preserving all supported field values and record order.
   This assumes writable storage with sufficient space and excludes external file modification or disk failure.
6. **Failure handling (NFR-06).** Invalid input and command-execution errors shall leave records, the current displayed
   view, and displayed indexes unchanged. A failed save shall be reported as an error. The current implementation
   does not roll back a model mutation that occurred before that failure, so the in-memory state can differ from the
   file until another save succeeds or the app restarts. A failure detected before writing, such as insufficient
   permissions, leaves the existing file unchanged, but saving is not atomic and an interrupted write is not guaranteed
   to preserve the previous file. A malformed data file starts as an empty address book and is not overwritten by
   startup itself, although a later successfully executed command that triggers saving can replace it.
7. **Keyboard usability (NFR-07).** All supported engagement-management and query operations shall be available through
   typed commands without mandatory mouse interaction. After an ordinary successful command, the command field shall
   clear and remain focused; after closing command help, focus shall return to it. On rejection, the field shall retain
   the input for correction. Command help shall be accessible from the keyboard.
8. **Readable feedback (NFR-08).** Errors shall be reported using text in the feedback area in addition to visually
   marking the command input, so colour alone is not needed to identify a rejected command.

The platform, distribution, local-storage and feedback requirements reflect the relevant
[tP product constraints](https://nus-cs2103-ay2627-s1.github.io/website/admin/tp-constraints.html).
Course process requirements, such as incremental delivery, are not product NFRs.

### Glossary

* **Client**: The person associated with an engagement. The current implementation treats two engagements as
  belonging to the same client only when their name, normalized phone number, and normalized email address all match;
  name and email comparisons ignore capitalization. A client is not stored as a separate model object.
* **Client contact details**: The client's name, phone number, and email address stored within each engagement.
* **Current displayed list**: The engagements currently visible after applying a search, sort, or upcoming-event
  view. Index-based commands refer to this list.
* **Engagement**: The primary record managed by ShutterLink. It combines client contact details, an event type, an
  event date, and an engagement stage. In the Java model, this record is represented by `Person`.
* **Engagement identity**: The fields used to reject duplicate engagement records: client identity, event type, and
  event date. Name, email, and event type comparisons ignore capitalization; phone separators are removed during
  normalization. Stage is not part of engagement identity.
* **Engagement stage**: The workflow status of an engagement: `Enquiry`, `Booked`, `Shot Completed`,
  `Awaiting Payment`, or `Delivered`.
* **Event type**: The category of photography event associated with an engagement, such as a wedding or birthday
  celebration.
* **Substring matching**: A search method that matches when the supplied text occurs anywhere within the selected
  field; ShutterLink performs this comparison without regard to capitalization.
* **Upcoming events**: Events scheduled from today through the next six calendar days, inclusive.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Instructions for manual testing**

Given below are instructions to test the app manually.

<div markdown="span" class="alert alert-info">:information_source: **Note:** These instructions only provide a starting point for testers to work on;
testers are expected to do more *exploratory* testing.

</div>

### Launch and shutdown

1. Initial launch

   1. Download the JAR file and copy it into an empty folder.

   1. Open a terminal in that folder and run `java -jar shutterlink.jar`.<br>
      Expected: The GUI opens with a set of sample engagements. The window size may not be optimal.

1. Saving window preferences

   1. Resize the window to an optimal size. Move the window to a different location. Close the window.

   1. Relaunch the app by running `java -jar shutterlink.jar` again.<br>
       Expected: The most recent window size and location are retained.
       If a saved monitor has been disconnected, expect the fallback position to keep the title bar on the primary
       screen, even when the saved window is larger than that screen. The saved size is unchanged.

### Viewing upcoming events

1. In a disposable dataset, add engagements dated yesterday, today, today + 6 days and today + 7 days.
   Add another engagement for today, after the first one, to check insertion-order ties.
2. Run `upcoming`. Expect only the two engagements for today and the one for today + 6 days, in date order.
   The two engagements for today must remain in insertion order. Check the heading's dates and count.
3. Run `upcoming 7`. Expect a usage error with the same displayed engagements and heading.
4. Run `list`, search for one engagement, then run `upcoming` again. Expect all engagements in the upcoming date window,
   regardless of the preceding search results. Run `list` again to restore the complete insertion-ordered list.
5. With no engagements in the window, expect an empty list and feedback naming the start and end dates.
6. For midnight behaviour, use the automated mutable-clock test rather than changing the operating system clock.
   The window stays unchanged until the next execution, then advances to the new local date.

### Listing and sorting engagements

1. Use a dataset with insertion order A (2030-12-31), B (2000-02-29), C (2000-02-29).
2. Run `list sort/date`. Expect B, C, A, indexes 1–3, heading `All engagements by event date (3)`,
   and feedback `Showing 3 engagement(s).` Repeating the command must retain B before C.
3. Run `list sort/name`. Expect an invalid-sort error and the same B, C, A view.
4. Run `list`. Expect A, B, C and heading `All engagements (3)`.
5. Search for one engagement, then run either list variant. Expect all three engagements again.
6. With no saved engagements, run each variant. Expect an empty list, the corresponding heading with `(0)`,
   and `No engagements to display.`
7. In a disposable dataset, sort and delete index 1. Expect the earliest displayed engagement to be removed,
   not the first engagement in insertion order. Restart after a successful save and verify insertion order.

### Adding, editing, and advancing stages

1. In an empty disposable dataset, add one engagement without `s/STAGE` and another with `s/booked`.<br>
   Expected: The first is displayed as `Enquiry`; the second is displayed as `Booked`.
2. Run `edit 1 s/Awaiting Payment`, followed by `edit 1 s/enquiry`.<br>
   Expected: Both edits succeed, showing that direct stage edits can move forward or backward and ignore
   capitalization.
3. Run `advance 1` repeatedly.<br>
   Expected: The stage follows `Enquiry` → `Booked` → `Shot Completed` → `Awaiting Payment` → `Delivered`.
4. Run `advance 1` once more.<br>
   Expected: ShutterLink reports that `Delivered` cannot be advanced and leaves the data and view unchanged.
5. Try an unsupported value such as `edit 1 s/Confirmed` and a missing value such as `edit 1 s/`.<br>
   Expected: Each command is rejected with the supported-stage constraint and makes no change.

### Validating and normalizing fields

1. Add a phone number containing a leading `+`, spaces, and hyphens, with 8–15 digits in total.<br>
   Expected: The command succeeds and the stored/displayed phone contains the leading `+` and digits only.
2. Try phone numbers with 7 and 16 digits.<br>
   Expected: Both are rejected.
3. Add an email containing uppercase letters.<br>
   Expected: It is accepted when otherwise valid and displayed in lowercase.
4. Try `d/2025-02-29`, `d/2024-02-29`, and a non-ISO date.<br>
   Expected: Only the valid leap-day value `2024-02-29` is accepted.
5. Add an event type containing repeated spaces or tabs.<br>
   Expected: It is stored with each run of whitespace normalized to one space, provided the normalized value contains
   only letters, digits, spaces, and hyphens and is no longer than 40 characters.

### Finding engagements

1. In a disposable dataset, add an engagement for Alice Tan with phone `+6591234567`, email `alice@example.com`, and
   event type `Wedding`. Add another for Alicia Lim with phone `81234567`, email `alicia@studio.com`, and event type
   `Portrait`. Give both engagements valid, distinct event dates.
2. Run `find n/ALI`, `find p/9123`, `find e/STUDIO`, and `find t/wed` one at a time.<br>
   Expected: The name search finds both engagements. Each other search finds only the engagement whose selected field
   contains the supplied text, ignoring capitalization. The heading and displayed indexes update to match each result.
3. Run `find n/Zachary`.<br>
   Expected: The list is empty and the feedback is `No matching engagements found.`
4. Run `find n/Alice`, then try `find`, `find n/`, `find n/Alice p/9123`, and `find n/Alice n/Bob`.<br>
   Expected: Each command is rejected and leaves the preceding view unchanged.
5. Run `list`.<br>
   Expected: All engagements reappear in insertion order.

### Detecting duplicate and similar contact details

1. In an empty disposable dataset, add an engagement for Alice Tan.
2. Try adding another engagement with the same name, normalized phone number, email address, event type, and event date,
   but a different stage.<br>
   Expected: The command is rejected as a duplicate because stage is not part of engagement identity. The error
   names Alice's existing engagement, including its stage.
3. Add an engagement for Bob Lim that has a different email, event type, and event date but the same phone number as
   Alice.<br>
   Expected: The engagement is added, the feedback warns that it shares Alice's phone number, and the feedback panel
   is highlighted in amber. The next command's feedback is no longer highlighted.
4. Add an engagement for Carol Lee with distinct details, then edit Carol's email to Alice's email.<br>
   Expected: The edit succeeds and the feedback warns that Carol now shares Alice's email address.
5. Add an engagement with the name `Raj s/o Kumar`, then run `find n/Raj s/o`.<br>
   Expected: The name is stored with `s/o`, and the search finds that engagement. `edit 1 s/o` is rejected with the
   supported-stage constraint, because `s/` after the index is read as the stage prefix.

### Deleting an engagement

1. Deleting an engagement while all engagements are being shown

   1. Prerequisites: List all engagements using the `list` command, with multiple engagements in the list.

   1. Test case: `delete 1`<br>
      Expected: The first engagement is deleted from the list. The status message shows the deleted engagement's
      details.

   1. Test case: `delete 0`<br>
      Expected: No engagement is deleted. The status message shows error details.

   1. Other incorrect delete commands to try: `delete`, `delete x`, `...` (where x is larger than the list size)<br>
      Expected: Similar to previous.

### Saving data

1. Dealing with missing/corrupted data files

   1. In a disposable folder, launch the app without `data/addressbook.json`.<br>
      Expected: Sample engagements are loaded. The data file is created only when a command other than `list` or
      `upcoming` successfully executes and triggers a save.

   1. Close the app, replace `data/addressbook.json` with malformed JSON, and relaunch it.<br>
      Expected: An empty address book is displayed and the malformed file remains unchanged at startup.

   1. Run `list` and then `upcoming`.<br>
      Expected: Both commands work without overwriting the malformed file.

   1. Run `clear` or another successfully executed command that triggers saving.<br>
      Expected: The file is replaced with valid JSON representing the current in-memory address book.

2. Dealing with a save failure

   1. Make the data file or its directory read-only, then run a data-changing command.<br>
      Expected: ShutterLink reports a save error. The change is visible in the current session, while the file retains
      its last saved contents. Restore write permission before continuing.

--------------------------------------------------------------------------------------------------------------------

## **Appendix: Effort**

ShutterLink started from AB3 (a generic contact list) and was reworked into a workflow tool for freelance event
photographers. Compared with AB3, the team's effort went into the following areas.

**Reworking the domain model.** AB3's `Person` (name, phone, email, address, tags) became a photography *engagement*:
address and tags were removed and three new value types were added: `EventType`, `EventDate` (a real ISO calendar
date) and the `Stage` enum with an explicit workflow order. Every layer that touches `Person` (parsers, commands,
JSON storage, UI cards, sample data and test utilities) had to be updated without breaking existing behaviour.

**New commands and views.**
* `advance` moves an engagement along the `Enquiry` → `Delivered` workflow; `edit` can set any stage directly.
* `list sort/date` sorts the display by event date without reordering the saved records, which required wrapping
  AB3's `FilteredList` in a `SortedList` and keeping index-based commands consistent with the displayed order.
* `upcoming` shows the next seven calendar days using an injectable `Clock`, so date boundaries, time zones and
  midnight roll-over are testable.
* `find` was redesigned to search one chosen field (name, phone, email or event type) by case-insensitive substring,
  with phone separators ignored.
* The list heading always states the active view and its size, and an engagement's full details open in a keyboard
  accessible dialog.

**Data integrity and input handling.** This was harder than in AB3 because a client can have many engagements:
* Duplicate detection distinguishes client identity, engagement identity and similar contact details
  (see [Detecting duplicate engagements and possible typos](#detecting-duplicate-engagements-and-possible-typos)).
  Shared phones or emails produce a warning that lists the affected records by their displayed indexes.
* Phones, emails, names and event types are validated and normalized consistently on input and on loading the data
  file, including international phone numbers, accented letters and apostrophes in names, and name relation markers
  such as `s/o` that clash with command prefixes.
* Parsers reject unsupported, repeated and missing parameters with messages that name the offending parameter, and
  view-only commands never rewrite the data file.

**Quality.** The project has more than 330 automated tests, including tests for date boundaries, storage failures and
parser edge cases, and changes were integrated through pull requests (over 80 merged).

**Reuse.** Apart from the AB3 code base and the libraries listed in the [Acknowledgements](#acknowledgements), no
code was reused from other projects.

