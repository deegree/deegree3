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

package org.deegree.cs.configuration.deegree.xml;

import static java.lang.System.currentTimeMillis;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.stax.FormattingXMLStreamWriter;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.components.Ellipsoid;
import org.deegree.cs.components.GeodeticDatum;
import org.deegree.cs.components.PrimeMeridian;
import org.deegree.cs.configuration.AbstractCRSProvider;
import org.deegree.cs.configuration.CRSConfiguration;
import org.deegree.cs.configuration.CRSProvider;
import org.deegree.cs.configuration.deegree.xml.exporters.CRSExporterBase;
import org.deegree.cs.configuration.deegree.xml.om.Parser;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.projections.Projection;
import org.deegree.cs.transformations.Transformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code>DeegreeCRSProvider</code> reads the deegree crs-config (based on it's own xml-schema) and creates the CRS's
 * (and their datums, conversion info's, ellipsoids and projections) if requested.
 * <p>
 * Attention, although urn's are case-sensitive, the deegreeCRSProvider is not. All incoming id's are toLowerCased!
 * </p>
 * <h2>Automatic loading of projection/transformation classes</h2> It is possible to create your own
 * projection/transformation classes, which can be automatically loaded.
 * <p>
 * You can achieve this loading by supplying the <b><code>class</code></b> attribute to a
 * <code>crs:projectedCRS/crs:projection</code> or <code>crs:coordinateSystem/crs:transformation</code> element in the
 * 'deegree-crs-configuration.xml'. This attribute must contain the full class name (with package), e.g.
 * &lt;crs:projection class='my.package.and.projection.Implementation'&gt;
 * </p>
 * Because the loading is done with reflections your classes must sustain following criteria: <h3>Projections</h3>
 * <ol>
 * <li>It must be a sub class of {@link org.deegree.cs.projections.Projection}</li>
 * <li>A constructor with following signature must be supplied: <br/>
 * <code>
 * public MyProjection( <br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.coordinatesystems.GeographicCRS} underlyingCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseNorthing,<br/>
 * &emsp;&emsp;&emsp;&emsp;double falseEasting,<br/>
 * &emsp;&emsp;&emsp;&emsp;javax.vecmath.Point2d naturalOrigin,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.components.Unit} units,<br/>
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
 * <li>It must be a sub class of {@link org.deegree.cs.transformations.polynomial.PolynomialTransformation}</li>
 * <li>A constructor with following signature must be supplied: <br/>
 * <code>
 * public MyTransformation( <br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; aValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.list&lt;Double&gt; bValues,<br/>
 * &emsp;&emsp;&emsp;&emsp;{@link org.deegree.cs.coordinatesystems.CoordinateSystem} targetCRS,<br/>
 * &emsp;&emsp;&emsp;&emsp;java.util.List&lt;org.w3c.dom.Element&gt; yourTransformationElements<br/>
 * );<br/>
 * </code>
 * <p>
 * The first three parameters are common to all polynomial values (for an explanation of their meaning take a look at
 * {@link org.deegree.cs.transformations.polynomial.PolynomialTransformation}). Again, the last list, will contain all
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
 * @param <T>
 *            The return type of the {@link CRSParser#getURIAsType(String)} method
 * 
 */
@LoggingNotes(debug = "the deegree XML format provider")
public class DeegreeCRSProvider<T> extends AbstractCRSProvider<T> {

    private static final String VERSION = "CRS_VERSION";

    private static Logger LOG = LoggerFactory.getLogger( DeegreeCRSProvider.class );

    private CRSExporterBase exporter;

    /**
     * @param properties
     *            containing information about the crs resource class and the file location of the crs configuration. If
     *            either is null the default mechanism is using the {@link Parser} and the deegree-crs-configuration.xml
     * @throws CRSConfigurationException
     *             if the give file or the default-crs-configuration.xml file could not be loaded.
     */
    @SuppressWarnings("unchecked")
    private DeegreeCRSProvider( Properties properties ) throws CRSConfigurationException {
        // rb: set to unchecked, the constructor is private and is only called from within the getInstance which is
        // valid.
        super( properties, CRSParser.class, null );
        exporter = new CRSExporterBase( new Properties( properties ) );
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
    public CRSParser<T> getResolver() {
        return (CRSParser<T>) super.getResolver();
    }

    public List<CRSCodeType[]> getAvailableCRSCodes() {
        List<CRSCodeType[]> codes = getResolver().getAvailableCRSCodes();
        return codes;
    }

    public List<CoordinateSystem> getAvailableCRSs() {
        List<CoordinateSystem> allSystems = new LinkedList<CoordinateSystem>();
        Set<String> knownIds = new HashSet<String>();
        // List<OMElement> allCRSIDs = getResolver().getAvailableCRSs();
        List<CRSCodeType[]> allCRSIDs = getResolver().getAvailableCRSCodes();
        final int total = allCRSIDs.size();
        int count = 0;
        int percentage = (int) Math.round( total / 100.d );
        int number = 0;
        LOG.info( "Trying to create a total of " + total + " coordinate systems." );
        for ( CRSCodeType[] crsID : allCRSIDs ) {
            if ( crsID != null ) {
                // String id = crsID.getTextContent();
                String id = crsID[0].getOriginal();
                if ( id != null && !"".equals( id.trim() ) ) {
                    if ( count++ % percentage == 0 ) {
                        System.out.println( ( number ) + ( ( number++ < 10 ) ? " " : "" ) + "% created" );
                    }
                    // boolean createdAlready =
                    // for ( int i = 0; i < allSystems.size() && !createdAlready; ++i ) {
                    // CoordinateSystem c = allSystems.get( i );
                    // createdAlready = ( c != null && c.hasCode( CRSCodeType.valueOf( id ) ) );
                    // }
                    if ( !knownIds.contains( id.toLowerCase() ) ) {
                        allSystems.add( getCRSByCode( CRSCodeType.valueOf( id ) ) );
                        for ( CRSCodeType code : crsID ) {
                            knownIds.add( code.getOriginal().toLowerCase() );
                        }
                    }
                }
            }
        }
        System.out.println();
        return allSystems;
    }

    @Override
    protected CoordinateSystem parseCoordinateSystem( T crsDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseCoordinateSystem( crsDefinition );
    }

    @Override
    public Transformation parseTransformation( T transformationDefinition )
                            throws CRSConfigurationException {
        return getResolver().parseTransformation( transformationDefinition );

    }

    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS )
                            throws CRSConfigurationException {
        return getResolver().getTransformation( sourceCRS, targetCRS );
    }

    // @Override
    // public CoordinateSystem getCRSByCode( CRSCodeType id )
    // throws CRSConfigurationException {
    // CRSParser<T> resolver = getResolver();
    // try {
    // return resolver.parseCoordinateSystem( resolver.getURIAsType( id.getOriginal() ) );
    // } catch ( IOException e ) {
    // LOG.error( e.getMessage(), e );
    // }
    // return null;
    // }

    @Override
    public CRSIdentifiable getIdentifiable( CRSCodeType id )
                            throws CRSConfigurationException {
        return getResolver().parseIdentifiableObject( id.getOriginal() );
    }

    /**
     * @param usedProjection
     * @param underlyingCRS
     * @return the Projection parsed from the configuration or
     *         <code>null<code> if no projection with given id was found.
     */
    public Projection getProjection( String usedProjection, GeographicCRS underlyingCRS ) {
        return getResolver().getProjectionForId( usedProjection, underlyingCRS );
    }

    /**
     * @param datumId
     * @return the datum denoted by given id or <code>null</code> if no datum with given id was found.
     */
    public GeodeticDatum getGeodeticDatumForId( String datumId ) {
        return getResolver().getGeodeticDatumForId( datumId );
    }

    /**
     * @param ellipsoidId
     * @return the ellipsoid denoted by given id or <code>null</code> if no ellipsoid with given id was found.
     */
    public Ellipsoid getEllipsoidForId( String ellipsoidId ) {
        return getResolver().getEllipsoidForId( ellipsoidId );
    }

    /**
     * @param pMeridianId
     * @return the PrimeMeridian denoted by given id or <code>null</code> if no PrimeMeridian with given id was found.
     */
    public PrimeMeridian getPrimeMeridianForId( String pMeridianId ) {
        return getResolver().getPrimeMeridianForId( pMeridianId );
    }

    /**
     * 
     * @param properties
     * @return a deegree stax or om based crs provider instance.
     */
    @SuppressWarnings("unchecked")
    public static DeegreeCRSProvider<?> getInstance( Properties properties ) {
        // rb: set to unchecked, because the generic castings are all valid.
        // read the properties to get a stax/om parser
        Version version = new Version( 0, 3, 0 );
        DeegreeCRSProvider<?> provider = null;
        CRSParser parser = null;
        if ( properties != null ) {
            String v = properties.getProperty( VERSION );
            if ( v != null && !"".equals( v ) ) {
                Version vers = Version.parseVersion( v );
                if ( vers.compareTo( version ) < 0 ) {
                    provider = new DeegreeCRSProvider<OMElement>( properties );
                    parser = new Parser( (DeegreeCRSProvider<OMElement>) provider, properties );
                } else {
                    provider = new DeegreeCRSProvider<StAXResource>( properties );
                    parser = new org.deegree.cs.configuration.deegree.xml.stax.Parser(
                                                                                       (DeegreeCRSProvider<StAXResource>) provider,
                                                                                       properties );
                }
            }
        }
        if ( provider == null ) {
            provider = new DeegreeCRSProvider<StAXResource>( properties );
            parser = new org.deegree.cs.configuration.deegree.xml.stax.Parser(
                                                                               (DeegreeCRSProvider<StAXResource>) provider,
                                                                               properties );
        }
        provider.setResolver( parser );
        return provider;
    }

    /**
     * Checks if the time of creating all crs's
     * 
     * @param args
     */
    public static void main( String[] args ) {
        CRSProvider provider = CRSConfiguration.getInstance().getProvider();

        long sT = currentTimeMillis();

        List<CRSCodeType[]> availableCRSCodes = provider.getAvailableCRSCodes();
        long eT = currentTimeMillis() - sT;
        System.out.println( "Action took: " + eT + " ms." );

        System.out.println( "size: " + availableCRSCodes.size() );
    }
}
