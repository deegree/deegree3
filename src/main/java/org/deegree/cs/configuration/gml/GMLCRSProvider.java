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

package org.deegree.cs.configuration.gml;

// import static org.deegree.commons.xml.XMLTools.getElement;
// import static org.deegree.commons.xml.XMLTools.getNodesAsStrings;
// import static org.deegree.commons.xml.XMLTools.getRequiredElement;
// import static org.deegree.commons.xml.XMLTools.getRequiredNodeAsDouble;
// import static org.deegree.commons.xml.XMLTools.getRequiredNodeAsString;
import static org.deegree.cs.components.Unit.createUnitFromString;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.COMPOUND;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.GEOCENTRIC;
import static org.deegree.cs.coordinatesystems.CoordinateSystem.CRSType.GEOGRAPHIC;
import static org.deegree.cs.projections.SupportedProjections.fromCodes;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.vecmath.Point2d;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Axis;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.components.Unit;
import org.deegree.cs.components.VerticalDatum;
import org.deegree.cs.configuration.AbstractCRSProvider;
import org.deegree.cs.coordinatesystems.CompoundCRS;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeocentricCRS;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.coordinatesystems.ProjectedCRS;
import org.deegree.cs.coordinatesystems.VerticalCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.projections.SupportedProjectionParameters;
import org.deegree.cs.projections.SupportedProjections;
import org.deegree.cs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.cs.projections.azimuthal.StereographicAlternative;
import org.deegree.cs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.cs.projections.conic.LambertConformalConic;
import org.deegree.cs.projections.cylindric.TransverseMercator;
import org.deegree.cs.transformations.SupportedTransformationParameters;
import org.deegree.cs.transformations.SupportedTransformations;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.coordinate.GeocentricTransform;
import org.deegree.cs.transformations.coordinate.NotSupportedTransformation;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.ntv2.NTv2Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>GMLCRSProvider</code> is a provider for a GML 3.2 backend, this may be a dictionary or a database.
 * 
 * Note: not all of the GML3.2. features are implemented yet, but the basics (transformations, crs's, axis, units,
 * projections) should work quite well.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
@LoggingNotes(debug = "Get information about the currently parsed coordinate system components.")
public class GMLCRSProvider extends AbstractCRSProvider<OMElement> {

    private static Logger LOG = LoggerFactory.getLogger( GMLCRSProvider.class );

    private static String PRE = CommonNamespaces.GML3_2_PREFIX + ":";

    private static String GMD_P = "gmd";

    private static String GCO_P = "gco";

    private static String GMD_PRE = GMD_P + ":";

    private static String GCO_PRE = GCO_P + ":";

    private static String GMD_NS = "http://www.isotc211.org/2005/gmd";

    private static String GCO_NS = "http://www.isotc211.org/2005/gco";

    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();
    static {
        nsContext.addNamespace( GMD_PRE, GMD_NS );
        nsContext.addNamespace( GCO_PRE, GCO_NS );
    }

    private XMLAdapter adapter;

    /**
     * The 'default constructor' which will be called by the CRSConfiguration
     * 
     * @param properties
     *            the properties which can hold information about the configuration of this GML provider.
     */
    public GMLCRSProvider( Properties properties ) {
        super( properties, GMLResource.class, null );
        if ( getResolver() == null ) {
            setResolver( new GMLFileResource( this, new Properties( properties ) ) );
        }
        adapter = new XMLAdapter();
    }

    public boolean canExport() {
        return false;
    }

    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        throw new UnsupportedOperationException( "Exporting to gml is currently not supported." );
    }

    public List<CRSCodeType[]> getAvailableCRSCodes()
                            throws CRSConfigurationException {
        return ( (GMLResource) getResolver() ).getAvailableCRSIds();
    }

    public List<CoordinateSystem> getAvailableCRSs()
                            throws CRSConfigurationException {
        return ( (GMLResource) getResolver() ).getAvailableCRSs();
    }

    /**
     * @param rootElement
     *            containing a gml:CRS dom representation.
     * @return a {@link CoordinateSystem} instance initialized with values from the given XML-OM gml:CRS fragment or
     *         <code>null</code> if the given root element is <code>null</code>
     * @throws CRSConfigurationException
     *             if something went wrong.
     */
    @Override
    protected CoordinateSystem parseCoordinateSystem( OMElement rootElement )
                            throws CRSConfigurationException {
        if ( rootElement == null ) {
            LOG.debug( "The given crs root element is null, returning nothing" );
            return null;
        }
        CoordinateSystem result = null;
        String localName = rootElement.getLocalName();

        try {
            if ( "ProjectedCRS".equalsIgnoreCase( localName ) ) {
                result = parseProjectedCRS( rootElement );
            } else if ( "CompoundCRS".equalsIgnoreCase( localName ) ) {
                result = parseCompoundCRS( rootElement );
            } else if ( "GeodeticCRS".equalsIgnoreCase( localName ) ) {
                result = parseGeodeticCRS( rootElement );
            } else {
                LOG.warn( "The given coordinate system:" + localName
                          + " is currently not supported by the deegree gml provider." );
            }
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        } catch ( IOException e ) {
            throw new CRSConfigurationException( e );
        }

        return result;
    }

    /**
     * Calls parseGMLTransformation for the catching of {@link XMLParsingException}.
     */
    @Override
    public Transformation parseTransformation( OMElement rootElement )
                            throws CRSConfigurationException {
        try {
            return parseGMLTransformation( rootElement, null, null );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        } catch ( IOException e ) {
            throw new CRSConfigurationException( e );
        }
    }

    /**
     * Parses some of the gml 3.2 transformation constructs. Currently only helmert transformations are supported.
     * 
     * @param rootElement
     * @param sourceCRS
     *            to be used as the source crs, if <code>null</code> the values from the given transformation will be
     *            parsed.
     * @param targetCRS
     *            to be used as the target crs, if <code>null</code> the values from the given transformation will be
     *            parsed.
     * @return the transformation.
     * @throws XMLParsingException
     * @throws IOException
     */
    public Transformation parseGMLTransformation( OMElement rootElement, CoordinateSystem sourceCRS,
                                                  CoordinateSystem targetCRS )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of transformation method resulted in: " + Arrays.toString( id.getCodes() ) );
        }
        Transformation result = getCachedIdentifiable( Transformation.class, id );
        CoordinateSystem source = sourceCRS;
        CoordinateSystem target = targetCRS;
        if ( result == null ) {
            if ( source == null ) {
                OMElement crsProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "sourceCRS", nsContext ) );
                OMElement crsElem = getRequiredXlinkedElement( crsProp, "*[1]" );
                source = parseCoordinateSystem( crsElem );
                if ( source == null ) {
                    throw new XMLParsingException( adapter, rootElement,
                                                   "The transformation could not be parsed, because the sourceCRS is not supported." );
                }
            }
            if ( target == null ) {
                // crsProp = getRequiredElement( rootElement, PRE + "targetCRS", nsContext );
                OMElement crsProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "targetCRS", nsContext ) );
                String tCRSLinked = retrieveXLink( crsProp );
                // rb: if the wgs 84 was referenced, use the default implementation, maybe this is not a good idea?
                if ( tCRSLinked != null
                     && ( tCRSLinked.contains( "4326" ) || tCRSLinked.toLowerCase().contains( "WGS84" ) ) ) {
                    target = GeographicCRS.WGS84;
                } else {
                    OMElement crsElem = getRequiredXlinkedElement( crsProp, "*[1]" );
                    target = parseCoordinateSystem( crsElem );
                }
                if ( target == null ) {
                    throw new XMLParsingException( adapter, rootElement,
                                                   "The transformation could not be parsed, because the targetCRS is not supported." );
                }
            }

            OMElement method = adapter.getRequiredElement( rootElement, new XPath( PRE + "method", nsContext ) );

            OMElement conversionMethod = getRequiredXlinkedElement( method, PRE + "OperationMethod" );
            CRSIdentifiable conversionMethodID = parseIdentifiedObject( conversionMethod );
            SupportedTransformations transform = SupportedTransformations.fromCodes( conversionMethodID.getCodes() );

            List<Pair<CRSIdentifiable, Object>> parameterValues = parseParameterValues( rootElement );
            switch ( transform ) {
            case GENERAL_POLYNOMIAL:
                LOG.warn( "The mapping of gml:Transformation to Polynomial transformations is not yet implemented." );
                result = new NotSupportedTransformation( source, target, id );
                break;
            case HELMERT_3:
            case HELMERT_7:
                result = createHelmert( id, parameterValues, source, target );
                break;
            case GEOGRAPHIC_GEOCENTRIC:
                LOG.warn( "The mapping of gml:Transformation to Geographic/Geocentic transformations is not necessary." );
                if ( target.getType() == GEOCENTRIC ) {
                    result = new GeocentricTransform( source, (GeocentricCRS) target );
                } else if ( target.getType() == COMPOUND ) {
                    if ( ( (CompoundCRS) target ).getUnderlyingCRS().getType() == GEOCENTRIC ) {
                        result = new GeocentricTransform( source,
                                                          (GeocentricCRS) ( (CompoundCRS) target ).getUnderlyingCRS() );
                    }
                } else {
                    result = new NotSupportedTransformation( source, target, id );
                }

                break;
            case LONGITUDE_ROTATION:
                LOG.warn( "The mapping of gml:Transformation to a longitude rotation is not necessary." );
                result = new NotSupportedTransformation( source, target, id );
                break;
            case NTV2:
                result = createNTv2( id, parameterValues, source, target );
                break;
            case NOT_SUPPORTED:
                LOG.warn( "The gml:Transformation could not be mapped to a deegree transformation." );
                result = new NotSupportedTransformation( source, target, id );
            }
        }
        return addIdToCache( result, false );
    }

    /**
     * Creates a {@link Helmert} transformation from the given parameter list.
     * 
     * @param id
     *            of the transformation.
     * @param parameterValues
     *            the list of values, the Object must be a {@link Double} (denoting a the rotation/translation/ppm of
     *            the helmert.)
     * @param source
     *            to go from
     * @param target
     *            to go to
     * 
     * @return a helmert transformation matrix from the given parameter list.
     */
    @SuppressWarnings("unchecked")
    protected Helmert createHelmert( CRSIdentifiable id, List<Pair<CRSIdentifiable, Object>> parameterValues,
                                     CoordinateSystem source, CoordinateSystem target ) {
        double dx = 0, dy = 0, dz = 0, ex = 0, ey = 0, ez = 0, ppm = 0;
        for ( Pair<CRSIdentifiable, Object> paramValue : parameterValues ) {
            if ( paramValue != null && ( paramValue.second instanceof Pair<?, ?> ) ) {
                Pair<Unit, Double> second = (Pair<Unit, Double>) paramValue.second;
                if ( second != null ) {
                    double value = second.second;
                    if ( !Double.isNaN( value ) ) {
                        CRSIdentifiable paramID = paramValue.first;
                        if ( paramID != null ) {
                            SupportedTransformationParameters paramType = SupportedTransformationParameters.fromCodes( paramID.getCodes() );
                            Unit unit = second.first;
                            // If a unit was given, convert the value to the internally used
                            // unit.
                            if ( unit != null && !unit.isBaseType() ) {
                                value = unit.toBaseUnits( value );
                            }
                            switch ( paramType ) {
                            case X_AXIS_ROTATION:
                                ex = value;
                                break;
                            case Y_AXIS_ROTATION:
                                ey = value;
                                break;
                            case Z_AXIS_ROTATION:
                                ez = value;
                                break;
                            case X_AXIS_TRANSLATION:
                                dx = value;
                                break;
                            case Y_AXIS_TRANSLATION:
                                dy = value;
                                break;
                            case Z_AXIS_TRANSLATION:
                                dz = value;
                                break;
                            case SCALE_DIFFERENCE:
                                ppm = value;
                                break;
                            default:
                                LOG.warn( "The (helmert) transformation parameter: " + paramID.getCodeAndName()
                                          + " could not be mapped to a valid parameter and will not be used." );
                                break;
                            }
                        }

                    }
                }
            }
        }
        return new Helmert( dx, dy, dz, ex, ey, ez, ppm, source, target, id, true );
    }

    /**
     * Create an {@link NTv2Transformation} from the given parameter list.
     * 
     * @param id
     *            of the transformation.
     * @param parameterValues
     *            the list of values, the Object must be a String (denoting a gridshift file url.)
     * @param source
     *            to go from
     * @param target
     *            to go to
     * @return an {@link NTv2Transformation} if a file was given, <code>null</code> otherwise.
     */
    protected NTv2Transformation createNTv2( CRSIdentifiable id, List<Pair<CRSIdentifiable, Object>> parameterValues,
                                             CoordinateSystem source, CoordinateSystem target ) {
        NTv2Transformation result = null;
        if ( !parameterValues.isEmpty() ) {
            Pair<CRSIdentifiable, Object> paramValue = parameterValues.get( 0 );
            if ( paramValue != null && ( paramValue.second instanceof String ) ) {
                String second = (String) paramValue.second;
                if ( second != null ) {
                    URL url = null;
                    try {
                        url = new URL( second );
                    } catch ( Throwable t ) {
                        LOG.debug( "Could not load NTv2 file from location: " + second );
                    }
                    if ( url != null ) {
                        result = new NTv2Transformation( source, target, id, url );
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param rootElement
     *            which is a subtype of gml:IdentifiedObject and gml:DefinitionType or gml:AbstractCRSType
     * @return the {@link CRSIdentifiable} instance, its values are filled with the values of the given gml instance.
     * @throws XMLParsingException
     *             if the given rootElement could not be parsed.
     */
    public CRSIdentifiable parseIdentifiedObject( OMElement rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            return null;
        }
        List<String> versions = new ArrayList<String>();
        List<String> descriptions = new ArrayList<String>();
        List<String> areasOfUse = new ArrayList<String>();
        String identifier = null;
        try {
            identifier = adapter.getRequiredNodeAsString( rootElement, new XPath( PRE + "identifier", nsContext ) );
        } catch ( XMLParsingException e ) {
            LOG.error( "Could not find the required identifier node for the given gml:identifiable with localname: "
                       + rootElement.getLocalName() );
            return null;
        }
        String[] identifiers = { identifier };

        String tmpDesc = adapter.getNodeAsString( rootElement, new XPath( PRE + "description", nsContext ), null );

        if ( tmpDesc != null ) {
            descriptions.add( tmpDesc );
        }
        // try to find the href
        OMElement descRef = adapter.getElement( rootElement, new XPath( PRE + "descriptionReference", nsContext ) );
        if ( descRef != null ) {
            String href = descRef.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
            if ( !"".equals( href ) ) {
                descriptions.add( href );
            }
        }
        List<OMElement> metaDatas = adapter.getElements( rootElement, new XPath( PRE + "metaDataProperty", nsContext ) );
        if ( metaDatas != null && metaDatas.size() > 0 ) {
            LOG.warn( "Ignoring meta data properties" );
            // for ( Element metaDataElement : metaDatas ) {
            // String metaData = "<![CDATA["
            // + DOMPrinter.nodeToString( metaDataElement, CharsetUtils.getSystemCharset() ) + "]]>";
            // versions.add( metaData );
            // }
        }

        // List<Element> domainsOfValidity = XMLTools.getElements( rootElement, PRE + "domainOfValidity", nsContext );
        List<OMElement> domainsOfValidity = adapter.getElements( rootElement, new XPath( PRE + "domainOfValidity",
                                                                                         nsContext ) );
        if ( domainsOfValidity != null && domainsOfValidity.size() > 0 ) {
            // <domainOfValidity><gmd:geographicElement><gmd:EX_GeographicBoundingBox>
            OMElement elem = adapter.getElement( domainsOfValidity.get( 0 ), new XPath( GMD_PRE + "EX_Extent/"
                                                                                        + GMD_PRE
                                                                                        + "geographicElement/"
                                                                                        + GMD_PRE
                                                                                        + "EX_GeographicBoundingBox",
                                                                                        nsContext ) );
            if ( elem != null ) {
                double w = adapter.getNodeAsDouble( elem, new XPath( GMD_PRE + "westBoundLongitude/" + GCO_PRE
                                                                     + "Decimal", nsContext ), -180 );
                double e = adapter.getNodeAsDouble( elem, new XPath( GMD_PRE + "eastBoundLongitude/" + GCO_PRE
                                                                     + "Decimal", nsContext ), 180 );
                double s = adapter.getNodeAsDouble( elem, new XPath( GMD_PRE + "southBoundLatitude/" + GCO_PRE
                                                                     + "Decimal", nsContext ), -90 );
                double n = adapter.getNodeAsDouble( elem, new XPath( GMD_PRE + "northBoundLatitude/" + GCO_PRE
                                                                     + "Decimal", nsContext ), 90 );
                areasOfUse.add( w + "," + s + "," + e + "," + n );

            } else {
                LOG.warn( "No 'gmd:geographicElement/gmd:EX_GeographicBoundingBox' found in domainOfValidity, ignoring" );
            }
        }
        // String[] scopes = XMLTools.getNodesAsStrings( rootElement, PRE + "scope", nsContext );
        String[] scopes = adapter.getNodesAsStrings( rootElement, new XPath( PRE + "scope", nsContext ) );
        if ( scopes != null && scopes.length > 0 ) {
            // LOG.debug( "scopes will be put in the area of uses" );
            for ( String scope : scopes ) {
                areasOfUse.add( "Scope: " + scope );
            }
        }

        // String[] names = getNodesAsStrings( rootElement, PRE + "name", nsContext );
        String[] names = adapter.getNodesAsStrings( rootElement, new XPath( PRE + "name", nsContext ) );
        if ( names != null && names.length > 0 ) {
            // LOG.debug( "Using defined names as identifiers as well" );
            // +1 for the identifier
            identifiers = new String[names.length + 1];
            identifiers[0] = identifier;
            System.arraycopy( names, 0, identifiers, 1, names.length );
        }

        // convert identifiers to codes
        CRSCodeType[] crsCodes = new CRSCodeType[identifiers.length];
        int n = identifiers.length;
        for ( int i = 0; i < n; i++ ) {
            crsCodes[i] = CRSCodeType.valueOf( identifiers[i] );
        }
        CRSIdentifiable result = new CRSIdentifiable( crsCodes, names, versions.toArray( new String[versions.size()] ),
                                                      descriptions.toArray( new String[descriptions.size()] ),
                                                      areasOfUse.toArray( new String[areasOfUse.size()] ) );
        return result;

    }

    /**
     * This methods parses the given element and maps it onto a {@link CompoundCRS}. Currently only gml:CompoundCRS 's
     * consisting of following combination is supported:
     * <ul>
     * <li>Projected CRS with VerticalCRS</li>
     * </ul>
     * 
     * Geographic crs with a height axis can be mapped in a {@link CompoundCRS} by calling the
     * {@link #parseGeodeticCRS(OMElement)}
     * 
     * @param rootElement
     *            containing a gml:CompoundCRS dom representation.
     * @return a {@link CompoundCRS} instance initialized with values from the given XML-OM gml:CompoundCRS fragment.
     * @throws XMLParsingException
     * @throws IOException
     */
    @SuppressWarnings("null")
    protected CompoundCRS parseCompoundCRS( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given crs root element is null, returning nothing" );
            return null;
        }

        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of compound crs resulted in: " + Arrays.toString( id.getCodes() ) );
        }

        List<OMElement> compRefSysProp = adapter.getRequiredElements( rootElement,
                                                                      new XPath( PRE + "componentReferenceSystem",
                                                                                 nsContext ) );
        if ( compRefSysProp.size() != 2 ) {
            throw new XMLParsingException( adapter, rootElement,
                                           "Currently, compound crs definitions can only constist of exactly two base crs's, you supplied: "
                                                                   + compRefSysProp.size() );
        }

        // Find the first and second crs's of the compound crs.
        OMElement first = compRefSysProp.get( 0 );
        OMElement second = compRefSysProp.get( 1 );
        // | " + PRE + "VerticalCRS"
        OMElement xlinkedElem1 = retrieveAndResolveXLink( first );
        OMElement xlinkedElem2 = retrieveAndResolveXLink( second );

        OMElement crsElement1 = null;
        OMElement crsElement2 = null;

        if ( xlinkedElem1 == null ) {
            crsElement1 = adapter.getRequiredElement( first, new XPath( "*[1]", nsContext ) );
        }
        if ( xlinkedElem2 == null ) {
            crsElement2 = adapter.getRequiredElement( first, new XPath( "*[2]", nsContext ) );
        }

        ProjectedCRS underlying = null;
        VerticalCRS vertical = null;

        if ( "ProjectedCRS".equals( crsElement1.getLocalName() ) ) {
            if ( "VerticalCRS".equals( crsElement2.getLocalName() ) ) {
                CoordinateSystem firstRes = parseProjectedCRS( crsElement1 );
                if ( firstRes.getType() == COMPOUND ) {
                    underlying = (ProjectedCRS) ( (CompoundCRS) firstRes ).getUnderlyingCRS();
                } else {
                    underlying = (ProjectedCRS) firstRes;
                }
                vertical = parseVerticalCRS( crsElement2 );

            } else {
                throw new XMLParsingException( adapter, crsElement2,
                                               "Currently only Compoundcrs's with the ProjectedCRS and VerticalCRS combination are supported, instead a:"
                                                                       + crsElement2.getLocalName() + " was found." );
            }
        } else if ( "VerticalCRS".equals( crsElement1.getLocalName() ) ) {
            if ( "ProjectedCRS".equals( crsElement2.getLocalName() ) ) {
                CoordinateSystem firstRes = parseProjectedCRS( crsElement2 );
                if ( firstRes.getType() == COMPOUND ) {
                    underlying = (ProjectedCRS) ( (CompoundCRS) firstRes ).getUnderlyingCRS();
                } else {
                    underlying = (ProjectedCRS) firstRes;
                }
                vertical = parseVerticalCRS( crsElement1 );
            } else {
                throw new XMLParsingException( adapter, crsElement2,
                                               "Currently only Compoundcrs's with the ProjectedCRS and VerticalCRS combination are supported, instead a:"
                                                                       + crsElement1.getLocalName() + " was found." );
            }
        } else {
            throw new XMLParsingException(
                                           adapter,
                                           crsElement1,
                                           "Currently only Compoundcrs's with the ProjectedCRS and VerticalCRS combination are supported, following elements were found:"
                                                                   + crsElement1.getLocalName()
                                                                   + " and "
                                                                   + crsElement2.getLocalName() + "." );
        }

        return new CompoundCRS( vertical.getVerticalAxis(), underlying, 0, id );
    }

    /**
     * @param rootElement
     *            containing a gml:ProjectedCRS dom representation.
     * @return a {@link ProjectedCRS} instance initialized with values from the given XML-OM gml:ProjectedCRS fragment
     *         or <code>null</code> if the given root element is <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     *             if a retrieval of an xlink of one of the subelements failed.
     */
    protected CoordinateSystem parseProjectedCRS( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given crs root element is null, returning nothing" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of projected crs resulted in: " + Arrays.toString( id.getCodes() ) );
        }

        OMElement baseGEOCRSElementProperty = adapter.getRequiredElement( rootElement, new XPath( PRE
                                                                                                  + "baseGeodeticCRS",
                                                                                                  nsContext ) );

        CoordinateSystem parsedBaseCRS = parseGeodeticCRS( getRequiredXlinkedElement( baseGEOCRSElementProperty,
                                                                                      PRE + "GeodeticCRS" ) );
        if ( parsedBaseCRS == null ) {
            throw new XMLParsingException( adapter, baseGEOCRSElementProperty,
                                           "No basetype for the projected crs found, each projected crs must have a base crs." );
        }
        GeographicCRS underlyingCRS = null;
        if ( parsedBaseCRS.getType() == COMPOUND ) {
            CoordinateSystem cmpBase = ( (CompoundCRS) parsedBaseCRS ).getUnderlyingCRS();
            if ( cmpBase.getType() != GEOGRAPHIC ) {
                throw new XMLParsingException( adapter, baseGEOCRSElementProperty,
                                               "Only geographic crs's can be the base type of a projected crs." );
            }
            underlyingCRS = (GeographicCRS) cmpBase;
        } else if ( parsedBaseCRS.getType() == GEOGRAPHIC ) {
            underlyingCRS = (GeographicCRS) parsedBaseCRS;
        } else {
            throw new XMLParsingException( adapter, baseGEOCRSElementProperty,
                                           "Only geographic crs's can be the base type of a projected crs." );
        }

        OMElement cartesianCSProperty = adapter.getRequiredElement( rootElement, new XPath( PRE + "cartesianCS",
                                                                                            nsContext ) );
        Axis[] axis = parseAxisFromCSType( getRequiredXlinkedElement( cartesianCSProperty, PRE + "CartesianCS" ) );
        if ( axis.length != 2 ) {
            throw new XMLParsingException( adapter, cartesianCSProperty,
                                           "The ProjectedCRS may only have 2 axis defined" );
        }

        OMElement conversionElementProperty = adapter.getRequiredElement( rootElement, new XPath( PRE + "conversion",
                                                                                                  nsContext ) );
        Projection projection = parseProjection(
                                                 getRequiredXlinkedElement( conversionElementProperty, PRE
                                                                                                       + "Conversion" ),
                                                 underlyingCRS );
        CoordinateSystem result = new ProjectedCRS( projection, axis, id );
        if ( parsedBaseCRS.getType() == COMPOUND ) {
            result = new CompoundCRS( ( (CompoundCRS) parsedBaseCRS ).getHeightAxis(), result,
                                      ( (CompoundCRS) parsedBaseCRS ).getDefaultHeight(), id );
        }
        return result;
    }

    /**
     * @param rootElement
     *            containing a gml:GeodeticCRS dom representation.
     * @return a {@link CoordinateSystem} instance initialized with values from the given XML-OM gml:GeodeticCRS
     *         fragment or <code>null</code> if the given root element is <code>null</code>. Note the result may be a
     *         {@link CompoundCRS}, a {@link GeographicCRS} or a {@link GeocentricCRS}, depending of the definition of
     *         the CS type.
     * @throws XMLParsingException
     * @throws IOException
     */
    protected CoordinateSystem parseGeodeticCRS( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given crs root element is null, returning nothing" );
            return null;
        }
        // check for xlink in the root element.

        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of geodetic crs resulted in: " + Arrays.toString( id.getCodes() ) );
        }

        OMElement datumElementProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "geodeticDatum",
                                                                                         nsContext ) );
        OMElement datumElement = getRequiredXlinkedElement( datumElementProp, PRE + "GeodeticDatum" );

        OMElement csTypeProp = adapter.getElement( rootElement, new XPath( PRE + "ellipsoidalCS", nsContext ) );
        OMElement csTypeElement = null;
        if ( csTypeProp == null ) {
            csTypeProp = adapter.getElement( rootElement, new XPath( PRE + "cartesianCS", nsContext ) );
            if ( csTypeProp == null ) {
                csTypeProp = adapter.getElement( rootElement, new XPath( PRE + "sphericalCS", nsContext ) );
                if ( csTypeProp == null ) {
                    throw new XMLParsingException( adapter, rootElement,
                                                   "The geodetic datum does not define one of the required cs types: ellipsoidal, cartesian or spherical." );
                }
                throw new XMLParsingException( adapter, csTypeProp, "The sphericalCS is currently not supported." );
            }
            csTypeElement = getRequiredXlinkedElement( csTypeProp, PRE + "CartesianCS" );

        } else {
            csTypeElement = getRequiredXlinkedElement( csTypeProp, PRE + "EllipsoidalCS" );
        }
        GeodeticDatum datum = parseDatum( datumElement );
        Axis[] axis = parseAxisFromCSType( csTypeElement );
        CoordinateSystem result = null;
        if ( axis != null ) {
            if ( "ellipsoidalCS".equals( csTypeProp.getLocalName() ) ) {
                if ( axis.length == 2 ) {
                    result = new GeographicCRS( datum, axis, id );
                } else {
                    result = new CompoundCRS( axis[2], new GeographicCRS( datum, new Axis[] { axis[0], axis[1] }, id ),
                                              0, id );
                }
            } else {
                result = new GeocentricCRS( datum, axis, id );
            }
        } else {
            throw new XMLParsingException( adapter, csTypeElement,
                                           "No Axes were found in the geodetic crs, this may not be." );
        }

        return result;
    }

    /**
     * @param rootElement
     *            containing a gml:GeodeticDatum dom representation.
     * @return a {@link GeodeticDatum} instance initialized with values from the given XML-OM fragment or
     *         <code>null</code> if the given root element is <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     *             if a retrieval of an xlink of one of the subelements failed.
     */
    protected GeodeticDatum parseDatum( OMElement rootElement )
                            throws IOException, XMLParsingException {
        if ( rootElement == null ) {
            LOG.debug( "The given datum element is null, returning nothing" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of datum resulted in: " + Arrays.toString( id.getCodes() ) );
        }
        GeodeticDatum result = getCachedIdentifiable( GeodeticDatum.class, id );
        if ( result == null ) {
            OMElement pmElementProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "primeMeridian",
                                                                                          nsContext ) );
            OMElement pmElement = getRequiredXlinkedElement( pmElementProp, PRE + "PrimeMeridian" );
            PrimeMeridian pm = parsePrimeMeridian( pmElement );

            OMElement ellipsoidElementProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "ellipsoid",
                                                                                                 nsContext ) );
            OMElement ellipsoidElement = getRequiredXlinkedElement( ellipsoidElementProp, PRE + "Ellipsoid" );
            Ellipsoid ellipsoid = parseEllipsoid( ellipsoidElement );
            result = new GeodeticDatum( ellipsoid, pm, id );
        }

        return addIdToCache( result, false );
    }

    /**
     * For the ellipsoidal and cartesian cs Types, this method also checks the consistency of axis (radian, radian,
     * [metre] ) or (metre, metre, [metre] ). If the conditions are not met, an xml parsing exception will be thrown as
     * well.
     * 
     * @param rootElement
     *            containing a (Ellipsoidal, Spherical, Cartesian) CS type dom representation.
     * @return a {@link Axis} array instance initialized with values from the given XML-OM fragment or <code>null</code>
     *         if the given root element is <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     *             if a retrieval of an xlink of one of the subelements failed.
     */
    protected Axis[] parseAxisFromCSType( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given coordinate type element is null, returning nothing" );
            return null;
        }
        List<OMElement> axisProps = adapter.getRequiredElements( rootElement, new XPath( PRE + "axis", nsContext ) );

        if ( axisProps.size() > 3 ) {
            throw new XMLParsingException( adapter, rootElement, "The CS type defines to many axes." );
        }
        if ( axisProps.size() == 0 ) {
            throw new XMLParsingException( adapter, rootElement, "The CS type defines no axes." );
        }

        Axis[] axis = new Axis[axisProps.size()];
        for ( int i = 0; i < axisProps.size(); i++ ) {
            OMElement axisElement = getRequiredXlinkedElement( axisProps.get( i ), PRE + "CoordinateSystemAxis" );
            Axis a = parseAxis( axisElement );
            if ( a == null ) {
                throw new XMLParsingException( adapter, axisElement, "Axis: " + i
                                                                     + " of the CS Type is null, this may not be." );
            }
            axis[i] = a;
        }
        if ( "cartesianCS".equalsIgnoreCase( rootElement.getLocalName() ) ) {
            for ( int i = 0; i < axis.length; ++i ) {
                if ( !axis[i].getUnits().canConvert( Unit.METRE ) ) {
                    throw new XMLParsingException( adapter, rootElement,
                                                   "The units of all axis of a (cartesian) cs must be convertable to metres. Axis "
                                                                           + i + " is not: " + axis[i] );
                }
            }
        } else if ( "ellipsoidalCS".equalsIgnoreCase( rootElement.getLocalName() ) ) {
            if ( axis.length < 2 && axis.length > 3 ) {
                throw new XMLParsingException( adapter, rootElement, "An ellipsoidal cs can only have 2 or 3 axis." );
            }
            if ( axis[0].getUnits() == null ) {
                LOG.debug( "Could not check axis [0]: " + axis[0] + " because it has no units." );
            } else if ( axis[1].getUnits() == null ) {
                LOG.debug( "Could not check axis [1]: " + axis[1] + " because it has no units." );
            } else {
                if ( !( axis[0].getUnits().canConvert( Unit.RADIAN ) && axis[1].getUnits().canConvert( Unit.RADIAN ) ) ) {
                    throw new XMLParsingException( adapter, rootElement,
                                                   "The axis of the geodetic (Geographic) crs are not consistent: "
                                                                           + axis[0] + ", " + axis[1] );
                }
                if ( axis.length == 3 ) {
                    if ( axis[2].getUnits() == null ) {
                        LOG.debug( "Could not check axis [2]: " + axis + " because it has no units." );
                    } else {
                        if ( !axis[2].getUnits().canConvert( Unit.METRE ) ) {
                            throw new XMLParsingException( adapter, rootElement,
                                                           "The units of the third axis of the ellipsoidal CS type must be convertable to metre it is not: "
                                                                                   + axis[2] );
                        }
                    }

                }
            }

        } else if ( "verticalcs".equalsIgnoreCase( rootElement.getLocalName() ) ) {
            if ( axis.length != 1 ) {
                throw new XMLParsingException( adapter, rootElement, "A vertical cs can only have 1 axis." );
            }
            if ( !axis[0].getUnits().canConvert( Unit.METRE ) ) {
                throw new XMLParsingException( adapter, rootElement,
                                               "The axis of the vertical crs is not convertable to metre, other values are currently not supported: "
                                                                       + axis[0] );
            }
        }

        return axis;
    }

    /**
     * @param rootElement
     *            containing an gml:CoordinateSystemAxis type dom representation.
     * @return an {@link Axis} instance initialized with values from the given XML-OM fragment or <code>null</code> if
     *         the given root element is <code>null</code> if the axis could not be mapped it's orientation will be
     *         {@link Axis#AO_OTHER}
     * 
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     */
    protected Axis parseAxis( OMElement rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            LOG.debug( "The given axis element is null, returning nothing" );
            return null;
        }
        String name = adapter.getRequiredNodeAsString( rootElement, new XPath( PRE + "axisAbbrev", nsContext ) );
        String orientation = adapter.getRequiredNodeAsString( rootElement, new XPath( PRE + "axisDirection", nsContext ) );
        Unit unit = parseUnitOfMeasure( rootElement );
        if ( unit == null ) {
            unit = Unit.METRE;
        }
        return new Axis( unit, name, orientation );
    }

    /**
     * @param rootElement
     *            containing a gml:Ellipsoid dom representation.
     * @return a {@link Ellipsoid} instance initialized with values from the given XML-OM fragment or <code>null</code>
     *         if the given root element is <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * 
     */
    protected Ellipsoid parseEllipsoid( OMElement rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            LOG.debug( "The given ellipsoid element is null, returning nothing" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of ellipsoid resulted in: " + Arrays.toString( id.getCodes() ) );
        }
        Ellipsoid result = getCachedIdentifiable( Ellipsoid.class, id );
        if ( result == null ) {

            OMElement semiMajorAxisElem = adapter.getRequiredElement( rootElement, new XPath( PRE + "semiMajorAxis",
                                                                                              nsContext ) );
            double semiMajorAxis = adapter.getRequiredNodeAsDouble( semiMajorAxisElem, new XPath( ".", nsContext ) );
            Unit unit = parseUnitOfMeasure( semiMajorAxisElem );

            OMElement otherParam = adapter.getRequiredElement( rootElement, new XPath( PRE + "secondDefiningParameter/"
                                                                                       + PRE
                                                                                       + "SecondDefiningParameter",
                                                                                       nsContext ) );
            OMElement param = adapter.getElement( otherParam, new XPath( PRE + "inverseFlattening", nsContext ) );
            int type = 0;// inverseFlattening
            if ( param == null ) {
                param = adapter.getElement( otherParam, new XPath( PRE + "semiMinorAxis", nsContext ) );
                if ( param == null ) {
                    param = adapter.getElement( otherParam, new XPath( PRE + "isSphere", nsContext ) );
                    if ( param == null ) {
                        throw new XMLParsingException( adapter, otherParam,
                                                       "The ellipsoid is missing one of inverseFlattening, semiMinorAxis or isSphere" );
                    }
                    type = 2; // sphere
                } else {
                    type = 1; // semiMinor
                }
            }
            double value = semiMajorAxis;
            if ( type == 2 ) {
                result = new Ellipsoid( unit, semiMajorAxis, semiMajorAxis, id );
            } else {
                Unit secondUnit = parseUnitOfMeasure( param );

                value = adapter.getNodeAsDouble( param, new XPath( ".", nsContext ), Double.NaN );
                if ( Double.isNaN( value ) ) {
                    throw new XMLParsingException( adapter, param,
                                                   "The second defining ellipsoid parameter is missing." );
                }
                if ( secondUnit != null ) {
                    if ( !secondUnit.canConvert( unit ) ) {
                        throw new XMLParsingException( adapter, param,
                                                       "Ellispoid axis can only contain comparable unit, supplied are: "
                                                                               + unit + " and " + secondUnit
                                                                               + " which are not convertable." );
                    }
                    if ( !secondUnit.equals( unit ) ) {
                        value = secondUnit.convert( value, unit );
                    }
                }
                if ( type == 0 ) {
                    result = new Ellipsoid( semiMajorAxis, unit, value, id );
                } else {
                    result = new Ellipsoid( unit, semiMajorAxis, value, id );
                }
            }
        }
        return addIdToCache( result, false );
    }

    /**
     * @param rootElement
     *            to create the pm from.
     * @return {@link PrimeMeridian#GREENWICH} or the appropriate pm if a longitude is defined.
     * @throws XMLParsingException
     */
    protected PrimeMeridian parsePrimeMeridian( OMElement rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            LOG.debug( "The given prime meridian element is null, returning Greenwich" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of prime meridian resulted in: " + Arrays.toString( id.getCodes() ) );
        }
        PrimeMeridian result = getCachedIdentifiable( PrimeMeridian.class, id.getCodes() );
        // if ( cache == null ) {
        // // check if the greenwich is already present.
        // cache = getCachedIdentifiable( result.getIdentifiers() );
        // }
        if ( result == null ) {
            OMElement gwLongitudeElem = adapter.getRequiredElement( rootElement, new XPath( PRE + "greenwichLongitude",
                                                                                            nsContext ) );
            double gwLongitude = adapter.getRequiredNodeAsDouble( gwLongitudeElem, new XPath( ".", nsContext ) );
            Unit unit = parseUnitOfMeasure( gwLongitudeElem );
            if ( unit != null && !unit.canConvert( Unit.RADIAN ) ) {
                LOG.error( "The primemeridian must have RADIAN as a base unit." );
            }

            if ( ( Math.abs( gwLongitude ) > 1E-11 ) ) {
                result = new PrimeMeridian( unit, gwLongitude, id );
            }
            if ( result == null ) {
                CRSCodeType[] codes = PrimeMeridian.GREENWICH.getCodes();
                CRSCodeType[] foundCodes = id.getCodes();
                CRSCodeType[] resultCodes = new CRSCodeType[codes.length + foundCodes.length];
                System.arraycopy( codes, 0, resultCodes, 0, codes.length );
                System.arraycopy( foundCodes, 0, resultCodes, foundCodes.length, foundCodes.length );
                id = new CRSIdentifiable( resultCodes, id.getNames(), id.getVersions(), id.getDescriptions(),
                                          id.getAreasOfUse() );
                result = new PrimeMeridian( Unit.RADIAN, 0, id );
            }
        }
        return addIdToCache( result, false );
    }

    /**
     * @param rootElement
     *            containing a gml:VerticalCRS dom representation.
     * @return a {@link VerticalCRS} instance initialized with values from the given XML-OM fragment or
     *         <code>null</code> if the given root element is <code>null</code>
     * @throws IOException
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * 
     */
    protected VerticalCRS parseVerticalCRS( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given vertical crs root element is null, returning nothing" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of vertical crs resulted in: " + Arrays.toString( id.getCodes() ) );
        }
        OMElement verticalCSProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "verticalCS", nsContext ) );
        OMElement verticalCSType = getRequiredXlinkedElement( verticalCSProp, PRE + "VerticalCS" );
        // the axis will be one which is metre consistent.
        Axis[] axis = parseAxisFromCSType( verticalCSType );
        OMElement verticalDatumProp = adapter.getRequiredElement( rootElement, new XPath( PRE + "verticalDatum",
                                                                                          nsContext ) );
        OMElement vdType = getRequiredXlinkedElement( verticalDatumProp, PRE + "VerticalDatum" );
        VerticalDatum vd = parseVerticalDatum( vdType );

        return new VerticalCRS( vd, axis, id );
    }

    /**
     * @param rootElement
     *            containing a gml:VerticalDatum dom representation.
     * @return a {@link VerticalDatum} instance initialized with values from the given XML-OM fragment or
     *         <code>null</code> if the given root element is <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * 
     */
    protected VerticalDatum parseVerticalDatum( OMElement rootElement )
                            throws XMLParsingException {
        if ( rootElement == null ) {
            LOG.debug( "The given vertical datum root element is null, returning nothing" );
            return null;
        }
        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        VerticalDatum result = getCachedIdentifiable( VerticalDatum.class, id );
        if ( result == null ) {
            result = new VerticalDatum( id );
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Parsing id of vertical datum resulted in: " + Arrays.toString( id.getCodes() ) );
            }
        }
        return addIdToCache( result, false );
    }

    /**
     * For now this method actually wraps all information in a gml:AbstractGeneralConversionType (or a derived subtype)
     * into an CRSIdentifiable Object (used for the Projections).
     * 
     * @param rootElement
     *            a gml:GeneralConversion element
     * @param underlyingCRS
     *            of the projection.
     * @return a Projection (Conversion) containing the mapped values from the given gml:Conversion
     *         XML-OM-representation.
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     */
    protected Projection parseProjection( OMElement rootElement, GeographicCRS underlyingCRS )
                            throws XMLParsingException, IOException {
        if ( rootElement == null || !"Conversion".equals( rootElement.getLocalName() ) ) {
            LOG.debug( "The given conversion root element is null, returning nothing" );
            return null;
        }

        CRSIdentifiable id = parseIdentifiedObject( rootElement );
        if ( id == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Parsing id of projection method resulted in: " + Arrays.toString( id.getCodes() ) );
        }

        Projection result = getCachedIdentifiable( Projection.class, id.getCodes() );
        if ( result == null ) {
            OMElement method = adapter.getRequiredElement( rootElement, new XPath( PRE + "method", nsContext ) );

            OMElement conversionMethod = getRequiredXlinkedElement( method, PRE + "OperationMethod" );
            CRSIdentifiable conversionMethodID = parseIdentifiedObject( conversionMethod );

            double falseNorthing = 0, falseEasting = 0, scale = 1, firstParallelLatitude = 0, secondParallelLatitude = 0, trueScaleLatitude = 0;
            Point2d naturalOrigin = new Point2d();
            Unit units = Unit.METRE;
            List<Pair<CRSIdentifiable, Object>> parameterValues = parseParameterValues( rootElement );
            for ( Pair<CRSIdentifiable, Object> paramValue : parameterValues ) {
                if ( paramValue != null && ( paramValue.second instanceof Pair<?, ?> ) ) {
                    Pair<Unit, Double> second = (Pair<Unit, Double>) paramValue.second;
                    if ( second != null ) {
                        double value = second.second;
                        if ( !Double.isNaN( value ) ) {
                            CRSIdentifiable paramID = paramValue.first;
                            if ( paramID != null ) {
                                SupportedProjectionParameters paramType = SupportedProjectionParameters.fromCodes( paramID.getCodes() );
                                Unit unit = second.first;
                                // If a unit was given, convert the value to the internally used unit.
                                if ( unit != null && !unit.isBaseType() ) {
                                    value = unit.toBaseUnits( value );
                                }
                                switch ( paramType ) {
                                case FALSE_EASTING:
                                    falseEasting = value;
                                    break;
                                case FALSE_NORTHING:
                                    falseNorthing = value;
                                    break;
                                case FIRST_PARALLEL_LATITUDE:
                                    firstParallelLatitude = value;
                                    break;
                                case LATITUDE_OF_NATURAL_ORIGIN:
                                    naturalOrigin.y = value;
                                    break;
                                case LONGITUDE_OF_NATURAL_ORIGIN:
                                    naturalOrigin.x = value;
                                    break;
                                case SCALE_AT_NATURAL_ORIGIN:
                                    scale = value;
                                    break;
                                case SECOND_PARALLEL_LATITUDE:
                                    secondParallelLatitude = value;
                                    break;
                                case TRUE_SCALE_LATITUDE:
                                    trueScaleLatitude = value;
                                case NOT_SUPPORTED:
                                default:
                                    LOG.warn( "The projection parameter: " + paramID.getCodeAndName()
                                              + " could not be mapped to any projection and will not be used." );
                                    break;
                                }
                            }

                        }
                    }
                }
            }

            SupportedProjections projection = fromCodes( conversionMethodID.getCodes() );
            switch ( projection ) {
            case TRANSVERSE_MERCATOR:
                boolean northernHemisphere = falseNorthing < 10000000;
                result = new TransverseMercator( northernHemisphere, underlyingCRS, falseNorthing, falseEasting,
                                                 naturalOrigin, units, scale, id );
                break;
            case LAMBERT_AZIMUTHAL_EQUAL_AREA:
                result = new LambertAzimuthalEqualArea( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                        units, scale, id );
                break;
            case LAMBERT_CONFORMAL:
                result = new LambertConformalConic( firstParallelLatitude, secondParallelLatitude, underlyingCRS,
                                                    falseNorthing, falseEasting, naturalOrigin, units, scale, id );
                break;
            case STEREOGRAPHIC_AZIMUTHAL:
                result = new StereographicAzimuthal( trueScaleLatitude, underlyingCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scale, id );
                break;
            case STEREOGRAPHIC_AZIMUTHAL_ALTERNATIVE:
                result = new StereographicAlternative( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                       units, scale, id );
                break;
            case NOT_SUPPORTED:
            default:
                LOG.error( "The conversion method (Projection): " + conversionMethodID.getCode()
                           + " is currently not supported by the deegree crs package." );
            }

            String remarks = adapter.getNodeAsString( rootElement, new XPath( PRE + "remarks", nsContext ), null );
            LOG.debug( "The remarks fo the conversion are not evaluated: " + remarks );
            String accuracy = adapter.getNodeAsString( rootElement, new XPath( PRE + "coordinateOperationAccuracy",
                                                                               nsContext ), null );
            LOG.debug( "The coordinateOperationAccuracy for the conversion are not evaluated: " + accuracy );
        }
        return addIdToCache( result, false );
    }

    /**
     * @param rootElement
     *            which should contain a list of parameter Value properties.
     * @return a list of Pairs containing the parsed OperationParamter and the value as a double, converted to the units
     *         defined in the value element, or the empty list if the rootElement is <code>null</code> or no
     *         parameterValues were found.
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     */
    protected List<Pair<CRSIdentifiable, Object>> parseParameterValues( OMElement rootElement )
                            throws XMLParsingException, IOException {
        List<Pair<CRSIdentifiable, Object>> result = new ArrayList<Pair<CRSIdentifiable, Object>>();
        if ( rootElement == null ) {
            LOG.debug( "The given parameter property root element is null, returning nothing" );
            return result;
        }
        List<OMElement> parameterValues = adapter.getElements( rootElement, new XPath( PRE + "parameterValue",
                                                                                       nsContext ) );
        if ( parameterValues == null || parameterValues.size() < 0 ) {
            LOG.debug( "The root element: " + rootElement.getLocalName() + " does not define any parameters." );
        } else {
            for ( OMElement paramValueProp : parameterValues ) {
                if ( paramValueProp != null ) {
                    Pair<CRSIdentifiable, Object> r = parseParameterValue( paramValueProp );
                    if ( r != null ) {
                        result.add( r );
                    }
                }
            }
        }
        return result;
    }

    /**
     * @param rootElement
     *            containing a parameter Value property.
     * @return a Pair containing the parsed OperationParamter and the value as a double or null if the rootElement is
     *         <code>null</code>
     * @throws XMLParsingException
     *             if the dom tree is not consistent or a required element is missing.
     * @throws IOException
     */
    protected Pair<CRSIdentifiable, Object> parseParameterValue( OMElement rootElement )
                            throws XMLParsingException, IOException {
        if ( rootElement == null ) {
            LOG.debug( "The given parameter property root element is null, returning nothing" );
            return null;
        }
        OMElement paramValue = adapter.getRequiredElement( rootElement, new XPath( PRE + "ParameterValue", nsContext ) );

        OMElement operationParameterProp = adapter.getRequiredElement( paramValue, new XPath( PRE
                                                                                              + "operationParameter",
                                                                                              nsContext ) );

        OMElement operationParameter = getRequiredXlinkedElement( operationParameterProp, PRE + "OperationParameter" );
        CRSIdentifiable paramID = parseIdentifiedObject( operationParameter );

        OMElement valueElem = adapter.getElement( paramValue, new XPath( PRE + "value", nsContext ) );
        Object value = null;
        if ( valueElem == null ) {
            LOG.debug( "No gml:value found in the gml:Conversion/gml:parameterValue/gml:ParameterValue/ node, trying gml:integerValue instead." );
            valueElem = adapter.getElement( paramValue, new XPath( PRE + "integerValue", nsContext ) );
            if ( valueElem == null ) {
                LOG.debug( "No gml:integerValue found in the gml:Conversion/gml:parameterValue/gml:ParameterValue/ node, trying gml:fileValue instead." );
                valueElem = adapter.getElement( paramValue, new XPath( PRE + "valueFile", nsContext ) );
                if ( valueElem == null ) {
                    LOG.debug( "Neither found a gml:integerValue in the gml:Conversion/gml:parameterValue/gml:ParameterValue/ node, ignoring this parameter value." );
                } else {
                    value = adapter.getNodeAsString( valueElem, new XPath( ".", nsContext ), null );
                    if ( value == null ) {
                        LOG.debug( "No value found for fileValue, returning null." );
                        return null;
                    }
                }
            }
        }
        if ( value == null && valueElem != null ) {
            double val = adapter.getNodeAsDouble( valueElem, new XPath( ".", nsContext ), Double.NaN );
            Unit units = parseUnitOfMeasure( valueElem );
            value = new Pair<Unit, Double>( units, val );
        }

        return new Pair<CRSIdentifiable, Object>( paramID, value );
    }

    /**
     * Returns the unit defined by the uomAttribute given of the given element. This method will use a 'colon' heuristic
     * to determine if the given uom is actually an urn (and thus represents an xlink-type). This will then be resolved
     * and mapped onto an unit.
     * 
     * @param elementContainingUOMAttribute
     *            an element containing the 'uom' attribute which will be mapped onto a known unit.
     * @return the mapped {@link Unit} or <code>null</code> if the given uomAttribute is empty or <code>null</code>, or
     *         no appropriate mapping could be found.
     * @throws XMLParsingException
     */
    protected Unit parseUnitOfMeasure( OMElement elementContainingUOMAttribute )
                            throws XMLParsingException {
        if ( elementContainingUOMAttribute == null ) {
            return null;
        }

        String uomAttribute = elementContainingUOMAttribute.getAttributeValue( new QName( "uom" ) );
        if ( uomAttribute == null || "".equals( uomAttribute.trim() ) ) {
            return null;
        }
        Unit result = getCachedIdentifiable( Unit.class, uomAttribute );
        if ( result == null ) {
            result = createUnitFromString( uomAttribute );
            if ( result == null ) {
                LOG.debug( "Trying to resolve the uri: " + uomAttribute + " from a gml:value/@uom node" );
                OMElement unitElement = null;
                try {
                    unitElement = getResolver().getURIAsType( uomAttribute );
                } catch ( IOException e ) {
                    // return null
                }
                if ( unitElement == null ) {
                    LOG.error( "Although an uri was determined, the XLinkresolver was not able to retrieve a valid XML-OM representation of the uom-uri. Error while resolving the following uom uri: "
                               + uomAttribute + "." );
                } else {
                    CRSIdentifiable unitID = parseIdentifiedObject( unitElement );
                    if ( unitID != null ) {
                        CRSCodeType[] codes = unitID.getCodes();
                        for ( int i = 0; i < codes.length && result == null; ++i ) {
                            result = createUnitFromString( codes[i].getOriginal() );
                        }
                    }

                }
            }
        }

        return addIdToCache( result, false );
    }

    /**
     * convenience method to retrieve a given required element either by resolving a optional xlink or by evaluating the
     * required element denoted by the xpath.
     * 
     * @param propertyElement
     *            to resolve an xlink from.
     * @param alternativeXPath
     *            denoting a path to the required node starting from the given propertyElement.
     * @return the dom-element in the xlink:href attribute of the given propertyElement or the required alternativeXPath
     *         element.
     * @throws XMLParsingException
     *             if the given propertyElement is <code>null</code> or the resulting xml dom-tree could not be parsed
     *             or the alternative xpath does not result in an Element.
     * @throws IOException
     *             if the xlink could not be properly resolved
     */
    protected OMElement getRequiredXlinkedElement( OMElement propertyElement, String alternativeXPath )
                            throws XMLParsingException, IOException {
        if ( propertyElement == null ) {
            throw new XMLParsingException( adapter, null, "The propertyElement may not be null" );
        }
        OMElement child = retrieveAndResolveXLink( propertyElement );
        if ( child == null ) {
            child = adapter.getRequiredElement( propertyElement, new XPath( alternativeXPath, nsContext ) );
        }
        return child;
    }

    /**
     * Retrieves the xlink:href of the given rootElement and use the XLinkResolver to resolve the xlink if it was given.
     * 
     * @param rootElement
     *            to retrieve and resolve
     * @return the resolved xlink:href attribute as an XML-OM element or <code>null</code> if the xlink could not be
     *         resolved (or was not given) or the rootElement is null.
     * @throws IOException
     */
    protected OMElement retrieveAndResolveXLink( OMElement rootElement )
                            throws IOException {
        if ( rootElement == null ) {
            LOG.debug( "Rootelement is null no xlink to retrieve." );
            return null;
        }
        String xlink = retrieveXLink( rootElement );
        OMElement result = null;
        if ( null != xlink && !"".equals( xlink ) ) {
            LOG.debug( "Found an xlink: " + xlink );
            // The conversion is given by a link, so resolve it.
            result = getResolver().getURIAsType( xlink );
            if ( result == null ) {
                LOG.error( "Although an xlink was given, the XLInkresolver was not able to retrieve a valid XML-OM representation of the uri it denotes. Error while resolving the following uri from rootElement: "
                           + rootElement.getLocalName() + ": " + xlink + ". No further evaluation can be done." );
            }
        } else {
            LOG.debug( "No xlink found in: " + rootElement.getLocalName() );
        }
        return result;
    }

    /**
     * Find an xlink:href attribute and return it's value, if not found, the empty String will be returned.
     * 
     * @param rootElement
     *            to get the attribute from.
     * @return the trimmed xlink:href attribute value or the empty String if not found or the rootElement is null;
     */
    protected String retrieveXLink( OMElement rootElement ) {
        if ( rootElement == null ) {
            return "";
        }
        // return rootElement.getAttribute( new QName( CommonNamespaces.XLNNS, "href" )
        // ).getNamespace().getNamespaceURI().trim();
        return rootElement.getAttributeValue( new QName( CommonNamespaces.XLNNS, "href" ) );
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return getResolver().getTransformation( sourceCRS, targetCRS );
    }

    @Override
    public CRSIdentifiable getIdentifiable( CRSCodeType id )
                            throws CRSConfigurationException {
        CRSIdentifiable result = getCachedIdentifiable( id );
        if ( result == null ) {
            OMElement idRes = null;
            try {
                idRes = getResolver().getURIAsType( id.getOriginal() );
            } catch ( IOException e ) {
                LOG.debug( "Exception occurred: " + e.getLocalizedMessage(), e );
            }
            if ( idRes != null ) {
                String localName = idRes.getLocalName();
                if ( localName != null ) {
                    try {
                        if ( "Transformation".equals( localName ) ) {
                            result = parseGMLTransformation( idRes, null, null );
                        } else if ( "Conversion".equalsIgnoreCase( localName ) ) {
                            result = parseProjection( idRes, null );
                        } else {
                            // try coordinatesystem
                            result = parseCoordinateSystem( idRes );
                        }
                    } catch ( XMLParsingException e ) {
                        LOG.debug( "Could not get an identifiable for id: " + id.getOriginal() + " because: "
                                   + e.getLocalizedMessage(), e );
                    } catch ( IOException e ) {
                        LOG.debug( "Could not get an identifiable for id: " + id.getOriginal() + " because: "
                                   + e.getLocalizedMessage(), e );
                    }

                }
            }
        }
        return result;
    }
}
