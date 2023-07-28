/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -
 and
 - Occam Labs UG (haftungsbeschränkt) -

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

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/
package org.deegree.services.wmts.controller;

import org.deegree.featureinfo.FeatureInfoManager;
import org.deegree.gml.GMLVersion;
import org.deegree.services.wmts.jaxb.FeatureInfoFormatsType;
import org.deegree.services.wmts.jaxb.FeatureInfoFormatsType.GetFeatureInfoFormat;
import org.deegree.services.wmts.jaxb.FeatureInfoFormatsType.GetFeatureInfoFormat.XSLTFile;
import org.deegree.workspace.ResourceInitException;
import org.deegree.workspace.ResourceLocation;
import org.deegree.workspace.Workspace;

/**
 * Builds a {@link FeatureInfoManager} from jaxb config.
 *
 * @author <a href="mailto:schmitz@occamlabs.de">Andreas Schmitz</a>
 */
class FeatureInfoManagerBuilder {

	static FeatureInfoManager buildFeatureInfoManager(FeatureInfoFormatsType conf, ResourceLocation<?> location,
			Workspace workspace) throws ResourceInitException {
		FeatureInfoManager featureInfoManager = new FeatureInfoManager(true);

		try {
			if (conf != null) {
				for (GetFeatureInfoFormat t : conf.getGetFeatureInfoFormat()) {
					if (t.getFile() != null) {
						featureInfoManager.addOrReplaceFormat(t.getFormat(),
								location.resolveToFile(t.getFile()).toString());
					}
					else {
						XSLTFile xsltFile = t.getXSLTFile();
						GMLVersion version = GMLVersion.valueOf(xsltFile.getGmlVersion().toString());
						featureInfoManager.addOrReplaceXsltFormat(t.getFormat(),
								location.resolveToUrl(xsltFile.getValue()), version, workspace);
					}
				}
			}
		}
		catch (Exception e) {
			throw new ResourceInitException(
					"GetFeatureInfo format handler could not be initialized: " + e.getLocalizedMessage(), e);
		}

		return featureInfoManager;
	}

}
