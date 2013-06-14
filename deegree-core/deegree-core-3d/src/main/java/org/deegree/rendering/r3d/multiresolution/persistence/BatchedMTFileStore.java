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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.deegree.commons.utils.nio.DirectByteBufferPool;
import org.deegree.rendering.r3d.multiresolution.MultiresolutionMesh;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceMetadata;

/**
 * The <code></code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class BatchedMTFileStore implements BatchedMTStore {

    private MultiresolutionMesh mesh;

    private ResourceMetadata<BatchedMTStore> metadata;

    public BatchedMTFileStore( URL dir, int maxDirectMemBytes, ResourceMetadata<BatchedMTStore> metadata )
                            throws IOException, URISyntaxException {
        this.metadata = metadata;
        DirectByteBufferPool pool = new DirectByteBufferPool( maxDirectMemBytes, "TODO" );
        mesh = new MultiresolutionMesh( new File( dir.toURI() ), pool );
    }

    @Override
    public MultiresolutionMesh getMesh() {
        return mesh;
    }

    public void destroy() {
        // nothing to cleanup
    }

    @Override
    public ResourceMetadata<? extends Resource> getMetadata() {
        return metadata;
    }

    @Override
    public void init() {
        // nothing to init
    }
}
