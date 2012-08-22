<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
		xmlns:app="http://www.deegree.org/app"
		xmlns:dog="http://www.deegree.org/dog"
		xmlns:gco="http://www.isotc211.org/2005/gco"
		xmlns:gmd="http://www.isotc211.org/2005/gmd"
		xmlns:gml="http://www.opengis.net/gml"
		xmlns:iso19112="http://www.opengis.net/iso19112"
		xmlns:wfs="http://www.opengis.net/wfs"
		xmlns:xlink="http://www.w3.org/1999/xlink"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" version="1.0" encoding="UTF-8"/>

        <xsl:template name="SI_GAZETTEER">
            <iso19112:SI_Gazetteer>
		<xsl:if test="boolean( app:SI_Gazetteer/@gml:id )">
                    <xsl:attribute name="gml:id">
                        <xsl:value-of select="app:SI_Gazetteer/@gml:id"/>
                    </xsl:attribute>
                </xsl:if>
		<xsl:if test="boolean( app:SI_Gazetteer/app:name )">
                    <iso19112:name>
                        <xsl:value-of select="app:SI_Gazetteer/app:name"/>
                    </iso19112:name>
                </xsl:if>
		<xsl:if test="boolean( app:SI_Gazetteer/app:scope )">
                    <iso19112:scope>
			<xsl:value-of select="app:SI_Gazetteer/app:scope"/>
                    </iso19112:scope>
                </xsl:if>
		<xsl:if test="boolean(app:SI_Gazetteer/app:custodian )">
                    <iso19112:custodian>
			<xsl:choose>
                            <xsl:when test="boolean( app:SI_Gazetteer/app:custodian/@xlink:href )">
				<xsl:attribute name="xlink:href">
                                    <xsl:value-of select="app:SI_Gazetteer/app:custodian/@xlink:href"/>
				</xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
				<xsl:apply-templates select="app:SI_Gazetteer/app:custodian/app:CI_ResponsibleParty"/>
                            </xsl:otherwise>
                        </xsl:choose>				
                    </iso19112:custodian>
                </xsl:if>
		<xsl:if test="boolean( app:SI_Gazetteer/app:territoryOfUse )">
                    <iso19112:territoryOfUse>
			<xsl:choose>
                            <xsl:when test="boolean( app:SI_Gazetteer/app:territoryOfUse/@xlink:href )">
				<xsl:attribute name="xlink:href">
                                    <xsl:value-of select="app:SI_Gazetteer/app:territoryOfUse/@xlink:href"/>
                                </xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
                            <!--xsl:call-template name="GEOGRAPHICBBOX"/-->
				<xsl:apply-templates select="app:SI_Gazetteer/app:territoryOfUse/app:SI_GeographicExtent"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </iso19112:territoryOfUse>
                </xsl:if>
                <!--xsl:if test="boolean( app:SI_Gazetteer/app:isGlobal )">
                    <iso19112:isGlobal>
			<xsl:value-of select="app:SI_Gazetteer/app:isGlobal"/>
                    </iso19112:isGlobal>
                </xsl:if-->
                <!--xsl:if test="boolean( app:SI_Gazetteer/app:srsName )">
                    <iso19112:srsName>
			<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:srsName"/>
                    </iso19112:srsName>
                </xsl:if-->
                <xsl:if test="boolean( app:SI_Gazetteer/app:featureType )">
                    <iso19112:featureType>
			<xsl:choose>
                            <xsl:when test="boolean( app:SI_Gazetteer/app:featureType/@xlink:href )">
				<xsl:attribute name="xlink:href">
                                    <xsl:value-of select=" app:SI_Gazetteer/app:featureType/@xlink:href"/>
                                </xsl:attribute>
                            </xsl:when>
                            <xsl:otherwise>
				<xsl:apply-templates select="app:SI_Gazetteer/app:featureType/app:SI_LocationType"/>
                            </xsl:otherwise>
                        </xsl:choose>				
                    </iso19112:featureType>
                </xsl:if>
            </iso19112:SI_Gazetteer>
	</xsl:template>

	<!-- is used for properties of app:Hauskoordinaten etc. -->
	<xsl:template name="GEOGRAPHICBBOX">
		<!--xsl:param name="pathtobox">.</xsl:param-->
		<gmd:EX_GeographicBoundingBox>
			<!--xsl:copy-of select="."/-->
			<gmd:westBoundLongitude>
				<gco:Decimal>
					<!--xsl:value-of select="$pathtobox"/-->
					<xsl:value-of select="app:westBoundLongitude"/>
				</gco:Decimal>
			</gmd:westBoundLongitude>
			<gmd:eastBoundLongitude>
				<gco:Decimal>
					<xsl:value-of select="app:eastBoundLongitude"/>
				</gco:Decimal>
			</gmd:eastBoundLongitude>
			<gmd:southBoundLatitude>
				<gco:Decimal>
					<xsl:value-of select="app:southBoundLatitude"/>
				</gco:Decimal>
			</gmd:southBoundLatitude>
			<gmd:northBoundLatitude>
				<gco:Decimal>
					<xsl:value-of select="app:northBoundLatitude"/>
				</gco:Decimal>
			</gmd:northBoundLatitude>
		</gmd:EX_GeographicBoundingBox>
	</xsl:template>
	
	<!-- is used for properties of SI_Gazetteer and SI_LocationType (with gml:id) -->
	<xsl:template name="GEOGRAPHICEXTENT">
		<gmd:EX_GeographicBoundingBox>
			<xsl:if test="boolean( @gml:id )">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="@gml:id"/>
				</xsl:attribute>
			</xsl:if>
			<gmd:westBoundLongitude>
				<gco:Decimal>
					<xsl:value-of select="app:westBoundLongitude"/>
				</gco:Decimal>
			</gmd:westBoundLongitude>
			<gmd:eastBoundLongitude>
				<gco:Decimal>
					<xsl:value-of select="app:eastBoundLongitude"/>
				</gco:Decimal>
			</gmd:eastBoundLongitude>
			<gmd:southBoundLatitude>
				<gco:Decimal>
					<xsl:value-of select="app:southBoundLatitude"/>
				</gco:Decimal>
			</gmd:southBoundLatitude>
			<gmd:northBoundLatitude>
				<gco:Decimal>
					<xsl:value-of select="app:northBoundLatitude"/>
				</gco:Decimal>
			</gmd:northBoundLatitude>
		</gmd:EX_GeographicBoundingBox>
	</xsl:template>
	
	<xsl:template name="TEMPORALEXTENT" />
	
        <xsl:template name="GAZETTEER">
		<iso19112:SI_Gazetteer>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/@gml:id )">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/@gml:id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:name )">
				<iso19112:name>
					<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:name"/>
				</iso19112:name>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:scope )">
				<iso19112:scope>
					<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:scope"/>
				</iso19112:scope>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:custodian )">
				<iso19112:custodian>
					<xsl:choose>
						<xsl:when test="boolean( app:gazetteer/app:SI_Gazetteer/app:custodian/@xlink:href )">
							<xsl:attribute name="xlink:href">
								<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:custodian/@xlink:href"/>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="app:gazetteer/app:SI_Gazetteer/app:custodian/app:CI_ResponsibleParty"/>
						</xsl:otherwise>
					</xsl:choose>				
				</iso19112:custodian>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:territoryOfUse )">
				<iso19112:territoryOfUse>
					<xsl:choose>
						<xsl:when test="boolean( app:gazetteer/app:SI_Gazetteer/app:territoryOfUse/@xlink:href )">
							<xsl:attribute name="xlink:href">
								<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:territoryOfUse/@xlink:href"/>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<!--xsl:call-template name="GEOGRAPHICBBOX"/-->
							<xsl:apply-templates select="app:gazetteer/app:SI_Gazetteer/app:territoryOfUse/app:SI_GeographicExtent"/>
						</xsl:otherwise>
					</xsl:choose>
				</iso19112:territoryOfUse>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:isGlobal )">
				<iso19112:isGlobal>
					<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:isGlobal"/>
				</iso19112:isGlobal>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:srsName )">
				<iso19112:srsName>
					<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:srsName"/>
				</iso19112:srsName>
			</xsl:if>
			<xsl:if test="boolean( app:gazetteer/app:SI_Gazetteer/app:featureType )">
				<iso19112:featureType>
					<xsl:choose>
						<xsl:when test="boolean( app:gazetteer/app:SI_Gazetteer/app:featureType/@xlink:href )">
							<xsl:attribute name="xlink:href">
								<xsl:value-of select="app:gazetteer/app:SI_Gazetteer/app:featureType/@xlink:href"/>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="app:gazetteer/app:SI_Gazetteer/app:featureType/app:SI_LocationType"/>
						</xsl:otherwise>
					</xsl:choose>				
				</iso19112:featureType>
			</xsl:if>
		</iso19112:SI_Gazetteer>
	</xsl:template>
	
	<xsl:template name="LOCATIONTYPE">
		<iso19112:SI_LocationType>
			<xsl:if test="boolean( @gml:id )">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="@gml:id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="boolean( app:name )">
				<iso19112:name>
					<xsl:value-of select="app:name"/>
				</iso19112:name>
			</xsl:if>
			<xsl:if test="boolean( app:identifier )">
				<iso19112:identifier>
					<xsl:value-of select="app:identifier"/>
				</iso19112:identifier>
			</xsl:if>
			<xsl:if test="boolean( app:theme )">
				<iso19112:theme>
					<xsl:value-of select="app:theme"/>
				</iso19112:theme>
			</xsl:if>
			<xsl:if test="boolean( app:definition )">
				<iso19112:definition>
					<xsl:value-of select="app:definition"/>
				</iso19112:definition>
			</xsl:if>
			<xsl:if test="boolean( app:territoryOfUse )">
				<iso19112:territoryOfUse>
					<xsl:choose>
						<xsl:when test="boolean( app:territoryOfUse/@xlink:href )">
							<xsl:attribute name="xlink:href">
								<xsl:value-of select="app:territoryOfUse/@xlink:href"/>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="app:territoryOfUse/app:SI_GeographicExtent"/>
						</xsl:otherwise>
					</xsl:choose>
				</iso19112:territoryOfUse>
			</xsl:if>
                        <xsl:if test="boolean( app:owner )">
				<iso19112:owner>
					<xsl:choose>
						<xsl:when test="boolean( app:owner/@xlink:href )">
							<xsl:attribute name="xlink:href">
								<xsl:value-of select="app:owner/@xlink:href"/>
							</xsl:attribute>
						</xsl:when>
						<xsl:otherwise>
							<xsl:apply-templates select="app:owner/app:CI_ResponsibleParty"/>
						</xsl:otherwise>
					</xsl:choose>
				</iso19112:owner>
			</xsl:if>
		</iso19112:SI_LocationType>
	</xsl:template>
	
	<xsl:template name="RESPONSIBLEPARTY">
		<iso19112:CI_ResponsibleParty>
			<xsl:if test="boolean( @gml:id )">
				<xsl:attribute name="gml:id">
					<xsl:value-of select="@gml:id"/>
				</xsl:attribute>
			</xsl:if>
			<xsl:if test="boolean( app:name )">
				<iso19112:name>
					<xsl:value-of select="app:name"/>
				</iso19112:name>
			</xsl:if>
			<xsl:if test="boolean( app:role )">
				<iso19112:role>
					<xsl:value-of select="app:role"/>
				</iso19112:role>
			</xsl:if>
		</iso19112:CI_ResponsibleParty>
	</xsl:template>

	<xsl:template match="app:gazetteer/app:SI_Gazetteer/app:territoryOfUse/app:SI_GeographicExtent">
		<xsl:call-template name="GEOGRAPHICEXTENT"/>
	</xsl:template>
	
        <xsl:template match="app:gazetteer/app:SI_Gazetteer/app:custodian/app:CI_ResponsibleParty">
		<xsl:call-template name="RESPONSIBLEPARTY"/>
	</xsl:template>	
        <xsl:template match="app:SI_Gazetteer/app:custodian/app:CI_ResponsibleParty">
		<xsl:call-template name="RESPONSIBLEPARTY"/>
	
        </xsl:template>	
        <xsl:template match="app:gazetteer/app:SI_Gazetteer/app:featureType/app:SI_LocationType">
		<xsl:call-template name="LOCATIONTYPE"/>
	</xsl:template>
        <xsl:template match="app:SI_Gazetteer/app:featureType/app:SI_LocationType">
		<xsl:call-template name="LOCATIONTYPE"/>
	</xsl:template>
        
	<xsl:template match="app:locationType/app:SI_LocationType/app:territoryOfUse/app:SI_GeographicExtent">
		<xsl:call-template name="GEOGRAPHICEXTENT"/>
	</xsl:template>
        <xsl:template match="app:territoryOfUse/app:SI_GeographicExtent">
		<xsl:call-template name="GEOGRAPHICEXTENT"/>
	</xsl:template>
        <xsl:template match="app:owner/app:CI_ResponsibleParty">
		<xsl:call-template name="RESPONSIBLEPARTY"/>
	</xsl:template>
</xsl:stylesheet>