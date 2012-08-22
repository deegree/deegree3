<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:gml="http://www.opengis.net/gml"
	xmlns:ogc="http://www.opengis.net/ogc"
	xmlns:csw="http://www.opengis.net/cat/csw"
	xmlns:wfs="http://www.opengis.net/wfs" xmlns:java="java"
	xmlns:mapping="org.deegree.ogcwebservices.csw.iso_profile.Mapping2_0_2_blob">
	<xsl:output method="xml" version="1.0" encoding="UTF-8"
		indent="yes" />

	<xsl:param name="NSP">a:a</xsl:param>

	<xsl:variable name="map" select="mapping:new( )" />

	<xsl:template match="csw:DescribeRecord">
		<xsl:copy-of select="." />
	</xsl:template>
	<xsl:template match="csw:GetRecords">
		<!-- will be used for GetRecords requests -->
		<wfs:GetFeature outputFormat="text/xml; subtype=gml/3.1.1"
			xmlns:gml="http://www.opengis.net/gml"
			xmlns:app="http://www.deegree.org/app">
			<xsl:if test="./@maxRecords != '' ">
				<xsl:attribute name="maxFeatures">
					<xsl:value-of select="./@maxRecords" />
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="./@startPosition != '' ">
				<xsl:attribute name="startPosition">
					<xsl:value-of select="./@startPosition" />
				</xsl:attribute>
			</xsl:if>
            <!-- WFS does not support maxFeatures="0", so set resultType to hits -->
            <xsl:choose>
                <xsl:when test="./@maxRecords = 0">
                    <xsl:attribute name="resultType">hits</xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
        			<xsl:if test="./@resultType = 'HITS' ">
        				<xsl:attribute name="resultType">hits</xsl:attribute>
        			</xsl:if>
        			<xsl:if test="./@resultType = 'RESULTS' ">
        				<xsl:attribute name="resultType">results</xsl:attribute>
        			</xsl:if>
                </xsl:otherwise>
            </xsl:choose>
			<xsl:for-each select="./csw:Query">
				<wfs:Query>
					<xsl:attribute name="typeName">app:CQP_Main</xsl:attribute>
					<xsl:apply-templates select="." />
				</wfs:Query>
			</xsl:for-each>
		</wfs:GetFeature>
		<xsl:apply-templates select="csw:ResponseHandler" />
	</xsl:template>
	<xsl:template match="csw:ResponseHandler" />
	<xsl:template match="csw:Query">
		<xsl:apply-templates select="csw:ElementSetName" />
		<xsl:for-each select="./child::*">
			<xsl:if test="local-name(.) = 'ElementName' ">
				<wfs:PropertyName>
					<xsl:apply-templates select="." />
				</wfs:PropertyName>
			</xsl:if>
		</xsl:for-each>
		<xsl:apply-templates select="csw:Constraint" />
		<xsl:apply-templates select="ogc:SortBy" />
	</xsl:template>
	<xsl:template match="ogc:SortBy">
		<ogc:SortBy>
			<xsl:for-each select="./child::*">
				<ogc:SortProperty>
					<ogc:PropertyName>
						<xsl:value-of
							select="mapping:mapSortProperty( $map, ./ogc:PropertyName, $NSP )" />
					</ogc:PropertyName>
					<xsl:copy-of select="ogc:SortOrder" />
				</ogc:SortProperty>
			</xsl:for-each>
		</ogc:SortBy>
	</xsl:template>
	<xsl:template match="csw:ElementSetName">				
		<xsl:if test=". = 'hits' ">
			<wfs:PropertyName>_COUNT_</wfs:PropertyName>
		</xsl:if>
	</xsl:template>

	<xsl:template match="csw:Constraint">
		<ogc:Filter>
			<xsl:apply-templates select="ogc:Filter" />
		</ogc:Filter>
	</xsl:template>

	<xsl:template match="ogc:Filter">
		<xsl:apply-templates select="ogc:And" />
		<xsl:apply-templates select="ogc:Or" />
		<xsl:apply-templates select="ogc:Not" />
		<xsl:if
			test="local-name(./child::*[1]) != 'And' and local-name(./child::*[1])!='Or' and local-name(./child::*[1])!='Not'">
			<xsl:for-each select="./child::*">
				<xsl:choose>
					<xsl:when
						test="local-name(.) = 'PropertyIsEqualTo'">
						<xsl:call-template name="propertyIsEqualTo" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="copyProperty" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:if>
	</xsl:template>

	<xsl:template match="ogc:And | ogc:Or | ogc:Not">
		<xsl:copy>
			<xsl:apply-templates select="ogc:And" />
			<xsl:apply-templates select="ogc:Or" />
			<xsl:apply-templates select="ogc:Not" />
			<xsl:for-each select="./child::*">
				<xsl:if
					test="local-name(.) != 'And' and local-name(.)!='Or' and local-name(.)!='Not'">
					<xsl:choose>
						<xsl:when
							test="local-name(.) = 'PropertyIsEqualTo'">
							<xsl:call-template name="propertyIsEqualTo" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="copyProperty" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:if>
			</xsl:for-each>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="propertyIsEqualTo">
		<xsl:variable name="propName" select="ogc:PropertyName" />

		<xsl:choose>
			<xsl:when
				test="contains( $propName, 'subject') or 
									contains( $propName, 'AlternateTitle') or 
									contains( $propName, 'ResourceIdentifier') or 
									contains( $propName, 'ResourceLanguage') or 
									contains( $propName, 'GeographicDescriptionCode') or 
                                    contains( $propName, 'TopicCategory')  or
									contains( $propName, 'ConditionApplyingToAccessAndUse') or 
                                    contains( $propName, 'AccessConstraints')  or 
                                    contains( $propName, 'OtherConstraints')  or
                                    contains( $propName, 'Classification') or
                                    contains( $propName, 'OperatesOn') or
                                    contains( $propName, 'OperatesOnIdentifier') or
                                    contains( $propName, 'Operation') or
                                    contains( $propName, 'OperatesOnName') or
                                    contains( $propName, 'CouplingType')  ">
				<ogc:PropertyIsLike>
					<xsl:attribute name="wildCard">%</xsl:attribute>
					<xsl:attribute name="singleChar">?</xsl:attribute>
					<xsl:attribute name="escapeChar">\</xsl:attribute>

					<xsl:if test="boolean( ./@matchCase )">
						<xsl:attribute name="matchCase">
							<xsl:value-of select="./@matchCase" />
						</xsl:attribute>
					</xsl:if>
					<ogc:PropertyName>
						<xsl:apply-templates select="$propName" />
					</ogc:PropertyName>
					<xsl:for-each select="./child::*">
						<xsl:if test="local-name(.) = 'Literal'">
							<ogc:Literal>
								<xsl:value-of
									select="mapping:getLiteralValueIsEqualTo( $map,  . )" />
							</ogc:Literal>
						</xsl:if>
						<xsl:if
							test="local-name(.) != 'PropertyName' and local-name(.) != 'Literal' ">
							<xsl:copy-of select="." />
						</xsl:if>
					</xsl:for-each>
				</ogc:PropertyIsLike>
			</xsl:when>
			<xsl:otherwise>
				<ogc:PropertyIsEqualTo>
					<xsl:if test="boolean( ./@matchCase )">
						<xsl:attribute name="matchCase">
							<xsl:value-of select="./@matchCase" />
						</xsl:attribute>
					</xsl:if>
					<ogc:PropertyName>
						<xsl:apply-templates select="$propName" />
					</ogc:PropertyName>

					<xsl:for-each select="./child::*">
						<xsl:if
							test="local-name(.) != 'PropertyName' ">
							<xsl:copy-of select="." />
						</xsl:if>
					</xsl:for-each>

				</ogc:PropertyIsEqualTo>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="copyProperty">
		<xsl:choose>
			<xsl:when test="local-name(.) = 'PropertyIsLike' ">
				<xsl:copy>
					<xsl:variable name="wildCard" select="./@wildCard" />
					<xsl:attribute name="wildCard">
						<xsl:value-of select="$wildCard" />
					</xsl:attribute>
					<xsl:attribute name="singleChar">
						<xsl:value-of select="./@singleChar" />
					</xsl:attribute>
					<xsl:attribute name="escapeChar">
						<xsl:value-of select="./@escape" /><xsl:value-of select="./@escapeChar" />
					</xsl:attribute>
					<xsl:if test="boolean( ./@matchCase )">
						<xsl:attribute name="matchCase">
							<xsl:value-of select="./@matchCase" />
						</xsl:attribute>
					</xsl:if>
					<xsl:variable name="propName"
						select="ogc:PropertyName" />
					<ogc:PropertyName>
						<xsl:apply-templates select="$propName" />
					</ogc:PropertyName>
					<xsl:for-each select="./child::*">
						<xsl:if
							test="local-name(.) != 'PropertyName'">
							<ogc:Literal>
								<xsl:value-of
									select="mapping:getLiteralValueIsLike( $map, ., $propName, wildCard )" />
							</ogc:Literal>
						</xsl:if>
					</xsl:for-each>
				</xsl:copy>
			</xsl:when>
			<xsl:otherwise>
				<xsl:copy>
					<xsl:if test="boolean( ./@matchCase )">
						<xsl:attribute name="matchCase">
							<xsl:value-of select="./@matchCase" />
						</xsl:attribute>
					</xsl:if>
					<xsl:variable name="propName"
						select="ogc:PropertyName" />
					<ogc:PropertyName>
						<xsl:apply-templates select="$propName" />
					</ogc:PropertyName>
					<xsl:for-each select="./child::*">
						<xsl:if
							test="local-name(.) != 'PropertyName' ">
							<xsl:copy-of select="." />
						</xsl:if>
					</xsl:for-each>
				</xsl:copy>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="ogc:PropertyName | csw:ElementName">
		<xsl:value-of
			select="mapping:mapPropertyValue( $map, ., $NSP )" />
	</xsl:template>

</xsl:stylesheet>
