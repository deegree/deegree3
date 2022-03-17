/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2016 - 2021 lat/lon GmbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.config;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.namespace.QName;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Parser for property names from a file.
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class PropertyNameParser {

    private static final Logger LOG = getLogger( PropertyNameParser.class );

    /**
     * Parses the property names from the passed file. The properties are parsed line by line. Empty lines as well as
     * comments (starting with #) are ignored. Leading and trailing white spaces are removed.
     *
     * @param pathToFile
     *            the path to the file, never <code>null</code>
     * @return the parsed properties, may be <code>null</code> if the properties file could not be parsed or empty if no
     *         properties are parseable
     */
    public List<QName> parsePropertiesWithPrimitiveHref( String pathToFile ) {
        Path path = Paths.get( pathToFile );
        return parsePropertiesWithPrimitiveHref( path );
    }

    /**
     * Parses the property names from the passed file. The properties are parsed line by line. Empty lines as well as
     * comments (starting with #) are ignored. Leading and trailing white spaces are removed.
     * 
     * @param pathToFile
     *            the path to the file, never <code>null</code>
     * @return the parsed properties, may be <code>null</code> if the properties file could not be parsed or empty if no
     *         properties are parseable
     */
    public List<QName> parsePropertiesWithPrimitiveHref( URI pathToFile ) {
        Path path = Paths.get( pathToFile );
        return parsePropertiesWithPrimitiveHref( path );
    }

    private List<QName> parsePropertiesWithPrimitiveHref( Path path ) {
        try (Stream<String> stream = Files.lines( path )) {
            ArrayList<QName> properties = new ArrayList<>();
            List<String> list = stream.collect( Collectors.toList() );
            parseList( properties, list );
            return properties;
        } catch ( NoSuchFileException e ) {
            LOG.error( "Referenced listOfPropertiesWithPrimitiveHref cannot be found and is ignored! " );
        } catch ( IOException i ) {
            LOG.error( "Referenced listOfPropertiesWithPrimitiveHref cannot be parsed and is ignored! Exception: "
                                + i.getMessage() );
        }
        return null;
    }

    private void parseList( ArrayList<QName> properties, List<String> list ) {
        for ( String entry : list ) {
            String trimmedEntry = entry.trim();
            if ( !trimmedEntry.isEmpty() && !trimmedEntry.startsWith( "#" ) ) {
                try {
                    QName qName = QName.valueOf( trimmedEntry );
                    properties.add( qName );
                } catch ( IllegalArgumentException e ) {
                    LOG.error( "One line of referenced listOfPropertiesWithPrimitiveHref cannot be parsed and is ignored: "
                                        + entry );
                }
            }
        }
    }

}