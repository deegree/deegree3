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
package org.deegree.metadata.persistence;

import static org.deegree.metadata.persistence.MetadataInspectorManager.InspectorKey.CoupledResourceInspector;
import static org.deegree.metadata.persistence.MetadataInspectorManager.InspectorKey.IdentifierInspector;
import static org.deegree.metadata.persistence.MetadataInspectorManager.InspectorKey.InspireInspector;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.metadata.persistence.iso.parsing.inspectation.FileIdentifierInspector;
import org.deegree.metadata.persistence.iso.parsing.inspectation.RecordInspector;
import org.deegree.metadata.persistence.iso19115.jaxb.IdentifierInspector;
import org.slf4j.Logger;

/**
 * Stores a collection of inspectors.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataInspectorManager {

    private static Logger LOG = getLogger( MetadataInspectorManager.class );

    private static ServiceLoader<MetadataInspectorProvider> inspectorLoader = ServiceLoader.load( MetadataInspectorProvider.class );

    private static Map<String, MetadataInspectorProvider> preToInsp;

    private static List<RecordInspector> recInsp = Collections.synchronizedList( new ArrayList<RecordInspector>() );

    public enum InspectorKey {
        IdentifierInspector, InspireInspector, CoupledResourceInspector;
    }

    private static Map<String, MetadataInspectorProvider> getInspectors() {
        if ( preToInsp == null ) {
            preToInsp = new HashMap<String, MetadataInspectorProvider>();
            try {
                for ( MetadataInspectorProvider provider : inspectorLoader ) {
                    LOG.debug( "Metadata inspector provider: " + provider + ", prefix: "
                               + provider.getInspectorKey().name() );
                    if ( preToInsp.containsKey( provider.getInspectorKey() ) ) {
                        LOG.error( "Multiple metadata inspectors of the same one: '"
                                   + provider.getInspectorKey().name() + "' -- omitting inspector '"
                                   + provider.getClass().getName() + "'." );
                        continue;
                    }
                    switch ( provider.getInspectorKey() ) {
                    case CoupledResourceInspector:
                        preToInsp.put( CoupledResourceInspector.name(), provider );
                        break;
                    case IdentifierInspector:
                        preToInsp.put( IdentifierInspector.name(), provider );
                        break;
                    case InspireInspector:
                        preToInsp.put( InspireInspector.name(), provider );
                        break;
                    }

                }
            } catch ( Exception e ) {
                LOG.error( e.getMessage(), e );
            }
        }
        return preToInsp;

    }

    public static List<RecordInspector> getAll() {
        return recInsp;
    }

    /**
     * 
     * @param configURL
     * @return
     * @throws MetadataStoreException
     */
    public static void initInspectors( URL configURL )
                            throws MetadataStoreException {

        LOG.info( "--------------------------------------------------------------------------------" );
        LOG.info( "Setting up metadata inspectors." );
        LOG.info( "--------------------------------------------------------------------------------" );
        List<RecordInspector> ri = new ArrayList<RecordInspector>();
        List<String> localName = new ArrayList<String>();
        try {
            XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( configURL.openStream() );
            StAXParsingHelper.nextElement( xmlReader );
            while ( xmlReader.hasNext() ) {
                int event = xmlReader.next();
                if ( event == XMLStreamConstants.START_ELEMENT ) {
                    localName.add( xmlReader.getLocalName() );
                }
            }

        } catch ( Exception e ) {
            String msg = "Error determining configuration namespace for file '" + configURL + "'";
            LOG.error( msg );
            throw new MetadataStoreException( msg );
        }
        for ( String l : localName ) {
            MetadataInspectorProvider provider = getInspectors().get( l );
            if ( provider == null ) {
                String msg = "No metadata inspector provider for localName: '" + l + "' (file: '" + configURL
                             + "') registered. Skipping it.";
                LOG.error( msg );
                throw new MetadataStoreException( msg );
            }
            ri.add( provider.getInspector( configURL ) );
        }

        if ( !ri.contains( FileIdentifierInspector.getInstance() ) ) {
            LOG.info( "No IdentifierInspector configured. A default instance will be added. " );
            ri.add( FileIdentifierInspector.newInstance( new IdentifierInspector() ) );
        }
        recInsp.addAll( ri );

    }
}
