<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of deegree, for copyright/license information, please visit http://www.deegree.org/license. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:cntxt="http://www.opengis.net/context" xmlns:sld="http://www.opengis.net/sld" xmlns:deegree="http://www.deegree.org/context" xmlns:xlink="http://www.w3.org/1999/xlink">
<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<!-- ===========================================================
	variable section; change if you do not use default installation path and web address
	==============================================================
-->
	<xsl:variable name="ROOTDIR">./</xsl:variable>
	<xsl:variable name="ONLINERESOURCE">http://localhost:8080/igeoportal-std</xsl:variable>
	<!-- ===========================================================
	transformation script
	==============================================================
-->
	<xsl:template match="cntxt:ViewContext">
		<cntxt:ViewContext>
			<xsl:attribute name="version"><xsl:value-of select="./@version"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:General"/>
			<xsl:apply-templates select="cntxt:LayerList"/>
		</cntxt:ViewContext>
	</xsl:template>
	<xsl:template match="cntxt:General">
		<cntxt:General>
			<xsl:apply-templates select="cntxt:Window"/>
			<xsl:apply-templates select="cntxt:BoundingBox"/>
			<xsl:apply-templates select="cntxt:Title"/>
			<xsl:apply-templates select="cntxt:KeywordList"/>
			<xsl:apply-templates select="cntxt:Abstract"/>
			<!--xsl:apply-templates select="cntxt:LogoURL"/-->
			<xsl:apply-templates select="cntxt:DescriptionURL"/>
			<xsl:apply-templates select="cntxt:ContactInformation"/>
			<xsl:choose>
				<xsl:when test="./cntxt:Extension/deegree:Frontend">
					<cntxt:Extension>
						<xsl:copy-of select="./cntxt:Extension/deegree:IOSettings"/>
						<xsl:copy-of select="./cntxt:Extension/deegree:Frontend"/>
						<xsl:copy-of select="./cntxt:Extension/deegree:MapParameter"/>
					</cntxt:Extension>
				</xsl:when>
				<xsl:otherwise>
					<cntxt:Extension xmlns:deegree="http://www.deegree.org/context">
						<deegree:IOSettings>
							<deegree:RootDirectory>
								<xsl:value-of select="$ROOTDIR"/>
							</deegree:RootDirectory>
							<deegree:TempDirectory>
								<deegree:Name>
									<xsl:value-of select="$ROOTDIR"/>
								</deegree:Name>
								<deegree:Access>
									<cntxt:OnlineResource xlink:type="simple">
										<xsl:attribute name="xlink:href"><xsl:value-of select="$ONLINERESOURCE"/></xsl:attribute>
									</cntxt:OnlineResource>
								</deegree:Access>
							</deegree:TempDirectory>
						</deegree:IOSettings>
						<deegree:Frontend scope="JSP">
							<deegree:Controller>.controller.jsp</deegree:Controller>
                            <deegree:Style>./css/deegree.css</deegree:Style>
                            <deegree:Header>header.jsp</deegree:Header>
                            <deegree:Footer>footer.jsp</deegree:Footer>
                            <deegree:CommonJS>
                                <deegree:Name>./javascript/event.js</deegree:Name>
                                <deegree:Name>./javascript/envelope.js</deegree:Name>
                                <deegree:Name>./javascript/geotransform.js</deegree:Name>
                                <deegree:Name>./javascript/gui/pushbutton.js</deegree:Name>
                                <deegree:Name>./javascript/gui/togglebutton.js</deegree:Name>
                                <deegree:Name>./javascript/model/layergroup.js</deegree:Name>
                                <deegree:Name>./javascript/layerutils.js</deegree:Name>
                                <deegree:Name>./javascript/rpc.js</deegree:Name>
                                <deegree:Name>./modules/recentertolayer/recentertolayer.js</deegree:Name>
                                <deegree:Name>./javascript/utils.js</deegree:Name>
                                <deegree:Name>./javascript/geometryutils.js</deegree:Name>
                                <deegree:Name>./javascript/exception.js</deegree:Name>                    
                                <deegree:Name>./javascript/json2.js</deegree:Name>
                                <deegree:Name>./javascript/request_handler.js</deegree:Name>
                                <deegree:Name>./javascript/geometries.js</deegree:Name>
                                <deegree:Name>./javascript/geometryfactory.js</deegree:Name>
                                <deegree:Name>./javascript/wktadapter.js</deegree:Name>
                                <deegree:Name>./javascript/feature.js</deegree:Name>
                            </deegree:CommonJS>
							<deegree:North hidden="false">
								
							</deegree:North>
							<deegree:East hidden="false">
								<deegree:Module hidden="false" type="content" width="150" height="50">
									<deegree:Name>ContextSwitcher</deegree:Name>
									<deegree:Content>./modules/wmc/contextswitcher.html</deegree:Content>
									<deegree:ModuleJS>./modules/wmc/contextswitcher.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>label</deegree:Name>
											<deegree:Value>'Theme selection:'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>listOfContexts</deegree:Name>
											<deegree:Value>'Utah|wmc_start.xml'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>size</deegree:Name>
											<deegree:Value>1</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
								
								<deegree:Module hidden="true" type="content" width="250" height="460">
                                    <deegree:Name>Legend</deegree:Name>
                                    <deegree:Content>./modules/legend/legend_dyn.jsp</deegree:Content>
                                    <deegree:ModuleJS>./modules/legend/legend_dyn.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>label</deegree:Name>
											<deegree:Value>'Legend'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>bgcolor</deegree:Name>
											<deegree:Value>''</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>layerlist</deegree:Name>
											<deegree:Value>this.layerList</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>width</deegree:Name>
											<deegree:Value>20</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>height</deegree:Name>
											<deegree:Value>20</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
								<deegree:Module hidden="false" type="content" width="250" height="460">
									<deegree:Name>LayerListView</deegree:Name>
									<deegree:Content>layerlistview.html</deegree:Content>
									<deegree:ModuleJS>./javascript/model/layerlist.js</deegree:ModuleJS>
									<deegree:ModuleJS>layerlistview.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>name</deegree:Name>
											<deegree:Value>'layerlistview'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>layerlist</deegree:Name>
											<deegree:Value>this.layerList</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>label</deegree:Name>
											<deegree:Value>'Utah'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>bgcolor</deegree:Name>
											<deegree:Value>'#cccccc'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>fgcolor</deegree:Name>
											<deegree:Value>'#aaaaaa'</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
							</deegree:East>
                            <deegree:South hidden="false">
								<deegree:Module hidden="false" type="content" width="300" height="20">
                                    <deegree:Name>CoordinateDisplay</deegree:Name>
                                    <deegree:Content>coordinatedisplay.html</deegree:Content>
                                    <deegree:ModuleJS>coordinatedisplay.js</deegree:ModuleJS>
                                    <deegree:ParameterList>
                                        <deegree:Parameter>
                                            <deegree:Name>digits</deegree:Name>
                                            <deegree:Value>3</deegree:Value>
                                        </deegree:Parameter>
                                        <deegree:Parameter>
                                            <deegree:Name>labelX</deegree:Name>
                                            <deegree:Value>'x:'</deegree:Value>
                                        </deegree:Parameter>
                                        <deegree:Parameter>
                                            <deegree:Name>labelY</deegree:Name>
                                            <deegree:Value>'y:'</deegree:Value>
                                        </deegree:Parameter>
                                    </deegree:ParameterList>
                                </deegree:Module>
                                <deegree:Module hidden="false" type="content" width="685" height="20">
									<deegree:Name>MenuBarBottom</deegree:Name>
									<deegree:Content>menubarbottom.html</deegree:Content>
									<deegree:ModuleJS>menubar_eventhandler.js</deegree:ModuleJS>
								</deegree:Module>
							</deegree:South>
							<deegree:West hidden="false">
								<deegree:Module hidden="false" type="content" width="150" height="150" scrolling="no">
									<deegree:Name>MapOverview</deegree:Name>
									<deegree:Content>./modules/mapoverview/mapoverview.html</deegree:Content>
									<deegree:ModuleJS>./modules/mapoverview/mapoverview.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>src</deegree:Name>
											<deegree:Value>'./overview_utah.gif'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>minx</deegree:Name>
											<deegree:Value>161923</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>miny</deegree:Name>
											<deegree:Value>4094621</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>maxx</deegree:Name>
											<deegree:Value>725119</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>maxy</deegree:Name>
											<deegree:Value>4657817</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>foregroundColor</deegree:Name>
											<deegree:Value>'#ff0000'</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>width</deegree:Name>
											<deegree:Value>150</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>height</deegree:Name>
											<deegree:Value>150</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
                                <deegree:Module hidden="false" type="content" width="150" height="50">
                                    <deegree:Name>ScaleSwitcher</deegree:Name>
                                    <deegree:Content>./modules/scaleswitcher/scaleswitcher.html</deegree:Content>
                                    <deegree:ModuleJS>./modules/scaleswitcher/scaleswitcher.js</deegree:ModuleJS>
                                    <deegree:ParameterList>
                                        <deegree:Parameter>
                                            <deegree:Name>label</deegree:Name>
                                            <deegree:Value>'Scale:'</deegree:Value>
                                        </deegree:Parameter>
                                        <deegree:Parameter>
                                            <deegree:Name>listOfScales</deegree:Name>
                                            <deegree:Value>'1:100;1:5000;1:10000;1:25000;1:50000;1:100000;1:500000;1:1000000;1:5000000'</deegree:Value>
                                        </deegree:Parameter>
                                    </deegree:ParameterList>
                                </deegree:Module>                                
							</deegree:West>
							<deegree:Center hidden="false">
								<deegree:Module hidden="false" type="toolbar" width="550" height="35">
									<deegree:Name>Toolbar</deegree:Name>
									<deegree:Content>./modules/gui/toolbar.html</deegree:Content>
									<deegree:ModuleJS>./modules/gui/toolbar.js</deegree:ModuleJS>
									<deegree:ModuleJS>./javascript/gui/buttongroup.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>refresh|refresh map</deegree:Name>
											<deegree:Value>PushButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>fullextent|zoom to full extent</deegree:Name>
											<deegree:Value>PushButton</deegree:Value>
										</deegree:Parameter>
                                        <deegree:Parameter>
                                            <deegree:Name>movetoprevious|move to previous map</deegree:Name>
                                            <deegree:Value>PushButton</deegree:Value>
                                        </deegree:Parameter>
                                        <deegree:Parameter>
                                            <deegree:Name>movetonext|move to next map</deegree:Name>
                                            <deegree:Value>PushButton</deegree:Value>
                                        </deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>zoomin|zoomin by mouse click or mouse drag</deegree:Name>
											<deegree:Value>ToggleButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>zoomout|zoomout by mouse click</deegree:Name>
											<deegree:Value>ToggleButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>featureinfo|get info to an object within the map</deegree:Name>
											<deegree:Value>ToggleButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>recenter|recenter the map by mouse click</deegree:Name>
											<deegree:Value>ToggleButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>move|drag the map by mouse with pressed mouse button</deegree:Name>
											<deegree:Value>ToggleButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>addwms|add additonal WMS to the map</deegree:Name>
											<deegree:Value>PushButton</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>print|generate print view</deegree:Name>
											<deegree:Value>PushButton</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
								<deegree:Module hidden="false" type="content" width="550" height="550" scrolling="no">
									<deegree:Name>MapView</deegree:Name>
									<deegree:Content>mapview.html</deegree:Content>
									<deegree:ModuleJS>mapview.js</deegree:ModuleJS>
									<deegree:ModuleJS>./javascript/model/mapcontroller.js</deegree:ModuleJS>
									<deegree:ModuleJS>./javascript/model/mapmodel.js</deegree:ModuleJS>
									<deegree:ModuleJS>./javascript/model/wmsrequestfactory.js</deegree:ModuleJS>
									<deegree:ModuleJS>./javascript/model/wmslayer.js</deegree:ModuleJS>
									<deegree:ParameterList>
										<deegree:Parameter>
											<deegree:Name>model</deegree:Name>
											<deegree:Value>this.mapModel</deegree:Value>
										</deegree:Parameter>
										<deegree:Parameter>
											<deegree:Name>border</deegree:Name>
											<deegree:Value>0</deegree:Value>
										</deegree:Parameter>
									</deegree:ParameterList>
								</deegree:Module>
							</deegree:Center>
						</deegree:Frontend>
						<deegree:MapParameter>
							<!--
                				  list of formats offered to the user for GetFeatureInfo requests. The
                				  administrator of the WMS client have make sure that each WMS that is
                				  registered to the client is able the serve the offered formats
                				  default =  text/html
                				  -->
							<deegree:OfferedInfoFormats>
								<deegree:Format>application/vnd.ogc.gml</deegree:Format>
								<deegree:Format selected="true">text/html</deegree:Format>
							</deegree:OfferedInfoFormats>
							<!--
                				  list of available factors (%) a map will be increased, descreased by a
                				  zoom operation. The value '*' indicates that the user will have the
                				  option to choose any value he likes
                				  -->
							<deegree:OfferedZoomFactor>
								<deegree:Factor selected="true">25</deegree:Factor>
							</deegree:OfferedZoomFactor>
							<!--
                				list of available factors (%) a map will be moved by a pan operation 
                				The value '*' indicates that the user will have the option to choose
                				any value he likes
                            -->
							<deegree:OfferedPanFactor>
								<deegree:Factor selected="true">15</deegree:Factor>
							</deegree:OfferedPanFactor>
							<!--
                              minimum scale (as defined by the WMS spec) to which the map can be zoomed in
                              deafult = 1 m
                            -->
							<deegree:MinScale>1</deegree:MinScale>
							<!--
                              maximum scale (as defined by the WMS spec) to which the map can be zoomed out
                              deafult = 100000 m
                            -->
                            <deegree:MaxScale>100000</deegree:MaxScale>
						</deegree:MapParameter>
					</cntxt:Extension>
				</xsl:otherwise>
			</xsl:choose>
		</cntxt:General>
	</xsl:template>
	<xsl:template match="cntxt:Window">
		<cntxt:Window>
			<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
		</cntxt:Window>
	</xsl:template>
	<xsl:template match="cntxt:BoundingBox">
		<cntxt:BoundingBox>
			<xsl:attribute name="SRS"><xsl:value-of select="./@SRS"/></xsl:attribute>
			<xsl:attribute name="minx"><xsl:value-of select="./@minx"/></xsl:attribute>
			<xsl:attribute name="miny"><xsl:value-of select="./@miny"/></xsl:attribute>
			<xsl:attribute name="maxx"><xsl:value-of select="./@maxx"/></xsl:attribute>
			<xsl:attribute name="maxy"><xsl:value-of select="./@maxy"/></xsl:attribute>
		</cntxt:BoundingBox>
	</xsl:template>
	<xsl:template match="cntxt:Title">
		<cntxt:Title>
			<xsl:value-of select="."/>
		</cntxt:Title>
	</xsl:template>
	<xsl:template match="cntxt:KeywordList">
		<cntxt:KeywordList>
			<xsl:for-each select="cntxt:Keyword">
				<cntxt:Keyword>
					<xsl:value-of select="."/>
				</cntxt:Keyword>
			</xsl:for-each>
		</cntxt:KeywordList>
	</xsl:template>
	<xsl:template match="cntxt:Abstract">
		<cntxt:Abstract>
			<xsl:value-of select="."/>
		</cntxt:Abstract>
	</xsl:template>
	<xsl:template match="cntxt:LogoURL">
		<cntxt:LogoURL>
			<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
			<xsl:attribute name="format"><xsl:value-of select="./@format"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:LogoURL>
	</xsl:template>
	<xsl:template match="cntxt:OnlineResource">
		<cntxt:OnlineResource>
			<xsl:attribute name="xlink:type"><xsl:value-of select="./@xlink:type"/></xsl:attribute>
			<xsl:attribute name="xlink:href"><xsl:value-of select="./@xlink:href"/></xsl:attribute>
		</cntxt:OnlineResource>
	</xsl:template>
	<xsl:template match="cntxt:DescriptionURL">
		<cntxt:DescriptionURL>
			<xsl:attribute name="format"><xsl:value-of select="./@format"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:DescriptionURL>
	</xsl:template>
	<xsl:template match="cntxt:ContactInformation">
		<cntxt:ContactInformation>
			<xsl:apply-templates select="cntxt:ContactPersonPrimary"/>
			<xsl:apply-templates select="cntxt:ContactPosition"/>
			<xsl:apply-templates select="cntxt:ContactAddress"/>
			<xsl:apply-templates select="cntxt:ContactVoiceTelephone"/>
			<xsl:apply-templates select="cntxt:ContactFacsimileTelephone"/>
			<xsl:apply-templates select="cntxt:ContactElectronicMailAddress"/>
		</cntxt:ContactInformation>
	</xsl:template>
	<xsl:template match="cntxt:ContactElectronicMailAddress">
		<cntxt:ContactElectronicMailAddress>
			<xsl:value-of select="."/>
		</cntxt:ContactElectronicMailAddress>
	</xsl:template>
	<xsl:template match="cntxt:ContactFacsimileTelephone">
		<cntxt:ContactFacsimileTelephone>
			<xsl:value-of select="."/>
		</cntxt:ContactFacsimileTelephone>
	</xsl:template>
	<xsl:template match="cntxt:ContactVoiceTelephone">
		<cntxt:ContactVoiceTelephone>
			<xsl:value-of select="."/>
		</cntxt:ContactVoiceTelephone>
	</xsl:template>
	<xsl:template match="cntxt:ContactPersonPrimary">
		<cntxt:ContactPersonPrimary>
			<xsl:apply-templates select="cntxt:ContactPerson"/>
			<xsl:apply-templates select="cntxt:ContactOrganization"/>
		</cntxt:ContactPersonPrimary>
	</xsl:template>
	<xsl:template match="cntxt:ContactPerson">
		<cntxt:ContactPerson>
			<xsl:value-of select="."/>
		</cntxt:ContactPerson>
	</xsl:template>
	<xsl:template match="cntxt:ContactOrganization">
		<cntxt:ContactOrganization>
			<xsl:value-of select="."/>
		</cntxt:ContactOrganization>
	</xsl:template>
	<xsl:template match="cntxt:ContactPosition">
		<cntxt:ContactPosition>
			<xsl:value-of select="."/>
		</cntxt:ContactPosition>
	</xsl:template>
	<xsl:template match="cntxt:ContactAddress">
		<cntxt:ContactAddress>
			<xsl:apply-templates select="cntxt:AddressType"/>
			<xsl:apply-templates select="cntxt:Address"/>
			<xsl:apply-templates select="cntxt:City"/>
			<xsl:apply-templates select="cntxt:StateOrProvince"/>
			<xsl:apply-templates select="cntxt:PostCode"/>
			<xsl:apply-templates select="cntxt:Country"/>
		</cntxt:ContactAddress>
	</xsl:template>
	<xsl:template match="cntxt:AddressType">
		<cntxt:AddressType>
			<xsl:value-of select="."/>
		</cntxt:AddressType>
	</xsl:template>
	<xsl:template match="cntxt:Address">
		<cntxt:Address>
			<xsl:value-of select="."/>
		</cntxt:Address>
	</xsl:template>
	<xsl:template match="cntxt:City">
		<cntxt:City>
			<xsl:value-of select="."/>
		</cntxt:City>
	</xsl:template>
	<xsl:template match="cntxt:StateOrProvince">
		<cntxt:StateOrProvince>
			<xsl:value-of select="."/>
		</cntxt:StateOrProvince>
	</xsl:template>
	<xsl:template match="cntxt:PostCode">
		<cntxt:PostCode>
			<xsl:value-of select="."/>
		</cntxt:PostCode>
	</xsl:template>
	<xsl:template match="cntxt:Country">
		<cntxt:Country>
			<xsl:value-of select="."/>
		</cntxt:Country>
	</xsl:template>
	<xsl:template match="cntxt:LayerList">
		<cntxt:LayerList>
			<xsl:apply-templates select="cntxt:Layer"/>
		</cntxt:LayerList>
	</xsl:template>
	<xsl:template match="cntxt:Layer">
		<cntxt:Layer>
			<xsl:attribute name="queryable"><xsl:value-of select="./@queryable"/></xsl:attribute>
			<xsl:attribute name="hidden"><xsl:value-of select="./@hidden"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:Server"/>
			<xsl:apply-templates select="cntxt:Name"/>
			<xsl:apply-templates select="cntxt:Title"/>
			<xsl:apply-templates select="cntxt:Abstract"/>
			<xsl:apply-templates select="cntxt:DataURL"/>
			<xsl:apply-templates select="cntxt:MetadataURL"/>
			<xsl:apply-templates select="cntxt:SRS"/>
			<xsl:apply-templates select="cntxt:FormatList"/>
			<xsl:apply-templates select="cntxt:StyleList"/>
			<xsl:if test="./cntxt:Extension/deegree:DataService">
				<cntxt:Extension>
					<xsl:copy-of select="./cntxt:Extension/deegree:DataService"/>
					<xsl:copy-of select="./cntxt:Extension/deegree:MasterLayer"/>
				</cntxt:Extension>
			</xsl:if>
		</cntxt:Layer>
	</xsl:template>
	<xsl:template match="cntxt:Server">
		<cntxt:Server>
			<xsl:attribute name="service"><xsl:value-of select="./@service"/></xsl:attribute>
			<xsl:attribute name="version"><xsl:value-of select="./@version"/></xsl:attribute>
			<xsl:attribute name="title"><xsl:value-of select="./@title"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:Server>
	</xsl:template>
	<xsl:template match="cntxt:Name">
		<cntxt:Name>
			<xsl:value-of select="."/>
		</cntxt:Name>
	</xsl:template>
	<xsl:template match="cntxt:DataURL">
		<cntxt:DataURL>
			<xsl:attribute name="format"><xsl:value-of select="./@format"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:DataURL>
	</xsl:template>
	<xsl:template match="cntxt:MetadataURL">
		<cntxt:MetadataURL>
			<xsl:attribute name="format"><xsl:value-of select="./@format"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:MetadataURL>
	</xsl:template>
	<xsl:template match="cntxt:SRS">
		<cntxt:SRS>
			<xsl:value-of select="."/>
		</cntxt:SRS>
	</xsl:template>
	<xsl:template match="cntxt:FormatList">
		<cntxt:FormatList>
			<xsl:for-each select="cntxt:Format">
				<cntxt:Format>
					<xsl:if test="./@current != ''">
						<xsl:attribute name="current"><xsl:value-of select="./@current"/></xsl:attribute>
					</xsl:if>
					<xsl:value-of select="."/>
				</cntxt:Format>
			</xsl:for-each>
		</cntxt:FormatList>
	</xsl:template>
	<xsl:template match="cntxt:StyleList">
		<cntxt:StyleList>
			<xsl:for-each select="cntxt:Style">
				<cntxt:Style>
					<xsl:if test="./@current != ''">
						<xsl:attribute name="current"><xsl:value-of select="./@current"/></xsl:attribute>
					</xsl:if>
					<xsl:apply-templates select="cntxt:Name"/>
					<xsl:apply-templates select="cntxt:Title"/>
					<xsl:apply-templates select="cntxt:LegendURL"/>
				</cntxt:Style>
			</xsl:for-each>
		</cntxt:StyleList>
	</xsl:template>
	<xsl:template match="cntxt:LegendURL">
		<cntxt:LegendURL>
			<xsl:attribute name="width"><xsl:value-of select="./@width"/></xsl:attribute>
			<xsl:attribute name="height"><xsl:value-of select="./@height"/></xsl:attribute>
			<xsl:attribute name="format"><xsl:value-of select="./@format"/></xsl:attribute>
			<xsl:apply-templates select="cntxt:OnlineResource"/>
		</cntxt:LegendURL>
	</xsl:template>
</xsl:stylesheet>
