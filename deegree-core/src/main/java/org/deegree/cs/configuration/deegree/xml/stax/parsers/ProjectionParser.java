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
import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.Mercator;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.slf4j.Logger;

/**
 * Stax-based configuration parser for projection objects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the currently parsed projections, as well as a stack trace if something went wrong.")
public class ProjectionParser extends DefinitionParser {

    private static final Logger LOG = getLogger( ProjectionParser.class );

    private static final QName ROOT = new QName( CRS_NS, "ProjectionDefinitions" );

    private static final QName USER_ELEM = new QName( CRS_NS, "UserDefined" );

    private static final QName LAEA_ELEM = new QName( CRS_NS, "LambertAzimuthalEqualArea" );

    private static final QName LCC_ELEM = new QName( CRS_NS, "LambertConformalConic" );

    private static final QName SA_ELEM = new QName( CRS_NS, "StereographicAzimuthal" );

    private static final QName SAA_ELEM = new QName( CRS_NS, "StereographicAlternative" );

    private static final QName TMERC_ELEM = new QName( CRS_NS, "TransverseMercator" );

    private static final QName MERC_ELEM = new QName( CRS_NS, "Mercator" );

    private final static Set<QName> knownProjections = new HashSet<QName>( 7 );

    /*
     * each id maps to the 'default' codetype, which is used to lookup a list of crs's with different underlying
     * Geographic crs
     */
    private final Map<String, CRSCodeType> idToCode = new HashMap<String, CRSCodeType>();

    /* each code type has a list of crs's with different underlying Geographic crs */
    private final Map<CRSCodeType, List<Projection>> duplicates = new HashMap<CRSCodeType, List<Projection>>();

    static {
        knownProjections.add( USER_ELEM );
        knownProjections.add( LAEA_ELEM );
        knownProjections.add( LCC_ELEM );
        knownProjections.add( SA_ELEM );
        knownProjections.add( SAA_ELEM );
        knownProjections.add( TMERC_ELEM );
        knownProjections.add( MERC_ELEM );
    }

    /**
     * @param provider
     * @param configURL
     */
    public ProjectionParser( DeegreeCRSProvider<StAXResource> provider, URL configURL ) {
        super( provider, configURL );
    }

    /**
     * @param reader
     *            to
     * @param underlyingCRS
     * @return the next datum on the stream.
     * @throws XMLStreamException
     */
    protected Projection parseProjection( XMLStreamReader reader, GeographicCRS underlyingCRS )
                            throws XMLStreamException {
        if ( reader == null || !super.moveReaderToNextIdentifiable( reader, knownProjections ) ) {
            LOG.debug( "Could not get projection, no more definitions left." );
            return null;
        }

        QName projectionName = reader.getName();
        boolean tmercNorthern = true;
        if ( TMERC_ELEM.equals( projectionName ) ) {
            // change schema to let projection be identifiable. fix method geodetic
            tmercNorthern = StAXParsingHelper.getAttributeValueAsBoolean( reader, null, "northernHemisphere", true );
        }
        LOG.debug( "At element: " + projectionName );
        String className = StAXParsingHelper.getAttributeValue( reader, "class" );

        CRSIdentifiable id = parseIdentifiable( reader );
        // All projections will have following parameters
        double latitudeOfNaturalOrigin = parseLatLonType( reader, new QName( CRS_NS, "LatitudeOfNaturalOrigin" ),
                                                          false, 0 );

        double longitudeOfNaturalOrigin = parseLatLonType( reader, new QName( CRS_NS, "LongitudeOfNaturalOrigin" ),
                                                           false, 0 );

        double scaleFactor = StAXParsingHelper.getElementTextAsDouble( reader, new QName( CRS_NS, "ScaleFactor" ), 1,
                                                                       true );
        double falseEasting = getElementTextAsDouble( reader, new QName( CRS_NS, "FalseEasting" ), 0, true );
        double falseNorthing = getElementTextAsDouble( reader, new QName( CRS_NS, "FalseNorthing" ), 0, true );

        Point2d naturalOrigin = new Point2d( longitudeOfNaturalOrigin, latitudeOfNaturalOrigin );
        // rb: the projections should actually be made aware of the axis units.
        Unit units = Unit.METRE;
        Projection result = null;
        if ( className != null && !"".equals( className.trim() ) ) {
            result = instantiateConfiguredClass( reader, className, id, underlyingCRS, falseNorthing, falseEasting,
                                                 naturalOrigin, units, scaleFactor );

        } else {
            if ( TMERC_ELEM.equals( projectionName ) ) {
                result = new TransverseMercator( tmercNorthern, underlyingCRS, falseNorthing, falseEasting,
                                                 naturalOrigin, units, scaleFactor, id );
            } else if ( LAEA_ELEM.equals( projectionName ) ) {
                result = new LambertAzimuthalEqualArea( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                        units, scaleFactor, id );
            } else if ( LCC_ELEM.equals( projectionName ) ) {

                double firstP = parseLatLonType( reader, new QName( CRS_NS, "FirstParallelLatitude" ), false,
                                                 Double.NaN );
                double secondP = parseLatLonType( reader, new QName( CRS_NS, "SecondParallelLatitude" ), false,
                                                  Double.NaN );
                result = new LambertConformalConic( firstP, secondP, underlyingCRS, falseNorthing, falseEasting,
                                                    naturalOrigin, units, scaleFactor, id );
            } else if ( SA_ELEM.equals( projectionName ) ) {
                double trueScaleL = parseLatLonType( reader, new QName( CRS_NS, "TrueScaleLatitude" ), false,
                                                     Double.NaN );
                result = new StereographicAzimuthal( trueScaleL, underlyingCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scaleFactor, id );
            } else if ( SAA_ELEM.equals( projectionName ) ) {
                result = new StereographicAlternative( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                       units, scaleFactor, id );
            } else if ( MERC_ELEM.equals( projectionName ) ) {
                result = new Mercator( underlyingCRS, falseNorthing, falseEasting, naturalOrigin, units, scaleFactor,
                                       id );
            } else {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_INVALID_PROJECTION",
                                                                          projectionName, knownProjections.toString() ) );

            }
        }
        // throw new CRSConfigurationException( Messages.getMessage( "CRS_STAX_CONFIG_PARSE_EXCEPTION",
        // "projection parameters", e.getMessage() ), e );
        if ( result != null ) {
            addToPrivateCache( result );
            result = getProvider().addIdToCache( result, false );
        }
        return result;
    }

    private void addToPrivateCache( Projection result ) {
        if ( result != null ) {
            CRSCodeType codeKey = idToCode.get( result.getCode().getOriginal().toLowerCase() );
            if ( codeKey == null ) {
                codeKey = result.getCode();
            }
            List<Projection> dupList = duplicates.get( codeKey );
            if ( dupList == null ) {
                dupList = new ArrayList<Projection>();
                duplicates.put( codeKey, dupList );
            }
            dupList.add( result );
            for ( CRSCodeType code : result.getCodes() ) {
                String cK = code.getOriginal().toLowerCase();
                if ( !idToCode.containsKey( cK ) ) {
                    idToCode.put( cK, codeKey );
                }
            }

        }
    }

    private Projection getFromCache( String id, GeographicCRS underlying ) {
        CRSCodeType codeKey = idToCode.get( id.toLowerCase() );
        if ( codeKey != null ) {
            List<Projection> dupList = duplicates.get( codeKey );
            if ( dupList != null && !dupList.isEmpty() ) {
                for ( Projection p : dupList ) {
                    if ( p != null && p.hasId( id, false, true ) && p.getGeographicCRS().equals( underlying ) ) {
                        return p;
                    }
                }
            }
        }
        return null;

    }

    /**
     * @param className
     * @param underlyingCRS
     * @return
     */
    private Projection instantiateConfiguredClass( XMLStreamReader reader, String className, CRSIdentifiable id,
                                                   GeographicCRS underlyingCRS, double falseNorthing,
                                                   double falseEasting, Point2d naturalOrigin, Unit units,
                                                   double scaleFactor ) {
        Projection result = null;
        LOG.debug( "Trying to load user defined projection class: " + className );
        try {
            Class<?> t = Class.forName( className );
            t.asSubclass( Projection.class );
            /**
             * try to get a constructor with a native type as a parameter, by going over the 'names' of the classes of
             * the parameters, the native type will show up as the typename e.g. int or long..... <code>
             * public Projection( CRSIdentifiable, GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
             * Point2d naturalOrigin, Unit units, double scale, XMLStreamReader reader )
             * </code>
             */

            /**
             * Load the constructor with the standard projection values and the element list.
             */
            Constructor<?> constructor = t.getConstructor( CRSIdentifiable.class, GeographicCRS.class, double.class,
                                                           double.class, Point2d.class, Unit.class, double.class,
                                                           XMLStreamReader.class );
            result = (Projection) constructor.newInstance( id, underlyingCRS, falseNorthing, falseEasting,
                                                           naturalOrigin, units, scaleFactor, reader );
        } catch ( ClassNotFoundException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( SecurityException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( NoSuchMethodException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( IllegalArgumentException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( InstantiationException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( IllegalAccessException e ) {
            LOG.error( e.getMessage(), e );
        } catch ( InvocationTargetException e ) {
            LOG.error( e.getMessage(), e );
        }
        if ( result == null ) {
            LOG.debug( "Loading of user defined projection class: " + className + " was not successful" );
        }
        return result;

    }

    /**
     * @param projectionId
     * @param underlyingCRS
     *            the crs to which the projection is defined.
     * @return the
     * @throws CRSConfigurationException
     */
    public Projection getProjectionForId( String projectionId, GeographicCRS underlyingCRS )
                            throws CRSConfigurationException {
        if ( projectionId == null || "".equals( projectionId.trim() ) ) {
            return null;
        }
        String tmpProjectionId = projectionId.trim();
        Projection result = getProvider().getCachedIdentifiable( Projection.class, tmpProjectionId );
        if ( result == null ) {
            try {
                result = parseProjection( getConfigReader(), underlyingCRS );
                while ( result != null && !result.hasId( tmpProjectionId, false, true ) ) {
                    result = parseProjection( getConfigReader(), underlyingCRS );
                }
            } catch ( XMLStreamException e ) {
                throw new CRSConfigurationException( e );
            }
        }
        // rb: found a matching id, but the base geographic may still be different so let's try it
        if ( result != null ) {
            if ( underlyingCRS != null && !underlyingCRS.equals( result.getGeographicCRS() ) ) {
                Projection p = getFromCache( projectionId, underlyingCRS );
                if ( p == null ) {
                    result = result.clone( underlyingCRS );
                    addToPrivateCache( result );
                } else {
                    result = p;
                }
            }

        }
        return result;
    }

    @Override
    protected QName expectedRootName() {
        return ROOT;
    }
}
