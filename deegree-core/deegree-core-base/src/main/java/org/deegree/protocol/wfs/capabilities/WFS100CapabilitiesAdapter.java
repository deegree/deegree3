package org.deegree.protocol.wfs.capabilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.deegree.commons.tom.ows.LanguageString;
import org.deegree.commons.xml.NamespaceBindings;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.XMLParsingException;
import org.deegree.commons.xml.XPath;
import org.deegree.cs.persistence.CRSManager;
import org.deegree.cs.refs.coordinatesystem.CRSRef;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.GeometryFactory;
import org.deegree.protocol.ows.metadata.OperationsMetadata;
import org.deegree.protocol.ows.metadata.ServiceIdentification;
import org.deegree.protocol.ows.metadata.ServiceProvider;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.metadata.WFSFeatureType;

/**
 * {@link WFSCapabilitiesAdapter} for documents that comply to the <a
 * href="http://www.opengeospatial.org/standards/wfs>WFS 1.0.0</a> specification.
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author$
 * 
 * @version $Revision$, $Date$
 */
public class WFS100CapabilitiesAdapter extends XMLAdapter implements WFSCapabilitiesAdapter {

    private static final NamespaceBindings nsContext = new NamespaceBindings();

    static {
        nsContext.addNamespace( "wfs", WFSConstants.WFS_NS );
    }

    @Override
    public ServiceIdentification parseServiceIdentification()
                            throws XMLParsingException {
        return null;
    }

    @Override
    public ServiceProvider parseServiceProvider()
                            throws XMLParsingException {
        return null;
    }

    @Override
    public OperationsMetadata parseOperationsMetadata()
                            throws XMLParsingException {
        return null;
    }

    @Override
    public List<WFSFeatureType> parseFeatureTypeList() {
        List<OMElement> ftEls = getElements( rootElement, new XPath( "wfs:FeatureTypeList/wfs:FeatureType", nsContext ) );
        List<WFSFeatureType> fts = new ArrayList<WFSFeatureType>( ftEls.size() );
        for ( OMElement ftEl : ftEls ) {
            fts.add( parseFeatureType( ftEl ) );
        }
        return fts;
    }

    private WFSFeatureType parseFeatureType( OMElement ftEl ) {

        // <xsd:element name="Name" type="xsd:QName"/>
        OMElement nameEl = getRequiredElement( ftEl, new XPath( "wfs:Name", nsContext ) );
        String prefixedName = nameEl.getText().trim();
        QName ftName = parseQName( prefixedName, nameEl );

        // <xsd:element ref="wfs:Title" minOccurs="0"/>
        List<LanguageString> titles = Collections.emptyList();
        String title = getNodeAsString( ftEl, new XPath( "wfs:Title", nsContext ), null );
        if ( title != null ) {
            titles = Collections.singletonList( new LanguageString( title, null ) );
        }

        // <xsd:element ref="wfs:Abstract" minOccurs="0"/>
        List<LanguageString> abstracts = Collections.emptyList();
        String ftAbstract = getNodeAsString( ftEl, new XPath( "wfs:Abstract", nsContext ), null );
        if ( ftAbstract != null ) {
            abstracts = Collections.singletonList( new LanguageString( ftAbstract, null ) );
        }

        // <xsd:element ref="wfs:Keywords" minOccurs="0"/>
        List<Object> keywords = null;
        // TODO
        // String keyword = getNodeAsString( ftEl, new XPath( "wfs:Keywords", nsContext ), null );

        // <xsd:element ref="wfs:SRS"/>
        String srs = getNodeAsString( ftEl, new XPath( "wfs:SRS", nsContext ), null );
        CRSRef defaultCrs = CRSManager.getCRSRef( srs );

        // <xsd:element name="Operations" type="wfs:OperationsType" minOccurs="0"/>
        // TODO

        // <xsd:element name="LatLongBoundingBox" type="wfs:LatLongBoundingBoxType" minOccurs="0"
        // maxOccurs="unbounded"/>
        List<OMElement> bboxEls = getElements( ftEl, new XPath( "wfs:LatLongBoundingBox", nsContext ) );
        List<Envelope> wgs84BBoxes = new ArrayList<Envelope>( bboxEls.size() );
        for ( OMElement bboxEl : bboxEls ) {
            double minX = getRequiredNodeAsDouble( bboxEl, new XPath( "@minx" ) );
            double minY = getRequiredNodeAsDouble( bboxEl, new XPath( "@miny" ) );
            double maxX = getRequiredNodeAsDouble( bboxEl, new XPath( "@maxx" ) );
            double maxY = getRequiredNodeAsDouble( bboxEl, new XPath( "@maxy" ) );
            CRSRef wgs84 = CRSManager.getCRSRef( "EPSG:4326", true );
            wgs84BBoxes.add( new GeometryFactory().createEnvelope( minX, minY, maxX, maxY, wgs84 ) );
        }

        // <xsd:element name="MetadataURL" type="wfs:MetadataURLType" minOccurs="0" maxOccurs="unbounded"/>
        // TODO
        List<Object> mdReferences = null;

        return new WFSFeatureType( ftName, titles, abstracts, null, keywords, defaultCrs, null, wgs84BBoxes,
                                   mdReferences, null );
    }

    @Override
    public Object parseFilterCapabilities() {
        return null;
    }
}
