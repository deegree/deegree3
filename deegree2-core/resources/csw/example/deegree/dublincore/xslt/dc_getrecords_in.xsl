<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:wfs="http://www.opengis.net/wfs" xmlns:app="http://www.deegree.org/csw/dc">
	<xsl:output encoding="UTF-8" indent="yes" method="xml" version="1.0"/>
	<xsl:include href="dc_constraint.xsl"/>
	<xsl:template match="csw:GetRecords">
		<wfs:GetFeature outputFormat="text/xml; subtype=gml/3.1.1" xmlns:gml="http://www.opengis.net/gml">
			<xsl:if test="@maxRecords != '' ">
				<xsl:attribute name="maxFeatures"><xsl:value-of select="@maxRecords"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@startPosition != '' ">
				<xsl:attribute name="startPosition"><xsl:value-of select="@startPosition"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@resultType = 'hits' or @resultType = 'HITS' ">
				<xsl:attribute name="resultType">hits</xsl:attribute>
			</xsl:if>			
			<xsl:apply-templates select="csw:Query"/>
		</wfs:GetFeature>
		<xsl:apply-templates select="csw:ResponseHandler"/>
	</xsl:template>
	<xsl:template match="csw:Query">
		<wfs:Query>
			<xsl:attribute name="typeName">app:Dataset</xsl:attribute>
			<xsl:choose>
				<xsl:when test="csw:ElementSetName = 'brief' ">
					<wfs:PropertyName>app:identifier</wfs:PropertyName>
					<wfs:PropertyName>app:title</wfs:PropertyName>
					<wfs:PropertyName>app:subject</wfs:PropertyName>
				</xsl:when>
				<xsl:when test="csw:ElementSetName = 'summary' ">
					<wfs:PropertyName>app:identifier</wfs:PropertyName>
					<wfs:PropertyName>app:title</wfs:PropertyName>
					<wfs:PropertyName>app:subject</wfs:PropertyName>
					<wfs:PropertyName>app:description</wfs:PropertyName>
				</xsl:when>
			</xsl:choose>
			<xsl:for-each select="csw:ElementName">
				<wfs:PropertyName>
					<xsl:call-template name="convertToWFSPropertyName">
						<xsl:with-param name="CSW_PROPERTY">
							<xsl:value-of select="text()"/>
						</xsl:with-param>
					</xsl:call-template>
				</wfs:PropertyName>
			</xsl:for-each>
			<xsl:apply-templates select="csw:Constraint"/>
		</wfs:Query>
	</xsl:template>
	<xsl:template match="csw:ResponseHandler"/>
</xsl:stylesheet>
