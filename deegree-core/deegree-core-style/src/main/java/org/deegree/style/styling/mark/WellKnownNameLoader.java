package org.deegree.style.styling.mark;

import java.awt.Shape;
import java.net.URL;
import java.util.function.Function;

import org.deegree.style.styling.components.Mark;

/**
 * Loader for loading Mark from custom WellKnownName
 * 
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 *
 * @since 3.4
 */
public interface WellKnownNameLoader {

    /**
     * Parse a WellKnownName Text into a mark
     * 
     * @param mark
     * @param wellKnownName
     *            WellKnownName to be parsed
     * @param resolver
     *            Resolver to resolve relative locations into URL, can be null
     * @return The Shape or null if this Loader is not responsible for that type of WellKnownName
     */
    public Shape parse( String wellKnownName, Function<String, URL> resolver );

    /**
     * Apply the Shape to the Mark
     * 
     * @param mark
     *            The Mark to be updated
     * @param shape
     *            The previously created shape
     */
    public default void apply( Mark mark, Shape shape ) {
        mark.shape = shape;
    }

    /**
     * Get order value for this Loader
     * 
     * Used to sort multiple factories and create a order list of loader
     * 
     * @return int of position in list
     */
    public default int order() {
        return 1000;
    }
}
