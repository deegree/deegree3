<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:gml="http://www.opengis.net/gml" xmlns:citygml="http://www.citygml.org/citygml/1/0/0" >

	<xsl:template name="CITYFURNITURE">
		<citygml:cityFurniture>
			<citygml:CityFurniture>		
				<xsl:call-template name="ADDGMLID"/>
				<citygml:dummyValue>1</citygml:dummyValue>
			</citygml:CityFurniture> 
		</citygml:cityFurniture>
		<xsl:call-template name="FEATURE"></xsl:call-template>
	</xsl:template>
			
	<xsl:template match="citygml:boundedBy/*">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="local-name( . )"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
