//$HeadURL: svn+ssh://aschmitz@wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
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
package org.deegree.tools.migration;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;

import org.deegree.commons.xml.XsltUtils;
import org.deegree.services.OWS;
import org.deegree.services.OwsManager;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.workspace.Resource;
import org.deegree.workspace.ResourceIdentifier;
import org.deegree.workspace.ResourceMetadata;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.WorkspaceUtils;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ThemeExtractor {

    private ThemeExtractor() {
    }

    public static void transform( Workspace workspace )
                            throws TransformerException, XMLStreamException, URISyntaxException, IOException {
        OwsManager mgr = workspace.getResourceManager( OwsManager.class );
        List<OWS> wmss = mgr.getByOWSClass( WMSController.class );
        for ( OWS ows : wmss ) {
            ResourceMetadata<? extends Resource> md = ows.getMetadata();
            ResourceIdentifier<? extends Resource> id = md.getIdentifier();
            File loc = md.getLocation().resolveToFile( id.getId() + ".xml" );
            ByteArrayOutputStream bos = new ByteArrayOutputStream();

            FileInputStream doc = new FileInputStream( loc );
            XsltUtils.transform( doc, ThemeExtractor.class.getResource( "extracttheme.xsl" ), bos );
            doc.close();

            ThemeXmlStreamEncoder.writeOut( bos );

            WorkspaceUtils.activateSynthetic( workspace, ThemeProvider.class, id.getId(),
                                              new String( bos.toByteArray(), Charset.forName( "UTF-8" ) ) );
        }
    }

}
