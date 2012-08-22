<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:wcs="http://www.opengis.net/wcs" xmlns:deegree="http://www.deegree.org/wcs" >

<xsl:output indent="yes"></xsl:output>

<xsl:param name="DRIVER">driver</xsl:param>
<xsl:param name="URL">url</xsl:param>
<xsl:param name="USER">user</xsl:param>
<xsl:param name="PASSWORD">password</xsl:param>
<xsl:param name="TABLE">table</xsl:param>

<xsl:template match="wcs:CoverageDescription">
	<wcs:CoverageDescription>
		<xsl:copy-of select="./@*"></xsl:copy-of>
		<xsl:apply-templates select="wcs:CoverageOffering"></xsl:apply-templates>
	</wcs:CoverageDescription>
</xsl:template>

<xsl:template match="wcs:CoverageOffering">
	<wcs:CoverageOffering>
		<xsl:copy-of select="./@*"></xsl:copy-of>
		<xsl:for-each select="*">
			<xsl:if test="local-name(.) != 'Extension' ">
				<xsl:copy-of select="."></xsl:copy-of>
			</xsl:if>
		</xsl:for-each>
		<xsl:apply-templates select="deegree:Extension"></xsl:apply-templates>
	</wcs:CoverageOffering>
</xsl:template>

<xsl:template match="deegree:Extension">
	<deegree:Extension type="databaseIndexed">
		<deegree:Resolution max="99999999" min="0">
			<deegree:Range>
				<deegree:Name>space</deegree:Name>
			</deegree:Range>
			<deegree:Database>
				<jdbc:JDBCConnection xmlns:jdbc="http://www.deegree.org/jdbc">
					<jdbc:Driver>
						<xsl:value-of select="$DRIVER"></xsl:value-of>
					</jdbc:Driver>
					<jdbc:Url>
						<xsl:value-of select="$URL"></xsl:value-of>					
					</jdbc:Url>
					<jdbc:User>
						<xsl:value-of select="$USER"></xsl:value-of>					
					</jdbc:User>
					<jdbc:Password>
						<xsl:value-of select="$PASSWORD"></xsl:value-of>
					</jdbc:Password>
					<jdbc:SecurityConstraints/>
					<jdbc:Encoding>iso-8859-1</jdbc:Encoding>
				</jdbc:JDBCConnection>
				<deegree:Table>
					<xsl:value-of select="$TABLE"></xsl:value-of>
				</deegree:Table>
			</deegree:Database>
		</deegree:Resolution>
	</deegree:Extension>
</xsl:template>
	
</xsl:stylesheet>
