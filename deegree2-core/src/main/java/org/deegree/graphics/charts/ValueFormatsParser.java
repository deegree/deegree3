//$$Header: $$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
   Department of Geography, University of Bonn
 and
   lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
----------------------------------------------------------------------------*/

package org.deegree.graphics.charts;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A Parser and tokenizer for the three defined formats.
 *
 * @author <a href="mailto:elmasry@lat-lon.de">Moataz Elmasry</a>
 * @author last edited by: $Author: elmasri$
 *
 * @version $Revision$, $Date: 28 Mar 2008 16:33:58$
 */
class ValueFormatsParser {

    /**
     * &CDU=34&SPD=36&Gruene=11
     */
    public final static int VALUE_FORMAT_SIMPLE = 1101;

    /**
     * &CDU=23,25,21,26&SPD=42 23 33 36
     */
    public final static int VALUE_FORMAT_SERIES = 1102;

    /**
     * &CDU=1970 23;1974,44;1978,43&SPD=1970,23;1974,44;1978,43
     */
    public final static int VALUE_FORMAT_XYSERIES = 1103;

    /**
     * Unknown format
     */
    public final static int VALUE_FORMAT_UNKNOWN = 1104;

    private String value;

    // private String regexSeries = "[\\w*,\\w*;]*[\\w*,\\w*]";
    // private String regexSeriesSep1 = "((\\w*,)*(\\w*){1})";
    //
    // private String regexSeriesSep2 = "((\\w*\\s)*(\\w*){1})";

    private String regexSeriesSep1 = "((\\-){0,1}\\d+(\\.\\d+){0,1},)*((\\-){0,1}\\d+(\\.\\d+){0,1})";

    private String regexSeriesSep2 = "((\\-){0,1}\\d+(\\.\\d+){0,1}\\s)*((\\-){0,1}\\d+(\\.\\d+){0,1})";

    // ex: &CDU=1970 23;1974,44;1978,43&SPD=1970,23;1974,44;1978,43
    // private String regexSeriesXY = "(\\d+(\\.\\d+){0,1},\\d+(\\.\\d+){0,1};)*";
    private String regexSeriesXYSep1 = "((\\-){0,1}\\d+(\\.\\d+){0,1},(\\-){0,1}\\d+(\\.\\d+){0,1};)*((\\-){0,1}\\d+(\\.\\d+){0,1},(\\-){0,1}\\d+(\\.\\d+){0,1})";

    private String regexSeriesXYSep2 = "((\\-){0,1}\\d+(\\.\\d+){0,1}\\s\\d+((\\-){0,1}\\.\\d+){0,1};)*((\\-){0,1}\\d+(\\.\\d+){0,1}\\s\\d+((\\-){0,1}\\.\\d+){0,1})";

    // Will only be used with xySeries
    private String separator = ";";

    private List<String> list = null;

    private Iterator<String> it = null;

    private int format = -1;

    private String separator1 = ",";

    private String separator2 = " ";

    /**
     * @param value
     * @throws IncorrectFormatException
     */
    public ValueFormatsParser( String value ) throws IncorrectFormatException {
        this.value = value;

        if ( value.contains( separator1 ) && value.contains( separator2 ) ) {
            throw new IncorrectFormatException( Messages.getMessage( "GRA_CHART_BAD_SEPARATOR", value, separator1,
                                                                     separator2 ) );

        }
        if ( !value.contains( separator ) ) {
            String sep = value.contains( separator1 ) ? separator1 : separator2;
            list = Arrays.asList( this.value.split( sep ) );
            it = list.iterator();
            format = getFormatType();
        } else {
            list = Arrays.asList( this.value.split( separator ) );
            it = list.iterator();
            format = getFormatType();
        }
    }

    /**
     * @return String
     */
    public String getValue() {
        return value;
    }

    /**
     * @return boolean
     */
    public boolean isFormatSimple() {
        // If simple format
        if ( format == VALUE_FORMAT_SIMPLE ) {
            return true;
        } else if ( format == -1 ) {
            // if not yet checked
            try {
                double d = Double.parseDouble( value );
                if ( String.valueOf( d ).length() != value.length() ) {
                    return false;
                }
                return true;
            } catch ( Exception e ) {
                return false;
            }
        } else {
            // if checked and not found simple
            return false;
        }
    }

    /**
     * @return boolean
     */
    public boolean isFormatSeries() {
        // if series
        if ( format == VALUE_FORMAT_SERIES ) {
            return true;
        } else if ( format == -1 ) {
            // if not checked
            boolean match = matchPattern( regexSeriesSep1 );
            if ( !match ) {
                return matchPattern( regexSeriesSep2 );
            }
            return match;
        } else {
            // if checked and found series
            return false;
        }
    }

    /**
     * @return boolean
     */
    public boolean isFormatSeriesXY() {
        // of seriesXY
        if ( format == VALUE_FORMAT_XYSERIES ) {
            return true;
        } else if ( format == -1 ) {
            // if not yet checked
            boolean match = matchPattern( regexSeriesXYSep1 );
            if ( !match ) {
                return matchPattern( regexSeriesXYSep2 );
            }
            return match;
        } else {
            // if checked and not found seriesXY
            return false;
        }
    }

    /**
     * @return boolean
     */
    public boolean isFormatUnknown() {
        if ( getFormatType() == VALUE_FORMAT_UNKNOWN ) {
            return true;
        }
        return false;
    }

    /**
     * @param pattern a regex to match
     * @return true if the pattern matches the value.
     */
    protected boolean matchPattern( String pattern ) {
        Pattern pat = Pattern.compile( pattern );
        Matcher matcher = null;
        try {
            matcher = pat.matcher( value );
        } catch ( Exception e ) {
            return false;
        }
        if ( matcher.matches() == false || matcher.group().length() != value.length() ) {
            return false;
        }
        return true;
    }

    /**
     * @return A public variable indicating the format type
     */
    public int getFormatType() {
        if ( isFormatSeriesXY() ) {
            format = VALUE_FORMAT_XYSERIES;
            return VALUE_FORMAT_XYSERIES;
        }
        if ( isFormatSeries() ) {
            format = VALUE_FORMAT_SERIES;
            return VALUE_FORMAT_SERIES;
        } else if ( isFormatSimple() ) {
            format = VALUE_FORMAT_SIMPLE;
            return VALUE_FORMAT_SIMPLE;
        } else {
            format = VALUE_FORMAT_UNKNOWN;
            return VALUE_FORMAT_UNKNOWN;
        }
    }

    /**
     * @return Separator
     */
    public String getSeparator() {
        return separator;
    }

    /**
     * @return Number of tupels parsed
     */
    public int getTupelsCount() {
        return list.size();
    }

    /**
     * Resets the market to the start of the string
     */
    public void start() {
        it = list.iterator();
    }

    /**
     * @return true if the tokenizer has more tupels, false otherwise
     */
    public boolean hasNext() {
        return it.hasNext();
    }

    /**
     * @return next tupel
     */
    public String getNext() {
        return it.next();
    }

    /**
     * @return nextTupel
     */
    public String nextTupel() {
        return it.next();
    }
}
