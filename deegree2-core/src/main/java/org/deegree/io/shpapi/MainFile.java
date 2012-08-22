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
import java.io.IOException;
import java.io.RandomAccessFile;

import org.deegree.model.spatialschema.ByteUtils;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * Uses class ByteUtils modified from the original package com.bbn.openmap.layer.shape <br>
 * Copyright (C) 1998 BBN Corporation 10 Moulton St. Cambridge, MA 02138 <br>
 * 
 * @version 16.08.2000
 * @author Andreas Poth
 * 
 */
public class MainFile {

    /*
     * A buffer for current record's header.
     */
    protected byte[] recHdr = new byte[ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH];

    private byte[] envRecBuf = new byte[36];

    private byte[] dataRecBuf = new byte[36];

    /*
     * instance variables
     */
    private FileHeader fh;

    private IndexFile shx;

    /*
     * file suffixes for shp
     */
    private static final String _shp = ".shp";

    /*
     * references to the main file
     */
    private RandomAccessFile raf;

    /**
     * Construct a MainFile from a file name.
     * 
     * @param url
     * @throws IOException
     */
    public MainFile( String url ) throws IOException {

        // creates raf
        raf = new RandomAccessFile( url + _shp, "r" );

        fh = new FileHeader( raf );

        shx = new IndexFile( url );

    }

    /**
     * Construct a MainFile from a file name.
     * 
     * @param url
     * @param rwflag
     * @throws IOException
     */
    public MainFile( String url, String rwflag ) throws IOException {

        // delet file if it exists
        File file = new File( url + _shp );

        if ( rwflag.indexOf( 'w' ) > -1 && file.exists() )
            file.delete();
        file = null;

        // creates raf
        raf = new RandomAccessFile( url + _shp, rwflag );

        fh = new FileHeader( raf, rwflag.indexOf( 'w' ) > -1 );

        shx = new IndexFile( url, rwflag );

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
        try {
            shx.close();
        } catch ( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * method: getFileMBR()<BR>
     * returns the minimum bounding rectangle of geometries<BR>
     * within the shape-file
     * 
     * @return the minimum bounding rectangle of geometries<BR>
     */
    public SHPEnvelope getFileMBR() {

        return fh.getFileMBR();

    }

    /**
     * method: getRecordNum()<BR>
     * returns the number of record with in a shape-file<BR>
     * 
     * @return the number of record with in a shape-file<BR>
     */
    public int getRecordNum() {

        return shx.getRecordNum();

    }

    /**
     * method: getRecordMBR(int RecNo)<BR>
     * returns the minimum bound rectangle of RecNo's Geometrie of the shape-file<BR>
     * 
     * @param RecNo
     * @return the minimum bound rectangle of RecNo's Geometrie of the shape-file<BR>
     * @throws IOException
     */
    public SHPEnvelope getRecordMBR( int RecNo )
                            throws IOException {

        SHPEnvelope recordMBR = null;

        // index in IndexArray (see IndexFile)
        int iaIndex = RecNo - 1;

        int off = shx.getRecordOffset( iaIndex );

        // off holds the offset of the shape-record in 16-bit words (= 2 byte)
        // multiply with 2 gets number of bytes to seek
        long rafPos = off * 2;

        // fetch shape record
        raf.seek( rafPos + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );

        if ( raf.read( envRecBuf, 0, envRecBuf.length ) != -1 ) {

            int shpType = ByteUtils.readLEInt( envRecBuf, 0 );

            // only for PolyLines, Polygons and MultiPoints minimum bounding rectangles are defined
            if ( shpType == ShapeConst.SHAPE_TYPE_POLYLINE || shpType == ShapeConst.SHAPE_TYPE_POLYGON
                 || shpType == ShapeConst.SHAPE_TYPE_MULTIPOINT ) {
                recordMBR = new SHPEnvelope( envRecBuf );
            } // end if shpType
        } // end if result

        return recordMBR;
    }

    /**
     * method: getByRecNo (int RecNo)<BR>
     * retruns a ShapeRecord-Geometry by RecorcNumber<BR>
     * 
     * @param RecNo
     * @return a ShapeRecord-Geometry by RecorcNumber<BR>
     * @throws IOException
     */
    public SHPGeometry getByRecNo( int RecNo )
                            throws IOException {

        SHPGeometry shpGeom = null;

        // index in IndexArray (see IndexFile)
        int iaIndex = RecNo - 1;

        int off = shx.getRecordOffset( iaIndex );

        // calculate length from 16-bit words (= 2 bytes) to lenght in bytes
        int len = shx.getRecordLength( iaIndex ) * 2;

        // off holds the offset of the shape-record in 16-bit words (= 2 byte)
        // multiply with 2 gets number of bytes to seek
        long rafPos = off * 2;

        // fetch record header
        raf.seek( rafPos );

        // fetch shape record
        raf.seek( rafPos + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );

        if ( len > dataRecBuf.length ) {
            dataRecBuf = new byte[len];
        }

        if ( raf.read( dataRecBuf, 0, len ) != -1 ) {

            int shpType = ByteUtils.readLEInt( dataRecBuf, 0 );

            // create a geometry out of record buffer with shapetype
            if ( shpType == ShapeConst.SHAPE_TYPE_POINT ) {
                shpGeom = new SHPPoint( dataRecBuf, 4 );
            } else if ( shpType == ShapeConst.SHAPE_TYPE_MULTIPOINT ) {
                shpGeom = new SHPMultiPoint( dataRecBuf );
            } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYLINE ) {
                shpGeom = new SHPPolyLine( dataRecBuf );
            } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYGON ) {
                shpGeom = new SHPPolygon( dataRecBuf );
            } else if ( shpType == ShapeConst.SHAPE_TYPE_POLYGONZ ) {
                shpGeom = new SHPPolygon3D( dataRecBuf );
            }

        } // end if result

        return shpGeom;

    }

    /**
     * method: getShapeType(int RecNo)<BR>
     * returns the minimum bound rectangle of RecNo's Geometrie of the shape-file<BR>
     * 
     * @param RecNo
     * @return the minimum bound rectangle of RecNo's Geometrie of the shape-file<BR>
     * @throws IOException
     */
    public int getShapeTypeByRecNo( int RecNo )
                            throws IOException {

        int shpType = -1;

        // index in IndexArray (see IndexFile)
        int iaIndex = RecNo - 1;

        int off = shx.getRecordOffset( iaIndex );

        // calculate length from 16-bit words (= 2 bytes) to length in bytes
        int len = 4;// shx.getRecordLength( iaIndex ) * 2;

        // off holds the offset of the shape-record in 16-bit words (= 2 byte)
        // multiply with 2 gets number of bytes to seek
        long rafPos = off * 2;

        // fetch shape record
        raf.seek( rafPos + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH );

        byte[] recBuf_ = new byte[len];

        if ( raf.read( recBuf_, 0, len ) != -1 ) {
            shpType = ByteUtils.readLEInt( recBuf_, 0 );
        } // end if result

        return shpType;
    }

    /**
     * method: public void write(byte[] bytearray)<BR>
     * appends a bytearray to the shape file<BR>
     * 
     * @param bytearray
     * @param record
     * @param mbr
     * @throws IOException
     */
    public void write( byte[] bytearray, IndexRecord record, SHPEnvelope mbr )
                            throws IOException {
        raf.seek( record.offset * 2 );
        raf.write( bytearray );
        shx.appendRecord( record, mbr );
    }

    /**
     * method: public void writeHeader(int filelength, byte shptype, SHPEnvelope mbr)<BR>
     * writes a header to the shape and index file<BR>
     * 
     * @param filelength
     * @param shptype
     * @param mbr
     * @throws IOException
     */
    public void writeHeader( int filelength, byte shptype, SHPEnvelope mbr )
                            throws IOException {
        fh.writeHeader( filelength, shptype, mbr );
        shx.writeHeader( shptype, mbr );
    }

}
