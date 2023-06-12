/**
 *
 */
package org.deegree.metadata.iso.persistence.sql;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XPath;
import org.deegree.metadata.iso.ISORecord;
import org.deegree.metadata.persistence.iso19115.jaxb.ISOMetadataStoreConfig.AnyText;

/**
 * @author markus
 *
 */
class AnyTextHelper {

	private final static String STOPWORD = " ";

	private static final NamespaceBindings ns = CommonNamespaces.getNamespaceContext();

	public static String getAnyText(ISORecord r, AnyText anyText) {

		String anyTextString = null;
		if (anyText == null || anyText.getCore() != null) {
			StringBuilder sb = new StringBuilder();

			for (String s : r.getAbstract()) {
				sb.append(s).append(STOPWORD);
			}
			for (String f : r.getFormat()) {
				sb.append(f).append(STOPWORD);
			}
			if (r.getIdentifier() != null) {
				sb.append(r.getIdentifier()).append(STOPWORD);
			}
			if (r.getLanguage() != null) {
				sb.append(r.getLanguage()).append(STOPWORD);
			}
			if (r.getModified() != null) {
				sb.append(r.getModified()).append(STOPWORD);
			}
			for (String f : r.getRelation()) {
				sb.append(f).append(STOPWORD);
			}
			for (String f : r.getTitle()) {
				sb.append(f).append(STOPWORD);
			}
			if (r.getType() != null) {
				sb.append(r.getType()).append(STOPWORD);
			}
			for (String f : r.getSubject()) {
				sb.append(f).append(STOPWORD);
			}
			sb.append(r.isHasSecurityConstraints()).append(STOPWORD);
			for (String f : r.getRights()) {
				sb.append(f).append(STOPWORD);
			}
			if (r.getContributor() != null) {
				sb.append(r.getContributor()).append(STOPWORD);
			}
			if (r.getPublisher() != null) {
				sb.append(r.getPublisher()).append(STOPWORD);
			}
			if (r.getSource() != null) {
				sb.append(r.getSource()).append(STOPWORD);
			}
			if (r.getCreator() != null) {
				sb.append(r.getCreator()).append(STOPWORD);
			}
			if (r.getParentIdentifier() != null) {
				sb.append(r.getParentIdentifier()).append(STOPWORD);
			}
			anyTextString = sb.toString();
		}
		else if (anyText.getAll() != null) {
			StringBuilder sb = new StringBuilder();
			try {
				XMLStreamReader xmlStream = r.getAsXMLStream();
				while (xmlStream.hasNext()) {
					xmlStream.next();
					if (xmlStream.getEventType() == XMLStreamConstants.CHARACTERS && !xmlStream.isWhiteSpace()) {
						sb.append(xmlStream.getText()).append(STOPWORD);
					}
				}
			}
			catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			anyTextString = sb.toString();

		}
		else if (anyText.getCustom() != null) {
			List<String> xpathList = anyText.getCustom().getXPath();
			if (xpathList != null && !xpathList.isEmpty()) {
				XPath[] path = new XPath[xpathList.size()];
				int counter = 0;
				for (String x : xpathList) {
					path[counter++] = new XPath(x, ns);
				}
				anyTextString = generateAnyText(r, path).toString();
			}
		}
		else {
			anyTextString = "";
		}
		return anyTextString;
	}

	private static StringBuilder generateAnyText(ISORecord r, XPath[] xpath) {
		StringBuilder sb = new StringBuilder();
		List<String> textNodes = new ArrayList<String>();

		for (XPath x : xpath) {

			String[] tmp = new XMLAdapter().getNodesAsStrings(r.getAsOMElement(), x);
			for (String s : tmp) {
				textNodes.add(s);
			}
		}
		for (String s : textNodes) {
			sb.append(s).append(STOPWORD);
		}

		return sb;
	}

}
