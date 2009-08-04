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

package org.deegree.crs.configuration.deegree.xml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.crs.CRSCodeType;
import org.deegree.crs.CRSIdentifiable;
import org.deegree.crs.configuration.AbstractCRSProvider;
import org.deegree.crs.coordinatesystems.CoordinateSystem;
import org.deegree.crs.exceptions.CRSConfigurationException;
import org.deegree.crs.i18n.Messages;
import org.deegree.crs.projections.Projection;
import org.deegree.crs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * <li>It must be a sub class of {@link org.deegree.crs.projections.Projection}</li>
 * <li>A constructor with following signature must be supplied: <br/> <code>
 * public MyProjection( <br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.crs.coordinatesystems.GeographicCRS} underlyingCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseNorthing,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseEasting,<br/>
 * &emsp;&emsp;&emsp;&emsp;javax.vecmath.Point2d naturalOrigin,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.crs.components.Unit} units,<br/>
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
 * <li>It must be a sub class of {@link org.deegree.crs.transformations.polynomial.PolynomialTransformation}</li>
 * <li>A constructor with following signature must be supplied: <br/> <code>
 * public MyTransformation( <br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; aValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; bValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.crs.coordinatesystems.CoordinateSystem} targetCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourTransformationElements<br/>
 * );<br/>
 * </code>
 * <p>
 * The first three parameters are common to all polynomial values (for an explanation of their meaning take a look at
 * {@link org.deegree.crs.transformations.polynomial.PolynomialTransformation}). Again, the last list, will contain all
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

public class DeegreeCRSProvider extends AbstractCRSProvider<OMElement> {

    private static Logger LOG = LoggerFactory.getLogger( DeegreeCRSProvider.class );

    private CRSExporter exporter;

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
                String className = "org.deegree.crs.configuration.deegree.xml.CRSParser_" + version;
                try {
                    Class<?> tClass = Class.forName( className );
                    tClass.asSubclass( CRSParser.class );
                    LOG.debug( "Trying to load configured CRS provider from classname: " + className );
                    Constructor<?> constructor = tClass.getConstructor( this.getClass(), Properties.class,
                                                                        OMElement.class );
                    if ( constructor == null ) {
                        LOG.error( "No constructor ( " + this.getClass() + ", Properties.class) found in class:"
                                   + className );
                    } else {
                        versionedParser = (CRSParser) constructor.newInstance( this, new Properties( properties ),
                                                                               versionedParser.getRootElement() );
                    }
                    className = "org.deegree.crs.configuration.deegree.xml.CRSExporter_" + version;
                    tClass = Class.forName( className );
                    tClass.asSubclass( CRSExporter.class );
                    LOG.debug( "Trying to load configured CRS exporter for version: " + version + " from classname: "
                               + className );
                    constructor = tClass.getConstructor( Properties.class );
                    if ( constructor == null ) {
                        LOG.error( "No constructor ( Properties.class ) found in class:" + className );
                    } else {
                        exporter = (CRSExporter) constructor.newInstance( new Properties( properties ) );
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

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        factory.setProperty( "javax.xml.stream.isRepairingNamespaces", Boolean.TRUE );

        try {
            XMLStreamWriter xmlWriter = new FormattingXMLStreamWriter( factory.createXMLStreamWriter( writer ) );
            exporter.export( crsToExport, xmlWriter );

            sb.append( out.toString( Charset.defaultCharset().displayName() ) );
        } catch ( UnsupportedEncodingException e ) {
            LOG.error( e.getLocalizedMessage(), e );
        } catch ( XMLStreamException e ) {
            LOG.error( "Error while exporting the coordinates: " + e.getLocalizedMessage(), e );
        }

    }

    /**
     * @return the casted resolver of the super class.
     */
    @Override
    public CRSParser getResolver() {
        return (CRSParser) super.getResolver();
    }

    public List<CRSCodeType> getAvailableCRSCodes() {
        return getResolver().getAvailableCRSCodes();
    }

    public List<CoordinateSystem> getAvailableCRSs() {
        List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
        // List<OMElement> allCRSIDs = getResolver().getAvailableCRSs();
        List<CRSCodeType> allCRSIDs = getResolver().getAvailableCRSCodes();
        final int total = allCRSIDs.size();
        int count = 0;
        int percentage = (int) Math.round( total / 100.d );
        int number = 0;
        LOG.info( "Trying to create a total of " + total + " coordinate systems." );        
        for ( CRSCodeType crsID : allCRSIDs ) {
            if ( crsID != null ) {
                // String id = crsID.getTextContent();
                String id = crsID.getOriginal();
                if ( id != null && !"".equals( id.trim() ) ) {
                    if ( count++ % percentage == 0 ) {
                        System.out.println( ( number ) + ( ( number++ < 10 ) ? " " : "" ) + "% created" );
                    }
                    boolean createdAlready = false;
                    for ( int i = 0; i < allSystems.size() && !createdAlready; ++i ) {
                        CoordinateSystem c = allSystems.get( i );
                        createdAlready = ( c != null && c.hasCode( CRSCodeType.valueOf( id ) ) );
                    }
                    if ( !createdAlready ) {
                        allSystems.add( getCRSByCode( CRSCodeType.valueOf( id ) ) );
                    }
                }
            }
        }
        System.out.println();
        return allSystems;
    }

    @Override
    protected CoordinateSystem parseCoordinateSystem( OMElement crsDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseCoordinateSystem( crsDefinition );
    }

    @Override
    public Transformation parseTransformation( OMElement transformationDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseTransformation( transformationDefinition );

    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return getResolver().getTransformation( sourceCRS, targetCRS );
    }

    @Override
    public CoordinateSystem getCRSByCode( CRSCodeType id )
                            throws CRSConfigurationException {
        CRSParser resolver = getResolver();
        try {
            return resolver.parseCoordinateSystem( resolver.getURIAsType( id.getOriginal() ) );
        } catch ( IOException e ) {
            LOG.error( e.getMessage(), e );
        }
        return null;
    }

    @Override
    public CRSIdentifiable getIdentifiable( CRSCodeType id )
                            throws CRSConfigurationException {
        // TODO Auto-generated method stub
        return null;
    }

}
