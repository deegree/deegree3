package org.deegree.protocol.wfs.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.ows.metadata.OperationsMetadata;
import org.deegree.commons.ows.metadata.ServiceIdentification;
import org.deegree.commons.ows.metadata.ServiceProvider;
import org.deegree.commons.tom.ows.CodeType;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.utils.Pair;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link WFSCapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wfs>WFS 1.0.0</a> specification.
 *
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class WFS100CapabilitiesAdapter extends XMLAdapter implements WFSCapabilitiesAdapter {

	private static final Logger LOG = LoggerFactory.getLogger(WFS100CapabilitiesAdapter.class);

	private static final NamespaceBindings nsContext = new NamespaceBindings();

	static {
		nsContext.addNamespace("wfs", WFSConstants.WFS_NS);
	}

	@Override
	public ServiceIdentification parseServiceIdentification() throws XMLParsingException {

		OMElement serviceEl = getElement(rootElement, new XPath("wfs:Service", nsContext));
		if (serviceEl == null) {
			LOG.warn("Mandatory element 'wfs:Service' is missing.");
			return null;
		}

		// <xsd:element name="Name" type="xsd:string"/>
		String name = getNodeAsString(serviceEl, new XPath("wfs:Name", nsContext), null);

		// <xsd:element ref="wfs:Title"/>
		List<LanguageString> titles = new ArrayList<LanguageString>();
		String title = getNodeAsString(serviceEl, new XPath("wfs:Title", nsContext), null);
		if (title != null) {
			titles.add(new LanguageString(title, null));
		}

		// <xsd:element ref="wfs:Abstract" minOccurs="0"/>
		List<LanguageString> abstracts = new ArrayList<LanguageString>();
		String abstr = getNodeAsString(serviceEl, new XPath("wfs:Abstract", nsContext), null);
		if (abstr != null) {
			abstracts.add(new LanguageString(abstr, null));
		}

		// <xsd:element ref="wfs:Keywords" minOccurs="0"/>
		List<Pair<List<LanguageString>, CodeType>> keywords = new ArrayList<Pair<List<LanguageString>, CodeType>>();
		String keywordsStr = getNodeAsString(serviceEl, new XPath("wfs:Keywords", nsContext), null);
		if (keywordsStr != null) {
			List<LanguageString> ls = Collections.singletonList(new LanguageString(keywordsStr, null));
			keywords.add(new Pair<List<LanguageString>, CodeType>(ls, null));
		}

		// <xsd:element ref="wfs:Fees" minOccurs="0"/>
		String fees = getNodeAsString(serviceEl, new XPath("wfs:Fees", nsContext), null);

		// <xsd:element ref="wfs:AccessConstraints" minOccurs="0"/>
		List<String> accessConstraints = new ArrayList<String>();
		String accessConstraintsStr = getNodeAsString(serviceEl, new XPath("wfs:AccessConstraints", nsContext), null);
		if (accessConstraintsStr != null) {
			accessConstraints.add(accessConstraintsStr);
		}

		return new ServiceIdentification(name, titles, abstracts, keywords, null, null, null, fees, accessConstraints);
	}

	@Override
	public ServiceProvider parseServiceProvider() throws XMLParsingException {
		String onlineResource = getNodeAsString(rootElement, new XPath("wfs:Service/wfs:OnlineResource", nsContext),
				"null");
		return new ServiceProvider(null, onlineResource, null);
	}

	@Override
	public OperationsMetadata parseOperationsMetadata() throws XMLParsingException {
		return null;
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

		// <xsd:element ref="wfs:Title" minOccurs="0"/>
		List<LanguageString> titles = Collections.emptyList();
		String title = getNodeAsString(ftEl, new XPath("wfs:Title", nsContext), null);
		if (title != null) {
			titles = Collections.singletonList(new LanguageString(title, null));
		}

		// <xsd:element ref="wfs:Abstract" minOccurs="0"/>
		List<LanguageString> abstracts = Collections.emptyList();
		String ftAbstract = getNodeAsString(ftEl, new XPath("wfs:Abstract", nsContext), null);
		if (ftAbstract != null) {
			abstracts = Collections.singletonList(new LanguageString(ftAbstract, null));
		}

		// <xsd:element ref="wfs:Keywords" minOccurs="0"/>
		List<Object> keywords = null;
		// TODO
		// String keyword = getNodeAsString( ftEl, new XPath( "wfs:Keywords", nsContext ),
		// null );

		// <xsd:element ref="wfs:SRS"/>
		String srs = getNodeAsString(ftEl, new XPath("wfs:SRS", nsContext), null);
		CRSRef defaultCrs = CRSManager.getCRSRef(srs);

		// <xsd:element name="Operations" type="wfs:OperationsType" minOccurs="0"/>
		// TODO

		// <xsd:element name="LatLongBoundingBox" type="wfs:LatLongBoundingBoxType"
		// minOccurs="0"
		// maxOccurs="unbounded"/>
		List<OMElement> bboxEls = getElements(ftEl, new XPath("wfs:LatLongBoundingBox", nsContext));
		List<Envelope> wgs84BBoxes = new ArrayList<Envelope>(bboxEls.size());
		for (OMElement bboxEl : bboxEls) {
			double minX = getRequiredNodeAsDouble(bboxEl, new XPath("@minx"));
			double minY = getRequiredNodeAsDouble(bboxEl, new XPath("@miny"));
			double maxX = getRequiredNodeAsDouble(bboxEl, new XPath("@maxx"));
			double maxY = getRequiredNodeAsDouble(bboxEl, new XPath("@maxy"));
			CRSRef wgs84 = CRSManager.getCRSRef("EPSG:4326", true);
			wgs84BBoxes.add(new GeometryFactory().createEnvelope(minX, minY, maxX, maxY, wgs84));
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

	@Override
	public List<String> parseLanguages() throws XMLParsingException {
		return null;
	}

}
