<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:app="http://www.deegree.org/app"
		xmlns:dog="http://www.deegree.org/dog"
		xmlns:gco="http://www.isotc211.org/2005/gco"
		xmlns:gmd="http://www.isotc211.org/2005/gmd"
		xmlns:gml="http://www.opengis.net/gml"
		xmlns:iso19112="http://www.opengis.net/iso19112"
		xmlns:wfs="http://www.opengis.net/wfs"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"		
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes" />
	<!--xsl:output method="xml" version="1.0" encoding="iso-8859-1" indent="yes"/-->
	<!--xsl:namespace-alias stylesheet-prefix="deegree" result-prefix="dog"/-->
	
	<xsl:include href="outfilter_basictypes.xsl"/>
    <xsl:include href="outfilter_counties.xml"/>
    <xsl:include href="outfilter_municipalities.xml"/>
	
	<xsl:template match="wfs:FeatureCollection">
		<wfs:FeatureCollection>
			<xsl:choose>
				<xsl:when test="boolean( ./@numberOfFeatures )">
					<xsl:attribute name="numberOfFeatures">
						<xsl:value-of select="./@numberOfFeatures"></xsl:value-of>
					</xsl:attribute>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="numberOfFeatures">
						<xsl:value-of select="count( gml:featureMember )"></xsl:value-of>
					</xsl:attribute>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:if test="boolean( ./@timeStamp )">
				<xsl:attribute name="timeStamp">
					<xsl:value-of select="./@timeStamp"></xsl:value-of>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="boolean( ./@lockId )">
				<xsl:attribute name="lockId">
					<xsl:value-of select="./@lockId"></xsl:value-of>
				</xsl:attribute>
			</xsl:if>
			<xsl:copy-of select="gml:boundedBy"/>
      
			<xsl:for-each select="gml:featureMember">
				<gml:featureMember>         
                    <xsl:if test="boolean( app:Counties )">
                      <xsl:call-template name="COUNTIES"></xsl:call-template>
                    </xsl:if>
                    <xsl:if test="boolean( app:Municipalities )">
                      <xsl:call-template name="MUNICIPALITIES"></xsl:call-template>
                    </xsl:if>
					<xsl:if test="boolean( app:SI_Gazetteer )">
						<xsl:call-template name="SI_GAZETTEER"></xsl:call-template>
					</xsl:if>
				</gml:featureMember>
			</xsl:for-each>
		</wfs:FeatureCollection>
	</xsl:template>
</xsl:stylesheet>