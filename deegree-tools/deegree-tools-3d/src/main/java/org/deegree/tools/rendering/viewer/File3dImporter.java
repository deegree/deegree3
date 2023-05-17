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
package org.deegree.tools.rendering.viewer;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.IOUtils;
import org.deegree.rendering.r3d.model.geometry.GeometryQualityModel;
import org.deegree.rendering.r3d.opengl.rendering.model.geometry.WorldRenderableObject;
import org.deegree.tools.rendering.manager.buildings.importers.CityGMLImporter;

/**
 * TODO add class documentation here
 *
 * @author <a href="mailto:bezema@lat-lon.de">Rutger Bezema</a>
 */
public class File3dImporter {

	public static List<GeometryQualityModel> gm;

	public static List<WorldRenderableObject> open(Frame parent, String fileName) {

		if (fileName == null || "".equals(fileName.trim())) {
			throw new InvalidParameterException("the file name may not be null or empty");
		}
		fileName = fileName.trim();

		CityGMLImporter openFile2;
		XMLInputFactory fac = XMLInputFactory.newInstance();
		InputStream in = null;
		try {
			XMLStreamReader reader = fac.createXMLStreamReader(in = new FileInputStream(fileName));
			reader.next();
			String ns = "http://www.opengis.net/citygml/1.0";
			openFile2 = new CityGMLImporter(null, null, null, reader.getNamespaceURI().equals(ns));
		}
		catch (Throwable t) {
			openFile2 = new CityGMLImporter(null, null, null, false);
		}
		finally {
			IOUtils.closeQuietly(in);
		}

		final CityGMLImporter openFile = openFile2;

		final JDialog dialog = new JDialog(parent, "Loading", true);

		dialog.getContentPane().setLayout(new BorderLayout());
		dialog.getContentPane()
			.add(new JLabel("<HTML>Loading file:<br>" + fileName + "<br>Please wait!</HTML>", SwingConstants.CENTER),
					BorderLayout.NORTH);
		final JProgressBar progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		progressBar.setIndeterminate(false);
		dialog.getContentPane().add(progressBar, BorderLayout.CENTER);

		dialog.pack();
		dialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setLocationRelativeTo(parent);

		final Thread openThread = new Thread() {
			/**
			 * Opens the file in a separate thread.
			 */
			@Override
			public void run() {
				// openFile.openFile( progressBar );
				if (dialog.isDisplayable()) {
					dialog.setVisible(false);
					dialog.dispose();
				}
			}
		};
		openThread.start();

		dialog.setVisible(true);
		List<WorldRenderableObject> result = null;
		try {
			result = openFile.importFromFile(fileName, 6, 2);
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gm = openFile.getQmList();

		//
		// if ( result != null ) {
		// openGLEventListener.addDataObjectToScene( result );
		// File f = new File( fileName );
		// setTitle( WIN_TITLE + f.getName() );
		// } else {
		// showExceptionDialog( "The file: " + fileName
		// + " could not be read,\nSee error log for detailed information." );
		// }
		return result;
	}

}
