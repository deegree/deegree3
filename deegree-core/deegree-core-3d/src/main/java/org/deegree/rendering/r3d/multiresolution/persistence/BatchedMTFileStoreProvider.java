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
package org.deegree.rendering.r3d.multiresolution.persistence;

import static java.util.Collections.singletonMap;

import java.net.URL;
import java.util.Map;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.jaxb.JAXBUtils;
import org.deegree.cs.coordinatesystems.ICRS;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.rendering.r3d.jaxb.batchedmt.BatchedMTFileStoreConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BatchedMTFileStoreProvider implements BatchedMTStoreProvider {

    private static final Logger LOG = LoggerFactory.getLogger( BatchedMTFileStoreProvider.class );

    private static final String CONFIG_NS = "http://www.deegree.org/datasource/3d/batchedmt/file";

    private static final String CONFIG_JAXB_PACKAGE = "org.deegree.rendering.r3d.jaxb.batchedmt";

    private static final String CONFIG_SCHEMA = "/META-INF/schemas/datasource/3d/batchedmt/3.0.0/file.xsd";

    public String getConfigNamespace() {
        return CONFIG_NS;
    }

    @Override
    public BatchedMTStore build( URL configURL ) {

        BatchedMTStore bs = null;
        try {
            BatchedMTFileStoreConfig config = (BatchedMTFileStoreConfig) JAXBUtils.unmarshall( CONFIG_JAXB_PACKAGE,
                                                                                               CONFIG_SCHEMA, configURL );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            ICRS crs = CRSManager.getCRSRef( config.getCrs() );
            URL dir = resolver.resolve( config.getDirectory() );
            int maxDirectMem = config.getMaxDirectMemory().intValue();
            bs = new BatchedMTFileStore( crs, dir, maxDirectMem );

        } catch ( Exception e ) {
            e.printStackTrace();
            String msg = "Error in BatchedMT store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new IllegalArgumentException( msg, e );
        }
        return bs;
    }

    public URL getConfigSchema() {
        return BatchedMTFileStoreProvider.class.getResource( CONFIG_SCHEMA );
    }

    public Map<String, URL> getConfigTemplates() {
        String path = "/META-INF/schemas/datasource/3d/batchedmt/3.0.0/example.xml";
        return singletonMap( "example", BatchedMTFileStoreProvider.class.getResource( path ) );
    }

}