//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
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
package org.deegree.record.persistence.dc;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMNamespace;
import org.deegree.commons.configuration.JDBCConnections;
import org.deegree.commons.configuration.PooledConnection;
import org.deegree.commons.jdbc.ConnectionManager;
import org.deegree.commons.utils.time.DateUtils;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.protocol.csw.CSWConstants.ResultType;
import org.deegree.protocol.csw.CSWConstants.SetOfReturnableElements;
import org.deegree.protocol.csw.CSWConstants.ConstraintLanguage;
import org.deegree.record.persistence.RecordStore;
import org.deegree.record.persistence.RecordStoreException;

/**
 * {@link RecordStore} implementation of Dublin Core.
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DCRecordStore implements RecordStore {

    public String output;

    private final QName typeNames = new QName( "", "Record", "csw" );

    private String connectionId;

    private String filterExpression;

    public DCRecordStore( String connectionId ) {
        this.connectionId = connectionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#describeRecord(javax.xml.stream.XMLStreamWriter)
     */
    @Override
    public void describeRecord() {

        // statt n File wird hier die DB angefragt!
        File file = new File( "/home/thomas/workspace/d3_core/src/org/deegree/record/persistence/dc/dc.xsd" );

        // if(typeNames.equals( new QName("","Record", "csw") )){
        XMLAdapter ada = new XMLAdapter( file );

        System.out.println( ada.toString() );
        output = ada.toString();
        OMNamespace elem = ada.getRootElement().getDefaultNamespace();
        // ada.getNamespaceContext( ada.getRootElement() );

        // this.typeNames = new QName(elem.getNamespaceURI());
        // }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#destroy()
     */
    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#init()
     */
    @Override
    public void init()
                            throws RecordStoreException {

        // TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getTypeNames()
     */
    @Override
    public QName getTypeName() {

        // int i = SELECT ID FROM formattype WHERE type = 'csw:Record'
        // if(i == 1){ new QName("", "Record", "csw");

        return typeNames;
    }

    /**
     * @return the output
     */
    public String getOutput() {
        return output;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.deegree.record.persistence.RecordStore#getRecords(javax.xml.stream.XMLStreamWriter,
     * javax.xml.namespace.QName)
     */
    @Override
    public void getRecords( XMLStreamWriter writer, QName typeName, SetOfReturnableElements returnableElement,
                            JDBCConnections con, ResultType resultType, ConstraintLanguage constraintLanguage, String constraint, String namespace )
                            throws SQLException, XMLStreamException {

        switch ( resultType ) {
        case results:

            doResultsOnGetRecord( writer, typeName, returnableElement, con,  namespace );
            break;
        case hits:

            doHitsOnGetRecord( writer, typeName, returnableElement, con );
            break;
        case validate:

            doValidateOnGetRecord( writer, typeName, returnableElement, con );
            break;
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doHitsOnGetRecord( XMLStreamWriter writer, QName typeName, SetOfReturnableElements returnableElement,
                                    JDBCConnections con )
                            throws SQLException, XMLStreamException {

        int countRows = 0;
        String selectStMt = "SELECT count(ds.id) FROM datasets AS ds ";// + sqlFilterExpression;

        // ConnectionManager.addConnections( con );
        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );
            ResultSet rs = conn.createStatement().executeQuery( selectStMt );

            while ( rs.next() ) {
                countRows = rs.getInt( 1 );
                System.out.println( rs.getInt( 1 ) );
            }

            writer.writeAttribute( "elementSet", returnableElement.name() );

            // writer.writeAttribute( "recordSchema", "");

            writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

            writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( 0 ) );

            writer.writeAttribute( "nextRecord", Integer.toString( 1 ) );

            writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );

            rs.close();
            conn.close();
        }

    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param formatType
     * @param returnableElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doResultsOnGetRecord( XMLStreamWriter writer, QName typeName,
                                       SetOfReturnableElements returnableElement, JDBCConnections con, String namespace )
                            throws SQLException, XMLStreamException {

        int countRows = 0;
        String selectCountRows = "SELECT count(id) FROM datasets";

        for ( PooledConnection pool : con.getPooledConnection() ) {
            Connection conn = ConnectionManager.getConnection( connectionId );

            ResultSet rsCountRows = conn.createStatement().executeQuery( selectCountRows );
            while ( rsCountRows.next() ) {
                countRows = rsCountRows.getInt( 1 );
                System.out.println( rsCountRows.getInt( 1 ) );
            }

            writer.writeAttribute( "elementSet", returnableElement.name() );

            // writer.writeAttribute( "recordSchema", "");

            writer.writeAttribute( "numberOfRecordsMatched", Integer.toString( countRows ) );

            writer.writeAttribute( "numberOfRecordsReturned", Integer.toString( countRows ) );

            // TODO static at the moment...should be considered to change to dynamic
            // in addition with maxRecords
            writer.writeAttribute( "nextRecord", Integer.toString( 0 ) );

            writer.writeAttribute( "expires", DateUtils.formatISO8601Date( new Date() ) );

            String formatType = null;
            switch ( returnableElement ) {

            case brief:
                formatType = "recordbrief";
                String selectBrief = "SELECT rb.data FROM datasets AS ds, " + formatType
                                     + " AS rb WHERE rb.fk_datasets = ds.id ";
                ResultSet rsBrief = conn.createStatement().executeQuery( selectBrief );

                while ( rsBrief.next() ) {
                    String result = rsBrief.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsBrief.close();
                break;
            case summary:
                formatType = "recordsummary";
                String selectSummary = "SELECT rb.data FROM datasets AS ds, " + formatType
                                       + " AS rb WHERE rb.fk_datasets = ds.id";
                ResultSet rsSummary = conn.createStatement().executeQuery( selectSummary );

                while ( rsSummary.next() ) {
                    String result = rsSummary.getString( 1 );

                    readXMLFragment( result, writer );

                }

                rsSummary.close();
                break;
            case full:
                formatType = "recordfull";

                String selectFull = "SELECT rb.data FROM datasets AS ds, " + formatType
                                    + " AS rb WHERE rb.fk_datasets = ds.id ";
                ResultSet rsFull = conn.createStatement().executeQuery( selectFull );

                while ( rsFull.next() ) {
                    String result = rsFull.getString( 1 );

                    readXMLFragment( result, writer );

                }
                rsFull.close();

                break;
            }// muss dann noch gecatcht werden

            conn.close();
        }

    }

    /**
     * Reads a valid XML fragment
     * 
     * @param result
     * @param xmlWriter
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */
    private void readXMLFragment( String result, XMLStreamWriter xmlWriter ) {

        XMLStreamReader xmlReader;
        try {
            xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( new StringReader( result ) );

            // skip START_DOCUMENT
            xmlReader.nextTag();

            XMLAdapter.writeElement( xmlWriter, xmlReader );

            xmlReader.close();

        } catch ( XMLStreamException e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch ( FactoryConfigurationError e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
    
    private String getFilterExpression(){
        
        String filter = "";
        
        
        return "";
    }
    
    public void setFilterExpression(String filterExpression){
        
    }

    /**
     * 
     * @param writer
     * @param typeName
     * @param returnatbleElement
     * @param con
     * @throws SQLException
     * @throws XMLStreamException
     */
    private void doValidateOnGetRecord( XMLStreamWriter writer, QName typeName,
                                        SetOfReturnableElements returnatbleElement, JDBCConnections con )
                            throws SQLException, XMLStreamException {
        // TODO Auto-generated method stub

    }
    
    
    /**
     * Transformation operation for the parsed filter expression.
     * 
     * @param constraint
     * @param constraintLanguage
     * @return
     */
    private String transformFilterExpression( ConstraintLanguage constraintLanguage, String constraint ) {

        String isoqp_title = "";
        String rest = "";
        constraint = constraint.replace( "\"", "" );

        for ( String s : constraint.split( " = " ) ) {
            if ( s.equals( "title" ) ) {
                isoqp_title = "isoqp_title";
            } else {
                rest = s;
            }
        }

        String sqlExpression = "INNER JOIN " + isoqp_title + " ON (ds.id = " + isoqp_title + ".fk_datasets) WHERE " + isoqp_title + ".title = " + rest;

        return sqlExpression;
    }

}
