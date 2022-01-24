/*----------------------------------------------------------------------------
 This file is part of deegree
 Copyright (C) 2001-2013 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -
 and others

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

 e-mail: info@deegree.org
 website: http://www.deegree.org/
----------------------------------------------------------------------------*/
package org.deegree.style.persistence.sld;

import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.deegree.style.persistence.sld.SLDParser.getStyles;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.style.persistence.StyleStore;
import org.deegree.style.se.unevaluated.Style;
import org.deegree.workspace.ResourceBuilder;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.standard.DefaultResourceLocation;

/**
 * This class is responsible for building SLD style stores.
 * 
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 * 
 * @since 3.4
 */
public class SldStyleStoreBuilder implements ResourceBuilder<StyleStore> {

    private ResourceMetadata<StyleStore> metadata;

    public SldStyleStoreBuilder( ResourceMetadata<StyleStore> metadata ) {
        this.metadata = metadata;
    }

    @Override
    public StyleStore build() {
        InputStream in = null;
        XMLStreamReader reader = null;
        try {
            in = metadata.getLocation().getAsStream();
            DefaultResourceLocation<StyleStore> loc = (DefaultResourceLocation<StyleStore>) metadata.getLocation();
            XMLInputFactory fac = XMLInputFactory.newInstance();
            reader = fac.createXMLStreamReader( loc.getAsFile().toString(), in );
            Map<String, LinkedList<Style>> map = getStyles( reader );
            return new SLDStyleStore( map, metadata );
        } catch ( Exception e ) {
            throw new ResourceInitException( "Could not read SLD style config.", e );
        } finally {
            try {
                if ( reader != null ) {
                    reader.close();
                }
            } catch ( XMLStreamException e ) {
                // eat it
            }
            closeQuietly( in );
        }
    }

}
