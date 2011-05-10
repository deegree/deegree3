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
import org.deegree.metadata.DCRecord;
import org.deegree.metadata.MetadataRecord;
import org.deegree.metadata.ebrim.model.RegistryObject;
import org.deegree.metadata.filter.XPathElementFilter;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class RegistryObjectRecord implements MetadataRecord {

    protected static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

    public static final String RIM_NS = "urn:oasis:names:tc:ebxml-regrep:xsd:rim:3.0";

    private static List<XPath> summaryFilterElementsXPath = new ArrayList<XPath>();

    private static List<XPath> briefFilterElementsXPath = new ArrayList<XPath>();

    protected XMLAdapter adapter;

    // private OMElement record;

    private RegistryObject object;

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

    public RegistryObjectRecord( OMElement record ) {
        this.adapter = new XMLAdapter( record );
    }

    public RegistryObjectRecord( XMLStreamReader xmlStream ) {
        this.adapter = new XMLAdapter( xmlStream );
    }

    /**
     * @return the parsed EbrimEORecord
     */
    public RegistryObject getParsedRecord() {
        if ( object == null ) {
            String id = parseId( adapter.getRootElement() );
            String home = parseHome( adapter.getRootElement() );
            String lid = parseLid( adapter.getRootElement() );
            String objectType = parseObjectType( adapter.getRootElement() );
            String status = parseStatus( adapter.getRootElement() );

            String extId = parseExtId( adapter.getRootElement() );
            String name = parseName( adapter.getRootElement() );
            String desc = parseDesc( adapter.getRootElement() );
            String versionInfo = parseVersionInfo( adapter.getRootElement() );
            this.object = new RegistryObject( id, home, lid, status, name, desc, versionInfo, extId, objectType,
                                              adapter.getRootElement() );
        }
        return object;
    }

    @Override
    public QName getName() {
        String name = getParsedRecord().getName();
        if ( name != null ) {
            return new QName( name );
        }
        return null;
    }

    @Override
    public String getIdentifier() {
        return getParsedRecord().getId();
    }

    @Override
    public String[] getTitle() {
        return new String[] { getParsedRecord().getName() };
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
        return new String[] { getParsedRecord().getDesc() };
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

    private String parseId( OMElement root ) {
        return adapter.getRequiredNodeAsString( root, new XPath( "./@id", ns ) );
    }

    private String parseLid( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./@lid", ns ), null );
    }

    private String parseHome( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./@home", ns ), null );
    }

    private String parseStatus( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./@status", ns ), null );
    }

    private String parseVersionInfo( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./rim:versionInfo/@versionName", ns ), null );
    }

    private String parseName( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./rim:Name/rim:LocalizedString/@value", ns ), null );
    }

    private String parseExtId( OMElement root ) {
        return adapter.getNodeAsString( root,
                                        new XPath( "./rim:ExternalIdentifier/rim:Name/rim:LocalizedString/@value", ns ),
                                        null );
    }

    private String parseDesc( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./rim:Description/rim:LocalizedString/@value", ns ), null );
    }

    private String parseObjectType( OMElement root ) {
        return adapter.getNodeAsString( root, new XPath( "./@objectType", ns ), null );
    }

}
