<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
xmlns:citygml="http://www.citygml.org/citygml/1/0/0" 
xmlns:java="java" 
xmlns:citygmlutil="org.deegree.framework.xml.CityGMLUtils"
xmlns="http://earth.google.com/kml/2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

	<xsl:template match="citygml:Material">
		<Style>
			<PolyStyle>
				<color>
					<xsl:value-of select="citygmlutil:getColor( .//citygml:diffusecolor,  .//citygml:transparency )"/>
				</color>
				<outline>0</outline>
			</PolyStyle>
		</Style>
	</xsl:template>

</xsl:stylesheet>
