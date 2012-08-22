<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="citygml:building">
		<xsl:for-each select="../citygml:linkedGeometry/citygml:LINK_FEAT_GEOM/citygml:geometry/citygml:Geometry//citygml:Polygon">
			<Placemark>
				<xsl:if test="citygml:material = false and citygml:texture = false">
					<styleUrl>#defaultPolygonStyle</styleUrl>
				</xsl:if>
				<xsl:apply-templates select="citygml:material/citygml:Material"/>
				<xsl:apply-templates select=".//gml:Polygon | .//gml:Surface"></xsl:apply-templates>
				<!--
				<xsl:apply-templates select="../citygml:linkedFeature/citygml:LINK_FEAT_FEAT/citygml:feature/citygml:Feature"/>
				-->
			</Placemark>
		</xsl:for-each>		
	</xsl:template>
	
</xsl:stylesheet>
