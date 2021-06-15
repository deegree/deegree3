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

import static org.deegree.commons.xml.CommonNamespaces.OWS_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DCT_NS;
import static org.deegree.protocol.csw.CSWConstants.DCT_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_PREFIX;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.datetime.Date;
import org.deegree.filter.Filter;
import org.deegree.filter.FilterEvaluationException;
import org.deegree.filter.expression.ValueReference;
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

    public static String DC_RECORD_NS = "http://www.opengis.net/cat/csw/2.0.2";

    public static final String SCHEMA_URL = "http://schemas.opengis.net/csw/2.0.2/record.xsd";

    private final QName root;

    private final String[] titles;

    private final String identifier;

    private final String[] _abstract;

    private final Envelope[] boundingBox;

    private final String[] formats;

    private final Date modified;

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
    private static final QName ows = new QName( OWS_NS, "", "ows" );

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
    public boolean eval( Filter filter )
                            throws FilterEvaluationException {
        throw new UnsupportedOperationException( "In-memory filter evaluation not implemented yet." );
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
    public String getIdentifier() {

        return identifier;
    }

    @Override
    public Date getModified() {

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
        throw new UnsupportedOperationException(
                                       "The serialize(writer, elementNames) method for Dublin Core is not implemented yet. " );

    }

    private void toDCFull( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "Record", csw.getNamespaceURI() );
        writer.writeNamespace( csw.getPrefix(), csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeSummaryElements( writer );
        writeFullElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeFullElements( XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( getCreator() != null ) {
            write( writer, "creator", getCreator(), dc );
        }
        if ( getPublisher() != null ) {
            write( writer, "publisher", getPublisher(), dc );
        }
        if ( getContributor() != null ) {
            write( writer, "contributor", getContributor(), dc );
        }
        if ( getSource() != null ) {
            write( writer, "source", getSource(), dc );
        }
        if ( getLanguage() != null ) {
            write( writer, "language", getLanguage(), dc );
        }
        for ( String r : getRights() ) {
            write( writer, "rights", r, dc );
        }
    }

    private void toDCSummary( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "SummaryRecord", csw.getNamespaceURI() );
        writer.writeNamespace( csw.getPrefix(), csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeSummaryElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeSummaryElements( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( String s : getSubject() ) {
            write( writer, "subject", s, dc );
        }
        for ( String f : getFormat() ) {
            write( writer, "format", f, dc );
        }
        for ( String r : getRelation() ) {
            write( writer, "relation", r, dc );
        }
        if ( getModified() != null ) {
            write( writer, "modified", getModified().getCalendar().getTime().toString(), dct );
        }
        for ( String a : getAbstract() ) {
            write( writer, "abstract", a, dct );
        }
    }

    private void write( XMLStreamWriter writer, String elementName, String value, QName ns )
                            throws XMLStreamException {
        if ( value != null ) {
            writer.writeStartElement( ns.getPrefix(), elementName, ns.getNamespaceURI() );
            writer.writeNamespace( ns.getPrefix(), ns.getNamespaceURI() );
            writer.writeCharacters( value );
            writer.writeEndElement();
        }
    }

    private void toDCBrief( XMLStreamWriter writer )
                            throws XMLStreamException {
        writer.writeStartElement( csw.getPrefix(), "BriefRecord", csw.getNamespaceURI() );
        writer.writeNamespace( csw.getPrefix(), csw.getNamespaceURI() );
        writeBriefElements( writer );
        writeBoundingBox( writer );
        writer.writeEndElement();

    }

    private void writeBriefElements( XMLStreamWriter writer )
                            throws XMLStreamException {
        if ( getIdentifier() != null ) {
            write( writer, "identifier", getIdentifier(), dc );
        }
        for ( String t : getTitle() ) {
            write( writer, "title", t, dc );
        }
        if ( getType() != null ) {
            write( writer, "type", getType(), dc );
        }
    }

    private void writeBoundingBox( XMLStreamWriter writer )
                            throws XMLStreamException {
        for ( Envelope b : getBoundingBox() ) {
            writer.writeStartElement( ows.getPrefix(), "BoundingBox", ows.getNamespaceURI() );
            writer.writeNamespace( ows.getPrefix(), ows.getNamespaceURI() );
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

    @Override
    public void update( ValueReference propName, String replaceValue ) {
        throw new UnsupportedOperationException( "Update is not allowed for DCRecords" );
    }

    @Override
    public void update( ValueReference propName, OMElement replaceValue ) {
        throw new UnsupportedOperationException( "Update is not allowed for DCRecords" );
    }

    @Override
    public void removeNode( ValueReference propName ) {
        throw new UnsupportedOperationException( "Remove is not allowed for DCRecords" );
    }

    @Override
    public OMElement getAsOMElement() {
        throw new UnsupportedOperationException( "Needs implementation." );
    }
}