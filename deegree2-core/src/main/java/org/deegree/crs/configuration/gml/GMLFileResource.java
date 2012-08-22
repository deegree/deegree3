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

package org.deegree.crs.configuration.gml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deegree.crs.configuration.resources.XMLFileResource;
import org.deegree.crs.coordinatesystems.CompoundCRS;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.coordinatesystems.GeographicCRS;
import org.deegree.crs.coordinatesystems.ProjectedCRS;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.transformations.Transformation;
import org.deegree.crs.transformations.coordinate.CRSTransformation;
import org.deegree.crs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.crs.transformations.helmert.Helmert;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.i18n.Messages;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;

/**
 * The <code>GMLFileResource</code> provides easy access to a gml3.2. dictionary file, which can be used together with
 * the {@link GMLCRSProvider}.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class GMLFileResource extends XMLFileResource implements GMLResource {

    /**
     *
     */
    private static final long serialVersionUID = -4389365894942107300L;

    private static ILogger LOG = LoggerFactory.getLogger( GMLFileResource.class );

    private static final String PRE = CommonNamespaces.GML3_2_PREFIX + ":";

    private static final String ID_XPATH = "//" + PRE + "dictionaryEntry/*[" + PRE + "identifier='";

    private static final String OR_CONTAINS_NAMES = " or " + PRE + "name='";

    private static final String TRANSFORM_XPATH = "/" + PRE + "Dictionary/" + PRE + "dictionaryEntry/" + PRE
                                                  + "Transformation";

    private List<Element> transformations;

    private Map<CoordinateSystem, Helmert> cachedWGS84Transformations;

    /**
     * @param provider
     * @param properties
     */
    public GMLFileResource( GMLCRSProvider provider, Properties properties ) {
        super( provider, properties, "Dictionary", CommonNamespaces.GML3_2_NS.toASCIIString() );
        try {
            transformations = XMLTools.getElements( getRootElement(), TRANSFORM_XPATH, nsContext );

        } catch ( XMLParsingException e ) {
            LOG.logError( e.getLocalizedMessage(), e );
        }
        cachedWGS84Transformations = new HashMap<CoordinateSystem, Helmert>();
    }

    public Helmert getWGS84Transformation( GeographicCRS sourceCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        if ( LOG.isDebug() ) {
            LOG.logDebug( "Searching for wgs84 transformation for given sourceCRS: "
                          + Arrays.toString( sourceCRS.getIdentifiers() ) );
        }
        Helmert result = cachedWGS84Transformations.get( sourceCRS );
        if ( result == null ) {
            Transformation parsedTransformation = getTransformation( sourceCRS, null );
            if ( parsedTransformation instanceof Helmert ) {
                LOG.logDebug( "Found an helmert transformation for sourceCRS: "
                              + Arrays.toString( sourceCRS.getIdentifiers() ) );
                result = (Helmert) parsedTransformation;
            } else {
                if ( parsedTransformation instanceof CRSTransformation ) {
                    CoordinateSystem target = ( (CRSTransformation) parsedTransformation ).getTargetCRS();
                    GeographicCRS t = null;
                    if ( LOG.isDebug() ) {
                        LOG.logDebug( "Found crstransformation for sourceCRS: "
                                      + Arrays.toString( sourceCRS.getIdentifiers() ) + " and targetCRS: "
                                      + Arrays.toString( target.getIdentifiers() )
                                      + " will now use the targetCRS to find a Helmert transformation." );
                    }
                    if ( target.getType() == CoordinateSystem.COMPOUND_CRS ) {
                        if ( ( (CompoundCRS) target ).getUnderlyingCRS().getType() == CoordinateSystem.PROJECTED_CRS ) {
                            t = ( (ProjectedCRS) ( (CompoundCRS) target ).getUnderlyingCRS() ).getGeographicCRS();
                        } else if ( ( (CompoundCRS) target ).getUnderlyingCRS().getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                            t = (GeographicCRS) target;
                        } else {
                            LOG.logWarning( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                        }
                    } else if ( target.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        t = ( (ProjectedCRS) target ).getGeographicCRS();
                    } else if ( target.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                        t = (GeographicCRS) target;
                    } else {
                        LOG.logWarning( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                    }
                    if ( t != null ) {
                        if ( LOG.isDebug() ) {
                            LOG.logDebug( "Trying to resolve target to find a wgs84transformation for the 'targetCRS': "
                                          + Arrays.toString( t.getIdentifiers() ) );
                        }
                        result = getWGS84Transformation( t );
                    }
                } else {
                    LOG.logWarning( "The transformation is not an instance of CRSTransformation nor a Helmert, ignoring it." );
                }
            }
        }

        if ( result != null ) {
            if ( LOG.isDebug() ) {
                LOG.logDebug( "For the given crs: " + sourceCRS.getIdentifier()
                              + " following helmert transformation was found:\n" + result );

            }

            cachedWGS84Transformations.put( sourceCRS, result );
        } else {
            LOG.logInfo( "No helmert transformation found for the given crs: " + sourceCRS.getIdentifier() );
        }
        return result;
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        List<Element> toBeRemoved = new ArrayList<Element>( transformations.size() );
        List<String> sourceIDs = Arrays.asList( sourceCRS.getIdentifiers() );
        List<String> targetIDs = null;
        if ( targetCRS != null ) {
            targetIDs = Arrays.asList( targetCRS.getIdentifiers() );
        } else {
            targetIDs = new ArrayList<String>();
        }
        Transformation result = null;
        for ( int i = 0; i < transformations.size() && result == null; ++i ) {
            Element transElem = transformations.get( i );
            if ( transElem != null ) {
                try {
                    Element sourceCRSProp = XMLTools.getRequiredElement( transElem, PRE + "sourceCRS", nsContext );
                    String transformSourceID = null;
                    String transformTargetID = null;
                    if ( sourceCRSProp != null ) {
                        transformSourceID = sourceCRSProp.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(),
                                                                          "href" );
                        if ( "".equals( transformSourceID ) ) {
                            transformSourceID = XMLTools.getRequiredNodeAsString( sourceCRSProp, "*[1]/" + PRE
                                                                                                 + "identifier",
                                                                                  nsContext );
                        }
                    }
                    if ( targetCRS != null ) {
                        Element targetCRSProp = XMLTools.getRequiredElement( transElem, PRE + "targetCRS", nsContext );
                        if ( targetCRSProp != null ) {

                            transformTargetID = targetCRSProp.getAttributeNS( CommonNamespaces.XLNNS.toASCIIString(),
                                                                              "href" );
                            if ( "".equals( transformTargetID ) ) {
                                transformTargetID = XMLTools.getRequiredNodeAsString( targetCRSProp, "*[1]/" + PRE
                                                                                                     + "identifier",
                                                                                      nsContext );
                            }
                        }
                    }

                    if ( sourceIDs.contains( transformSourceID ) ) {
                        result = getProvider().parseTransformation( transElem );
                        if ( targetCRS == null ) {
                            // Trying to find a helmert transformation
                            LOG.logDebug( "Resolving a possible transformation." );
                            if ( result != null && !( result instanceof Helmert ) ) {
                                result = getTransformation( result.getTargetCRS(), null );
                            }
                        } else {
                            if ( !targetIDs.contains( transformTargetID ) ) {
                                LOG.logDebug( "Found a transformation with gml:id: "
                                              + transElem.getAttributeNS( CommonNamespaces.GML3_2_NS.toASCIIString(),
                                                                          "id" )
                                              + ", but the target does not match the source crs, trying to build transformation chain." );
                                Transformation second = getTransformation( result.getTargetCRS(), targetCRS );
                                if ( second != null ) {
                                    result = new ConcatenatedTransform( result, second );
                                } else {
                                    LOG.logDebug( "The transformation with gml:id: "
                                                  + transElem.getAttributeNS(
                                                                              CommonNamespaces.GML3_2_NS.toASCIIString(),
                                                                              "id" )
                                                  + " is not the start of transformation chain, discarding it. " );
                                    result = null;
                                }
                            }
                        }
                    }

                } catch ( XMLParsingException e ) {
                    toBeRemoved.add( transElem );
                    LOG.logWarning( "No source CRS id could be found in this transformation(gml:id): "
                                    + transElem.getAttributeNS( CommonNamespaces.GML3_2_NS.toASCIIString(), "id" )
                                    + " this is not correct, removing transformation from cache." );
                    LOG.logWarning( e.getMessage() );
                }
            }
            if ( toBeRemoved.size() > 0 ) {
                transformations.removeAll( toBeRemoved );
            }
        }
        return result;
    }

    public Element getURIAsType( String uri )
                            throws IOException {
        Element result = null;
        try {
            result = XMLTools.getElement( getRootElement(), ID_XPATH + uri + "'" + OR_CONTAINS_NAMES + uri + "']",
                                          nsContext );
        } catch ( XMLParsingException e ) {
            LOG.logError( e );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.crs.configuration.gml.GMLResource#getAvailableCRSIds()
     */
    public List<String> getAvailableCRSIds() {
        List<Element> crsIDs = new LinkedList<Element>();
        try {
            crsIDs.addAll( XMLTools.getElements( getRootElement(), ID_XPATH, nsContext ) );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_GET_ALL_ELEMENT_IDS", e.getMessage() ),
                                                 e );
        }

        List<String> result = new ArrayList<String>();
        for ( Element crs : crsIDs ) {
            if ( crs != null ) {
                result.add( XMLTools.getStringValue( crs ) );
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.crs.configuration.gml.GMLResource#getAvailableCRSIds()
     */
    public List<String[]> getSortedAvailableCRSIds() {
        List<Element> crsIDs = new LinkedList<Element>();
        try {
            crsIDs.addAll( XMLTools.getElements( getRootElement(), ID_XPATH, nsContext ) );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_GET_ALL_ELEMENT_IDS", e.getMessage() ),
                                                 e );
        }

        List<String[]> result = new ArrayList<String[]>();
        for ( Element crs : crsIDs ) {
            if ( crs != null ) {
                result.add( new String[] { XMLTools.getStringValue( crs ) } );
            }
        }
        return result;
    }

    public List<CoordinateSystem> getAvailableCRSs() {
        throw new UnsupportedOperationException( "Retrieval of all crs is currently not supported." );
    }

    public List<Transformation> getTransformations() {
        throw new UnsupportedOperationException( "Retrieval of all transformations is currently not supported." );
    }
}
