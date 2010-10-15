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
package org.deegree.metadata;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DCT_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_PREFIX;
import static org.deegree.protocol.ows.OWSCommonXMLAdapter.OWS_NS;
import static org.deegree.protocol.ows.OWSCommonXMLAdapter.OWS_PREFIX;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import jj2000.j2k.NotImplementedError;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.filter.Filter;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * DublinCore {@link MetadataRecord}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class DCRecord implements MetadataRecord {

    private final QName root;

    private final String[] titles;

    private final String[] identifier;

    private final String[] _abstract;

    private final Envelope[] boundingBox;

    private final String[] formats;

    private final Date[] modified;

    private final String[] relations;

    private final String[] subject;

    private final String type;

    private final String[] rights;

    private final String creator;

    private final String contributor;

    private final String publisher;

    private final String language;

    private final String source;

    // uri, localname, prefix
    private static final QName ows = new QName( OWS_NS, "", OWS_PREFIX );

    private static final QName csw = new QName( CSW_202_NS, "", CSW_PREFIX );

    private static final QName dc = new QName( DC_NS, "", DC_PREFIX );

    private static final QName dct = new QName( DCT_NS, "", DCT_PREFIX );

    public DCRecord( MetadataRecord record ) {
        this.titles = record.getTitle();
        this.identifier = record.getIdentifier();
        this._abstract = record.getAbstract();
        this.boundingBox = record.getBoundingBox();
        this.formats = record.getFormat();
        this.modified = record.getModified();
        this.relations = record.getRelation();
        this.subject = record.getSubject();
        this.type = record.getType();
        this.rights = record.getRights();
        this.creator = record.getCreator();
        this.contributor = record.getContributor();
        this.publisher = record.getPublisher();
        this.language = record.getLanguage();
        this.source = record.getSource();
        this.root = record.getName();

    }

    @Override
    public QName getName() {
        return root;
    }

    @Override
    public boolean eval( Filter filter ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] getAbstract() {

        return _abstract;
    }

    @Override
    public Envelope[] getBoundingBox() {

        return boundingBox;
    }

    @Override
    public String[] getFormat() {

        return formats;
    }

    @Override
    public String[] getIdentifier() {

        return identifier;
    }

    @Override
    public Date[] getModified() {

        return modified;
    }

    @Override
    public String[] getRelation() {
        return relations;
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getSubject() {

        return subject;
    }

    @Override
    public String[] getTitle() {

        return titles;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {

        switch ( returnType ) {
        case brief:
            toDCBrief( writer );
            break;
        case summary:
            toDCSummary( writer );
            break;
        case full:
            toDCFull( writer );
            break;
        default:
            toDCBrief( writer );
            break;
        }

    }

    @Override
    public void serialize( XMLStreamWriter writer, String[] elementNames )
                            throws XMLStreamException {

        throw new NotImplementedError(
                                       "The serialize(writer, elementNames) method for Dublin Core is not implemented yet. " );

    }

    private void toDCFull( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "Record", csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeSummaryElements( writer );
        writeFullElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeFullElements( XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( getCreator() != null ) {
            writer.writeStartElement( dc.getPrefix(), "creator", dc.getNamespaceURI() );
            writer.writeCharacters( getCreator() );
            writer.writeEndElement();
        }
        if ( getPublisher() != null ) {
            writer.writeStartElement( dc.getPrefix(), "publisher", dc.getNamespaceURI() );
            writer.writeCharacters( getPublisher() );
            writer.writeEndElement();
        }
        if ( getContributor() != null ) {
            writer.writeStartElement( dc.getPrefix(), "contributor", dc.getNamespaceURI() );
            writer.writeCharacters( getContributor() );
            writer.writeEndElement();
        }
        if ( getSource() != null ) {
            writer.writeStartElement( dc.getPrefix(), "source", dc.getNamespaceURI() );
            writer.writeCharacters( getSource() );
            writer.writeEndElement();
        }
        if ( getLanguage() != null ) {
            writer.writeStartElement( dc.getPrefix(), "language", dc.getNamespaceURI() );
            writer.writeCharacters( getLanguage() );
            writer.writeEndElement();
        }

        for ( String r : getRights() ) {
            writer.writeStartElement( dc.getPrefix(), "rights", dc.getNamespaceURI() );
            writer.writeCharacters( r );
            writer.writeEndElement();
        }

    }

    private void toDCSummary( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "SummaryRecord", csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeSummaryElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeSummaryElements( XMLStreamWriter writer )
                            throws XMLStreamException {

        for ( String s : getSubject() ) {
            writer.writeStartElement( dc.getPrefix(), "subject", dc.getNamespaceURI() );
            writer.writeCharacters( s );
            writer.writeEndElement();
        }
        for ( String f : getFormat() ) {
            writer.writeStartElement( dc.getPrefix(), "format", dc.getNamespaceURI() );
            writer.writeCharacters( f );
            writer.writeEndElement();
        }
        for ( String r : getRelation() ) {
            writer.writeStartElement( dc.getPrefix(), "relation", dc.getNamespaceURI() );
            writer.writeCharacters( r );
            writer.writeEndElement();
        }
        for ( Date d : getModified() ) {
            writer.writeStartElement( dct.getPrefix(), "modified", dct.getNamespaceURI() );
            writer.writeCharacters( d.getDate().toString() );
            writer.writeEndElement();
        }
        for ( String a : getAbstract() ) {
            writer.writeStartElement( dct.getPrefix(), "abstract", dct.getNamespaceURI() );
            writer.writeCharacters( a );
            writer.writeEndElement();
        }

    }

    private void toDCBrief( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "BriefRecord", csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeBriefElements( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( String i : getIdentifier() ) {
            writer.writeStartElement( dc.getPrefix(), "identifier", dc.getNamespaceURI() );
            writer.writeCharacters( i );
            writer.writeEndElement();
        }
        for ( String t : getTitle() ) {
            writer.writeStartElement( dc.getPrefix(), "title", dc.getNamespaceURI() );
            writer.writeCharacters( t );
            writer.writeEndElement();
        }
        if ( getType() != null ) {
            writer.writeStartElement( dc.getPrefix(), "type", dc.getNamespaceURI() );
            writer.writeCharacters( getType() );
            writer.writeEndElement();
        }
    }

    private void writeBoundingBox( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( Envelope b : getBoundingBox() ) {
            writer.writeStartElement( ows.getPrefix(), "BoundingBox", ows.getNamespaceURI() );
            writer.writeStartElement( ows.getPrefix(), "LowerCorner", ows.getNamespaceURI() );
            writer.writeCharacters( b.getMin().get0() + " " + b.getMin().get1() );
            writer.writeEndElement();
            writer.writeStartElement( ows.getPrefix(), "UpperCorner", ows.getNamespaceURI() );
            writer.writeCharacters( b.getMax().get0() + " " + b.getMax().get1() );
            writer.writeEndElement();
            writer.writeEndElement();
        }
    }

    @Override
    public DCRecord toDublinCore() {

        return this;
    }

    @Override
    public String getContributor() {

        return contributor;
    }

    @Override
    public String getLanguage() {

        return language;
    }

    @Override
    public String getPublisher() {

        return publisher;
    }

    @Override
    public String[] getRights() {

        return rights;
    }

    @Override
    public String getSource() {

        return source;
    }

    @Override
    public String getCreator() {

        return creator;
    }

}