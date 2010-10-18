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

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.filter.Filter;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;

/**
 * Base interface for metadata records.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public interface MetadataRecord {

    public QName getName();

    public String[] getIdentifier();

    public String[] getTitle();

    public String getType();

    public String[] getFormat();

    public String[] getRelation();

    public Date[] getModified();

    public String[] getAbstract();

    public Object[] getSpatial();

    public String[] getSubject();

    public String getSource();

    public String[] getRights();

    public String getCreator();

    public String getPublisher();

    public String getContributor();

    public String getLanguage();

    public Envelope[] getBoundingBox();

    /**
     * Returns the Dublin Core representation of the requested record.
     * 
     * @return {@link DCRecord}.
     */
    public DCRecord toDublinCore();

    /**
     * Returns whether this {@link MetadataRecord} matches the given {@link Filter} expression.
     * 
     * @param filter
     *            filter to evaluate, must not be <code>null</code>
     * @return true, if the record matches the filter, false otherwise
     */
    public boolean eval( Filter filter );

    /**
     * Writes the XML representation of this record to the given XML stream.
     * 
     * @param writer
     *            xml writer to write to, must not be <code>null</code>
     * @param returnType
     *            the element set to return, never <code>null</code>
     * @throws XMLStreamException
     *             if the writing of the XML fails
     */
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException;

    /**
     * Writes the XML representation of the specified elements to the given XML stream.
     * 
     * @param writer
     *            xml writer to write to, must not be <code>null</code>
     * @param elementNames
     *            the element set to return, must not be <Code>null</Code>.
     * @throws XMLStreamException
     *             if the writing of the XML fails
     */
    public void serialize( XMLStreamWriter writer, String[] elementNames )
                            throws XMLStreamException;
}