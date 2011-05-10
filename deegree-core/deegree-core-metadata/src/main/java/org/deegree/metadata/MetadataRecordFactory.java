//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
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

import static org.deegree.metadata.DCRecord.DC_RECORD_NS;
import static org.deegree.metadata.ebrim.RegistryObject.RIM_NS;
import static org.deegree.metadata.iso.ISORecord.ISO_RECORD_NS;

import java.io.File;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.metadata.ebrim.Association;
import org.deegree.metadata.ebrim.ClassificationNode;
import org.deegree.metadata.ebrim.Classification;
import org.deegree.metadata.ebrim.ExtrinsicObject;
import org.deegree.metadata.ebrim.RegistryPackage;
import org.deegree.metadata.iso.ISORecord;

/**
 * Main entry point for creating {@link MetadataRecord} instances from XML representations.
 * 
 * TODO Factory concept needs reconsideration, especially with regard to plugability for different metadata formats
 * (ISO, ebRIM, ...). Ideally, this factory shouldn't have any compile-time dependencies to the concrete record types.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class MetadataRecordFactory {

    /**
     * Creates a new {@link MetadataRecord} from the given element.
     * 
     * @param rootEl
     *            root element, must not be <code>null</code>
     * @return metadata record instance, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the metadata format is unknown / record invalid
     */
    public static MetadataRecord create( OMElement rootEl )
                            throws IllegalArgumentException {
        String ns = rootEl.getNamespace().getNamespaceURI();
        if ( ISO_RECORD_NS.equals( ns ) ) {
            return new ISORecord( rootEl );
        }
        if ( RIM_NS.equals( ns ) ) {
            String name = rootEl.getLocalName();
            if ( "ExtrinsicObject".equals( name ) ) {
                return new ExtrinsicObject( rootEl );
            } else if ( "Association".equals( name ) ) {
                return new Association( rootEl );
            } else if ( "Classification".equals( name ) ) {
                return new Classification( rootEl );
            } else if ( "ClassificationNode".equals( name ) ) {
                return new ClassificationNode( rootEl );
            } else if ( "RegistryPackage".equals( name ) ) {
                return new RegistryPackage( rootEl );
            }
            throw new IllegalArgumentException( "Unknown / unsuppported RegistryObject '" + name + "'." );
        }
        if ( DC_RECORD_NS.equals( ns ) ) {
            throw new UnsupportedOperationException( "Creating DC records from XML is not implemented yet." );
        }
        throw new IllegalArgumentException( "Unknown / unsuppported metadata namespace '" + ns + "'." );
    }

    /**
     * Creates a new {@link MetadataRecord} from the given file.
     * 
     * @param file
     *            record file, must not be <code>null</code>
     * @return metadata record instance, never <code>null</code>
     * @throws IllegalArgumentException
     *             if the metadata format is unknown / record invalid
     */
    public static MetadataRecord create( File file )
                            throws IllegalArgumentException {
        return create( new XMLAdapter( file ).getRootElement() );
    }
}