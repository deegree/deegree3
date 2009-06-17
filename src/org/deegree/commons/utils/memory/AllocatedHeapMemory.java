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

package org.deegree.commons.utils.memory;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.primitive.Point;

/**
 * The <code>AllocatedHeapMemory</code> class provides methods for calculating the heap memory for some primitive objects.
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 * @author last edited by: $Author$
 * @version $Revision$, $Date$
 *
 */
public class AllocatedHeapMemory {
    /**
     * The number of bytes a reference will have
     */
    public static final int REF_SIZE;

    /**
     * The number of bytes a simple new Object without any fields will consume.
     */
    public static final int INSTANCE_SIZE;

    /**
     * Number of bytes for an int
     */
    public static final int INT_SIZE = Integer.SIZE / 8;

    /**
     * Number of bytes for an int
     */
    public static final int LONG_SIZE = Long.SIZE / 8;

    /**
     * Number of bytes for a float
     */
    public static final int FLOAT_SIZE = Float.SIZE / 8;

    /**
     * Number of bytes for a float
     */
    public static final int DOUBLE_SIZE = Double.SIZE / 8;

    static {
        String bits = System.getProperty( "sun.arch.data.model" );
        if ( bits == null || "unknown".equalsIgnoreCase( bits ) ) {
            // set to 32
            bits = "32";
        }
        REF_SIZE = Integer.valueOf( bits ) / 8;
        INSTANCE_SIZE = REF_SIZE * 2;
    }

    /**
     * This will return the size of an instance of a new Object, if the asReference is true, the size of a reference
     * will be added as well.
     *
     * @param asReference
     *
     * @return occupied bytes of a simple object, optional as a reference.
     */
    public static final int instanceAndReferenceSize( boolean asReference ) {
        return ( asReference ? REF_SIZE : 0 ) + INSTANCE_SIZE;
    }

    /**
     * The JVM allocates 8 bytes at a time..
     *
     * @param currentSize
     *            of the object
     * @return the next module 8 value.
     */
    public static final long roundToMem( long currentSize ) {
        if ( currentSize % 8 != 0 ) {
            currentSize += 8 - ( currentSize % 8 );
        }
        return currentSize;
    }

    //

    /**
     * The approximate size of the given String Object in bytes.
     *
     * @param s
     *            to check
     * @param asReference
     *            true if the String is a reference inside another class (if the size of a reference should be added).
     * @param internalised
     *            the resulting string will not be internalised, for example if a StringBuilder is used.
     * @return the approximate size of a String Object in bytes
     */
    public static long sizeOfString( String s, boolean asReference, boolean internalised ) {

        long localSize = 0;
        if ( s != null ) {
            // 4 for reference (optimistic, internalization)
            // 8 for Object (optimistic, internalization)
            // String has following fields
            // char[] value == 4 (reference) +
            // INSTANCE_SIZE for the elementData class and the
            // length = 8
            // int offset = 4
            // int count = 4
            // int hash = 4
            // total 32
            if ( internalised ) {
                localSize = REF_SIZE;
            } else {
                localSize = instanceAndReferenceSize( asReference ) + 4 * REF_SIZE + INSTANCE_SIZE + ( s.length() * 2 );
                if ( !asReference ) {
                    localSize = roundToMem( localSize );
                }
            }
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object, not the objects in them.
     */
    public static long sizeOfObjectArray( Object[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            // 4 for reference
            // 16 for the elementData class and the length
            localSize = instanceAndReferenceSize( asReference ) + 8 + ( REF_SIZE * o.length );
            if ( !asReference ) {
                localSize = roundToMem( localSize );
            }
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    private static long sizeOfPrimitiveArray( int length, int typeSize, boolean asReference ) {
        // for reference
        // InstanceSize for the elementData class and the length
        long localSize = instanceAndReferenceSize( asReference ) + REF_SIZE + ( typeSize * length );
        if ( !asReference ) {
            localSize = roundToMem( localSize );
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object.
     */
    public static long sizeOfFloatArray( float[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            localSize = sizeOfPrimitiveArray( o.length, FLOAT_SIZE, asReference );
        } else {
            if ( asReference ) {
                localSize = REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object
     */
    public static long sizeOfDoubleArray( double[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            localSize = sizeOfPrimitiveArray( o.length, DOUBLE_SIZE, asReference );
        } else {
            if ( asReference ) {
                localSize = REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object
     */
    public static long sizeOfByteArray( byte[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            localSize = sizeOfPrimitiveArray( o.length, 1, asReference );
        } else {
            if ( asReference ) {
                localSize = REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object
     */
    public static long sizeOfIntArray( int[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            localSize = sizeOfPrimitiveArray( o.length, INT_SIZE, asReference );
        } else {
            if ( asReference ) {
                localSize = REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param o
     * @param asReference
     *            true if the array is a reference inside another class (if the size of a reference should be added).
     * @return The size of the actual array object
     */
    public static long sizeOfLongArray( int[] o, boolean asReference ) {
        long localSize = 0;
        if ( o != null ) {
            localSize = sizeOfPrimitiveArray( o.length, LONG_SIZE, asReference );
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param ar
     * @return
     */
    private static long sizeOfList( ArrayList<?> ar ) {
        long localSize = 0;
        if ( ar != null ) {
            // size
            localSize = REF_SIZE;
            // array
            ar.trimToSize();
            localSize += REF_SIZE + INSTANCE_SIZE + REF_SIZE + ( REF_SIZE * ar.size() );
        }
        return localSize;
    }

    /**
     *
     * @param ll
     *            to calculate from
     * @return
     */
    private static long sizeOfList( LinkedList<?> ll ) {
        long localSize = 0;
        if ( ll != null ) {
            // 4 (for the pointer to elementData)
            // + 4 (for elementCount).
            // The elementData array will
            // take 16 (for the elementData class and the length) plus 4 * elementData.length.
            // We then follow the
            // hierarchy up and discover the variable int modCount in the superclass java.util.AbstractList, which will
            // take up the minimum 8 bytes
            // Header Entry
            localSize = REF_SIZE;
            // header instance
            localSize += sizeOfEntry( true );

            // size field
            localSize += REF_SIZE;
            // object reference
            localSize += ( sizeOfEntry( true ) * ll.size() );
        }
        return localSize;
    }

    private static long sizeOfEntry( boolean withObjectRef ) {
        // each entry has a

        // ref to next
        long localSize = REF_SIZE;
        // ref to prev
        localSize += REF_SIZE;

        if ( withObjectRef ) {
            // Object reference
            localSize += REF_SIZE;
        }
        // instance size
        localSize += instanceAndReferenceSize( false );
        return localSize;

    }

    /**
     * The size of the Objects referenced in the list are not accounted for.
     *
     * @param l
     *            get the bytes for.
     * @param asReference
     *            true if the list is a reference inside another class (if the size of a reference should be added)
     * @return the size of bytes of the given list.
     */
    public static long sizeOfList( List<?> l, boolean asReference ) {
        long localSize = 0;
        if ( l != null ) {
            // class
            localSize = instanceAndReferenceSize( asReference );
            // superclass modcount
            localSize += REF_SIZE;
            if ( l instanceof ArrayList ) {
                localSize += sizeOfList( (ArrayList<?>) l );
            } else if ( l instanceof LinkedList ) {
                localSize += sizeOfList( (LinkedList<?>) l );
            }
            if ( !asReference ) {
                localSize = roundToMem( localSize );
            }
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param b
     * @param asReference
     *            true if the buffer is a reference inside another class (if the size of a reference should be added)
     * @return the size of the given buffer.
     */
    public static long sizeOfBuffer( Buffer b, boolean asReference ) {
        long localSize = 0;
        if ( b != null ) {
            // class
            localSize = instanceAndReferenceSize( asReference );
            // superclass mark, position, limit, capacity
            localSize += 4 * INT_SIZE;
            localSize += LONG_SIZE;
            // offset is in all buffers
            localSize += INT_SIZE;
            // array
            localSize += REF_SIZE;
            // boolean
            localSize += INT_SIZE;
            if ( !b.isDirect() ) {
                // array
                localSize += instanceAndReferenceSize( true );
            } else {
                // Direct buffer keep a reference as well (viewedBuffer)
                localSize += REF_SIZE;
                // Cleaner
                localSize += instanceAndReferenceSize( true );
                // refs in cleaner
                localSize += 2 * instanceAndReferenceSize( true );
                // thunk Dealocate
                localSize += instanceAndReferenceSize( true );
                // address
                localSize += LONG_SIZE;
                // cappacity
                localSize += INT_SIZE;

                // TBD 8 byte don't know where from
                localSize += REF_SIZE;

            }
            if ( b instanceof ByteBuffer ) {
                localSize += b.capacity();
            } else if ( b instanceof FloatBuffer ) {
                localSize += b.capacity() * FLOAT_SIZE;
            } else if ( b instanceof IntBuffer ) {
                localSize += b.capacity() * INT_SIZE;
            } else if ( b instanceof DoubleBuffer ) {
                localSize += b.capacity() * DOUBLE_SIZE;
            } else if ( b instanceof LongBuffer ) {
                localSize += b.capacity() * LONG_SIZE;
            } else {
                localSize += b.capacity();
                // not supported yet
            }
            if ( !asReference ) {
                localSize = roundToMem( localSize );
            }
        } else {
            if ( asReference ) {
                localSize = REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     *
     * @param env
     *            to get the size for.
     * @param asReference
     *            true if the Envelope is a reference inside another class (if the size of a reference should be added)
     * @return the number of bytes this class has in memory
     */
    public static long sizeOfEnvelope( Envelope env, boolean asReference ) {
        long localSize = 0;
        if ( env != null ) {
            // 4 for reference
            // 8 for Object
            // Envelope has:
            // 2 * Point //assuming centroid is null
            // an id String
            // a crs
            localSize = instanceAndReferenceSize( asReference ) + sizeOfPoint( env.getMin(), true, false )
                        + sizeOfPoint( env.getMax(), true, false ) + sizeOfString( env.getId(), true, true )
                        + sizeOfCRS( env.getCoordinateSystem(), true );
            if ( !asReference ) {
                localSize = roundToMem( localSize );
            }
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     * The size of a point
     *
     * @param p
     *            to check
     * @param asReference
     *            true if the Point is a reference inside another class (if the size of a reference should be added)
     * @param withCRS
     *            true if the crs should be taken into account.
     * @return the size of bytes of the given list.
     */
    public static long sizeOfPoint( Point p, boolean asReference, boolean withCRS ) {
        long localSize = 0;
        if ( p != null ) {
            // 4 for reference
            // 8 for Object
            // one point has:
            // a double[]
            // an id String
            // a crs
            localSize += instanceAndReferenceSize( asReference ) + sizeOfString( p.getId(), true, true )
                         + sizeOfDoubleArray( p.getAsArray(), true )
                         + ( ( withCRS ) ? sizeOfCRS( p.getCoordinateSystem(), true ) : 0 );
            if ( !asReference ) {
                localSize = roundToMem( localSize );
            }

        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

    /**
     * @param coordinateSystem
     * @param asReference
     * @return 0 or 256 bytes, this method should check all the references and components... not so trivial.
     */
    public static long sizeOfCRS( CRS coordinateSystem, boolean asReference ) {
        long localSize = 0;
        if ( coordinateSystem != null ) {
            // Aargh, lots of stuff here:
            // Identiable
            // lets just say it's about 256 bytes
            localSize = 256;
        } else {
            if ( asReference ) {
                localSize += REF_SIZE;
            }
        }
        return localSize;
    }

}
