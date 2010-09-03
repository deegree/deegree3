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
package org.deegree.services.wcs;

import static org.deegree.services.controller.OGCFrontController.getServiceWorkspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deegree.commons.xml.XMLProcessingException;
import org.deegree.coverage.AbstractCoverage;
import org.deegree.coverage.rangeset.AxisSubset;
import org.deegree.coverage.rangeset.Interval;
import org.deegree.coverage.rangeset.RangeSet;
import org.deegree.coverage.rangeset.RangeSetBuilder;
import org.deegree.coverage.rangeset.SingleValue;
import org.deegree.coverage.rangeset.Interval.Closure;
import org.deegree.coverage.raster.AbstractRaster;
import org.deegree.coverage.raster.MultiResolutionRaster;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory;
import org.deegree.coverage.raster.data.container.RasterDataContainerFactory.LoadingPolicy;
import org.deegree.coverage.raster.interpolation.InterpolationType;
import org.deegree.coverage.raster.utils.RasterFactory;
import org.deegree.cs.CRS;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.utils.GeometryUtils;
import org.deegree.services.exception.ServiceInitException;
import org.deegree.services.jaxb.wcs.AxisValue;
import org.deegree.services.jaxb.wcs.Interpolation;
import org.deegree.services.jaxb.wcs.IntervalType;
import org.deegree.services.jaxb.wcs.RangeSetType;
import org.deegree.services.jaxb.wcs.ServiceConfiguration;
import org.deegree.services.jaxb.wcs.SupportOptions;
import org.deegree.services.jaxb.wcs.TypedType;
import org.deegree.services.jaxb.wcs.RangeSetType.AxisDescription;
import org.deegree.services.jaxb.wcs.ServiceConfiguration.Coverage;
import org.deegree.services.wcs.coverages.MultiResolutionCoverage;
import org.deegree.services.wcs.coverages.SimpleCoverage;
import org.deegree.services.wcs.coverages.WCSCoverage;
import org.deegree.services.wcs.model.CoverageOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class builds WCServices from the deegree WCS configuration files.
 * 
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */
public class WCServiceBuilder {

    private static final Logger LOG = LoggerFactory.getLogger( WCServiceBuilder.class );

    private final ServiceConfigurationXMLAdapter adapter;

    private WCService wcsService;

    private SupportOptions defaultOptions;

    /**
     * @param adapter
     */
    public WCServiceBuilder( ServiceConfigurationXMLAdapter adapter ) {
        this.adapter = adapter;
    }

    /**
     * @param conf
     * @return nothing
     * @throws FileNotFoundException
     * @throws XMLProcessingException
     */
    public static WCService createService( File conf )
                            throws XMLProcessingException, FileNotFoundException {
        ServiceConfigurationXMLAdapter adapt = new ServiceConfigurationXMLAdapter();
        adapt.load( new FileInputStream( conf ) );
        return new WCServiceBuilder( adapt ).buildService();
    }

    /**
     * @return a new WCService
     */
    public WCService buildService() {
        wcsService = new WCService();
        ServiceConfiguration wcsConf = adapter.parse();

        RasterDataContainerFactory.setDefaultLoadingPolicy( LoadingPolicy.CACHED );

        defaultOptions = wcsConf.getSupportOptions();
        if ( defaultOptions == null ) {
            defaultOptions = new SupportOptions();
        }
        // if the list of outputformats is not defined by the config, use all available formats.
        List<String> defaultFormats = defaultOptions.getOutputFormat();
        if ( defaultFormats.isEmpty() ) {
            defaultFormats.addAll( RasterFactory.getAllSupportedWritingFormats() );
            Collections.sort( defaultFormats );
        }

        for ( Coverage coverage : wcsConf.getCoverage() ) {
            try {
                wcsService.addCoverage( extractCoverage( coverage ) );
            } catch ( ServiceInitException ex ) {
                LOG.error( "unable to load coverage: {}", ex.getMessage(), ex );
            }
        }
        return wcsService;
    }

    private WCSCoverage extractCoverage( Coverage coverage )
                            throws ServiceInitException {
        String id = coverage.getCoverageStoreId();
        AbstractCoverage cov = getServiceWorkspace().getCoverageBuilderManager().get( id );
        if ( cov == null ) {
            throw new ServiceInitException( "No coverage store with id '" + id + "' is known." );
        }
        WCSCoverage result = null;
        try {
            if ( cov instanceof AbstractRaster ) {
                result = buildCoverage( coverage, (AbstractRaster) cov );
            } else if ( cov instanceof MultiResolutionRaster ) {
                result = buildCoverage( coverage, (MultiResolutionRaster) cov );
            }

            if ( result != null ) {
                // first add the rangesets,
                RangeSet configuredRS = getRangeSet( coverage.getRangeSet() );
                if ( configuredRS != null ) {
                    result.setRangeSet( configuredRS );
                }

                // create envelopes matching the different output crs's.
                Set<String> srs = result.getCoverageOptions().getCRSs();
                Envelope origEnv = result.getEnvelope();
                for ( String crs : srs ) {
                    try {
                        if ( !origEnv.getCoordinateSystem().equals( new CRS( crs ) ) ) {
                            Envelope env = GeometryUtils.createConvertedEnvelope( origEnv, new CRS( crs ) );
                            result.responseEnvelopes.add( env );
                        }
                    } catch ( TransformationException e ) {
                        LOG.debug( "Could not create an envelope for the output crs: " + crs + " because: "
                                   + e.getLocalizedMessage() );
                    }
                }
            }
        } catch ( Exception e ) {
            throw new ServiceInitException( e.getMessage(), e );
        }
        return result;
    }

    private RangeSet getRangeSet( RangeSetType rangeSetType ) {
        RangeSet result = null;
        if ( rangeSetType != null ) {
            String name = rangeSetType.getName();
            if ( name != null ) {
                SingleValue<?> nullValue = mapTT( rangeSetType.getNullValue() );
                List<AxisDescription> axisDescription = rangeSetType.getAxisDescription();
                List<AxisSubset> ass = new ArrayList<AxisSubset>( axisDescription.size() );
                if ( !axisDescription.isEmpty() ) {
                    for ( AxisDescription ad : axisDescription ) {
                        if ( ad.getName() != null ) {
                            AxisValue av = ad.getAxisValue();
                            List<IntervalType> ci = av.getInterval();
                            List<Interval<?, ?>> intervals = new ArrayList<Interval<?, ?>>( ci.size() );
                            for ( IntervalType it : ci ) {
                                SingleValue<?> min = mapTT( it.getMin() );
                                SingleValue<?> max = mapTT( it.getMax() );
                                Closure closure = Closure.fromString( it.getClosure().get( 0 ) );
                                intervals.add( new Interval( min, max, closure, null, false, null ) );
                            }

                            List<TypedType> csv = av.getSingleValue();
                            List<SingleValue<?>> singleValues = new ArrayList<SingleValue<?>>( csv.size() );
                            for ( TypedType tt : csv ) {
                                singleValues.add( mapTT( tt ) );
                            }

                            ass.add( new AxisSubset( ad.getName(), ad.getLabel(), intervals, singleValues ) );
                        }
                    }
                }
                result = new RangeSet( name, rangeSetType.getLabel(), ass, nullValue );
            } else {
                LOG.warn( "One of the configured rangesets does not provide a name, this may not be." );
            }
        }
        return result;
    }

    private final static SingleValue<?> mapTT( TypedType tt ) {
        if ( tt == null ) {
            return null;
        }
        return SingleValue.createFromString( tt.getType(), tt.getValue() );
    }

    private WCSCoverage buildCoverage( Coverage coverage, MultiResolutionRaster mrr ) {
        CoverageOptions options = buildOptions( coverage.getNativeFormat(), coverage.getSupportOptions() );
        RangeSet rs = RangeSetBuilder.createBandRangeSetFromRaster(
                                                                    "generated",
                                                                    "Automatically generated dataset, created from the native raster types.",
                                                                    mrr.getRaster( mrr.getResolutions().get(
                                                                                                             mrr.getResolutions().size() - 1 ) ) );
        return new MultiResolutionCoverage( coverage.getName(), coverage.getLabel(), mrr, options, rs );
    }

    private SimpleCoverage buildCoverage( Coverage coverage, AbstractRaster raster ) {
        CoverageOptions options = buildOptions( coverage.getNativeFormat(), coverage.getSupportOptions() );
        RangeSet rs = RangeSetBuilder.createBandRangeSetFromRaster(
                                                                    "generated",
                                                                    "Automatically generated dataset, created from the native raster types.",
                                                                    raster );

        return new SimpleCoverage( coverage.getName(), coverage.getLabel(), raster, options, rs );
    }

    private CoverageOptions buildOptions( String nativeFormat, SupportOptions options ) {
        List<String> formats, crs;
        List<InterpolationType> interpolation;
        if ( options == null ) {
            formats = new LinkedList<String>();
            interpolation = new LinkedList<InterpolationType>();
            crs = new LinkedList<String>();
        } else {
            formats = options.getOutputFormat();
            if ( formats.isEmpty() ) {
                formats.addAll( defaultOptions.getOutputFormat() );
            }

            interpolation = mapInterpolation( options.getInterpolation() );
            if ( interpolation.isEmpty() ) {
                interpolation.addAll( mapInterpolation( defaultOptions.getInterpolation() ) );
            }
            crs = options.getSupportedCRS();
            if ( crs.isEmpty() ) {
                crs.addAll( defaultOptions.getSupportedCRS() );
            }

        }
        if ( options == null || options.isExtend() ) {
            formats.addAll( defaultOptions.getOutputFormat() );
            interpolation.addAll( mapInterpolation( defaultOptions.getInterpolation() ) );
            crs.addAll( defaultOptions.getSupportedCRS() );
        }

        return new CoverageOptions( nativeFormat, formats, crs, interpolation );
    }

    /**
     * @param confInterpolation
     * @return
     */
    private List<InterpolationType> mapInterpolation( List<Interpolation> confInterpolation ) {
        List<InterpolationType> result = new ArrayList<InterpolationType>( confInterpolation.size() );
        for ( Interpolation ip : confInterpolation ) {
            switch ( ip ) {
            case BILINEAR:
                result.add( InterpolationType.BILINEAR );
                break;
            case NEAREST_NEIGHBOR:
                result.add( InterpolationType.NEAREST_NEIGHBOR );
                break;
            case NONE:
                result.add( InterpolationType.NONE );
                break;
            }
        }
        return result;
    }

    private File getFile( String filename ) {
        try {
            URL fileURL = adapter.resolve( filename );
            return new File( fileURL.toURI() );
        } catch ( URISyntaxException e ) {
            LOG.warn( "unable to resolve filename {}", filename );
        } catch ( MalformedURLException e ) {
            LOG.warn( "unable to resolve filename {}", filename );
        }
        return new File( filename );
    }

}
