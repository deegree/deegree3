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
package org.deegree.client.mdeditor.config;

import java.util.HashMap;
import java.util.Map;

import org.deegree.client.mdeditor.model.FormConfiguration;

/**
 * TODO add class documentation here
 * 
 * @author <a href="mailto:buesching@lat-lon.de">Lyn Buesching</a>
 * @author last edited by: $Author: lyn $
 * 
 * @version $Revision: $, $Date: $
 */
public class FormConfigurationFactory {

    private static Map<String, FormConfiguration> formConfigurations = new HashMap<String, FormConfiguration>();

    /**
     * @param id
     *            identifier of the configuration
     * @return the configuration assigned by the key, if no configuration exist a new one will be created
     * @throws ConfigurationException
     */
    public static FormConfiguration getOrCreateFormConfiguration( String id )
                            throws ConfigurationException {
        if ( !formConfigurations.containsKey( id ) ) {
            FormConfigurationParser parser = new FormConfigurationParser();
            FormConfiguration configuration = parser.parseConfiguration( Configuration.getFormConfURL() );
            formConfigurations.put( id, configuration );
        }
        return formConfigurations.get( id );
    }

    /**
     * @param id
     *            identifier of the configuration to reload
     * @return reloads the configuration with the given key
     * @throws ConfigurationException
     */
    public static void reloadFormConfiguration( String key )
                            throws ConfigurationException {
        FormConfigurationParser parser = new FormConfigurationParser();
        FormConfiguration configuration = parser.parseConfiguration( Configuration.getFormConfURL() );
        formConfigurations.put( key, configuration );
    }

}
