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

package org.deegree.model.crs.configuration.gml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.model.crs.CRSCodeType;
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

    private static final String TRANSFORM_XPATH = "/" + PRE + "Dictionary/" + PRE + "dictionaryEntry/" + PRE
                                                  + "Transformation";

    private List<OMElement> transformations;

    private Map<CoordinateSystem, Helmert> cachedWGS84Transformations;
    
    private XMLAdapter adapter;

    /**
     * @param provider
     * @param properties
     */
    public GMLFileResource( GMLCRSProvider provider, Properties properties ) {
        super( provider, properties, "Dictionary", CommonNamespaces.GML3_2_NS );
        try {
            transformations = getElements( getRootElement(), new XPath( TRANSFORM_XPATH, nsContext ) );
        } catch ( XMLParsingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }
        cachedWGS84Transformations = new HashMap<CoordinateSystem, Helmert>();
        adapter = new XMLAdapter();
    }

    public Helmert getWGS84Transformation( GeographicCRS sourceCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Searching for wgs84 transformation for given sourceCRS: "
                       + Arrays.toString( sourceCRS.getCodes() ) );
        }
        Helmert result = cachedWGS84Transformations.get( sourceCRS );
        if ( result == null ) {
            Transformation parsedTransformation = getTransformation( sourceCRS, null );
            if ( parsedTransformation instanceof Helmert ) {
                LOG.debug( "Found an helmert transformation for sourceCRS: "
                           + Arrays.toString( sourceCRS.getCodes() ) );
                result = (Helmert) parsedTransformation;
            } else {
                if ( parsedTransformation instanceof CRSTransformation ) {
                    CoordinateSystem target = ( (CRSTransformation) parsedTransformation ).getTargetCRS();
                    GeographicCRS t = null;
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Found crstransformation for sourceCRS: "
                                   + Arrays.toString( sourceCRS.getCodes() ) + " and targetCRS: "
                                   + Arrays.toString( target.getCodes() )
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
                                       + Arrays.toString( t.getCodes() ) );
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
                LOG.debug( "For the given crs: " + sourceCRS.getCode()
                           + " following helmert transformation was found:\n" + result );

            }

            cachedWGS84Transformations.put( sourceCRS, result );
        } else {
            LOG.info( "No helmert transformation found for the given crs: " + sourceCRS.getCode() );
        }
        return result;
    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        if ( sourceCRS == null ) {
            return null;
        }
        List<OMElement> toBeRemoved = new ArrayList<OMElement>( transformations.size() );
        List<CRSCodeType> sourceCodes = Arrays.asList( sourceCRS.getCodes() );
        List<String> sourceIDs = new ArrayList<String>();
        for ( CRSCodeType sourceCode : sourceCodes )
            sourceIDs.add( sourceCode.getEquivalentString() );
        List<String> targetIDs = null;
        if ( targetCRS != null ) {
            targetIDs = new ArrayList<String>();
            List<CRSCodeType> targetCodes = Arrays.asList( targetCRS.getCodes() );
            for ( CRSCodeType targetCode : targetCodes ) 
                targetIDs.add( targetCode.getEquivalentString() );
        } else {
            targetIDs = new ArrayList<String>();
        }
        Transformation result = null;
        for ( int i = 0; i < transformations.size() && result == null; ++i ) {
            OMElement transElem = transformations.get( i );
            if ( transElem != null ) {
                try {
                	OMElement sourceCRSProp = getRequiredElement( transElem, new XPath( PRE + "sourceCRS", nsContext  ) );
                	
                    String transformSourceID = null;
                    String transformTargetID = null;
                    if ( sourceCRSProp != null ) {
                    	transformSourceID = sourceCRSProp.getAttribute( new QName( CommonNamespaces.XLNNS, "href" ) ).getNamespace().getNamespaceURI();
                        
                        if ( "".equals( transformSourceID ) ) {
                        	transformSourceID = adapter.getRequiredNodeAsString( sourceCRSProp, new XPath( "*[1]/" + PRE
                                    + "identifier", nsContext ) );
                        }
                    }
                    if ( targetCRS != null ) {
                        OMElement targetCRSProp = adapter.getRequiredElement( transElem, new XPath( PRE + "targetCRS", nsContext ) );
                        if ( targetCRSProp != null ) {

                        	transformTargetID = targetCRSProp.getAttribute( new QName( CommonNamespaces.XLNNS, "href" ) ).getNamespace().getNamespaceURI();
                            if ( "".equals( transformTargetID ) ) {
                            	transformTargetID = adapter.getRequiredNodeAsString( targetCRSProp, new XPath( "*[1]/" + PRE
                                        + "identifier", nsContext ) );
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
                                        + transElem.getAttribute( new QName( CommonNamespaces.GML3_2_NS, "id" ) ).getNamespace().getNamespaceURI()
                                        + ", but the target does not match the source crs, trying to build transformation chain." );
                                Transformation second = getTransformation( result.getTargetCRS(), targetCRS );
                                if ( second != null ) {
                                    result = new ConcatenatedTransform( result, second );
                                } else {
                                	LOG.debug( "The transformation with gml:id: "
                                            + transElem.getAttribute( new QName( CommonNamespaces.GML3_2_NS, "id" ) ).getNamespace().getNamespaceURI()
                                            + " is not the start of transformation chain, discarding it. " );
                                    result = null;
                                }
                            }
                        }
                    }

                } catch ( XMLParsingException e ) {
                    toBeRemoved.add( transElem );
                    LOG.warn( "No source CRS id could be found in this transformation(gml:id): "
                            + transElem.getAttribute( new QName( CommonNamespaces.GML3_2_NS, "id" ) ).getNamespace().getNamespaceURI()
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

    public OMElement getURIAsType( String uri )
                            throws IOException {
        OMElement result = null;
        try {
        	result = adapter.getElement( getRootElement(), new XPath( "//gml3_2:dictionaryEntry/gml3_2:*[gml3_2:identifier='" + uri + "']" +
        			" | //gml3_2:dictionaryEntry/gml3_2:*[gml3_2:name='" + uri + "']", nsContext ) );        		
        } catch ( XMLParsingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }
        return result;
    }
}
