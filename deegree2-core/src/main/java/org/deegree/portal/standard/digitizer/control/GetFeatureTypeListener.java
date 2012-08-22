//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.portal.standard.digitizer.control;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.deegree.datatypes.Types;
import org.deegree.enterprise.control.ajax.AbstractListener;
import org.deegree.enterprise.control.ajax.ResponseHandler;
import org.deegree.enterprise.control.ajax.WebEvent;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.HttpUtils;
import org.deegree.framework.util.StringTools;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.GMLSchema;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcwebservices.OWSUtils;
import org.deegree.ogcwebservices.wfs.capabilities.WFSCapabilities;
import org.deegree.ogcwebservices.wfs.operation.DescribeFeatureType;
import org.deegree.portal.Constants;
import org.deegree.portal.context.DataService;
import org.deegree.portal.context.Layer;
import org.deegree.portal.context.ViewContext;
import org.deegree.portal.standard.digitizer.model.FeatureTypeDescription;
import org.deegree.portal.standard.digitizer.model.FeatureTypeProperty;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:name@deegree.org">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class GetFeatureTypeListener extends AbstractListener {

    private static final ILogger LOG = LoggerFactory.getLogger( GetFeatureTypeListener.class );

    private static String baseRequest = "SERVICE=WFS&VERSION=1.1.0&REQUEST=DescribeFeatureType&TYPENAME=";

    @Override
    public void actionPerformed( WebEvent event, ResponseHandler responseHandler )
                            throws IOException {
        String layerName = (String) event.getParameter().get( "name" );
        String url = (String) event.getParameter().get( "url" );

        HttpSession session = event.getSession();
        ViewContext vc = (ViewContext) session.getAttribute( Constants.CURRENTMAPCONTEXT );
        Layer layer = vc.getLayerList().getLayer( layerName, url );

        DataService ds = layer.getExtension().getDataService();

        FeatureTypeDescription desc = null;
        try {
            if ( ds.getServer().getService().toLowerCase().indexOf( "wfs" ) > -1 ) {
                desc = handleWFS( layer, ds );                
            } else if ( ds.getServer().getService().toLowerCase().indexOf( "file" ) > -1 ) {
                // TODO
            } else if ( ds.getServer().getService().toLowerCase().indexOf( "database" ) > -1 ) {
                // TODO
            }
        } catch ( Exception e ) {
            handleException( responseHandler, e );
            return;
        }

        String charEnc = getRequest().getCharacterEncoding();
        if ( charEnc == null ) {
            charEnc = Charset.defaultCharset().displayName();
        }
        responseHandler.setContentType( "application/json; charset=" + charEnc );
        responseHandler.writeAndClose( false, desc );
    }

    private FeatureTypeDescription handleWFS( Layer layer, DataService ds )
                            throws Exception {
        String url;
        
        String featureTypeName = ds.getFeatureType();
        String[] tmp = StringTools.toArray( featureTypeName, "{}", false );
        String req = baseRequest + "app" + tmp[1] + "&NAMESPACE=xmlns(app=" + tmp[0] + ")";

        WFSCapabilities capa = (WFSCapabilities) layer.getExtension().getDataService().getServer().getCapabilities();        
        url = OWSUtils.getHTTPGetOperationURL( capa, DescribeFeatureType.class ).toExternalForm();
        url = HttpUtils.normalizeURL( url );
        InputStream is = null;
        try {
            is = HttpUtils.performHttpGet( url, req, timeout, null, null, null ).getResponseBodyAsStream();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new Exception( "can not load GML schema from: " + url );
        }
        
        GMLSchema schema = null;
        try {
            GMLSchemaDocument xsd = new GMLSchemaDocument();
            xsd.load( is, url );
            schema = xsd.parseGMLSchema();
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new Exception( "can not parse GML schema from: " + url );
        }
        FeatureType featureType = schema.getFeatureTypes()[0];
        PropertyType[] pts = featureType.getProperties();
        List<FeatureTypeProperty> ftps = new ArrayList<FeatureTypeProperty>( pts.length );
        FeatureTypeDescription desc = new FeatureTypeDescription();        
        try {
            for ( PropertyType propertyType : pts ) {
                if ( propertyType.getType() != Types.GEOMETRY && propertyType.getType() != Types.FEATURE ) {
                    FeatureTypeProperty ftp = new FeatureTypeProperty();
                    ftp.setName( propertyType.getName().getLocalName() );
                    ftp.setNamespace( propertyType.getName().getNamespace().toASCIIString() );
                    ftp.setType( Types.getTypeNameForSQLTypeCode( propertyType.getType() ) );
                    ftp.setRepeatable( propertyType.getMaxOccurs() > 1 );
                    ftp.setOptional( propertyType.getMinOccurs() == 0 );
                    ftps.add( ftp );
                } else if ( propertyType.getType() == Types.GEOMETRY ) {
                    desc.setGeomPropertyName( propertyType.getName().getLocalName() );
                    desc.setGeomPropertyNamespace( propertyType.getName().getNamespace().toASCIIString()  );
                }
            }
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            throw new Exception( "can not create featureType from GML schema: " + url );
        }        
        desc.setWfsURL( ds.getServer().getOnlineResource().toExternalForm() );
        desc.setName( featureType.getName().getLocalName() );
        desc.setNamespace( featureType.getName().getNamespace().toASCIIString() );
        desc.setProperties( ftps.toArray( new FeatureTypeProperty[ftps.size()] ) );
        desc.setSourceType( "OGC:WFS" );
        return desc;
    }

}
