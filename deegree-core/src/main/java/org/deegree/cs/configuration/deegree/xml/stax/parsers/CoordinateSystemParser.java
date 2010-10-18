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

package org.deegree.cs.configuration.deegree.xml.stax.parsers;

import static org.deegree.commons.xml.stax.StAXParsingHelper.getRequiredText;
import static org.deegree.commons.xml.stax.StAXParsingHelper.nextElement;
import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.GEOGRAPHIC;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.PROJECTED;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stax-based parser for Coordinate system objects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the currently parsed crs, as well as a stack trace if something went wrong.")
public class CoordinateSystemParser extends DefinitionParser {

    private static Logger LOG = LoggerFactory.getLogger( CoordinateSystemParser.class );

    private static final QName ROOT = new QName( CRS_NS, "CRSDefinitions" );

    private static final QName COMP_ELEM = new QName( CRS_NS, "CompoundCRS" );

    private static final QName GEO_ELEM = new QName( CRS_NS, "GeographicCRS" );

    private static final QName GEOC_ELEM = new QName( CRS_NS, "GeocentricCRS" );

    private static final QName PROJ_ELEM = new QName( CRS_NS, "ProjectedCRS" );

    private final static Set<QName> knownCRS = new HashSet<QName>( 4 );

    static {
        knownCRS.add( COMP_ELEM );
        knownCRS.add( GEO_ELEM );
        knownCRS.add( GEOC_ELEM );
        knownCRS.add( PROJ_ELEM );
    }

    /**
     * @param provider
     *            to be used for callback.
     * @param configURL
     *            to be used for the configuration.
     */
    public CoordinateSystemParser( DeegreeCRSProvider<StAXResource> provider, URL configURL ) {
        super( provider, configURL );
    }

    /**
     * @param crsId
     * @return the
     * @throws CRSConfigurationException
     */
    public CoordinateSystem getCRSForId( String crsId )
                            throws CRSConfigurationException {
        if ( crsId == null || "".equals( crsId.trim() ) ) {
            return null;
        }
        String tmpCRSId = crsId.trim();
        CoordinateSystem result = getProvider().getCachedIdentifiable( CoordinateSystem.class, tmpCRSId );
        if ( result == null ) {
            try {
                final XMLStreamReader configReader = getConfigReader();
                result = parseCoordinateSystem( configReader );
                if ( result != null ) {
                    getProvider().addIdToCache( result, false );
                }
                while ( result != null && !result.hasId( tmpCRSId, false, true ) ) {
                    result = parseCoordinateSystem( configReader );
                    if ( result != null ) {
                        getProvider().addIdToCache( result, false );
                    }
                }

            } catch ( XMLStreamException e ) {
                throw new CRSConfigurationException( e );
            }
        }
        return result;
    }

    /**
     * @param reader
     *            to be parsed
     * @return an instance of the given crs or <code>null</code> if the crsDefinition is <code>null</code> or could not
     *         be mapped to a valid type.
     * @throws CRSConfigurationException
     *             if something went wrong while constructing the crs.
     * @throws XMLStreamException
     */
    public CoordinateSystem parseCoordinateSystem( XMLStreamReader reader )
                            throws CRSConfigurationException, XMLStreamException {
        if ( reader == null || !super.moveReaderToNextIdentifiable( reader, knownCRS ) ) {
            LOG.debug( "Could not get a crs, no more definitions left." );
            return null;
        }
        QName crsType = reader.getName();

        // the crsDefinition should point to the correct crs.
        CoordinateSystem result = null;
        try {
            if ( GEO_ELEM.equals( crsType ) ) {
                result = parseGeographicCRS( reader );
            } else if ( PROJ_ELEM.equals( crsType ) ) {
                result = parseProjectedCRS( reader );
            } else if ( GEOC_ELEM.equals( crsType ) ) {
                result = parseGeocentricCRS( reader );
            } else if ( COMP_ELEM.equals( crsType ) ) {
                result = parseCompoundCRS( reader );
            }
        } catch ( XMLStreamException e ) {
            LOG.error( "Error while reading from xml stream." );
        }

        if ( result == null && LOG.isDebugEnabled() ) {
            LOG.debug( "The element with name " + crsType + " could not be mapped to a valid deegreec-crs, currently "
                       + knownCRS + " are supported." );
        }
        return result;
    }

    /**
     * Creates an axis array for the given crs element.
     * 
     * @param reader
     *            to be parsed from
     * @return an Array of axis defining their order.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred, or the axisorder uses
     *             names which were not defined in the axis elements.
     * @throws XMLStreamException
     */
    protected Axis[] parseAxisOrder( XMLStreamReader reader )
                            throws CRSConfigurationException, XMLStreamException {
        List<Axis> confAxis = new ArrayList<Axis>();
        while ( new QName( CRS_NS, "Axis" ).equals( reader.getName() ) ) {
            // end tag Axis
            StAXParsingHelper.nextElement( reader );
            try {
                confAxis.add( parseAxis( reader ) );
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Axis",
                                                                          e.getMessage() ), e );
            }
            nextElement( reader );

        }
        if ( confAxis.isEmpty() ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Axis",
                                                                      "No axis were parsed." ) );
        }
        return confAxis.toArray( new Axis[confAxis.size()] );
    }

    /**
     * Parses an axis element
     * 
     * @param reader
     * @return the parsed axis element
     * @throws XMLStreamException
     * @throws XMLParsingException
     */
    protected Axis parseAxis( XMLStreamReader reader )
                            throws XMLStreamException, XMLParsingException {
        String axisName = getRequiredText( reader, new QName( CRS_NS, "Name" ), true );
        Unit unit = parseUnit( reader, true );
        String axisOrientation = getRequiredText( reader, new QName( CRS_NS, "AxisOrientation" ), true );
        return new Axis( unit, axisName, axisOrientation );

    }

    /**
     * Retrieves a transformation from the resource.
     * 
     * @param transformationDefinition
     * @return the parsed transformation or <code>null</code> if no transformation could be parsed.
     */
    public Transformation parseTransformation( OMElement transformationDefinition ) {
        throw new UnsupportedOperationException(
                                                 "The parsing of transformations is not supported by this version of the deegree crs parser." );
    }

    /**
     * Parse all polynomial transformations for a given crs.
     * 
     * @param reader
     *            to parse the transformations for.
     * @return the list of transformations or the empty list if no transformations were found. Never <code>null</code>.
     */
    protected List<Transformation> parseAlternativeTransformations( XMLStreamReader reader ) {
        List<Transformation> result = new LinkedList<Transformation>();
        return result;
    }

    /**
     * @param reader
     *            from which the crs is to be created (using chached datums, conversioninfos and projections).
     * @return a projected coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     * @throws XMLStreamException
     */
    protected CoordinateSystem parseProjectedCRS( XMLStreamReader reader )
                            throws CRSConfigurationException, XMLStreamException {
        if ( reader == null ) {
            return null;
        }
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( reader );

        Axis[] axis = parseAxisOrder( reader );

        List<Transformation> transformations = parseAlternativeTransformations( reader );
        // Unit units = parseUnit( crsElement );

        String usedGeographicCRS = null;
        try {
            usedGeographicCRS = getRequiredText( reader, new QName( CRS_NS, "UsedGeographicCRS" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projectiontType or usedGeographicCRS",
                                                                      reader.getLocalName(), e.getMessage() ), e );
        }

        String usedProjection = null;
        try {
            usedProjection = getRequiredText( reader, new QName( CRS_NS, "UsedProjection" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "UsedProjection",
                                                                      reader.getLocalName(), e.getMessage() ), e );
        }
        // first create the datum.
        if ( usedGeographicCRS == null || "".equals( usedGeographicCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
                                                                      "UsedGeographicCRS", id.getCode() ) );
        }
        GeographicCRS geoCRS = (GeographicCRS) getProvider().getCRSByCode( CRSCodeType.valueOf( usedGeographicCRS ) );
        if ( geoCRS == null || geoCRS.getType() != GEOGRAPHIC ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_CRSREF",
                                                                      id.getCode(), usedGeographicCRS ) );
        }

        // then the projection.
        Projection projection = getProvider().getProjection( usedProjection, geoCRS );
        if ( projection == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_PROJREF",
                                                                      id.getCode(), usedProjection ) );
        }

        // adding to cache will be done in AbstractCRSProvider.
        return new ProjectedCRS( transformations, projection, axis, id );
    }

    /**
     * @param reader
     *            positioned on the crs to be created (using cached datums, conversioninfos and projections).
     * 
     * @return a geographic coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     * @throws XMLStreamException
     */
    protected CoordinateSystem parseGeographicCRS( XMLStreamReader reader )
                            throws CRSConfigurationException, XMLStreamException {
        if ( reader == null ) {
            return null;
        }
        CRSIdentifiable id = parseIdentifiable( reader );
        // no need to get it from the cache, because the abstract provider checked it already.
        Axis[] axis = parseAxisOrder( reader );

        List<Transformation> transformations = parseAlternativeTransformations( reader );
        // get the datum
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( reader, id.getCode().getOriginal() );

        GeographicCRS result = new GeographicCRS( transformations, usedDatum, axis, id );
        // adding to cache will be done in AbstractCRSProvider.
        return result;
    }

    /**
     * @param reader
     *            from which the crs is to be created (using cached datums, conversioninfos and projections).
     * @return a geocentric coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     * @throws XMLStreamException
     */
    protected CoordinateSystem parseGeocentricCRS( XMLStreamReader reader )
                            throws CRSConfigurationException, XMLStreamException {
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( reader );
        Axis[] axis = parseAxisOrder( reader );
        List<Transformation> transformations = parseAlternativeTransformations( reader );
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( reader, id.getCode().getOriginal() );
        GeocentricCRS result = new GeocentricCRS( transformations, usedDatum, axis, id );
        // adding to cache will be done in AbstractCRSProvider.
        return result;
    }

    /**
     * @param reader
     *            from which the crs is to be created.
     * 
     * @return a compound coordinatesystem based on the given xml-element.
     * @throws XMLStreamException
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    protected CoordinateSystem parseCompoundCRS( XMLStreamReader reader )
                            throws XMLStreamException {
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( reader );
        String usedCRS = null;
        try {
            usedCRS = getRequiredText( reader, new QName( CRS_NS, "UsedCRS" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "usedCRS",
                                                                      ( ( reader == null ) ? "null"
                                                                                          : reader.getLocalName() ),
                                                                      e.getMessage() ), e );

        }
        if ( usedCRS == null || "".equals( usedCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedCRS",
                                                                      id.getCode() ) );
        }

        CoordinateSystem usedCoordinateSystem = getProvider().getCRSByCode( CRSCodeType.valueOf( usedCRS ) );
        if ( usedCoordinateSystem == null
             || ( usedCoordinateSystem.getType() != GEOGRAPHIC && usedCoordinateSystem.getType() != PROJECTED ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_COMPOUND_FALSE_CRSREF", id.getCode(),
                                                                      usedCRS ) );
        }

        // get the datum
        Axis heightAxis = null;
        try {
            StAXParsingHelper.skipRequiredElement( reader, new QName( CRS_NS, "HeightAxis" ) );
            heightAxis = parseAxis( reader );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "HeightAxis",
                                                                      e.getLocalizedMessage() ), e );
        }

        double defaultHeight = StAXParsingHelper.getElementTextAsDouble( reader, new QName( CRS_NS, "DefaultHeight" ),
                                                                         0, true );
        // adding to cache will be done in AbstractCRSProvider.
        return new CompoundCRS( heightAxis, usedCoordinateSystem, defaultHeight, id );
    }

    /**
     * Parses the required usedDatum element from the given parentElement (probably a crs element).
     * 
     * @param reader
     *            pointing to the required usedDatum element from.
     * @param parentID
     *            optional for an appropriate error message.
     * @return the Datum.
     * @throws CRSConfigurationException
     *             if a parsing error occurred, the node was not defined or an illegal id reference (not found) was
     *             given.
     * @throws XMLStreamException
     */
    protected GeodeticDatum parseReferencedGeodeticDatum( XMLStreamReader reader, String parentID )
                            throws CRSConfigurationException, XMLStreamException {
        String datumID = null;
        try {
            datumID = getRequiredText( reader, new QName( CRS_NS, "UsedDatum" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "datumID", parentID,
                                                                      e.getMessage() ), e );
        }
        if ( datumID == null || "".equals( datumID.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedDatum",
                                                                      parentID ) );
        }
        GeodeticDatum usedDatum = getProvider().getGeodeticDatumForId( datumID );
        if ( usedDatum == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_USEDDATUM_IS_NULL", datumID, parentID ) );
        }
        return usedDatum;
    }

    @Override
    protected QName expectedRootName() {
        return ROOT;
    }

    /**
     * @return the list of all available crs id's
     */
    public List<CRSCodeType[]> getAvailableCRSs() {

        List<CRSCodeType[]> result = new ArrayList<CRSCodeType[]>( 4000 );
        URL url = super.getConfigURL();
        InputStream confStream = null;
        try {
            confStream = url.openStream();
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader( url.toExternalForm(),
                                                                                          confStream );
            StAXParsingHelper.nextElement( reader );

            CoordinateSystem crs = parseCoordinateSystem( reader );
            while ( crs != null ) {
                getProvider().addIdToCache( crs, false );
                result.add( crs.getCodes() );
                crs = parseCoordinateSystem( reader );
            }
        } catch ( Exception e ) {
            LOG.debug( "Could not get available crs's stack: ", e );
            LOG.error( "Could not get available crs's because: " + e.getLocalizedMessage() );
        } finally {
            if ( confStream != null ) {
                try {
                    confStream.close();
                } catch ( IOException e ) {
                    LOG.error( "Could not close the stream, letting it open because: " + e.getLocalizedMessage() );
                }
            }
        }
        return result;
    }
}
