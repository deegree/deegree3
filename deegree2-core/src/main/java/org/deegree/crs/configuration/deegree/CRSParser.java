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

package org.deegree.crs.configuration.deegree;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.vecmath.Point2d;

import org.deegree.crs.Identifiable;
import org.deegree.crs.components.Axis;
import org.deegree.crs.components.Ellipsoid;
import org.deegree.crs.components.GeodeticDatum;
import org.deegree.crs.components.PrimeMeridian;
import org.deegree.crs.components.Unit;
import org.deegree.crs.configuration.resources.XMLFileResource;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeocentricCRS;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.crs.projections.conic.LambertConformalConic;
import org.deegree.crs.projections.cylindric.Mercator;
import org.deegree.crs.projections.cylindric.TransverseMercator;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.crs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.crs.transformations.polynomial.PolynomialTransformation;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * The <code>CRSParser</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class CRSParser extends XMLFileResource {

    /**
     * 
     */
    private static final long serialVersionUID = -5621078575657838568L;

    private static ILogger LOG = LoggerFactory.getLogger( CRSParser.class );

    /**
     * The prefix to use.
     */
    private final static String PRE = CommonNamespaces.CRS_PREFIX + ":";

    private final static String XPATH_PRE = "*[" + PRE + "id='";

    /**
     * @param provider
     *            to be used for callback.
     * @param rootElement
     *            to be used for the configuration.
     */
    public CRSParser( DeegreeCRSProvider provider, Element rootElement ) {
        super( provider, rootElement );
    }

    /**
     * @param provider
     * @param properties
     */
    public CRSParser( DeegreeCRSProvider provider, Properties properties ) {
        this( provider, properties, "definitions", CommonNamespaces.CRSNS.toASCIIString() );
        // TODO Auto-generated constructor stub
    }

    /**
     * @param provider
     * @param properties
     * @param defaultRootElement
     * @param namespace
     */
    public CRSParser( DeegreeCRSProvider provider, Properties properties, String defaultRootElement, String namespace ) {
        super( provider, properties, defaultRootElement, namespace );
    }

    /**
     * @param crsDefintion
     *            to be parsed
     * @return an instance of the given crs or <code>null</code> if the crsDefinition is <code>null</code> or could
     *         not be mapped to a valid type.
     * @throws CRSConfigurationException
     *             if something went wrong while constructing the crs.
     */
    public CoordinateSystem parseCoordinateSystem( Element crsDefintion )
                            throws CRSConfigurationException {
        if ( crsDefintion == null ) {
            return null;
        }
        String crsType = crsDefintion.getLocalName();

        CoordinateSystem result = null;
        if ( "geographicCRS".equalsIgnoreCase( crsType ) ) {
            result = parseGeographicCRS( crsDefintion );
        } else if ( "projectedCRS".equalsIgnoreCase( crsType ) ) {
            result = parseProjectedCRS( crsDefintion );
        } else if ( "geocentricCRS".equalsIgnoreCase( crsType ) ) {
            result = parseGeocentricCRS( crsDefintion );
        } else if ( "compoundCRS".equalsIgnoreCase( crsType ) ) {
            result = parseCompoundCRS( crsDefintion );
        }

        if ( result == null && LOG.isDebug() ) {
            LOG.logDebug( "The element with localname "
                          + crsDefintion.getLocalName()
                          + " could not be mapped to a valid deegreec-crs, currently projectedCRS, geographicCRS, geocentricCRS and compoundCRS are supported." );
        }
        return result;
    }

    public Element getURIAsType( String uri )
                            throws IOException {
        if ( uri == null || "".equals( uri ) ) {
            return null;
        }
        String id = uri.trim().toUpperCase();
        Element crsElement = null;
        // String xPath ="//*[crs:id='EPSG:31466']";
        String xPath = XPATH_PRE + id + "']";
        try {
            crsElement = XMLTools.getElement( getRootElement(), xPath, nsContext );
        } catch ( XMLParsingException e ) {
            LOG.logError( Messages.getMessage( "CRS_CONFIG_NO_RESULT_FOR_ID", id, e.getMessage() ), e );
        }
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Trying to find elements with xpath: " + xPath
                          + ( ( crsElement == null ) ? " [failed]" : " [success]" ) );
        }
        return crsElement;
    }

    public Helmert getWGS84Transformation( GeographicCRS sourceCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        return sourceCRS.getGeodeticDatum().getWGS84Conversion();
    }

    /**
     * @return the version of the root element of the empty string if no version attribute was found in the root
     *         element.
     * @throws CRSConfigurationException
     *             if the root element is empty
     */
    public String getVersion()
                            throws CRSConfigurationException {
        if ( getRootElement() == null ) {
            throw new CRSConfigurationException( "The crs parser has no root element, this cannot be." );
        }
        return getRootElement().getAttribute( "version" );
    }

    /**
     * Parses all elements of the identifiable object.
     * 
     * @param element
     *            the xml-representation of the id-object
     * @return the identifiable object or <code>null</code> if no id was given.
     * @throws CRSConfigurationException
     */
    protected Identifiable parseIdentifiable( Element element )
                            throws CRSConfigurationException {
        try {
            String[] identifiers = XMLTools.getNodesAsStrings( element, PRE + "id", nsContext );
            if ( identifiers == null || identifiers.length == 0 ) {
                String msg = Messages.getMessage( "CRS_CONFIG_NO_ID", ( ( element == null ) ? "null"
                                                                                           : element.getLocalName() ) );
                throw new CRSConfigurationException( msg );
            }
            for ( int i = 0; i < identifiers.length; ++i ) {
                if ( identifiers[i] != null ) {
                    identifiers[i] = identifiers[i].toUpperCase().trim();
                }
            }
            String[] names = XMLTools.getNodesAsStrings( element, PRE + "name", nsContext );
            String[] versions = XMLTools.getNodesAsStrings( element, PRE + "version", nsContext );
            String[] descriptions = XMLTools.getNodesAsStrings( element, PRE + "description", nsContext );
            String[] areasOfUse = XMLTools.getNodesAsStrings( element, PRE + "areaOfUse", nsContext );
            return new Identifiable( identifiers, names, versions, descriptions, areasOfUse );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Identifiable",
                                                                      ( ( element == null ) ? "null"
                                                                                           : element.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
    }

    /**
     * Creates an axis array for the given crs element.
     * 
     * @param crsElement
     *            to be parsed
     * @return an Array of axis defining their order.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred, or the axisorder uses
     *             names which were not defined in the axis elements.
     */
    protected Axis[] parseAxisOrder( Element crsElement )
                            throws CRSConfigurationException {
        String axisOrder = null;
        try {
            axisOrder = XMLTools.getRequiredNodeAsString( crsElement, PRE + "axisOrder", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "AxisOrder",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        if ( axisOrder == null || "".equals( axisOrder.trim() ) ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "AxisOrder",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      " axisOrder element may not be empty" ) );
        }
        axisOrder = axisOrder.trim();
        String[] order = axisOrder.trim().split( "," );
        Axis[] axis = new Axis[order.length];
        String XPATH = PRE + "Axis[" + PRE + "name = '";
        for ( int i = 0; i < order.length; ++i ) {
            String t = order[i];
            if ( t != null && !"".equals( t.trim() ) ) {
                t = t.trim();
                try {
                    Element axisElement = XMLTools.getRequiredElement( crsElement, XPATH + t + "']", nsContext );
                    String axisOrientation = XMLTools.getRequiredNodeAsString( axisElement, PRE + "axisOrientation",
                                                                               nsContext );
                    Unit unit = parseUnit( axisElement );
                    axis[i] = new Axis( unit, t, axisOrientation );
                } catch ( XMLParsingException e ) {
                    throw new CRSConfigurationException(
                                                         Messages.getMessage(
                                                                              "CRS_CONFIG_PARSE_ERROR",
                                                                              "Axis",
                                                                              ( ( crsElement == null ) ? "null"
                                                                                                      : crsElement.getLocalName() ),
                                                                              e.getMessage() ), e );
                }
            }
        }
        return axis;
    }

    /**
     * Retrieves a transformation from the resource.
     * 
     * @param transformationDefinition
     * @return the parsed transformation or <code>null</code> if no transformation could be parsed.
     */
    public Transformation parseTransformation( Element transformationDefinition ) {
        throw new UnsupportedOperationException(
                                                 "The parsing of transformations is not supported by this version of the deegree crs parser." );
    }

    /**
     * Parse all polynomial transformations for a given crs.
     * 
     * @param crsElement
     *            to parse the transformations for.
     * @return the list of transformations or the empty list if no transformations were found. Never <code>null</code>.
     */
    protected List<PolynomialTransformation> parseAlternativeTransformations( Element crsElement ) {
        List<Element> usedTransformations = null;
        try {
            usedTransformations = XMLTools.getElements( crsElement, PRE + "polynomialTransformation", nsContext );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
        }
        List<PolynomialTransformation> result = new LinkedList<PolynomialTransformation>();
        if ( usedTransformations != null ) {
            for ( Element transformationElement : usedTransformations ) {
                PolynomialTransformation transform = getTransformation( transformationElement );
                if ( transform != null ) {
                    result.add( transform );
                }
            }
        }
        return result;
    }

    /**
     * Parses the transformation variables from the given crs:coordinatesystem/crs:polynomialTransformation element. If
     * the class attribute is given, this method will try to invoke an instance of the given class, if it fails
     * <code>null</code> will be returned.
     * 
     * @param transformationElement
     *            to parse the values from.
     * @return the instantiated transformation or <code>null</code> if it could not be instantiated.
     */
    protected PolynomialTransformation getTransformation( Element transformationElement ) {
        if ( transformationElement == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_INVALID_NULL_PARAMETER",
                                                                      "transformationElement" ) );
        }

        // order is not evaluated yet, because I do not know if it is required.
        // int order = -1;
        String tCRS = null;
        List<Double> aValues = new LinkedList<Double>();
        List<Double> bValues = new LinkedList<Double>();
        Element usedTransformation = null;
        try {
            usedTransformation = XMLTools.getRequiredElement( transformationElement, "*[1]", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "the transformation to use",
                                                                      transformationElement.getLocalName(),
                                                                      e.getLocalizedMessage() ), e );
        }

        try {
            // order = XMLTools.getNodeAsInt( usedTransformation, PRE + "polynomialOrder", nsContext, -1 );
            tCRS = XMLTools.getRequiredNodeAsString( usedTransformation, PRE + "targetCRS", nsContext );
            Element tmp = XMLTools.getRequiredElement( usedTransformation, PRE + "xParameters", nsContext );
            String tmpValues = XMLTools.getStringValue( tmp );
            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    aValues.add( Double.parseDouble( t ) );
                }
            }
            tmp = XMLTools.getRequiredElement( usedTransformation, PRE + "yParameters", nsContext );
            tmpValues = XMLTools.getStringValue( tmp );
            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    bValues.add( Double.parseDouble( t ) );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
        }

        if ( tCRS == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "targetCRS",
                                                                      ( ( usedTransformation == null ) ? "null"
                                                                                                      : usedTransformation.getLocalName() ),
                                                                      "it is required and must denote a valid crs )" ) );
        }

        if ( aValues.size() == 0 || bValues.size() == 0 ) {
            throw new CRSConfigurationException(
                                                 "The polynomial variables (xParameters and yParameters element) defining the approximation to a given transformation function are required and may not be empty" );
        }

        CoordinateSystem targetCRS = getProvider().getCRSByID( tCRS );

        PolynomialTransformation result = null;
        String name = usedTransformation.getLocalName().trim();
        String className = transformationElement.getAttribute( "class" );
        LOG.logDebug( "Trying to create transformation with name: " + name );
        if ( null != className && !"".equals( className.trim() ) ) {
            LOG.logDebug( "Trying to load user defined transformation class: " + className );
            try {
                Class<?> t = Class.forName( className );
                t.asSubclass( PolynomialTransformation.class );

                List<Element> children = XMLTools.getElements( usedTransformation, "*", nsContext );
                List<Element> otherValues = new LinkedList<Element>();
                for ( Element child : children ) {
                    if ( child != null ) {
                        String localName = child.getLocalName().trim();
                        if ( !( "targetCRS".equals( localName ) || "xParameters".equals( localName )
                                || "yParameters".equals( localName ) || "polynomialOrder".equals( localName ) ) ) {
                            otherValues.add( child );
                        }
                    }
                }
                /**
                 * Load the constructor with the standard projection values and the element list.
                 */
                /**
                 * For now, just load the constructor with the two lists and the crs class.
                 */
                Constructor<?> constructor = t.getConstructor( aValues.getClass(), bValues.getClass(),
                                                               targetCRS.getClass() );
                result = (PolynomialTransformation) constructor.newInstance( aValues, bValues, targetCRS );
            } catch ( ClassNotFoundException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( SecurityException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( NoSuchMethodException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( IllegalArgumentException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( InstantiationException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( IllegalAccessException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( InvocationTargetException e ) {
                LOG.logError( e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                // this will probably never happen.
                LOG.logError( e.getMessage(), e );
            }
            if ( result == null ) {
                LOG.logDebug( "Loading of user defined transformation class: " + className + " was not successful" );
            }
        } else if ( "leastsquare".equalsIgnoreCase( name ) ) {
            float scaleX = 1;
            float scaleY = 1;
            try {
                scaleX = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleX", nsContext, 1 );
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not parse scaleX from crs:leastsquare, because: " + e.getMessage(), e );
            }
            try {
                scaleY = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleY", nsContext, 1 );
            } catch ( XMLParsingException e ) {
                LOG.logError( "Could not parse scaleY from crs:leastsquare, because: " + e.getMessage(), e );
            }
            result = new LeastSquareApproximation( aValues, bValues, null, targetCRS, scaleX, scaleY );
        }
        return result;
    }

    /**
     * Parses a unit from the given xml-parent.
     * 
     * @param parent
     *            xml-node to parse the unit from.
     * @return the unit object.
     * @throws CRSConfigurationException
     *             if the unit object could not be created.
     */
    protected Unit parseUnit( Element parent )
                            throws CRSConfigurationException {
        String unitId = null;
        try {
            unitId = XMLTools.getNodeAsString( parent, PRE + "units", nsContext, null );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "units",
                                                                      ( ( parent == null ) ? "null"
                                                                                          : parent.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        Unit result = getProvider().getCachedIdentifiable( Unit.class, unitId );
        if ( result == null ) {
            result = Unit.createUnitFromString( unitId );
            if ( result == null ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage(
                                                                          "CRS_CONFIG_PARSE_ERROR",
                                                                          "units",
                                                                          ( ( parent == null ) ? "null"
                                                                                              : parent.getLocalName() ),
                                                                          "unknown unit: " + unitId ) );
            }
        }
        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using chached datums, conversioninfos and projections).
     * @return a projected coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    protected CoordinateSystem parseProjectedCRS( Element crsElement )
                            throws CRSConfigurationException {
        if ( crsElement == null ) {
            return null;
        }
        // no need to get it from the cache, because the abstract provider checked it already.
        Identifiable id = parseIdentifiable( crsElement );

        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        // Unit units = parseUnit( crsElement );

        Element usedProjection = null;
        String usedGeographicCRS = null;
        try {
            usedProjection = XMLTools.getRequiredElement( crsElement, PRE + "projection", nsContext );
            usedGeographicCRS = XMLTools.getRequiredNodeAsString( crsElement, PRE + "usedGeographicCRS", nsContext );

        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projectiontType or usedGeographicCRS",
                                                                      crsElement.getLocalName(), e.getMessage() ), e );

        }
        // first create the datum.
        if ( usedGeographicCRS == null || "".equals( usedGeographicCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
                                                                      "usedGeographicCRS", id.getIdentifier() ) );
        }
        GeographicCRS geoCRS = (GeographicCRS) getProvider().getCRSByID( usedGeographicCRS );
        if ( geoCRS == null || geoCRS.getType() != CoordinateSystem.GEOGRAPHIC_CRS ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_CRSREF",
                                                                      id.getIdentifier(), usedGeographicCRS ) );
        }

        // // then the projection.
        // if ( usedProjection == null || "".equals( usedProjection.trim() ) ) {
        // throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
        // "projectionType", id.getIdentifier() ) );
        // }
        // Projection projection = getProjectionByID( usedProjection, (GeographicCRS) geoCRS, axis[0].getUnits() );
        Projection projection = parseProjection( usedProjection, geoCRS, axis[0].getUnits() );
        if ( projection == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_PROJREF",
                                                                      id.getIdentifier(), usedProjection ) );
        }
        // adding to cache will be done in AbstractCRSProvider.
        return new ProjectedCRS( transformations, projection, axis, id );
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using cached datums, conversioninfos and projections).
     * 
     * @return a geographic coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    protected CoordinateSystem parseGeographicCRS( Element crsElement )
                            throws CRSConfigurationException {
        if ( crsElement == null ) {
            return null;
        }
        Identifiable id = parseIdentifiable( crsElement );
        // no need to get it from the cache, because the abstract provider checked it already.
        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        // get the datum
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getIdentifier() );

        GeographicCRS result = new GeographicCRS( transformations, usedDatum, axis, id );
        // adding to cache will be done in AbstractCRSProvider.
        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using cached datums, conversioninfos and projections).
     * @return a geocentric coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    protected CoordinateSystem parseGeocentricCRS( Element crsElement )
                            throws CRSConfigurationException {
        // no need to get it from the cache, because the abstract provider checked it already.
        Identifiable id = parseIdentifiable( crsElement );
        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getIdentifier() );
        GeocentricCRS result = new GeocentricCRS( transformations, usedDatum, axis, id );
        // adding to cache will be done in AbstractCRSProvider.
        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created.
     * 
     * @return a compound coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    protected CoordinateSystem parseCompoundCRS( Element crsElement ) {
        // no need to get it from the cache, because the abstract provider checked it already.
        Identifiable id = parseIdentifiable( crsElement );
        String usedCRS = null;
        try {
            usedCRS = XMLTools.getRequiredNodeAsString( crsElement, PRE + "usedCRS", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "usedCRS",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      e.getMessage() ), e );

        }
        if ( usedCRS == null || "".equals( usedCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedCRS",
                                                                      id.getIdentifier() ) );
        }
        CoordinateSystem usedCoordinateSystem = getProvider().getCRSByID( usedCRS );
        if ( usedCoordinateSystem == null
             || ( usedCoordinateSystem.getType() != CoordinateSystem.GEOGRAPHIC_CRS && usedCoordinateSystem.getType() != CoordinateSystem.PROJECTED_CRS ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_COMPOUND_FALSE_CRSREF",
                                                                      id.getIdentifier(), usedCRS ) );
        }

        // get the datum
        Axis heigtAxis = null;
        double defaultHeight = 0;
        try {
            Element axisElement = XMLTools.getRequiredElement( crsElement, PRE + "heightAxis", nsContext );
            String axisName = XMLTools.getRequiredNodeAsString( axisElement, PRE + "name", nsContext );
            String axisOrientation = XMLTools.getRequiredNodeAsString( axisElement, PRE + "axisOrientation", nsContext );
            Unit unit = parseUnit( axisElement );
            heigtAxis = new Axis( unit, axisName, axisOrientation );
            defaultHeight = XMLTools.getNodeAsDouble( crsElement, PRE + "defaultHeight", nsContext, 0 );
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            throw new CRSConfigurationException( e.getMessage() );
        }
        // adding to cache will be done in AbstractCRSProvider.
        return new CompoundCRS( heigtAxis, usedCoordinateSystem, defaultHeight, id );
    }

    /**
     * Parses the required usedDatum element from the given parentElement (probably a crs element).
     * 
     * @param parentElement
     *            to parse the required usedDatum element from.
     * @param parentID
     *            optional for an appropriate error message.
     * @return the Datum.
     * @throws CRSConfigurationException
     *             if a parsing error occurred, the node was not defined or an illegal id reference (not found) was
     *             given.
     */
    protected GeodeticDatum parseReferencedGeodeticDatum( Element parentElement, String parentID )
                            throws CRSConfigurationException {
        String datumID = null;
        try {
            datumID = XMLTools.getRequiredNodeAsString( parentElement, PRE + "usedDatum", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "datumID",
                                                                      ( ( parentElement == null ) ? "null"
                                                                                                 : parentElement.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        if ( datumID == null || "".equals( datumID.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedDatum",
                                                                      parentID ) );
        }
        GeodeticDatum usedDatum = getGeodeticDatumFromID( datumID );
        if ( usedDatum == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_USEDDATUM_IS_NULL", datumID, parentID ) );
        }
        return usedDatum;
    }

    /**
     * @param datumID
     * @return the
     * @throws CRSConfigurationException
     */
    protected GeodeticDatum getGeodeticDatumFromID( String datumID )
                            throws CRSConfigurationException {
        if ( datumID == null || "".equals( datumID.trim() ) ) {
            return null;
        }
        String tmpDatumID = datumID.trim();
        GeodeticDatum result = getProvider().getCachedIdentifiable( GeodeticDatum.class, tmpDatumID );
        if ( result == null ) {
            Element datumElement = null;
            try {
                datumElement = getURIAsType( tmpDatumID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( datumElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "datum", tmpDatumID ) );
            }
            // get the identifiable.
            Identifiable id = parseIdentifiable( datumElement );

            // get the ellipsoid.
            Ellipsoid ellipsoid = null;
            try {
                String ellipsID = XMLTools.getRequiredNodeAsString( datumElement, PRE + "usedEllipsoid", nsContext );
                if ( ellipsID != null && !"".equals( ellipsID.trim() ) ) {
                    ellipsoid = getEllipsoidFromID( ellipsID );
                }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "usedEllipsoid",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }
            if ( ellipsoid == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_DATUM_HAS_NO_ELLIPSOID",
                                                                          tmpDatumID ) );
            }

            // get the primemeridian if any.
            PrimeMeridian pMeridian = null;
            try {
                String pMeridianID = XMLTools.getNodeAsString( datumElement, PRE + "usedPrimeMeridian", nsContext, null );
                if ( pMeridianID != null && !"".equals( pMeridianID.trim() ) ) {
                    pMeridian = getPrimeMeridianFromID( pMeridianID );
                }
                if ( pMeridian == null ) {
                    pMeridian = PrimeMeridian.GREENWICH;
                }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                          "usedPrimeMeridian",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }

            // get the WGS84 if any.
            Helmert cInfo = null;
            try {
                String infoID = XMLTools.getNodeAsString( datumElement, PRE + "usedWGS84ConversionInfo", nsContext,
                                                          null );
                if ( infoID != null && !"".equals( infoID.trim() ) ) {
                    cInfo = getConversionInfoFromID( infoID );
                }
                // if ( cInfo == null ) {
                // cInfo = new Helmert( "Created by DeegreeCRSProvider" );
                // }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                          "wgs84ConversionInfo",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }

            result = new GeodeticDatum( ellipsoid, pMeridian, cInfo, id.getIdentifiers(), id.getNames(),
                                        id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
        }
        return getProvider().addIdToCache( result, false );

    }

    /**
     * @param meridianID
     *            the id to search for.
     * @return the primeMeridian with given id or <code>null</code>
     * @throws CRSConfigurationException
     *             if the longitude was not set or the units could not be parsed.
     */
    protected PrimeMeridian getPrimeMeridianFromID( String meridianID )
                            throws CRSConfigurationException {
        if ( meridianID == null || "".equals( meridianID.trim() ) ) {
            return null;
        }
        PrimeMeridian result = getProvider().getCachedIdentifiable( PrimeMeridian.class, meridianID );
        if ( result == null ) {
            Element meridianElement = null;
            try {
                meridianElement = getURIAsType( meridianID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( meridianElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "primeMeridian",
                                                                          meridianID ) );
            }
            Identifiable id = parseIdentifiable( meridianElement );
            Unit units = parseUnit( meridianElement );
            double longitude = 0;
            try {
                longitude = XMLTools.getRequiredNodeAsDouble( meridianElement, PRE + "longitude", nsContext );
                boolean inDegrees = XMLTools.getNodeAsBoolean( meridianElement, PRE + "longitude/@inDegrees",
                                                               nsContext, true );
                longitude = ( longitude != 0 && inDegrees ) ? Math.toRadians( longitude ) : longitude;
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "longitude",
                                                                          meridianElement.getLocalName(),
                                                                          e.getMessage() ), e );
            }
            result = new PrimeMeridian( units, longitude, id );
        }
        return getProvider().addIdToCache( result, false );
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
    protected Ellipsoid getEllipsoidFromID( String ellipsoidID )
                            throws CRSConfigurationException {
        if ( ellipsoidID == null || "".equals( ellipsoidID.trim() ) ) {
            return null;
        }
        Ellipsoid result = getProvider().getCachedIdentifiable( Ellipsoid.class, ellipsoidID );
        if ( result == null ) {
            Element ellipsoidElement = null;
            try {
                ellipsoidElement = getURIAsType( ellipsoidID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( ellipsoidElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "ellipsoid",
                                                                          ellipsoidID ) );
            }
            Identifiable id = parseIdentifiable( ellipsoidElement );
            Unit units = parseUnit( ellipsoidElement );

            double semiMajor = Double.NaN;
            double inverseFlattening = Double.NaN;
            double eccentricity = Double.NaN;
            double semiMinorAxis = Double.NaN;

            try {
                semiMajor = XMLTools.getRequiredNodeAsDouble( ellipsoidElement, PRE + "semiMajorAxis", nsContext );
                inverseFlattening = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "inverseFlattening", nsContext,
                                                              Double.NaN );
                eccentricity = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "eccentricity", nsContext, Double.NaN );
                semiMinorAxis = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "semiMinorAxis", nsContext,
                                                          Double.NaN );
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "ellipsoid",
                                                                          ellipsoidElement.getLocalName(),
                                                                          e.getMessage() ), e );
            }
            if ( Double.isNaN( inverseFlattening ) && Double.isNaN( eccentricity ) && Double.isNaN( semiMinorAxis ) ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_ELLIPSOID_MISSES_PARAM",
                                                                          ellipsoidID ) );
            }
            if ( !Double.isNaN( inverseFlattening ) ) {
                result = new Ellipsoid( semiMajor, units, inverseFlattening, id.getIdentifiers(), id.getNames(),
                                        id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            } else if ( !Double.isNaN( eccentricity ) ) {
                result = new Ellipsoid( semiMajor, eccentricity, units, id.getIdentifiers(), id.getNames(),
                                        id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            } else {
                result = new Ellipsoid( units, semiMajor, semiMinorAxis, id.getIdentifiers(), id.getNames(),
                                        id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            }
        }

        return getProvider().addIdToCache( result, false );
    }

    /**
     * @param infoID
     *            to get the conversioninfo from.
     * @return the configured wgs84 conversion info parameters.
     * @throws CRSConfigurationException
     */
    protected Helmert getConversionInfoFromID( String infoID )
                            throws CRSConfigurationException {
        if ( infoID == null || "".equals( infoID.trim() ) ) {
            return null;
        }
        LOG.logDebug( "Searching for the wgs84 with id: " + infoID );
        Helmert result = getProvider().getCachedIdentifiable( Helmert.class, infoID );
        if ( result == null ) {

            Element cInfoElement = null;
            try {
                cInfoElement = getURIAsType( infoID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( cInfoElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT",
                                                                          "wgs84ConversionInfo", infoID ) );
            }
            Identifiable identifiable = parseIdentifiable( cInfoElement );
            double xT = 0, yT = 0, zT = 0, xR = 0, yR = 0, zR = 0, scale = 0;
            try {
                xT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "xAxisTranslation", nsContext, 0 );
                yT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "yAxisTranslation", nsContext, 0 );
                zT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "zAxisTranslation", nsContext, 0 );
                xR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "xAxisRotation", nsContext, 0 );
                yR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "yAxisRotation", nsContext, 0 );
                zR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "zAxisRotation", nsContext, 0 );
                scale = XMLTools.getNodeAsDouble( cInfoElement, PRE + "scaleDifference", nsContext, 0 );
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "conversionInfo",
                                                                          "definitions", e.getMessage() ), e );
            }
            result = new Helmert( xT, yT, zT, xR, yR, zR, scale, null, GeographicCRS.WGS84, identifiable );
        }
        return getProvider().addIdToCache( result, false );
    }

    /**
     * Parses and instantiates the projection from the given element.
     * 
     * @param projectionElement
     *            to create the projection from.
     * @param underlyingCRS
     *            of the projected crs
     * @param units
     *            of the projected crs
     * @return the configured projection or <code>null</code> if not defined or found.
     * @throws CRSConfigurationException
     */
    protected Projection parseProjection( Element projectionElement, GeographicCRS underlyingCRS, Unit units )
                            throws CRSConfigurationException {
        if ( projectionElement == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_INVALID_NULL_PARAMETER", "projectionElement" ) );
        }
        try {
            Element usedProjection = XMLTools.getRequiredElement( projectionElement, "*[1]", nsContext );
            // All projections will have following parameters
            double latitudeOfNaturalOrigin = XMLTools.getNodeAsDouble( usedProjection, PRE + "latitudeOfNaturalOrigin",
                                                                       nsContext, 0 );
            boolean inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "latitudeOfNaturalOrigin/@inDegrees",
                                                           nsContext, true );
            latitudeOfNaturalOrigin = ( latitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( latitudeOfNaturalOrigin )
                                                                                   : latitudeOfNaturalOrigin;

            double longitudeOfNaturalOrigin = XMLTools.getNodeAsDouble( usedProjection, PRE
                                                                                        + "longitudeOfNaturalOrigin",
                                                                        nsContext, 0 );
            inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "longitudeOfNaturalOrigin/@inDegrees",
                                                   nsContext, true );
            longitudeOfNaturalOrigin = ( longitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( longitudeOfNaturalOrigin )
                                                                                     : longitudeOfNaturalOrigin;

            double scaleFactor = XMLTools.getNodeAsDouble( usedProjection, PRE + "scaleFactor", nsContext, 0 );
            double falseEasting = XMLTools.getNodeAsDouble( usedProjection, PRE + "falseEasting", nsContext, 0 );
            double falseNorthing = XMLTools.getNodeAsDouble( usedProjection, PRE + "falseNorthing", nsContext, 0 );

            String projectionName = usedProjection.getLocalName().trim();
            String className = projectionElement.getAttribute( "class" );
            Point2d naturalOrigin = new Point2d( longitudeOfNaturalOrigin, latitudeOfNaturalOrigin );
            Projection result = null;
            if ( className != null && !"".equals( className.trim() ) ) {
                LOG.logDebug( "Trying to load user defined projection class: " + className );
                try {
                    Class<?> t = Class.forName( className );
                    t.asSubclass( Projection.class );
                    /**
                     * try to get a constructor with a native type as a parameter, by going over the 'names' of the
                     * classes of the parameters, the native type will show up as the typename e.g. int or long.....
                     * <code>
                     * public Projection( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                     * Point2d naturalOrigin, Unit units, double scale, boolean conformal, boolean equalArea )
                     * </code>
                     */
                    List<Element> children = XMLTools.getElements( usedProjection, "*", nsContext );
                    List<Element> otherValues = new LinkedList<Element>();
                    for ( Element child : children ) {
                        if ( child != null ) {
                            String localName = child.getLocalName().trim();
                            if ( !( "latitudeOfNaturalOrigin".equals( localName )
                                    || "longitudeOfNaturalOrigin".equals( localName )
                                    || "scaleFactor".equals( localName ) || "falseEasting".equals( localName ) || "falseNorthing".equals( localName ) ) ) {
                                otherValues.add( child );
                            }
                        }
                    }
                    /**
                     * Load the constructor with the standard projection values and the element list.
                     */
                    Constructor<?> constructor = t.getConstructor( GeographicCRS.class, double.class, double.class,
                                                                   Point2d.class, Unit.class, double.class, List.class );
                    result = (Projection) constructor.newInstance( underlyingCRS, falseNorthing, falseEasting,
                                                                   naturalOrigin, units, scaleFactor, otherValues );
                } catch ( ClassNotFoundException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( SecurityException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( NoSuchMethodException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( IllegalArgumentException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( InstantiationException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( IllegalAccessException e ) {
                    LOG.logError( e.getMessage(), e );
                } catch ( InvocationTargetException e ) {
                    LOG.logError( e.getMessage(), e );
                }
                if ( result == null ) {
                    LOG.logDebug( "Loading of user defined transformation class: " + className + " was not successful" );
                }

            } else {
                // no selfdefined projection, try one of the following, for the projection specific parameters, if any.
                if ( "transverseMercator".equalsIgnoreCase( projectionName ) ) {
                    // change schema to let projection be identifiable. fix method geodetic
                    boolean northernHemi = XMLTools.getNodeAsBoolean( usedProjection, PRE + "northernHemisphere",
                                                                      nsContext, true );
                    result = new TransverseMercator( northernHemi, underlyingCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scaleFactor );
                } else if ( "lambertAzimuthalEqualArea".equalsIgnoreCase( projectionName ) ) {
                    result = new LambertAzimuthalEqualArea( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                            units, scaleFactor );
                } else if ( "lambertConformalConic".equalsIgnoreCase( projectionName ) ) {
                    double firstP = XMLTools.getNodeAsDouble( usedProjection, PRE + "firstParallelLatitude", nsContext,
                                                              Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "firstParallelLatitude/@inDegrees",
                                                           nsContext, true );
                    firstP = ( !Double.isNaN( firstP ) && inDegrees ) ? Math.toRadians( firstP ) : firstP;

                    double secondP = XMLTools.getNodeAsDouble( usedProjection, PRE + "secondParallelLatitude",
                                                               nsContext, Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "secondParallelLatitude/@inDegrees",
                                                           nsContext, true );
                    secondP = ( !Double.isNaN( secondP ) && inDegrees ) ? Math.toRadians( secondP ) : secondP;
                    result = new LambertConformalConic( firstP, secondP, underlyingCRS, falseNorthing, falseEasting,
                                                        naturalOrigin, units, scaleFactor );
                } else if ( "stereographicAzimuthal".equalsIgnoreCase( projectionName ) ) {
                    double trueScaleL = XMLTools.getNodeAsDouble( usedProjection, PRE + "trueScaleLatitude", nsContext,
                                                                  Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "trueScaleLatitude/@inDegrees",
                                                           nsContext, true );
                    trueScaleL = ( !Double.isNaN( trueScaleL ) && inDegrees ) ? Math.toRadians( trueScaleL )
                                                                             : trueScaleL;
                    result = new StereographicAzimuthal( trueScaleL, underlyingCRS, falseNorthing, falseEasting,
                                                         naturalOrigin, units, scaleFactor );
                } else if ( "StereographicAlternative".equalsIgnoreCase( projectionName ) ) {
                    result = new StereographicAlternative( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                           units, scaleFactor );
                } else if ( "mercator".equalsIgnoreCase( projectionName ) ) {
                    result = new Mercator( underlyingCRS, falseNorthing, falseEasting, naturalOrigin, units,
                                           scaleFactor );
                } else {
                    throw new CRSConfigurationException(
                                                         Messages.getMessage(
                                                                              "CRS_CONFIG_PROJECTEDCRS_INVALID_PROJECTION",
                                                                              projectionName,
                                                                              "StereographicAlternative, stereographicAzimuthal, lambertConformalConic, lambertAzimuthalEqualArea, transverseMercator" ) );

                }
            }
            return result;
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projection parameters",
                                                                      projectionElement.getLocalName(), e.getMessage() ),
                                                 e );

        }
    }

    /**
     * @param <T>
     *            should be at least of Type Identifiable.
     * @param uniqueList
     *            to check against
     * @param mapping
     *            to added the id of T to if it is found duplicate.
     * @param toBeChecked
     *            to check.
     * @return the cached T if found or the given identifiable.
     */
    protected <T extends Identifiable> T checkForUniqueness( List<T> uniqueList, Map<String, String> mapping,
                                                             T toBeChecked ) {
        T result = toBeChecked;
        if ( uniqueList.contains( toBeChecked ) ) {
            int index = uniqueList.indexOf( toBeChecked );
            LOG.logInfo( "The Identifiable with id: " + toBeChecked.getIdentifier() + " was found to be equal with: "
                         + uniqueList.get( index ).getIdentifier() );
            String key = uniqueList.get( index ).getIdentifier();
            boolean updatedEPSG = false;
            if ( key != null && !"".equals( key.trim() ) ) {
                String value = mapping.get( key );
                String tbcID = toBeChecked.getIdentifier().toUpperCase();
                // it would be nicest to get the epsg code if any.
                if ( !key.toUpperCase().startsWith( "EPSG:" ) && tbcID.startsWith( "EPSG:" ) ) {
                    if ( value == null || "".equals( value ) ) {
                        value = key;
                    } else {
                        value += ", " + key;
                    }
                    updatedEPSG = true;
                    mapping.remove( key );
                    key = toBeChecked.getIdentifier();
                } else {
                    if ( value == null || "".equals( value ) ) {
                        value = toBeChecked.getIdentifier();
                    } else {
                        value += ", " + toBeChecked.getIdentifier();
                    }
                }
                mapping.put( key, value );
            }
            // if updated to epsg, cache the epsg instead and remove the old identifiable.
            if ( updatedEPSG ) {
                uniqueList.remove( index );
                uniqueList.add( toBeChecked );
            } else {
                result = uniqueList.get( index );
            }
        } else {
            LOG.logDebug( "Adding: " + toBeChecked.getIdentifier() + " to cache." );
            uniqueList.add( toBeChecked );
        }
        return result;
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        LOG.logError( "The retrieval of transformations is not supported for this deegree crs configuration format." );
        return null;
    }

    /**
     * Gets the Element for the given id and heuristically check the localname of the resulting root Element. This
     * version supports following local names (see schema): <code>
     * <ul>
     * <li>ellipsoid</li>
     * <li>geodeticDatum</li>
     * <li>projectedCRS</li>
     * <li>geographicCRS</li>
     * <li>compoundCRS</li>
     * <li>geocentricCRS</li>
     * <li>primeMeridian</li>
     * <li>wgs84Transformation</li>
     * </ul>
     * </code>
     * 
     * @param id
     *            to look for.
     * @return the instantiated {@link Identifiable} or <code>null</code> if it could not be parsed.
     */
    public Identifiable parseIdentifiableObject( String id ) {
        if ( id == null || "".equals( id ) ) {
            return null;
        }
        Element resolvedID = null;
        try {
            resolvedID = getURIAsType( id );
        } catch ( IOException e ) {
            throw new CRSConfigurationException( e );
        }
        Identifiable result = null;
        if ( resolvedID != null ) {
            String localName = resolvedID.getLocalName();
            if ( localName != null && !"".equals( localName.trim() ) ) {
                if ( localName.equals( "ellipsoid" ) ) {
                    result = getEllipsoidFromID( id );
                } else if ( localName.equals( "geodeticDatum" ) ) {
                    result = getGeodeticDatumFromID( id );
                } else if ( localName.equals( "projectedCRS" ) || localName.equals( "geographicCRS" )
                            || localName.equals( "compoundCRS" ) || localName.equals( "geocentricCRS" ) ) {
                    result = getProvider().getCRSByID( id );
                } else if ( localName.equals( "primeMeridian" ) ) {
                    result = getPrimeMeridianFromID( id );
                } else if ( localName.equals( "wgs84Transformation" ) ) {
                    result = getConversionInfoFromID( id );
                }
            }
        }
        return result;
    }

    public List<Transformation> getTransformations() {
        return null;
    }
}
