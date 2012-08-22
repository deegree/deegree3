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
package org.deegree.graphics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.io.JDBCConnection;
import org.deegree.io.oraclegeoraster.GeoRasterDescription;
import org.deegree.model.coverage.grid.Format;
import org.deegree.model.coverage.grid.GridCoverage;
import org.deegree.model.coverage.grid.GridCoverageExchange;
import org.deegree.model.coverage.grid.GridCoverageReader;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.model.spatialschema.GeometryFactory;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.wcs.configuration.Directory;
import org.deegree.ogcwebservices.wcs.configuration.DirectoryResolution;
import org.deegree.ogcwebservices.wcs.configuration.Extension;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.configuration.FileResolution;
import org.deegree.ogcwebservices.wcs.configuration.OracleGeoRasterResolution;
import org.deegree.ogcwebservices.wcs.configuration.Resolution;
import org.deegree.ogcwebservices.wcs.configuration.Shape;
import org.deegree.ogcwebservices.wcs.configuration.ShapeResolution;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;

/**
 *
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class LazyRasterLayer extends AbstractLayer {

    private ILogger LOG = LoggerFactory.getLogger( LazyRasterLayer.class );

    private Extension resource;

    private CoverageOffering coverageOffering;

    /**
     *
     * @param name
     * @param coverageOffering
     * @throws Exception
     */
    public LazyRasterLayer( String name, CoverageOffering coverageOffering ) throws Exception {
        super( name );
        this.coverageOffering = coverageOffering;
        resource = coverageOffering.getExtension();
    }

    /**
     *
     * @param name
     * @param crs
     * @param coverageOffering
     * @throws Exception
     */
    public LazyRasterLayer( String name, CoordinateSystem crs, CoverageOffering coverageOffering ) throws Exception {
        super( name, crs );
        this.coverageOffering = coverageOffering;
        if ( coverageOffering != null ) {
            resource = coverageOffering.getExtension();
        }
    }

    /**
     * @param crs
     * @throws Exception
     *
     */
    public void setCoordinatesSystem( CoordinateSystem crs )
                            throws Exception {
        // not supported yet
    }

    /*
     * (non-Javadoc)
     *
     * @see org.deegree.graphics.AbstractLayer#getBoundingBox()
     */
    @Override
    public Envelope getBoundingBox() {
        return coverageOffering.getDomainSet().getSpatialDomain().getEnvelops()[0];
    }

    /**
     *
     * @param envelope
     * @param resolution
     * @return grid coverage for envelope and resolution
     * @throws IOException
     * @throws InvalidParameterValueException
     */
    public GridCoverage getRaster( Envelope envelope, double resolution )
                            throws InvalidParameterValueException, IOException {

        Resolution[] resolutions = resource.getResolutions( resolution );

        String nativeCRS = coverageOffering.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        CoordinateSystem crs;
        try {
            crs = CRSFactory.create( nativeCRS );
        } catch ( UnknownCRSException e ) {
            throw new InvalidParameterValueException( e );
        }
        envelope = GeometryFactory.createEnvelope( envelope.getMin(), envelope.getMax(), crs );

        GridCoverageReader reader = null;
        if ( resolutions[0] instanceof FileResolution ) {
            reader = getFileReader( resolutions, envelope );
        } else if ( resolutions[0] instanceof ShapeResolution ) {
            reader = getShapeReader( resolutions, envelope );
        } else if ( resolutions[0] instanceof DirectoryResolution ) {
            reader = getDirectoryReader( resolutions, envelope );
        } else if ( resolutions[0] instanceof OracleGeoRasterResolution ) {
            reader = getOracleGeoRasterReader( resolutions, envelope );
        } else {
            throw new InvalidParameterValueException( "not supported coverage resolution: "
                                                      + resolutions[0].getClass().getName() );
        }
        return reader.read( null );
    }

    /**
     *
     * @param resolutions DirectoryResolutions
     * @param env
     * @return grid coverage reader for resolutions
     * @throws IOException
     * @throws InvalidParameterValueException
     */
    private GridCoverageReader getDirectoryReader( Resolution[] resolutions, Envelope env )
                            throws IOException, InvalidParameterValueException {

        LOG.logInfo( "reading coverage from directories" );

        Directory[] dirs = ( (DirectoryResolution) resolutions[0] ).getDirectories( env );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( coverageOffering.getSupportedFormats().getNativeFormat() );

        return gce.getReader( dirs, coverageOffering, env, format );
    }

    /**
     *
     * @param resolutions FileResolutions
     * @param env
     * @return grid coverage reader for resolutions
     * @throws IOException
     * @throws InvalidParameterValueException
     */
    private GridCoverageReader getFileReader( Resolution[] resolutions, Envelope env )
                            throws IOException, InvalidParameterValueException {

        LOG.logInfo( "reading coverage from files" );

        File[] files = ( (FileResolution) resolutions[0] ).getFiles();
        List<File> list = new ArrayList<File>();
        for ( int i = 0; i < files.length; i++ ) {
            Envelope fileEnv = files[i].getEnvelope();
            if ( fileEnv.intersects( env ) ) {
                list.add( files[i] );
            }
        }
        files = list.toArray( new File[list.size()] );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( coverageOffering.getSupportedFormats().getNativeFormat() );

        return gce.getReader( files, coverageOffering, env, format );
    }

    /**
     *
     * @param resolutions OracleGeoRasterResolution
     * @param env
     * @return grid coverage reader for resolutions
     * @throws InvalidParameterValueException
     * @throws IOException
     */
    private GridCoverageReader getOracleGeoRasterReader( Resolution[] resolutions, Envelope env )
                            throws InvalidParameterValueException, IOException {

        LOG.logInfo( "reading coverage from oracle georaster" );

        JDBCConnection jdbc = ( (OracleGeoRasterResolution) resolutions[0] ).getJDBCConnection();
        String table = ( (OracleGeoRasterResolution) resolutions[0] ).getTable();
        String rdtTable = ( (OracleGeoRasterResolution) resolutions[0] ).getRdtTable();
        String column = ( (OracleGeoRasterResolution) resolutions[0] ).getColumn();
        String identification = ( (OracleGeoRasterResolution) resolutions[0] ).getIdentification();
        int level = ( (OracleGeoRasterResolution) resolutions[0] ).getLevel();
        GeoRasterDescription grd = new GeoRasterDescription( jdbc, table, rdtTable, column, identification, level );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( coverageOffering.getSupportedFormats().getNativeFormat() );

        return gce.getReader( grd, coverageOffering, env, format );

    }

    /**
     *
     * @param resolutions ShapeResolution
     * @param env
     * @return grid coverage reader for resolutions
     * @throws IOException
     * @throws InvalidParameterValueException
     */
    private GridCoverageReader getShapeReader( Resolution[] resolutions, Envelope env )
                            throws IOException, InvalidParameterValueException {

        LOG.logInfo( "reading coverage from shapes" );

        Shape shape = ( (ShapeResolution) resolutions[0] ).getShape();

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( coverageOffering.getSupportedFormats().getNativeFormat() );
        return gce.getReader( shape, coverageOffering, env, format );

    }

}
