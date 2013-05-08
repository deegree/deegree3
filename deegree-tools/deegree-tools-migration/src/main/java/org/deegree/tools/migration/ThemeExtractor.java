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
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.deegree.commons.config.DeegreeWorkspace;
import org.deegree.commons.config.ResourceState;
import org.deegree.services.controller.WebServicesConfiguration;
import org.deegree.services.wms.controller.WMSController;
import org.deegree.theme.persistence.ThemeProvider;
import org.deegree.workspace.WorkspaceUtils;

/**
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author: stranger $
 * 
 * @version $Revision: $, $Date: $
 */
public class ThemeExtractor {

    private final DeegreeWorkspace workspace;

    private Transformer transformer;

    public ThemeExtractor( DeegreeWorkspace workspace ) throws TransformerConfigurationException {
        this.workspace = workspace;
        TransformerFactory fac = TransformerFactory.newInstance();
        InputStream xsl = ThemeExtractor.class.getResourceAsStream( "extracttheme.xsl" );
        this.transformer = fac.newTransformer( new StreamSource( xsl ) );
    }

    public void transform()
                            throws TransformerException, XMLStreamException {
        WebServicesConfiguration mgr = workspace.getSubsystemManager( WebServicesConfiguration.class );
        ResourceState<?>[] states = mgr.getStates();
        for ( ResourceState<?> s : states ) {
            if ( s.getResource() instanceof WMSController ) {
                File loc = s.getConfigLocation();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                transformer.transform( new StreamSource( loc ), new StreamResult( bos ) );

                ThemeXmlStreamEncoder.writeOut( bos );

                WorkspaceUtils.activateSynthetic( workspace.getNewWorkspace(), ThemeProvider.class, s.getId(),
                                                  new String( bos.toByteArray(), Charset.forName( "UTF-8" ) ) );
            }
        }
    }

}
