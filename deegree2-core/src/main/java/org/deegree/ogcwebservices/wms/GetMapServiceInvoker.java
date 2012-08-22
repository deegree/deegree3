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
package org.deegree.ogcwebservices.wms;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.deegree.datatypes.QualifiedName;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.graphics.sld.AbstractLayer;
import org.deegree.graphics.sld.FeatureTypeConstraint;
import org.deegree.graphics.sld.LayerFeatureConstraints;
import org.deegree.graphics.sld.UserLayer;
import org.deegree.i18n.Messages;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.GeometryPropertyType;
import org.deegree.ogcbase.ElementStep;
import org.deegree.ogcbase.PropertyPath;
import org.deegree.ogcbase.PropertyPathStep;
import org.deegree.ogcwebservices.InconsistentRequestException;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.ogcwebservices.wfs.operation.FeatureTypeDescription;
import org.deegree.ogcwebservices.wms.capabilities.Layer;
import org.deegree.ogcwebservices.wms.configuration.AbstractDataSource;
import org.xml.sax.SAXException;

/**
 * 
 * 
 * 
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version 1.0. $Revision$, $Date$
 * 
 * @since 2.0
 */
public abstract class GetMapServiceInvoker {

    private static final ILogger LOG = LoggerFactory.getLogger( GetMapServiceInvoker.class );

    private static Map<QualifiedName, List<PropertyPath>> geoProps;

    static {
        if ( geoProps == null ) {
            geoProps = new HashMap<QualifiedName, List<PropertyPath>>();
        }
    }

    /**
     * default get map handler
     */
    protected final DefaultGetMapHandler handler;

    /**
     * scale denominator
     */
    protected double scaleDen = 0;

    /**
     * 
     * @param handler
     * @param scaleDen
     */
    public GetMapServiceInvoker( DefaultGetMapHandler handler, double scaleDen ) {
        this.handler = handler;
        this.scaleDen = scaleDen;
    }

    /**
     * the method accesses the GML schema of the feature types being part of the passed FeatureTypeConstraints and
     * extracts the geometry property definition. The names of the geometry properties will be added as PropertyPath's
     * to passed list
     * 
     * @param layer
     * @param ftc
     * @param pp
     * @return the list of property paths
     * @throws OGCWebServiceException
     * @throws SAXException
     * @throws IOException
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    protected List<PropertyPath> findGeoProperties( AbstractLayer layer, FeatureTypeConstraint[] ftc,
                                                    List<PropertyPath> pp )
                            throws OGCWebServiceException, IOException, SAXException, XMLSchemaException,
                            XMLParsingException, UnknownCRSException {

        GMLSchemaDocument doc = null;
        for ( int i = 0; i < ftc.length; i++ ) {
            if ( geoProps.get( ftc[i].getFeatureTypeName() ) == null ) {
                if ( layer instanceof UserLayer && ( (UserLayer) layer ).getRemoteOWS() != null ) {
                    doc = getSchemaFromRemoteWFS( (UserLayer) layer, ftc[i] );
                } else {
                    doc = getSchemaFromLocalWFS( layer, ftc[i] );
                }
                GMLSchema schema = doc.parseGMLSchema();
                FeatureType ft = schema.getFeatureType( ftc[i].getFeatureTypeName() );
                if ( ft == null ) {
                    throw new OGCWebServiceException( Messages.get( "WFS_FEATURE_TYPE_UNKNOWN",
                                                                    ftc[i].getFeatureTypeName() ) );
                }
                GeometryPropertyType[] gpt = ft.getGeometryProperties();

                List<PropertyPath> tmp = new ArrayList<PropertyPath>( gpt.length );
                for ( int j = 0; j < gpt.length; j++ ) {
                    try {
                        String pre = ftc[i].getFeatureTypeName().getPrefix();
                        QualifiedName qn = new QualifiedName( pre, gpt[j].getName().getLocalName(),
                                                              gpt[j].getName().getNamespace() );
                        PropertyPathStep step = new ElementStep( qn );
                        List<PropertyPathStep> steps = new ArrayList<PropertyPathStep>();
                        steps.add( step );
                        PropertyPath prop = new PropertyPath( steps );
                        pp.add( prop );
                        tmp.add( prop );
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }
                }
                geoProps.put( ftc[i].getFeatureTypeName(), tmp );
            } else {
                List<PropertyPath> tmp = geoProps.get( ftc[i].getFeatureTypeName() );
                pp.addAll( tmp );
            }

        }

        return pp;
    }

    /**
     * accesses the GML schema assigned to the feature type contained within the passed FeatureTypeConstraint from a
     * remote WFS by performing a DescribeFeatureType request
     * 
     * @param layer
     * @param ftc
     * @return the schema document
     * @throws InconsistentRequestException
     * @throws InvalidParameterValueException
     * @throws OGCWebServiceException
     */
    private GMLSchemaDocument getSchemaFromLocalWFS( AbstractLayer layer, FeatureTypeConstraint ftc )
                            throws InconsistentRequestException, InvalidParameterValueException, OGCWebServiceException {
        StringBuffer sb = new StringBuffer( 300 );
        sb.append( "SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=" );
        QualifiedName qn = ftc.getFeatureTypeName();
        sb.append( qn.getPrefixedName() );
        sb.append( "&NAMESPACE=xmlns(" ).append( qn.getPrefix() ).append( '=' );
        sb.append( qn.getNamespace().toASCIIString() ).append( ')' );
        LOG.logDebug( "DescribeFeaturetType for UserLayer: ", sb );
        DescribeFeatureType dft = DescribeFeatureType.create( "rt", sb.toString() );
        OGCWebService wfs = getResponsibleService( layer );
        FeatureTypeDescription ftd = (FeatureTypeDescription) wfs.doService( dft );
        GMLSchemaDocument doc = new GMLSchemaDocument();
        doc.setRootElement( ftd.getFeatureTypeSchema().getRootElement() );
        return doc;
    }

    /**
     * accesses the GML schema assigned to the feature type contained within the passed FeatureTypeConstraint from a
     * local WFS by performing a DescribeFeatureType request
     * 
     * @param ftc
     * @return the schema document
     * @throws IOException
     * @throws SAXException
     * @throws MalformedURLException
     */
    private GMLSchemaDocument getSchemaFromRemoteWFS( UserLayer layer, FeatureTypeConstraint ftc )
                            throws IOException, SAXException, MalformedURLException {
        URL url = layer.getRemoteOWS().getOnlineResource();
        StringBuffer sb = new StringBuffer( 300 );
        sb.append( OWSUtils.validateHTTPGetBaseURL( url.toExternalForm() ) );
        sb.append( "SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=" );
        QualifiedName qn = ftc.getFeatureTypeName();
        sb.append( qn.getPrefixedName() );
        if ( qn.getNamespace() != null ) {
            sb.append( "&NAMESPACE=xmlns(" ).append( qn.getPrefix() ).append( '=' );
            sb.append( qn.getNamespace().toASCIIString() ).append( ')' );
        }
        LOG.logDebug( "DescribeFeaturetType for UserLayer: ", sb );
        GMLSchemaDocument doc = new GMLSchemaDocument();
        doc.load( new URL( sb.toString() ) );
        return doc;
    }

    /**
     * @param layer
     * @return the service instance
     * @throws OGCWebServiceException
     *             if no service could be found
     */
    protected OGCWebService getResponsibleService( AbstractLayer layer )
                            throws OGCWebServiceException {

        LayerFeatureConstraints lfc = layer.getLayerFeatureConstraints();
        FeatureTypeConstraint[] ftc = lfc.getFeatureTypeConstraint();
        Layer root = handler.getConfiguration().getLayer();
        OGCWebService wfs = findService( root, ftc[0].getFeatureTypeName() );
        if ( wfs == null ) {
            throw new OGCWebServiceException( this.getClass().getName(), "feature type: " + ftc[0].getFeatureTypeName()
                                                                         + " is not served by this WMS/WFS" );
        }

        return wfs;
    }

    /**
     * searches/findes the WFService that is resposible for handling the feature types of the current request. If no
     * WFService instance can be found <code>null</code> will be returned to indicated that the current feature type is
     * not served by the internal WFS of a WMS
     * 
     * @param layer
     * @param featureType
     * @return the service instance or null
     * @throws OGCWebServiceException
     */
    private OGCWebService findService( Layer layer, QualifiedName featureType )
                            throws OGCWebServiceException {
        Layer[] layers = layer.getLayer();
        for ( int i = 0; i < layers.length; i++ ) {
            AbstractDataSource[] ad = layers[i].getDataSource();
            if ( ad != null ) {
                for ( int j = 0; j < ad.length; j++ ) {
                    if ( ad[j].getName().equals( featureType ) ) {

                        return ad[j].getOGCWebService();
                    }
                }
            }
            // recursion
            OGCWebService wfs = findService( layers[i], featureType );
            if ( wfs != null ) {
                return wfs;
            }
        }

        return null;
    }

    /**
     * extracts all used namespace definitions from a set of PropertyPath's
     * 
     * @param pp
     * @return the used namespace definitions from a set of PropertyPath's
     */
    protected Map<String, URI> extractNameSpaceDef( List<PropertyPath> pp ) {
        Map<String, URI> map = new HashMap<String, URI>();
        for ( int i = 0; i < pp.size(); i++ ) {
            NamespaceContext nsc = pp.get( i ).getNamespaceContext();
            map.putAll( nsc.getNamespaceMap() );
        }
        return map;
    }

}
