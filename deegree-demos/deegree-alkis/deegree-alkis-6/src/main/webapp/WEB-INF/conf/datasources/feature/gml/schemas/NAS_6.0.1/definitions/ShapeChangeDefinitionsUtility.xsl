<?xml version="1.0" encoding="iso-8859-1"?>
<!-- (c) 2007 interactive instruments GmbH -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:def="http://www.interactive-instruments.de/ShapeChange/Definitions/0.5" xmlns:xlink="http://www.w3.org/1999/xlink">
	<xsl:template match="def:TypeDefinition" mode="header">
		<a>
			<xsl:attribute name="name"><xsl:value-of select="@gml:id"/></xsl:attribute>
			<table>
				<tr>
					<td width="100%" bgcolor="#D0D0D0">
						<big>
							<b>
								<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
							</b> (<xsl:value-of select="def:classification"/>)
						</big>
					</td>
					<td>
						<br/>
					</td>
				</tr>
			</table>
		</a>
	</xsl:template>
	<xsl:template match="def:PropertyDefinition" mode="header">
		<a>
			<xsl:attribute name="name"><xsl:value-of select="@gml:id"/></xsl:attribute>
			<table>
				<tr>
					<td width="100%" bgcolor="#F0F0F0">
						<b>
							<xsl:choose>
								<xsl:when test="def:type='attribute'">Attribute: </xsl:when>
								<xsl:when test="def:type='associationRole'">Association Role: </xsl:when>
							</xsl:choose>
							<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
						</b>
					</td>
					<td>
						<br/>
					</td>
				</tr>
			</table>
		</a>
	</xsl:template>
	<xsl:template match="def:TypeDefinition" mode="body">
		<table cellspacing="10">
			<xsl:if test="gml:description">
				<tr valign="top">
					<td>
							<u>Documentation:</u>
					</td>
					<td>
							<xsl:value-of select="gml:description"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/title']">
				<tr valign="top">
					<td>
							<u>Title:</u>
					</td>
					<td>
							<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/title']"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/secondaryCode']">
				<tr valign="top">
					<td>
							<u>Code:</u>
					</td>
					<td>
							<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/secondaryCode']"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="def:supertypeName | def:supertypeRef">
				<tr valign="top">
					<td>
							<u>Supertypes:</u>
					</td>
					<td>
						<xsl:for-each select="def:supertypeName">
							<xsl:value-of select="."/><br/>
						</xsl:for-each>
						<xsl:for-each select="def:supertypeRef">
									<xsl:variable name="ref" select="./@xlink:href"/>
									<xsl:variable name="vtr" select="substring-after(substring-after(substring-after($ref,'::'),':'),':')"/>
									<xsl:variable name="refid" select="//def:TypeDefinition[gml:identifier=$ref]/@gml:id"/>
									<xsl:choose>
										<xsl:when test="$refid">
											<a>
												<xsl:attribute name="href">#<xsl:value-of select="$refid"/></xsl:attribute>
												<xsl:value-of select="$vtr"/>
											</a>
										</xsl:when>
										<xsl:otherwise>
											<a>
												<xsl:attribute name="href"><xsl:value-of select="$vtr"/>.definitions.xml</xsl:attribute>
												<xsl:value-of select="$vtr"/>
											</a>
										</xsl:otherwise>
									</xsl:choose>
									<br/>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:dictionaryEntry/def:PropertyDefinition[def:type='attribute']">
				<tr valign="top">
					<td>
						<u>Attributes:</u>
					</td>
					<td>
						<xsl:for-each select="gml:dictionaryEntry/def:PropertyDefinition[def:type='attribute']">
							<a>
								<xsl:attribute name="href">#<xsl:value-of select="@gml:id"/></xsl:attribute>
								<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
							</a>
							<br/>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:dictionaryEntry/def:PropertyDefinition[def:type='associationRole']">
				<tr valign="top">
					<td>
						<u>Associations:</u>
					</td>
					<td>
						<xsl:for-each select="gml:dictionaryEntry/def:PropertyDefinition[def:type='associationRole']">
							<a>
								<xsl:attribute name="href">#<xsl:value-of select="@gml:id"/></xsl:attribute>
								<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
							</a>
							<br/>
						</xsl:for-each>
					</td>
				</tr>
			</xsl:if>
			<xsl:for-each select="def:taggedValue">
				<tr valign="top">
					<td>
						<u><xsl:value-of select="@tag"/>:</u>
					</td>
					<td>
						<xsl:value-of select="."/>
					</td>
				</tr>
			</xsl:for-each>
		</table>
		<table>
			<tr>
				<td width="100%" align="right">
						<small>
							<a>
								<xsl:attribute name="href">index.<xsl:value-of select="substring-before(substring-after(gml:identifier,'::'),':')"/>.definitions.xml</xsl:attribute>back to overview
							</a>
						</small>
				</td>
				<td>
					<br/>
				</td>
			</tr>
		</table>
	</xsl:template>
	<xsl:template match="def:PropertyDefinition" mode="body">
		<table cellspacing="10">
			<xsl:if test="gml:description">
				<tr valign="top">
					<td>
							<u>Documentation:</u>
					</td>
					<td>
							<xsl:value-of select="gml:description"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/title']">
				<tr valign="top">
					<td>
							<u>Title:</u>
					</td>
					<td>
							<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/title']"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/secondaryCode']">
				<tr valign="top">
					<td>
							<u>Code:</u>
					</td>
					<td>
							<xsl:value-of select="gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/secondaryCode']"/>
					</td>
				</tr>
			</xsl:if>
			<xsl:if test="gml:cardinality">
				<tr valign="top">
					<td>
							<u>Cardinality:</u>
					</td>
					<td>
							<xsl:value-of select="def:cardinality"/>
					</td>
				</tr>
			</xsl:if>
			<tr valign="top">
				<xsl:choose>
					<xsl:when test="def:valueTypeName | def:valueTypeRef">
						<xsl:choose>
							<xsl:when test="def:valueTypeName">
								<td>
									<u>Type:</u>
								</td>
								<td>
									<xsl:value-of select="def:valueTypeName"/>
									<xsl:if test="gml:dictionaryEntry/def:ListedValueDefinition">
										(listed value)
									</xsl:if>
								</td>
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="ref" select="def:valueTypeRef/@xlink:href"/>
								<xsl:variable name="refid" select="//def:TypeDefinition[gml:identifier=$ref]/@gml:id"/>
								<xsl:variable name="vtr" select="substring-after(substring-after(substring-after(def:valueTypeRef/@xlink:href,'::'),':'),':')"/>
								<xsl:variable name="vtt" select="substring-before(substring-after(substring-after(substring-after(substring-before(def:valueTypeRef/@xlink:href,'::'),':'),':'),':'),':')"/>
								<xsl:choose>
									<xsl:when test="$vtt='featureType'">
										<td>
											<u>Feature Type:</u>
										</td>
									</xsl:when>
									<xsl:when test="$vtt='objectType'">
										<td>
											<u>Object Type:</u>
										</td>
									</xsl:when>
									<xsl:when test="$vtt='dataType' or $vtt='unionType'">
										<td>
											<u>Data Type:</u>
										</td>
									</xsl:when>
									<xsl:otherwise>
										<td>
											<u>Type:</u>
										</td>
									</xsl:otherwise>
								</xsl:choose>
								<td>
									<a>
										<xsl:attribute name="href">
											<xsl:choose>
												<xsl:when test="$refid">#<xsl:value-of select="$refid"/></xsl:when>
												<xsl:otherwise><xsl:value-of select="$vtr"/>.definitions.xml</xsl:otherwise>
											</xsl:choose>
										</xsl:attribute>
										<xsl:value-of select="$vtr"/>
									</a>
								</td>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<td>
							<u>Type:</u>
						</td>
						<td>
							<i>unknown</i>
						</td>
					</xsl:otherwise>
				</xsl:choose>
			</tr>
			<xsl:if test="gml:dictionaryEntry/def:ListedValueDefinition">
				<tr valign="top">
					<td/>
					<td>
						<table>
							<tr>
								<td>
									<u>Value</u>
								</td>
								<td width="10">
									<br/>
								</td>
								<td>
									<u>Code</u>
								</td>
								<td width="10">
									<br/>
								</td>
								<td>
									<u>Documentation</u>
								</td>
							</tr>
							<xsl:for-each select="gml:dictionaryEntry/def:ListedValueDefinition">
								<tr>
									<td valign="top">
										<xsl:value-of select="gml:name"/>
									</td>
									<td width="10">
										<br/>
									</td>
									<td valign="top">
										<xsl:value-of select="substring-after(substring-after(substring-after(substring-after(substring-after(gml:identifier,'::'),':'),':'),':'),':')"/>
									</td>
									<td width="10">
										<br/>
									</td>
									<td valign="top">
										<xsl:if test="gml:description">
											<xsl:value-of select="gml:description"/>
										</xsl:if>
									</td>
								</tr>
							</xsl:for-each>
						</table>
					</td>
				</tr>
			</xsl:if>
		</table>
		<table>
			<tr>
				<td width="100%" align="right">
						<small>
							<a>
								<xsl:attribute name="href">#<xsl:value-of select="../../@gml:id"/></xsl:attribute>back to <xsl:value-of select="../../gml:name[@codeSpace='http://www.interactive-instruments.de/ShapeChange/Definitions/0.5/name']"/>
							</a>
						</small>
				</td>
				<td>
					<br/>
				</td>
			</tr>
		</table>
	</xsl:template>
</xsl:stylesheet>
