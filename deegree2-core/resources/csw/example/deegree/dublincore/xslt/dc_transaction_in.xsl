<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://www.purl.org/dc/terms/" xmlns:dcmiBox="http://dublincore.org/documents/2000/07/11/dcmi-box/" xmlns:app="http://www.deegree.org/csw/dc" xmlns:csw="http://www.opengis.net/cat/csw" xmlns:gml="http://www.opengis.net/gml" xmlns:iso19115="http://schemas.opengis.net/iso19115full" xmlns:ogc="http://www.opengis.net/ogc" xmlns:smXML="http://metadata.dgiwg.org/smXML" xmlns:wfs="http://www.opengis.net/wfs" version="1.0">
	<xsl:output encoding="UTF-8" indent="yes" method="xml" version="1.0"/>
	<xsl:include href="dc_constraint.xsl"/>
	<xsl:template match="csw:Transaction">
		<wfs:Transaction xmlns:xlink="http://www.w3.org/1999/xlink" service="WFS" version="1.1.0">
			<xsl:apply-templates/>
		</wfs:Transaction>
	</xsl:template>
	<xsl:template match="csw:Insert">
		<wfs:Insert idgen="GenerateNew">
			<!-- copy handle attribute (if it exists) -->
			<xsl:if test="@handle != ''">
				<xsl:attribute name="handle"><xsl:value-of select="@handle"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="csw:Record">
				<!-- bring properties in correct order (as the WFS depends on it) -->
				<app:Dataset>
					<!-- optional (min=0, max=1): abstract -->
					<xsl:choose>
						<xsl:when test="count(dct:abstract) &lt;= 1">
							<xsl:for-each select="dct:abstract">
								<app:abstract>
									<xsl:value-of select="normalize-space(text())"/>
								</app:abstract>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain zero or one dct:abstract properties.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=unbounded): contributor -->
					<xsl:for-each select="dc:contributor">
						<app:contributor>
							<xsl:value-of select="normalize-space(text())"/>
						</app:contributor>
					</xsl:for-each>
					<!-- mandatory (min=1, max=1): coverage -->
					<xsl:choose>
						<xsl:when test="count(dc:coverage) = 1">
							<app:coverage>
								<xsl:apply-templates select="dc:coverage/dcmiBox:Box"/>
							</app:coverage>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:coverage property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=unbounded): creator -->
					<xsl:for-each select="dc:creator">
						<app:creator>
							<xsl:value-of select="normalize-space(text())"/>
						</app:creator>
					</xsl:for-each>
					<!-- optional (min=0, max=1): date -->
					<xsl:choose>
						<xsl:when test="count(dc:date) &lt;= 1">
							<xsl:for-each select="dc:date">
								<app:date>
									<xsl:value-of select="normalize-space(text())"/>
								</app:date>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain zero or one dc:date properties.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- mandatory (min=1, max=1): description -->
					<xsl:choose>
						<xsl:when test="count(dc:description) = 1">
							<app:description>
								<xsl:value-of select="normalize-space(dc:description/text())"/>
							</app:description>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:description property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- mandatory (min=1, max=1): format -->
					<xsl:choose>
						<xsl:when test="count(dc:format) = 1">
							<app:format>
								<xsl:value-of select="normalize-space(dc:format/text())"/>
							</app:format>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:format property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- mandatory (min=1, max=1): identifier -->
					<xsl:choose>
						<xsl:when test="count(dc:identifier) = 1">
							<app:identifier>
								<xsl:value-of select="normalize-space(dc:identifier/text())"/>
							</app:identifier>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:identifier property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=1): language -->
					<xsl:choose>
						<xsl:when test="count(dc:language) &lt;= 1">
							<xsl:for-each select="dc:language">
								<app:language>
									<xsl:value-of select="normalize-space(text())"/>
								</app:language>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain zero or one dc:language properties.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=1): publisher -->
					<xsl:choose>
						<xsl:when test="count(dc:publisher) &lt;= 1">
							<xsl:for-each select="dc:publisher">
								<app:publisher>
									<xsl:value-of select="normalize-space(text())"/>
								</app:publisher>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain zero or one dc:publisher properties.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=unbounded): relation -->
					<xsl:for-each select="dc:relation">
						<app:relation>
							<xsl:value-of select="normalize-space(text())"/>
						</app:relation>
					</xsl:for-each>					
					<!-- optional (min=0, max=1): rights -->
					<xsl:choose>
						<xsl:when test="count(dc:rights) &lt;= 1">
							<xsl:for-each select="dc:rights">
								<app:rights>
									<xsl:value-of select="normalize-space(text())"/>
								</app:rights>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain zero or one dc:rights properties.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- optional (min=0, max=unbounded): source -->
					<xsl:for-each select="dc:source">
						<app:source>
							<xsl:value-of select="normalize-space(text())"/>
						</app:source>
					</xsl:for-each>
					<!-- optional (min=1, max=unbounded): subject -->
					<xsl:choose>
						<xsl:when test="count(dc:subject) >= 1">
							<xsl:for-each select="dc:subject">
								<app:subject>
									<xsl:value-of select="normalize-space(text())"/>
								</app:subject>
							</xsl:for-each>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain at least one dc:subject property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- mandatory (min=1, max=1): title -->
					<xsl:choose>
						<xsl:when test="count(dc:title) = 1">
							<app:title>
								<xsl:value-of select="normalize-space(dc:title/text())"/>
							</app:title>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:title property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
					<!-- mandatory (min=1, max=1): type -->
					<xsl:choose>
						<xsl:when test="count(dc:type) = 1">
							<app:type>
								<xsl:value-of select="normalize-space(dc:type/text())"/>
							</app:type>
						</xsl:when>
						<xsl:otherwise>
							<xsl:message terminate="yes">A valid record must contain exactly one dc:type property.</xsl:message>
						</xsl:otherwise>
					</xsl:choose>
				</app:Dataset>
			</xsl:for-each>
		</wfs:Insert>
	</xsl:template>
	<xsl:template match="dcmiBox:Box">
		<xsl:variable name="minx">
			<xsl:value-of select="dcmiBox:westlimit"/>
		</xsl:variable>
		<xsl:variable name="maxx">
			<xsl:value-of select="dcmiBox:eastlimit"/>
		</xsl:variable>
		<xsl:variable name="miny">
			<xsl:value-of select="dcmiBox:northlimit"/>
		</xsl:variable>
		<xsl:variable name="maxy">
			<xsl:value-of select="dcmiBox:southlimit"/>
		</xsl:variable>
		<gml:Polygon>
			<xsl:attribute name="srsName"><xsl:value-of select="@projection"/></xsl:attribute>
			<gml:outerBoundaryIs>
				<gml:LinearRing>
					<gml:coordinates cs="," decimal="." ts=" ">
						<xsl:value-of select="concat( $minx, ',', $miny, ' ', $minx, ',', $maxy, ' ', $maxx, ',', $maxy, ' ', $maxx, ',', $miny, ' ',$minx, ',', $miny)"/>
					</gml:coordinates>
				</gml:LinearRing>
			</gml:outerBoundaryIs>
		</gml:Polygon>
	</xsl:template>
	<xsl:template match="csw:Update">
		<wfs:Update>
			<xsl:attribute name="typeName">app:Dataset</xsl:attribute>
			<!-- copy handle attribute (if it exists) -->
			<xsl:if test="@handle != ''">
				<xsl:attribute name="handle"><xsl:value-of select="@handle"/></xsl:attribute>
			</xsl:if>
			<xsl:for-each select="csw:RecordProperty">
				<wfs:Property>
					<wfs:Name>
						<xsl:call-template name="convertToWFSPropertyName">
							<xsl:with-param name="CSW_PROPERTY">
								<xsl:value-of select="csw:Name/text()"/>
							</xsl:with-param>
						</xsl:call-template>
					</wfs:Name>
					<wfs:Value>
						<xsl:choose>
							<xsl:when test="csw:Name/text() = 'dc:coverage'">
								<xsl:apply-templates select="csw:Value/dcmiBox:Box"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="csw:Value/text()"/>
							</xsl:otherwise>
						</xsl:choose>
					</wfs:Value>
				</wfs:Property>
			</xsl:for-each>
			<!-- mandatory (min=1, max=1): csw:Constraint -->
			<xsl:choose>
				<xsl:when test="count(csw:Constraint)  = 1">
					<xsl:apply-templates select="csw:Constraint"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">A valid csw:Update operation must contain exactly one csw:Constraint element.</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Update>
	</xsl:template>
	<xsl:template match="csw:Delete">
		<wfs:Delete>
			<xsl:attribute name="typeName">app:Dataset</xsl:attribute>
			<!-- copy handle attribute (if it exists) -->
			<xsl:if test="@handle != ''">
				<xsl:attribute name="handle"><xsl:value-of select="@handle"/></xsl:attribute>
			</xsl:if>
			<!-- mandatory (min=1, max=1): csw:Constraint -->
			<xsl:choose>
				<xsl:when test="count(csw:Constraint)  = 1">
					<xsl:apply-templates select="csw:Constraint"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:message terminate="yes">A valid csw:Delete operation must contain exactly one csw:Constraint element.</xsl:message>
				</xsl:otherwise>
			</xsl:choose>
		</wfs:Delete>
	</xsl:template>
</xsl:stylesheet>
