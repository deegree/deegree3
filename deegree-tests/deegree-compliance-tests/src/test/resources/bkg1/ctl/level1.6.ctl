<?xml version="1.0" encoding="UTF-8"?>
<package
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns="http://www.occamlab.com/ctl"
   xmlns:ctl="http://www.occamlab.com/ctl"
   xmlns:parsers="http://www.occamlab.com/te/parsers"
   xmlns:p="http://teamengine.sourceforge.net/parsers"
   xmlns:saxon="http://saxon.sf.net/"
   xmlns:csw="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:csw2="http://www.opengis.net/cat/csw/2.0.2"
   xmlns:iso19115="http://schemas.opengis.net/iso19115full"
   xmlns:ows="http://www.opengis.net/ows"
   xmlns:ogc="http://www.opengis.net/ogc"
   xmlns:gmd="http://www.isotc211.org/2005/gmd"
   xmlns:gco="http://www.isotc211.org/2005/gco"
   xmlns:dc="http://purl.org/dc/elements/1.1/"
   xmlns:dct="http://purl.org/dc/terms/"
   xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
   xmlns:xlink="http://www.w3.org/1999/xlink"
   xmlns:xi="http://www.w3.org/2001/XInclude">

  <test name="csw:level1.6">
    <param name="csw.GetCapabilities.document"/>
    <assertion>Run tests for level 1.6 compliance.</assertion>
    <code>

      <xsl:variable name="csw.GetCapabilities.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.get.url">
        <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Get/@xlink:href"/>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecords.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecords']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.GetRecordById.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='GetRecordById']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.post.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='XML']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="csw.DescribeRecord.soap.url">
        <xsl:choose>
          <xsl:when test="boolean($csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href)">
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post[ows:Constraint/ows:Value='SOAP']/@xlink:href"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$csw.GetCapabilities.document//ows:OperationsMetadata/ows:Operation[@name='DescribeRecord']/ows:DCP/ows:HTTP/ows:Post/@xlink:href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <call-test name="csw:CorrectnessOfResult.GetCapabilities-INSPIRECapabilitiesExtension">
        <with-param name="csw.GetCapabilities.get.url" select="$csw.GetCapabilities.get.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-ConditionApplyingToAccessAndUseFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-LimitationsOnPublicAccessANDFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-LimitationsOnPublicAccessANDORFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-DegreeFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-LineageFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

      <call-test name="csw:INSPIREConformance.GetRecords-ComplexANDORFilter">
        <with-param name="csw.GetRecords.soap.url" select="$csw.GetRecords.soap.url"/>
      </call-test>

    </code>
  </test>

  <test name="csw:CorrectnessOfResult.GetCapabilities-INSPIRECapabilitiesExtension">
    <param name="csw.GetCapabilities.get.url"/>
    <assertion>
      The response to a GetCapabilities request (HTTP/GET request where KVP´s  must be defined as follows: service = “CSW”, request = “GetCapabilities) must satisfy the additional assertion:
      1. all INSPIRE queryables shall be listed in the section (Constraint) “AdditionalQueryables”, as in:
      &lt;ows:Constraint name="AdditionalQueryables"&gt;
        &lt;ows:Value&gt;Degree&lt;/ows:Value&gt;
        &lt;ows:Value&gt;AccessConstraints&lt;/ows:Value&gt;
        &lt;ows:Value&gt;OtherConstraints&lt;/ows:Value&gt;
        &lt;ows:Value&gt;Classification&lt;/ows:Value&gt;
        &lt;ows:Value&gt;ConditionApplyingToAccessAndUse&lt;/ows:Value&gt;
        &lt;ows:Value&gt;Lineage&lt;/ows:Value&gt;
        &lt;ows:Value&gt;SpecificationTitle&lt;/ows:Value&gt;
        &lt;ows:Value&gt;SpecificationDate&lt;/ows:Value&gt;
        &lt;ows:Value&gt;SpecificationDateType&lt;/ows:Value&gt;
      &lt;/ows:Constraint&gt;
    </assertion>
    <comment>Pass if the response of the GetRecords requests (sent via
      HTTP/GET) hold the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetCapabilities.get.url"/>
          </url>
          <method>GET</method>
          <param name="service">CSW</param>
          <param name="request">GetCapabilities</param>
        </request>
      </xsl:variable>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'Degree'])">
        <message>FAILURE: the assertion 1 failed (value Degree is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'AccessConstraints'])">
        <message>FAILURE: the assertion 1 failed (value AccessConstraints is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'OtherConstraints'])">
        <message>FAILURE: the assertion 1 failed (value OtherConstraints is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'Classification'])">
        <message>FAILURE: the assertion 1 failed (value Classification is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'ConditionApplyingToAccessAndUse'])">
        <message>FAILURE: the assertion 1 failed (value ConditionApplyingToAccessAndUse is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'Lineage'])">
        <message>FAILURE: the assertion 1 failed (value Lineage is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'SpecificationTitle'])">
        <message>FAILURE: the assertion 1 failed (value SpecificationTitle is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'SpecificationDate'])">
        <message>FAILURE: the assertion 1 failed (value SpecificationDate is missing)</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not($response//ows:Constraint[@name='AdditionalQueryables']/ows:Value[text() = 'SpecificationDateType'])">
        <message>FAILURE: the assertion 1 failed (value SpecificationDateType is missing)</message>
        <fail/>
      </xsl:if>
    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-ConditionApplyingToAccessAndUseFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which useLimitation attribute value satisfies the correct INSPIRE default (“no conditions apply”)  when no conditions apply. (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘summary’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">summary</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:language</ogc:PropertyName>
                            <ogc:Literal>ger</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:ConditionApplyingToAccessAndUse</ogc:PropertyName>
                            <ogc:Literal>no conditions apply</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-LimitationsOnPublicAccessANDFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which AccessConstraints are equal to “otherRestrictions”, which OtherConstraints are defined “no limitations” and which Classification is “unclassified” (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
		<GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
		  <Query typeNames="gmd:MD_Metadata">
		    <ElementSetName typeNames="">brief</ElementSetName>
		    <Constraint version="1.1.0">
		      <ogc:Filter>
			<ogc:And>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:language</ogc:PropertyName>
			    <ogc:Literal>ger</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:type</ogc:PropertyName>
			    <ogc:Literal>dataset</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:AccessConstraints</ogc:PropertyName>
			    <ogc:Literal>otherRestrictions</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			  <ogc:PropertyIsEqualTo>
			    <ogc:PropertyName>apiso:OtherConstraints</ogc:PropertyName>
			    <ogc:Literal>no limitations</ogc:Literal>
			  </ogc:PropertyIsEqualTo>
			</ogc:And>
		      </ogc:Filter>
		    </Constraint>
		  </Query>
		</GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-LimitationsOnPublicAccessANDORFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which Classification is “unclassified” and either the AccessConstraints are equal to “otherRestrictions” (and OtherConstraints are defined “no limitations”) or where the AccessConstraints are equal to “intellectualPropertyRights” (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="10">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:language</ogc:PropertyName>
                            <ogc:Literal>ger</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:Classification</ogc:PropertyName>
                            <ogc:Literal>unclassified</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Or>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:AccessConstraints</ogc:PropertyName>
                                <ogc:Literal>otherRestrictions</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:OtherConstraints</ogc:PropertyName>
                                <ogc:Literal>no limitations</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:AccessConstraints</ogc:PropertyName>
                              <ogc:Literal>intellectualPropertyRights</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          </ogc:Or>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-DegreeFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which are conformant to the “INSPIRE Data Specification on Hydrography” data specification (a version published after the 01.12.2008) (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 1 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="5">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">full</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:language</ogc:PropertyName>
                            <ogc:Literal>ger</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:SpecificationTitle</ogc:PropertyName>
                            <ogc:Literal>INSPIRE Data Specification on Hydrography</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsGreaterThanOrEqualTo>
                            <ogc:PropertyName>apiso:SpecificationDate</ogc:PropertyName>
                            <ogc:Literal>2008-12-01</ogc:Literal>
                          </ogc:PropertyIsGreaterThanOrEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:SpecificationDateType</ogc:PropertyName>
                            <ogc:Literal>publication</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:Degree</ogc:PropertyName>
                            <ogc:Literal>true</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;0)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-LineageFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for dataset metadata records which Lineage statement includes the name “DLM” (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response includes 3 ‘brief’ metadata entry returned in the http://www.isotc211.org/2005/gmd format
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate each MD_Metadata entry in the Namespace “http://www.isotc211.org/2005/gmd” separate with http://schemas.opengis.net/csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd
         b. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="20">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:language</ogc:PropertyName>
                            <ogc:Literal>ger</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:type</ogc:PropertyName>
                            <ogc:Literal>dataset</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:Lineage</ogc:PropertyName>
                            <ogc:Literal>*DLM*</ogc:Literal>
                          </ogc:PropertyIsLike>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)&gt;2)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <message>Testing <xsl:value-of select="count($response//gmd:MD_Metadata)" /> gmd:MD_Metadata elements.</message>

      <xsl:for-each select="$response//gmd:MD_Metadata">
        <call-test name="ctl:XMLValidatingParser">
          <with-param name="doc"><xsl:copy-of select="."/></with-param>
          <with-param name="instruction">
            <parsers:schemas>
              <parsers:schema type="resource">csw/2.0.2/profiles/apiso/1.0.0/apiso.xsd</parsers:schema>
            </parsers:schemas>
          </with-param>
        </call-test>
      </xsl:for-each>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

  <test name="csw:INSPIREConformance.GetRecords-ComplexANDORFilter">
    <param name="csw.GetRecords.soap.url"/>
    <assertion>
      Search for metadata records (no services) which touch the Münsterland area, which Classification is “unclassified” and either the AccessConstraints are equal to “otherRestrictions” (and OtherConstraints are defined “no limitations”) or where the AccessConstraints are equal to “intellectualPropertyRights”, which are conformant to the “INSPIRE Data Specification on Hydrography” data specification (the version published at the 19.12.2008) and which Lineage statement includes the name “DLM” (s. query below).
      The response of the GetRecords request must satisfy the applicable assertions:
      1. the filter request is understood by the server and no exception concerning the request is thrown
      2. the response should state that 1 metadata entry is matched but no metadata entry is returned
      3. the XML representation of the response is valid structured concerning the CSW 2.0.2 AP ISO 1.0 schemas. This response can be validated in two steps:
         a. validate the CSW 2.0.2 response frame (the GetRecordsResponse element within the “http://www.opengis.net/cat/csw/2.0.2” namespace) with http://schemas.opengis.net/csw/2.0.2/CSW-discovery.xsd
    </assertion>
    <comment>Pass if the response of the GetRecords request (sent via
      HTTP/SOAP/POST/XML) holds the relevant assertions.</comment>
    <code>
      <xsl:variable name="response">
        <request>
          <url>
            <xsl:value-of select="$csw.GetRecords.soap.url"/>
          </url>
          <method>POST</method>
          <header name="action">urn:unused</header>
          <header name="SOAPAction">urn:unused</header>
          <header name="Content-Type">application/soap+xml</header>
          <body>
            <soap:Envelope xmlns:soap="http://www.w3.org/2003/05/soap-envelope">
              <soap:Header />
              <soap:Body>
                <GetRecords xmlns="http://www.opengis.net/cat/csw/2.0.2" xmlns:ogc="http://www.opengis.net/ogc" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:apiso="http://www.opengis.net/cat/csw/apiso/1.0" xmlns:ows="http://www.opengis.net/ows" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dct="http://purl.org/dc/terms/" xmlns:gml="http://www.opengis.net/gml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" service="CSW" version="2.0.2" resultType="results" outputFormat="application/xml" outputSchema="http://www.isotc211.org/2005/gmd" startPosition="1" maxRecords="0">
                  <Query typeNames="gmd:MD_Metadata">
                    <ElementSetName typeNames="">brief</ElementSetName>
                    <Constraint version="1.1.0">
                      <ogc:Filter>
                        <ogc:And>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:language</ogc:PropertyName>
                            <ogc:Literal>ger</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:Not>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:type</ogc:PropertyName>
                              <ogc:Literal>service</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          </ogc:Not>
                          <ogc:BBOX>
                            <ogc:PropertyName>apiso:BoundingBox</ogc:PropertyName>
                            <gml:Envelope srsName="urn:ogc:def:crs:EPSG:7.4:4326">
                              <gml:lowerCorner>7.30 51.80</gml:lowerCorner>
                              <gml:upperCorner>7.50 52.10</gml:upperCorner>
                            </gml:Envelope>
                          </ogc:BBOX>
                          <ogc:Or>
                            <ogc:And>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:AccessConstraints</ogc:PropertyName>
                                <ogc:Literal>otherRestrictions</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                              <ogc:PropertyIsEqualTo>
                                <ogc:PropertyName>apiso:OtherConstraints</ogc:PropertyName>
                                <ogc:Literal>no limitations</ogc:Literal>
                              </ogc:PropertyIsEqualTo>
                            </ogc:And>
                            <ogc:PropertyIsEqualTo>
                              <ogc:PropertyName>apiso:AccessConstraints</ogc:PropertyName>
                              <ogc:Literal>intellectualPropertyRights</ogc:Literal>
                            </ogc:PropertyIsEqualTo>
                          </ogc:Or>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:ConditionApplyingToAccessAndUse</ogc:PropertyName>
                            <ogc:Literal>no conditions apply</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:SpecificationTitle</ogc:PropertyName>
                            <ogc:Literal>INSPIRE Data Specification on Hydrography</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:SpecificationDate</ogc:PropertyName>
                            <ogc:Literal>2008-12-19</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:SpecificationDateType</ogc:PropertyName>
                            <ogc:Literal>publication</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsEqualTo>
                            <ogc:PropertyName>apiso:Degree</ogc:PropertyName>
                            <ogc:Literal>true</ogc:Literal>
                          </ogc:PropertyIsEqualTo>
                          <ogc:PropertyIsLike escapeChar="\" singleChar="?" wildCard="*">
                            <ogc:PropertyName>apiso:Lineage</ogc:PropertyName>
                            <ogc:Literal>*DLM*</ogc:Literal>
                          </ogc:PropertyIsLike>
                        </ogc:And>
                      </ogc:Filter>
                    </Constraint>
                  </Query>
                </GetRecords>
              </soap:Body>
            </soap:Envelope>
          </body>
        </request>
      </xsl:variable>

      <xsl:if test="boolean($response/soap:Envelope/soap:Body/ogc:ExceptionReport)">
        <message>FAILURE: the first assertion failed</message>
        <fail/>
      </xsl:if>

      <xsl:if test="not(count($response//gmd:MD_Metadata)=0 and $response/soap:Envelope/soap:Body/csw2:GetRecordsResponse/csw2:SearchResults/@numberOfRecordsMatched=1)">
        <message>FAILURE: the second assertion failed</message>
        <fail/>
      </xsl:if>

      <call-test name="ctl:XMLValidatingParser">
        <with-param name="doc"><xsl:apply-templates select="$response/soap:Envelope/soap:Body/csw2:GetRecordsResponse"/></with-param>
        <with-param name="instruction">
          <parsers:schemas>
            <parsers:schema type="resource">csw/2.0.2/CSW-discovery.xsd</parsers:schema>
          </parsers:schemas>
        </with-param>
      </call-test>

    </code>
  </test>

</package>
