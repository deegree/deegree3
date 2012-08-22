// $HeadURL$
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
package org.deegree.ogcwebservices.wcs;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.deegree.crs.exceptions.CRSException;
import org.deegree.datatypes.parameter.GeneralParameterValueIm;
import org.deegree.datatypes.parameter.OperationParameterIm;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.StringTools;
import org.deegree.io.JDBCConnection;
import org.deegree.io.oraclegeoraster.GeoRasterDescription;
import org.deegree.model.coverage.Coverage;
import org.deegree.model.coverage.grid.AbstractGridCoverage;
import org.deegree.model.coverage.grid.DatabaseIndexedGCMetadata;
import org.deegree.model.coverage.grid.Format;
import org.deegree.model.coverage.grid.GridCoverageExchange;
import org.deegree.model.coverage.grid.GridCoverageReader;
import org.deegree.model.coverage.grid.ImageGridCoverage;
import org.deegree.model.crs.CRSFactory;
import org.deegree.model.crs.CRSTransformationException;
import org.deegree.model.crs.CoordinateSystem;
import org.deegree.model.crs.GeoTransformer;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.spatialschema.Envelope;
import org.deegree.ogcwebservices.InvalidParameterValueException;
import org.deegree.ogcwebservices.OGCWebService;
import org.deegree.ogcwebservices.OGCWebServiceException;
import org.deegree.ogcwebservices.OGCWebServiceRequest;
import org.deegree.ogcwebservices.getcapabilities.OGCCapabilities;
import org.deegree.ogcwebservices.wcs.configuration.DatabaseResolution;
import org.deegree.ogcwebservices.wcs.configuration.Directory;
import org.deegree.ogcwebservices.wcs.configuration.DirectoryResolution;
import org.deegree.ogcwebservices.wcs.configuration.Extension;
import org.deegree.ogcwebservices.wcs.configuration.File;
import org.deegree.ogcwebservices.wcs.configuration.FileResolution;
import org.deegree.ogcwebservices.wcs.configuration.OracleGeoRasterResolution;
import org.deegree.ogcwebservices.wcs.configuration.Resolution;
import org.deegree.ogcwebservices.wcs.configuration.ScriptResolution;
import org.deegree.ogcwebservices.wcs.configuration.Shape;
import org.deegree.ogcwebservices.wcs.configuration.ShapeResolution;
import org.deegree.ogcwebservices.wcs.configuration.WCSConfiguration;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageDescription;
import org.deegree.ogcwebservices.wcs.describecoverage.CoverageOffering;
import org.deegree.ogcwebservices.wcs.describecoverage.DescribeCoverage;
import org.deegree.ogcwebservices.wcs.describecoverage.InvalidCoverageDescriptionExcpetion;
import org.deegree.ogcwebservices.wcs.getcapabilities.ContentMetadata;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSGetCapabilities;
import org.deegree.ogcwebservices.wcs.getcapabilities.WCSRequestValidator;
import org.deegree.ogcwebservices.wcs.getcoverage.GetCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.ResultCoverage;
import org.deegree.ogcwebservices.wcs.getcoverage.SpatialSubset;
import org.xml.sax.SAXException;

/**
 * @version $Revision$
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @author last edited by: $Author$
 *
 * @version 1.0. $Revision$, $Date$
 *
 * @since 2.0
 */

public class WCService implements OGCWebService {

    private static final ILogger LOG = LoggerFactory.getLogger( WCService.class );

    private int nor = 5;

    private int degree = 3;

    /**
     *
     */
    private WCSConfiguration configuration = null;

    /**
     * creates a WCService from a configuration
     *
     * @param configuration
     */
    public WCService( WCSConfiguration configuration ) {
        this.configuration = configuration;
        URL url = WCService.class.getResource( "crstransform.properties" );
        Properties props = new Properties();
        try {
            InputStream is = url.openStream();
            props.load( is );
            is.close();
            nor = Integer.parseInt( props.getProperty( "number_of_reference_points" ) );
            degree = Integer.parseInt( props.getProperty( "degree" ) );
        } catch ( Exception e ) {
            LOG.logError( e.getMessage(), e );
            LOG.logInfo( "could not load definiton for crs transformation parameters, use default values" );
        }
    }

    /**
     * returns the capabilities of the WCS
     *
     * @return capabilities of the WCS
     */
    public OGCCapabilities getCapabilities() {
        return configuration;
    }

    /**
     * @param request
     * @return a CoverageDescription fitting the request
     * @throws OGCWebServiceException
     *             if an exception occurs in the process of creating the description.
     */
    private CoverageDescription describeCoverage( DescribeCoverage request )
                            throws OGCWebServiceException {

        WCSRequestValidator.validate( configuration, request );
        CoverageOffering[] co = null;
        try {
            co = getCoverageOfferings( request );
        } catch ( IOException ioe ) {
            LOG.logError( StringTools.stackTraceToString( ioe ) );
            throw new OGCWebServiceException( ioe.getMessage() );
        } catch ( SAXException saxe ) {
            LOG.logError( StringTools.stackTraceToString( saxe ) );
            throw new OGCWebServiceException( saxe.getMessage() );
        }
        CoverageDescription cd = new CoverageDescription( co, request.getVersion() );
        return cd;
    }

    /**
     * @param request
     * @return a given Coverage for the request
     * @throws OGCWebServiceException
     *             if any kind of exception occurs
     */
    private Coverage getCoverage( GetCoverage request )
                            throws OGCWebServiceException {

        WCSRequestValidator.validate( configuration, request );
        Coverage cov = null;
        if ( request.getOutput().getFormat().getCode().equals( "GML" ) ) {
            CoverageOffering co;
            try {
                co = getCoverageOffering( request );
            } catch ( InvalidCoverageDescriptionExcpetion e ) {
                LOG.logError( "CoverageDescription is not valid", e );
                throw new OGCWebServiceException( getClass().getName(), "CoverageDescription is not valid: "
                                                                        + e.getMessage() );
            } catch ( IOException e ) {
                LOG.logError( "could not read CoverageDescription", e );
                throw new OGCWebServiceException( getClass().getName(), "could not read CoverageDescription: "
                                                                        + e.getMessage() );
            } catch ( SAXException e ) {
                LOG.logError( "could not parse CoverageDescription", e );
                throw new OGCWebServiceException( getClass().getName(), "could not parse CoverageDescription: "
                                                                        + e.getMessage() );
            }
            Envelope env = request.getDomainSubset().getSpatialSubset().getEnvelope();
            BufferedImage bi = new BufferedImage( 2, 2, BufferedImage.TYPE_INT_ARGB );
            cov = new ImageGridCoverage( co, env, bi );
        } else {
            cov = readCoverage( request );
        }

        return cov;
    }

    /**
     * method for event based request procrssing
     *
     * @param request
     *            object containing the request.
     * @return depending on the request one of, {@link WCSGetCapabilities}, {@link GetCoverage} or
     *         {@link DescribeCoverage}
     */
    public Object doService( OGCWebServiceRequest request )
                            throws OGCWebServiceException {

        Object response = null;
        if ( request instanceof WCSGetCapabilities ) {
            WCSRequestValidator.validate( configuration, request );
            response = getCapabilities();
        } else if ( request instanceof GetCoverage ) {
            Coverage cov = getCoverage( (GetCoverage) request );
            response = new ResultCoverage( cov, cov.getClass(), ( (GetCoverage) request ).getOutput().getFormat(),
                                           (GetCoverage) request );
        } else if ( request instanceof DescribeCoverage ) {
            response = describeCoverage( (DescribeCoverage) request );
        }
        return response;
    }

    /**
     * returns the <code>CoverageOffering</code> s according to the coverages names contained in
     * the passed request. If the request doesn't contain one or more named coverage
     * <code>CoverageOffering</code> s for all coverages known by the WCS will be returned.
     *
     * @param request
     *            DescribeCoverage request
     * @return the configured coverings
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    private CoverageOffering[] getCoverageOfferings( DescribeCoverage request )
                            throws IOException, SAXException, InvalidCoverageDescriptionExcpetion {

        String[] coverages = request.getCoverages();
        CoverageOffering[] co = null;
        ContentMetadata cm = configuration.getContentMetadata();
        if ( coverages.length == 0 ) {
            // get descriptions of all coverages
            CoverageOfferingBrief[] cob = cm.getCoverageOfferingBrief();
            co = new CoverageOffering[cob.length];
            for ( int i = 0; i < cob.length; i++ ) {
                URL url = cob[i].getConfiguration();
                CoverageDescription cd = CoverageDescription.createCoverageDescription( url );
                co[i] = cd.getCoverageOffering( cob[i].getName() );
            }
        } else {
            // get descriptions of all requested coverages
            co = new CoverageOffering[coverages.length];
            for ( int i = 0; i < coverages.length; i++ ) {
                CoverageOfferingBrief cob = cm.getCoverageOfferingBrief( coverages[i] );
                URL url = cob.getConfiguration();
                CoverageDescription cd = CoverageDescription.createCoverageDescription( url );
                co[i] = cd.getCoverageOffering( cob.getName() );
            }
        }

        return co;
    }

    /**
     * The method reads and returns the coverage described by the passed request.
     *
     * @param request
     * @return a Coverage read from the given resolution
     * @throws InvalidCoverageDescriptionExcpetion
     */
    private Coverage readCoverage( GetCoverage request )
                            throws InvalidCoverageDescriptionExcpetion, InvalidParameterValueException,
                            OGCWebServiceException {

        Coverage result = null;

        try {
            CoverageOffering co = getCoverageOffering( request );

            Resolution[] resolutions = getResolutions( co, request );
            if ( resolutions == null || resolutions.length == 0 ) {
                throw new InvalidParameterValueException(
                                                          "No data source defined the requested combination of spatial resolution and ranges" );
            }
            GridCoverageReader reader = null;

            LOG.logDebug( "getting responsible GridCoverageReader" );
            if ( resolutions[0] instanceof FileResolution ) {
                reader = getFileReader( resolutions, co, request );
            } else if ( resolutions[0] instanceof ShapeResolution ) {
                reader = getShapeReader( resolutions, co, request );
            } else if ( resolutions[0] instanceof DirectoryResolution ) {
                reader = getDirectoryReader( resolutions, co, request );
            } else if ( resolutions[0] instanceof OracleGeoRasterResolution ) {
                reader = getOracleGeoRasterReader( resolutions, co, request );
            } else if ( resolutions[0] instanceof DatabaseResolution ) {
                reader = getDatabaseRasterReader( resolutions, co, request );
            } else if ( resolutions[0] instanceof ScriptResolution ) {
                reader = getScriptBasedFileReader( resolutions, co, request );
            }

            LOG.logDebug( "resolution reader: " + resolutions[0] );
            LOG.logDebug( "found reader: " + reader.getClass() );
            List<GeneralParameterValueIm> list = new ArrayList<GeneralParameterValueIm>( 20 );

            Envelope gridSize = (Envelope) request.getDomainSubset().getSpatialSubset().getGrid();
            Envelope targetEnv = request.getDomainSubset().getSpatialSubset().getEnvelope();

            int width = (int) ( gridSize.getWidth() + 1 );
            int height = (int) ( gridSize.getHeight() + 1 );
            OperationParameterIm op = new OperationParameterIm( "width", null, new Integer( width ) );
            list.add( new GeneralParameterValueIm( op ) );
            op = new OperationParameterIm( "height", null, new Integer( height ) );
            list.add( new GeneralParameterValueIm( op ) );
            GeneralParameterValueIm[] gpvs = new GeneralParameterValueIm[list.size()];
            result = reader.read( list.toArray( gpvs ) );
            if ( result == null ) {
                throw new InvalidCoverageDescriptionExcpetion(
                                                               "Couldn't read a coverage for the requested resolution and/or area" );
            }
            LOG.logDebug( "found result: " + result );

            // transform Coverage into another CRS if required
            String crs = request.getOutput().getCrs().getCode();
            if ( crs == null ) {
                crs = request.getDomainSubset().getRequestSRS().getCode();
            }
            if ( !crs.equalsIgnoreCase( co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0] ) ) {
                LOG.logDebug( "transforming coverage to " + crs );
                GeoTransformer gt = new GeoTransformer( crs );

                result = gt.transform( (AbstractGridCoverage) result, targetEnv, width, height, nor, degree, null );
            }

        } catch ( IOException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( SAXException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( CRSTransformationException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( UnknownCRSException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        } catch ( InterruptedException e ) {
            LOG.logError( e.getMessage(), e );
            throw new OGCWebServiceException( e.getMessage() );
        }
        return result;
    }

    private GridCoverageReader getScriptBasedFileReader( Resolution[] resolutions, CoverageOffering co,
                                                         GetCoverage request )
                            throws UnknownCRSException, CRSTransformationException, InvalidParameterValueException,
                            IOException, InterruptedException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        CoordinateSystem crs = CRSFactory.create( nativeCRS );
        // calculates the envelope to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        ScriptResolution sr = (ScriptResolution) resolutions[0];
        String script = sr.getScript();
        // create random filename
        String tempFile = UUID.randomUUID().toString();

        LOG.logDebug( "script: ", script );
        String outFile = null;
        try {
            Envelope gridSize = (Envelope) request.getDomainSubset().getSpatialSubset().getGrid();
            Class<?> clzz = Class.forName( script );
            Class<?>[] prmClass = new Class[] { String.class, String.class, String.class, String.class, String.class,
                                            Integer.class, Integer.class, String.class };
            Object[] prm = new Object[8];
            prm[0] = "" + envelope.getMin().getX() + ',' + envelope.getMin().getY() + ',' + envelope.getMax().getX()
                     + ',' + envelope.getMax().getY();
            prm[1] = sr.getStorageLocation() + java.io.File.separator + tempFile;
            prm[2] = sr.getResultFormat();
            prm[3] = Double.toString( sr.getMinScale() );
            prm[4] = Double.toString( sr.getMaxScale() );
            prm[5] = (int) gridSize.getWidth();
            prm[6] = (int) gridSize.getHeight();
            prm[7] = request.getVendorSpecificParameter( "TIMESTAMP" );
            ExternalDataAccess eda = (ExternalDataAccess) clzz.getConstructor( prmClass ).newInstance( prm );
            outFile = eda.perform();
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new InterruptedException( e.getMessage() );
        }

        File file = new File( crs, outFile, envelope );
        File[] files = new File[] { file };
        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );
        return gce.getReader( files, co, envelope, format );
    }

    /**
     * returns the <code>CoverageOffering</code> describing the access to the data sources behind
     * the requested coverage
     *
     * @param request
     *            GetCoverage request
     * @return the Coverage Offering fitting the request
     * @throws IOException
     * @throws SAXException
     * @throws InvalidCoverageDescriptionExcpetion
     */
    private CoverageOffering getCoverageOffering( GetCoverage request )
                            throws IOException, SAXException, InvalidCoverageDescriptionExcpetion {

        ContentMetadata cm = configuration.getContentMetadata();
        CoverageOfferingBrief cob = cm.getCoverageOfferingBrief( request.getSourceCoverage() );
        URL url = cob.getConfiguration();
        CoverageDescription cd = CoverageDescription.createCoverageDescription( url );
        return cd.getCoverageOffering( request.getSourceCoverage() );
    }

    /**
     * returns the <code>Resolution</code> s matching the scale, region and range parameters of
     * the passed request
     *
     * @param co
     * @param request
     * @return the <code>Resolution</code> s matching the scale, region and range parameters of
     *         the passed request
     * @throws CRSException
     * @throws CRSTransformationException
     */
    private Resolution[] getResolutions( CoverageOffering co, GetCoverage request )
                            throws UnknownCRSException, CRSTransformationException {

        Extension extension = co.getExtension();
        return extension.getResolutions( calcSpatialResolution( co, request ) );
    }

    /**
     * calculates the spatial resolution of the coverage described by a GetCoverage request
     *
     * @param co
     * @param request
     * @return
     * @throws UnknownCRSException
     * @throws CRSTransformationException
     */
    private double calcSpatialResolution( CoverageOffering co, GetCoverage request )
                            throws UnknownCRSException, CRSTransformationException {

        SpatialSubset sps = request.getDomainSubset().getSpatialSubset();
        // determine resolution of the requested coverage
        Envelope env = calculateRequestEnvelope( request, co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0] );
        Envelope grid = (Envelope) sps.getGrid();
        double qx = env.getWidth() / grid.getWidth();
        double qy = env.getHeight() / grid.getHeight();
        double reso = qx;
        // if x- and y-direction has different resolution in the GetCoverage
        // request use the finest
        if ( qy < qx ) {
            reso = qy;
        }
        return reso;
    }

    /**
     * returns a <code>GridCoverageReader</code> for accessing the data source of the target
     * coverage of the passed GetCoverage request. The reader will be constructed from all
     * <code>File</code> s matching the filter conditions defined in the passed GeCoverage
     * request. <BR>
     * At the moment just the first field of the passed <code>Resolution</code> array will be
     * considered!
     *
     * @param resolutions
     *            <code>Resolution</code> to get a reader for
     * @param co
     *            description of the requested coverage
     * @param request
     * @return <code>GridCoverageReader</code>
     * @throws IOException
     * @throws UnknownCRSException
     * @throws CRSTransformationException
     */
    private GridCoverageReader getFileReader( Resolution[] resolutions, CoverageOffering co, GetCoverage request )
                            throws IOException, InvalidParameterValueException, UnknownCRSException,
                            CRSTransformationException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        // calculates the envevole to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        File[] files = ( (FileResolution) resolutions[0] ).getFiles();
        List<File> list = new ArrayList<File>();
        for ( int i = 0; i < files.length; i++ ) {
            Envelope fileEnv = files[i].getEnvelope();
            if ( fileEnv.intersects( envelope ) ) {
                list.add( files[i] );
            }
        }
        files = list.toArray( new File[list.size()] );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );
        return gce.getReader( files, co, envelope, format );
    }

    /**
     * returns a <code>GridCoverageReader</code> for accessing the data source of the target
     * coverage of the passed GetCoverage request. The reader will be constructed from all
     * <code>Shape</code> s matching the filter conditions defined in the passed GeCoverage
     * request. At least this should be just one! <BR>
     * At the moment just the first field of the passed <code>Resolution</code> array will be
     * considered!
     *
     * @param resolutions
     * @param co
     * @param request
     * @return a GridCoverageReader which is able to read shape files.
     * @throws IOException
     * @throws UnknownCRSException
     * @throws CRSTransformationException
     */
    private GridCoverageReader getShapeReader( Resolution[] resolutions, CoverageOffering co, GetCoverage request )
                            throws IOException, InvalidParameterValueException, UnknownCRSException,
                            CRSTransformationException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        // calculates the envevole to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        Shape shape = ( (ShapeResolution) resolutions[0] ).getShape();

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );
        return gce.getReader( shape, co, envelope, format );

    }

    /**
     * returns a <code>GridCoverageReader</code> for accessing the data source of the target
     * coverage of the passed GetCoverage request. The reader will be constructed from all
     * <code>Directory</code> s matching the filter conditions defined in the passed GeCoverage
     * request. At least this should be just one! <BR>
     * At the moment just the first field of the passed <code>Resolution</code> array will be
     * considered!
     *
     * @param resolutions
     * @param co
     * @param request
     * @return the GridCoverageReader which reads directories
     * @throws IOException
     * @throws UnknownCRSException
     * @throws CRSTransformationException
     */
    private GridCoverageReader getDirectoryReader( Resolution[] resolutions, CoverageOffering co, GetCoverage request )
                            throws IOException, InvalidParameterValueException, UnknownCRSException,
                            CRSTransformationException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        // calculates the envevole to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        Directory[] dirs = ( (DirectoryResolution) resolutions[0] ).getDirectories( envelope );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );

        GridCoverageReader reader = gce.getReader( dirs, co, envelope, format );

        return reader;
    }

    /**
     *
     * @param resolutions
     * @param co
     * @param request
     * @return
     * @throws CRSTransformationException
     * @throws UnknownCRSException
     * @throws IOException
     * @throws InvalidParameterValueException
     */
    private GridCoverageReader getDatabaseRasterReader( Resolution[] resolutions, CoverageOffering co,
                                                        GetCoverage request )
                            throws UnknownCRSException, CRSTransformationException, InvalidParameterValueException,
                            IOException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        // calculates the envevole to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        DatabaseResolution dr = (DatabaseResolution) resolutions[0];

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );
        double reso = calcSpatialResolution( co, request );
        DatabaseIndexedGCMetadata digcmd = new DatabaseIndexedGCMetadata( dr.getJDBCConnection(), (float) reso,
                                                                          dr.getTable(), dr.getRootDir(), false );

        return gce.getReader( digcmd, co, envelope, format );
    }

    /**
     * returns a <code>GridCoverageReader</code> for accessing the data source of the target
     * coverage of the passed GetCoverage request. The reader will be constructed from the JDBCV
     * connnection defined in the CoverageDescription extension.<BR>
     * At the moment just the first field of the passed <code>Resolution</code> array will be
     * considered!
     *
     * @param resolutions
     * @param co
     * @param request
     * @return a <code>GridCoverageReader</code>.
     * @throws InvalidParameterValueException
     * @throws IOException
     * @throws UnknownCRSException
     * @throws CRSTransformationException
     */
    private GridCoverageReader getOracleGeoRasterReader( Resolution[] resolutions, CoverageOffering co,
                                                         GetCoverage request )
                            throws InvalidParameterValueException, IOException, UnknownCRSException,
                            CRSTransformationException {

        String nativeCRS = co.getSupportedCRSs().getNativeSRSs()[0].getCodes()[0];
        // calculates the envevole to be used by the created GridCoverageReader
        Envelope envelope = calculateRequestEnvelope( request, nativeCRS );

        JDBCConnection jdbc = ( (OracleGeoRasterResolution) resolutions[0] ).getJDBCConnection();
        String table = ( (OracleGeoRasterResolution) resolutions[0] ).getTable();
        String rdtTable = ( (OracleGeoRasterResolution) resolutions[0] ).getRdtTable();
        String column = ( (OracleGeoRasterResolution) resolutions[0] ).getColumn();
        String identification = ( (OracleGeoRasterResolution) resolutions[0] ).getIdentification();
        int level = ( (OracleGeoRasterResolution) resolutions[0] ).getLevel();
        GeoRasterDescription grd = new GeoRasterDescription( jdbc, table, rdtTable, column, identification, level );

        GridCoverageExchange gce = new GridCoverageExchange( null );
        Format format = new Format( co.getSupportedFormats().getNativeFormat() );

        return gce.getReader( grd, co, envelope, format );

    }

    /**
     * According to WCS 1.0.0 the CRS of the GetCoverage request BBOX can be different to the
     * desired CRS of the resulting coverage. This method transforms the request CRS to the output
     * CRS if requiered. At the moment deegree WCS doesn't support transformation of grid coverages
     * so the output CRS will always be the native CRS of te data.
     *
     * @param request
     * @param nativeCrs
     * @return a boundingbox of the request
     * @throws CRSTransformationException
     * @throws UnknownCRSException
     */
    private Envelope calculateRequestEnvelope( GetCoverage request, String nativeCrs )
                            throws UnknownCRSException, CRSTransformationException {

        SpatialSubset spsu = request.getDomainSubset().getSpatialSubset();
        Envelope envelope = spsu.getEnvelope();

        String reqCrs = request.getDomainSubset().getRequestSRS().getCode();

        GeoTransformer gt = new GeoTransformer( nativeCrs );
        return gt.transform( envelope, CRSFactory.create( reqCrs ), true );

    }

}
