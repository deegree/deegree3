//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.commons.dataaccess;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;

/**
 * The <code>CSVReader</code> reads a csv file line by line. Note the regular expression for quotes may not work under
 * all circumstances, a Tokenizer might be a better solution.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * 
 */
public class CSVReader extends LineNumberReader {
    private final static Logger LOG = getLogger( CSVReader.class );

    private final String delimiter;

    private String[] columnNames;

    private final int numberOfColumns;

    private final boolean firstLineContainsColumnNames;

    private final static Pattern QUOTED_REGEX = Pattern.compile( "\\\"" );

    private final Pattern splitRegex;

    private boolean firstLineRead = false;

    /**
     * @param in
     * @param delimiter
     * @param firstLineContainsColumnNames
     */
    public CSVReader( Reader in, String delimiter, boolean firstLineContainsColumnNames ) {
        super( in );
        this.delimiter = delimiter;
        this.firstLineContainsColumnNames = firstLineContainsColumnNames;
        splitRegex = Pattern.compile( ( delimiter == null || "".equals( delimiter ) ) ? "," : delimiter );
        columnNames = readFirstLine();
        if ( columnNames == null || columnNames.length == 0 ) {
            throw new IllegalArgumentException( "Could not read the first line. " );
        }
        numberOfColumns = columnNames.length;
    }

    private String[] readFirstLine() {
        try {
            String line = super.readLine();
            firstLineRead = true;
            return parseLine( line );
        } catch ( IOException e ) {
            throw new IllegalArgumentException( "Could not read the first line because: " + e.getLocalizedMessage(), e );
        }
    }

    /**
     * @return the parsed line
     */
    private String[] parseLine( String line ) {
        if ( line == null ) {
            return null;
        }
        List<String> result = new LinkedList<String>();
        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Trying to parse (line: " + getLineNumber() + "): " + line );
        }
        if ( !"".equals( line ) ) {
            String[] quoted = splitQuoted( line );
            for ( String q : quoted ) {
                String[] delimited = splitDelimeter( q );
                for ( String d : delimited ) {
                    if ( !"".equals( d ) ) {
                        if ( LOG.isTraceEnabled() ) {
                            LOG.trace( "Adding parsed value: " + d );
                        }
                        result.add( d );
                    }
                }
            }
        }
        return result.toArray( new String[0] );
    }

    /**
     * Splits the given line into it's quoted values.
     * 
     * @param line
     *            to 'delimiter' separate.
     * @return the splitted string.
     */
    private String[] splitQuoted( String line ) {
        return QUOTED_REGEX.split( line );
    }

    /**
     * Splits the given line into it's (delimiter seperated) values.
     * 
     * @param line
     *            to 'delimiter' separate.
     * @return the splitted string.
     */
    private String[] splitDelimeter( String line ) {
        return splitRegex.split( line );
    }

    /**
     * @return the values of the
     * @throws IOException
     */
    public String[] parseLine()
                            throws IOException {
        if ( getLineNumber() == 1 ) {
            if ( !firstLineContainsColumnNames ) {
                if ( firstLineRead ) {
                    firstLineRead = false;
                    return columnNames;
                }
            }
        }
        return parseLine( readLine() );
    }

    /**
     * @return the values of the first row, which may be the column names or the first line of data.
     */
    public final String[] getColumnsNames() {
        return columnNames;
    }

    /**
     * @return the numberOfColumns
     */
    public final int getNumberOfColumns() {
        return numberOfColumns;
    }

    /**
     * @return the delimiter
     */
    public final String getDelimiter() {
        return delimiter;
    }
}
