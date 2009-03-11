//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 http://www.giub.uni-bonn.de/deegree/
 lat/lon GmbH
 http://www.lat-lon.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 Lesser General Public License for more details.
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 Contact:

 Andreas Poth
 lat/lon GmbH
 Aennchenstr. 19
 53177 Bonn
 Germany
 E-Mail: poth@lat-lon.de

 Prof. Dr. Klaus Greve
 Department of Geography
 University of Bonn
 Meckenheimer Allee 166
 53115 Bonn
 Germany
 E-Mail: greve@giub.uni-bonn.de
 ---------------------------------------------------------------------------*/

package org.deegree.rendering.r3d.opengl.rendering.managers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.deegree.commons.utils.GraphvizDot;
import org.deegree.model.geometry.Envelope;

/**
 * The <code>RenderableManager</code> class TODO add class documentation here.
 * 
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 * @param <T>
 *            type of this manager
 * 
 */
public class RenderableManager<T extends Positionable> implements Collection<T> {

    private final QuadTree<T> root;

    private final int numberOfObjectsInLeaf;

    private final Envelope validDomain;

    /**
     * @param validDomain
     * @param numberOfObjectsInLeaf
     */
    public RenderableManager( Envelope validDomain, int numberOfObjectsInLeaf ) {
        this.validDomain = validDomain;
        root = new QuadTree<T>( validDomain, numberOfObjectsInLeaf );
        this.numberOfObjectsInLeaf = numberOfObjectsInLeaf;
    }

    /**
     * @param validDomain
     *            of this Renderable Manager
     */
    public RenderableManager( Envelope validDomain ) {
        this( validDomain, 120 );
    }

    @Override
    public boolean add( T renderable ) {
        return root.add( renderable );
    }

    @Override
    public boolean remove( Object o ) {
        if ( o instanceof Positionable ) {
            return root.remove( (Positionable) o );
        }
        return false;
    }

    /**
     * @param env
     * @return the list of objects this manager manages.
     */
    public List<T> getObjects( Envelope env ) {
        return root.getObjects( env );
    }

    /**
     * @param env
     * @param comparator
     * @return the list of objects this manager manages.
     */
    public List<T> getObjects( Envelope env, Comparator<T> comparator ) {
        return root.getObjects( env, comparator );
    }

    /**
     * @param comparator
     * @return All objects this manager manages, sorted by using the given comparator.
     */
    public List<T> getObjects( Comparator<T> comparator ) {
        return root.getObjects( validDomain, comparator );
    }

    @Override
    public Iterator<T> iterator() {
        return root.getObjects().iterator();
    }

    @Override
    public int size() {
        return root.getObjects().size();
    }

    @Override
    public boolean addAll( Collection<? extends T> c ) {
        boolean result = true;
        for ( T p : c ) {
            if ( !result ) {
                break;
            }
            result = root.add( p );
        }
        return result;
    }

    @Override
    public void clear() {
        root.clear();
    }

    @Override
    public boolean contains( Object o ) {
        if ( o instanceof Positionable ) {
            return root.contains( (Positionable) o );
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
            result = root.add( (T) o );
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

}
