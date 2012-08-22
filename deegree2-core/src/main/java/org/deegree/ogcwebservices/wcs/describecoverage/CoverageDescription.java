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
package org.deegree.ogcwebservices.wcs.describecoverage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.deegree.framework.util.NetWorker;
import org.deegree.model.crs.UnknownCRSException;
import org.xml.sax.SAXException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */

public class CoverageDescription {

    private CoverageOffering[] coverageOffering = new CoverageOffering[0];

    private Map<String, CoverageOffering> map = new HashMap<String, CoverageOffering>( 100 );

    private String version = "1.0.0";

    /**
     * creates a <tt>CoverageDescription</tt> from a DOM document assigen by the passed URL
     *
     * @return created <tt>CoverageDescription</tt>
     * @exception IOException
     * @exception SAXException
     * @exception InvalidCoverageDescriptionExcpetion
     */
    public static CoverageDescription createCoverageDescription( URL url )
                            throws IOException, SAXException, InvalidCoverageDescriptionExcpetion {
        CoverageDescriptionDocument covDescDoc = new CoverageDescriptionDocument();
        if ( url == null ) {
            throw new InvalidCoverageDescriptionExcpetion( "location URL for a coverage description document is null" );
        }
        if ( !NetWorker.existsURL( url ) ) {
            throw new InvalidCoverageDescriptionExcpetion( "location URL: " + url
                                                           + "for a coverage description document doesn't exist" );
        }
        covDescDoc.load( url );
        return new CoverageDescription( covDescDoc );
    }

    /**
     * @param covDescDoc
     * @exception InvalidCoverageDescriptionExcpetion
     */
    public CoverageDescription( CoverageDescriptionDocument covDescDoc ) throws InvalidCoverageDescriptionExcpetion {
        setVersion( covDescDoc.getVersion() );
        try {
            setCoverageOfferings( covDescDoc.getCoverageOfferings() );
        } catch ( UnknownCRSException e ) {
            throw new InvalidCoverageDescriptionExcpetion( e.getMessage() );
        }
    }

    /**
     * @param coverageOffering
     */
    public CoverageDescription( CoverageOffering[] coverageOffering, String version ) {
        setVersion( version );
        setCoverageOfferings( coverageOffering );
    }

    /**
     * @return Returns the coverageOffering.
     */
    public CoverageOffering[] getCoverageOfferings() {
        return coverageOffering;
    }

    /**
     * returns a <tt>CoverageOffering</tt> identified by its name. if no <tt>CoverageOffering</tt>
     * is known by a <tt>CoverageDescription</tt> with the passed name, <tt>null</tt> will be
     * returned.
     *
     * @param name
     * @return a <tt>CoverageOffering</tt> identified by its name.
     */
    public CoverageOffering getCoverageOffering( String name ) {
        return map.get( name );
    }

    /**
     * @param coverageOffering
     *            The coverageOffering to set.
     */
    public void setCoverageOfferings( CoverageOffering[] coverageOffering ) {
        if ( coverageOffering == null ) {
            coverageOffering = new CoverageOffering[0];
        }
        map.clear();
        for ( int i = 0; i < coverageOffering.length; i++ ) {
            map.put( coverageOffering[i].getName(), coverageOffering[i] );
        }
        this.coverageOffering = coverageOffering;
    }

    /**
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     */
    public void setVersion( String version ) {
        this.version = version;
    }

}
