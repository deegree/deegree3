//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2012 by:
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
package org.deegree.maven;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

/**
 * @goal list-modules-wiki
 * 
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date: 2011-09-13 15:43:38 +0200 (Di, 13. Sep
 *          2011) $
 */
public class ModuleListMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		FileOutputStream fos = null;		
		try {
			if (!project.getPackaging().equalsIgnoreCase("pom")) {

				String status = project.getProperties().getProperty(
						"deegree.module.status");
				if (status == null) {
					status = "unknown";
				}

				File f = new File("/tmp/" + status + ".txt");
				fos = new FileOutputStream(f, true);
				PrintWriter writer = new PrintWriter(fos);
				
				writer.print("||");
				writer.print(project.getArtifactId());
				writer.print("||");
				writer.print(project.getDescription());
				writer.print("||");
				writer.print("\n");
				writer.close();
			}
		} catch (FileNotFoundException e) {
			throw new MojoExecutionException(e.getMessage());
		} finally {
			IOUtils.closeQuietly(fos);
		}
	}
}
