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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wps.provider.sextante;

import java.util.Iterator;
import java.util.NoSuchElementException;
import es.unex.sextante.dataObjects.IFeature;
import es.unex.sextante.dataObjects.IFeatureIterator;
import es.unex.sextante.exceptions.IteratorException;

/**
 * This is an iterator for {@link IFeature}s. <br>
 * He will used by {@link VectorLayerImpl}.
 * 
 * @author <a href="mailto:pabel@lat-lon.de">Jens Pabel</a>
 * 
 */
public class FeatureIteratorImpl implements IFeatureIterator {

    // java.util.Iterator
    private Iterator<IFeature> it;

    /**
     * Creates a {@link FeatureIteratorImpl} from a {@link Iterator}.
     * 
     * @param it
     *            {@link Iterator}.
     */
    public FeatureIteratorImpl( Iterator<IFeature> it ) {
        this.it = it;
    }

    @Override
    public void close() {
        it = null;
    }

    @Override
    public boolean hasNext() {
        return it.hasNext();
    }

    @Override
    public IFeature next()
                            throws IteratorException {

        IFeature f = null;

        try {
            f = it.next();

        } catch ( NoSuchElementException e ) {
            throw new IteratorException( "No features in the layer!" );
        }

        return f;
    }
    
}
