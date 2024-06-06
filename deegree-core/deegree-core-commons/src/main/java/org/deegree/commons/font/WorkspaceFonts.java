/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2022 by:
 - grit graphische Informationstechnik Beratungsgesellschaft mbH -

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

 grit graphische Informationstechnik Beratungsgesellschaft mbH
 Landwehrstr. 143, 59368 Werne
 Germany
 http://www.grit.de/

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
package org.deegree.commons.font;

import static org.deegree.commons.utils.TunableParameter.get;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.deegree.workspace.Initializable;
import org.deegree.workspace.Workspace;
import org.deegree.workspace.standard.DefaultWorkspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkspaceFonts implements Initializable {

	protected static final Set<String> PROCESSED_FILES = new HashSet<>();

	private static final boolean ENABLED = get("deegree.workspace.allow-font-loading", false);

	private static final Logger LOG = LoggerFactory.getLogger(WorkspaceFonts.class);

	private static final String FONT_DIR = "fonts";

	@Override
	public void init(Workspace workspace) {
		if (!ENABLED) {
			LOG.debug(
					"Loading fonts from workspace is disabled, set deegree.workspace.allow-font-loading=true to enable it.");
			return;
		}
		LOG.info("--------------------------------------------------------------------------------");
		LOG.info("Fonts in workspace.");
		LOG.info("--------------------------------------------------------------------------------");
		if (loadFontsFromWorkspace(workspace)) {
			LOG.info("Fonts successfully loaded from workspace.");
			return;
		}
		LOG.info("No Fonts to register");
	}

	private boolean loadFontsFromWorkspace(final Workspace ws) {
		File fontDir = new File(((DefaultWorkspace) ws).getLocation(), FONT_DIR);
		if (!fontDir.isDirectory()) {
			return false;
		}
		boolean loaded = false;
		for (File f : FileUtils.listFiles(fontDir, new String[] { "ttf" }, false)) {
			loaded = true;
			registerOnce(f);
		}
		return loaded;
	}

	/**
	 * Load font and register it in the local {@link GraphicsEnvironment}
	 *
	 * Note: If a file has already been processed, it wont be loaded or registered again
	 * @param fontFile font to be processed
	 */
	static void registerOnce(File fontFile) {
		if (fontFile == null) {
			return;
		}
		final String fileKey = fontFile.getAbsolutePath();
		if (PROCESSED_FILES.contains(fileKey)) {
			// do not re-register fonts, as fonts can not be deregistered in
			// GraphicsEnvironment
			LOG.info("Skip file '{}' because it was already processed.", fontFile.getName());
			return;
		}
		PROCESSED_FILES.add(fileKey);
		try {
			Font f = Font.createFont(Font.TRUETYPE_FONT, fontFile);
			GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
			LOG.info("Loaded Font: {} (face: {}, family: {}, file: {})", f.getName(), f.getFontName(), f.getFamily(),
					fontFile.getName());
		}
		catch (Exception e) {
			LOG.warn("Font '{}' could not be loaded: {}", e.getMessage());
			LOG.trace("Exception", e);
		}
	}

}
