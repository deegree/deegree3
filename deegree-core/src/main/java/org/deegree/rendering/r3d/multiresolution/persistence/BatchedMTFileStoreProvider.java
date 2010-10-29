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

import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.deegree.commons.xml.XMLAdapter;
import org.deegree.cs.CRS;
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

    @Override
    public String getConfigNamespace() {
        return "http://www.deegree.org/datasource/batchedmt/file";
    }

    @Override
    public BatchedMTStore build( URL configURL ) {

        BatchedMTStore bs = null;
        try {
            JAXBContext jc = JAXBContext.newInstance( "org.deegree.rendering.r3d.jaxb.batchedmt" );
            Unmarshaller u = jc.createUnmarshaller();
            BatchedMTFileStoreConfig config = (BatchedMTFileStoreConfig) u.unmarshal( configURL );

            XMLAdapter resolver = new XMLAdapter();
            resolver.setSystemId( configURL.toString() );

            CRS crs = new CRS( config.getCrs() );
            URL dir = resolver.resolve( config.getDirectory());
            int maxDirectMem = config.getMaxDirectMemory().intValue();
            bs = new BatchedMTFileStore(crs, dir, maxDirectMem);

        } catch ( Exception e ) {
            e.printStackTrace();
            String msg = "Error in BatchedMT store configuration file '" + configURL + "': " + e.getMessage();
            LOG.error( msg );
            throw new IllegalArgumentException( msg, e );
        }
        return bs;
    }
}
