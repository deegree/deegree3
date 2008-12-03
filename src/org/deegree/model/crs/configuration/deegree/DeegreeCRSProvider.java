//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.model.crs.configuration.deegree;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLTools;
import org.deegree.model.crs.CRSIdentifiable;
import org.deegree.model.crs.configuration.AbstractCRSProvider;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.transformations.Transformation;
import org.deegree.model.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * The <code>DeegreeCRSProvider</code> reads the deegree crs-config (based on it's own xml-schema) and creates the
 * CRS's (and their datums, conversion info's, ellipsoids and projections) if requested.
 * <p>
 * Attention, although urn's are case-sensitive, the deegreeCRSProvider is not. All incoming id's are toLowerCased!
 * </p>
 * <h2>Automatic loading of projection/transformation classes</h2>
 * It is possible to create your own projection/transformation classes, which can be automatically loaded.
 * <p>
 * You can achieve this loading by supplying the <b><code>class</code></b> attribute to a
 * <code>crs:projectedCRS/crs:projection</code> or <code>crs:coordinateSystem/crs:transformation</code> element in
 * the 'deegree-crs-configuration.xml'. This attribute must contain the full class name (with package), e.g.
 * &lt;crs:projection class='my.package.and.projection.Implementation'&gt;
 * </p>
 * Because the loading is done with reflections your classes must sustain following criteria:
 * <h3>Projections</h3>
 * <ol>
 * <li>It must be a sub class of {@link org.deegree.model.crs.projections.Projection}</li>
 * <li>A constructor with following signature must be supplied: <br/> <code>
 * public MyProjection( <br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.model.crs.coordinatesystems.GeographicCRS} underlyingCRS,<br/> 
 * &emsp;&emsp;&emsp;&emsp;double falseNorthing,<br/> 
 * &emsp;&emsp;&emsp;&emsp;double falseEasting,<br/> 
 * &emsp;&emsp;&emsp;&emsp;javax.vecmath.Point2d naturalOrigin,<br/> 
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.model.crs.components.Unit} units,<br/> 
 * &emsp;&emsp;&emsp;&emsp;double scale,<br/> 
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourProjectionElements<br/>
 * );<br/> 
 * </code>
 * <p>
 * The first six parameters are common to all projections (for an explanation of their meaning take a look at
 * {@link Projection}). The last list, will contain all xml-dom elements you supplied in the deegree configuration
 * (child elements of the crs:projection/crs:MyProjection), thus relieving you of the parsing of the
 * deegree-crs-configuration.xml document.
 * </p>
 * </li>
 * </ol>
 * <h3>Transformations</h3>
 * <ol>
 * <li>It must be a sub class of {@link org.deegree.model.crs.transformations.polynomial.PolynomialTransformation}</li>
 * <li>A constructor with following signature must be supplied: <br/> <code>
 * public MyTransformation( <br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; aValues,<br/> 
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; bValues,<br/> 
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.model.crs.coordinatesystems.CoordinateSystem} targetCRS,<br/> 
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourTransformationElements<br/> 
 * );<br/> 
 * </code>
 * <p>
 * The first three parameters are common to all polynomial values (for an explanation of their meaning take a look at
 * {@link org.deegree.model.crs.transformations.polynomial.PolynomialTransformation}). Again, the last list, will
 * contain all xml-dom elements you supplied in the deegree configuration (child elements of the
 * crs:transformation/crs:MyTransformation), thus relieving you of the parsing of the deegree-crs-configuration.xml
 * document.
 * </p>
 * </li>
 * </ol>
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * 
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 * 
 */

public class DeegreeCRSProvider extends AbstractCRSProvider<Element> {

    private static Logger LOG = LoggerFactory.getLogger( DeegreeCRSProvider.class );

    private CRSExporter exporter;

    /**
     * The namespaces used in deegree.
     */
    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * The prefix to use.
     */
    private final static String PRE = CommonNamespaces.CRS_PREFIX + ":";

    // /**
    // * The EPSG-Database defines only 48 different ellipsoids, set to 60, will --probably-- result in no collisions.
    // */
    // private final Map<String, Ellipsoid> ellipsoids = new HashMap<String, Ellipsoid>( 60 * 5 );
    //
    // /**
    // * The EPSG-Database defines over 400 different Geodetic Datums, set to 450, will --probably-- result in no
    // * collisions.
    // */
    // private final Map<String, GeodeticDatum> datums = new HashMap<String, GeodeticDatum>( 450 * 5 );
    //
    // /**
    // * The EPSG-Database defines over 1100 different CoordinateTransformations, set to 1200, will --probably-- result
    // in
    // * no collisions.
    // */
    // private final Map<String, Helmert> conversionInfos = new HashMap<String, Helmert>( 1200 * 5 );
    //
    // /**
    // * Theoretically infinite prime meridians could be defined, let's set it to a more practical number of 42.
    // */
    // private final Map<String, PrimeMeridian> primeMeridians = new HashMap<String, PrimeMeridian>( 42 * 5 );
    //
    // /**
    // * The EPSG-Database defines over 2960 different ProjectedCRS's, set to 3500, will --probably-- result in no
    // * collisions.
    // */
    // private final Map<String, ProjectedCRS> projectedCRSs = new HashMap<String, ProjectedCRS>( 3500 * 5 );
    //
    // /**
    // * The EPSG-Database defines over 490 different GeographicCRS's (geodetic), set to 600, will --probably-- result
    // in
    // * no collisions.
    // */
    // private final Map<String, GeographicCRS> geographicCRSs = new HashMap<String, GeographicCRS>( 600 * 5 );
    //
    // /**
    // * The EPSG-Database defines ???
    // */
    // private final Map<String, CompoundCRS> compoundCRSs = new HashMap<String, CompoundCRS>( 600 * 5 );
    //
    // /**
    // * The EPSG-Database doesn't define GeocentricCRS's, set to 30, will --probably-- result in no collisions.
    // */
    // private final Map<String, GeocentricCRS> geocentricCRSs = new HashMap<String, GeocentricCRS>( 30 );
    //
    // private final List<GeocentricCRS> cachedGeocentricCRSs = new LinkedList<GeocentricCRS>();
    //
    // private final Map<String, String> doubleGeocentricCRSs = new HashMap<String, String>( 5000 );
    //
    // private final List<CompoundCRS> cachedCompoundCRSs = new LinkedList<CompoundCRS>();
    //
    // private final Map<String, String> doubleCompoundCRSs = new HashMap<String, String>( 5000 );
    //
    // private final List<GeographicCRS> cachedGeoCRSs = new LinkedList<GeographicCRS>();
    //
    // private final Map<String, String> doubleGeos = new HashMap<String, String>( 5000 );
    //
    // private final List<ProjectedCRS> cachedProjCRSs = new LinkedList<ProjectedCRS>();
    //
    // private final Map<String, String> doubleProjCRS = new HashMap<String, String>( 3000 );
    //
    // private final List<GeodeticDatum> cachedDatums = new LinkedList<GeodeticDatum>();
    //
    // private final Map<String, String> doubleDatums = new HashMap<String, String>( 3000 );
    //
    // private final List<Helmert> cachedToWGS = new LinkedList<Helmert>();
    //
    // private final Map<String, String> doubleToWGS = new HashMap<String, String>( 3000 );
    //
    // private final List<Ellipsoid> cachedEllipsoids = new LinkedList<Ellipsoid>();
    //
    // private final Map<String, String> doubleEllipsoids = new HashMap<String, String>( 3000 );
    //
    // private final List<PrimeMeridian> cachedMeridians = new LinkedList<PrimeMeridian>();
    //
    // private final Map<String, String> doubleMeridians = new HashMap<String, String>( 3000 );
    //
    // private final Map<String, String> doubleProjections = new HashMap<String, String>( 3000 );

    // /**
    // * The root element of the deegree - crs - configuration.
    // */
    // private Element rootElement;

    // private boolean checkForDoubleDefinition = false;

    /**
     * @param properties
     *            containing information about the crs resource class and the file location of the crs configuration. If
     *            either is null the default mechanism is using the {@link CRSParser} and the
     *            deegree-crs-configuration.xml
     * @throws CRSConfigurationException
     *             if the give file or the default-crs-configuration.xml file could not be loaded.
     */
    public DeegreeCRSProvider( Properties properties ) throws CRSConfigurationException {
        super( properties, CRSParser.class, null );
        if ( getResolver() == null ) {
            CRSParser versionedParser = new CRSParser( this, new Properties( properties ) );
            String version = versionedParser.getVersion();
            if ( !"".equals( version ) ) {
                version = version.trim().replaceAll( "\\.", "_" );
                String className = "org.deegree.model.crs.deegree.configuration.CRSParser_" + version;
                try {
                    Class<?> tClass = Class.forName( className );
                    tClass.asSubclass( CRSParser.class );
                    LOG.debug( "Trying to load configured CRS provider from classname: " + className );
                    Constructor<?> constructor = tClass.getConstructor( this.getClass(), Properties.class,
                                                                        Element.class );
                    if ( constructor == null ) {
                        LOG.error( "No constructor ( " + this.getClass() + ", Properties.class) found in class:"
                                   + className );
                    } else {
                        versionedParser = (CRSParser) constructor.newInstance( this, new Properties( properties ),
                                                                               versionedParser.getRootElement() );
                    }
                    className = "org.deegree.model.crs.deegree.configuration.CRSExporter_" + version;
                    tClass = Class.forName( className );
                    tClass.asSubclass( CRSExporter.class );
                    LOG.debug( "Trying to load configured CRS exporter for version: " + version + " from classname: "
                               + className );
                    constructor = tClass.getConstructor( Properties.class );
                    if ( constructor == null ) {
                        LOG.error( "No constructor ( Properties.class ) found in class:" + className );
                    } else {
                        versionedParser = (CRSParser) constructor.newInstance( new Properties( properties ) );
                    }
                } catch ( InstantiationException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ) );
                } catch ( IllegalAccessException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( ClassNotFoundException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( SecurityException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( NoSuchMethodException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( IllegalArgumentException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( InvocationTargetException e ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, e.getMessage() ), e );
                } catch ( Throwable t ) {
                    LOG.error( Messages.getMessage( "CRS_CONFIG_INSTANTIATION_ERROR", className, t.getMessage() ), t );
                }
            } else {
                exporter = new CRSExporter( new Properties( properties ) );
            }
            setResolver( versionedParser );
        }
    }

    public boolean canExport() {
        return exporter != null;
    }

    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        if ( exporter == null ) {
            throw new UnsupportedOperationException( "Exporting is not supported for this deegree-crs version" );
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter( out );
        exporter.export( writer, crsToExport );

        try {
            sb.append( out.toString( Charset.defaultCharset().displayName() ) );
        } catch ( UnsupportedEncodingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        }

    }

    /**
     * @return the casted resolver of the super class.
     */
    @Override
    public CRSParser getResolver() {
        return (CRSParser) super.getResolver();
    }

    public List<String> getAvailableCRSIds()
                            throws CRSConfigurationException {
        List<Element> allCRSIDs = new LinkedList<Element>();

        try {
            allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "geographicCRS/" + PRE
                                                                                    + "id", nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "projectedCRS/" + PRE
                                                                                    + "id", nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "geocentricCRS/" + PRE
                                                                                    + "id", nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "compoundCRS/" + PRE
                                                                                    + "id", nsContext ) );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_GET_ALL_ELEMENT_IDS", e.getMessage() ),
                                                 e );
        }
        List<String> result = new LinkedList<String>();
        for ( Element crs : allCRSIDs ) {
            if ( crs != null ) {
                result.add( XMLTools.getStringValue( crs ) );
            }
        }
        return result;
    }

    public List<CoordinateSystem> getAvailableCRSs()
                            throws CRSConfigurationException {
        List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
        if ( getResolver().getRootElement() != null ) {
            List<Element> allCRSIDs = new LinkedList<Element>();

            try {
                allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "geographicCRS/"
                                                                                        + PRE + "id", nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "projectedCRS/"
                                                                                        + PRE + "id", nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "geocentricCRS/"
                                                                                        + PRE + "id", nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( getResolver().getRootElement(), "//" + PRE + "compoundCRS/"
                                                                                        + PRE + "id", nsContext ) );
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_GET_ALL_ELEMENT_IDS",
                                                                          e.getMessage() ), e );
            }
            final int total = allCRSIDs.size();
            int count = 0;
            int percentage = (int) Math.round( total / 100.d );
            int number = 0;
            System.out.println( "Trying to create a total of " + total + " coordinate systems." );
            for ( Element crsID : allCRSIDs ) {
                if ( crsID != null ) {
                    String id = crsID.getTextContent();
                    if ( id != null && !"".equals( id.trim() ) ) {
                        if ( count++ % percentage == 0 ) {
                            System.out.print( "\r" + ( number ) + ( ( number++ < 10 ) ? "  " : " " ) + "% created" );
                        }
                        allSystems.add( getCRSByID( id ) );
                    }
                }
            }
            System.out.println();
            // if ( checkForDoubleDefinition ) {
            // allSystems.addAll( cachedGeoCRSs );
            // allSystems.addAll( cachedProjCRSs );
            // allSystems.addAll( cachedGeocentricCRSs );
            // allSystems.addAll( cachedCompoundCRSs );
            // StringBuilder cachedGeos = new StringBuilder( "Cached Geographic coordinatesystems (" );
            // cachedGeos.append( cachedGeoCRSs.size() ).append( "):\n" );
            // for ( GeographicCRS geo : cachedGeoCRSs ) {
            // cachedGeos.append( geo.getIdentifier() ).append( "\n" );
            // }
            // System.out.println( cachedGeos.toString() );
            // if ( !doubleGeos.isEmpty() ) {
            // Set<String> keys = doubleGeos.keySet();
            // LOG.info( "Following geographic crs's could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleGeos.get( key ) );
            // }
            // }
            // if ( !doubleProjCRS.isEmpty() ) {
            // Set<String> keys = doubleProjCRS.keySet();
            // LOG.info( "Following projected crs's could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleProjCRS.get( key ) );
            // }
            //
            // }
            // if ( !doubleGeocentricCRSs.isEmpty() ) {
            // Set<String> keys = doubleGeocentricCRSs.keySet();
            // LOG.info( "Following geocentric crs's could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleGeocentricCRSs.get( key ) );
            // }
            // }
            //
            // if ( !doubleProjections.isEmpty() ) {
            // Set<String> keys = doubleProjections.keySet();
            // LOG.info( "Following projections could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleProjections.get( key ) );
            // }
            // }
            // if ( !doubleDatums.isEmpty() ) {
            // Set<String> keys = doubleDatums.keySet();
            // LOG.info( "Following datums could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleDatums.get( key ) );
            // }
            // }
            // if ( !doubleToWGS.isEmpty() ) {
            // Set<String> keys = doubleToWGS.keySet();
            // LOG.info( "Following wgs conversion infos could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleToWGS.get( key ) );
            // }
            // }
            // if ( !doubleEllipsoids.isEmpty() ) {
            // Set<String> keys = doubleEllipsoids.keySet();
            // LOG.info( "Following ellipsoids could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleEllipsoids.get( key ) );
            // }
            // }
            // if ( !doubleMeridians.isEmpty() ) {
            // Set<String> keys = doubleEllipsoids.keySet();
            // LOG.info( "Following prime meridians could probably be mapped on eachother" );
            // for ( String key : keys ) {
            // LOG.info( key + " : " + doubleMeridians.get( key ) );
            // }
            // }
            // } else {
            // Collection<CompoundCRS> cCRSs = compoundCRSs.values();
            // for ( String key : compoundCRSs.keySet() ) {
            // CompoundCRS crs = compoundCRSs.get( key );
            // if ( !allSystems.contains( crs ) ) {
            // allSystems.add( crs );
            // }
            // }
            // for ( String key : geographicCRSs.keySet() ) {
            // GeographicCRS crs = geographicCRSs.get( key );
            // if ( !allSystems.contains( crs ) ) {
            // allSystems.add( crs );
            // }
            // }
            // for ( String key : geocentricCRSs.keySet() ) {
            // GeocentricCRS crs = geocentricCRSs.get( key );
            // if ( !allSystems.contains( crs ) ) {
            // allSystems.add( crs );
            // }
            // }
            // for ( String key : projectedCRSs.keySet() ) {
            // ProjectedCRS crs = projectedCRSs.get( key );
            // if ( !allSystems.contains( crs ) ) {
            // allSystems.add( crs );
            // }
            // }
            // }

        } else {
            LOG.debug( "The root element is null, is this correct behaviour?" );
        }
        return allSystems;
    }

    public CRSIdentifiable getIdentifiable( String id )
                            throws CRSConfigurationException {
        CRSIdentifiable result = getCachedIdentifiable( id );
        if ( result == null ) {
            result = getResolver().parseIdentifiableObject( id );
        }
        return result;
    }

    @Override
    protected CoordinateSystem parseCoordinateSystem( Element crsDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseCoordinateSystem( crsDefinition );
    }

    @Override
    public Transformation parseTransformation( Element transformationDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseTransformation( transformationDefinition );

    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return getResolver().getTransformation( sourceCRS, targetCRS );
    }

}
