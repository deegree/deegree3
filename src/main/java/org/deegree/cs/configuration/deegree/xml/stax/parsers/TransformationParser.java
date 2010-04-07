//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
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

package org.deegree.cs.configuration.deegree.xml.stax.parsers;

import static org.deegree.commons.xml.stax.StAXParsingHelper.getAttributeValue;
import static org.deegree.commons.xml.stax.StAXParsingHelper.getElementTextAsDouble;
import static org.deegree.cs.configuration.deegree.xml.stax.Parser.CRS_NS;
import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.utils.log.LoggingNotes;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.cs.CRSCodeType;
import org.deegree.cs.CRSIdentifiable;
import org.deegree.cs.configuration.deegree.xml.DeegreeCRSProvider;
import org.deegree.cs.configuration.deegree.xml.stax.StAXResource;
import org.deegree.cs.coordinatesystems.CoordinateSystem;
import org.deegree.cs.coordinatesystems.GeographicCRS;
import org.deegree.cs.exceptions.CRSConfigurationException;
import org.deegree.cs.i18n.Messages;
import org.deegree.cs.transformations.Transformation;
import org.deegree.cs.transformations.TransformationFactory;
import org.deegree.cs.transformations.TransformationFactory.DSTransform;
import org.deegree.cs.transformations.coordinate.ConcatenatedTransform;
import org.deegree.cs.transformations.helmert.Helmert;
import org.deegree.cs.transformations.ntv2.NTv2Transformation;
import org.deegree.cs.transformations.polynomial.LeastSquareApproximation;
import org.slf4j.Logger;

/**
 * Stax-based configuration parser for transformation objects.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
@LoggingNotes(debug = "Get information about the currently parsed transformation, as well as a stack trace if something went wrong.")
public class TransformationParser extends DefinitionParser {

    private static final Logger LOG = getLogger( TransformationParser.class );

    private static final QName ROOT = new QName( CRS_NS, "TransformationDefinitions" );

    private static final QName USER_ELEM = new QName( CRS_NS, "UserDefined" );

    private static final QName HELM_ELEM = new QName( CRS_NS, "Helmert" );

    private static final QName NTV2_ELEM = new QName( CRS_NS, "NTv2" );

    private static final QName LSQUARE_ELEM = new QName( CRS_NS, "LeastSquare" );

    private final static Set<QName> knownTransformations = new HashSet<QName>( 4 );

    static {
        knownTransformations.add( USER_ELEM );
        knownTransformations.add( HELM_ELEM );
        knownTransformations.add( NTV2_ELEM );
        knownTransformations.add( LSQUARE_ELEM );
    }

    /** maps crs names to transformations */
    private final HashMap<String, Set<Transformation>> availableTransformations = new HashMap<String, Set<Transformation>>();

    private DSTransform datumShiftOperation;

    /**
     * @param provider
     * @param confURL
     * @param datumShift
     */
    public TransformationParser( DeegreeCRSProvider<StAXResource> provider, URL confURL, DSTransform datumShift ) {
        super( provider, confURL );
        datumShiftOperation = datumShift;
    }

    /**
     * @param infoID
     *            to get the conversioninfo from.
     * @return the configured wgs84 conversion info parameters.
     * @throws CRSConfigurationException
     */
    protected Helmert getConversionInfoForID( String infoID )
                            throws CRSConfigurationException {
        if ( infoID == null || "".equals( infoID.trim() ) ) {
            return null;
        }
        LOG.debug( "Searching for the wgs84 with id: " + infoID );
        Helmert result = getProvider().getCachedIdentifiable( Helmert.class, infoID );
        if ( result == null ) {
            try {
                Transformation tmpRes = parseTransformation( getConfigReader() );
                while ( tmpRes != null && !( tmpRes instanceof Helmert ) && !tmpRes.hasId( infoID, true, true ) ) {
                    tmpRes = parseTransformation( getConfigReader() );
                }
                if ( tmpRes != null && ( tmpRes instanceof Helmert ) ) {
                    result = (Helmert) tmpRes;
                }
            } catch ( XMLStreamException e ) {
                throw new CRSConfigurationException( e );
            }
        }
        return result;
    }

    /**
     * @param transformId
     *            to get the transformation for.
     * @return the configured wgs84 conversion info parameters.
     * @throws CRSConfigurationException
     */
    public Transformation getTransformationForId( String transformId )
                            throws CRSConfigurationException {
        if ( transformId == null || "".equals( transformId.trim() ) ) {
            return null;
        }
        LOG.debug( "Searching for the transformation with id: " + transformId );
        Transformation result = getProvider().getCachedIdentifiable( Transformation.class, transformId );
        if ( result == null ) {
            try {
                Transformation tmpRes = parseTransformation( getConfigReader() );
                while ( tmpRes != null && !tmpRes.hasId( transformId, false, true ) ) {
                    tmpRes = parseTransformation( getConfigReader() );
                }

            } catch ( XMLStreamException e ) {
                throw new CRSConfigurationException( e );
            }
        }
        return result;
    }

    /**
     * @param reader
     *            to
     * @return the next datum on the stream.
     * @throws XMLStreamException
     */
    protected Transformation parseTransformation( XMLStreamReader reader )
                            throws XMLStreamException {
        if ( reader == null || !super.moveReaderToNextIdentifiable( reader, knownTransformations ) ) {
            LOG.debug( "Could not get transformation, no more definitions left." );
            return null;
        }

        QName transformName = reader.getName();
        String className = getAttributeValue( reader, "class" );

        CRSIdentifiable identifiable = parseIdentifiable( reader );

        String sourceCRS = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "SourceCRS" ), true );
        String targetCRS = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "TargetCRS" ), true );

        CoordinateSystem src = getProvider().getCRSByCode( new CRSCodeType( sourceCRS ) );
        CoordinateSystem tar = getProvider().getCRSByCode( new CRSCodeType( targetCRS ) );
        if ( src == null ) {
            LOG.debug( reader.getLocation() + ") could not determine referenced source coordinate system." );
        }

        if ( tar == null ) {
            LOG.debug( reader.getLocation() + ") could not determine referenced target coordinate system." );
        }

        Transformation result = null;
        if ( className != null && !"".equals( className.trim() ) ) {
            result = instantiateConfiguredClass( reader, className, identifiable, src, tar );

        } else {
            if ( HELM_ELEM.equals( transformName ) ) {
                result = parseHelmert( reader, src, tar, identifiable );
            } else if ( NTV2_ELEM.equals( transformName ) ) {
                result = parseNTv2( reader, src, tar, identifiable );
            } else if ( LSQUARE_ELEM.equals( transformName ) ) {
                result = parseLeastSquare( reader, src, tar, identifiable );
            } else {
                throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "transformation",
                                                                          "definitions",
                                                                          "Transformation type: " + transformName

                                                                          + " is not known to the crs yet." ) );
            }
        }
        if ( result != null ) {
            getProvider().addIdToCache( result, false );
            Set<Transformation> avTransforms = this.availableTransformations.get( sourceCRS.toLowerCase() );
            if ( avTransforms == null ) {
                avTransforms = new HashSet<Transformation>();
                this.availableTransformations.put( sourceCRS.toLowerCase(), avTransforms );
            }
            avTransforms.add( result );
        }
        return result;
    }

    /**
     * @param className
     * @param underlyingCRS
     * @return
     */
    private Transformation instantiateConfiguredClass( XMLStreamReader reader, String className, CRSIdentifiable id,
                                                       CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        Transformation result = null;
        LOG.debug( "Trying to load user defined transformation class: " + className );
        try {
            Class<?> t = Class.forName( className );
            t.asSubclass( Transformation.class );
            /**
             * try to get a constructor with a native type as a parameter, by going over the 'names' of the classes of
             * the parameters, the native type will show up as the typename e.g. int or long..... <code>
             * public Transformation( transformId, sourceCRS, targetCRS, xmlStreamReader )
             * </code>
             */

            /**
             * Load the constructor with the standard projection values and the element list.
             */
            Constructor<?> constructor = t.getConstructor( CRSIdentifiable.class, CoordinateSystem.class,
                                                           CoordinateSystem.class, XMLStreamReader.class );
            result = (Transformation) constructor.newInstance( id, sourceCRS, targetCRS, reader );
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
        return result;

    }

    /**
     * @param reader
     * @param targetCRS
     * @param sourceCRS
     * @param identifiable
     * @return
     * @throws XMLStreamException
     */
    private Transformation parseLeastSquare( XMLStreamReader reader, CoordinateSystem sourceCRS,
                                             CoordinateSystem targetCRS, CRSIdentifiable identifiable )
                            throws XMLStreamException {
        List<Double> aValues = new LinkedList<Double>();
        List<Double> bValues = new LinkedList<Double>();
        String tmpValues = null;
        try {
            tmpValues = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "XParameters" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        }

        if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
            String[] split = tmpValues.split( "\\s" );
            for ( String t : split ) {
                aValues.add( Double.parseDouble( t ) );
            }
        }
        try {
            tmpValues = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "YParameters" ), true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        }
        if ( tmpValues != null && !"".equals( tmpValues.trim() ) ) {
            String[] split = tmpValues.split( "\\s" );
            for ( String t : split ) {
                bValues.add( Double.parseDouble( t ) );
            }
        }

        if ( aValues.size() == 0 || bValues.size() == 0 ) {
            throw new CRSConfigurationException(
                                                 "The polynomial variables (xParameters and yParameters element) defining the approximation to a given transformation function are required and may not be empty" );
        }

        float scaleX = 1;
        float scaleY = 1;
        try {
            scaleX = (float) getElementTextAsDouble( reader, new QName( CRS_NS, "ScaleX" ), 1, true );
        } catch ( XMLParsingException e ) {
            LOG.error( "Could not parse scaleX from crs:leastsquare, because: " + e.getMessage(), e );
        }
        try {
            scaleY = (float) getElementTextAsDouble( reader, new QName( CRS_NS, "ScaleY" ), 1, true );
        } catch ( XMLParsingException e ) {
            LOG.error( "Could not parse scaleY from crs:leastsquare, because: " + e.getMessage(), e );
        }
        return new LeastSquareApproximation( aValues, bValues, sourceCRS, targetCRS, scaleX, scaleY );
    }

    /**
     * @param reader
     * @param targetCRS
     * @param sourceCRS
     * @param identifiable
     * @return
     * @throws XMLStreamException
     */
    private Transformation parseNTv2( XMLStreamReader reader, CoordinateSystem sourceCRS, CoordinateSystem targetCRS,
                                      CRSIdentifiable identifiable )
                            throws XMLStreamException {
        URL gridFile = null;
        try {
            String file = StAXParsingHelper.getRequiredText( reader, new QName( CRS_NS, "Gridfile" ), true );
            gridFile = StAXParsingHelper.resolve( file, reader );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( e );
        } catch ( MalformedURLException e ) {
            throw new CRSConfigurationException( e );
        }
        return new NTv2Transformation( sourceCRS, targetCRS, identifiable, gridFile );
    }

    /**
     * @param reader
     * @param targetCRS
     * @param sourceCRS
     * @param identifiable
     * @return
     * @throws XMLStreamException
     */
    private Transformation parseHelmert( XMLStreamReader reader, CoordinateSystem sourceCRS,
                                         CoordinateSystem targetCRS, CRSIdentifiable identifiable )
                            throws XMLStreamException {
        double xT = 0, yT = 0, zT = 0, xR = 0, yR = 0, zR = 0, scale = 0;
        try {
            xT = getElementTextAsDouble( reader, new QName( CRS_NS, "XAxisTranslation" ), 0, true );
            yT = getElementTextAsDouble( reader, new QName( CRS_NS, "YAxisTranslation" ), 0, true );
            zT = getElementTextAsDouble( reader, new QName( CRS_NS, "ZAxisTranslation" ), 0, true );
            xR = getElementTextAsDouble( reader, new QName( CRS_NS, "XAxisRotation" ), 0, true );
            yR = getElementTextAsDouble( reader, new QName( CRS_NS, "YAxisRotation" ), 0, true );
            zR = getElementTextAsDouble( reader, new QName( CRS_NS, "ZAxisRotation" ), 0, true );
            scale = getElementTextAsDouble( reader, new QName( CRS_NS, "ScaleDifference" ), 0, true );
        } catch ( XMLParsingException e ) {
            throw new CRSConfigurationException( Messages.getMessage( "CRS_CONFIG_PARSE_ERROR", "helmert",
                                                                      "definitions", e.getMessage() ), e );
        }
        return new Helmert( xT, yT, zT, xR, yR, zR, scale, null, GeographicCRS.WGS84, identifiable );
    }

    @Override
    protected QName expectedRootName() {
        return ROOT;
    }

    /**
     * @param sourceCRS
     * @param targetCRS
     * @return the (concatenated) configured transform between the source and the target crs. Calling this method is not
     *         the same as creating a new Transformation chain with the
     *         {@link TransformationFactory#createFromCoordinateSystems(CoordinateSystem, CoordinateSystem)}
     */
    public Transformation getTransformation( CoordinateSystem sourceCRS, CoordinateSystem targetCRS ) {
        if ( !super.readEntireFile() ) {
            // first parse the entire configuration file until the end.
            getTransformationForId( "a" );
        }
        // a will be null;
        String[] ids = sourceCRS.getOrignalCodeStrings();
        List<Transformation> hits = new LinkedList<Transformation>();
        for ( String id : ids ) {
            String c = id.toLowerCase();
            if ( availableTransformations.containsKey( c ) ) {
                Set<Transformation> set = availableTransformations.get( c );
                for ( Transformation t : set ) {
                    if ( targetCRS.equals( t.getTargetCRS() ) ) {
                        if ( datumShiftOperation.isPreferred( t ) ) {
                            return t;
                        }
                        // found a possible hit, but it was not preferred (e.g. found NTv2 but Helmert is preferred.)
                        hits.add( t );
                    }
                }
                // no direct transformation found, trying a chain?
                for ( Transformation t : set ) {
                    if ( !"Helmert".equals( t.getImplementationName() ) ) {
                        Transformation nt = getTransformation( t.getTargetCRS(), targetCRS );
                        if ( nt != null ) {
                            return new ConcatenatedTransform( t, nt );
                        }
                    }
                }
            }
        }
        if ( !hits.isEmpty() ) {
            // return the first (not best) hit.
            return hits.get( 0 );
        }
        return null;
    }

    /**
     * @param sourceCRS
     * @return the configured helmert for the given source crs or <code>null</code>
     */
    public Helmert getConversionInfo( GeographicCRS sourceCRS ) {
        if ( !super.readEntireFile() ) {
            // first parse the entire configuration file until the end.
            getTransformationForId( "a" );
        }
        // a will be null;
        String[] ids = sourceCRS.getOrignalCodeStrings();
        for ( String id : ids ) {
            String c = id.toLowerCase();
            if ( availableTransformations.containsKey( c ) ) {
                Set<Transformation> set = availableTransformations.get( c );
                for ( Transformation t : set ) {
                    if ( t instanceof Helmert ) {
                        return (Helmert) t;
                    }
                }
                // no direct transformation found, trying a chain?
            }
        }
        return null;
    }
}
