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

package org.deegree.tile.tilematrixset;

import static org.deegree.commons.utils.MapUtils.DEFAULT_PIXEL_SIZE;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.metadata.SpatialMetadata;
import org.deegree.tile.TileMatrix;
import org.deegree.tile.TileMatrixSet;
import org.deegree.tile.tilematrixset.jaxb.TileMatrixSetConfig;

/**
 * <code>DefaultTileMatrixSetProvider</code>
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision: 31882 $, $Date: 2011-09-15 02:05:04 +0200 (Thu, 15 Sep 2011) $
 */

public class DefaultTileMatrixSetProvider implements TileMatrixSetProvider {

    private static final URL SCHEMA_URL = DefaultTileMatrixSetProvider.class.getResource( "/META-INF/schemas/datasource/tile/tilematrixset/3.2.0/tilematrixset.xsd" );

    private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        this.workspace = workspace;
    }

    @Override
    public TileMatrixSet create( URL configUrl )
                            throws ResourceInitException {
        try {
            TileMatrixSetConfig cfg = (TileMatrixSetConfig) JAXBUtils.unmarshall( "org.deegree.tile.tilematrixset.jaxb",
                                                                                  SCHEMA_URL, configUrl, workspace );

            ICRS crs = CRSManager.lookup( cfg.getCRS() );
            List<TileMatrix> matrices = new ArrayList<TileMatrix>();
            for ( org.deegree.tile.tilematrixset.jaxb.TileMatrixSetConfig.TileMatrix tm : cfg.getTileMatrix() ) {
                double res = tm.getScaleDenominator() * DEFAULT_PIXEL_SIZE;
                double minx = tm.getTopLeftCorner().get( 0 );
                double maxy = tm.getTopLeftCorner().get( 1 );
                double maxx = tm.getTileWidth().longValue() * tm.getMatrixWidth().longValue() * res + minx;
                double miny = maxy - tm.getTileHeight().longValue() * tm.getMatrixHeight().longValue() * res;
                Envelope env = new GeometryFactory().createEnvelope( minx, miny, maxx, maxy, crs );
                SpatialMetadata smd = new SpatialMetadata( env, Collections.singletonList( crs ) );
                TileMatrix md = new TileMatrix( tm.getIdentifier(), smd, tm.getTileWidth(), tm.getTileHeight(), res,
                                                tm.getMatrixWidth(), tm.getMatrixHeight() );
                matrices.add( md );
            }

            String identifier = new File( configUrl.getPath() ).getName();
            String wknScaleSet = cfg.getWellKnownScaleSet();
            return new TileMatrixSet( identifier, wknScaleSet, matrices, matrices.get( 0 ).getSpatialMetadata() );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not create tile matrix set. Reason: " + e.getLocalizedMessage(), e );
        }
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/tile/tilematrixset";
    }

    @Override
    public URL getConfigSchema() {
        return SCHEMA_URL;
    }

}
