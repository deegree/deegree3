<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron"
  defaultPhase="DefaultPhase"
  version="1.5">

  <sch:title>Rules for WFS-1.1.0 GetCapabilities response entities.</sch:title>

        <sch:ns prefix="wfs" uri="http://www.opengis.net/wfs"/>
        <sch:ns prefix="ows" uri="http://www.opengis.net/ows"/>
        <sch:ns prefix="ogc" uri="http://www.opengis.net/ogc"/>

  <sch:phase id="DefaultPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="OptionalElementsPattern"/>
  </sch:phase>

  <sch:phase id="RequiredBasicElementsPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="OptionalElementsPattern"/>
    <sch:active pattern="CRSReferencesPattern"/>
    <sch:active pattern="RequiredBasicBindingsPattern"/>
    <sch:active pattern="RequiredOperatorsPattern"/>
  </sch:phase>

  <sch:phase id="RequiredTransactionBindingsPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="OptionalElementsPattern"/>
    <sch:active pattern="CRSReferencesPattern"/>
    <sch:active pattern="RequiredBasicBindingsPattern"/>
    <sch:active pattern="RequiredOperatorsPattern"/>
    <sch:active pattern="RequiredTransactionBindingsPattern"/>
  </sch:phase>

  <sch:phase id="RequiredOperatorsPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="RequiredOperatorsPattern"/>
  </sch:phase>

  <sch:phase id="AbbreviatedContentPhase">
    <sch:active pattern="CapabilitiesPattern"/>
    <sch:active pattern="AbbreviatedContentPattern"/>
  </sch:phase>

  <sch:pattern id="CapabilitiesPattern" name="CapabilitiesPattern">
    <sch:p xml:lang="en">Checks that the document is a WFS v1.1.0 capabilities document.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset"
        test="wfs:WFS_Capabilities"
        diagnostics="includedDocElem">
        The document element must have [local name] = "WFS_Capabilities" and [namespace name] = "http://www.opengis.net/wfs".
      </sch:assert>
      <sch:assert id="docElement.version"
        test="wfs:WFS_Capabilities/@version='1.1.0'">
        The @version attribute must have the value "1.1.0".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredBasicBindingsPattern" name="RequiredBasicBindingsPattern">
    <sch:p xml:lang="en">
    Checks that all HTTP method bindings required for WFS-Basic conformance are present.
    </sch:p>
    <sch:rule id="RequiredBasicBindings" context="/wfs:WFS_Capabilities">
      <sch:assert id="GetCapabilities-GET"
        test="ows:OperationsMetadata/ows:Operation[@name='GetCapabilities']/ows:DCP/ows:HTTP/ows:Get">
        Missing mandatory binding for GetCapabilities using the GET method.
      </sch:assert>
      <sch:assert id="DescribeFeatureType"
        test="//ows:Operation[@name='DescribeFeatureType']/ows:DCP/ows:HTTP">
        Missing mandatory binding for DescribeFeatureType using the POST or GET method.
      </sch:assert>
      <sch:assert id="GetFeature"
        test="//ows:Operation[@name='GetFeature']/ows:DCP/ows:HTTP">
        Missing mandatory binding for GetFeature request using the POST or GET method.
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredTransactionBindingsPattern" name="RequiredTransactionBindingsPattern">
    <sch:p xml:lang="en">
    Checks that all HTTP method bindings required for WFS-Transaction conformance are present.
    </sch:p>
    <sch:rule id="RequiredTransactionBindings" context="/wfs:WFS_Capabilities">
      <sch:assert id="Transaction-POST"
        test="//ows:Operation[@name='Transaction']/ows:DCP/ows:HTTP/ows:Post">
        Missing mandatory binding for Transaction requests using the POST method.
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="RequiredOperatorsPattern" name="RequiredOperatorsPattern">
    <sch:p xml:lang="en">
    Checks that the minimal set of filter predicates are supported.
    </sch:p>
    <sch:rule id="RequiredOperators" context="//ogc:Filter_Capabilities">
      <sch:assert id="SpatialOperators.BBOX"
        test="ogc:Spatial_Capabilities/ogc:SpatialOperators/ogc:SpatialOperator[@name='BBOX']">Missing mandatory spatial operator: BBOX.</sch:assert>
      <sch:assert id="Id_Capabilities.EID"
        test="ogc:Id_Capabilities/ogc:EID">Missing mandatory Id operator: EID.</sch:assert>
      <sch:assert id="ComparisonOperators.EqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'EqualTo'">Missing mandatory comparison operator: EqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.NotEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'NotEqualTo'">Missing mandatory comparison operator: NotEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.LessThan"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'LessThan'">Missing mandatory comparison operator: LessThan.</sch:assert>
      <sch:assert id="ComparisonOperators.GreaterThan"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'GreaterThan'">Missing mandatory comparison operator: GreaterThan.</sch:assert>
      <sch:assert id="ComparisonOperators.LessThanEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'LessThanEqualTo'">Missing mandatory comparison operator: LessThanEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.GreaterThanEqualTo"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'GreaterThanEqualTo'">Missing mandatory comparison operator: GreaterThanEqualTo.</sch:assert>
      <sch:assert id="ComparisonOperators.Between"
        test="ogc:Scalar_Capabilities/ogc:ComparisonOperators/ogc:ComparisonOperator = 'Between'">Missing mandatory comparison operator: Between.</sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="OptionalElementsPattern" name="OptionalElementsPattern">
    <sch:p xml:lang="en">Checks for the presence of all optional child elements.</sch:p>
    <sch:rule id="docElement.children" context="/wfs:WFS_Capabilities">
      <sch:assert id="ServiceIdentification"
        test="ows:ServiceIdentification">
        Document is incomplete: the ows:ServiceIdentification element is missing.
      </sch:assert>
      <sch:assert id="ServiceProvider"
        test="ows:ServiceProvider">
        Document is incomplete: the ows:ServiceProvider element is missing.
      </sch:assert>
      <sch:assert id="OperationsMetadata"
        test="ows:OperationsMetadata">
        Document is incomplete: the ows:OperationsMetadata element is missing.
      </sch:assert>
      <sch:assert id="FeatureTypeList"
        test="wfs:FeatureTypeList">
        Document is incomplete: the wfs:FeatureTypeList element is missing.
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="CRSReferencesPattern" name="CRSReferencesPattern">
    <sch:p xml:lang="en">Checks that all CRS references are valid URI values.</sch:p>
    <sch:rule id="CRSReferences" context="//wfs:DefaultSRS">
      <sch:assert id="DefaultSRS" test="starts-with(., 'urn:ogc:def:crs:') or starts-with(., 'http')"
        diagnostics="DefaultSRS.value">
        DefaultSRS value is not a valid URI (invalid scheme name or URN namespace identifier).
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="AbbreviatedContentPattern" name="AbbreviatedContentPattern">
    <sch:p xml:lang="en">
    Checks that all optional top-level elements are NOT included.
    </sch:p>
    <sch:rule id="prune.optional" context="/wfs:WFS_Capabilities">
      <sch:report id="NoServiceIdentification"
        test="ows:ServiceIdentification">
        The ows:ServiceIdentification element is included.
      </sch:report>
      <sch:report id="NoServiceProvider"
        test="ows:ServiceProvider">
        The ows:ServiceProvider element is included.
      </sch:report>
      <sch:report id="NoOperationsMetadata"
        test="ows:OperationsMetadata">
        The ows:OperationsMetadata element is included.
      </sch:report>
      <sch:report id="NoFeatureTypeList"
        test="wfs:FeatureTypeList">
        The wfs:FeatureTypeList element is included.
      </sch:report>
    </sch:rule>
  </sch:pattern>

  <sch:diagnostics>
        <sch:diagnostic id="includedDocElem">
The included document element has [local name] = <sch:value-of select="local-name(/*[1])"/>
and [namespace name] = <sch:value-of select="namespace-uri(/*[1])"/>.
        </sch:diagnostic>
        <sch:diagnostic id="serviceVersion">
The advertised service version is <sch:value-of select="//wfs:WFS_Capabilities/@version"/>.
        </sch:diagnostic>
    <sch:diagnostic id="DefaultSRS.value">
The DefaultSRS is <sch:value-of select="."/>.
        </sch:diagnostic>
</sch:diagnostics>

</sch:schema>