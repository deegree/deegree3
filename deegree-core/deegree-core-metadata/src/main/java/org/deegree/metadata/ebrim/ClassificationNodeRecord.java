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

import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.ebrim.model.ClassificationNode;
import org.deegree.metadata.ebrim.model.RegistryObject;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:goltz@lat-lon.org">Lyn Goltz</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class ClassificationNodeRecord extends RegistryObjectRecord {

    public ClassificationNodeRecord( XMLStreamReader xmlReader ) {
        super( xmlReader );
    }

    public ClassificationNodeRecord( OMElement record ) {
        super( record );
    }

    @Override
    public ClassificationNode getParsedRecord() {
        RegistryObject ro = super.getParsedRecord();
        OMElement record = adapter.getRootElement();
        String parent = adapter.getNodeAsString( record, new XPath( "./@parent", ns ), null );
        String code = adapter.getNodeAsString( record, new XPath( "./@code", ns ), null );
        String path = adapter.getNodeAsString( record, new XPath( "./@path", ns ), null );
        return new ClassificationNode( ro, parent, code, path );
    }

}
