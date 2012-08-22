<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:java="java" 
xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils"
xmlns:citygmlutil="org.deegree.framework.xml.CityGMLUtils"
xmlns:wfs="http://www.opengis.net/wfs" 
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:param name="SRCCRS">EPSG:31466</xsl:param>
	<xsl:param name="TARGETCRS">EPSG:4326</xsl:param>
	
	<xsl:template match="gml:Polygon | gml:Surface">		
		<Polygon>
			<altitudeMode>absolute</altitudeMode>
			<outerBoundaryIs>
				<LinearRing>
					<coordinates xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils">
						<xsl:value-of select="geometryutil:getPolygonOuterRing( . , $SRCCRS, $TARGETCRS )"></xsl:value-of>
					</coordinates>
				</LinearRing>
			</outerBoundaryIs>
			<xsl:for-each select=".//gml:interior | .//gml:innerBoundaryIs">
				<innerBoundaryIs>
					<LinearRing>
						<coordinates xmlns:java="java" xmlns:geometryutil="org.deegree.framework.xml.GeometryUtils">
							<xsl:value-of select="geometryutil:getPolygonInnerRing( .. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
							<xsl:value-of select="geometryutil:getPolygonInnerRing( ../.. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
							<xsl:value-of select="geometryutil:getPolygonInnerRing( ../../.. , position(), $SRCCRS, $TARGETCRS )"></xsl:value-of>
						</coordinates>
					</LinearRing>
				</innerBoundaryIs>
			</xsl:for-each>
		</Polygon>
	</xsl:template>

</xsl:stylesheet>
