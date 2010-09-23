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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.datetime.Date;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.filter.Filter;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.csw.CSWConstants.ReturnableElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents an ISO 19115 {@link MetadataRecord}.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class ISORecord implements MetadataRecord {
    private static Logger LOG = LoggerFactory.getLogger( ISORecord.class );

    private XMLStreamReader root;

    public ISORecord( XMLStreamReader root ) {
        this.root = root;
    }

    @Override
    public boolean eval( Filter filter ) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String[] getAbstract() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Envelope[] getBoundingBox() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getFormat() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getIdentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Date[] getModified() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getRelation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object[] getSpatial() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String[] getTitle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void serialize( XMLStreamWriter writer, ReturnableElement returnType )
                            throws XMLStreamException {

        root.nextTag();
        XMLAdapter.writeElement( writer, root );

        root.close();

    }

    @Override
    public DCRecord toDublinCore() {
        // TODO Auto-generated method stub
        return null;
    }

}