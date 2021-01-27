//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.workspace.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceException;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceLocation;

/**
 * File based resource location.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class DefaultResourceLocation<T extends Resource> implements ResourceLocation<T> {

    private File file;

    private ResourceIdentifier<T> identifier;

    public DefaultResourceLocation( File file, ResourceIdentifier<T> identifier ) {
        this.file = file;
        this.identifier = identifier;
    }

    @Override
    public String getNamespace() {
        InputStream fis = null;
        XMLStreamReader in = null;
        try {
            in = XMLInputFactory.newInstance().createXMLStreamReader( fis = getAsStream() );
            while ( !in.isStartElement() ) {
                in.next();
            }
            return in.getNamespaceURI();
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if ( in != null ) {
                try {
                    in.close();
                } catch ( Exception e ) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            IOUtils.closeQuietly( fis );
        }
        return null;
    }

    @Override
    public ResourceIdentifier<T> getIdentifier() {
        return identifier;
    }

    @Override
    public InputStream getAsStream() {
        try {
            return new FileInputStream( file );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public InputStream resolve( String path ) {
        try {
            return new FileInputStream( resolveToFile( path ) );
        } catch ( FileNotFoundException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return identifier.toString();
    }

    @Override
    public File resolveToFile( String path ) {
        return new File( file.toURI().resolve( path ) );
    }

    @Override
    public URL resolveToUrl( String path ) {
        try {
            try {
                URL url = new URL( path );
                if ( url.toURI().isAbsolute() ) {
                    return url;
                }
            } catch ( Exception e ) {
                // try as relative
            }
            return file.toURI().resolve( path ).toURL();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public File getAsFile() {
        return file;
    }

    @Override
    public void deactivate() {
        File f = new File( file.getParentFile(), identifier.getId() + ".ignore" );
        file.renameTo( f );
        file = f;
    }

    @Override
    public void activate() {
        File f = new File( file.getParentFile(), identifier.getId() + ".xml" );
        file.renameTo( f );
        file = f;
    }

    @Override
    public void setContent( InputStream in ) {
        try {
            file.getParentFile().mkdirs();
            FileUtils.copyInputStreamToFile( in, file );
        } catch ( IOException e ) {
            throw new ResourceException( e.getLocalizedMessage(), e );
        }
    }

}
