<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<!--
		checks if the passed parameter is a ISO Date or a DatTime. If it is just a Date
		'T00:00:00' will be added before performing value-of
	-->
	<xsl:template name="toISODateTime">
		<xsl:param name="datetime"></xsl:param>
		<xsl:choose>
			<xsl:when test="contains( $datetime, 'T' )">
				<xsl:value-of select="$datetime"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="concat( $datetime, 'T00:00:00')"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
