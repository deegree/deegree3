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
package org.deegree.tools.datastore;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.util.Properties;

import org.deegree.datatypes.Types;
import org.deegree.datatypes.parameter.InvalidParameterNameException;
import org.deegree.framework.util.StringTools;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class enables a user to add a new property to a deegree WFS feature type definition. It is
 * possible to add a simple property from the feature types major table, a simple property from
 * another table and a complex property from another already available feature type.
 *
 *
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ModifyFTProperties {

    private static NamespaceContext nsCntxt = CommonNamespaces.getNamespaceContext();

    private static URI xsd = CommonNamespaces.XSNS;

    private static URI dgwfs = CommonNamespaces.DEEGREEWFS;

    private URL ftDefFile;

    private String featureType;

    private String propertyName;

    private String source;

    private String from;

    private String to;

    private int relType = 0;

    private String databaseFieldName;

    private int type = 0;

    /**
     *
     * @param ftDefFile
     *            schema file containing feature type defintion
     * @param featureType
     *            qualified name of the feature to enhance
     * @param propertyName
     *            name of the new property
     * @param databaseFieldName
     * @param type
     *            type code of the ne2 property (@see org.deegree.datatypes.Types)
     */
    public ModifyFTProperties( URL ftDefFile, String featureType, String propertyName, String databaseFieldName,
                               int type ) {
        this.ftDefFile = ftDefFile;
        this.featureType = featureType;
        this.propertyName = propertyName;
        this.type = type;
        this.databaseFieldName = databaseFieldName;
    }

    /**
     *
     * @param ftDefFile
     *            schema file containing feature type defintion
     * @param featureType
     *            qualified name of the feature to enhance
     * @param propertyName
     *            name of the new property
     * @param databaseFieldName
     * @param table
     * @param from
     * @param to
     * @param type
     *            type code of the new property (@see org.deegree.datatypes.Types)
     * @param relType
     */
    public ModifyFTProperties( URL ftDefFile, String featureType, String propertyName, String databaseFieldName,
                               String table, String from, String to, int type, int relType ) {
        this.ftDefFile = ftDefFile;
        this.featureType = featureType;
        this.propertyName = propertyName;
        this.type = type;
        this.source = table;
        this.from = from;
        this.to = to;
        this.relType = relType;
        this.databaseFieldName = databaseFieldName;
    }

    /**
     * adds a property from the feature types major table
     *
     * @throws Exception
     */
    public void addSimplePropertyFromMainTable()
                            throws Exception {

        XMLFragment xml = new XMLFragment();
        xml.load( ftDefFile );

        if ( doesPropertyAlreadyExist( xml, propertyName ) ) {
            throw new InvalidParameterNameException( "Property already exits", "propertyName" );
        }

        Element cType = getPropertyParent( xml );

        Element elem = XMLTools.appendElement( cType, xsd, "element" );
        elem.setAttribute( "name", propertyName );
        elem.setAttribute( "type", "xsd:" + Types.getXSDTypeForSQLType( type, 1, 0 ) );
        Element el = XMLTools.appendElement( elem, xsd, "annotation" );
        el = XMLTools.appendElement( el, xsd, "appinfo" );
        el = XMLTools.appendElement( el, dgwfs, "deegreewfs:Content" );
        el = XMLTools.appendElement( el, dgwfs, "deegreewfs:MappingField" );
        el.setAttribute( "field", databaseFieldName );
        el.setAttribute( "type", Types.getTypeNameForSQLTypeCode( type ) );

        File file = new File( ftDefFile.getFile() );
        FileOutputStream fos = new FileOutputStream( file );
        xml.write( fos );
        fos.close();
    }

    /**
     * returns the parent node where to add the additional property
     *
     * @param xml
     * @return the parent node where to add the additional property
     * @throws XMLParsingException
     */
    private Element getPropertyParent( XMLFragment xml )
                            throws XMLParsingException {
        String xpath = StringTools.concat( 100, "xs:complexType[./@name = '", featureType, "Type']/xs:complexContent/",
                                           "xs:extension/xs:sequence" );
        return (Element) XMLTools.getNode( xml.getRootElement(), xpath, nsCntxt );
    }

    /**
     * returns true if a property with the same name as the one to add already exists for a feature
     * type
     *
     * @param xml
     * @param propertyName
     * @return true if property already exists
     * @throws XMLParsingException
     */
    private boolean doesPropertyAlreadyExist( XMLFragment xml, String propertyName )
                            throws XMLParsingException {
        String xPath = ".//xsd:element[./@name = '" + propertyName + "']";
        nsCntxt.addNamespace( "xsd", xsd );
        Node node = XMLTools.getNode( xml.getRootElement(), xPath, nsCntxt );
        return node != null;
    }

    /**
     * @throws Exception
     */
    public void addSimplePropertyFromOtherTable()
                            throws Exception {
        XMLFragment xml = new XMLFragment();
        xml.load( ftDefFile );

        if ( doesPropertyAlreadyExist( xml, propertyName ) ) {
            throw new InvalidParameterNameException( "Property already exits", "propertyName" );
        }

        Element cType = getPropertyParent( xml );

        Element elem = XMLTools.appendElement( cType, xsd, "element" );
        elem.setAttribute( "name", propertyName );
        elem.setAttribute( "type", "xsd:" + Types.getXSDTypeForSQLType( type, 1, 0 ) );
        Element el = XMLTools.appendElement( elem, xsd, "annotation" );
        el = XMLTools.appendElement( el, xsd, "appinfo" );
        el = XMLTools.appendElement( el, dgwfs, "deegreewfs:Content" );
        Element mfElem = XMLTools.appendElement( el, dgwfs, "deegreewfs:MappingField" );
        mfElem.setAttribute( "field", databaseFieldName );
        mfElem.setAttribute( "type", Types.getTypeNameForSQLTypeCode( type ) );

        // append relation informations
        Element relElem = XMLTools.appendElement( el, dgwfs, "deegreewfs:Relation" );
        el = XMLTools.appendElement( relElem, dgwfs, "deegreewfs:From" );
        el = XMLTools.appendElement( el, dgwfs, "deegreewfs:MappingField" );
        el.setAttribute( "field", from );
        el.setAttribute( "type", Types.getTypeNameForSQLTypeCode( relType ) );
        el = XMLTools.appendElement( relElem, dgwfs, "deegreewfs:To" );
        el = XMLTools.appendElement( el, dgwfs, "deegreewfs:MappingField" );
        el.setAttribute( "field", to );
        el.setAttribute( "type", Types.getTypeNameForSQLTypeCode( relType ) );
        el.setAttribute( "table", source );

        File file = new File( ftDefFile.getFile() );
        FileOutputStream fos = new FileOutputStream( file );
        xml.write( fos );
        fos.close();
    }

    /**
     *
     */
    public void addComplexProperty() {
        // TODO
    }

    private static boolean validate( Properties map ) {
        if ( map.getProperty( "-action" ) == null ) {
            return false;
        }
        if ( map.getProperty( "-xsd" ) == null ) {
            return false;
        }
        if ( map.getProperty( "-featureType" ) == null ) {
            return false;
        }
        if ( map.getProperty( "-propertyName" ) == null ) {
            return false;
        }
        return true;
    }

    private static void printHelp() {
        System.out.println( "properties:" );
        System.out.println( "-action (addProperty|removeProperty)" );
        System.out.println( "-xsd" );
        System.out.println( "-featureType" );
        System.out.println( "-propertyName" );
        System.out.println( "-type (simple|complex)" );
        System.out.println( "must be set!" );
        System.out.println( "If -source is set " );
        System.out.println( "-fkSource" );
        System.out.println( "-fkTarget" );
        System.out.println( "-fkType" );
        System.out.println( "must be set too!" );
    }

    /**
     * @param args
     * @throws Exception
     */
    public static void main( String[] args )
                            throws Exception {

        Properties map = new Properties();
        for ( int i = 0; i < args.length; i += 2 ) {
            System.out.println( args[i + 1] );
            map.put( args[i], args[i + 1] );
        }
        if ( !validate( map ) ) {
            printHelp();
            return;
        }

        String action = map.getProperty( "-action" );
        URL url = new URL( map.getProperty( "-xsd" ) );
        String ft = map.getProperty( "-featureType" );
        String prop = map.getProperty( "-propertyName" );
        if ( "addProperty".equals( action ) ) {
            String field = map.getProperty( "-fieldName" );
            int type = Types.getTypeCodeForSQLType( map.getProperty( "-propertyType" ) );
            if ( "simple".equals( map.getProperty( "-type" ) ) && map.getProperty( "-source" ) == null ) {
                ModifyFTProperties add = new ModifyFTProperties( url, ft, prop, field, type );
                add.addSimplePropertyFromMainTable();
            }
            if ( "simple".equals( map.getProperty( "-type" ) ) && map.getProperty( "-source" ) != null ) {
                String table = map.getProperty( "-source" );
                String from = map.getProperty( "-fkSource" );
                String to = map.getProperty( "-fkTarget" );
                int fkType = Types.getTypeCodeForSQLType( map.getProperty( "-fkType" ) );
                ModifyFTProperties add = new ModifyFTProperties( url, ft, prop, field, table, from, to, fkType, type );
                add.addSimplePropertyFromOtherTable();
            } else if ( "complex".equals( map.getProperty( "-type" ) ) ) {
                // TODO
                throw new Exception( "not supported yet" );
            } else {
                throw new Exception( "not supported operation" );
            }

        } else if ( "removeProperty".equals( action ) ) {
            // TODO
            throw new Exception( "not supported yet" );
        } else {
            throw new Exception( "not supported operation" );
        }

    }

}
