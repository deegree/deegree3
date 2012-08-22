<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" xmlns:app="http://www.deegree.org/app" xmlns:dog="http://www.deegree.org/dog" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:gml="http://www.opengis.net/gml" xmlns:iso19112="http://www.opengis.net/iso19112" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:java="java" xmlns:mapping="org.deegree.enterprise.servlet.WFSRequestMapping" exclude-result-prefixes="java mapping dog">
	<xsl:param name="NSP">a:a</xsl:param>
	<xsl:template match="/">
		<xsl:apply-templates select="wfs:GetFeature"/>
	</xsl:template>
	<xsl:template match="wfs:GetFeature">
		<wfs:GetFeature>
			<xsl:copy-of select="./@*"/>
			<xsl:apply-templates select="wfs:Query"/>
		</wfs:GetFeature>
	</xsl:template>
	<xsl:template match="wfs:Query">
		<xsl:variable name="TN">
			<xsl:value-of select="substring-after( @typeName, ':' )"/>
		</xsl:variable>
		<wfs:Query>
			<xsl:copy-of select="./@srsName"/>
			<xsl:attribute name="typeName"><xsl:value-of select="concat( 'app:', $TN )"/></xsl:attribute>
			<xsl:apply-templates select="wfs:PropertyName"/>
			<xsl:apply-templates select="ogc:Filter"/>
			<xsl:apply-templates select="ogc:SortBy"/>
		</wfs:Query>
	</xsl:template>
	<xsl:template match="ogc:Filter">
		<ogc:Filter>
			<xsl:choose>
				<xsl:when test="count( ogc:FeatureId ) = 1 or count( ogc:GmlObjectId ) = 1">
					<xsl:apply-templates select="ogc:FeatureId | ogc:GmlObjectId"/>
				</xsl:when>
				<xsl:when test="count( ogc:FeatureId ) > 1 or count( ogc:GmlObjectId ) > 1">
					<ogc:Or>
						<xsl:apply-templates select="ogc:FeatureId | ogc:GmlObjectId"/>
					</ogc:Or>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="node()|@*"/>
				</xsl:otherwise>
			</xsl:choose>
		</ogc:Filter>
	</xsl:template>
	<xsl:template match="SortBy">
		<ogc:SortBy>
			<xsl:apply-templates select="ogc:SortProperty"/>
		</ogc:SortBy>
	</xsl:template>
	<xsl:template match="ogc:SortProperty">
		<ogc:SortProperty>
			<xsl:for-each select="ogc:PropertyName">
				<ogc:PropertyName>
					<xsl:apply-templates select="node()|@*"/>
				</ogc:PropertyName>
			</xsl:for-each>
			<xsl:copy-of select="ogc:SortOrder"/>
		</ogc:SortProperty>
	</xsl:template>
	<xsl:template match="node()|@*">
		<xsl:variable name="TMP">
			<xsl:value-of select="local-name(.) "/>
		</xsl:variable>
		<xsl:copy>
			<xsl:choose>
				<xsl:when test="$TMP = 'PropertyName' ">
					<xsl:if test="local-name(.) = 'PropertyName'">
						<xsl:value-of select="mapping:mapPropertyValue( ., //@typeName, $NSP )"/>
					</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates select="node()|@*"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
</xsl:stylesheet>
