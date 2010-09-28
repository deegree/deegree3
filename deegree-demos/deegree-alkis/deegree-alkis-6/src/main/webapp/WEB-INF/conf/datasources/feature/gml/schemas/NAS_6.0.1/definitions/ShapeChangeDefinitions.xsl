<?xml version="1.0" encoding="iso-8859-1"?>
<!-- (c) 2007 interactive instruments GmbH -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:def="http://www.interactive-instruments.de/ShapeChange/Definitions/0.5" xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:output method="html"/>
	<xsl:include href="ShapeChangeDefinitionsUtility.xsl"/>
	<xsl:template match="/">
		<html>
			<head>
				<title>Definitions for application schema "<xsl:value-of select="/def:ApplicationSchemaDefinition/gml:name"/>"</title>
			</head>
			<body>
				<h1>Definitions for application schema "<xsl:value-of select="/def:ApplicationSchemaDefinition/gml:name"/>"</h1>
				<xsl:if test="/def:ApplicationSchemaDefinition/gml:description">
					<h2>Documentation</h2>
					<p>
						<xsl:value-of select="/def:ApplicationSchemaDefinition/gml:description"/>
					</p>
				</xsl:if>
				<a>
					<xsl:attribute name="name">overview</xsl:attribute>
					<h2>List of types</h2>
				</a>
				<table cellspacing="10">
					<thead>
						<tr>
							<td align="left">
								<u>
									<b>feature/object types</b>
								</u>
							</td>
							<td align="left">
								<u>
									<b>data types</b>
								</u>
							</td>
						</tr>
					</thead>
					<tbody>
						<tr valign="top">
							<td width="50%">
								<xsl:for-each select="//def:TypeDefinition[def:classification='featureType' or def:classification='objectType' or def:classification='mixinType']">
									<xsl:sort select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
									<xsl:variable name="type" select="."/>
									<a>
										<xsl:attribute name="href">#<xsl:value-of select="$type/@gml:id"/></xsl:attribute>
										<xsl:value-of select="$type/gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
									</a>
									<br/>
								</xsl:for-each>
								<xsl:for-each select="//gml:dictionaryEntry[contains(@xlink:href,':featureType:') or contains(@xlink:href,':objectType:') or contains(@xlink:href,':mixinType:')]">
									<xsl:sort select="substring-after(substring-after(substring-after(@xlink:href,'::'),':'),':')"/>
									<xsl:variable name="type" select="substring-after(substring-after(substring-after(@xlink:href,'::'),':'),':')"/>
									<a>
										<xsl:attribute name="href"><xsl:value-of select="$type"/>.definitions.xml</xsl:attribute>
										<xsl:value-of select="$type"/>
									</a>
									<br/>
								</xsl:for-each>
							</td>
							<td width="50%">
								<xsl:for-each select="//def:TypeDefinition[def:classification='dataType' or def:classification='unionType' or def:classification='basicType']">
									<xsl:sort select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
									<xsl:variable name="type" select="."/>
									<a>
										<xsl:attribute name="href">#<xsl:value-of select="$type/@gml:id"/></xsl:attribute>
										<xsl:value-of select="$type/gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
									</a>
									<br/>
								</xsl:for-each>
								<xsl:for-each select="//gml:dictionaryEntry[contains(@xlink:href,':dataType:') or contains(@xlink:href,':unionType:') or contains(@xlink:href,':basicType:')]">
									<xsl:sort select="substring-after(substring-after(substring-after(@xlink:href,'::'),':'),':')"/>
									<xsl:variable name="type" select="substring-after(substring-after(substring-after(@xlink:href,'::'),':'),':')"/>
									<a>
										<xsl:attribute name="href"><xsl:value-of select="$type"/>.definitions.xml</xsl:attribute>
										<xsl:value-of select="$type"/>
									</a>
									<br/>
								</xsl:for-each>
							</td>
						</tr>
					</tbody>
				</table>
				<xsl:if test="//def:TypeDefinition">
					<h2>Types</h2>
					<xsl:for-each select="//def:TypeDefinition">
						<xsl:sort select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
						<xsl:apply-templates select="." mode="header"/>
						<xsl:apply-templates select="." mode="body"/>
						<xsl:for-each select="gml:dictionaryEntry/def:PropertyDefinition">
							<xsl:apply-templates select="." mode="header"/>
							<xsl:apply-templates select="." mode="body"/>
						</xsl:for-each>
					</xsl:for-each>
				</xsl:if>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
