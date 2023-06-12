/*-
 * #%L
 * deegree-cli-utility
 * %%
 * Copyright (C) 2022 grit graphische Informationstechnik Beratungsgesellschaft mbH
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.deegree.tools.featurestoresql.loader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import org.deegree.feature.stream.FeatureInputStream;
import org.deegree.gml.GMLStreamReader;

/**
 * Extension point that allows to use custom FeatureInputStreams
 *
 * @author <a href="mailto:reichhelm@grit.de">Stephan Reichhelm</a>
 */
public interface FeatureStreamFactory {

	/**
	 * Check if the root element of the XML stream is applicable
	 * @param rootElement root element from the document or stream
	 * @return true if this factory can supply a capable FeatureInputStream
	 */
	public boolean isApplicableToDocumentRoot(QName rootElement);

	public FeatureInputStream createStream(XMLStreamReader xmlStream, GMLStreamReader gmlStream);

}
