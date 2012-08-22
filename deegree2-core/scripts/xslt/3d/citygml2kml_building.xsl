<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:gml="http://www.opengis.net/gml" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template name="BUILDING">
		<xsl:for-each select=".//gml:surfaceMember">
			<Placemark>
				<xsl:apply-templates select="citygml:TexturedSurface"></xsl:apply-templates>
				<xsl:apply-templates select="gml:Polygon | gml:Surface"></xsl:apply-templates>
			</Placemark>
		</xsl:for-each>		
	</xsl:template>
	
</xsl:stylesheet>
