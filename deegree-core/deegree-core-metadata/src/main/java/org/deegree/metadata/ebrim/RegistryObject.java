//$HeadURL: svn+ssh://lbuesching@svn.wald.intevation.de/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2010 by:
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
package org.deegree.metadata.ebrim;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.commons.xml.stax.StAXParsingHelper;
import org.deegree.filter.Filter;
import org.deegree.filter.expression.PropertyName;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.gml.GMLInputFactory;
import org.deegree.gml.GMLStreamReader;
import org.deegree.gml.GMLVersion;
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.filter.XPathElementFilter;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class RegistryObject implements MetadataRecord {

    private static final Logger LOG = getLogger( RegistryObject.class );

    protected static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

    public static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

    private static List<XPath> summaryFilterElementsXPath = new ArrayList<XPath>();

    private static List<XPath> briefFilterElementsXPath = new ArrayList<XPath>();

    protected XMLAdapter adapter;

    static {
        ns.addNamespace( "rim", RIM_NS );
        ns.addNamespace( "wrs", "http://www.opengis.net/cat/wrs/1.0" );

        // briefFilterElementsXPath.add( new XPath( "./@id", ns ) );
        // briefFilterElementsXPath.add( new XPath( "./@lid", ns ) );
        // briefFilterElementsXPath.add( new XPath( "./@objectType", ns ) );
        // briefFilterElementsXPath.add( new XPath( "./@status", ns ) );
        briefFilterElementsXPath.add( new XPath( "./rim:VersionInfo", ns ) );

        // summaryFilterElementsXPath.add( new XPath( "./@id", ns ) );
        // summaryFilterElementsXPath.add( new XPath( "./@lid", ns ) );
        // summaryFilterElementsXPath.add( new XPath( "./@objectType", ns ) );
        // summaryFilterElementsXPath.add( new XPath( "./@status", ns ) );
        summaryFilterElementsXPath.add( new XPath( "./rim:VersionInfo", ns ) );
        summaryFilterElementsXPath.add( new XPath( "./rim:Slot", ns ) );
        // TODO: As specified by the value of the the Accept-Language request header field (if present).
        summaryFilterElementsXPath.add( new XPath( "./rim:Name", ns ) );
        summaryFilterElementsXPath.add( new XPath( "./rim:Description", ns ) );
    }

    public RegistryObject( OMElement record ) {
        this.adapter = new XMLAdapter( record );
    }

    public RegistryObject( XMLStreamReader xmlStream ) {
        this.adapter = new XMLAdapter( xmlStream );
    }

    @Override
    public QName getName() {
        return null;
    }

    @Override
    public String getIdentifier() {
        return getId();
    }

    @Override
    public String[] getTitle() {
        return new String[] { getROName() };
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getRelation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date getModified() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getAbstract() {
        return new String[] { getDesc() };
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getSubject() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getSource() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getRights() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCreator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getPublisher() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getContributor() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getLanguage() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Envelope[] getBoundingBox() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public OMElement getAsOMElement() {
        return adapter.getRootElement();
    }

    @Override
    public DCRecord toDublinCore() {
        return new DCRecord( this );
    }

    @Override
    public boolean eval( Filter filter ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {
        XMLStreamReader xmlStream = adapter.getRootElement().getXMLStreamReader();
        switch ( returnType ) {
        case brief:
            StAXParsingHelper.skipStartDocument( xmlStream );
            OMElement briefFilter = new XPathElementFilter( adapter.getRootElement(), briefFilterElementsXPath );
            briefFilter.detach();
            generateOutput( writer, briefFilter.getXMLStreamReader() );
            break;
        case full:
            StAXParsingHelper.skipStartDocument( xmlStream );
            XMLAdapter.writeElement( writer, xmlStream );
            break;
        case summary:
        default:
            StAXParsingHelper.skipStartDocument( xmlStream );
            OMElement sumFilter = new XPathElementFilter( adapter.getRootElement(), summaryFilterElementsXPath );
            sumFilter.detach();
            generateOutput( writer, sumFilter.getXMLStreamReader() );
            break;
        }

    }

    private void generateOutput( XMLStreamWriter writer, XMLStreamReader filter )
                            throws XMLStreamException {
        while ( filter.hasNext() ) {
            if ( filter.getEventType() == XMLStreamConstants.START_ELEMENT ) {
                XMLAdapter.writeElement( writer, filter );
            } else {
                filter.next();
            }
        }
        filter.close();
    }

    @Override
    public void serialize( XMLStreamWriter writer, String[] elementNames )
                            throws XMLStreamException {
        // TODO Auto-generated method stub

    }

    @Override
    public void update( PropertyName propName, String replaceValue ) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update( PropertyName propName, OMElement replaceValue ) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void removeNode( PropertyName propName ) {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the id
     */
    public String getId() {
        return adapter.getRequiredNodeAsString( adapter.getRootElement(), new XPath( "./@id", ns ) );
    }

    /**
     * @return the name
     */
    public String getROName() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./rim:Name/rim:LocalizedString/@value",
                                                                             ns ), null );
    }

    /**
     * @return the desc
     */
    public String getDesc() {
        return adapter.getNodeAsString( adapter.getRootElement(),
                                        new XPath( "./rim:Description/rim:LocalizedString/@value", ns ), null );
    }

    /**
     * @return the extId
     */
    public String getExtId() {
        return adapter.getNodeAsString( adapter.getRootElement(),
                                        new XPath( "./rim:ExternalIdentifier/rim:Name/rim:LocalizedString/@value", ns ),
                                        null );
    }

    /**
     * @return the home
     */
    public String getHome() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./@home", ns ), null );
    }

    /**
     * @return the lid
     */
    public String getLid() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./@lid", ns ), null );
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./@status", ns ), null );
    }

    /**
     * @return the versionInfo
     */
    public String getVersionInfo() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./rim:versionInfo/@versionName", ns ),
                                        null );
    }

    /**
     * @return the objectType
     */
    public String getObjectType() {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./@objectType", ns ), null );
    }

    public Geometry getGeometrySlotValue( String slotName ) {
        OMElement geomElem = adapter.getElement( adapter.getRootElement(),
                                                 new XPath( "./rim:Slot[@name='" + slotName
                                                            + "']/wrs:ValueList/wrs:AnyValue[1]/*", ns ) );
        if ( geomElem != null ) {
            try {
                GMLStreamReader gmlReader = GMLInputFactory.createGMLStreamReader( GMLVersion.GML_31,
                                                                                   geomElem.getXMLStreamReader() );
                return gmlReader.readGeometry();
            } catch ( Exception e ) {
                String msg = "Could not parse geometry " + geomElem;
                LOG.debug( msg, e );
                e.printStackTrace();
                throw new IllegalArgumentException( msg );
            }
        }
        return null;
    }

    public String[] getSlotValueList( String slotName ) {
        return adapter.getNodesAsStrings( adapter.getRootElement(), new XPath( "./rim:Slot[@name='" + slotName
                                                                               + "']/rim:ValueList/rim:Value", ns ) );
    }

    public String getSlotValue( String slotName ) {
        return adapter.getNodeAsString( adapter.getRootElement(), new XPath( "./rim:Slot[@name='" + slotName
                                                                             + "']/rim:ValueList/rim:Value[1]", ns ),
                                        null );
    }

    public String[] getSlotNames() {
        return adapter.getNodesAsStrings( adapter.getRootElement(), new XPath( "./rim:Slot/@name", ns ) );
    }

    /**
     * @return the encapsulated XML
     */
    public OMElement getElement() {
        return adapter.getRootElement();
    }
}
