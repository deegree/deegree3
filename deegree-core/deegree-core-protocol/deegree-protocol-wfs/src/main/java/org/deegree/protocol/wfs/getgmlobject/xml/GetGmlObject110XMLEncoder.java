package org.deegree.protocol.wfs.getgmlobject.xml;

import static org.deegree.protocol.wfs.WFSConstants.WFS_PREFIX;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.cs.exceptions.TransformationException;
import org.deegree.cs.exceptions.UnknownCRSException;
import org.deegree.protocol.wfs.WFSConstants;
import org.deegree.protocol.wfs.getgmlobject.GetGmlObject;

/**
 * Encodes {@link GetGmlObject} objects according to WFS specification 1.1.0.
 *
 * @author <a href="mailto:Tschirner@bafg.de">Sven Tschirner</a>
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 */
public class GetGmlObject110XMLEncoder {

	/**
	 * Serializes a {@link GetGmlObject}-object for WFS 1.1.0 GetGmlObject-requests
	 * @param getGmlObject the {@link GetGmlObject}-object to be serialized, must not be
	 * <code>null</code>
	 * @param writer target of the xml stream, must not be <code>null</code>
	 * @throws XMLStreamException
	 * @throws UnknownCRSException
	 * @throws TransformationException
	 */
	public static void export(GetGmlObject getGmlObject, XMLStreamWriter writer)
			throws XMLStreamException, UnknownCRSException, TransformationException {

		writer.writeStartDocument();
		writer.writeStartElement(WFS_PREFIX, "GetGmlObject", WFSConstants.WFS_NS);
		writer.writeNamespace(WFSConstants.WFS_PREFIX, WFSConstants.WFS_NS);
		writer.writeNamespace(CommonNamespaces.OGC_PREFIX, CommonNamespaces.OGCNS);
		writer.writeNamespace(CommonNamespaces.GML_PREFIX, CommonNamespaces.GMLNS);
		writer.writeNamespace(CommonNamespaces.XSI_PREFIX, CommonNamespaces.XSINS);
		writer.writeAttribute(CommonNamespaces.XSINS, "schemaLocation",
				"http://www.opengis.net/wfs http://schemas.opengis.net/wfs/1.1.0/wfs.xsd");

		/* write <GetFeature>-element attributes */

		Version version = getGmlObject.getVersion();
		if ((version != null)) {
			if (version.compareTo(new Version(1, 1, 0)) != 0) {
				throw new IllegalArgumentException(
						"Only WFS-GetGmlObject 1.1.0 serialization is supported by this encoder");
			}
			writer.writeAttribute("version", version.toString());
		}

		String handle = getGmlObject.getHandle();
		if ((handle != null) && (!handle.equals(""))) {
			writer.writeAttribute("handle", handle);
		}

		String outputFormat = getGmlObject.getOutputFormat();
		if ((outputFormat != null) && (!outputFormat.equals(""))) {
			writer.writeAttribute("outputFormat", outputFormat);
		}

		String traverseXlinkDepth = getGmlObject.getTraverseXlinkDepth();
		if ((traverseXlinkDepth != null) && (!traverseXlinkDepth.equals(""))) {
			writer.writeAttribute("traverseXlinkDepth", traverseXlinkDepth);
		}
		else { /* otherwise set this mandatory attribute to value '*' */
			writer.writeAttribute("traverseXlinkDepth", "*");
		}

		Integer traverseXlinkExpiry = getGmlObject.getTraverseXlinkExpiry();
		if (traverseXlinkExpiry != null) {
			writer.writeAttribute("traverseXlinkExpiry", traverseXlinkExpiry.toString());
		}

		/* write <GmlObjectId> child elements */
		String requestedId = getGmlObject.getRequestedId();
		if (requestedId != null) {
			writer.writeStartElement(CommonNamespaces.OGC_PREFIX, "GmlObjectId", CommonNamespaces.OGCNS);
			writer.writeAttribute(CommonNamespaces.GMLNS, "id", requestedId);
			writer.writeEndElement();
		}
		else {
			throw new IllegalArgumentException("Exactly one local or external identifier has to be declared!");
		}
	}

}