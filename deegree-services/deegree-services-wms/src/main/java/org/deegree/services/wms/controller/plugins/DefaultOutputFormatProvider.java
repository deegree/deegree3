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
package org.deegree.services.wms.controller.plugins;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.deegree.rendering.r2d.context.LazyImageRenderContext;
import org.deegree.rendering.r2d.context.RenderContext;
import org.deegree.rendering.r2d.context.RenderingInfo;
import org.deegree.rendering.r2d.context.SvgRenderContext;

/**
 * @author <a href="mailto:schmitz@lat-lon.de">Andreas Schmitz</a>
 */
public class DefaultOutputFormatProvider implements OutputFormatProvider {

	private static final Collection<String> SUPPORTED_OUTPUT_FORMATS = new LinkedHashSet<String>(
			Arrays.asList("image/png", "image/png; subtype=8bit", "image/png; mode=8bit", "image/gif", "image/jpeg",
					"image/tiff", "image/x-ms-bmp", "image/svg+xml"));

	@Override
	public Collection<String> getSupportedOutputFormats() {
		return SUPPORTED_OUTPUT_FORMATS;
	}

	@Override
	public RenderContext getRenderers(RenderingInfo info, OutputStream outputStream) {
		if ("image/svg+xml".equals(info.getFormat())) {
			return SvgRenderContext.createInstance(info, outputStream);
		}
		else {
			return new LazyImageRenderContext(info, outputStream);
		}
	}

}
