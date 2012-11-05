//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.tile.persistence.geotiff;

import static org.deegree.commons.xml.jaxb.JAXBUtils.unmarshall;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.tile.TileDataSet;
import org.deegree.tile.persistence.GenericTileStore;
import org.deegree.tile.persistence.TileStoreProvider;
import org.deegree.tile.persistence.geotiff.jaxb.GeoTIFFTileStoreJAXB;

/**
 * The <code>GeoTIFFTileStoreProvider</code> provides a <code>TileMatrixSet</code> out of a GeoTIFF file (tiled
 * BIGTIFF).
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class GeoTIFFTileStoreProvider implements TileStoreProvider {

    // private static final Logger LOG = getLogger( GeoTIFFTileStoreProvider.class );

    private static final URL SCHEMA = GeoTIFFTileStoreProvider.class.getResource( "/META-INF/schemas/datasource/tile/geotiff/3.2.0/geotiff.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public GenericTileStore create( URL configUrl )
                            throws ResourceInitException {
        try {
            GeoTIFFTileStoreJAXB cfg = (GeoTIFFTileStoreJAXB) unmarshall( "org.deegree.tile.persistence.geotiff.jaxb",
                                                                          SCHEMA, configUrl, workspace );

            GeoTiffTileDataSetMapBuilder builder = new GeoTiffTileDataSetMapBuilder( workspace, configUrl, cfg );

            Map<String, TileDataSet> map = builder.buildTileDataSetMap();
            return new GenericTileStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Unable to create tile store.", e );
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/tile/geotiff";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA;
    }

    @Override
    public List<File> getTileStoreDependencies( File config ) {
        return Collections.<File> emptyList();
    }

}
