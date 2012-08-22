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

package org.deegree.ogcwebservices.wms.dataaccess;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.framework.util.IDGenerator;
import org.deegree.framework.util.StringTools;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureFactory;
import org.deegree.model.feature.FeatureProperty;
import org.deegree.model.feature.schema.FeatureType;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.model.spatialschema.Geometry;
import org.deegree.model.spatialschema.WKTAdapter;
import org.deegree.ogcwebservices.wfs.operation.FeatureResult;
import org.deegree.ogcwebservices.wms.dataaccess.ExternalVectorDataAccess;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfo;
import org.deegree.ogcwebservices.wms.operation.GetFeatureInfoResult;
import org.deegree.ogcwebservices.wms.operation.GetLegendGraphic;
import org.deegree.ogcwebservices.wms.operation.GetMap;

/**
 * expects configuration like:
 * <pre>
 * # name of the feature type that will be created (important for style definition)
 * FeatureType={http://www.deegree.org/app}:WKTType
 * # name of the vendor specific GetMap parameter that shall be evaluated
 * WKT=WKT
 * </pre>
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 *
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 *
 */
public class WKTMarker implements ExternalVectorDataAccess {

    private Properties props;

    private FeatureType ft;

    private BufferedImage legend = new BufferedImage( 10, 10, BufferedImage.TYPE_INT_RGB );

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalVectorDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetMap)
     */
    public FeatureResult perform( GetMap getMap )
                            throws Exception {
        String wkt = getMap.getVendorSpecificParameter( props.getProperty( "WKT" ) );
        FeatureCollection fc = FeatureFactory.createFeatureCollection( "UUID_" + UUID.randomUUID().toString(), 5 );
        if ( wkt != null ) {
            CoordinateSystem crs = CRSFactory.create( getMap.getSrs() );
            String[] s = StringTools.toArray( wkt, ";", false );
            for ( String string : s ) {
                Geometry geom = WKTAdapter.wrap( string, crs );
                FeatureProperty[] fp = new FeatureProperty[3];
                fp[0] = FeatureFactory.createFeatureProperty( ft.getPropertyName( 0 ),
                                                              (int) IDGenerator.getInstance().generateUniqueID() );
                fp[1] = FeatureFactory.createFeatureProperty( ft.getPropertyName( 1 ), wkt );
                fp[2] = FeatureFactory.createFeatureProperty( ft.getPropertyName( 2 ), geom );
                fc.add( FeatureFactory.createFeature( "UUID_" + UUID.randomUUID(), ft, fp ) );
            }

        }
        return new FeatureResult( getMap, fc );
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetFeatureInfo)
     */
    public GetFeatureInfoResult perform( GetFeatureInfo gfi ) {
        return new GetFeatureInfoResult( gfi, "" );
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#perform(org.deegree.ogcwebservices.wms.operation
     * .GetLegendGraphic)
     */
    public BufferedImage perform( GetLegendGraphic glg ) {
        return legend;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.ogcwebservices.wms.dataaccess.ExternalDataAccess#setConfigurationFile(java.net.URL)
     */
    public void setConfigurationFile( URL url )
                            throws IOException {
        props = new Properties();
        InputStream is = url.openStream();
        props.load( is );
        is.close();

        String ftName = props.getProperty( "FeatureType" );
        QualifiedName qn = new QualifiedName( ftName );
        PropertyType[] pt = new PropertyType[3];
        pt[0] = FeatureFactory.createSimplePropertyType( new QualifiedName( "ID", qn.getNamespace() ), Types.INTEGER,
                                                         false );
        pt[1] = FeatureFactory.createSimplePropertyType( new QualifiedName( "WKT", qn.getNamespace() ), Types.VARCHAR,
                                                         false );
        pt[2] = FeatureFactory.createSimplePropertyType( new QualifiedName( "GEOM", qn.getNamespace() ),
                                                         Types.GEOMETRY, false );
        ft = FeatureFactory.createFeatureType( qn, false, pt );
    }
}
