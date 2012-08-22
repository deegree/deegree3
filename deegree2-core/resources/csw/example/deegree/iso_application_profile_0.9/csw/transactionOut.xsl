<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:wfs="http://www.opengis.net/wfs" xmlns:ogc="http://www.opengis.net/ogc" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format"
 xmlns:csw="http://www.opengis.net/cat/csw" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:gml="http://www.opengis.net/gml">
	<xsl:template match="wfs:TransactionResponse">
		<csw:TransactionResponse>
			<xsl:if test="./@version">
				<xsl:attribute name="version"><xsl:value-of select="./@version"/></xsl:attribute>
			</xsl:if>
			<xsl:apply-templates select="wfs:TransactionSummary"/>
			<xsl:apply-templates select="wfs:InsertResults"/>
		</csw:TransactionResponse>
	</xsl:template>
	<xsl:template match="wfs:TransactionSummary">
		<csw:TransactionSummary>
			<xsl:if test="./@requestId">
				<xsl:attribute name="requestId"><xsl:value-of select="./@requestId"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="wfs:totalInserted">
				<csw:totalInserted>
					<xsl:value-of select="wfs:totalInserted"/>
				</csw:totalInserted>
			</xsl:if>
			<xsl:if test="wfs:totalUpdated">
				<csw:totalUpdated>
					<xsl:value-of select="wfs:totalUpdated"/>
				</csw:totalUpdated>
			</xsl:if>
			<xsl:if test="wfs:totalDeleted">
				<csw:totalDeleted>
					<xsl:value-of select="wfs:totalDeleted"/>
				</csw:totalDeleted>
			</xsl:if>
		</csw:TransactionSummary>	
	</xsl:template>
	<xsl:template match="wfs:InsertResults">
		<csw:InsertResult>
			<xsl:if test="./@handleRef">
				<xsl:attribute name="handleRef"><xsl:value-of select="./@handleRef"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="wfs:Feature /ogc:FeatureId">
				<csw:BriefRecord>
					<dc:identifier>
						<xsl:if test="./@fid">
							<xsl:value-of select="./@fid"/>
						</xsl:if>
						<xsl:if test="./@gml:id">
							<xsl:value-of select="./@gml:id"/>
						</xsl:if>
					</dc:identifier>
				</csw:BriefRecord>
			</xsl:for-each>
		</csw:InsertResult>		
	</xsl:template>
</xsl:stylesheet>
