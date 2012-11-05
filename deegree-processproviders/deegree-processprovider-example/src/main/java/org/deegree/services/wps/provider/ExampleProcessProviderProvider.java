//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.services.wps.provider;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link ProcessProviderProvider} for the {@link ExampleProcessProvider}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ExampleProcessProviderProvider implements ProcessProviderProvider {

    private static final Logger LOG = LoggerFactory.getLogger( ExampleProcessProviderProvider.class );

    private static final String CONFIG_NAMESPACE = "http://www.deegree.org/processes/example";

    @Override
    public String getConfigNamespace() {
        return CONFIG_NAMESPACE;
    }

    @Override
    public ProcessProvider create( URL configURL ) {

        LOG.info( "Configuring example process provider using file '" + configURL + "'." );

        Map<String, String> processIdToReturnValue = new HashMap<String, String>();

        try {
            XMLStreamReader xmlStream = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            while ( xmlStream.getEventType() != XMLStreamConstants.END_DOCUMENT ) {
                if ( xmlStream.isStartElement() && "Process".equals( xmlStream.getLocalName() ) ) {
                    String processId = xmlStream.getAttributeValue( null, "id" );
                    String returnValue = xmlStream.getElementText();
                    processIdToReturnValue.put( processId, returnValue );
                } else {
                    xmlStream.next();
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            throw new RuntimeException( "Error parsing example process provider configuration '" + configURL + "': "
                                        + e.getMessage() );
        }

        return new ExampleProcessProvider( processIdToReturnValue );
    }

    @SuppressWarnings("unchecked")
    public Class<? extends ResourceManager>[] getDependencies() {
        return new Class[] {};
    }

    public void init( DeegreeWorkspace workspace ) {
        // this.workspace = workspace;
    }

    public URL getConfigSchema() {
        return null;
    }
}
