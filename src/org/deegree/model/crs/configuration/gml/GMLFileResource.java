//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2007 by:
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

package org.deegree.model.crs.configuration.gml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLTools;
import org.deegree.model.crs.configuration.resources.XMLFileResource;
import org.deegree.model.crs.coordinatesystems.CompoundCRS;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.transformations.Transformation;
import org.deegree.model.crs.transformations.coordinate.CRSTransformation;
import org.deegree.model.crs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.model.crs.transformations.helmert.Helmert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class GMLFileResource extends XMLFileResource {

    /**
     * 
     */
    private static final long serialVersionUID = -4389365894942107300L;

    private static Logger LOG = LoggerFactory.getLogger( GMLFileResource.class );

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
        super( provider, properties, "Dictionary", CommonNamespaces.GML3_2_NS );
        try {
            transformations = XMLTools.getElements( getRootElement(), TRANSFORM_XPATH, nsContext );

        } catch ( XMLParsingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }
        cachedWGS84Transformations = new HashMap<CoordinateSystem, Helmert>();
    }

    public Helmert getWGS84Transformation( GeographicCRS sourceCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searching for wgs84 transformation for given sourceCRS: "
                       + Arrays.toString( sourceCRS.getIdentifiers() ) );
        }
        Helmert result = cachedWGS84Transformations.get( sourceCRS );
        if ( result == null ) {
            Transformation parsedTransformation = getTransformation( sourceCRS, null );
            if ( parsedTransformation instanceof Helmert ) {
                LOG.debug( "Found an helmert transformation for sourceCRS: "
                           + Arrays.toString( sourceCRS.getIdentifiers() ) );
                result = (Helmert) parsedTransformation;
            } else {
                if ( parsedTransformation instanceof CRSTransformation ) {
                    CoordinateSystem target = ( (CRSTransformation) parsedTransformation ).getTargetCRS();
                    GeographicCRS t = null;
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Found crstransformation for sourceCRS: "
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
                            LOG.warn( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                        }
                    } else if ( target.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        t = ( (ProjectedCRS) target ).getGeographicCRS();
                    } else if ( target.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                        t = (GeographicCRS) target;
                    } else {
                        LOG.warn( "Wgs84 Transformation lookup is currently only supported for GeographicCRS-chains." );
                    }
                    if ( t != null ) {
                        if ( LOG.isDebugEnabled() ) {
                            LOG.debug( "Trying to resolve target to find a wgs84transformation for the 'targetCRS': "
                                       + Arrays.toString( t.getIdentifiers() ) );
                        }
                        result = getWGS84Transformation( t );
                    }
                } else {
                    LOG.warn( "The transformation is not an instance of CRSTransformation nor a Helmert, ignoring it." );
                }
            }
        }

        if ( result != null ) {
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "For the given crs: " + sourceCRS.getIdentifier()
                           + " following helmert transformation was found:\n" + result );

            }

            cachedWGS84Transformations.put( sourceCRS, result );
        } else {
            LOG.info( "No helmert transformation found for the given crs: " + sourceCRS.getIdentifier() );
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
                        transformSourceID = sourceCRSProp.getAttributeNS( CommonNamespaces.XLNNS, "href" );
                        if ( "".equals( transformSourceID ) ) {
                            transformSourceID = XMLTools.getRequiredNodeAsString( sourceCRSProp, "*[1]/" + PRE
                                                                                                 + "identifier",
                                                                                  nsContext );
                        }
                    }
                    if ( targetCRS != null ) {
                        Element targetCRSProp = XMLTools.getRequiredElement( transElem, PRE + "targetCRS", nsContext );
                        if ( targetCRSProp != null ) {

                            transformTargetID = targetCRSProp.getAttributeNS( CommonNamespaces.XLNNS, "href" );
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
                            LOG.debug( "Resolving a possible transformation." );
                            if ( result != null && !( result instanceof Helmert ) ) {
                                result = getTransformation( result.getTargetCRS(), null );
                            }
                        } else {
                            if ( !targetIDs.contains( transformTargetID ) ) {
                                LOG.debug( "Found a transformation with gml:id: "
                                           + transElem.getAttributeNS( CommonNamespaces.GML3_2_NS, "id" )
                                           + ", but the target does not match the source crs, trying to build transformation chain." );
                                Transformation second = getTransformation( result.getTargetCRS(), targetCRS );
                                if ( second != null ) {
                                    result = new ConcatenatedTransform( result, second );
                                } else {
                                    LOG.debug( "The transformation with gml:id: "
                                               + transElem.getAttributeNS( CommonNamespaces.GML3_2_NS, "id" )
                                               + " is not the start of transformation chain, discarding it. " );
                                    result = null;
                                }
                            }
                        }
                    }

                } catch ( XMLParsingException e ) {
                    toBeRemoved.add( transElem );
                    LOG.warn( "No source CRS id could be found in this transformation(gml:id): "
                              + transElem.getAttributeNS( CommonNamespaces.GML3_2_NS, "id" )
                              + " this is not correct, removing transformation from cache." );
                    LOG.warn( e.getMessage() );
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
            LOG.error( e.getLocalizedMessage(), e );
        }
        return result;
    }
}
