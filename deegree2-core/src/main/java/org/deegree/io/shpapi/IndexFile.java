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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import org.deegree.model.spatialschema.ByteUtils;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * Uses class ShapeUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 *
 *
 * @version 31.07.2000
 * @author Andreas Poth
 */

public class IndexFile {

    private static final String _shx = ".shx";

    private RandomAccessFile raf;

    /**
     * The length of an index record. (8 byte)
     */
    private static final int INDEX_RECORD_LENGTH = 8;

    /**
     * array which holds the content of .shx-file:
     */
    private IndexRecord[] indexArray = null;

    /**
     * IndexFileHeader is equal to ShapeFileHeader
     */
    private FileHeader fh;

    /**
     * minimum bounding rectangle of the shape-file
     */
    private SHPEnvelope fileMBR;

    /**
     * number of Records in .shp, .shx., .dbf has to be identical
     */
    private int RecordNum;

    /**
     * file position offset
     */
    private long offset;

    /**
     * length of the indexfile
     */
    private int filelength = 0;

    /**
     * Construct a IndexFile from a file name.
     *
     * @param url
     * @throws IOException
     */
    public IndexFile( String url ) throws IOException {

        /*
         * creates raf
         */
        raf = new RandomAccessFile( url + _shx, "r" );

        /*
         * construct Header as ShapeFileHeader
         */
        fh = new FileHeader( raf );

        fileMBR = fh.getFileMBR();

        /*
         * construct indexArray
         */
        setIndexArray();

    }

    /**
     * Construct a IndexFile from a file name.
     *
     * @param url
     * @param rwflag
     * @throws IOException
     */
    public IndexFile( String url, String rwflag ) throws IOException {

        // delete file if it exists
        File file = new File( url + _shx );
        if ( rwflag.indexOf( 'w' ) > -1 && file.exists() ) {
            file.delete();
            FileOutputStream os = new FileOutputStream( file, false );
            os.close();
        }
        file = null;
        raf = new RandomAccessFile( url + _shx, rwflag );
        // if the 2nd arg is true an empty header will be
        // written by FileHeader
        fh = new FileHeader( raf, rwflag.indexOf( 'w' ) > -1 );
        fileMBR = fh.getFileMBR();
        offset = raf.length();

        if ( offset < 100 ) {
            offset = ShapeConst.SHAPE_FILE_HEADER_LENGTH;
        }
        setIndexArray();
    }

    /**
     *
     */
    public void close() {
        try {
            raf.close();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }

    }

    /**
     * method: writeHeader(int filelength, byte shptype,SHPEnvelope mbr) <BR>
     * Writes a header into the index file. <BR>
     *
     * @param shptype
     * @param mbr
     * @throws IOException
     */
    public void writeHeader( int shptype, SHPEnvelope mbr )
                            throws IOException {

        byte[] header = new byte[ShapeConst.SHAPE_FILE_HEADER_LENGTH];

        ByteUtils.writeBEInt( header, 0, ShapeConst.SHAPE_FILE_CODE );
        ByteUtils.writeBEInt( header, 24, filelength );
        ByteUtils.writeLEInt( header, 28, ShapeConst.SHAPE_FILE_VERSION );
        ByteUtils.writeLEInt( header, 32, shptype );
        ShapeUtils.writeBox( header, 36, mbr );

        raf.seek( 0 );
        raf.write( header, 0, ShapeConst.SHAPE_FILE_HEADER_LENGTH );
    }

    /**
     * method: getFileMBR() <BR>
     * returns the minimum bounding rectangle of the shape-file <BR>
     *
     * @return the minimum bounding rectangle of the shape-file <BR>
     */
    public SHPEnvelope getFileMBR() {

        return fileMBR;

    }

    /**
     * method: setIndexArray() <BR>
     * local constructor for local field indexArray <BR>
     */
    private void setIndexArray()
                            throws IOException {

        byte[] recBuf = new byte[INDEX_RECORD_LENGTH];
        long rafPos = ShapeConst.SHAPE_FILE_HEADER_LENGTH;
        int iaIndex = 0;
        ArrayList<IndexRecord> indexArrayVector = new ArrayList<IndexRecord>( 10000 );

        raf.seek( rafPos );
        // loop over index records, until EOF
        while ( raf.read( recBuf, 0, INDEX_RECORD_LENGTH ) != -1 ) {
            IndexRecord ir = new IndexRecord( recBuf );

            // set ArrayVector item as index record
            indexArrayVector.add( ir );

            // array index adjustment
            ++iaIndex;

            // filepos adjustment
            rafPos = rafPos + INDEX_RECORD_LENGTH;
            raf.seek( rafPos );
        } // end of while

        // iaIndex holds Record Number
        RecordNum = iaIndex;

        // copy vector into indexArray
        indexArray = indexArrayVector.toArray( new IndexRecord[RecordNum] );
    }

    /**
     * method: getIndexArray() <BR>
     * clones local field indexArray <BR>
     *
     * @return the index record
     */
    public IndexRecord[] getIndexArray() {
        return indexArray;
    }

    /**
     * method: getRecordNum() <BR>
     * function to get number of Records <BR>
     *
     * @return number of Records <BR>
     */
    public int getRecordNum() {
        return RecordNum;
    }

    /**
     * methode: getRecordOffset (int RecNo) <BR>
     * function to get Record offset by Record number <BR>
     *
     * @param RecNo
     * @return offset by Record number <BR>
     */
    public int getRecordOffset( int RecNo ) {
        if ( RecNo >= 0 ) {
            return indexArray[RecNo].offset;
        }
        return -1;
    }

    /**
     * method: getRecordLength (int RecNo) <BR>
     * function to get Record Length by Record number <BR>
     *
     * @param RecNo
     * @return Record Length by Record number <BR>
     */
    public int getRecordLength( int RecNo ) {
        if ( RecNo >= 0 ) {
            return indexArray[RecNo].length;
        }
        return -1;
    }

    /**
     * method: getIndexRecord (int RecNo) <BR>
     * function to get Index Record by Record number <BR>
     *
     * @param RecNo
     * @return Index Record by Record number <BR>
     */
    public IndexRecord getIndexRecord( int RecNo ) {
        IndexRecord ir = new IndexRecord();
        if ( RecNo >= 0 ) {
            return ir = indexArray[RecNo];
        }
        return ir;
    }

    /**
     * appends an index record to the indexfile
     *
     * @param record
     * @param mbr
     * @throws IOException
     */
    public void appendRecord( IndexRecord record, SHPEnvelope mbr )
                            throws IOException {
        offset = raf.length();
        raf.seek( offset );
        raf.write( record.writeIndexRecord() );
        offset = offset + INDEX_RECORD_LENGTH;
        // actualize mbr
        if ( fileMBR.west > mbr.west ) {
            fileMBR.west = mbr.west;
        }
        if ( fileMBR.east < mbr.east ) {
            fileMBR.east = mbr.east;
        }
        if ( fileMBR.south > mbr.south ) {
            fileMBR.south = mbr.south;
        }
        if ( fileMBR.north < mbr.north ) {
            fileMBR.north = mbr.north;
        }
        raf.seek( 36 );
        raf.write( fileMBR.writeLESHPEnvelope() );

        // actualize file length
        filelength = (int) offset / 2;
    }
}
