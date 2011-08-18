<?xml version="1.0" encoding="UTF-8"?>
<ctl:package
 xmlns="http://www.w3.org/2001/XMLSchema"
 xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
 xmlns:ctl="http://www.occamlab.com/ctl"
 xmlns:parsers="http://www.occamlab.com/te/parsers"
 xmlns:p="http://teamengine.sourceforge.net/parsers"
 xmlns:saxon="http://saxon.sf.net/"
 xmlns:wfs="http://www.opengis.net/wfs"
 xmlns:gml="http://www.opengis.net/gml"
 xmlns:ows="http://www.opengis.net/ows"
 xmlns:xlink="http://www.w3.org/1999/xlink"
 xmlns:xi="http://www.w3.org/2001/XInclude"
 xmlns:xsd="http://www.w3.org/2001/XMLSchema">

	<ctl:test name="wfs:readiness-tests">
		<ctl:param name="wfs.GetCapabilities.document"/>
		<ctl:param name="wfs-transaction"/>
        <ctl:param name="wfs-xlink"/>
		<ctl:param name="gmlsf.profile.level"/>
		<ctl:assertion>
        Assess readiness of the IUT. Check the retrieved capabilities document for
        available service endpoints; determine if the service is available and
        is ready to undergo further testing.
        </ctl:assertion>
        <ctl:comment>
        The capabilities document is first checked for the presence of required
        HTTP method bindings. Then a GetCapabilities request is submitted to the
        SUT using the GET method. A subsequent GetFeature request to retrieve
        one of the records in the test data set is then submitted and checked
        for a non-empty response. If any of these checks fail, execution of the
        test suite is aborted.
        </ctl:comment>
		<ctl:code>
          <xsl:choose>
            <xsl:when test="string-length($wfs-transaction) gt 0">
              <ctl:call-test name="ctl:SchematronValidatingParser">
		        <ctl:with-param name="doc" select="$wfs.GetCapabilities.document" />
		        <ctl:with-param name="schema">sch/wfs/1.1.0/Capabilities.sch</ctl:with-param>
		        <ctl:with-param name="phase">RequiredTransactionBindingsPhase</ctl:with-param>
	          </ctl:call-test>
            </xsl:when>
            <xsl:otherwise>
              <ctl:call-test name="ctl:SchematronValidatingParser">
		        <ctl:with-param name="doc" select="$wfs.GetCapabilities.document" />
		        <ctl:with-param name="schema">sch/wfs/1.1.0/Capabilities.sch</ctl:with-param>
		        <ctl:with-param name="phase">RequiredBasicElementsPhase</ctl:with-param>
	          </ctl:call-test>
            </xsl:otherwise>
          </xsl:choose>

            <xsl:variable name="GetCapabilities.get.url">
		        <xsl:value-of select="$wfs.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
		    </xsl:variable>
            <xsl:variable name="GetFeature.get.url">
			    <xsl:value-of select="$wfs.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetFeature']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
		    </xsl:variable>
            <xsl:variable name="GetFeature.post.url">
			    <xsl:value-of select="$wfs.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetFeature']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
		    </xsl:variable>

            <xsl:choose>
              <xsl:when test="not(starts-with($GetCapabilities.get.url,'http'))">
                  <ctl:message>
                  FAILURE: HTTP endpoint for GetCapabilities using GET method not found in capabilities document.
                  </ctl:message>
                  <ctl:fail />
              </xsl:when>
              <xsl:when test="not(starts-with($GetFeature.post.url,'http')) and not(starts-with($GetFeature.get.url,'http'))">
                  <ctl:message>
                  FAILURE: HTTP endpoint for GetFeature using POST or GET method not found in capabilities document.
                  </ctl:message>
                  <ctl:fail />
              </xsl:when>
              <xsl:otherwise>
                <xsl:variable name="response1">
				  <ctl:request>
					<ctl:url>
						<xsl:value-of select="$GetCapabilities.get.url"/>
					</ctl:url>
					<ctl:method>GET</ctl:method>
					<ctl:param name="service">WFS</ctl:param>
					<ctl:param name="version">1.1.0</ctl:param>
					<ctl:param name="request">GetCapabilities</ctl:param>
					<p:XMLValidatingParser.GMLSF1/>
				  </ctl:request>
			    </xsl:variable>
				<xsl:variable name="response2">
				  <xsl:choose>
							<xsl:when test="not($GetFeature.post.url = '')">
							  <ctl:request>
								<ctl:url>
									<xsl:value-of select="$GetFeature.post.url"/>
								</ctl:url>
								<ctl:method>POST</ctl:method>
								<ctl:body>
									<wfs:GetFeature xmlns:wfs="http://www.opengis.net/wfs" version="1.1.0" service="WFS">
										<wfs:Query xmlns:sf="http://cite.opengeospatial.org/gmlsf" typeName="sf:PrimitiveGeoFeature" />
									</wfs:GetFeature>
								</ctl:body>
								<p:XMLValidatingParser.GMLSF1/>
							  </ctl:request>
							</xsl:when>
                            <xsl:otherwise>
                              <ctl:request>
								<ctl:url>
									<xsl:value-of select="$GetFeature.get.url"/>
								</ctl:url>
								<ctl:method>GET</ctl:method>
								<ctl:param name="request">GetFeature</ctl:param>
								<ctl:param name="service">WFS</ctl:param>
								<ctl:param name="version">1.1.0</ctl:param>
								<ctl:param name="typename">sf:PrimitiveGeoFeature</ctl:param>
								<ctl:param name="namespace">xmlns(sf=http://cite.opengeospatial.org/gmlsf)</ctl:param>
								<p:XMLValidatingParser.GMLSF1/>
							  </ctl:request>
                            </xsl:otherwise>
				  </xsl:choose>
				</xsl:variable>

                <xsl:choose>
                  <xsl:when test="not($response1/*)">
                    <ctl:message>
                    FAILURE: The response from <xsl:value-of select="$GetCapabilities.get.url"/> could not be read or is invalid.
                    </ctl:message>
                    <ctl:fail/>
                  </xsl:when>
                  <xsl:when test="not($response2/*)">
                    <ctl:message>
                    FAILURE: The GetFeature response could not be read or is invalid.
                    </ctl:message>
                    <ctl:fail/>
                  </xsl:when>
                  <xsl:when test="not($response1/wfs:WFS_Capabilities)">
                    <ctl:message>
                    FAILURE: The response entity is NOT a wfs:WFS_Capabilities document.
                    The document element has [local name] = <xsl:value-of select="local-name($response1/*[1])"/> and [namespace name] = <xsl:value-of select="namespace-uri($response1/*[1])"/>.
                    </ctl:message>
                    <ctl:fail/>
                  </xsl:when>
                  <xsl:when test="(count($response2//gml:featureMember) + count($response2//gml:featureMembers/*)) &lt; 4">
                    <ctl:message>
                    FAILURE: The resulting wfs:FeatureCollection must include at least 4 sf:PrimitiveGeoFeature instances from the test data set.
                    </ctl:message>
                    <ctl:fail/>
                  </xsl:when>
                  <xsl:otherwise>
					 <ctl:call-test name="wfs:basic-main">
						<ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
						<ctl:with-param name="gmlsf.profile.level" select="$gmlsf.profile.level"/>
						<ctl:with-param name="wfs-xlink" select="$wfs-xlink"/>
					 </ctl:call-test>
					 <xsl:if test="string-length($wfs-transaction) gt 0">
						<ctl:call-test name="wfs:transaction-main">
							<ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
							<ctl:with-param name="gmlsf.profile.level" select="$gmlsf.profile.level"/>
						</ctl:call-test>
					 </xsl:if>
					 <xsl:if test="string-length($wfs-xlink) gt 0">
                       <ctl:call-test name="wfs:XLinkTests">
							<ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
						</ctl:call-test>
                     </xsl:if>
				  </xsl:otherwise>
				</xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
		</ctl:code>
	</ctl:test>

</ctl:package>

