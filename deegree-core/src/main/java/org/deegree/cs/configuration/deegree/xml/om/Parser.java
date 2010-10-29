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

package org.deegree.cs.configuration.deegree.xml.om;

import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.GEOGRAPHIC;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.PROJECTED;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.configuration.deegree.xml.CRSParser;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.resources.XMLFileResource;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.Mercator;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.cs.transformations.polynomial.PolynomialTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>Parser</code> containing the parsing code for configuration up to version 0.4 of the crs (include deegree2
 * crs-configuration files).
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about the currently parsed coordinate system components.")
public class Parser extends XMLFileResource implements CRSParser<OMElement> {

    /**
     * 
     */
    private static final long serialVersionUID = -5621078575657838568L;

    private static Logger LOG = LoggerFactory.getLogger( Parser.class );

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
    public Parser( DeegreeCRSProvider<OMElement> provider, OMElement rootElement ) {
        super( provider, rootElement );
    }

    /**
     * @param provider
     * @param properties
     */
    public Parser( DeegreeCRSProvider<OMElement> provider, Properties properties ) {
        this( provider, properties, "definitions", CommonNamespaces.CRSNS );
    }

    /**
     * @param provider
     * @param properties
     * @param defaultRootElement
     * @param namespace
     */
    public Parser( DeegreeCRSProvider<OMElement> provider, Properties properties, String defaultRootElement,
                   String namespace ) {
        super( provider, properties, defaultRootElement, namespace );
    }

    /**
     * @param crsDefintion
     *            to be parsed
     * @return an instance of the given crs or <code>null</code> if the crsDefinition is <code>null</code> or could not
     *         be mapped to a valid type.
     * @throws CRSConfigurationException
     *             if something went wrong while constructing the crs.
     */
    public CoordinateSystem parseCoordinateSystem( OMElement crsDefintion )
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

        if ( result == null && LOG.isDebugEnabled() ) {
            LOG.debug( "The element with localname "
                       + crsDefintion.getLocalName()
                       + " could not be mapped to a valid deegreec-crs, currently projectedCRS, geographicCRS, geocentricCRS and compoundCRS are supported." );
        }
        return result;
    }

    // TODO find a better name
    public OMElement getURIAsType( String uri )
                            throws IOException {
        if ( uri == null || "".equals( uri ) ) {
            return null;
        }
        String id = uri.trim().toUpperCase();
        OMElement crsElement = null;
        // String xPath ="//*[crs:id='EPSG:31466']";
        String xPath = XPATH_PRE + id + "']";
        try {
            // crsElement = XMLTools.getElement( getRootElement(), xPath, nsContext );
            crsElement = getElement( getRootElement(), new XPath( xPath, nsContext ) );

        } catch ( XMLParsingException e ) {
            LOG.error( Messages.getMessage( "CRS_CONFIG_NO_RESULT_FOR_ID", id, e.getMessage() ), e );
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Trying to find elements with xpath: " + xPath
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
        return getRootElement().getAttributeValue( new QName( "version" ) );
    }

    /**
     * Parses all elements of the identifiable object.
     * 
     * @param element
     *            the xml-representation of the id-object
     * @return the identifiable object or <code>null</code> if no id was given.
     * @throws CRSConfigurationException
     */
    protected CRSIdentifiable parseIdentifiable( OMElement element )
                            throws CRSConfigurationException {
        try {
            String[] identifiers = getNodesAsStrings( element, new XPath( PRE + "id", nsContext ) );
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
            String[] names = getNodesAsStrings( element, new XPath( PRE + "name", nsContext ) );
            String[] versions = getNodesAsStrings( element, new XPath( PRE + "version", nsContext ) );
            String[] descriptions = getNodesAsStrings( element, new XPath( PRE + "description", nsContext ) );
            String[] areasOfUse = getNodesAsStrings( element, new XPath( PRE + "areaOfUse", nsContext ) );

            // convert the string IDs to CRSCodeTypes
            Set<CRSCodeType> codeSet = new HashSet<CRSCodeType>();
            int n = identifiers.length;
            for ( int i = 0; i < n; i++ )
                codeSet.add( CRSCodeType.valueOf( identifiers[i] ) );
            return new CRSIdentifiable( codeSet.toArray( new CRSCodeType[codeSet.size()] ), names, versions,
                                        descriptions, areasOfUse );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "CRSIdentifiable",
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
    protected Axis[] parseAxisOrder( OMElement crsElement )
                            throws CRSConfigurationException {
        String axisOrder = null;
        try {
            axisOrder = getRequiredNodeAsString( crsElement, new XPath( PRE + "axisOrder", nsContext ) );
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
                    // OMElement axisElement = XMLTools.getRequiredElement( crsElement, XPATH + t + "']", nsContext );
                    OMElement axisElement = getRequiredElement( crsElement, new XPath( XPATH + t + "']", nsContext ) );
                    // String axisOrientation = XMLTools.getRequiredNodeAsString( axisElement, PRE + "axisOrientation",
                    // nsContext );
                    String axisOrientation = getRequiredNodeAsString( axisElement, new XPath( PRE + "axisOrientation",
                                                                                              nsContext ) );
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
    public Transformation parseTransformation( OMElement transformationDefinition ) {
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
    protected List<Transformation> parseAlternativeTransformations( OMElement crsElement ) {
        List<OMElement> usedTransformations = null;
        try {
            usedTransformations = getElements( crsElement, new XPath( PRE + "polynomialTransformation", nsContext ) );
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
        }
        List<Transformation> result = new LinkedList<Transformation>();
        if ( usedTransformations != null ) {
            for ( OMElement transformationElement : usedTransformations ) {
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
    protected PolynomialTransformation getTransformation( OMElement transformationElement ) {
        if ( transformationElement == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_INVALID_NULL_PARAMETER",
                                                                      "transformationElement" ) );
        }

        // order is not evaluated yet, because I do not know if it is required.
        // int order = -1;
        String tCRS = null;
        List<Double> aValues = new LinkedList<Double>();
        List<Double> bValues = new LinkedList<Double>();
        OMElement usedTransformation = null;
        try {
            usedTransformation = getRequiredElement( transformationElement, new XPath( "*[1]", nsContext ) );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "the transformation to use",
                                                                      transformationElement.getLocalName(),
                                                                      e.getLocalizedMessage() ), e );
        }

        try {
            // order = XMLTools.getNodeAsInt( usedTransformation, PRE + "polynomialOrder", nsContext, -1 );
            tCRS = getRequiredNodeAsString( usedTransformation, new XPath( PRE + " targetCRS", nsContext ) );
            OMElement tmp = getRequiredElement( usedTransformation, new XPath( PRE + "xParameters", nsContext ) );
            String tmpValues = getNodeAsString( tmp, new XPath( ".", nsContext ), null );

            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    aValues.add( Double.parseDouble( t ) );
                }
            }
            tmp = getRequiredElement( usedTransformation, new XPath( PRE + "yParameters", nsContext ) );
            tmpValues = getNodeAsString( tmp, new XPath( ".", nsContext ), null );
            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    bValues.add( Double.parseDouble( t ) );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
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

        CoordinateSystem targetCRS = getProvider().getCRSByCode( CRSCodeType.valueOf( tCRS ) );

        PolynomialTransformation result = null;
        String name = usedTransformation.getLocalName().trim();
        String className = transformationElement.getAttributeValue( new QName( "class" ) );
        LOG.debug( "Trying to create transformation with name: " + name );
        if ( null != className && !"".equals( className.trim() ) ) {
            LOG.debug( "Trying to load user defined transformation class: " + className );
            try {
                Class<?> t = Class.forName( className );
                t.asSubclass( PolynomialTransformation.class );

                // List<Element> children = XMLTools.getElements( usedTransformation, "*", nsContext );
                List<OMElement> children = getElements( usedTransformation, new XPath( "*", nsContext ) );
                List<OMElement> otherValues = new LinkedList<OMElement>();
                for ( OMElement child : children ) {
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
            } catch ( XMLParsingException e ) {
                // this will probably never happen.
                LOG.error( e.getMessage(), e );
            }
            if ( result == null ) {
                LOG.debug( "Loading of user defined transformation class: " + className + " was not successful" );
            }
        } else if ( "leastsquare".equalsIgnoreCase( name ) ) {
            float scaleX = 1;
            float scaleY = 1;
            try {
                // scaleX = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleX", nsContext, 1 );
                scaleX = (float) getNodeAsDouble( usedTransformation, new XPath( PRE + "scaleX", nsContext ), 1 );
            } catch ( XMLParsingException e ) {
                LOG.error( "Could not parse scaleX from crs:leastsquare, because: " + e.getMessage(), e );
            }
            try {
                // scaleY = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleY", nsContext, 1 );
                scaleY = (float) getNodeAsDouble( usedTransformation, new XPath( PRE + "scaleY", nsContext ), 1 );
            } catch ( XMLParsingException e ) {
                LOG.error( "Could not parse scaleY from crs:leastsquare, because: " + e.getMessage(), e );
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
    protected Unit parseUnit( OMElement parent )
                            throws CRSConfigurationException {
        String unitId = null;
        try {
            unitId = getNodeAsString( parent, new XPath( PRE + "units", nsContext ), null );
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
    protected CoordinateSystem parseProjectedCRS( OMElement crsElement )
                            throws CRSConfigurationException {
        if ( crsElement == null ) {
            return null;
        }
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( crsElement );

        Axis[] axis = parseAxisOrder( crsElement );
        List<Transformation> transformations = parseAlternativeTransformations( crsElement );
        // Unit units = parseUnit( crsElement );

        OMElement usedProjection = null;
        String usedGeographicCRS = null;
        try {
            usedProjection = getRequiredElement( crsElement, new XPath( PRE + "projection", nsContext ) );
            usedGeographicCRS = getRequiredNodeAsString( crsElement, new XPath( PRE + "usedGeographicCRS", nsContext ) );

        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projectiontType or usedGeographicCRS",
                                                                      crsElement.getLocalName(), e.getMessage() ), e );

        }
        // first create the datum.
        if ( usedGeographicCRS == null || "".equals( usedGeographicCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
                                                                      "usedGeographicCRS", id.getCode() ) );
        }
        GeographicCRS geoCRS = (GeographicCRS) getProvider().getCRSByCode( CRSCodeType.valueOf( usedGeographicCRS ) );
        if ( geoCRS == null || geoCRS.getType() != GEOGRAPHIC ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_CRSREF",
                                                                      id.getCode(), usedGeographicCRS ) );
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
                                                                      id.getCode(), usedProjection ) );
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
    protected CoordinateSystem parseGeographicCRS( OMElement crsElement )
                            throws CRSConfigurationException {
        if ( crsElement == null ) {
            return null;
        }
        CRSIdentifiable id = parseIdentifiable( crsElement );
        // no need to get it from the cache, because the abstract provider checked it already.
        Axis[] axis = parseAxisOrder( crsElement );
        List<Transformation> transformations = parseAlternativeTransformations( crsElement );
        // get the datum
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getCode().getOriginal() );

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
    protected CoordinateSystem parseGeocentricCRS( OMElement crsElement )
                            throws CRSConfigurationException {
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( crsElement );
        Axis[] axis = parseAxisOrder( crsElement );
        List<Transformation> transformations = parseAlternativeTransformations( crsElement );
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getCode().getOriginal() );
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
    protected CoordinateSystem parseCompoundCRS( OMElement crsElement ) {
        // no need to get it from the cache, because the abstract provider checked it already.
        CRSIdentifiable id = parseIdentifiable( crsElement );
        String usedCRS = null;
        try {
            usedCRS = getRequiredNodeAsString( crsElement, new XPath( PRE + "usedCRS", nsContext ) );
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
                                                                      id.getCode() ) );
        }
        CoordinateSystem usedCoordinateSystem = getProvider().getCRSByCode( CRSCodeType.valueOf( usedCRS ) );
        if ( usedCoordinateSystem == null
             || ( usedCoordinateSystem.getType() != GEOGRAPHIC && usedCoordinateSystem.getType() != PROJECTED ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_COMPOUND_FALSE_CRSREF", id.getCode(),
                                                                      usedCRS ) );
        }

        // get the datum
        Axis heigtAxis = null;
        double defaultHeight = 0;
        try {
            OMElement axisElement = getRequiredElement( crsElement, new XPath( PRE + "heightAxis", nsContext ) );
            String axisName = getRequiredNodeAsString( axisElement, new XPath( PRE + "name", nsContext ) );
            String axisOrientation = getRequiredNodeAsString( axisElement, new XPath( PRE + "axisOrientation",
                                                                                      nsContext ) );
            Unit unit = parseUnit( axisElement );
            heigtAxis = new Axis( unit, axisName, axisOrientation );
            defaultHeight = getNodeAsDouble( crsElement, new XPath( PRE + "defaultHeight", nsContext ), 0 );
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
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
    protected GeodeticDatum parseReferencedGeodeticDatum( OMElement parentElement, String parentID )
                            throws CRSConfigurationException {
        String datumID = null;
        try {
            datumID = getRequiredNodeAsString( parentElement, new XPath( PRE + "usedDatum", nsContext ) );
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
        GeodeticDatum usedDatum = getGeodeticDatumForId( datumID );
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
    public GeodeticDatum getGeodeticDatumForId( String datumID )
                            throws CRSConfigurationException {
        if ( datumID == null || "".equals( datumID.trim() ) ) {
            return null;
        }
        String tmpDatumID = datumID.trim();
        GeodeticDatum result = getProvider().getCachedIdentifiable( GeodeticDatum.class, tmpDatumID );
        if ( result == null ) {
            OMElement datumElement = null;
            try {
                datumElement = getURIAsType( tmpDatumID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( datumElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "datum", tmpDatumID ) );
            }
            // get the identifiable.
            CRSIdentifiable id = parseIdentifiable( datumElement );

            // get the ellipsoid.
            Ellipsoid ellipsoid = null;
            try {
                String ellipsID = getRequiredNodeAsString( datumElement, new XPath( PRE + " usedEllipsoid", nsContext ) );
                if ( ellipsID != null && !"".equals( ellipsID.trim() ) ) {
                    ellipsoid = getEllipsoidForId( ellipsID );
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
                String pMeridianID = getNodeAsString( datumElement, new XPath( PRE + "usedPrimeMeridian", nsContext ),
                                                      null );
                if ( pMeridianID != null && !"".equals( pMeridianID.trim() ) ) {
                    pMeridian = getPrimeMeridianForId( pMeridianID );
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
                String infoID = getNodeAsString( datumElement, new XPath( PRE + "usedWGS84ConversionInfo", nsContext ),
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

            result = new GeodeticDatum( ellipsoid, pMeridian, cInfo, id.getCodes(), id.getNames(), id.getVersions(),
                                        id.getDescriptions(), id.getAreasOfUse() );
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
    public PrimeMeridian getPrimeMeridianForId( String meridianID )
                            throws CRSConfigurationException {
        if ( meridianID == null || "".equals( meridianID.trim() ) ) {
            return null;
        }
        PrimeMeridian result = getProvider().getCachedIdentifiable( PrimeMeridian.class, meridianID );
        if ( result == null ) {
            OMElement meridianElement = null;
            try {
                meridianElement = getURIAsType( meridianID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( meridianElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "primeMeridian",
                                                                          meridianID ) );
            }
            CRSIdentifiable id = parseIdentifiable( meridianElement );
            Unit units = parseUnit( meridianElement );
            double longitude = 0;
            try {
                longitude = getRequiredNodeAsDouble( meridianElement, new XPath( PRE + "longitude", nsContext ) );
                boolean inDegrees = getNodeAsBoolean( meridianElement, new XPath( PRE + "longitude/@inDegrees",
                                                                                  nsContext ), true );
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
    public Ellipsoid getEllipsoidForId( String ellipsoidID )
                            throws CRSConfigurationException {
        if ( ellipsoidID == null || "".equals( ellipsoidID.trim() ) ) {
            return null;
        }
        Ellipsoid result = getProvider().getCachedIdentifiable( Ellipsoid.class, ellipsoidID );
        if ( result == null ) {
            OMElement ellipsoidElement = null;
            try {
                ellipsoidElement = getURIAsType( ellipsoidID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( ellipsoidElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "ellipsoid",
                                                                          ellipsoidID ) );
            }
            CRSIdentifiable id = parseIdentifiable( ellipsoidElement );
            Unit units = parseUnit( ellipsoidElement );

            double semiMajor = Double.NaN;
            double inverseFlattening = Double.NaN;
            double eccentricity = Double.NaN;
            double semiMinorAxis = Double.NaN;

            try {
                semiMajor = getRequiredNodeAsDouble( ellipsoidElement, new XPath( PRE + "semiMajorAxis", nsContext ) );
                inverseFlattening = getNodeAsDouble( ellipsoidElement,
                                                     new XPath( PRE + "inverseFlattening", nsContext ), Double.NaN );
                eccentricity = getNodeAsDouble( ellipsoidElement, new XPath( PRE + "eccentricity", nsContext ),
                                                Double.NaN );
                semiMinorAxis = getNodeAsDouble( ellipsoidElement, new XPath( PRE + "semiMinorAxis", nsContext ),
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
                result = new Ellipsoid( semiMajor, units, inverseFlattening, id.getCodes(), id.getNames(),
                                        id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            } else if ( !Double.isNaN( eccentricity ) ) {
                result = new Ellipsoid( semiMajor, eccentricity, units, id.getCodes(), id.getNames(), id.getVersions(),
                                        id.getDescriptions(), id.getAreasOfUse() );
            } else {
                result = new Ellipsoid( units, semiMajor, semiMinorAxis, id.getCodes(), id.getNames(),
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
        LOG.debug( "Searching for the wgs84 with id: " + infoID );
        Helmert result = getProvider().getCachedIdentifiable( Helmert.class, infoID );
        if ( result == null ) {

            OMElement cInfoElement = null;
            try {
                cInfoElement = getURIAsType( infoID );
            } catch ( IOException e ) {
                throw new CRSConfigurationException( e );
            }
            if ( cInfoElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT",
                                                                          "wgs84ConversionInfo", infoID ) );
            }
            CRSIdentifiable identifiable = parseIdentifiable( cInfoElement );
            double xT = 0, yT = 0, zT = 0, xR = 0, yR = 0, zR = 0, scale = 0;
            try {
                xT = getNodeAsDouble( cInfoElement, new XPath( PRE + "xAxisTranslation", nsContext ), 0 );
                yT = getNodeAsDouble( cInfoElement, new XPath( PRE + "yAxisTranslation", nsContext ), 0 );
                zT = getNodeAsDouble( cInfoElement, new XPath( PRE + "zAxisTranslation", nsContext ), 0 );
                xR = getNodeAsDouble( cInfoElement, new XPath( PRE + "xAxisRotation", nsContext ), 0 );
                yR = getNodeAsDouble( cInfoElement, new XPath( PRE + "yAxisRotation", nsContext ), 0 );
                zR = getNodeAsDouble( cInfoElement, new XPath( PRE + "zAxisRotation", nsContext ), 0 );
                scale = getNodeAsDouble( cInfoElement, new XPath( PRE + "scaleDifference", nsContext ), 0 );
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
    protected Projection parseProjection( OMElement projectionElement, GeographicCRS underlyingCRS, Unit units )
                            throws CRSConfigurationException {
        if ( projectionElement == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_INVALID_NULL_PARAMETER", "projectionElement" ) );
        }
        try {
            OMElement usedProjection = getRequiredElement( projectionElement, new XPath( "*[1]", nsContext ) );
            // All projections will have following parameters
            double latitudeOfNaturalOrigin = getNodeAsDouble( usedProjection, new XPath( PRE
                                                                                         + "latitudeOfNaturalOrigin",
                                                                                         nsContext ), 0 );
            boolean inDegrees = getNodeAsBoolean( usedProjection,
                                                  new XPath( PRE + "latitudeOfNaturalOrigin/@inDegrees", nsContext ),
                                                  true );
            latitudeOfNaturalOrigin = ( latitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( latitudeOfNaturalOrigin )
                                                                                   : latitudeOfNaturalOrigin;

            double longitudeOfNaturalOrigin = getNodeAsDouble( usedProjection, new XPath( PRE
                                                                                          + "longitudeOfNaturalOrigin",
                                                                                          nsContext ), 0 );
            inDegrees = getNodeAsBoolean( usedProjection, new XPath( PRE + "longitudeOfNaturalOrigin/@inDegrees",
                                                                     nsContext ), true );
            longitudeOfNaturalOrigin = ( longitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( longitudeOfNaturalOrigin )
                                                                                     : longitudeOfNaturalOrigin;

            double scaleFactor = getNodeAsDouble( usedProjection, new XPath( PRE + "scaleFactor", nsContext ), 0 );
            double falseEasting = getNodeAsDouble( usedProjection, new XPath( PRE + "falseEasting", nsContext ), 0 );
            double falseNorthing = getNodeAsDouble( usedProjection, new XPath( PRE + "falseNorthing", nsContext ), 0 );

            String projectionName = usedProjection.getLocalName().trim();
            String className = projectionElement.getAttributeValue( new QName( "class" ) );
            Point2d naturalOrigin = new Point2d( longitudeOfNaturalOrigin, latitudeOfNaturalOrigin );
            Projection result = null;
            if ( className != null && !"".equals( className.trim() ) ) {
                LOG.debug( "Trying to load user defined projection class: " + className );
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
                    List<OMElement> children = getElements( usedProjection, new XPath( "*", nsContext ) );
                    List<OMElement> otherValues = new LinkedList<OMElement>();
                    for ( OMElement child : children ) {
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
                    LOG.debug( "Loading of user defined transformation class: " + className + " was not successful" );
                }

            } else {
                // no selfdefined projection, try one of the following, for the projection specific parameters, if any.
                if ( "transverseMercator".equalsIgnoreCase( projectionName ) ) {
                    // change schema to let projection be identifiable. fix method geodetic
                    boolean northernHemi = getNodeAsBoolean( usedProjection, new XPath( PRE + "northernHemisphere",
                                                                                        nsContext ), true );
                    result = new TransverseMercator( northernHemi, underlyingCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scaleFactor );
                } else if ( "lambertAzimuthalEqualArea".equalsIgnoreCase( projectionName ) ) {
                    result = new LambertAzimuthalEqualArea( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                            units, scaleFactor );
                } else if ( "lambertConformalConic".equalsIgnoreCase( projectionName ) ) {
                    double firstP = getNodeAsDouble( usedProjection, new XPath( PRE + "firstParallelLatitude",
                                                                                nsContext ), Double.NaN );
                    inDegrees = getNodeAsBoolean( usedProjection, new XPath( PRE + "firstParallelLatitude/@inDegrees",
                                                                             nsContext ), true );
                    firstP = ( !Double.isNaN( firstP ) && inDegrees ) ? Math.toRadians( firstP ) : firstP;

                    double secondP = getNodeAsDouble( usedProjection, new XPath( PRE + "secondParallelLatitude",
                                                                                 nsContext ), Double.NaN );
                    inDegrees = getNodeAsBoolean( usedProjection, new XPath( PRE + "secondParallelLatitude/@inDegrees",
                                                                             nsContext ), true );
                    secondP = ( !Double.isNaN( secondP ) && inDegrees ) ? Math.toRadians( secondP ) : secondP;
                    result = new LambertConformalConic( firstP, secondP, underlyingCRS, falseNorthing, falseEasting,
                                                        naturalOrigin, units, scaleFactor );
                } else if ( "stereographicAzimuthal".equalsIgnoreCase( projectionName ) ) {
                    double trueScaleL = getNodeAsDouble( usedProjection, new XPath( PRE + "trueScaleLatitude",
                                                                                    nsContext ), Double.NaN );
                    inDegrees = getNodeAsBoolean( usedProjection, new XPath( PRE + "trueScaleLatitude/@inDegrees",
                                                                             nsContext ), true );
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

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        LOG.debug( "The retrieval of transformations is not supported for this (0.2.0) deegree crs configuration format." );
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
     * @return the instantiated {@link CRSIdentifiable} or <code>null</code> if it could not be parsed.
     */
    public CRSIdentifiable parseIdentifiableObject( String id ) {
        if ( id == null || "".equals( id ) ) {
            return null;
        }
        OMElement resolvedID = null;
        try {
            resolvedID = getURIAsType( id );
        } catch ( IOException e ) {
            throw new CRSConfigurationException( e );
        }
        CRSIdentifiable result = null;
        if ( resolvedID != null ) {
            String localName = resolvedID.getLocalName();
            if ( localName != null && !"".equals( localName.trim() ) ) {
                if ( localName.equals( "ellipsoid" ) ) {
                    result = getEllipsoidForId( id );
                } else if ( localName.equals( "geodeticDatum" ) ) {
                    result = getGeodeticDatumForId( id );
                } else if ( localName.equals( "projectedCRS" ) || localName.equals( "geographicCRS" )
                            || localName.equals( "compoundCRS" ) || localName.equals( "geocentricCRS" ) ) {
                    result = getProvider().getCRSByCode( CRSCodeType.valueOf( id ) );
                } else if ( localName.equals( "primeMeridian" ) ) {
                    result = getPrimeMeridianForId( id );
                } else if ( localName.equals( "wgs84Transformation" ) ) {
                    result = getConversionInfoFromID( id );
                }
            }
        }
        return result;
    }

    /**
     * 
     * @return all available codetypes
     * @throws CRSConfigurationException
     */
    public List<CRSCodeType[]> getAvailableCRSCodes()
                            throws CRSConfigurationException {
        List<CRSCodeType[]> result = new LinkedList<CRSCodeType[]>();

        List<OMElement> crss = getElements( getRootElement(), new XPath( "//" + PRE + "geographicCRS", nsContext ) );
        addCRSIds( result, crss );
        crss = getElements( getRootElement(), new XPath( "//" + PRE + "projectedCRS", nsContext ) );
        addCRSIds( result, crss );
        crss = getElements( getRootElement(), new XPath( "//" + PRE + "geocentricCRS", nsContext ) );
        addCRSIds( result, crss );
        crss = getElements( getRootElement(), new XPath( "//" + PRE + "compoundCRS", nsContext ) );
        addCRSIds( result, crss );

        // for ( OMElement crs : allCRSIDs ) {
        // if ( crs != null ) {
        // result.add();
        // }
        // }
        return result;
    }

    /**
     * adds a list of arrays to the given result set
     * 
     * @throws CRSConfigurationException
     */
    private void addCRSIds( List<CRSCodeType[]> result, List<OMElement> crsElements )
                            throws CRSConfigurationException {
        try {
            for ( OMElement crs : crsElements ) {
                List<OMElement> ids = getElements( crs, new XPath( "./" + PRE + "id", nsContext ) );
                if ( !ids.isEmpty() ) {
                    CRSCodeType[] r = new CRSCodeType[ids.size()];
                    for ( int i = 0; i < ids.size(); ++i ) {
                        OMElement id = ids.get( i );
                        if ( id != null ) {
                            r[i] = CRSCodeType.valueOf( getNodeAsString( id, new XPath( ".", nsContext ), null ) );
                        }
                    }
                    result.add( r );
                }
            }
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_GET_ALL_ELEMENT_IDS", e.getMessage() ),
                                                 e );
        }
    }

    public Projection getProjectionForId( String usedProjection, GeographicCRS underlyingCRS ) {
        LOG.debug( "Projection on id not supported for crs version < 0.3" );
        return null;
    }

}
