package org.deegree.protocol.wfs.capabilities;

import static org.deegree.protocol.wfs.WFSConstants.WFS_NS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.protocol.ows.capabilities.OWSCommon100CapabilitiesAdapter;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;

/**
 * {@link WFSCapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wfs>WFS 1.1.0</a> specification.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFS110CapabilitiesAdapter extends OWSCommon100CapabilitiesAdapter implements WFSCapabilitiesAdapter {

	public WFS110CapabilitiesAdapter() {
		nsContext.addNamespace("wfs", WFS_NS);
	}

	@Override
	public List<WFSFeatureType> parseFeatureTypeList() {
		List<OMElement> ftEls = getElements(rootElement, new XPath("wfs:FeatureTypeList/wfs:FeatureType", nsContext));
		List<WFSFeatureType> fts = new ArrayList<WFSFeatureType>(ftEls.size());
		for (OMElement ftEl : ftEls) {
			fts.add(parseFeatureType(ftEl));
		}
		return fts;
	}

	private WFSFeatureType parseFeatureType(OMElement ftEl) {

		// <xsd:element name="Name" type="xsd:QName"/>
		OMElement nameEl = getRequiredElement(ftEl, new XPath("wfs:Name", nsContext));
		String prefixedName = nameEl.getText().trim();
		QName ftName = parseQName(prefixedName, nameEl);

		// <xsd:element name="Title" type="xsd:string">
		List<LanguageString> titles = Collections.emptyList();
		String title = getRequiredNodeAsString(ftEl, new XPath("wfs:Title", nsContext));
		titles = Collections.singletonList(new LanguageString(title.trim(), null));

		// <xsd:element name="Abstract" type="xsd:string" minOccurs="0">
		List<LanguageString> abstracts = Collections.emptyList();
		String ftAbstract = getNodeAsString(ftEl, new XPath("wfs:Abstract", nsContext), null);
		if (ftAbstract != null) {
			abstracts = Collections.singletonList(new LanguageString(ftAbstract.trim(), null));
		}

		// <xsd:element ref="ows:Keywords" minOccurs="0" maxOccurs="unbounded"/>
		List<Object> keywords = null;
		// TODO

		// <xsd:element name="DefaultSRS" type="xsd:anyURI">
		String srs = getNodeAsString(ftEl, new XPath("wfs:SRS", nsContext), null);
		CRSRef defaultCrs = CRSManager.getCRSRef(srs);

		// <xsd:element name="OtherSRS" type="xsd:anyURI" minOccurs="0"
		// maxOccurs="unbounded">
		String[] otherSrsStrings = getNodesAsStrings(ftEl, new XPath("wfs:OtherSRS", nsContext));
		List<CRSRef> otherSrs = new ArrayList<CRSRef>(otherSrsStrings.length);
		for (String srsString : otherSrsStrings) {
			otherSrs.add(CRSManager.getCRSRef(srsString));
		}

		// <xsd:element name="NoSRS">

		// <xsd:element name="Operations" type="wfs:OperationsType" minOccurs="0"/>
		// TODO

		// <xsd:element name="OutputFormats" type="wfs:OutputFormatListType"
		// minOccurs="0"/>
		// TODO

		// <xsd:element ref="ows:WGS84BoundingBox" minOccurs="0" maxOccurs="unbounded"/>
		List<OMElement> bboxEls = getElements(ftEl, new XPath("ows:WGS84BoundingBox", nsContext));
		List<Envelope> wgs84BBoxes = new ArrayList<Envelope>(bboxEls.size());
		for (OMElement bboxEl : bboxEls) {
			wgs84BBoxes.add(parseWGS84BoundingBox(bboxEl));
		}

		// <xsd:element name="MetadataURL" type="wfs:MetadataURLType" minOccurs="0"
		// maxOccurs="unbounded"/>
		// TODO
		List<Object> mdReferences = null;

		return new WFSFeatureType(ftName, titles, abstracts, null, keywords, defaultCrs, null, wgs84BBoxes,
				mdReferences, null);
	}

	@Override
	public Object parseFilterCapabilities() {
		return null;
	}

}
