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

package org.deegree.cs.configuration.deegree.xml.stax.parsers;

import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.slf4j.Logger;

/**
 * Stax-based configuration parser for prime meridian objects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the currently parsed primemeridian, as well as a stack trace if something went wrong.")
public class PrimemeridianParser extends DefinitionParser {
    private static final Logger LOG = getLogger( PrimemeridianParser.class );

    private final static QName PM_ELEMENT = new QName( CRS_NS, "PrimeMeridian" );

    private static final QName ROOT = new QName( CRS_NS, "PMDefinitions" );

    /**
     * @param provider
     * @param primeMeridanFile
     */
    public PrimemeridianParser( DeegreeCRSProvider<StAXResource> provider, URL primeMeridanFile ) {
        super( provider, primeMeridanFile );
    }

    /**
     * @param meridianId
     *            the id to search for.
     * @return the primeMeridian with given id or <code>null</code>
     * @throws CRSConfigurationException
     *             if the longitude was not set or the units could not be parsed.
     */
    public PrimeMeridian getPrimeMeridianForId( String meridianId )
                            throws CRSConfigurationException {
        if ( meridianId == null || "".equals( meridianId.trim() ) ) {
            return null;
        }
        PrimeMeridian result = getProvider().getCachedIdentifiable( PrimeMeridian.class, meridianId );
        if ( result == null ) {
            try {
                result = parsePrimeMeridian( getConfigReader() );
                while ( result != null && !result.hasId( meridianId, false, true ) ) {
                    result = parsePrimeMeridian( getConfigReader() );
                }
            } catch ( XMLStreamException e ) {
                throw new CRSConfigurationException( e );
            }
        }

        return result;
    }

    /**
     * @param reader
     *            to use
     * @return the next PrimeMeridian or null if no more definitions were found.
     * @throws XMLStreamException
     */
    protected PrimeMeridian parsePrimeMeridian( XMLStreamReader reader )
                            throws XMLStreamException {
        if ( reader == null || !super.moveReaderToNextIdentifiable( reader, PM_ELEMENT ) ) {
            LOG.debug( "Could not get prime meridian no more definitions found." );
            return null;
        }

        CRSIdentifiable id = parseIdentifiable( reader );
        Unit units = parseUnit( reader, true );
        double longitude = 0;
        try {
            longitude = super.parseLatLonType( reader, new QName( CRS_NS, "Longitude" ), true, 0 );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        }
        return getProvider().addIdToCache( new PrimeMeridian( units, longitude, id ), false );
    }

    @Override
    protected QName expectedRootName() {
        return ROOT;
    }

}
