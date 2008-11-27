//$HeadURL$
/*----------------    FILE HEADER  ------------------------------------------
 This file is part of deegree.
 Copyright (C) 2001-2008 by:
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

package org.deegree.commons.utils;

import static org.deegree.commons.utils.CollectionUtils.removeDuplicates;

import java.util.LinkedList;

import junit.framework.TestCase;

/**
 * <code>CollectionUtilsTest</code>
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class CollectionUtilsTest extends TestCase {

    /**
     * 
     */
    public void testRemoveDuplicates() {
        LinkedList<String> list = new LinkedList<String>();
        list.add( "test" );
        list.add( "test2" );
        list.add( "test3" );
        list.add( "test" );
        list.add( "test5" );
        list.add( "test6" );
        list.add( "test" );
        LinkedList<String> list2 = new LinkedList<String>();
        list2.add( "test" );
        list2.add( "test2" );
        list2.add( "test3" );
        list2.add( "test5" );
        list2.add( "test6" );
        assertEquals( removeDuplicates( list ), list2 );
        assertEquals( list, list2 );
    }

}
