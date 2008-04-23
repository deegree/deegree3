//$HeadURL: $
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

package org.deegree.model.crs.configuration;

import static org.deegree.commons.xml.CommonNamespaces.CRSNS;
import static org.deegree.model.crs.projections.ProjectionUtils.EPS11;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.vecmath.Point2d;
import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceContext;
import org.deegree.commons.xml.XMLFragment;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XMLTools;
import org.deegree.model.crs.Identifiable;
import org.deegree.model.crs.components.Axis;
import org.deegree.model.crs.components.Ellipsoid;
import org.deegree.model.crs.components.GeodeticDatum;
import org.deegree.model.crs.components.PrimeMeridian;
import org.deegree.model.crs.components.Unit;
import org.deegree.model.crs.coordinatesystems.CompoundCRS;
import org.deegree.model.crs.coordinatesystems.CoordinateSystem;
import org.deegree.model.crs.coordinatesystems.GeocentricCRS;
import org.deegree.model.crs.coordinatesystems.GeographicCRS;
import org.deegree.model.crs.coordinatesystems.ProjectedCRS;
import org.deegree.model.crs.exceptions.CRSConfigurationException;
import org.deegree.model.crs.projections.Projection;
import org.deegree.model.crs.projections.azimuthal.LambertAzimuthalEqualArea;
import org.deegree.model.crs.projections.azimuthal.StereographicAlternative;
import org.deegree.model.crs.projections.azimuthal.StereographicAzimuthal;
import org.deegree.model.crs.projections.conic.LambertConformalConic;
import org.deegree.model.crs.projections.cylindric.TransverseMercator;
import org.deegree.model.crs.transformations.helmert.WGS84ConversionInfo;
import org.deegree.model.crs.transformations.polynomial.LeastSquareApproximation;
import org.deegree.model.crs.transformations.polynomial.PolynomialTransformation;
import org.deegree.model.i18n.Messages;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

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
 * {@link org.deegree.model.crs.transformations.polynomial.PolynomialTransformation}). Again, the last list, will contain all
 * xml-dom elements you supplied in the deegree configuration (child elements of the
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

public class DeegreeCRSProvider implements CRSProvider {

    private static Log LOG = LogFactory.getLog( DeegreeCRSProvider.class );

    /**
     * The standard configuration file, points to deegree-crs-configuration.xml.
     */
    private static final String STANDARD_CONFIG = "deegree-crs-configuration.xml";

    /**
     * The namespaces used in deegree.
     */
    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    /**
     * The prefix to use.
     */
    private final static String PRE = CommonNamespaces.CRS_PREFIX + ":";

    /**
     * The namespace to use.
     */
    private final static String CRS_URI = CommonNamespaces.CRSNS.toASCIIString();

    /**
     * The EPSG-Database defines only 48 different ellipsoids, set to 60, will --probably-- result in no collisions.
     */
    private final Map<String, Ellipsoid> ellipsoids = new HashMap<String, Ellipsoid>( 60 * 5 );

    /**
     * The EPSG-Database defines over 400 different Geodetic Datums, set to 450, will --probably-- result in no
     * collisions.
     */
    private final Map<String, GeodeticDatum> datums = new HashMap<String, GeodeticDatum>( 450 * 5 );

    /**
     * The EPSG-Database defines over 1100 different CoordinateTransformations, set to 1200, will --probably-- result in
     * no collisions.
     */
    private final Map<String, WGS84ConversionInfo> conversionInfos = new HashMap<String, WGS84ConversionInfo>( 1200 * 5 );

    /**
     * Theoretically infinite prime meridians could be defined, let's set it to a more practical number of 42.
     */
    private final Map<String, PrimeMeridian> primeMeridians = new HashMap<String, PrimeMeridian>( 42 * 5 );

    /**
     * The EPSG-Database defines over 2960 different ProjectedCRS's, set to 3500, will --probably-- result in no
     * collisions.
     */
    private final Map<String, ProjectedCRS> projectedCRSs = new HashMap<String, ProjectedCRS>( 3500 * 5 );

    /**
     * The EPSG-Database defines over 490 different GeographicCRS's (geodetic), set to 600, will --probably-- result in
     * no collisions.
     */
    private final Map<String, GeographicCRS> geographicCRSs = new HashMap<String, GeographicCRS>( 600 * 5 );

    /**
     * The EPSG-Database defines ???
     */
    private final Map<String, CompoundCRS> compoundCRSs = new HashMap<String, CompoundCRS>( 600 * 5 );

    /**
     * The EPSG-Database doesn't define GeocentricCRS's, set to 30, will --probably-- result in no collisions.
     */
    private final Map<String, GeocentricCRS> geocentricCRSs = new HashMap<String, GeocentricCRS>( 30 );

    private final List<GeocentricCRS> cachedGeocentricCRSs = new LinkedList<GeocentricCRS>();

    private final Map<String, String> doubleGeocentricCRSs = new HashMap<String, String>( 5000 );

    private final List<CompoundCRS> cachedCompoundCRSs = new LinkedList<CompoundCRS>();

    private final Map<String, String> doubleCompoundCRSs = new HashMap<String, String>( 5000 );

    private final List<GeographicCRS> cachedGeoCRSs = new LinkedList<GeographicCRS>();

    private final Map<String, String> doubleGeos = new HashMap<String, String>( 5000 );

    private final List<ProjectedCRS> cachedProjCRSs = new LinkedList<ProjectedCRS>();

    private final Map<String, String> doubleProjCRS = new HashMap<String, String>( 3000 );

    private final List<GeodeticDatum> cachedDatums = new LinkedList<GeodeticDatum>();

    private final Map<String, String> doubleDatums = new HashMap<String, String>( 3000 );

    private final List<WGS84ConversionInfo> cachedToWGS = new LinkedList<WGS84ConversionInfo>();

    private final Map<String, String> doubleToWGS = new HashMap<String, String>( 3000 );

    private final List<Ellipsoid> cachedEllipsoids = new LinkedList<Ellipsoid>();

    private final Map<String, String> doubleEllipsoids = new HashMap<String, String>( 3000 );

    private final List<PrimeMeridian> cachedMeridians = new LinkedList<PrimeMeridian>();

    private final Map<String, String> doubleMeridians = new HashMap<String, String>( 3000 );

    private final Map<String, String> doubleProjections = new HashMap<String, String>( 3000 );

    /**
     * The root element of the deegree - crs - configuration.
     */
    private Element rootElement;

    private boolean checkForDoubleDefinition = false;

    /**
     * Empty constructor may only be used for exporting, other usage will result in undefined behavior.
     */
    public DeegreeCRSProvider() {
        Document doc = XMLTools.create();
        rootElement = doc.createElementNS( CommonNamespaces.CRSNS.toASCIIString(), PRE + "definitions" );
        rootElement = (Element) doc.importNode( rootElement, false );
        rootElement = (Element) doc.appendChild( rootElement );
    }

    /**
     * @param f
     *            to load coordinate system definitions from if null, the standard configuration file will be used, by
     *            searching '/' first and if unsuccessful in org.deegree.model.crs.configuration.
     * @throws CRSConfigurationException
     *             if the give file or the default-crs-configuration.xml file could not be loaded.
     */
    public DeegreeCRSProvider( File f ) throws CRSConfigurationException {
        InputStream is = null;
        if ( f == null ) {
            LOG.debug( "No configuration file given, trying to load standard-crs-configuration.xml" );
            is = DeegreeCRSProvider.class.getResourceAsStream( "/" + STANDARD_CONFIG );
            if ( is == null ) {
                is = DeegreeCRSProvider.class.getResourceAsStream( STANDARD_CONFIG );
            } else {
                LOG.debug( "Using the configuration file loaded from root directory instead of org.deegree.model.crs.configuration" );
            }
            if ( is == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_DEFAULT_CONFIG_FOUND" ) );
            }
        } else {
            LOG.debug( "Trying to load configuration from file: " + f.getAbsoluteFile() );
            try {
                is = new FileInputStream( f );
            } catch ( FileNotFoundException e ) {
                throw new CRSConfigurationException( e );
            }
        }
        Reader read = new BufferedReader( new InputStreamReader( is ) );
        try {
            XMLFragment configDocument = new XMLFragment( read, XMLFragment.DEFAULT_URL );
            rootElement = configDocument.getRootElement();
        } catch ( MalformedURLException e ) {
            throw new CRSConfigurationException( e );
        } catch ( IOException e ) {
            throw new CRSConfigurationException( e );
        } catch ( SAXException e ) {
            throw new CRSConfigurationException( e );
        } finally {
            try {
                read.close();
            } catch ( IOException e ) {
                // could not close the stream, just leave it as it is.
            }
        }
    }

    public boolean canExport() {
        return true;
    }

    public synchronized CoordinateSystem getCRSByID( String crsId )
                            throws CRSConfigurationException {
        if ( crsId != null && !"".equals( crsId.trim() ) ) {

            crsId = crsId.toUpperCase().trim();
            LOG.debug( "Trying to load crs with id: " + crsId + " from cache." );
            if ( geographicCRSs.containsKey( crsId ) && geographicCRSs.get( crsId ) != null ) {
                GeographicCRS r = geographicCRSs.get( crsId );
                LOG.debug( "Found geographic crs: " + r + " from given id: " + crsId );
                if ( checkForDoubleDefinition ) {
                    if ( cachedGeoCRSs.contains( r ) ) {
                        int index = cachedGeoCRSs.indexOf( r );
                        r = cachedGeoCRSs.get( index );
                        LOG.debug( "Found chached geographic crs: " + r );
                    }
                }
                LOG.debug( "Found geographic crs: " + r
                              + " cached, it should be equal with geographic crs with id: " + crsId );
                return r;
            } else if ( projectedCRSs.containsKey( crsId ) && projectedCRSs.get( crsId ) != null ) {
                ProjectedCRS r = projectedCRSs.get( crsId );
                if ( checkForDoubleDefinition ) {
                    if ( cachedProjCRSs.contains( r ) ) {
                        int index = cachedProjCRSs.indexOf( r );
                        r = cachedProjCRSs.get( index );
                    }
                }
                LOG.debug( "Found projected crs with id: " + r
                              + " cached, it should be equal with projected crs with id: " + crsId );
                return r;
            } else if ( geocentricCRSs.containsKey( crsId ) && geocentricCRSs.get( crsId ) != null ) {
                GeocentricCRS r = geocentricCRSs.get( crsId );
                if ( checkForDoubleDefinition ) {
                    if ( cachedGeocentricCRSs.contains( r ) ) {
                        int index = cachedGeocentricCRSs.indexOf( r );
                        r = cachedGeocentricCRSs.get( index );
                    }
                }
                LOG.debug( "Found geocentric crs with id: " + r
                              + " cached, it should be equal with geocentric crs with id: " + crsId );
                return r;
            } else if ( compoundCRSs.containsKey( crsId ) && compoundCRSs.get( crsId ) != null ) {
                CompoundCRS r = compoundCRSs.get( crsId );
                return r;
            }
            LOG.debug( "No crs with id: " + crsId + " found in cache." );
            if ( rootElement == null ) {
                this.notifyAll();
                return null;
            }
            Element crsElement = getTopElementFromID( crsId );
            if ( crsElement == null ) {
                LOG.debug( "The requested crs id: " + crsId
                              + " could not be mapped to a configured CoordinateSystem." );
                this.notifyAll();
                return null;
            }
            String crsType = crsElement.getLocalName();
            if ( crsType == null || "".equals( crsType.trim() ) ) {
                LOG.debug( "The requested crs id: " + crsId
                              + " could not be mapped to a configured CoordinateSystem." );
                this.notifyAll();
                return null;
            }
            if ( "geographicCRS".equalsIgnoreCase( crsType ) ) {
                return parseGeographicCRS( crsElement );
            } else if ( "projectedCRS".equalsIgnoreCase( crsType ) ) {
                return parseProjectedCRS( crsElement );
            } else if ( "geocentricCRS".equalsIgnoreCase( crsType ) ) {
                return parseGeocentricCRS( crsElement );
            } else if ( "compoundCRS".equalsIgnoreCase( crsType ) ) {
                return parseCompoundCRS( crsElement );
            }
        }
        LOG.debug( "The id: "
                      + crsId
                      + " could not be mapped to a valid deegreec-crs, currently projectedCRS, geographicCRS and geocentricCRS are supported." );
        return null;
    }

    public void export( StringBuilder sb, List<CoordinateSystem> crsToExport ) {
        if ( crsToExport != null ) {
            if ( crsToExport.size() != 0 ) {
                LOG.debug( "Trying to export: " + crsToExport.size() + " coordinate systems." );
                XMLFragment frag = new XMLFragment( new QName( "crs", "definitions", CRSNS.toASCIIString() ) );
                Element root = frag.getRootElement();
                LinkedList<String> exportedIDs = new LinkedList<String>();
                for ( CoordinateSystem crs : crsToExport ) {
                    if ( crs.getType() == CoordinateSystem.GEOCENTRIC_CRS ) {
                        export( (GeocentricCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                        export( (GeographicCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.PROJECTED_CRS ) {
                        export( (ProjectedCRS) crs, root, exportedIDs );
                    } else if ( crs.getType() == CoordinateSystem.COMPOUND_CRS ) {
                        export( (CompoundCRS) crs, root, exportedIDs );
                    }
                }
                root.normalize();
                Document validDoc = createValidDocument( root );
                try {
                    XMLFragment frag2 = new XMLFragment( validDoc, "http://www.deegree.org/crs" );
                    sb.append( frag2.getAsPrettyString() );
                } catch ( MalformedURLException e ) {
                    LOG.error( "Could not export crs definitions because: " + e.getMessage(), e );
                }
            } else {
                LOG.warn( "No coordinate system were given (list.size() == 0)." );
            }
        } else {
            LOG.error( "No coordinate system were given (list == null)." );
        }
    }

    public List<String> getAvailableCRSIds()
                            throws CRSConfigurationException {
        List<Element> allCRSIDs = new LinkedList<Element>();

        try {
            allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "geographicCRS/" + PRE + "id[1]",
                                                    nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "projectedCRS/" + PRE + "id[1]",
                                                    nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "geocentricCRS/" + PRE + "id[1]",
                                                    nsContext ) );
            allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "compoundCRS/" + PRE + "id[1]", nsContext ) );
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
        if ( rootElement != null ) {
            List<Element> allCRSIDs = new LinkedList<Element>();

            try {
                allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "geographicCRS/" + PRE + "id[1]",
                                                        nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "projectedCRS/" + PRE + "id[1]",
                                                        nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "geocentricCRS/" + PRE + "id[1]",
                                                        nsContext ) );
                allCRSIDs.addAll( XMLTools.getElements( rootElement, "//" + PRE + "compoundCRS/" + PRE + "id[1]",
                                                        nsContext ) );
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
                        getCRSByID( id );
                    }
                }
            }
            System.out.println();
            if ( checkForDoubleDefinition ) {
                allSystems.addAll( cachedGeoCRSs );
                allSystems.addAll( cachedProjCRSs );
                allSystems.addAll( cachedGeocentricCRSs );
                allSystems.addAll( cachedCompoundCRSs );
                StringBuilder cachedGeos = new StringBuilder( "Cached Geographic coordinatesystems (" );
                cachedGeos.append( cachedGeoCRSs.size() ).append( "):\n" );
                for ( GeographicCRS geo : cachedGeoCRSs ) {
                    cachedGeos.append( geo.getIdentifier() ).append( "\n" );
                }
                System.out.println( cachedGeos.toString() );
                if ( !doubleGeos.isEmpty() ) {
                    Set<String> keys = doubleGeos.keySet();
                    LOG.info( "Following geographic crs's could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleGeos.get( key ) );
                    }
                }
                if ( !doubleProjCRS.isEmpty() ) {
                    Set<String> keys = doubleProjCRS.keySet();
                    LOG.info( "Following projected crs's could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleProjCRS.get( key ) );
                    }

                }
                if ( !doubleGeocentricCRSs.isEmpty() ) {
                    Set<String> keys = doubleGeocentricCRSs.keySet();
                    LOG.info( "Following geocentric crs's could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleGeocentricCRSs.get( key ) );
                    }
                }

                if ( !doubleProjections.isEmpty() ) {
                    Set<String> keys = doubleProjections.keySet();
                    LOG.info( "Following projections could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleProjections.get( key ) );
                    }
                }
                if ( !doubleDatums.isEmpty() ) {
                    Set<String> keys = doubleDatums.keySet();
                    LOG.info( "Following datums could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleDatums.get( key ) );
                    }
                }
                if ( !doubleToWGS.isEmpty() ) {
                    Set<String> keys = doubleToWGS.keySet();
                    LOG.info( "Following wgs conversion infos could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleToWGS.get( key ) );
                    }
                }
                if ( !doubleEllipsoids.isEmpty() ) {
                    Set<String> keys = doubleEllipsoids.keySet();
                    LOG.info( "Following ellipsoids could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleEllipsoids.get( key ) );
                    }
                }
                if ( !doubleMeridians.isEmpty() ) {
                    Set<String> keys = doubleEllipsoids.keySet();
                    LOG.info( "Following prime meridians could probably be mapped on eachother" );
                    for ( String key : keys ) {
                        LOG.info( key + " : " + doubleMeridians.get( key ) );
                    }
                }
            } else {
                //Collection<CompoundCRS> cCRSs = compoundCRSs.values();
                for ( String key : compoundCRSs.keySet() ) {
                    CompoundCRS crs = compoundCRSs.get( key );
                    if ( !allSystems.contains( crs ) ) {
                        allSystems.add( crs );
                    }
                }
                for ( String key : geographicCRSs.keySet() ) {
                    GeographicCRS crs = geographicCRSs.get( key );
                    if ( !allSystems.contains( crs ) ) {
                        allSystems.add( crs );
                    }
                }
                for ( String key : geocentricCRSs.keySet() ) {
                    GeocentricCRS crs = geocentricCRSs.get( key );
                    if ( !allSystems.contains( crs ) ) {
                        allSystems.add( crs );
                    }
                }
                for ( String key : projectedCRSs.keySet() ) {
                    ProjectedCRS crs = projectedCRSs.get( key );
                    if ( !allSystems.contains( crs ) ) {
                        allSystems.add( crs );
                    }
                }
            }

        } else {
            LOG.debug( "The root element is null, is this correct behaviour?" );
        }
        return allSystems;
    }

    private Document createValidDocument( Element root ) {
        // List<Element> lastInput = new LinkedList<Element>( 100 );
        try {
            List<Element> valid = XMLTools.getElements( root, PRE + "ellipsoid", nsContext );
            valid.addAll( XMLTools.getElements( root, PRE + "geodeticDatum", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "projectedCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "geographicCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "compoundCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "geocentricCRS", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "primeMeridian", nsContext ) );
            valid.addAll( XMLTools.getElements( root, PRE + "wgs84Transformation", nsContext ) );
            Document doc = XMLTools.create();
            Element newRoot = doc.createElementNS( CommonNamespaces.CRSNS.toASCIIString(), PRE + "definitions" );
            newRoot = (Element) doc.importNode( newRoot, false );
            newRoot = (Element) doc.appendChild( newRoot );
            for ( int i = 0; i < valid.size(); ++i ) {
                Element el = valid.get( i );
                el = (Element) doc.importNode( el, true );
                newRoot.appendChild( el );
            }
            XMLTools.appendNSBinding( newRoot, CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS );
            newRoot.setAttributeNS(
                                    CommonNamespaces.XSINS.toASCIIString(),
                                    "xsi:schemaLocation",
                                    "http://www.deegree.org/crs c:/windows/profiles/rutger/EIGE~VO5/eclipse-projekte/coordinate_systems/resources/schema/crsdefinition.xsd" );
            return doc;
        } catch ( XMLParsingException xmle ) {
            xmle.printStackTrace();
        }
        return root.getOwnerDocument();
    }

    /**
     * Export the projected CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param projectedCRS
     *            to be exported
     * @param rootNode
     *            to export the projected CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( ProjectedCRS projectedCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( projectedCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projectedCRS" );
            exportAbstractCRS( projectedCRS, crsElement );
            GeographicCRS underLyingCRS = projectedCRS.getGeographicCRS();
            export( underLyingCRS, rootNode, exportedIds );

            // Add a reference from the geographicCRS element to the projectedCRS element.
            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedGeographicCRS",
                                    underLyingCRS.getIdentifier() );

            export( projectedCRS.getProjection(), crsElement );

            // Add the ids to the exportedID list.
            for ( String eID : projectedCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the geocentric/geographic CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geographicCRS
     *            to be exported
     * @param rootNode
     *            to export the geographic CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeographicCRS geographicCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( geographicCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geographicCRS" );
            exportAbstractCRS( geographicCRS, crsElement );

            // export the datum.
            GeodeticDatum datum = geographicCRS.getGeodeticDatum();
            if ( datum != null ) {
                export( datum, rootNode, exportedIds );
                // Add a reference from the datum element to the geographic element.
                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
            }
            // Add the ids to the exportedID list.
            for ( String eID : geographicCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the compoundCRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param compoundCRS
     *            to be exported
     * @param rootNode
     *            to export the geographic CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( CompoundCRS compoundCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( compoundCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "compoundCRS" );
            exportIdentifiable( compoundCRS, crsElement );
            CoordinateSystem underLyingCRS = compoundCRS.getUnderlyingCRS();
            if ( underLyingCRS.getType() == CoordinateSystem.GEOGRAPHIC_CRS ) {
                export( (GeographicCRS) underLyingCRS, rootNode, exportedIds );
            } else if ( underLyingCRS.getType() == CoordinateSystem.PROJECTED_CRS ) {
                export( (ProjectedCRS) underLyingCRS, rootNode, exportedIds );
            }

            // Add a reference from the geographicCRS element to the projectedCRS element.
            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedCRS", underLyingCRS.getIdentifier() );
            export( compoundCRS.getHeightAxis(), crsElement );

            XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "defaultHeight",
                                    Double.toString( compoundCRS.getDefaultHeight() ) );

            // Add the ids to the exportedID list.
            for ( String eID : compoundCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Export the projection to it's appropriate deegree-crs-definitions form.
     * 
     * @param projection
     *            to be exported
     * @param rootNode
     *            to export the projection to.
     */
    private void export( Projection projection, Element rootNode ) {
        Element rootElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "projection" );
        String elementName = projection.getName();
        Element projectionElement = XMLTools.appendElement( rootElement, CommonNamespaces.CRSNS, PRE + elementName );
        // exportIdentifiable( projection, projectionElement );
        Element tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE
                                                                                         + "latitudeOfNaturalOrigin",
                                              Double.toString( Math.toDegrees( projection.getProjectionLatitude() ) ) );
        tmp.setAttribute( "inDegrees", "true" );
        tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "longitudeOfNaturalOrigin",
                                      Double.toString( Math.toDegrees( projection.getProjectionLongitude() ) ) );
        tmp.setAttribute( "inDegrees", "true" );

        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "scaleFactor",
                                Double.toString( projection.getScale() ) );
        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseEasting",
                                Double.toString( projection.getFalseEasting() ) );
        XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "falseNorthing",
                                Double.toString( projection.getFalseNorthing() ) );
        if ( "transverseMercator".equalsIgnoreCase( elementName ) ) {
            XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "northernHemisphere",
                                    Boolean.toString( ( (TransverseMercator) projection ).getHemisphere() ) );
        } else if ( "lambertConformalConic".equalsIgnoreCase( elementName ) ) {
            double paralellLatitude = ( (LambertConformalConic) projection ).getFirstParallelLatitude();
            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                paralellLatitude = Math.toDegrees( paralellLatitude );
                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS, PRE + "firstParallelLatitude",
                                              Double.toString( paralellLatitude ) );
                tmp.setAttribute( "inDegrees", "true" );
            }
            paralellLatitude = ( (LambertConformalConic) projection ).getSecondParallelLatitude();
            if ( !Double.isNaN( paralellLatitude ) && Math.abs( paralellLatitude ) > EPS11 ) {
                paralellLatitude = Math.toDegrees( paralellLatitude );
                tmp = XMLTools.appendElement( projectionElement, CommonNamespaces.CRSNS,
                                              PRE + "secondParallelLatitude", Double.toString( paralellLatitude ) );
                tmp.setAttribute( "inDegrees", "true" );
            }
        } else if ( "stereographicAzimuthal".equalsIgnoreCase( elementName ) ) {
            tmp = XMLTools.appendElement(
                                          projectionElement,
                                          CommonNamespaces.CRSNS,
                                          PRE + "trueScaleLatitude",
                                          Double.toString( ( (StereographicAzimuthal) projection ).getTrueScaleLatitude() ) );
            tmp.setAttribute( "inDegrees", "true" );
        }
    }

    /**
     * Export the confInvo to it's appropriate deegree-crs-definitions form.
     * 
     * @param confInvo
     *            to be exported
     * @param rootNode
     *            to export the confInvo to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( WGS84ConversionInfo confInvo, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( confInvo.getIdentifier() ) ) {
            Element convElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "wgs84Transformation" );
            exportIdentifiable( confInvo, convElement );

            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisTranslation",
                                    Double.toString( confInvo.dx ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisTranslation",
                                    Double.toString( confInvo.dy ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisTranslation",
                                    Double.toString( confInvo.dz ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "xAxisRotation",
                                    Double.toString( confInvo.ex ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "yAxisRotation",
                                    Double.toString( confInvo.ey ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "zAxisRotation",
                                    Double.toString( confInvo.ez ) );
            XMLTools.appendElement( convElement, CommonNamespaces.CRSNS, PRE + "scaleDifference",
                                    Double.toString( confInvo.ppm ) );

            // Add the ids to the exportedID list.
            for ( String eID : confInvo.getIdentifiers() ) {
                exportedIds.add( eID );
            }

            // finally add the WGS84-Transformation node to the rootnode.
            rootNode.appendChild( convElement );
        }

    }

    /**
     * Export the PrimeMeridian to it's appropriate deegree-crs-definitions form.
     * 
     * @param pMeridian
     *            to be exported
     * @param rootNode
     *            to export the pMeridian to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( PrimeMeridian pMeridian, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( pMeridian.getIdentifier() ) ) {
            Element meridianElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "primeMeridian" );
            exportIdentifiable( pMeridian, meridianElement );
            export( pMeridian.getAngularUnit(), meridianElement );
            XMLTools.appendElement( meridianElement, CommonNamespaces.CRSNS, PRE + "longitude",
                                    Double.toString( pMeridian.getLongitude() ) );

            // Add the ids to the exportedID list.
            for ( String eID : pMeridian.getIdentifiers() ) {
                exportedIds.add( eID );
            }

            // finally add the prime meridian node to the rootnode.
            rootNode.appendChild( meridianElement );
        }
    }

    /**
     * Export the ellipsoid to it's appropriate deegree-crs-definitions form.
     * 
     * @param ellipsoid
     *            to be exported
     * @param rootNode
     *            to export the ellipsoid to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( Ellipsoid ellipsoid, Element rootNode, final List<String> exportedIds ) {
        if ( !exportedIds.contains( ellipsoid.getIdentifier() ) ) {
            Element ellipsoidElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "ellipsoid" );
            exportIdentifiable( ellipsoid, ellipsoidElement );
            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "semiMajorAxis",
                                    Double.toString( ellipsoid.getSemiMajorAxis() ) );
            XMLTools.appendElement( ellipsoidElement, CommonNamespaces.CRSNS, PRE + "inverseFlatting",
                                    Double.toString( ellipsoid.getInverseFlattening() ) );
            export( ellipsoid.getUnits(), ellipsoidElement );

            // Add the ids to the exportedID list.
            for ( String eID : ellipsoid.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the ellipsoid node to the rootnode.
            rootNode.appendChild( ellipsoidElement );
        }
    }

    /**
     * Export the datum to it's appropriate deegree-crs-definitions form.
     * 
     * @param datum
     *            to be exported
     * @param rootNode
     *            to export the datum to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeodeticDatum datum, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( datum.getIdentifier() ) ) {
            Element datumElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geodeticDatum" );
            exportIdentifiable( datum, datumElement );
            /**
             * EXPORT the ELLIPSOID
             */
            Ellipsoid ellipsoid = datum.getEllipsoid();
            if ( ellipsoid != null ) {
                export( ellipsoid, rootNode, exportedIds );
                // Add a reference from the ellipsoid element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedEllipsoid",
                                        ellipsoid.getIdentifier() );
            }

            /**
             * EXPORT the PRIME_MERIDIAN
             */
            PrimeMeridian pMeridian = datum.getPrimeMeridian();
            if ( pMeridian != null ) {
                export( pMeridian, rootNode, exportedIds );
                // Add a reference from the prime meridian element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedPrimeMeridian",
                                        pMeridian.getIdentifier() );
            }

            /**
             * EXPORT the WGS-84-Conversion INFO
             */
            WGS84ConversionInfo confInvo = datum.getWGS84Conversion();
            if ( confInvo != null ) {
                export( confInvo, rootNode, exportedIds );
                // Add a reference from the prime meridian element to the datum element.
                XMLTools.appendElement( datumElement, CommonNamespaces.CRSNS, PRE + "usedWGS84ConversionInfo",
                                        confInvo.getIdentifier() );
            }

            // Add the ids to the exportedID list.
            for ( String eID : datum.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the datum node to the rootnode.
            rootNode.appendChild( datumElement );
        }
    }

    /**
     * Export toplevel crs features.
     * 
     * @param crs
     *            to be exported
     * @param crsElement
     *            to export to
     */
    private void exportAbstractCRS( CoordinateSystem crs, Element crsElement ) {
        exportIdentifiable( crs, crsElement );
        Axis[] axis = crs.getAxis();
        StringBuilder axisOrder = new StringBuilder( 200 );
        for ( int i = 0; i < axis.length; ++i ) {
            Axis a = axis[i];
            export( a, crsElement );
            axisOrder.append( a.getName() );
            if ( ( i + 1 ) < axis.length ) {
                axisOrder.append( ", " );
            }
        }
        XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "axisOrder", axisOrder.toString() );

        export( crs.getTransformations(), crsElement );

    }

    /**
     * Export the geocentric CRS to it's appropriate deegree-crs-definitions form.
     * 
     * @param geocentricCRS
     *            to be exported
     * @param rootNode
     *            to export the geocentric CRS to.
     * @param exportedIds
     *            a list of id's already exported.
     */
    private void export( GeocentricCRS geocentricCRS, Element rootNode, List<String> exportedIds ) {
        if ( !exportedIds.contains( geocentricCRS.getIdentifier() ) ) {
            Element crsElement = XMLTools.appendElement( rootNode, CommonNamespaces.CRSNS, PRE + "geocentricCRS" );
            exportAbstractCRS( geocentricCRS, crsElement );
            // export the datum.
            GeodeticDatum datum = geocentricCRS.getGeodeticDatum();
            if ( datum != null ) {
                export( datum, rootNode, exportedIds );
                // Add a reference from the datum element to the geocentric element.
                XMLTools.appendElement( crsElement, CommonNamespaces.CRSNS, PRE + "usedDatum", datum.getIdentifier() );
            }
            // Add the ids to the exportedID list.
            for ( String eID : geocentricCRS.getIdentifiers() ) {
                exportedIds.add( eID );
            }
            // finally add the crs node to the rootnode.
            rootNode.appendChild( crsElement );
        }
    }

    /**
     * Creates the basic nodes of the identifiable object.
     * 
     * @param id
     *            object to be exported.
     * @param currentNode
     *            to expand
     */
    private void exportIdentifiable( Identifiable id, Element currentNode ) {
        for ( String i : id.getIdentifiers() ) {
            if ( i != null ) {
                XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "id", i );
            }

        }
        if ( id.getNames() != null && id.getNames().length > 0 ) {
            for ( String i : id.getNames() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "name", i );
                }
            }
        }
        if ( id.getVersions() != null && id.getVersions().length > 0 ) {
            for ( String i : id.getVersions() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "version", i );
                }
            }
        }
        if ( id.getDescriptions() != null && id.getDescriptions().length > 0 ) {
            for ( String i : id.getDescriptions() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "description", i );
                }
            }
        }
        if ( id.getAreasOfUse() != null && id.getAreasOfUse().length > 0 ) {
            for ( String i : id.getAreasOfUse() ) {
                if ( i != null ) {
                    XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "areaOfUse", i );
                }
            }
        }
    }

    /**
     * Export an axis to xml in the crs-definitions schema layout.
     * 
     * @param axis
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( Axis axis, Element currentNode ) {
        Document doc = currentNode.getOwnerDocument();
        Element axisElement = doc.createElementNS( CRS_URI, PRE + "Axis" );
        // The name.
        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "name", axis.getName() );

        // the units.
        Unit units = axis.getUnits();
        export( units, axisElement );

        XMLTools.appendElement( axisElement, CommonNamespaces.CRSNS, PRE + "axisOrientation",
                                axis.getOrientationAsString() );
        currentNode.appendChild( axisElement );
    }

    /**
     * Export a list of transformations to the crs element to xml with respect to the crs-definitions schema layout.
     * 
     * @param transformations
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( List<PolynomialTransformation> transformations, Element currentNode ) {
        for ( PolynomialTransformation transformation : transformations ) {
            Element transformationElement = XMLTools.appendElement( currentNode, CRSNS, PRE
                                                                                        + "polynomialTransformation" );
            if ( !"leastsquare".equals( transformation.getName().toLowerCase() ) ) {
                transformationElement.setAttribute( "class", transformation.getClass().getCanonicalName() );
            }
            Element transformElement = XMLTools.appendElement( transformationElement, CRSNS, PRE
                                                                                             + transformation.getName() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "polynomialOrder",
                                    Integer.toString( transformation.getOrder() ) );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "xParameters",
                                    transformation.getFirstParams().toString() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "yParameters",
                                    transformation.getSecondParams().toString() );
            XMLTools.appendElement( transformElement, CRSNS, PRE + "targetCRS",
                                    transformation.getTargetCRS().getIdentifier() );
        }
    }

    /**
     * Export a unit to xml in the crs-definitions schema layout.
     * 
     * @param units
     *            to be exported.
     * @param currentNode
     *            to export to.
     */
    private void export( Unit units, Element currentNode ) {
        if ( units != null && currentNode != null ) {
            XMLTools.appendElement( currentNode, CommonNamespaces.CRSNS, PRE + "units", units.getName() );
        }
    }

    /**
     * Parses all elements of the identifiable object.
     * 
     * @param element
     *            the xml-representation of the id-object
     * @return the identifiable object or <code>null</code> if no id was given.
     * @throws CRSConfigurationException
     */
    private Identifiable parseIdentifiable( Element element )
                            throws CRSConfigurationException {
        try {
            String[] identifiers = XMLTools.getNodesAsStrings( element, PRE + "id", nsContext );
            if ( identifiers == null || identifiers.length == 0 ) {
                String msg = Messages.getMessage( "CRS_CONFIG_NO_ID", ( ( element == null ) ? "null"
                                                                                           : element.getLocalName() ) );
                throw new CRSConfigurationException( msg );
            }
            for ( int i = 0; i < identifiers.length; ++i ) {
                if ( identifiers[i] != null ) {
                    identifiers[i] = identifiers[i].toUpperCase().trim();
                }
            }
            String[] names = XMLTools.getNodesAsStrings( element, PRE + "name", nsContext );
            String[] versions = XMLTools.getNodesAsStrings( element, PRE + "version", nsContext );
            String[] descriptions = XMLTools.getNodesAsStrings( element, PRE + "description", nsContext );
            String[] areasOfUse = XMLTools.getNodesAsStrings( element, PRE + "areaOfuse", nsContext );
            return new Identifiable( identifiers, names, versions, descriptions, areasOfUse );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "Identifiable",
                                                                      ( ( element == null ) ? "null"
                                                                                           : element.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
    }

    /**
     * Creates an axis array for the given crs element.
     * 
     * @param crsElement
     *            to be parsed
     * @return an Array of axis defining their order.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred, or the axisorder uses
     *             names which were not defined in the axis elements.
     */
    private Axis[] parseAxisOrder( Element crsElement )
                            throws CRSConfigurationException {
        String axisOrder = null;
        try {
            axisOrder = XMLTools.getRequiredNodeAsString( crsElement, PRE + "axisOrder", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "AxisOrder",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        if ( axisOrder == null || "".equals( axisOrder.trim() ) ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "AxisOrder",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      " axisOrder element may not be empty" ) );
        }
        axisOrder = axisOrder.trim();
        String[] order = axisOrder.trim().split( "," );
        Axis[] axis = new Axis[order.length];
        String XPATH = PRE + "Axis[" + PRE + "name = '";
        for ( int i = 0; i < order.length; ++i ) {
            String t = order[i];
            if ( t != null && !"".equals( t.trim() ) ) {
                t = t.trim();
                try {
                    Element axisElement = XMLTools.getRequiredElement( crsElement, XPATH + t + "']", nsContext );
                    String axisOrientation = XMLTools.getRequiredNodeAsString( axisElement, PRE + "axisOrientation",
                                                                               nsContext );
                    Unit unit = parseUnit( axisElement );
                    axis[i] = new Axis( unit, t, axisOrientation );
                } catch ( XMLParsingException e ) {
                    throw new CRSConfigurationException(
                                                         Messages.getMessage(
                                                                              "CRS_CONFIG_PARSE_ERROR",
                                                                              "Axis",
                                                                              ( ( crsElement == null ) ? "null"
                                                                                                      : crsElement.getLocalName() ),
                                                                              e.getMessage() ), e );
                }
            }
        }
        return axis;
    }

    /**
     * Parse all polynomial transformations for a given crs.
     * 
     * @param crsElement
     *            to parse the transformations for.
     * @return the list of transformations or the empty list if no transformations were found. Never <code>null</code>.
     */
    protected List<PolynomialTransformation> parseAlternativeTransformations( Element crsElement ) {
        List<Element> usedTransformations = null;
        try {
            usedTransformations = XMLTools.getElements( crsElement, PRE + "polynomialTransformation", nsContext );
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
        }
        List<PolynomialTransformation> result = new LinkedList<PolynomialTransformation>();
        if ( usedTransformations != null ) {
            for ( Element transformationElement : usedTransformations ) {
                PolynomialTransformation transform = getTransformation( transformationElement );
                if ( transform != null ) {
                    result.add( transform );
                }
            }
        }
        return result;
    }

    /**
     * Parses the transformation variables from the given crs:coordinatesystem/crs:polynomialTransformation element. If
     * the class attribute is given, this method will try to invoke an instance of the given class, if it fails
     * <code>null</code> will be returned.
     * 
     * @param transformationElement
     *            to parse the values from.
     * @return the instantiated transformation or <code>null</code> if it could not be instantiated.
     */
    protected PolynomialTransformation getTransformation( Element transformationElement ) {
        if ( transformationElement == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_INVALID_NULL_PARAMETER",
                                                                      "transformationElement" ) );
        }

        // order is not evaluated yet, because I do not know if it is required.
        // int order = -1;
        String tCRS = null;
        List<Double> aValues = new LinkedList<Double>();
        List<Double> bValues = new LinkedList<Double>();
        Element usedTransformation = null;
        try {
            usedTransformation = XMLTools.getRequiredElement( transformationElement, "*[1]", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "the transformation to use",
                                                                      transformationElement.getLocalName(),
                                                                      e.getLocalizedMessage() ), e );
        }

        try {
            // order = XMLTools.getNodeAsInt( usedTransformation, PRE + "polynomialOrder", nsContext, -1 );
            tCRS = XMLTools.getRequiredNodeAsString( usedTransformation, PRE + "targetCRS", nsContext );
            Element tmp = XMLTools.getRequiredElement( usedTransformation, PRE + "xParameters", nsContext );
            String tmpValues = XMLTools.getStringValue( tmp );
            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    aValues.add( Double.parseDouble( t ) );
                }
            }
            tmp = XMLTools.getRequiredElement( usedTransformation, PRE + "yParameters", nsContext );
            tmpValues = XMLTools.getStringValue( tmp );
            if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
                String[] split = tmpValues.split( "\\s" );
                for ( String t : split ) {
                    bValues.add( Double.parseDouble( t ) );
                }
            }
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
        }

        if ( tCRS == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "targetCRS",
                                                                      ( ( usedTransformation == null ) ? "null"
                                                                                                      : usedTransformation.getLocalName() ),
                                                                      "it is required and must denote a valid crs )" ) );
        }

        if ( aValues.size() == 0 || bValues.size() == 0 ) {
            throw new CRSConfigurationException(
                                                 "The polynomial variables (xParameters and yParameters element) defining the approximation to a given transformation function are required and may not be empty" );
        }

        CoordinateSystem targetCRS = getCRSByID( tCRS );

        PolynomialTransformation result = null;
        String name = usedTransformation.getLocalName().trim();
        String className = transformationElement.getAttribute( "class" );
        LOG.debug( "Trying to create transformation with name: " + name );
        if ( null != className && !"".equals( className.trim() ) ) {
            LOG.debug( "Trying to load user defined transformation class: " + className );
            try {
                Class<?> t = Class.forName( className );
                t.asSubclass( PolynomialTransformation.class );

                List<Element> children = XMLTools.getElements( usedTransformation, "*", nsContext );
                List<Element> otherValues = new LinkedList<Element>();
                for ( Element child : children ) {
                    if ( child != null ) {
                        String localName = child.getLocalName().trim();
                        if ( !( "targetCRS".equals( localName ) || "xParameters".equals( localName )
                                || "yParameters".equals( localName ) || "polynomialOrder".equals( localName ) ) ) {
                            otherValues.add( child );
                        }
                    }
                }
                /**
                 * Load the constructor with the standard projection values and the element list.
                 */
                /**
                 * For now, just load the constructor with the two lists and the crs class.
                 */
                Constructor<?> constructor = t.getConstructor( aValues.getClass(), bValues.getClass(),
                                                               targetCRS.getClass() );
                result = (PolynomialTransformation) constructor.newInstance( aValues, bValues, targetCRS );
            } catch ( ClassNotFoundException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( SecurityException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( NoSuchMethodException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( IllegalArgumentException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( InstantiationException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( IllegalAccessException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( InvocationTargetException e ) {
                LOG.error( e.getMessage(), e );
            } catch ( XMLParsingException e ) {
                // this will probably never happen.
                LOG.error( e.getMessage(), e );
            }
            if ( result == null ) {
                LOG.debug( "Loading of user defined transformation class: " + className + " was not successful" );
            }
        } else if ( "leastsquare".equalsIgnoreCase( name ) ) {
            float scaleX = 1;
            float scaleY = 1;
            try {
                scaleX = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleX", nsContext, 1 );
            } catch ( XMLParsingException e ) {
                LOG.error( "Could not parse scaleX from crs:leastsquare, because: " + e.getMessage(), e );
            }
            try {
                scaleY = (float) XMLTools.getNodeAsDouble( usedTransformation, PRE + "scaleY", nsContext, 1 );
            } catch ( XMLParsingException e ) {
                LOG.error( "Could not parse scaleY from crs:leastsquare, because: " + e.getMessage(), e );
            }
            result = new LeastSquareApproximation( aValues, bValues, targetCRS, scaleX, scaleY );
        }
        return result;
    }

    /**
     * Parses a unit from the given xml-parent.
     * 
     * @param parent
     *            xml-node to parse the unit from.
     * @return the unit object.
     * @throws CRSConfigurationException
     *             if the unit object could not be created.
     */
    private Unit parseUnit( Element parent )
                            throws CRSConfigurationException {
        String units = null;
        try {
            units = XMLTools.getNodeAsString( parent, PRE + "units", nsContext, null );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "units",
                                                                      ( ( parent == null ) ? "null"
                                                                                          : parent.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        Unit unit = Unit.createUnitFromString( units );
        if ( unit == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "units",
                                                                      ( ( parent == null ) ? "null"
                                                                                          : parent.getLocalName() ),
                                                                      "unknown unit: " + units ) );
        }
        return unit;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using chached datums, conversioninfos and projections).
     * @return a projected coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    private CoordinateSystem parseProjectedCRS( Element crsElement )
                            throws CRSConfigurationException {
        Identifiable id = parseIdentifiable( crsElement );
        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        // Unit units = parseUnit( crsElement );

        // String usedProjection = null;
        Element usedProjection = null;
        String usedGeographicCRS = null;
        try {
            // usedProjection = XMLTools.getRequiredNodeAsString( crsElement, PRE + "usedProjection", nsContext );
            usedProjection = XMLTools.getRequiredElement( crsElement, PRE + "projection", nsContext );
            usedGeographicCRS = XMLTools.getRequiredNodeAsString( crsElement, PRE + "usedGeographicCRS", nsContext );

        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projectiontType or usedGeographicCRS",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : id.getIdentifier() ),
                                                                      e.getMessage() ), e );

        }
        // first create the datum.
        if ( usedGeographicCRS == null || "".equals( usedGeographicCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
                                                                      "usedGeographicCRS", id.getIdentifier() ) );
        }
        GeographicCRS geoCRS = (GeographicCRS) getCRSByID( usedGeographicCRS );
        if ( geoCRS == null || geoCRS.getType() != CoordinateSystem.GEOGRAPHIC_CRS ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_CRSREF",
                                                                      id.getIdentifier(), usedGeographicCRS ) );
        }

        // // then the projection.
        // if ( usedProjection == null || "".equals( usedProjection.trim() ) ) {
        // throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY",
        // "projectionType", id.getIdentifier() ) );
        // }
        // Projection projection = getProjectionByID( usedProjection, (GeographicCRS) geoCRS, axis[0].getUnits() );
        Projection projection = getProjection( usedProjection, geoCRS, axis[0].getUnits() );
        if ( projection == null ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PROJECTEDCRS_FALSE_PROJREF",
                                                                      id.getIdentifier(), usedProjection ) );
        }

        ProjectedCRS result = new ProjectedCRS( transformations, projection, axis, id );
        for ( String s : id.getIdentifiers() ) {
            LOG.debug( "Adding id: " + s + "to projected crs cache." );
            projectedCRSs.put( s, result );
        }
        if ( checkForDoubleDefinition ) {
            result = checkForUniqueness( cachedProjCRSs, doubleProjCRS, result );
        }
        // remove the child, thus resulting in a smaller xml-tree.
        rootElement.removeChild( crsElement );
        LOG.debug( "Returning projected crs: " + result );

        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using cached datums, conversioninfos and projections).
     * 
     * @return a geographic coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    private CoordinateSystem parseGeographicCRS( Element crsElement )
                            throws CRSConfigurationException {
        Identifiable id = parseIdentifiable( crsElement );
        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        // get the datum
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getIdentifier() );

        GeographicCRS result = new GeographicCRS( transformations, usedDatum, axis, id );
        for ( String s : id.getIdentifiers() ) {
            LOG.debug( "Adding id: " + s + "to geographic crs cache." );
            geographicCRSs.put( s, result );
        }
        if ( checkForDoubleDefinition ) {
            result = checkForUniqueness( cachedGeoCRSs, doubleGeos, result );
        }
        // remove the child, thus resulting in a smaller dom tree.
        rootElement.removeChild( crsElement );
        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created (using cached datums, conversioninfos and projections).
     * @return a geocentric coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    private CoordinateSystem parseGeocentricCRS( Element crsElement )
                            throws CRSConfigurationException {
        Identifiable id = parseIdentifiable( crsElement );
        Axis[] axis = parseAxisOrder( crsElement );
        List<PolynomialTransformation> transformations = parseAlternativeTransformations( crsElement );
        GeodeticDatum usedDatum = parseReferencedGeodeticDatum( crsElement, id.getIdentifier() );
        GeocentricCRS result = new GeocentricCRS( transformations, usedDatum, axis, id );
        for ( String identifier : id.getIdentifiers() ) {
            geocentricCRSs.put( identifier, result );
        }
        if ( checkForDoubleDefinition ) {
            result = checkForUniqueness( cachedGeocentricCRSs, doubleGeocentricCRSs, result );
        }
        rootElement.removeChild( crsElement );
        return result;
    }

    /**
     * @param crsElement
     *            from which the crs is to be created.
     * 
     * @return a compound coordinatesystem based on the given xml-element.
     * @throws CRSConfigurationException
     *             if a required element could not be found, or an xmlParsingException occurred.
     */
    private CoordinateSystem parseCompoundCRS( Element crsElement ) {
        Identifiable id = parseIdentifiable( crsElement );

        String usedCRS = null;
        try {
            usedCRS = XMLTools.getRequiredNodeAsString( crsElement, PRE + "usedCRS", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "usedCRS",
                                                                      ( ( crsElement == null ) ? "null"
                                                                                              : crsElement.getLocalName() ),
                                                                      e.getMessage() ), e );

        }
        if ( usedCRS == null || "".equals( usedCRS.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedCRS",
                                                                      id.getIdentifier() ) );
        }
        CoordinateSystem usedCoordinateSystem = getCRSByID( usedCRS );
        if ( usedCoordinateSystem == null
             || ( usedCoordinateSystem.getType() != CoordinateSystem.GEOGRAPHIC_CRS && usedCoordinateSystem.getType() != CoordinateSystem.PROJECTED_CRS ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_COMPOUND_FALSE_CRSREF",
                                                                      id.getIdentifier(), usedCRS ) );
        }

        // get the datum
        Axis heigtAxis = null;
        double defaultHeight = 0;
        try {
            Element axisElement = XMLTools.getRequiredElement( crsElement, PRE + "heightAxis", nsContext );
            String axisName = XMLTools.getRequiredNodeAsString( axisElement, PRE + "name", nsContext );
            String axisOrientation = XMLTools.getRequiredNodeAsString( axisElement, PRE + "axisOrientation", nsContext );
            Unit unit = parseUnit( axisElement );
            heigtAxis = new Axis( unit, axisName, axisOrientation );
            defaultHeight = XMLTools.getNodeAsDouble( crsElement, PRE + "defaultHeight", nsContext, 0 );
        } catch ( XMLParsingException e ) {
            LOG.error( e.getMessage(), e );
            throw new CRSConfigurationException( e.getMessage() );
        }

        CompoundCRS result = new CompoundCRS( heigtAxis, usedCoordinateSystem, defaultHeight, id );
        for ( String s : id.getIdentifiers() ) {
            LOG.debug( "Adding id: " + s + "to compound crs cache." );
            compoundCRSs.put( s, result );
        }
        if ( checkForDoubleDefinition ) {
            result = checkForUniqueness( cachedCompoundCRSs, doubleCompoundCRSs, result );
        }
        // remove the child, thus resulting in a smaller dom tree.
        rootElement.removeChild( crsElement );
        return result;
    }

    /**
     * Reads an element from the configuration with xpath *[crs:id='givenID'] and returns the found element.
     * 
     * @param id
     *            to search for
     * @return the element or <code>null</code> if no such element was found.
     */
    private Element getTopElementFromID( String id ) {
        if ( rootElement == null ) {
            LOG.debug( "The Root element is null, hence no crs's are available" );
            return null;
        }
        Element crsElement = null;
        // String xPath ="//*[crs:id='EPSG:31466']";
        String xPath = "*[" + PRE + "id='" + id + "']";
        try {
            crsElement = XMLTools.getElement( rootElement, xPath, nsContext );
        } catch ( XMLParsingException e ) {
            LOG.error( Messages.getMessage( "CRS_CONFIG_NO_RESULT_FOR_ID", id, e.getMessage() ), e );
        }
        LOG.debug( "Trying to find elements with xpath: " + xPath
                      + ( ( crsElement == null ) ? " [failed]" : " [success]" ) );
        return crsElement;
    }

    /**
     * Parses the required usedDatum element from the given parentElement (probably a crs element).
     * 
     * @param parentElement
     *            to parse the required usedDatum element from.
     * @param parentID
     *            optional for an appropriate error message.
     * @return the Datum.
     * @throws CRSConfigurationException
     *             if a parsing error occurred, the node was not defined or an illegal id reference (not found) was
     *             given.
     */
    private GeodeticDatum parseReferencedGeodeticDatum( Element parentElement, String parentID )
                            throws CRSConfigurationException {
        String datumID = null;
        try {
            datumID = XMLTools.getRequiredNodeAsString( parentElement, PRE + "usedDatum", nsContext );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage(
                                                                      "CRS_CONFIG_PARSE_ERROR",
                                                                      "datumID",
                                                                      ( ( parentElement == null ) ? "null"
                                                                                                 : parentElement.getLocalName() ),
                                                                      e.getMessage() ), e );
        }
        if ( datumID == null || "".equals( datumID.trim() ) ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_REFERENCE_ID_IS_EMPTY", "usedDatum",
                                                                      parentID ) );
        }
        GeodeticDatum usedDatum = getGeodeticDatumFromID( datumID );
        if ( usedDatum == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_USEDDATUM_IS_NULL", datumID, parentID ) );
        }
        return usedDatum;
    }

    /**
     * @param datumID
     * @return the
     * @throws CRSConfigurationException
     */
    private GeodeticDatum getGeodeticDatumFromID( String datumID )
                            throws CRSConfigurationException {
        if ( datumID != null && !"".equals( datumID.trim() ) ) {
            datumID = datumID.trim();
            if ( datums.containsKey( datumID ) && datums.get( datumID ) != null ) {
                GeodeticDatum r = datums.get( datumID );
                if ( checkForDoubleDefinition ) {
                    if ( cachedDatums.contains( r ) ) {
                        int index = cachedDatums.indexOf( r );
                        r = cachedDatums.get( index );
                    }
                }
                return r;
            }
            Element datumElement = getTopElementFromID( datumID );
            if ( datumElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "datum", datumID ) );
            }
            // get the identifiable.
            Identifiable id = parseIdentifiable( datumElement );

            // get the ellipsoid.
            Ellipsoid ellipsoid = null;
            try {
                String ellipsID = XMLTools.getRequiredNodeAsString( datumElement, PRE + "usedEllipsoid", nsContext );
                if ( ellipsID != null && !"".equals( ellipsID.trim() ) ) {
                    ellipsoid = getEllipsoidFromID( ellipsID );
                }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "usedEllipsoid",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }

            // get the primemeridian if any.
            PrimeMeridian pMeridian = null;
            try {
                String pMeridianID = XMLTools.getNodeAsString( datumElement, PRE + "usedPrimeMeridian", nsContext, null );
                if ( pMeridianID != null && !"".equals( pMeridianID.trim() ) ) {
                    pMeridian = getPrimeMeridianFromID( pMeridianID );
                }
                if ( pMeridian == null ) {
                    pMeridian = PrimeMeridian.GREENWICH;
                }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                          "usedPrimeMeridian",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }

            // get the WGS84 if any.
            WGS84ConversionInfo cInfo = null;
            try {
                String infoID = XMLTools.getNodeAsString( datumElement, PRE + "usedWGS84ConversionInfo", nsContext,
                                                          null );
                if ( infoID != null && !"".equals( infoID.trim() ) ) {
                    cInfo = getConversionInfoFromID( infoID );
                }
                if ( cInfo == null ) {
                    cInfo = new WGS84ConversionInfo( "Created by DeegreeCRSProvider" );
                }
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException(
                                                     Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                          "wgs84ConversionInfo",
                                                                          datumElement.getLocalName(), e.getMessage() ),
                                                     e );
            }
            if ( ellipsoid == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_DATUM_HAS_NO_ELLIPSOID", datumID ) );
            }
            GeodeticDatum result = new GeodeticDatum( ellipsoid, pMeridian, cInfo, id.getIdentifiers(), id.getNames(),
                                                      id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            for ( String s : id.getIdentifiers() ) {
                datums.put( s, result );
            }
            if ( checkForDoubleDefinition ) {
                result = checkForUniqueness( cachedDatums, doubleDatums, result );
            }

            // remove the datum from the xml-tree.
            rootElement.removeChild( datumElement );
            return result;
        }

        return null;
    }

    /**
     * @param meridianID
     *            the id to search for.
     * @return the primeMeridian with given id or <code>null</code>
     * @throws CRSConfigurationException
     *             if the longitude was not set or the units could not be parsed.
     */
    private PrimeMeridian getPrimeMeridianFromID( String meridianID )
                            throws CRSConfigurationException {
        if ( meridianID != null && !"".equals( meridianID.trim() ) ) {
            if ( primeMeridians.containsKey( meridianID ) && primeMeridians.get( meridianID ) != null ) {
                PrimeMeridian r = primeMeridians.get( meridianID );
                if ( checkForDoubleDefinition ) {
                    if ( cachedMeridians.contains( r ) ) {
                        int index = cachedMeridians.indexOf( r );
                        r = cachedMeridians.get( index );
                    }
                }
                return r;
            }
            Element meridianElement = getTopElementFromID( meridianID );
            if ( meridianElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "primeMeridian",
                                                                          meridianID ) );
            }
            Identifiable id = parseIdentifiable( meridianElement );
            Unit units = parseUnit( meridianElement );
            double longitude = 0;
            try {
                longitude = XMLTools.getRequiredNodeAsDouble( meridianElement, PRE + "longitude", nsContext );
                boolean inDegrees = XMLTools.getNodeAsBoolean( meridianElement, PRE + "longitude/@inDegrees",
                                                               nsContext, true );
                longitude = ( longitude != 0 && inDegrees ) ? Math.toRadians( longitude ) : longitude;
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "longitude",
                                                                          meridianElement.getLocalName(),
                                                                          e.getMessage() ), e );
            }
            PrimeMeridian result = new PrimeMeridian( units, longitude, id.getIdentifiers(), id.getNames(),
                                                      id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
            for ( String s : id.getIdentifiers() ) {
                primeMeridians.put( s, result );
            }
            if ( checkForDoubleDefinition ) {
                result = checkForUniqueness( cachedMeridians, doubleMeridians, result );
            }
            // remove the prime meridian, thus resulting in a smaller xml-tree.
            rootElement.removeChild( meridianElement );
            return result;
        }
        return null;
    }

    /**
     * Tries to find a cached ellipsoid, if not found, the config will be checked.
     * 
     * @param ellipsoidID
     * @return an ellipsoid or <code>null</code> if no ellipsoid with given id was found, or the id was
     *         <code>null</code> or empty.
     * @throws CRSConfigurationException
     *             if something went wrong.
     */
    private Ellipsoid getEllipsoidFromID( String ellipsoidID )
                            throws CRSConfigurationException {
        if ( ellipsoidID != null && !"".equals( ellipsoidID.trim() ) ) {
            if ( ellipsoids.containsKey( ellipsoidID ) && ellipsoids.get( ellipsoidID ) != null ) {
                Ellipsoid r = ellipsoids.get( ellipsoidID );
                if ( checkForDoubleDefinition ) {
                    if ( cachedEllipsoids.contains( r ) ) {
                        int index = cachedEllipsoids.indexOf( r );
                        r = cachedEllipsoids.get( index );
                    }
                }
                return r;
            }
            Element ellipsoidElement = getTopElementFromID( ellipsoidID );
            if ( ellipsoidElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT", "ellipsoid",
                                                                          ellipsoidID ) );
            }
            Identifiable id = parseIdentifiable( ellipsoidElement );
            try {
                double semiMajor = XMLTools.getRequiredNodeAsDouble( ellipsoidElement, PRE + "semiMajorAxis", nsContext );

                Unit units = parseUnit( ellipsoidElement );
                double inverseFlattening = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "inverseFlatting",
                                                                     nsContext, Double.NaN );
                double eccentricity = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "eccentricity", nsContext,
                                                                Double.NaN );
                double semiMinorAxis = XMLTools.getNodeAsDouble( ellipsoidElement, PRE + "semiMinorAxis", nsContext,
                                                                 Double.NaN );
                if ( Double.isNaN( inverseFlattening ) && Double.isNaN( eccentricity ) && Double.isNaN( semiMinorAxis ) ) {
                    throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_ELLIPSOID_MISSES_PARAM",
                                                                              ellipsoidID ) );
                }

                Ellipsoid result = null;
                if ( !Double.isNaN( inverseFlattening ) ) {
                    result = new Ellipsoid( semiMajor, units, inverseFlattening, id.getIdentifiers(), id.getNames(),
                                            id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
                } else if ( !Double.isNaN( eccentricity ) ) {
                    result = new Ellipsoid( semiMajor, eccentricity, units, id.getIdentifiers(), id.getNames(),
                                            id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
                } else {
                    result = new Ellipsoid( units, semiMajor, semiMinorAxis, id.getIdentifiers(), id.getNames(),
                                            id.getVersions(), id.getDescriptions(), id.getAreasOfUse() );
                }
                for ( String s : id.getIdentifiers() ) {
                    ellipsoids.put( s, result );
                }
                if ( checkForDoubleDefinition ) {
                    result = checkForUniqueness( cachedEllipsoids, doubleEllipsoids, result );
                }
                // remove the ellipsoid from the xml-tree.
                rootElement.removeChild( ellipsoidElement );
                return result;
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "ellipsoid",
                                                                          ellipsoidElement.getLocalName(),
                                                                          e.getMessage() ), e );
            }
        }

        return null;
    }

    /**
     * @param infoID
     *            to get the conversioninfo from.
     * @return the configured wgs84 conversion info parameters.
     * @throws CRSConfigurationException
     */
    private WGS84ConversionInfo getConversionInfoFromID( String infoID )
                            throws CRSConfigurationException {
        if ( infoID != null && !"".equals( infoID.trim() ) ) {
            if ( conversionInfos.containsKey( infoID ) && conversionInfos.get( infoID ) != null ) {
                WGS84ConversionInfo r = conversionInfos.get( infoID );
                if ( checkForDoubleDefinition ) {
                    if ( cachedToWGS.contains( r ) ) {
                        int index = cachedToWGS.indexOf( r );
                        r = cachedToWGS.get( index );
                    }
                }
                return r;
            }
            Element cInfoElement = getTopElementFromID( infoID );
            if ( cInfoElement == null ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_NO_ELEMENT",
                                                                          "wgs84ConversionInfo", infoID ) );
            }
            Identifiable identifiable = parseIdentifiable( cInfoElement );
            double xT = 0, yT = 0, zT = 0, xR = 0, yR = 0, zR = 0, scale = 0;
            try {
                xT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "xAxisTranslation", nsContext, 0 );
                yT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "yAxisTranslation", nsContext, 0 );
                zT = XMLTools.getNodeAsDouble( cInfoElement, PRE + "zAxisTranslation", nsContext, 0 );
                xR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "xAxisRotation", nsContext, 0 );
                yR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "yAxisRotation", nsContext, 0 );
                zR = XMLTools.getNodeAsDouble( cInfoElement, PRE + "zAxisRotation", nsContext, 0 );
                scale = XMLTools.getNodeAsDouble( cInfoElement, PRE + "scaleDifference", nsContext, 0 );
            } catch ( XMLParsingException e ) {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "conversionInfo",
                                                                          "definitions", e.getMessage() ), e );
            }
            WGS84ConversionInfo result = new WGS84ConversionInfo( xT, yT, zT, xR, yR, zR, scale, identifiable );
            for ( String id : identifiable.getIdentifiers() ) {
                conversionInfos.put( id, result );
            }
            if ( checkForDoubleDefinition ) {
                result = checkForUniqueness( cachedToWGS, doubleToWGS, result );
            }
            // remove the child, thus resulting in a smaller xml-tree.
            rootElement.removeChild( cInfoElement );
            return result;

        }
        return null;
    }

    /**
     * Parses and instantiates the projection from the given element.
     * 
     * @param projectionElement
     *            to create the projection from.
     * @param underlyingCRS
     *            of the projected crs
     * @param units
     *            of the projected crs
     * @return the configured projection or <code>null</code> if not defined or found.
     * @throws CRSConfigurationException
     */
    protected Projection getProjection( Element projectionElement, GeographicCRS underlyingCRS, Unit units )
                            throws CRSConfigurationException {
        if ( projectionElement == null ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_INVALID_NULL_PARAMETER", "projectionElement" ) );
        }
        try {
            Element usedProjection = XMLTools.getRequiredElement( projectionElement, "*[1]", nsContext );
            // All projections will have following parameters
            double latitudeOfNaturalOrigin = XMLTools.getNodeAsDouble( usedProjection, PRE + "latitudeOfNaturalOrigin",
                                                                       nsContext, 0 );
            boolean inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "latitudeOfNaturalOrigin/@inDegrees",
                                                           nsContext, true );
            latitudeOfNaturalOrigin = ( latitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( latitudeOfNaturalOrigin )
                                                                                   : latitudeOfNaturalOrigin;

            double longitudeOfNaturalOrigin = XMLTools.getNodeAsDouble( usedProjection, PRE
                                                                                        + "longitudeOfNaturalOrigin",
                                                                        nsContext, 0 );
            inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "longitudeOfNaturalOrigin/@inDegrees",
                                                   nsContext, true );
            longitudeOfNaturalOrigin = ( longitudeOfNaturalOrigin != 0 && inDegrees ) ? Math.toRadians( longitudeOfNaturalOrigin )
                                                                                     : longitudeOfNaturalOrigin;

            double scaleFactor = XMLTools.getNodeAsDouble( usedProjection, PRE + "scaleFactor", nsContext, 0 );
            double falseEasting = XMLTools.getNodeAsDouble( usedProjection, PRE + "falseEasting", nsContext, 0 );
            double falseNorthing = XMLTools.getNodeAsDouble( usedProjection, PRE + "falseNorthing", nsContext, 0 );

            String projectionName = usedProjection.getLocalName().trim();
            String className = projectionElement.getAttribute( "class" );
            Point2d naturalOrigin = new Point2d( longitudeOfNaturalOrigin, latitudeOfNaturalOrigin );
            Projection result = null;
            if ( className != null && !"".equals( className.trim() ) ) {
                LOG.debug( "Trying to load user defined projection class: " + className );
                try {
                    Class<?> t = Class.forName( className );
                    t.asSubclass( Projection.class );
                    System.out.println( "t: " + t );
                    /**
                     * try to get a constructor with a native type as a parameter, by going over the 'names' of the
                     * classes of the parameters, the native type will show up as the typename e.g. int or long.....
                     * <code>
                     * public Projection( GeographicCRS geographicCRS, double falseNorthing, double falseEasting,
                     * Point2d naturalOrigin, Unit units, double scale, boolean conformal, boolean equalArea ) 
                     * </code>
                     */
                    List<Element> children = XMLTools.getElements( usedProjection, "*", nsContext );
                    List<Element> otherValues = new LinkedList<Element>();
                    for ( Element child : children ) {
                        if ( child != null ) {
                            String localName = child.getLocalName().trim();
                            if ( !( "latitudeOfNaturalOrigin".equals( localName )
                                    || "longitudeOfNaturalOrigin".equals( localName )
                                    || "scaleFactor".equals( localName ) || "falseEasting".equals( localName ) || "falseNorthing".equals( localName ) ) ) {
                                otherValues.add( child );
                            }
                        }
                    }
                    /**
                     * Load the constructor with the standard projection values and the element list.
                     */
                    Constructor<?> constructor = t.getConstructor( GeographicCRS.class, double.class, double.class,
                                                                   Point2d.class, Unit.class, double.class, List.class );
                    result = (Projection) constructor.newInstance( underlyingCRS, falseNorthing, falseEasting,
                                                                   naturalOrigin, units, scaleFactor, otherValues );
                } catch ( ClassNotFoundException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( SecurityException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( NoSuchMethodException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( IllegalArgumentException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( InstantiationException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( IllegalAccessException e ) {
                    LOG.error( e.getMessage(), e );
                } catch ( InvocationTargetException e ) {
                    LOG.error( e.getMessage(), e );
                }
                if ( result == null ) {
                    LOG.debug( "Loading of user defined transformation class: " + className + " was not successful" );
                }

            } else {
                // no selfdefined projection, try one of the following, for the projection specific parameters, if any.
                if ( "transverseMercator".equalsIgnoreCase( projectionName ) ) {
                    // change schema to let projection be identifiable. fix method geodetic
                    boolean northernHemi = XMLTools.getNodeAsBoolean( usedProjection, PRE + "northernHemisphere",
                                                                      nsContext, true );
                    result = new TransverseMercator( northernHemi, underlyingCRS, falseNorthing, falseEasting,
                                                     naturalOrigin, units, scaleFactor );
                } else if ( "lambertAzimuthalEqualArea".equalsIgnoreCase( projectionName ) ) {
                    result = new LambertAzimuthalEqualArea( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                            units, scaleFactor );
                } else if ( "lambertConformalConic".equalsIgnoreCase( projectionName ) ) {
                    double firstP = XMLTools.getNodeAsDouble( usedProjection, PRE + "firstParallelLatitude", nsContext,
                                                              Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "firstParallelLatitude/@inDegrees",
                                                           nsContext, true );
                    firstP = ( !Double.isNaN( firstP ) && inDegrees ) ? Math.toRadians( firstP ) : firstP;

                    double secondP = XMLTools.getNodeAsDouble( usedProjection, PRE + "secondParallelLatitude",
                                                               nsContext, Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "secondParallelLatitude/@inDegrees",
                                                           nsContext, true );
                    secondP = ( !Double.isNaN( secondP ) && inDegrees ) ? Math.toRadians( secondP ) : secondP;
                    result = new LambertConformalConic( firstP, secondP, underlyingCRS, falseNorthing, falseEasting,
                                                        naturalOrigin, units, scaleFactor );
                } else if ( "stereographicAzimuthal".equalsIgnoreCase( projectionName ) ) {
                    double trueScaleL = XMLTools.getNodeAsDouble( usedProjection, PRE + "trueScaleLatitude", nsContext,
                                                                  Double.NaN );
                    inDegrees = XMLTools.getNodeAsBoolean( usedProjection, PRE + "trueScaleLatitude/@inDegrees",
                                                           nsContext, true );
                    trueScaleL = ( !Double.isNaN( trueScaleL ) && inDegrees ) ? Math.toRadians( trueScaleL )
                                                                             : trueScaleL;
                    result = new StereographicAzimuthal( trueScaleL, underlyingCRS, falseNorthing, falseEasting,
                                                         naturalOrigin, units, scaleFactor );
                } else if ( "StereographicAlternative".equalsIgnoreCase( projectionName ) ) {
                    result = new StereographicAlternative( underlyingCRS, falseNorthing, falseEasting, naturalOrigin,
                                                           units, scaleFactor );
                } else {
                    throw new CRSConfigurationException(
                                                         Messages.getMessage(
                                                                              "CRS_CONFIG_PROJECTEDCRS_INVALID_PROJECTION",
                                                                              projectionName,
                                                                              "StereographicAlternative, stereographicAzimuthal, lambertConformalConic, lambertAzimuthalEqualArea, transverseMercator" ) );

                }
            }
            return result;
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException(
                                                 Messages.getMessage( "CRS_CONFIG_PARSE_ERROR",
                                                                      "projection parameters",
                                                                      projectionElement.getLocalName(), e.getMessage() ),
                                                 e );

        }
    }

    /**
     * @param <T>
     *            should be at least of Type Identifiable.
     * @param uniqueList
     *            to check against
     * @param mapping
     *            to added the id of T to if it is found duplicate.
     * @param toBeChecked
     *            to check.
     * @return the cached T if found or the given identifiable.
     */
    private <T extends Identifiable> T checkForUniqueness( List<T> uniqueList, Map<String, String> mapping,
                                                           T toBeChecked ) {
        T result = toBeChecked;
        if ( uniqueList.contains( toBeChecked ) ) {
            int index = uniqueList.indexOf( toBeChecked );
            LOG.info( "The Identifiable with id: " + toBeChecked.getIdentifier() + " was found to be equal with: "
                         + uniqueList.get( index ).getIdentifier() );
            String key = uniqueList.get( index ).getIdentifier();
            boolean updatedEPSG = false;
            if ( key != null && !"".equals( key.trim() ) ) {
                String value = mapping.get( key );
                String tbcID = toBeChecked.getIdentifier().toUpperCase();
                // it would be nicest to get the epsg code if any.
                if ( !key.toUpperCase().startsWith( "EPSG:" ) && tbcID.startsWith( "EPSG:" ) ) {
                    if ( value == null || "".equals( value ) ) {
                        value = key;
                    } else {
                        value += ", " + key;
                    }
                    updatedEPSG = true;
                    mapping.remove( key );
                    key = toBeChecked.getIdentifier();
                } else {
                    if ( value == null || "".equals( value ) ) {
                        value = toBeChecked.getIdentifier();
                    } else {
                        value += ", " + toBeChecked.getIdentifier();
                    }
                }
                mapping.put( key, value );
            }
            // if updated to epsg, cache the epsg instead and remove the old identifiable.
            if ( updatedEPSG ) {
                uniqueList.remove( index );
                uniqueList.add( toBeChecked );
            } else {
                result = uniqueList.get( index );
            }
        } else {
            LOG.debug( "Adding: " + toBeChecked.getIdentifier() + " to cache." );
            uniqueList.add( toBeChecked );
        }
        return result;
    }
}
