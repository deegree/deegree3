//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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

 Occam Labs Schmitz & Schneider GbR
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.style.persistence.sld;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.style.persistence.sld.SLDParser.getStyles;

import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceInitException;
import org.deegree.commons.config.ResourceManager;
import org.deegree.commons.utils.ProxyUtils;
import org.deegree.style.persistence.StyleStoreProvider;
import org.deegree.style.se.unevaluated.Style;

/**
 * @author stranger
 * 
 */
public class SLDStyleStoreProvider implements StyleStoreProvider {

    private static final URL CONFIG_SCHEMA = SLDStyleStoreProvider.class.getResource( "/META-INF/SCHEMAS_OPENGIS_NET/sld/1.1.0/StyledLayerDescriptor.xsd" );

    // private DeegreeWorkspace workspace;

    @Override
    public void init( DeegreeWorkspace workspace ) {
        // this.workspace = workspace;
    }

    @Override
    public SLDStyleStore create( URL configUrl )
                            throws ResourceInitException {
        InputStream in = null;
        XMLStreamReader reader = null;
        try {
            in = configUrl.openStream();
            XMLInputFactory fac = XMLInputFactory.newInstance();
            reader = fac.createXMLStreamReader( configUrl.toExternalForm(), in );
            Map<String, LinkedList<Style>> map = getStyles( reader );
            return new SLDStyleStore( map );
        } catch ( Throwable e ) {
            throw new ResourceInitException( "Could not read SLD style config.", e );
        } finally {
            try {
                if ( reader != null ) {
                    reader.close();
                }
            } catch ( XMLStreamException e ) {
                // eat it
            }
            closeQuietly( in );
        }
    }

    @Override
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] { ProxyUtils.class };
    }

    @Override
    public String getConfigNamespace() {
        return "http://www.opengis.net/sld";
    }

    @Override
    public URL getConfigSchema() {
        return CONFIG_SCHEMA;
    }

}
