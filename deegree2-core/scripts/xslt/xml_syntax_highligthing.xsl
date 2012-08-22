<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">

	<xsl:output encoding="iso-8859-1"  method="html"></xsl:output>

	<xsl:template match="/">
		<html>
			<head>
				<title></title>
			</head>
			<body>
				<xsl:call-template name="ITERATE"></xsl:call-template>	
			</body>
		</html>
	</xsl:template>
	
	<xsl:template name="ITERATE">
		<xsl:for-each select="./child::*">
			<ul style="line-height:1.1em; list-style-position:inside">
				<li style="line-height:1.1em; list-style-type:none">
					<xsl:call-template name="OPENNODE"></xsl:call-template>
					<xsl:choose>
						<xsl:when test="count( child::* ) = 0 ">	
							<xsl:value-of select="."></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
								<xsl:call-template name="ITERATE"></xsl:call-template>				
						</xsl:otherwise>
					</xsl:choose>			
					<xsl:call-template name="CLOSENODE"></xsl:call-template>
				</li>
			</ul>
		</xsl:for-each>
	</xsl:template>
		
	<xsl:template name="OPENNODE">
		<a style="color:#0000FF;">&lt;</a><xsl:value-of select="name(.)"/><xsl:call-template name="NAMESP"/><xsl:apply-templates select="./@*"/><a style="color:#0000FF;">&gt;</a>
	</xsl:template>
	
	<xsl:template name="CLOSENODE">
		<a style="color:#0000FF; line-height:1.4em">&lt;/</a><xsl:value-of select="name(.)"></xsl:value-of><a style="color:#0000FF;">&gt;</a>
	</xsl:template>
	
	<xsl:template match="@*"><a style="color:#FF0000;"><xsl:value-of select="concat( ' ',  name(.), '=' )"/></a>"<xsl:value-of select="."/>"</xsl:template>
	
	<xsl:template name="NAMESP"><a style="color:#FF0000;"> xmlns=</a>"<xsl:value-of select="namespace-uri(.)"/>"</xsl:template>

</xsl:stylesheet>
