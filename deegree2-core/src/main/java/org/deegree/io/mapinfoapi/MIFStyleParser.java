//$HeadURL$
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

package org.deegree.io.mapinfoapi;

import static org.deegree.io.mapinfoapi.MapInfoReader.parseCommaList;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * <code>MIFStyleParser</code>
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MIFStyleParser {

    private StreamTokenizer mif;

    private File baseFile;

    /**
     * @param mif
     * @param baseFile
     *            for resolving relative file names
     */
    public MIFStyleParser( StreamTokenizer mif, File baseFile ) {
        this.mif = mif;
        this.baseFile = baseFile;
    }

    /**
     * @return a map with the symbol parameters, or null, if no symbol clause was found
     * @throws IOException
     */
    public HashMap<String, String> parseSymbol()
                            throws IOException {
        // ignore style
        if ( mif.sval != null && mif.sval.equals( "symbol" ) ) {
            LinkedList<String> list = parseCommaList( mif );

            HashMap<String, String> map = new HashMap<String, String>();

            if ( list.size() == 3 ) {
                // MapInfo 3.0 syntax
                map.put( "shape", list.poll() );
                map.put( "color", list.poll() );
                map.put( "size", list.poll() );
                return map;
            }

            if ( list.size() == 6 ) {
                // ttf font syntax
                map.put( "shape", list.poll() );
                map.put( "color", list.poll() );
                map.put( "size", list.poll() );
                map.put( "fontname", list.poll() );
                map.put( "fontstyle", list.poll() );
                map.put( "rotation", list.poll() );
                return map;
            }

            if ( list.size() == 4 ) {
                // custom bitmap file
                map.put( "filename", new File( baseFile, list.poll() ).getAbsolutePath() );
                map.put( "color", list.poll() );
                map.put( "size", list.poll() );
                map.put( "customstyle", list.poll() );
                return map;
            }
        }

        return null;
    }

    /**
     * @return a map with the pen parameters, or null, if no pen clause was found
     * @throws IOException
     */
    public HashMap<String, String> parsePen()
                            throws IOException {
        if ( mif.sval != null && mif.sval.equals( "pen" ) ) {
            LinkedList<String> list = parseCommaList( mif );

            HashMap<String, String> map = new HashMap<String, String>();

            if ( list.size() == 3 ) {
                map.put( "width", list.poll() );
                map.put( "pattern", list.poll() );
                map.put( "color", list.poll() );
            }

            return map;
        }

        return null;
    }

    /**
     * @return a map with the brush parameters, or null, if no brush clause was found
     * @throws IOException
     */
    public HashMap<String, String> parseBrush()
                            throws IOException {
        if ( mif.sval != null && mif.sval.equals( "brush" ) ) {
            LinkedList<String> list = parseCommaList( mif );
            HashMap<String, String> map = new HashMap<String, String>();

            map.put( "pattern", list.poll() );
            map.put( "forecolor", list.poll() );
            if ( list.size() > 0 ) {
                map.put( "backcolor", list.poll() );
            }

            return map;
        }

        return null;
    }

    /**
     * @return a map with the brush parameters, or null, if no brush clause was found
     * @throws IOException
     */
    public HashMap<String, String> parseText()
                            throws IOException {
        HashMap<String, String> map = new HashMap<String, String>();

        if ( mif.sval != null && mif.sval.equals( "font" ) ) {
            LinkedList<String> list = parseCommaList( mif );

            map.put( "fontname", list.poll() );
            map.put( "style", list.poll() );
            map.put( "size", list.poll() );
            map.put( "forecolor", list.poll() );
            if ( list.size() > 0 ) {
                map.put( "backcolor", list.poll() );
            }
        }

        if ( mif.sval != null && mif.sval.equals( "spacing" ) ) {
            mif.nextToken();
            mif.nextToken();
        }

        if ( mif.sval != null && mif.sval.equals( "justify" ) ) {
            mif.nextToken();
            mif.nextToken();
        }

        if ( mif.sval != null && mif.sval.equals( "angle" ) ) {
            mif.nextToken();
            mif.nextToken();
        }

        if ( mif.sval != null && mif.sval.equals( "label" ) ) {
            mif.nextToken();
            mif.nextToken(); // line
            mif.nextToken(); // simple/arrow
            mif.nextToken(); // x
            mif.nextToken(); // y
        }

        if ( map.isEmpty() ) {
            return null;
        }
        return map;
    }

}
