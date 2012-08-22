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

package org.deegree.io.shpapi;

import org.deegree.model.spatialschema.ByteUtils;

/**
 * Class representing a record of an ESRI .shx file.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class IndexRecord {

    protected int offset;

    protected int length;

    /**
     *
     *
     */
    public IndexRecord() {
        this.offset = 0;
        this.length = 0;
    }

    /**
     *
     * @param off
     * @param len
     */
    public IndexRecord( int off, int len ) {
        this.offset = off;
        this.length = len;
    }

    /**
     *
     * @param recBuf
     */
    public IndexRecord( byte[] recBuf ) {
        this.offset = ByteUtils.readBEInt( recBuf, 0 );
        this.length = ByteUtils.readBEInt( recBuf, 4 );
    }

    /**
     *
     * @return index record length
     */
    public int getLength() {
        return length;
    }

    /**
     *
     * @return index record offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     *
     * @return index record as byte arry
     */
    public byte[] writeIndexRecord() {
        byte[] arr = new byte[8];
        ByteUtils.writeBEInt( arr, 0, offset );
        ByteUtils.writeBEInt( arr, 4, length );
        return arr;
    }

}
