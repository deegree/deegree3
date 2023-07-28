/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2011 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

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
package org.deegree.commons.xml;

import java.io.InputStream;
import java.io.Reader;

import org.w3c.dom.ls.LSInput;

/**
 * Generic implementation of <code>LSInput</code>.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GenericLSInput implements LSInput {

	private String baseUri;

	private InputStream byteStream;

	private boolean certifiedText;

	private Reader characterStream;

	private String encoding;

	private String publicId;

	private String stringData;

	private String systemId;

	@Override
	public String getBaseURI() {
		return baseUri;
	}

	@Override
	public InputStream getByteStream() {
		return byteStream;
	}

	@Override
	public boolean getCertifiedText() {
		return certifiedText;
	}

	@Override
	public Reader getCharacterStream() {
		return characterStream;
	}

	@Override
	public String getEncoding() {
		return encoding;
	}

	@Override
	public String getPublicId() {
		return publicId;
	}

	@Override
	public String getStringData() {
		return stringData;
	}

	@Override
	public String getSystemId() {
		return systemId;
	}

	@Override
	public void setBaseURI(String baseUri) {
		this.baseUri = baseUri;
	}

	@Override
	public void setByteStream(InputStream byteStream) {
		this.byteStream = byteStream;
	}

	@Override
	public void setCertifiedText(boolean certifiedText) {
		this.certifiedText = certifiedText;
	}

	@Override
	public void setCharacterStream(Reader characterStream) {
		this.characterStream = characterStream;
	}

	@Override
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	@Override
	public void setPublicId(String publicId) {
		this.publicId = publicId;
	}

	@Override
	public void setStringData(String stringData) {
		this.stringData = stringData;
	}

	@Override
	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

}
