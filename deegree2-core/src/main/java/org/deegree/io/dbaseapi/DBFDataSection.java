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

package org.deegree.io.dbaseapi;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Class representing a record of the data section of a dBase III/IV file<BR>
 * at the moment only the daata types character ("C") and numeric ("N") are supported
 * 
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DBFDataSection {

    /**
     * length of one record in bytes
     */
    private int recordlength = 0;

    private FieldDescriptor[] fieldDesc = null;

    private List<ByteContainer> data = new ArrayList<ByteContainer>();

    /**
     * constructor
     */
    public DBFDataSection( FieldDescriptor[] fieldDesc ) {

        this.fieldDesc = fieldDesc;

        // calculate length of the data section
        recordlength = 0;
        for ( int i = 0; i < this.fieldDesc.length; i++ ) {

            byte[] fddata = this.fieldDesc[i].getFieldDescriptor();

            recordlength += fddata[16];

            fddata = null;

        }

        recordlength++;

    }

    /**
     * method: public setRecord(ArrayList recData) writes a data record to byte array representing the data section of
     * the dBase file. The method gets the data type of each field in recData from fieldDesc wich has been set at the
     * constructor.
     */
    public void setRecord( List<?> recData )
                            throws DBaseException {

        setRecord( data.size(), recData );

    }

    /**
     * method: public setRecord(int index, ArrayList recData) writes a data record to byte array representing the data
     * section of the dBase file. The method gets the data type of each field in recData from fieldDesc wich has been
     * set at the constructor. index specifies the location of the retrieved record in the datasection. if an invalid
     * index is used an exception will be thrown
     */
    public void setRecord( int index, List<?> recData )
                            throws DBaseException {

        ByteContainer datasec = new ByteContainer( recordlength );

        if ( ( index < 0 ) || ( index > data.size() ) )
            throw new DBaseException( "invalid index: " + index );

        if ( recData.size() != this.fieldDesc.length )
            throw new DBaseException( "invalid size of recData" );

        int offset = 0;

        datasec.data[offset] = 0x20;

        offset++;

        byte[] b = null;

        // write every field on the ArrayList to the data byte array
        for ( int i = 0; i < recData.size(); i++ ) {
            byte[] fddata = this.fieldDesc[i].getFieldDescriptor();
            switch ( fddata[11] ) {

            // if data type is character
            case (byte) 'C':

                if ( recData.get( i ) == null ) {
                    b = new byte[0];
                } else {
                    b = recData.get( i ).toString().getBytes();
                }
                // TODO
                // Maybe skip this and and trim to b.length   
                if ( b.length > fddata[16] ) {
                    throw new DBaseException( "string contains too many characters " + (String) recData.get( i ) );
                }
                for ( int j = 0; j < b.length; j++ )
                    datasec.data[offset + j] = b[j];
                for ( int j = b.length; j < fddata[16]; j++ )
                    datasec.data[offset + j] = 0x20;
                break;
            case (byte) 'N':
                if ( recData.get( i ) != null && !( recData.get( i ) instanceof Number ) )
                    throw new DBaseException( "invalid data type at field: " + i );
                if ( recData.get( i ) == null ) {
                    b = new byte[0];
                } else {
                    b = ( (Number) recData.get( i ) ).toString().getBytes();
                }
                if ( b.length > fddata[16] )
                    throw new DBaseException( "string contains too many characters " + (String) recData.get( i ) );
                for ( int j = 0; j < b.length; j++ )
                    datasec.data[offset + j] = b[j];
                for ( int j = b.length; j < fddata[16]; j++ )
                    datasec.data[offset + j] = 0x0;
                break;
            case (byte) 'D':
                if ( recData.get( i ) != null && !( recData.get( i ) instanceof Date ) )
                    throw new DBaseException( "invalid data type at field: " + i );
                if ( recData.get( i ) == null ) {
                    b = new byte[0];
                } else {
                    SimpleDateFormat sdf_ = new SimpleDateFormat( "yyyy-MM-dd", Locale.getDefault() );
                    b = sdf_.format( (Date) recData.get( i )  ).getBytes();
                }                
                if ( b.length > fddata[16] )
                    throw new DBaseException( "string contains too many characters " + (Date) recData.get( i ) );
                for ( int j = 0; j < b.length; j++ )
                    datasec.data[offset + j] = b[j];
                for ( int j = b.length; j < fddata[16]; j++ )
                    datasec.data[offset + j] = 0x0;
                break;
              default: {
                System.out.println("TTTT" + (char)fddata[11] );
                throw new DBaseException( "data type not supported" );
            }

            }

            offset += fddata[16];

        }

        // puts the record to the ArrayList (container)
        data.add( index, datasec );

    }

    /**
     * method: public byte[] getDataSection() returns the data section as a byte array.
     */
    public byte[] getDataSection() {

        // allocate memory for all datarecords on one array + 1 byte
        byte[] outdata = new byte[data.size() * recordlength + 1];

        // set the file terminating byte
        outdata[outdata.length - 1] = 0x1A;

        // get all records from the ArrayList and put it
        // on a single array
        int j = 0;
        for ( int i = 0; i < data.size(); i++ ) {
            ByteContainer bc = data.get( i );
            for ( int k = 0; k < recordlength; k++ ) {
                outdata[j++] = bc.data[k];
            }
        }
        return outdata;
    }

    public void getDataSection( OutputStream os )
                            throws IOException {

        // get all records from the ArrayList and write it into a stream
        for ( int i = 0; i < data.size(); i++ ) {
            ByteContainer bc = data.get( i );
            for ( int k = 0; k < recordlength; k++ ) {
                os.write( bc.data[k] );
            }

        }
        // set the file terminating byte        
        os.write( 0x1A );
    }

    /**
     * method: public int getNoOfRecords() returns the number of records within the container
     */
    public int getNoOfRecords() {
        return data.size();
    }

}

class ByteContainer {

    public byte[] data = null;

    public ByteContainer( int size ) {

        data = new byte[size];

    }

}
