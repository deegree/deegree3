<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:wfs="http://www.opengis.net/wfs" xmlns:app="http://www.deegree.org/csw/dc" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:java="java" xmlns:minmax="org.deegree.framework.xml.MinMaxExtractor">
	<xsl:param name="REQUEST_ID"/>
	<xsl:param name="SEARCH_STATUS"/>
	<xsl:param name="TIMESTAMP"/>
	<xsl:param name="ELEMENT_SET"/>
	<xsl:param name="RECORD_SCHEMA"/>
	<xsl:param name="RECORDS_MATCHED"/>
	<xsl:param name="RECORDS_RETURNED"/>
	<xsl:param name="NEXT_RECORD"/>
	<xsl:param name="RESULT_TYPE"/>
	<xsl:template match="wfs:FeatureCollection">
		<csw:GetRecordsResponse xmlns:csw="http://www.opengis.net/cat/csw">
			<xsl:attribute name="xsi:schemaLocation">http://www.opengis.net/cat/csw http://schemas.opengis.net/csw/2.0.0/CSW-discovery.xsd</xsl:attribute>
			<csw:RequestId>
				<xsl:value-of select="$REQUEST_ID"/>
			</csw:RequestId>
			<csw:SearchStatus>
				<xsl:attribute name="status"><xsl:value-of select="$SEARCH_STATUS"/></xsl:attribute>
				<xsl:attribute name="timestamp"><xsl:value-of select="$TIMESTAMP"/></xsl:attribute>
			</csw:SearchStatus>
			<csw:SearchResults>
				<xsl:if test="$ELEMENT_SET = 'summary' or $ELEMENT_SET = 'brief' or $ELEMENT_SET = 'full' or $ELEMENT_SET = 'hits'">
					<xsl:attribute name="elementSet"><xsl:value-of select="$ELEMENT_SET"/></xsl:attribute>
				</xsl:if>
				<xsl:attribute name="recordSchema"><xsl:value-of select="$RECORD_SCHEMA"/></xsl:attribute>
				<xsl:attribute name="numberOfRecordsMatched"><xsl:value-of select="$RECORDS_MATCHED"/></xsl:attribute>
				<xsl:attribute name="numberOfRecordsReturned"><xsl:value-of select="$RECORDS_RETURNED"/></xsl:attribute>
				<xsl:attribute name="nextRecord"><xsl:value-of select="$NEXT_RECORD"/></xsl:attribute>
				<xsl:if test="$RESULT_TYPE != 'HITS'">
					<xsl:for-each select="gml:featureMember/app:Dataset">
						<csw:Record>
							<xsl:choose>
								<xsl:when test="$ELEMENT_SET = 'brief'">
									<xsl:apply-templates select="app:identifier | app:title | app:subject"/>
								</xsl:when>
								<xsl:when test="$ELEMENT_SET = 'summary'">
									<xsl:apply-templates select="app:identifier | app:title | app:subject | description"/>
								</xsl:when>
								<xsl:otherwise>
									<xsl:apply-templates select="app:*"/>
								</xsl:otherwise>
							</xsl:choose>
						</csw:Record>
					</xsl:for-each>
				</xsl:if>
			</csw:SearchResults>
		</csw:GetRecordsResponse>
	</xsl:template>
	<!-- map feature properties to dublincore properties -->
	<xsl:template match="app:abstract">
		<dct:abstract>
			<xsl:value-of select="."/>
		</dct:abstract>
	</xsl:template>
	<xsl:template match="app:contributor">
		<dc:contributor>
			<xsl:value-of select="."/>
		</dc:contributor>
	</xsl:template>
	<xsl:template match="app:coverage">
		<dc:coverage>
			<dcmiBox:Box name="Geographic">
				<xsl:attribute name="projection"><xsl:value-of select="gml:Polygon/@srsName"/></xsl:attribute>
				<xsl:variable name="xmax">
					<xsl:value-of select="minmax:getXMax(./child::*[1])"/>
				</xsl:variable>
				<xsl:variable name="xmin">
					<xsl:value-of select="minmax:getXMin(./child::*[1])"/>
				</xsl:variable>
				<xsl:variable name="ymax">
					<xsl:value-of select="minmax:getYMax(./child::*[1])"/>
				</xsl:variable>
				<xsl:variable name="ymin">
					<xsl:value-of select="minmax:getYMin(./child::*[1])"/>
				</xsl:variable>
				<dcmiBox:northlimit units="decimal degrees">
					<xsl:value-of select="$ymax"/>
				</dcmiBox:northlimit>
				<dcmiBox:eastlimit units="decimal degrees">
					<xsl:value-of select="$xmax"/>
				</dcmiBox:eastlimit>
				<dcmiBox:southlimit units="decimal degrees">
					<xsl:value-of select="$ymin"/>
				</dcmiBox:southlimit>
				<dcmiBox:westlimit units="decimal degrees">
					<xsl:value-of select="$xmin"/>
				</dcmiBox:westlimit>
			</dcmiBox:Box>
		</dc:coverage>
	</xsl:template>
	<xsl:template match="app:creator">
		<dc:creator>
			<xsl:value-of select="."/>
		</dc:creator>
	</xsl:template>
	<xsl:template match="app:date">
		<dc:date>
			<xsl:value-of select="."/>
		</dc:date>
	</xsl:template>
	<xsl:template match="app:description">
		<dc:description>
			<xsl:value-of select="."/>
		</dc:description>
	</xsl:template>
	<xsl:template match="app:format">
		<dc:format>
			<xsl:value-of select="."/>
		</dc:format>
	</xsl:template>
	<xsl:template match="app:identifier">
		<dc:identifier>
			<xsl:value-of select="."/>
		</dc:identifier>
	</xsl:template>
	<xsl:template match="app:language">
		<dc:language>
			<xsl:value-of select="."/>
		</dc:language>
	</xsl:template>
	<xsl:template match="app:publisher">
		<dc:publisher>
			<xsl:value-of select="."/>
		</dc:publisher>
	</xsl:template>
	<xsl:template match="app:relation">
		<dc:relation>
			<xsl:value-of select="."/>
		</dc:relation>
	</xsl:template>
	<xsl:template match="app:rights">
		<dc:rights>
			<xsl:value-of select="."/>
		</dc:rights>
	</xsl:template>
	<xsl:template match="app:source">
		<dc:source>
			<xsl:value-of select="."/>
		</dc:source>
	</xsl:template>
	<xsl:template match="app:subject">
		<dc:subject>
			<xsl:value-of select="."/>
		</dc:subject>
	</xsl:template>
	<xsl:template match="app:title">
		<dc:title>
			<xsl:value-of select="."/>
		</dc:title>
	</xsl:template>
	<xsl:template match="app:type">
		<dc:type>
			<xsl:value-of select="."/>
		</dc:type>
	</xsl:template>
</xsl:stylesheet>
