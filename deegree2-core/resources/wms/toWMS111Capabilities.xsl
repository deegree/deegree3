<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xlink="http://www.w3.org/1999/xlink" >
	<xsl:template match="WMT_MS_Capabilities" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
		<WMT_MS_Capabilities version="1.1.1">
			<xsl:attribute name="updateSequence"><xsl:value-of select="@updateSequence"/></xsl:attribute>
			<xsl:apply-templates select="Service"/>
			<xsl:apply-templates select="Capability"/>
		</WMT_MS_Capabilities>
	</xsl:template>
	<!-- ======================================================= -->
	<!-- service -->
	<!-- ======================================================= -->
	<xsl:template match="Service">
		<Service>
			<Name>OGC:WMS</Name>
			<Title>
				<xsl:value-of select="./Title"/>
			</Title>
			<xsl:if test="Abstract != ''">
				<Abstract>
					<xsl:value-of select="./Abstract"/>
				</Abstract>
			</xsl:if>
			<xsl:if test="KeywordList != ''">
				<KeywordList>
					<xsl:apply-templates select="KeywordList/Keyword"/>
				</KeywordList>
			</xsl:if>
			<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
				<xsl:attribute name="xlink:href"><xsl:value-of select="OnlineResource/@href"/></xsl:attribute>
			</OnlineResource>
			<xsl:if test="ContactInformation != ''">
				<ContactInformation>
					<ContactPersonPrimary>
						<ContactPerson>
							<xsl:value-of select="ContactInformation/ContactPersonPrimary/ContactPerson"/>
						</ContactPerson>
						<ContactOrganization>
							<xsl:value-of select="ContactInformation/ContactPersonPrimary/ContactOrganization"/>
						</ContactOrganization>
					</ContactPersonPrimary>
					<xsl:if test="ContactInformation/ContactPosition != ''">
						<ContactPosition>
							<xsl:value-of select="ContactInformation/ContactPosition"/>
						</ContactPosition>
					</xsl:if>
					<ContactAddress>
						<AddressType>
							<xsl:value-of select="ContactInformation/ContactAddress/AddressType"/>
						</AddressType>
						<Address>
							<xsl:value-of select="ContactInformation/ContactAddress/Address"/>
						</Address>
						<City>
							<xsl:value-of select="ContactInformation/ContactAddress/City"/>
						</City>
						<StateOrProvince>
							<xsl:value-of select="ContactInformation/ContactAddress/StateOrProvince"/>
						</StateOrProvince>
						<PostCode>
							<xsl:value-of select="ContactInformation/ContactAddress/Postcode"/>
						</PostCode>
						<Country>
							<xsl:value-of select="ContactInformation/ContactAddress/County"/>
						</Country>
					</ContactAddress>
					<xsl:if test="ContactInformation/ContactVoiceTelephone != ''">
						<ContactVoiceTelephone>
							<xsl:value-of select="ContactInformation/ContactVoiceTelephone"/>
						</ContactVoiceTelephone>
					</xsl:if>
					<xsl:if test="ContactInformation/ContactFacsimileTelephone != ''">
						<ContactFacsimileTelephone>
							<xsl:value-of select="ContactInformation/ContactFacsimileTelephone"/>
						</ContactFacsimileTelephone>
					</xsl:if>
					<xsl:if test="ContactInformation/ContactElectronicMailAddress != ''">
						<ContactElectronicMailAddress>
							<xsl:value-of select="ContactInformation/ContactElectronicMailAddress"/>
						</ContactElectronicMailAddress>
					</xsl:if>
				</ContactInformation>
			</xsl:if>
			<xsl:if test="Fees != ''">
				<Fees>
					<xsl:value-of select="Fees"/>
				</Fees>
			</xsl:if>
			<xsl:if test="AccessConstraints != ''">
				<AccessConstraints>
					<xsl:value-of select="AccessConstraints"/>
				</AccessConstraints>
			</xsl:if>
		</Service>
	</xsl:template>
	<!-- ======================================================= -->
	<!-- capabilitiy -->
	<!-- ======================================================= -->
	<xsl:template match="Capability">
		<Capability>
			<xsl:apply-templates select="Request"/>
			<Exception>
				<xsl:apply-templates select="Exception/Format"/>
			</Exception>
			<xsl:apply-templates select="UserDefinedSymbolization"/>
			<xsl:apply-templates select="Layer"/>
		</Capability>
	</xsl:template>
	<xsl:template match="UserDefinedSymbolization">
		<xsl:if test="UserDefinedSymbolization != ''">
			<UserDefinedSymbolization SupportSLD="1" UserLayer="1" UserStyle="1">
				<xsl:attribute name="SupportSLD"><xsl:value-of select="@SupportSLD"/></xsl:attribute>
				<xsl:attribute name="UserLayer"><xsl:value-of select="@UserLayer"/></xsl:attribute>
				<xsl:attribute name="UserStyle"><xsl:value-of select="@UserStyle"/></xsl:attribute>
			</UserDefinedSymbolization>
		</xsl:if>
	</xsl:template>
	<!-- ======================================================= -->
	<!-- Request -->
	<!-- ======================================================= -->
	<xsl:template match="Request">
		<Request>
			<GetCapabilities>
				<Format>application/vnd.ogc.wms_xml</Format>
				<DCPType>
					<HTTP>
						<Get>
							<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
								<xsl:attribute name="xlink:href"><xsl:value-of select="GetCapabilities/DCPType/HTTP/Get/OnlineResource/@href"/></xsl:attribute>
							</OnlineResource>
						</Get>
					</HTTP>
				</DCPType>
			</GetCapabilities>
			<GetMap>
				<xsl:apply-templates select="GetMap/Format"/>
				<DCPType>
					<HTTP>
						<Get>
							<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
								<xsl:attribute name="xlink:href"><xsl:value-of select="./GetMap/DCPType/HTTP/Get/OnlineResource/@href"/></xsl:attribute>
							</OnlineResource>
						</Get>
						<Post>
							<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
								<xsl:attribute name="xlink:href"><xsl:value-of select="GetMap/DCPType/HTTP/Post/OnlineResource/@href"/></xsl:attribute>
							</OnlineResource>
						</Post>
					</HTTP>
				</DCPType>
			</GetMap>
			<GetFeatureInfo>
				<xsl:apply-templates select="GetFeatureInfo/Format"/>
				<DCPType>
					<HTTP>
						<Get>
							<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
								<xsl:attribute name="xlink:href"><xsl:value-of select="GetFeatureInfo/DCPType/HTTP/Get/OnlineResource/@href"/></xsl:attribute>
							</OnlineResource>
						</Get>
					</HTTP>
				</DCPType>
			</GetFeatureInfo>
		</Request>
	</xsl:template>
	<xsl:template match="*/Format">
		<Format>
			<xsl:value-of select="."/>
		</Format>
	</xsl:template>
	<!-- ======================================================= -->
	<!-- Layer -->
	<!-- ======================================================= -->
	<xsl:template match="Layer">
		<Layer>
			<xsl:if test="@queryable != ''">
				<xsl:attribute name="queryable"><xsl:value-of select="@queryable"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@cascaded != ''">
				<xsl:attribute name="cascaded"><xsl:value-of select="@cascaded"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@opaque != ''">
				<xsl:attribute name="opaque"><xsl:value-of select="@opaque"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@noSubsets != ''">
				<xsl:attribute name="noSubsets"><xsl:value-of select="@noSubsets"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@fixedWidth != ''">
				<xsl:attribute name="fixedWidth"><xsl:value-of select="@fixedWidth"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="@fixedHeight != ''">
				<xsl:attribute name="fixedHeight"><xsl:value-of select="@fixedHeight"/></xsl:attribute>
			</xsl:if>
			<xsl:if test="./Name != ''">
				<Name>
					<xsl:value-of select="./Name"/>
				</Name>
			</xsl:if>
			<Title>
				<xsl:value-of select="./Title"/>
			</Title>
			<xsl:if test="Abstract != ''">
				<Abstract>
					<xsl:value-of select="./Abstract"/>
				</Abstract>
			</xsl:if>
			<xsl:if test="KeywordList != ''">
				<KeywordList>
					<xsl:apply-templates select="KeywordList/Keyword"/>
				</KeywordList>
			</xsl:if>
			<xsl:for-each select="SRS">
				<SRS>
					<xsl:value-of select="."/>
				</SRS>
			</xsl:for-each>
			<xsl:apply-templates select="LatLonBoundingBox"/>
			<xsl:apply-templates select="BoundingBox"/>
			<xsl:if test="Attribution != ''">
				<Attribution>
					<Title>
						<xsl:value-of select="./Title"/>
					</Title>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="Attribution/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
					<LogoURL>
						<xsl:attribute name="width"><xsl:value-of select="Attribution/LogoURL/@width"/></xsl:attribute>
						<xsl:attribute name="height"><xsl:value-of select="Attribution/LogoURL/@height"/></xsl:attribute>
						<Format>
							<xsl:value-of select="./Attribution/LogoURL/Format"/>
						</Format>
						<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
							<xsl:attribute name="xlink:href"><xsl:value-of select="Attribution/LogoURL/OnlineResource/@xlink:href"/></xsl:attribute>
						</OnlineResource>
					</LogoURL>
				</Attribution>
			</xsl:if>
			<xsl:if test="AuthorityURL != ''">
				<AuthorityURL name="latlon">
					<xsl:attribute name="name"><xsl:value-of select="AuthorityURL/@name"/></xsl:attribute>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="AuthorityURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</AuthorityURL>
			</xsl:if>
			<xsl:if test="Identifier != ''">
				<Identifier authority="latlon">
					<xsl:attribute name="authority"><xsl:value-of select="Identifier/@authority"/></xsl:attribute>
					<xsl:value-of select="Identifier"/>
				</Identifier>
			</xsl:if>
			<xsl:if test="MetadataURL != ''">
				<MetadataURL type="TC211">
					<Format>
						<xsl:value-of select="MetadataURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="MetadataURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</MetadataURL>
			</xsl:if>
			<xsl:if test="DataURL != ''">
				<DataURL>
					<Format>
						<xsl:value-of select="DataURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="DataURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</DataURL>
			</xsl:if>
			<xsl:if test="FeatureListURL != ''">
				<FeatureListURL>
					<Format>
						<xsl:value-of select="FeatureListURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="FeatureListURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</FeatureListURL>
			</xsl:if>
			<xsl:apply-templates select="Style"/>
			<xsl:if test="ScaleHint/@min != ''">
				<ScaleHint min="0.0" max="1000.0">
					<xsl:attribute name="min"><xsl:value-of select="ScaleHint /@min"/></xsl:attribute>
					<xsl:attribute name="max"><xsl:value-of select="ScaleHint /@max"/></xsl:attribute>
				</ScaleHint>
			</xsl:if>
			<xsl:apply-templates select="Layer"/>
		</Layer>
	</xsl:template>
	<xsl:template match="LatLonBoundingBox">
		<LatLonBoundingBox>
			<xsl:attribute name="minx"><xsl:value-of select="@minx"/></xsl:attribute>
			<xsl:attribute name="miny"><xsl:value-of select="@miny"/></xsl:attribute>
			<xsl:attribute name="maxx"><xsl:value-of select="@maxx"/></xsl:attribute>
			<xsl:attribute name="maxy"><xsl:value-of select="@maxy"/></xsl:attribute>
		</LatLonBoundingBox>
	</xsl:template>
	<xsl:template match="BoundingBox">
		<BoundingBox>
			<xsl:attribute name="SRS"><xsl:value-of select="@SRS"/></xsl:attribute>
			<xsl:attribute name="minx"><xsl:value-of select="@minx"/></xsl:attribute>
			<xsl:attribute name="miny"><xsl:value-of select="@miny"/></xsl:attribute>
			<xsl:attribute name="maxx"><xsl:value-of select="@maxx"/></xsl:attribute>
			<xsl:attribute name="maxy"><xsl:value-of select="@maxy"/></xsl:attribute>
			<xsl:if test="@resx != ''">
				<xsl:if test="@resx != 0">
					<xsl:attribute name="resx"><xsl:value-of select="@resx"/></xsl:attribute>
				</xsl:if>
			</xsl:if>
			<xsl:if test="@resy != ''">
				<xsl:if test="@resx != 0">
					<xsl:attribute name="resy"><xsl:value-of select="@resy"/></xsl:attribute>
				</xsl:if>
			</xsl:if>
		</BoundingBox>
	</xsl:template>
	<!-- ======================================================= -->
	<!-- Layer -->
	<!-- ======================================================= -->
	<xsl:template match="Style">
		<Style>
			<Name>
				<xsl:value-of select="Name"/>
			</Name>
			<Title>
				<xsl:value-of select="Title"/>
			</Title>
			<xsl:if test="Abstract != ''">
				<Abstract>
					<xsl:value-of select="Abstract"/>
				</Abstract>
			</xsl:if>
			<xsl:if test="LegendURL != ''">
				<LegendURL>
					<xsl:attribute name="width"><xsl:value-of select="LegendURL/@width"/></xsl:attribute>
					<xsl:attribute name="height"><xsl:value-of select="LegendURL/@height"/></xsl:attribute>
					<Format>
						<xsl:value-of select="LegendURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="LegendURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</LegendURL>
			</xsl:if>
			<xsl:if test="StyleSheetURL != ''">
				<StyleSheetURL>
					<Format>
						<xsl:value-of select="StyleSheetURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="StyleSheetURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</StyleSheetURL>
			</xsl:if>
			<xsl:if test="StyleURL != ''">
				<StyleURL>
					<Format>
						<xsl:value-of select="StyleURL/Format"/>
					</Format>
					<OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple">
						<xsl:attribute name="xlink:href"><xsl:value-of select="StyleURL/OnlineResource/@xlink:href"/></xsl:attribute>
					</OnlineResource>
				</StyleURL>
			</xsl:if>
		</Style>
	</xsl:template>
	<xsl:template match="*/Keyword">
		<Keyword>
			<xsl:value-of select="."/>
		</Keyword>
	</xsl:template>
</xsl:stylesheet>
