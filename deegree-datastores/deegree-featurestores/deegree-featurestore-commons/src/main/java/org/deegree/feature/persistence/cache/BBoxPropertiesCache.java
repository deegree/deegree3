//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/deegree3/trunk/deegree-datastores/deegree-featurestore/deegree-featurestore-commons/src/main/java/org/deegree/feature/persistence/cache/FeatureStoreCache.java $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.feature.persistence.cache;

import static java.util.Collections.synchronizedMap;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.deegree.commons.tom.ReferenceResolvingException;
import org.deegree.commons.utils.StringUtils;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.geometry.primitive.Point;
import org.slf4j.Logger;

/**
 * {@link BBoxCache} based on a Java properties file.
 * 
 * @see BBoxCache
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: mschneider $
 * 
 * @version $Revision$
 */
public class BBoxPropertiesCache implements BBoxCache {

    private static final Logger LOG = getLogger( BBoxPropertiesCache.class );

    private final File propsFile;

    private final Map<String, Envelope> ftNameToEnvelope = synchronizedMap( new TreeMap<String, Envelope>() );

    /**
     * Creates a new {@link BBoxPropertiesCache} instance.
     * 
     * @param propsFile
     *            properties file, must not be <code>null</code>
     * @throws IOException
     */
    public BBoxPropertiesCache( File propsFile ) throws IOException {
        this.propsFile = propsFile;
        if ( !propsFile.exists() ) {
            LOG.info( "File '" + propsFile.getCanonicalPath() + "' does not exist. Will be created as needed." );
            return;
        }
        if ( !propsFile.isFile() ) {
            LOG.error( "File '" + propsFile.getCanonicalPath() + "' does not denote a standard file." );
            return;
        }

        Properties props = new Properties();
        InputStream is = new FileInputStream( propsFile );
        try {
            props.load( is );
        } finally {
            IOUtils.closeQuietly( is );
        }

        Enumeration<?> e = props.propertyNames();
        while ( e.hasMoreElements() ) {
            String propName = (String) e.nextElement();
            String propValue = props.getProperty( propName );
            Envelope env = decodePropValue( propValue );
            LOG.debug( "Envelope for feature type '{}': {}", propName, env );
            ftNameToEnvelope.put( propName, env );
        }
    }

    @Override
    public Envelope get( QName ftName ) {
        String s = ftName.toString();
        if ( !ftNameToEnvelope.containsKey( s ) ) {
            throw new IllegalArgumentException( "No envelope information for feature type '" + ftName + "' in cache." );
        }
        return ftNameToEnvelope.get( s );
    }

    @Override
    public boolean contains( QName ftName ) {
        return ftNameToEnvelope.containsKey( ftName.toString() );
    }

    @Override
    public void set( QName ftName, Envelope bbox ) {
        ftNameToEnvelope.put( ftName.toString(), bbox );

        // TODO really do this every time?
        try {
            persist();
        } catch ( IOException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void persist()
                            throws IOException {
        Properties props = new Properties();
        for ( String ftName : ftNameToEnvelope.keySet() ) {
            props.put( ftName, encodePropValue( ftNameToEnvelope.get( ftName ) ) );
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream( propsFile );
            props.store( out, null );
        } catch ( Throwable t ) {
            LOG.warn( "Unable to store cached envelopes in file '" + propsFile + "': " + t.getMessage() );
        } finally {
            IOUtils.closeQuietly( out );
        }
    }

    private final Envelope decodePropValue( String s ) {
        if ( s == null || s.isEmpty() ) {
            return null;
        }
        String[] parts = StringUtils.split( s, "," );
        String srsName = parts[0];
        CRSRef crs;
        try {
            crs = CRSManager.getCRSRef( srsName );
            crs.getReferencedObject();
        } catch ( ReferenceResolvingException e ) {
            throw new IllegalArgumentException( e.getMessage() );
        }
        double[] coords = new double[parts.length - 1];
        for ( int i = 0; i < parts.length - 1; i++ ) {
            coords[i] = Double.parseDouble( parts[i + 1].trim() );
        }
        if ( coords.length % 2 != 0 ) {
            throw new IllegalArgumentException();
        }
        double[] min = new double[coords.length / 2];
        double[] max = new double[coords.length / 2];
        for ( int i = 0, dim = coords.length / 2; i < dim; i++ ) {
            min[i] = coords[i];
            max[i] = coords[i + dim];
        }
        return new GeometryFactory().createEnvelope( min, max, crs );
    }

    private final String encodePropValue( Envelope env ) {
        if ( env == null || env.getCoordinateSystem() == null ) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append( env.getCoordinateSystem().getId() );
        Point p = env.getMin();
        for ( double d : p.getAsArray() ) {
            sb.append( ',' );
            sb.append( d );
        }
        p = env.getMax();
        for ( double d : p.getAsArray() ) {
            sb.append( ',' );
            sb.append( d );
        }
        return sb.toString();
    }
}
