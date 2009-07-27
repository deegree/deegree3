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
package org.deegree.commons.utils;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

/**
 *
 *
 * @author <a href="mailto:tonnhofer@lat-lon.de">Oliver Tonnhofer</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$ }
 */
public class FileUtilsTest {

    /**
     * Test method for {@link org.deegree.commons.utils.FileUtils#getBasename(java.io.File)}.
     */
    @Test
    public void testGetBasename() {
        assertEquals( "/tmp/foo", FileUtils.getBasename( new File( "/tmp/foo.txt" ) ) );
        assertEquals( "/tmp/foo", FileUtils.getBasename( new File( "/tmp/foo" ) ) );
        assertEquals( "../foo", FileUtils.getBasename( new File( "../foo.txt" ) ) );
        assertEquals( "/tmp", FileUtils.getBasename( new File( "/tmp/" ) ) );
        assertEquals( "/tmp.dir/foo", FileUtils.getBasename( new File( "/tmp.dir/foo" ) ) );
        assertEquals( "", FileUtils.getBasename( new File( "" ) ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.FileUtils#getFileExtension(java.io.File)}.
     */
    @Test
    public void testGetFileExtension() {
        assertEquals( "txt", FileUtils.getFileExtension( new File( "/tmp/foo.txt" ) ) );
        assertEquals( "txt", FileUtils.getFileExtension( new File( "/tmp/foo.bar.txt" ) ) );
        assertEquals( "", FileUtils.getFileExtension( new File( "/tmp/foo.bar." ) ) );
        assertEquals( "", FileUtils.getFileExtension( new File( "/tmp/foo" ) ) );
        assertEquals( "", FileUtils.getFileExtension( new File( "/tmp.dir/foo" ) ) );
        assertEquals( "", FileUtils.getFileExtension( new File( "" ) ) );
    }

    /**
     * Test method for {@link org.deegree.commons.utils.FileUtils#getFileExtension(java.io.File)}.
     */
    @Test
    public void testGetExclusiveExtension() {
        assertEquals( "foo", FileUtils.getFilename( new File( "/tmp/foo.txt" ) ) );
        assertEquals( "foo.bar", FileUtils.getFilename( new File( "/tmp/foo.bar.txt" ) ) );
        assertEquals( "foo.bar", FileUtils.getFilename( new File( "/tmp/foo.bar." ) ) );
        assertEquals( "foo", FileUtils.getFilename( new File( "/tmp/foo" ) ) );
        assertEquals( "foo", FileUtils.getFilename( new File( "/tmp.dir/foo" ) ) );
        assertEquals( "", FileUtils.getFilename( new File( "" ) ) );
    }

}
