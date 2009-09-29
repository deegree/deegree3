//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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

package org.deegree.rendering.r3d.opengl.rendering.model.manager;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.geometry.Envelope;
import org.deegree.rendering.r3d.ViewParams;

/**
 * The <code>RenderableManager</code> is a collection based on a quadtree which can hold {@link PositionableModel}
 * objects and cull them according to a given {@link ViewParams} view frustum.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            type of this manager
 * 
 */
public class RenderableManager<T extends PositionableModel> implements Collection<T> {

    private final QTModelScene<T> root;

    private final int numberOfObjectsInLeaf;

    private final Envelope validDomain;

    private int size;

    /**
     * @param validDomain
     * @param numberOfObjectsInLeaf
     * @param maxPixelError
     */
    public RenderableManager( Envelope validDomain, int numberOfObjectsInLeaf, double maxPixelError ) {
        this.validDomain = validDomain;
        root = new QTModelScene<T>( validDomain, numberOfObjectsInLeaf, maxPixelError );
        this.numberOfObjectsInLeaf = numberOfObjectsInLeaf;
    }

    /**
     * @param validDomain
     *            of this Renderable Manager
     */
    public RenderableManager( Envelope validDomain ) {
        this( validDomain, 120, 1 );
    }

    @Override
    public boolean add( T renderable ) {
        boolean result = root.insert( renderable );
        if ( result ) {
            size++;
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove( Object o ) {
        if ( o instanceof PositionableModel ) {
            boolean result = root.remove( (T) o );
            if ( result ) {
                size--;
            }
            return result;
        }
        return false;
    }

    /**
     * @param env
     * @return the list of objects this manager manages.
     */
    public List<T> getObjects( Envelope env ) {
        return root.query( env );
    }

    /**
     * @param viewParams
     * @return the list of objects this manager manages.
     */
    public Set<T> getObjects( ViewParams viewParams ) {
        return root.getObjects( viewParams );
    }

    /**
     * @return All objects this manager manages
     */
    public List<T> getObjects() {
        return root.query( validDomain );
    }

    @Override
    public Iterator<T> iterator() {
        return root.getObjects().iterator();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        boolean result = true;
        for ( T p : c ) {
            if ( !result ) {
                break;
            }
            result = root.insert( p );
        }
        return result;
    }

    @Override
    public void clear() {
        root.clear();
    }

    @Override
    public boolean contains( Object o ) {
        if ( o instanceof PositionableModel ) {
            return root.contains( (PositionableModel) o );
        }
        return false;
    }

    @Override
    public boolean containsAll( Collection<?> c ) {
        boolean result = true;
        for ( Object o : c ) {
            if ( !result ) {
                break;
            }
            result = contains( o );
        }
        return result;
    }

    @Override
    public boolean isEmpty() {
        return root.getObjects().isEmpty();
    }

    @Override
    public boolean removeAll( Collection<?> c ) {
        boolean result = true;
        for ( Object o : c ) {
            if ( !result ) {
                break;
            }
            result = remove( o );
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean retainAll( Collection<?> c ) {
        root.clear();
        boolean result = true;
        for ( Object o : c ) {
            if ( !result ) {
                break;
            }
            result = root.insert( (T) o );
        }
        return result;
    }

    @Override
    public Object[] toArray() {
        return root.getObjects().toArray();
    }

    @Override
    public <T> T[] toArray( T[] a ) {
        return root.getObjects().toArray( a );
    }

    /**
     * @return the number of objects a leaf will contain.
     */
    public final int getNumberOfObjectsInLeaf() {
        return numberOfObjectsInLeaf;
    }

    /**
     * @return the domain of this manager
     */
    public final Envelope getValidDomain() {
        return validDomain;
    }

    /**
     * Create a graphviz dot representation of this manager.
     * 
     * @param filename
     *            to be written to
     * @throws IOException
     *             if the file could not be written to.
     */
    public void createDotFile( String filename )
                            throws IOException {
        BufferedWriter out = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( filename ) ) );
        GraphvizDot.startDiGraph( out );
        root.outputAsDot( out, "", 0, -1 );
        GraphvizDot.endGraph( out );
        out.close();
    }

    /**
     * @return the used max pixel error.
     */
    public double getMaxPixelError() {
        return root.getMaxPixelError();
    }

}
