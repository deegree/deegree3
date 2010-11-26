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

import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsDouble;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredElementTextAsDouble;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.slf4j.Logger;

/**
 * Stax-based configuration parser for ellipsoid objects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the currently parsed ellipsoid, as well as a stack trace if something went wrong.")
public class EllipsoidParser extends DefinitionParser {

    private static final Logger LOG = getLogger( EllipsoidParser.class );

    private static final QName ELLIPS_ELEM = new QName( CRS_NS, "Ellipsoid" );

    private static final QName ROOT = new QName( CRS_NS, "EllipsoidDefinitions" );

    /**
     * @param provider
     * @param confURL
     */
    public EllipsoidParser( DeegreeCRSProvider<StAXResource> provider, URL confURL ) {
        super( provider, confURL );
    }

    /**
     * Tries to find a cached ellipsoid, if not found, the config will be checked.
     * 
     * @param ellipsoidID
     * @return an ellipsoid or <code>null</code> if no ellipsoid with given id was found, or the id was
     *         <code>null</code> or empty.
     * @throws CRSConfigurationException
     *             if something went wrong.
     */
    public Ellipsoid getEllipsoidForId( String ellipsoidID )
                            throws CRSConfigurationException {
        if ( ellipsoidID == null || "".equals( ellipsoidID.trim() ) ) {
            return null;
        }
        Ellipsoid result = getProvider().getCachedIdentifiable( Ellipsoid.class, ellipsoidID );
        if ( result == null ) {
            try {
                result = parseEllipsoid( getConfigReader() );
                while ( result != null && !result.hasId( ellipsoidID, false, true ) ) {
                    result = parseEllipsoid( getConfigReader() );
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
     * @return the next ellipsoid or null if no more definitions were found.
     * @throws XMLStreamException
     */
    protected Ellipsoid parseEllipsoid( XMLStreamReader reader )
                            throws XMLStreamException {
        if ( reader == null || !super.moveReaderToNextIdentifiable( reader, ELLIPS_ELEM ) ) {
            LOG.debug( "Could not get ellipsoid no more definitions found." );
            return null;
        }

        CRSIdentifiable id = parseIdentifiable( reader );

        Unit units = parseUnit( reader, true );

        double semiMajor = Double.NaN;
        double inverseFlattening = Double.NaN;
        double eccentricity = Double.NaN;
        double semiMinorAxis = Double.NaN;

        try {
            semiMajor = getRequiredElementTextAsDouble( reader, new QName( CRS_NS, "SemiMajorAxis" ), true );
            inverseFlattening = getElementTextAsDouble( reader, new QName( CRS_NS, "InverseFlattening" ), Double.NaN,
                                                        true );
            eccentricity = getElementTextAsDouble( reader, new QName( CRS_NS, "Eccentricity" ), Double.NaN, true );
            semiMinorAxis = getElementTextAsDouble( reader, new QName( CRS_NS, "SemiMinorAxis" ), Double.NaN, true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "ellipsoid",
                                                                      ELLIPS_ELEM, e.getMessage() ), e );
        }
        if ( Double.isNaN( inverseFlattening ) && Double.isNaN( eccentricity ) && Double.isNaN( semiMinorAxis ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_STAX_CONFIG_ELLIPSOID_MISSES_PARAM",
                                                                      reader.getLocation() ) );
        }
        Ellipsoid result = null;
        if ( !Double.isNaN( inverseFlattening ) ) {
            result = new Ellipsoid( semiMajor, units, inverseFlattening, id.getCodes(), id.getNames(),
                                    id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
        } else if ( !Double.isNaN( eccentricity ) ) {
            result = new Ellipsoid( semiMajor, eccentricity, units, id.getCodes(), id.getNames(), id.getVersions(),
                                    id.getDescriptions(), id.getAreasOfUse() );
        } else {
            result = new Ellipsoid( units, semiMajor, semiMinorAxis, id.getCodes(), id.getNames(), id.getVersions(),
                                    id.getDescriptions(), id.getAreasOfUse() );
        }
        result = getProvider().addIdToCache( result, false );
        nextElement( reader );// end ellipsoid

        return result;
    }

    @Override
    protected QName expectedRootName() {
        return ROOT;
    }
}
