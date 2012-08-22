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
package org.deegree.io.datastore.schema;

import static org.deegree.framework.xml.XMLTools.getElements;
import static org.deegree.framework.xml.XMLTools.getNodeAsBoolean;
import static org.deegree.framework.xml.XMLTools.getNodeAsQualifiedName;
import static org.deegree.framework.xml.XMLTools.getRequiredElement;
import static org.deegree.framework.xml.XMLTools.getRequiredNodeAsQualifiedName;
import static org.deegree.ogcbase.CommonNamespaces.DEEGREEWFS_PREFIX;
import static org.deegree.ogcbase.CommonNamespaces.XS_PREFIX;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.deegree.datatypes.QualifiedName;
import org.deegree.datatypes.Types;
import org.deegree.datatypes.UnknownTypeException;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.util.Pair;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.framework.xml.schema.ComplexTypeDeclaration;
import org.deegree.framework.xml.schema.ElementDeclaration;
import org.deegree.framework.xml.schema.SimpleTypeDeclaration;
import org.deegree.framework.xml.schema.TypeDeclaration;
import org.deegree.framework.xml.schema.TypeReference;
import org.deegree.framework.xml.schema.XMLSchemaException;
import org.deegree.i18n.Messages;
import org.deegree.io.datastore.AnnotationDocument;
import org.deegree.io.datastore.Datastore;
import org.deegree.io.datastore.DatastoreConfiguration;
import org.deegree.io.datastore.DatastoreRegistry;
import org.deegree.io.datastore.idgenerator.IdGenerator;
import org.deegree.io.datastore.schema.MappedGMLId.IDPART_INFO;
import org.deegree.io.datastore.schema.TableRelation.FK_INFO;
import org.deegree.io.datastore.schema.content.ConstantContent;
import org.deegree.io.datastore.schema.content.FieldContent;
import org.deegree.io.datastore.schema.content.FunctionParam;
import org.deegree.io.datastore.schema.content.MappingField;
import org.deegree.io.datastore.schema.content.MappingGeometryField;
import org.deegree.io.datastore.schema.content.SQLFunctionCall;
import org.deegree.io.datastore.schema.content.SimpleContent;
import org.deegree.io.datastore.schema.content.SpecialContent;
import org.deegree.model.crs.UnknownCRSException;
import org.deegree.model.feature.schema.GMLSchemaDocument;
import org.deegree.model.feature.schema.PropertyType;
import org.deegree.ogcbase.CommonNamespaces;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Parser for GML schema documents which are annotated with mapping (persistence) information.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider </a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class MappedGMLSchemaDocument extends GMLSchemaDocument {

    private static final long serialVersionUID = 8293629056821438839L;

    private static final ILogger LOG = LoggerFactory.getLogger( MappedGMLSchemaDocument.class );

    private String namespacePrefix;

    private URI defaultSRS;

    private boolean suppressXLinkOutput;

    private DatastoreConfiguration dsConfiguration;

    /**
     * Returns the class representation of the underlying mapped GML schema document.
     *
     * @return the class representation of the underlying mapped GML schema document
     * @throws XMLParsingException
     * @throws XMLSchemaException
     * @throws UnknownCRSException
     */
    public MappedGMLSchema parseMappedGMLSchema()
                            throws XMLParsingException, XMLSchemaException, UnknownCRSException {
        parseGlobalAnnotations();
        SimpleTypeDeclaration[] simpleTypes = extractSimpleTypeDeclarations();
        ComplexTypeDeclaration[] complexTypes = extractComplexTypeDeclarations();
        ElementDeclaration[] elementDeclarations = extractElementDeclarations();
        return new MappedGMLSchema( getTargetNamespace(), simpleTypes, complexTypes, elementDeclarations,
                                    this.namespacePrefix, this.defaultSRS, this.dsConfiguration,
                                    this.suppressXLinkOutput, this );
    }

    /**
     * Parses the global "xs:annotation/xs:appinfo" block.
     * <p>
     * Delegates the datastore specific configuration options to the responsible {@link AnnotationDocument} parser.
     *
     * @throws XMLParsingException
     */
    @SuppressWarnings("unchecked")
    private void parseGlobalAnnotations()
                            throws XMLParsingException {

        Element appinfoElement = (Element) XMLTools.getRequiredNode( getRootElement(), "xs:annotation/xs:appinfo",
                                                                     nsContext );
        this.namespacePrefix = XMLTools.getRequiredNodeAsString( appinfoElement, "deegreewfs:Prefix", nsContext );
        String backend = XMLTools.getRequiredNodeAsString( appinfoElement, "deegreewfs:Backend", nsContext );
        this.suppressXLinkOutput = XMLTools.getNodeAsBoolean( appinfoElement, "deegreewfs:SuppressXLinkOutput/text()",
                                                              nsContext, false );
        this.defaultSRS = XMLTools.getRequiredNodeAsURI( appinfoElement, "deegreewfs:DefaultSRS", nsContext );

        Class<Datastore> datastoreClass = null;
        try {
            datastoreClass = DatastoreRegistry.getDatastoreClass( backend );
        } catch ( IllegalArgumentException e ) {
            String msg = Messages.getMessage( "DATASTORE_UNKNOWN_TYPE_CODE", backend );
            LOG.logInfo( msg );
            try {
                datastoreClass = (Class<Datastore>) Class.forName( backend );
            } catch ( ClassNotFoundException e1 ) {
                msg = Messages.getMessage( "DATASTORE_UNKNOWN_TYPE_AND_CLASS", backend );
                throw new XMLParsingException( msg );
            }
        }

        AnnotationDocument annotationParser = null;
        try {
            Datastore ds = datastoreClass.newInstance();
            annotationParser = ds.getAnnotationParser();
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "DATASTORE_CLASS_INSTANTIATION_ERROR", datastoreClass.getName(),
                                              e.getMessage() );
            throw new XMLParsingException( msg );
        }

        annotationParser.setRootElement( this.getRootElement() );
        annotationParser.setSystemId( this.getSystemId() );
        try {
            this.dsConfiguration = annotationParser.parseDatastoreConfiguration();
        } catch ( XMLParsingException e ) {
            LOG.logError( e.getMessage(), e );
            String msg = Messages.getMessage( "DATASTORE_CONFIGURATION_BLOCK_FAULTY", e.getMessage() );
            throw new XMLParsingException( msg );
        }
    }

    /**
     * Parses the given <code>Element</code> as an annotated 'xs:element' declaration (with mapping information).
     *
     * @param element
     *            'xs:element' declaration to be parsed
     * @return object representation of the declaration
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the limitations of this class
     */
    @Override
    protected MappedElementDeclaration parseElementDeclaration( Element element )
                            throws XMLParsingException {

        QualifiedName name = new QualifiedName( XMLTools.getRequiredNodeAsString( element, "@name", nsContext ),
                                                getTargetNamespace() );
        if ( name.getLocalName().length() == 0 ) {
            String msg = "Error in schema document. Empty name (\"\") in element declaration found.";
            throw new XMLSchemaException( msg );
        }

        boolean isAbstract = XMLTools.getNodeAsBoolean( element, "@abstract", nsContext, false );

        TypeReference typeReference = null;
        Node typeNode = XMLTools.getNode( element, "@type", nsContext );
        if ( typeNode != null ) {
            typeReference = new TypeReference( parseQualifiedName( typeNode ) );
        } else {
            if ( LOG.isDebug() ) {
                LOG.logDebug( "Trying to find a complexType below the element named " + element.getAttribute( "name" )
                              + "." );
            }
            // inline type declaration
            Element elem = (Element) XMLTools.getRequiredNode( element, getFullName( "complexType" ), nsContext );
            TypeDeclaration type = parseComplexTypeDeclaration( elem );
            typeReference = new TypeReference( type );
        }

        int minOccurs = XMLTools.getNodeAsInt( element, "@minOccurs", nsContext, 1 );
        int maxOccurs = -1;
        String maxOccursString = XMLTools.getNodeAsString( element, "@maxOccurs", nsContext, "1" );
        if ( !"unbounded".equals( maxOccursString ) ) {
            try {
                maxOccurs = Integer.parseInt( maxOccursString );
            } catch ( NumberFormatException e ) {
                throw new XMLParsingException( "Invalid value ('" + maxOccursString + "') in 'maxOccurs' attribute. "
                                               + "Must be a valid integer value or 'unbounded'." );
            }
        }

        QualifiedName substitutionGroup = null;
        Node substitutionGroupNode = XMLTools.getNode( element, "@substitutionGroup", nsContext );
        if ( substitutionGroupNode != null ) {
            substitutionGroup = parseQualifiedName( substitutionGroupNode );
        }

        Element annotationElement = (Element) XMLTools.getNode( element, getFullName( "annotation" ), nsContext );

        return new MappedElementDeclaration( name, isAbstract, typeReference, minOccurs, maxOccurs, substitutionGroup,
                                             annotationElement );
    }

    /**
     * Parses the given <code>Element</code> as an annotated 'xs:complexType' declaration (with mapping information).
     *
     * @param element
     *            'xs:complexType' declaration to be parsed
     * @return object representation of the declaration
     * @throws XMLParsingException
     *             if the document is not a valid XML Schema document or does not match the limitations of this class
     */
    @Override
    protected MappedComplexTypeDeclaration parseComplexTypeDeclaration( Element element )
                            throws XMLParsingException {

        QualifiedName name = null;
        String localName = XMLTools.getNodeAsString( element, "@name", nsContext, null );
        if ( localName != null ) {
            name = new QualifiedName( localName, getTargetNamespace() );
            if ( localName.length() == 0 ) {
                String msg = "Error in schema document. Empty name (\"\") for complexType " + "declaration found.";
                throw new XMLSchemaException( msg );
            }
        }

        List<Node> subElementList = null;
        TypeReference extensionBase = null;
        Node extensionBaseNode = XMLTools.getNode( element, getFullName( "complexContent/" )
                                                            + getFullName( "extension/@base" ), nsContext );
        if ( extensionBaseNode != null ) {
            extensionBase = new TypeReference( parseQualifiedName( extensionBaseNode ) );
            subElementList = XMLTools.getNodes( element, getFullName( "complexContent/" ) + getFullName( "extension/" )
                                                         + getFullName( "sequence/" ) + getFullName( "element" ),
                                                nsContext );
        } else {
            subElementList = XMLTools.getRequiredNodes( element, getFullName( "sequence/" ) + getFullName( "element" ),
                                                        nsContext );
        }

        ElementDeclaration[] subElements = new ElementDeclaration[subElementList.size()];
        for ( int i = 0; i < subElements.length; i++ ) {
            Element subElement = (Element) subElementList.get( i );
            subElements[i] = parseElementDeclaration( subElement );
        }
        Element annotationElement = (Element) XMLTools.getNode( element, getFullName( "annotation" ), nsContext );

        return new MappedComplexTypeDeclaration( name, extensionBase, subElements, annotationElement );
    }

    /**
     * Extracts the "gml:id" information from the given "xs:annotation" element.
     *
     * @param annotationElement
     *            "xs:annotation" element
     * @param defaultTableName
     *            name for table if "deegreewfs:table"-element is missing
     * @return "gml:id" information as MappedGMLId
     * @throws XMLSchemaException
     *             if a syntactic or semantic error is found
     */
    MappedGMLId extractGMLId( Element annotationElement, String defaultTableName )
                            throws XMLSchemaException {
        MappedGMLId gmlId = null;
        try {
            String table = XMLTools.getNodeAsString( annotationElement, "xs:appinfo/deegreewfs:table/text()",
                                                     nsContext, defaultTableName );
            Element gmlIdElement = (Element) XMLTools.getNode( annotationElement, "xs:appinfo/deegreewfs:gmlId",
                                                               nsContext );
            if ( gmlIdElement != null ) {
                gmlId = parseGMLIdDefinition( gmlIdElement, table );
            } else {
                MappingField[] idFields = new MappingField[] { new MappingField( table, "fid", Types.VARCHAR ) };
                IdGenerator idGenerator = IdGenerator.getInstance( IdGenerator.TYPE_UUID, new Properties() );
                gmlId = new MappedGMLId( defaultTableName.toUpperCase(), "_", idFields, idGenerator,
                                         IDPART_INFO.notIDPart );
            }
        } catch ( XMLParsingException e ) {
            throw new XMLSchemaException( e.getMessage(), e );
        }
        return gmlId;
    }

    /**
     * Extracts the mapping information for a simple property type from the given "xs:annotation" element.
     *
     * @param element
     *            "xs:annotation" element
     * @param propertyName
     *            name of the property (local part is used as default field name if it is not specified explicitly in
     *            the "MappingField" element)
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param table
     *            name of the table that is associated with the feature type that this property belongs to
     * @return simple property type with persistence information
     * @throws XMLSchemaException
     *             if a syntactic or semantic error is found
     */
    MappedSimplePropertyType parseMappedSimplePropertyType( Element element, QualifiedName propertyName, int type,
                                                            int minOccurs, int maxOccurs, boolean isIdentityPart,
                                                            String table )
                            throws XMLSchemaException {

        MappedSimplePropertyType pt = null;
        try {
            Element contentElement = (Element) XMLTools.getRequiredNode( element, "xs:appinfo/deegreewfs:Content",
                                                                         nsContext );
            // sanity check (common user error)
            Node typeNode = XMLTools.getNode( contentElement, "@type", nsContext );
            if ( typeNode != null ) {
                String msg = "Content element for simple property type '" + propertyName
                             + "' must not contain a type attribute.";
                throw new XMLParsingException( msg );
            }

            // table relations
            List<Node> relationElements = XMLTools.getNodes( contentElement, "deegreewfs:Relation", nsContext );
            TableRelation[] tableRelations = new TableRelation[relationElements.size()];
            String fromTable = table;
            for ( int i = 0; i < tableRelations.length; i++ ) {
                tableRelations[i] = parseTableRelation( (Element) relationElements.get( i ), fromTable );
                fromTable = tableRelations[i].getToTable();
            }

            SimpleContent content = null;

            // check type of content
            Node node = XMLTools.getNode( contentElement, "deegreewfs:MappingField", nsContext );
            if ( node != null ) {
                String defaultField = propertyName.getLocalName();
                content = parseMappingField( (Element) node, defaultField, table );
            } else {
                node = XMLTools.getNode( contentElement, "deegreewfs:Constant", nsContext );
                if ( node != null ) {
                    content = parseConstantContent( (Element) node );
                } else {
                    node = XMLTools.getNode( contentElement, "deegreewfs:SQLFunctionCall", nsContext );
                    if ( node != null ) {
                        content = parseSQLFunctionCall( (Element) node, table );
                    } else {
                        String msg = Messages.getMessage( "DATASTORE_ANNOTATION_SIMPLE_CONTENT_ERROR", propertyName );
                        throw new XMLParsingException( msg );
                    }
                }
            }

            pt = new MappedSimplePropertyType( propertyName, type, minOccurs, maxOccurs, isIdentityPart,
                                               tableRelations, content );
        } catch ( XMLParsingException e ) {
            throw new XMLSchemaException( "Error in definition of simple property '" + propertyName + "': "
                                          + e.getMessage() );
        }
        return pt;
    }

    /**
     * Parses the given element as a "deegreewfs:Constant" element.
     *
     * @param element
     *            "deegreewfs:Constant" element
     * @return java representation of element
     * @throws XMLParsingException
     */
    ConstantContent parseConstantContent( Element element )
                            throws XMLParsingException {
        String constant = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );
        return new ConstantContent( constant );
    }

    /**
     * Parses the given "deegreewfs:SpecialContent" element.
     *
     * @param element
     *            "deegreewfs:SpecialContent" element
     * @return java representation of element
     * @throws XMLParsingException
     */
    SpecialContent parseSpecialContent( Element element )
                            throws XMLParsingException {

        SpecialContent content = null;
        String variable = XMLTools.getRequiredNodeAsString( element, "text()", nsContext );

        try {
            content = new SpecialContent( variable );
        } catch ( Exception e ) {
            String msg = Messages.getMessage( "DATASTORE_PARSING_SPECIAL_CONTENT", e.getMessage() );
            throw new XMLParsingException( msg );
        }
        return content;
    }

    /**
     * Parses the given element as a "deegreewfs:SQLFunctionCall" element.
     *
     * @param element
     *            "deegreewfs:SQLFunctionCall" element
     * @param table
     * @return java representation of element
     * @throws XMLParsingException
     */
    SQLFunctionCall parseSQLFunctionCall( Element element, String table )
                            throws XMLParsingException {

        String callString = XMLTools.getRequiredNodeAsString( element, "@call", nsContext );

        String typeName = XMLTools.getRequiredNodeAsString( element, "@type", nsContext );
        int typeCode = -1;
        try {
            typeCode = Types.getTypeCodeForSQLType( typeName );
        } catch ( UnknownTypeException e ) {
            throw new XMLParsingException( "Invalid field type: " + e.getMessage(), e );
        }

        List<Node> nl = XMLTools.getNodes( element, "deegreewfs:FunctionParam", nsContext );
        List<FunctionParam> functionParams = new ArrayList<FunctionParam>();
        Iterator<Node> iter = nl.iterator();
        while ( iter.hasNext() ) {
            Element paramElement = (Element) iter.next();
            functionParams.add( parseFunctionParam( paramElement, table ) );
        }

        // validate variable references
        int maxVar = extractMaxVariableNumber( callString );
        if ( maxVar > functionParams.size() ) {
            String msg = "Error in FunctionCall definition ('" + callString + "') - call string uses variable $"
                         + maxVar + ", but only has " + functionParams.size() + " FunctionParam elements.";
            throw new XMLParsingException( msg );
        }

        // check SRS for all function params (mapped geometry columns)
        int internalSRS = -1;
        for ( FunctionParam param : functionParams ) {
            if ( param instanceof FieldContent ) {
                MappingField mf = ( (FieldContent) param ).getField();
                if ( mf instanceof MappingGeometryField ) {
                    int thisSRS = ( (MappingGeometryField) mf ).getSRS();
                    if ( internalSRS == -1 ) {
                        internalSRS = thisSRS;
                    } else {
                        if ( internalSRS != thisSRS ) {
                            String msg = Messages.getMessage( "DATASTORE_SQL_FUNCTION_CALL_INVALID_SRS", internalSRS,
                                                              thisSRS );
                            throw new XMLParsingException( msg );
                        }
                    }
                }
            }
        }

        // set SRS for all 'SpecialContent' function params
        for ( FunctionParam param : functionParams ) {
            if ( param instanceof SpecialContent ) {
                ( (SpecialContent) param ).setSRS( internalSRS );
            }
        }

        return new SQLFunctionCall( callString, typeCode, functionParams );
    }

    /**
     * Extracts maximum variable numbers ('$i') used in the given call string.
     *
     * TODO: handle leading zeros
     *
     * @param callString
     * @return maximum variable numbers used in the given call string
     */
    private int extractMaxVariableNumber( String callString )
                            throws XMLParsingException {

        int maxVar = 0;

        int foundAt = callString.indexOf( '$' );
        while ( foundAt != -1 ) {
            foundAt++;
            String varNumberString = "";
            while ( foundAt < callString.length() ) {
                char numberChar = callString.charAt( foundAt++ );
                if ( numberChar >= '0' && numberChar <= '9' ) {
                    varNumberString += numberChar;
                } else {
                    break;
                }
            }

            if ( varNumberString.length() == 0 ) {
                String msg = "Error in call attribute ('" + callString
                             + "') of FunctionCall definition - parameters must be specified by "
                             + "the '$' symbol, followed by a positive integer.";
                throw new XMLParsingException( msg );
            }

            try {
                int varNo = Integer.parseInt( varNumberString );
                if ( varNo > maxVar ) {
                    maxVar = varNo;
                }
                if ( varNo == 0 ) {
                    String msg = "Error in call attribute ('" + callString
                                 + "') of FunctionCall definition - $0 is not a valid variable "
                                 + "identifier. Counting of variables starts with 1.";
                    throw new XMLParsingException( msg );
                }
            } catch ( NumberFormatException e ) {
                assert ( false );
            }

            // find next '$' symbol
            foundAt = callString.indexOf( '$', foundAt );
        }
        return maxVar;
    }

    /**
     * Parses the given "deegreewfs:FunctionParam" element.
     * <p>
     * Valid child elements:
     * <ul>
     * <li>deegreewfs:MappingField</li>
     * <li>deegreewfs:ConstantContent</li>
     * <li>deegreewfs:SpecialContent</li>
     * </ul>
     *
     * @param element
     *            "deegreewfs:FunctionParam" element
     * @param table
     * @return java representation of element
     * @throws XMLParsingException
     */
    FunctionParam parseFunctionParam( Element element, String table )
                            throws XMLParsingException {

        FunctionParam param = null;

        Element childElement = XMLTools.getFirstChildElement( element );
        if ( childElement == null || !childElement.getNamespaceURI().equals( CommonNamespaces.DEEGREEWFS.toString() ) ) {
            String msg = Messages.getMessage( "DATASTORE_PARSING_SQL_FUNCTION_CALL" );
            throw new XMLParsingException( msg );
        }

        if ( "MappingField".equals( childElement.getLocalName() ) ) {
            // table relations
            List<Node> relationElements = XMLTools.getNodes( element, "deegreewfs:Relation", nsContext );
            TableRelation[] tablePath = new TableRelation[relationElements.size()];
            String fromTable = table;
            for ( int i = 0; i < tablePath.length; i++ ) {
                tablePath[i] = parseTableRelation( (Element) relationElements.get( i ), fromTable );
                fromTable = tablePath[i].getToTable();
            }
            // TODO do this a better way
            String type = XMLTools.getRequiredNodeAsString( childElement, "@type", nsContext );
            MappingField field;
            if ( "GEOMETRY".equals( type ) ) {
                field = parseGeometryMappingField( childElement, null, fromTable );
            } else {
                field = parseMappingField( childElement, null, fromTable );
            }
            param = new FieldContent( field, tablePath );
        } else if ( "ConstantContent".equals( childElement.getLocalName() ) ) {
            param = parseConstantContent( childElement );
        } else if ( "SpecialContent".equals( childElement.getLocalName() ) ) {
            param = parseSpecialContent( childElement );
        } else {
            String msg = Messages.getMessage( "DATASTORE_PARSING_SQL_FUNCTION_CALL" );
            throw new XMLParsingException( msg );
        }

        return param;
    }

    /**
     * Extracts the mapping information for a geometry property type from the given "xs:annotation" element.
     *
     * @param element
     *            "xs:annotation" element
     * @param propertyName
     *            name of the property (local part is used as default field name if it is not specified explicitly in
     *            the "MappingField" element)
     * @param typeName
     * @param type
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param table
     *            name of the table that is associated with the feature type that this property belongs to
     * @return geometry property type with persistence information
     * @throws XMLSchemaException
     *             if a syntactic or semantic error is found
     * @throws UnknownCRSException
     */
    MappedGeometryPropertyType parseMappedGeometryPropertyType( Element element, QualifiedName propertyName,
                                                                QualifiedName typeName, int type, int minOccurs,
                                                                int maxOccurs, boolean isIdentityPart, String table )
                            throws XMLSchemaException, UnknownCRSException {

        MappedGeometryPropertyType pt = null;
        try {
            Element contentElement = (Element) XMLTools.getRequiredNode( element, "xs:appinfo/deegreewfs:Content",
                                                                         nsContext );
            // sanity check (common error)
            Node typeNode = XMLTools.getNode( contentElement, "@type", nsContext );
            if ( typeNode != null ) {
                throw new XMLParsingException( "Content element must not contain a type attribute." );
            }

            URI srs = XMLTools.getNodeAsURI( element, "deegreewfs:SRS/text()", nsContext, this.defaultSRS );

            Element mfElement = (Element) XMLTools.getRequiredNode( contentElement, "deegreewfs:MappingField",
                                                                    nsContext );
            String defaultField = propertyName.getLocalName();
            MappingGeometryField mappingField = parseGeometryMappingField( mfElement, defaultField, table );
            List<Node> relationElements = XMLTools.getNodes( contentElement, "deegreewfs:Relation", nsContext );
            TableRelation[] tableRelations = new TableRelation[relationElements.size()];
            String fromTable = table;
            for ( int i = 0; i < tableRelations.length; i++ ) {
                tableRelations[i] = parseTableRelation( (Element) relationElements.get( i ), fromTable );
                fromTable = tableRelations[i].getToTable();
            }

            pt = new MappedGeometryPropertyType( propertyName, typeName, type, minOccurs, maxOccurs, isIdentityPart,
                                                 srs, tableRelations, mappingField );
        } catch ( XMLParsingException e ) {
            throw new XMLSchemaException( "Error in definition of geometry property '" + propertyName + "': "
                                          + e.getMessage() );
        }
        return pt;
    }

    /**
     * Extracts the mapping information for a feature property type from the given "xs:annotation" element.
     *
     * @param element
     *            "xs:annotation" element
     * @param propertyName
     *            name of the property (local part is used as default field name if it is not specified explicitly in
     *            the "MappingField" element)
     * @param minOccurs
     * @param maxOccurs
     * @param isIdentityPart
     * @param table
     *            name of the table that is associated with the feature type that this property belongs to
     * @param isReferenceType
     *            true, if this property is of type "gml:ReferenceType", false otherwise
     * @return feature property type with persistence information
     * @throws XMLSchemaException
     *             if a syntactic or semantic error is found
     */
    MappedFeaturePropertyType parseMappedFeaturePropertyType( Element element, QualifiedName propertyName,
                                                              int minOccurs, int maxOccurs, boolean isIdentityPart,
                                                              String table, boolean isReferenceType )
                            throws XMLSchemaException {

        MappedFeaturePropertyType pt = null;
        try {
            Element contentElement = (Element) XMLTools.getRequiredNode( element, "xs:appinfo/deegreewfs:Content",
                                                                         nsContext );

            // sanity check (common error)
            Node mfNode = XMLTools.getNode( element, "deegreewfs:MappingField", nsContext );
            if ( mfNode != null ) {
                throw new XMLParsingException( "Content element must not contain a MappingField element." );
            }

            QualifiedName containedFT = parseQualifiedName( XMLTools.getRequiredNode( contentElement, "@type",
                                                                                      nsContext ) );
            MappedFeatureTypeReference containedFTRef = new MappedFeatureTypeReference( containedFT );

            List<Node> relationElements = XMLTools.getNodes( contentElement, "deegreewfs:Relation", nsContext );
            TableRelation[] tableRelations = new TableRelation[relationElements.size()];
            String fromTable = table;
            for ( int i = 0; i < tableRelations.length; i++ ) {
                tableRelations[i] = parseTableRelation( (Element) relationElements.get( i ), fromTable );
                fromTable = tableRelations[i].getToTable();
            }

            boolean allowExternalLinks = getNodeAsBoolean( contentElement, "@allowExternalLinks", nsContext, false );

            pt = new MappedFeaturePropertyType( propertyName, Types.FEATURE_PROPERTY_NAME, Types.FEATURE, minOccurs,
                                                maxOccurs, isIdentityPart, tableRelations, containedFTRef,
                                                isReferenceType, allowExternalLinks );
        } catch ( XMLParsingException e ) {
            throw new XMLSchemaException( "Error in definition of feature property '" + propertyName + "': "
                                          + e.getMessage() );
        }
        return pt;
    }

    /**
     * Parses the given 'MappingField' element.
     *
     * @param element
     *            'MappingField' element
     * @param defaultField
     *            if null, the element must have a 'field' attribute, otherwise the given value is used, if the element
     *            misses a 'field' attribute
     * @param defaultTable
     *            if null, the element must have a 'table' attribute, otherwise the 'table' attribute must be left out
     *            or match the given value
     * @return class representation of 'MappingField' element
     * @throws XMLParsingException
     *             if a syntactic or semantic error is found
     */
    private MappingField parseMappingField( Element element, String defaultField, String defaultTable )
                            throws XMLParsingException {

        MappingField mappingField = null;

        String field = null;
        if ( defaultField == null ) {
            field = XMLTools.getRequiredNodeAsString( element, "@field", nsContext );
        } else {
            field = XMLTools.getNodeAsString( element, "@field", nsContext, defaultField );
        }

        String typeName = XMLTools.getRequiredNodeAsString( element, "@type", nsContext );
        int typeCode = -1;
        try {
            typeCode = Types.getTypeCodeForSQLType( typeName );
        } catch ( UnknownTypeException e ) {
            throw new XMLParsingException( "Invalid field type: " + e.getMessage(), e );
        }

        String table = null;
        if ( defaultTable == null ) {
            // if table is unspecified, this is resolved later (in MappedGMLSchema)
            // TODO clean this up
            table = XMLTools.getNodeAsString( element, "@table", nsContext, null );
        } else {
            table = XMLTools.getNodeAsString( element, "@table", nsContext, defaultTable );
            if ( !table.equals( defaultTable ) ) {
                throw new XMLParsingException( "Specified 'table' attribute ('" + table
                                               + "') in 'MappingField' element is inconsistent; leave out or use '"
                                               + defaultTable + "' instead." );
            }
        }

        boolean auto = XMLTools.getNodeAsBoolean( element, "@auto", nsContext, false );
        mappingField = new MappingField( table, field, typeCode, auto );

        return mappingField;
    }

    /**
     * Parses the given 'MappingField' element.
     *
     * @param element
     *            'MappingField' element
     * @param defaultField
     *            if null, the element must have a 'field' attribute, otherwise the given value is used, if the element
     *            misses a 'field' attribute
     * @param defaultTable
     *            if null, the element must have a 'table' attribute, otherwise the 'table' attribute must be left out
     *            or match the given value
     * @return class representation of 'MappingField' element
     * @throws XMLParsingException
     *             if a syntactic or semantic error is found
     */
    private MappingGeometryField parseGeometryMappingField( Element element, String defaultField, String defaultTable )
                            throws XMLParsingException {

        MappingGeometryField mappingField = null;

        String field = null;
        if ( defaultField == null ) {
            field = XMLTools.getRequiredNodeAsString( element, "@field", nsContext );
        } else {
            field = XMLTools.getNodeAsString( element, "@field", nsContext, defaultField );
        }

        String typeName = XMLTools.getRequiredNodeAsString( element, "@type", nsContext );
        int typeCode = Types.OTHER;
        if ( !( "GEOMETRY".equals( typeName ) ) ) {
            try {
                typeCode = Types.getTypeCodeForSQLType( typeName );
            } catch ( UnknownTypeException e ) {
                throw new XMLParsingException( "Invalid field type: " + e.getMessage(), e );
            }
        }

        String table = null;
        if ( defaultTable == null ) {
            // if table is unspecified, this is resolved later (in MappedGMLSchema)
            // TODO clean this up
            table = XMLTools.getNodeAsString( element, "@table", nsContext, null );
        } else {
            table = XMLTools.getNodeAsString( element, "@table", nsContext, defaultTable );
            if ( !table.equals( defaultTable ) ) {
                String msg = "Specified 'table' attribute ('" + table
                             + "') in 'MappingField' element is inconsistent; leave out or use '" + defaultTable
                             + "' instead.";
                throw new XMLParsingException( msg );
            }
        }

        int internalSrs = XMLTools.getNodeAsInt( element, "@srs", nsContext, -1 );
        mappingField = new MappingGeometryField( table, field, typeCode, internalSrs );
        return mappingField;
    }

    /**
     * Parses the given 'gmlId' element.
     *
     * @param element
     *            'gmlId' element
     * @param table
     *            the associated table of the FeatureType
     * @return class representation of 'gmlId' element
     * @throws XMLParsingException
     *             if a syntactic or semantic error is found
     */
    private MappedGMLId parseGMLIdDefinition( Element element, String table )
                            throws XMLParsingException {
        String prefix = XMLTools.getNodeAsString( element, "@prefix", nsContext, "" );
        String separator = XMLTools.getNodeAsString( element, "@separator", nsContext, "" );

        List<Node> mappingFieldElementList = XMLTools.getRequiredNodes( element, "deegreewfs:MappingField", nsContext );
        MappingField[] mappingFields = new MappingField[mappingFieldElementList.size()];
        for ( int i = 0; i < mappingFields.length; i++ ) {
            Element mappingFieldElement = (Element) mappingFieldElementList.get( i );
            mappingFields[i] = parseMappingField( mappingFieldElement,
                                                  XMLTools.getRequiredNodeAsString( mappingFieldElement, "@field",
                                                                                    nsContext ), table );
        }

        IDPART_INFO idpart_info = IDPART_INFO.noIDInfo;
        String identityPart = XMLTools.getNodeAsString( element, "deegreewfs:IdentityPart/text()", nsContext, null );
        if ( identityPart != null ) {
            if ( "false".equals( identityPart ) ) {
                idpart_info = IDPART_INFO.notIDPart;
            } else {
                idpart_info = IDPART_INFO.isIDPart;
            }
        }

        IdGenerator idGenerator = null;
        Element idGeneratorElement = (Element) XMLTools.getNode( element, "deegreewfs:IdGenerator", nsContext );
        if ( idGeneratorElement != null ) {
            idGenerator = parseGMLIdGenerator( idGeneratorElement );
        } else {
            idGenerator = IdGenerator.getInstance( IdGenerator.TYPE_UUID, new Properties() );
        }
        return new MappedGMLId( prefix, separator, mappingFields, idGenerator, idpart_info );
    }

    /**
     * Parses the given 'IdGenerator' element.
     *
     * @param element
     *            'IdGenerator' element
     * @return object representation of 'IdGenerator' element
     * @throws XMLParsingException
     *             if a syntactic or semantic error is found
     */
    private IdGenerator parseGMLIdGenerator( Element element )
                            throws XMLParsingException {
        String type = XMLTools.getRequiredNodeAsString( element, "@type", nsContext );
        Properties params = new Properties();
        List<Node> paramElementList = XMLTools.getNodes( element, "deegreewfs:param", nsContext );
        Iterator<Node> iter = paramElementList.iterator();
        while ( iter.hasNext() ) {
            Element paramElement = (Element) iter.next();
            String name = XMLTools.getRequiredNodeAsString( paramElement, "@name", nsContext );
            String value = XMLTools.getRequiredNodeAsString( paramElement, "text()", nsContext );
            params.setProperty( name, value );
        }
        IdGenerator idGenerator = IdGenerator.getInstance( type, params );
        return idGenerator;
    }

    private TableRelation parseTableRelation( Element element, String fromTable )
                            throws XMLParsingException {
        List<Node> fromMappingElements = XMLTools.getRequiredNodes( element, "deegreewfs:From/deegreewfs:MappingField",
                                                                    nsContext );
        List<Node> toMappingElements = XMLTools.getRequiredNodes( element, "deegreewfs:To/deegreewfs:MappingField",
                                                                  nsContext );
        if ( fromMappingElements.size() != toMappingElements.size() ) {
            throw new XMLParsingException( "Error in 'Relation' element: number of 'MappingField' elements "
                                           + "below 'From' and 'To' elements do not match." );
        }
        FK_INFO fkInfo = FK_INFO.noFKInfo;
        boolean fromIsFK = XMLTools.getNodeAsBoolean( element, "deegreewfs:From/@fk", nsContext, false );
        boolean toIsFK = XMLTools.getNodeAsBoolean( element, "deegreewfs:To/@fk", nsContext, false );
        if ( fromIsFK && toIsFK ) {
            throw new XMLParsingException( "Error in 'Relation' element: either 'To' or 'From' can "
                                           + "have a 'fk' attribute with value 'true', but not both." );
        }
        if ( fromIsFK ) {
            fkInfo = FK_INFO.fkIsFromField;
        }
        if ( toIsFK ) {
            fkInfo = FK_INFO.fkIsToField;
        }
        MappingField[] fromMappingFields = new MappingField[fromMappingElements.size()];
        MappingField[] toMappingFields = new MappingField[fromMappingFields.length];
        for ( int i = 0; i < fromMappingFields.length; i++ ) {
            fromMappingFields[i] = parseMappingField( (Element) fromMappingElements.get( i ), null, fromTable );
            toMappingFields[i] = parseMappingField( (Element) toMappingElements.get( i ), null, null );
        }

        // parse id generator
        // TODO sanity checks
        IdGenerator idGenerator = null;
        if ( fromIsFK ) {
            Element idGeneratorElement = (Element) XMLTools.getNode( element, "deegreewfs:To/deegreewfs:IdGenerator",
                                                                     nsContext );
            if ( idGeneratorElement != null ) {
                idGenerator = parseGMLIdGenerator( idGeneratorElement );
            } else {
                idGenerator = IdGenerator.getInstance( IdGenerator.TYPE_UUID, new Properties() );
            }
        } else {
            Element idGeneratorElement = (Element) XMLTools.getNode( element, "deegreewfs:From/deegreewfs:IdGenerator",
                                                                     nsContext );
            if ( idGeneratorElement != null ) {
                idGenerator = parseGMLIdGenerator( idGeneratorElement );
            } else {
                idGenerator = IdGenerator.getInstance( IdGenerator.TYPE_UUID, new Properties() );
            }
        }

        return new TableRelation( fromMappingFields, toMappingFields, fkInfo, idGenerator );
    }

    /**
     * Returns the value of the "deegreewfs:visible" element.
     *
     * @param annotationElement
     * @return -1 if it is not present, 0 if it is "false", 1 if it is "true"
     * @throws XMLParsingException
     */
    public int parseVisible( Element annotationElement )
                            throws XMLParsingException {
        int visibleCode = -1;
        String visible = XMLTools.getNodeAsString( annotationElement, "xs:appinfo/deegreewfs:visible/text()",
                                                   nsContext, null );
        if ( visible != null ) {
            if ( "false".equals( visible ) ) {
                visibleCode = 0;
            } else {
                visibleCode = 1;
            }
        }
        return visibleCode;
    }

    /**
     * Parses the 'updatable' status of the given feature type annotation element.
     *
     * @param annotationElement
     * @return true, if update transactions may be performed on the feature type, false otherwise
     * @throws XMLParsingException
     */
    public boolean parseIsUpdatable( Element annotationElement )
                            throws XMLParsingException {
        return XMLTools.getNodeAsBoolean( annotationElement, "xs:appinfo/deegreewfs:transaction/@update", nsContext,
                                          false );
    }

    /**
     * Parses the 'deletable' status of the given feature type annotation element.
     *
     * @param annotationElement
     * @return true, if delete transactions may be performed on the feature type, false otherwise
     * @throws XMLParsingException
     */
    public boolean parseIsDeletable( Element annotationElement )
                            throws XMLParsingException {
        return XMLTools.getNodeAsBoolean( annotationElement, "xs:appinfo/deegreewfs:transaction/@delete", nsContext,
                                          false );
    }

    /**
     * Parses the 'insertable' status of the given feature type annotation element.
     *
     * @param annotationElement
     * @return true, if insert transactions may be performed on the feature type, false otherwise
     * @throws XMLParsingException
     */
    public boolean parseIsInsertable( Element annotationElement )
                            throws XMLParsingException {
        return XMLTools.getNodeAsBoolean( annotationElement, "xs:appinfo/deegreewfs:transaction/@insert", nsContext,
                                          false );
    }

    /**
     * Parses the 'isPseudoFeatureType' element of the given feature type annotation element.
     *
     * @param annotationElement
     * @return true, if feature type is a pseudo feature type, false otherwise
     * @throws XMLParsingException
     */
    public boolean parseIsPseudoFeatureType( Element annotationElement )
                            throws XMLParsingException {
        return XMLTools.getNodeAsBoolean( annotationElement, "xs:appinfo/deegreewfs:isPseudoFeatureType/text()",
                                          nsContext, false );
    }

    /**
     * Returns the value of the "deegreewfs:IdentityPart" element.
     *
     * @param annotationElement
     * @return -1 if it is not present, 0 if it is "false", 1 if it is "true"
     * @throws XMLParsingException
     */
    public int parseIdentityPart( Element annotationElement )
                            throws XMLParsingException {
        int identityCode = -1;
        String identityPart = XMLTools.getNodeAsString( annotationElement, "xs:appinfo/deegreewfs:IdentityPart/text()",
                                                        nsContext, null );
        if ( identityPart != null ) {
            if ( "false".equals( identityPart ) ) {
                identityCode = 0;
            } else {
                identityCode = 1;
            }
        }
        return identityCode;
    }

    /**
     * Returns the value of the "deegreewfs:DefaultSRS" element.
     *
     * @param annotationElement
     * @param defaultValue
     * @return default SRS of the feature type
     * @throws XMLParsingException
     */
    public URI parseDefaultSRS( Element annotationElement, URI defaultValue )
                            throws XMLParsingException {
        URI defaultSRS = XMLTools.getNodeAsURI( annotationElement, "xs:appinfo/deegreewfs:DefaultSRS/text()",
                                                nsContext, defaultValue );
        return defaultSRS;
    }

    /**
     * Returns the values of the "deegreewfs:OtherSRS" elements.
     *
     * @param annotationElement
     * @return alternative SRS that may be used to query the feature type
     * @throws XMLParsingException
     */
    public URI[] parseOtherSRS( Element annotationElement )
                            throws XMLParsingException {
        URI[] otherSRS = XMLTools.getNodesAsURIs( annotationElement, "xs:appinfo/deegreewfs:OtherSRS/text()", nsContext );
        return otherSRS;
    }

    /**
     * @param annotationElement
     * @param table
     * @param schema
     * @return a list of default GML properties to include. The qualified name specified (if not null), after which
     *         property this one should be included.
     * @throws XMLParsingException
     */
    public LinkedList<Pair<PropertyType, QualifiedName>> parseGMLDefaultProps( Element annotationElement, String table,
                                                                               MappedGMLSchema schema )
                            throws XMLParsingException {
        Element appinfo = getRequiredElement( annotationElement, XS_PREFIX + ":appinfo", nsContext );

        LinkedList<Pair<PropertyType, QualifiedName>> list = new LinkedList<Pair<PropertyType, QualifiedName>>();

        if ( appinfo == null ) {
            return list;
        }

        for ( Element e : getElements( appinfo, DEEGREEWFS_PREFIX + ":StandardGMLProps/" + XS_PREFIX + ":element",
                                       nsContext ) ) {
            try {
                QualifiedName realName = getRequiredNodeAsQualifiedName( e, "@name", nsContext );
                Pair<PropertyType, QualifiedName> p = new Pair<PropertyType, QualifiedName>();
                p.first = schema.buildPropertyTypeRealName( parseElementDeclaration( e ), table, realName );
                p.second = getNodeAsQualifiedName( e, "@insertAfter", nsContext, null );
                list.add( p );
            } catch ( UnknownCRSException e1 ) {
                throw new XMLParsingException( "Wrapped exception", e1 );
            }
        }

        return list;
    }
}
