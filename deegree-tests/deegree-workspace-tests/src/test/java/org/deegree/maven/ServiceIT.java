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

 Occam Labs UG (haftungsbeschränkt)
 Godesberger Allee 139, 53175 Bonn
 Germany
 http://www.occamlabs.de/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.maven;

import org.deegree.maven.ithelper.ServiceIntegrationTestHelper;
import org.deegree.maven.ithelper.TestEnvironment;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.File;

import static org.slf4j.LoggerFactory.getLogger;

/**
 *
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 *
 * @version $Revision$, $Date$
 */
public class ServiceIT {

    private static final Logger LOG = getLogger(ServiceIT.class);

    private boolean testCapabilities = true;

    private boolean testLayers = true;

    private boolean testRequests = true;

    private File workspace = new File("./src/main/webapp/WEB-INF/workspace");

    private TestEnvironment env = new TestEnvironment(System.getProperties());

    @Test
    public void execute() throws Exception {
        try {
            if (!workspace.exists()) {
                workspace = new File(env.getBasedir(), "src/main/webapp/WEB-INF/conf");
                if (!workspace.exists()) {
                    LOG.error("Could not find a workspace to operate on.");
                    throw new RuntimeException("Could not find a workspace to operate on.");
                }
                LOG.warn("Default/configured workspace did not exist, using existing " + workspace
                        + " instead.");
            }

            ServiceIntegrationTestHelper helper = new ServiceIntegrationTestHelper(env);

            File[] listed = new File(workspace, "services").listFiles();
            if (listed != null) {
                for (File f : listed) {
                    String nm = f.getName().toLowerCase();
                    if (nm.length() != 7) {
                        continue;
                    }
                    String service = nm.substring(0, 3).toUpperCase();
                    if (testCapabilities) {
                        helper.testCapabilities(service);
                    }
                    if (testLayers) {
                        helper.testLayers(service);
                        LOG.info("All maps can be requested.");
                    }
                }
            }

            if (testRequests) {
                helper.testRequests();
            }
        } catch (NoClassDefFoundError e) {
            LOG.warn("Class not found, not performing any tests.");
        }
    }

}

