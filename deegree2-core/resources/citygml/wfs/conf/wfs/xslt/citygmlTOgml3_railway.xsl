<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:gml="http://www.opengis.net/gml" xmlns:citygml="http://www.citygml.org/citygml/1/0/0" >

	<xsl:template name="RAILWAY">
		<citygml:railway>
			<citygml:Railway>		
				<xsl:call-template name="ADDGMLID"/>
				<citygml:dummyValue>1</citygml:dummyValue>
			</citygml:Railway> 
		</citygml:railway>
		<xsl:call-template name="FEATURE"></xsl:call-template>
	</xsl:template>
			
	<xsl:template match="citygml:boundedBy/*">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="local-name( . )"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="citygml:TrafficArea">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="'tracfficArea'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="citygml:AuxiliaryTrafficArea">
		<xsl:call-template name="LINK_FEAT">
			<xsl:with-param name="ROLE" select="'auxiliaryTracfficArea'"></xsl:with-param>
		</xsl:call-template>
	</xsl:template>
	
</xsl:stylesheet>
