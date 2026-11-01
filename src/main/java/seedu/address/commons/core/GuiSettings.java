package seedu.address.commons.core;

import java.awt.Point;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import seedu.address.commons.util.ToStringBuilder;

/**
 * A Serializable class that contains the GUI settings.
 * Guarantees: immutable.
 */
public class GuiSettings implements Serializable {

    private static final double DEFAULT_HEIGHT = 600;
    private static final double DEFAULT_WIDTH = 740;
    private static final int VISIBLE_TITLE_WIDTH = 160;
    private static final int VISIBLE_TITLE_HEIGHT = 40;

    private final double windowWidth;
    private final double windowHeight;
    private final Point windowCoordinates;

    /**
     * Constructs a {@code GuiSettings} with the default height, width and position.
     */
    public GuiSettings() {
        windowWidth = DEFAULT_WIDTH;
        windowHeight = DEFAULT_HEIGHT;
        windowCoordinates = null; // null represent no coordinates
    }

    /**
     * Constructs a {@code GuiSettings} with the specified height, width and position.
     */
    public GuiSettings(double windowWidth, double windowHeight, int xPosition, int yPosition) {
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        windowCoordinates = new Point(xPosition, yPosition);
    }

    public double getWindowWidth() {
        return windowWidth;
    }

    public double getWindowHeight() {
        return windowHeight;
    }

    public Point getWindowCoordinates() {
        return windowCoordinates != null ? new Point(windowCoordinates) : null;
    }

    /**
     * Returns whether both saved dimensions are finite and strictly positive.
     * Rejects dimensions that cannot be restored as a usable JavaFX window size.
     */
    public boolean hasValidWindowSize() {
        return Double.isFinite(windowWidth) && windowWidth > 0
                && Double.isFinite(windowHeight) && windowHeight > 0;
    }

    /**
     * Returns whether a usable portion of the saved title bar is inside a connected screen's work area.
     * A 160-by-40 area keeps enough of the title bar reachable to move the window.
     */
    public boolean hasVisibleWindowTitle(List<Rectangle2D> screens) {
        return windowCoordinates != null && screens.stream().anyMatch(screen ->
                screen.contains(windowCoordinates.getX(), windowCoordinates.getY(),
                        VISIBLE_TITLE_WIDTH, VISIBLE_TITLE_HEIGHT));
    }

    /**
     * Returns a centered fallback position clamped to keep the title bar inside {@code screen}.
     * The saved window size and coordinates are unchanged.
     */
    public Point2D getFallbackWindowPosition(Rectangle2D screen) {
        double x = Math.max(screen.getMinX(), Math.min(screen.getMaxX() - VISIBLE_TITLE_WIDTH,
                screen.getMinX() + (screen.getWidth() - windowWidth) / 2));
        double y = Math.max(screen.getMinY(), Math.min(screen.getMaxY() - VISIBLE_TITLE_HEIGHT,
                screen.getMinY() + (screen.getHeight() - windowHeight) / 2));
        return new Point2D(x, y);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof GuiSettings otherGuiSettings)) {
            return false;
        }

        return windowWidth == otherGuiSettings.windowWidth
                && windowHeight == otherGuiSettings.windowHeight
                && Objects.equals(windowCoordinates, otherGuiSettings.windowCoordinates);
    }

    @Override
    public int hashCode() {
        return Objects.hash(windowWidth, windowHeight, windowCoordinates);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("windowWidth", windowWidth)
                .add("windowHeight", windowHeight)
                .add("windowCoordinates", windowCoordinates)
                .toString();
    }
}
