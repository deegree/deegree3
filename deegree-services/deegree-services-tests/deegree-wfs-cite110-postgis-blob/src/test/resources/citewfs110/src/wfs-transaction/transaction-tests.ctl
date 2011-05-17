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

  <!-- include test groups for the WFS-Transaction conformance class -->
  <xi:include href="Transaction/Transaction-XML.xml"/>
  <xi:include href="LockFeature/LockFeature-XML.xml"/>
  <xi:include href="GetFeatureWithLock/GetFeatureWithLock-XML.xml"/>

	<ctl:test name="wfs:transaction-main">
      <ctl:param name="wfs.GetCapabilities.document"/>
      <ctl:param name="gmlsf.profile.level"/>
      <ctl:assertion>Run test group for the WFS-Transaction conformance class.</ctl:assertion>
	  <ctl:code>
	  
         <!-- determine if service supports atomic transactions -->
         <xsl:variable name="supports.atomic.trx" as="xsd:boolean">
           <xsl:variable name="rsp0">
			 <ctl:request>
				<ctl:url>
				  <xsl:value-of select="$wfs.GetCapabilities.document//ows:Operation[@name='Transaction']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
				</ctl:url>
                <ctl:method>POST</ctl:method>
				<ctl:body>
<wfs:Transaction service="WFS" version="1.1.0"
  xmlns:wfs="http://www.opengis.net/wfs"
  xmlns:gml="http://www.opengis.net/gml"
  xmlns:sf="http://cite.opengeospatial.org/gmlsf">
	<wfs:Insert handle="insert-1">
      <sf:UnknownFeature gml:id="id20080125">
        <gml:name codeSpace="http://cite.opengeospatial.org/gmlsf">id20080125</gml:name>
      </sf:UnknownFeature>
    </wfs:Insert>
</wfs:Transaction>
				</ctl:body>
				<p:XMLValidatingParser.GMLSF1/>
			 </ctl:request>
		   </xsl:variable>
           <xsl:choose>
             <xsl:when test="$rsp0//wfs:TransactionResults"><xsl:value-of select="false()"/></xsl:when>
             <xsl:otherwise>
               <!-- received exception report -->
               <xsl:value-of select="true()"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:variable>

         <xsl:choose>
			 <xsl:when test="$supports.atomic.trx">
                 <ctl:message>The service under test supports atomic transactions.</ctl:message>
			 </xsl:when>
			 <xsl:otherwise>
                 <ctl:message>The service under test does NOT support atomic transactions.</ctl:message>
			 </xsl:otherwise>
         </xsl:choose>  
         
		 <!-- Run mandatory test groups -->
		 <ctl:call-test name="wfs:run-Transaction-POST">
		   <ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
		   <ctl:with-param name="gmlsf.level" select="$gmlsf.profile.level"/>
		   <ctl:with-param name="supports.atomic.trx" select="$supports.atomic.trx"/>
		 </ctl:call-test>
		 <!-- run test groups for optional HTTP bindings that have been implemented -->
		 <xsl:if test="$wfs.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='LockFeature']/ows:DCP/ows:HTTP/ows:Post/@xlink:href">
		   <ctl:message>LockFeature using the POST method is implemented.</ctl:message>
		   <ctl:call-test name="wfs:run-LockFeature-POST">
			 <ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
			 <ctl:with-param name="supports.atomic.trx" select="$supports.atomic.trx"/>
		   </ctl:call-test>
		 </xsl:if>
		 <xsl:if test="$wfs.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetFeatureWithLock']/ows:DCP/ows:HTTP/ows:Post/@xlink:href">
		   <ctl:message>GetFeatureWithLock using the POST method is implemented.</ctl:message>
		   <ctl:call-test name="wfs:run-GetFeatureWithLock-POST">
			 <ctl:with-param name="wfs.GetCapabilities.document" select="$wfs.GetCapabilities.document"/>
			 <ctl:with-param name="supports.atomic.trx" select="$supports.atomic.trx"/>
		   </ctl:call-test>
		 </xsl:if>

      </ctl:code>
    </ctl:test>
    
    <!-- subsidiary validation tests available to test groups -->
    <ctl:test name="wfs:GetFeatureById-KVP">
      <ctl:param name="wfs.GetFeature.get.url"/>
      <ctl:param name="id"/>
      <ctl:param name="empty.response"/>
      <ctl:assertion>Attempts to fetch a feature by identifier using the GetFeature/GET binding.</ctl:assertion>
      <ctl:comment>
      If empty.response = 'false', then test passes if the response contains the
      matching feature as a child of either gml:featureMember or gml:featureMembers.
      Otherwise the test passes only if the response is empty.
      </ctl:comment>
      <ctl:code>
         <xsl:variable name="response">
				<ctl:request>
					<ctl:url>
						<xsl:value-of select="$wfs.GetFeature.get.url"/>
					</ctl:url>
					<ctl:method>GET</ctl:method>
					<ctl:param name="service">WFS</ctl:param>
                    <ctl:param name="version">1.1.0</ctl:param>
					<ctl:param name="request">GetFeature</ctl:param>
                    <ctl:param name="featureid"><xsl:value-of select="encode-for-uri($id)"/></ctl:param>
                    <p:XMLValidatingParser.GMLSF1/>
				</ctl:request>
	    </xsl:variable>
        <xsl:choose>
          <xsl:when test="not($response//wfs:FeatureCollection)">
	        <ctl:message>FAILURE: Expected valid wfs:FeatureCollection in response.</ctl:message>
		    <ctl:fail/>
	      </xsl:when>
          <xsl:when test="$empty.response = 'false'">
            <xsl:variable name="fid">
              <xsl:value-of select="$response//gml:featureMember/*[1]/@gml:id"/>
            </xsl:variable>
            <xsl:variable name="fid.alt">
             <xsl:value-of select="$response//gml:featureMembers/*[1]/@gml:id"/>
            </xsl:variable>
            <xsl:if test="($fid != $id) and ($fid.alt != $id)">
              <ctl:message>FAILURE: Did not get feature with matching gml:id (<xsl:value-of select="$id"/>).</ctl:message>
              <ctl:fail/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="(count($response//gml:featureMember) + count($response//gml:featureMembers/*)) > 0">
              <ctl:message>FAILURE: Expected empty GetFeature response.</ctl:message>
              <ctl:fail/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </ctl:code>
	</ctl:test>

    <ctl:test name="wfs:GetFeatureByName">
      <ctl:param name="wfs.GetFeature.post.url"/>
      <ctl:param name="type"/>
      <ctl:param name="name.value"/>
      <ctl:param name="empty.response"/>
      <ctl:assertion>Attempt to fetch a feature by name using the GetFeature/POST binding.</ctl:assertion>
      <ctl:comment>
      If empty.response = 'false', then the test passes if the response contains
      at least one matching feature as a child of either gml:featureMember or
      gml:featureMembers. Otherwise the test passes only if the response is empty.
      </ctl:comment>
      <ctl:code>
         <xsl:variable name="response">
				<ctl:request>
					<ctl:url>
						<xsl:value-of select="$wfs.GetFeature.post.url"/>
					</ctl:url>
					<ctl:method>POST</ctl:method>
					<ctl:body>
                    <foo:GetFeature xmlns:foo="http://www.opengis.net/wfs"
                      service="WFS" version="1.1.0">
						<foo:Query xmlns:sf="http://cite.opengeospatial.org/gmlsf" typeName="{$type}">
							<ogc:Filter xmlns:ogc="http://www.opengis.net/ogc">
								<ogc:PropertyIsEqualTo>
									<ogc:PropertyName xmlns:gml="http://www.opengis.net/gml">gml:name</ogc:PropertyName>
									<ogc:Literal><xsl:value-of select="$name.value"/></ogc:Literal>
								</ogc:PropertyIsEqualTo>
							</ogc:Filter>
						</foo:Query>
					</foo:GetFeature>
                    </ctl:body>
                    <p:XMLValidatingParser.GMLSF1/>
				</ctl:request>
	    </xsl:variable>
        <xsl:variable name="featureCount" select="count($response//gml:featureMember) + count($response//gml:featureMembers/*)" />
        <xsl:choose>
          <xsl:when test="not($response//wfs:FeatureCollection)">
	        <ctl:message>FAILURE: Expected valid wfs:FeatureCollection in response.</ctl:message>
		    <ctl:fail/>
	      </xsl:when>
          <xsl:when test="$empty.response = 'false'">
            <xsl:if test="$featureCount = 0">
              <ctl:message>FAILURE: GetFeature response is empty. Expected feature(s) with gml:name="<xsl:value-of select="$name.value"/>"</ctl:message>
              <ctl:fail/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:if test="$featureCount > 0">
              <ctl:message>FAILURE: Expected empty GetFeature response (where gml:name="<xsl:value-of select="$name.value"/>")</ctl:message>
              <ctl:fail/>
            </xsl:if>
          </xsl:otherwise>
        </xsl:choose>
      </ctl:code>
   </ctl:test>
</ctl:package>

