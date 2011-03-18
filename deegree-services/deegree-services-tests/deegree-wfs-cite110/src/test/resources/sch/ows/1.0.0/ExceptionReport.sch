<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
  defaultPhase="DefaultPhase" 
  version="1.5">
  
  <sch:title>Rules for exception reporting.</sch:title>
  
  <sch:ns prefix="ows" uri="http://www.opengis.net/ows"/>
  
  <sch:phase id="DefaultPhase">
    <sch:active pattern="ExceptionReportPattern"/>
  </sch:phase>  
  
  <sch:phase id="VersionNegotiationFailedPhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="VersionNegotiationFailedPattern"/>
  </sch:phase>

  <sch:phase id="MissingParameterValuePhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="MissingParameterValuePattern"/>
  </sch:phase>

  <sch:phase id="InvalidParameterValuePhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="InvalidParameterValuePattern"/>
  </sch:phase>

  <sch:phase id="InvalidUpdateSequencePhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="InvalidUpdateSequencePattern"/>
  </sch:phase>

  <sch:phase id="OperationNotSupportedPhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="OperationNotSupportedPattern"/>
  </sch:phase>

  <sch:phase id="NoApplicableCodePhase">
    <sch:active pattern="ExceptionReportPattern"/>
    <sch:active pattern="NoApplicableCodePattern"/>
  </sch:phase>

  <sch:pattern id="ExceptionReportPattern" name="ExceptionReportPattern">
    <sch:p xml:lang="en">Checks that the document is an OWS exception report.</sch:p>
    <sch:rule id="ExceptionReport" context="/">
      <sch:assert id="ExceptionReport.infoset" 
        test="ows:ExceptionReport"
        diagnostics="includedDocElem">
	The document element must have [local name] = "ExceptionReport" and [namespace name] = "http://www.opengis.net/ows".
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="VersionNegotiationFailedPattern" name="VersionNegotiationFailedPattern">
    <sch:p xml:lang="en">Checks for the VersionNegotiationFailed exception code.</sch:p>
    <sch:rule id="VersionNegotiationFailed" context="/ows:ExceptionReport">
      <sch:assert id="VersionNegotiationFailed.code" 
        test="ows:Exception/@exceptionCode = 'VersionNegotiationFailed'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "VersionNegotiationFailed".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="MissingParameterValuePattern" name="MissingParameterValuePattern">
    <sch:p xml:lang="en">Checks for the MissingParameterValue exception code.</sch:p>
    <sch:rule id="MissingParameterValue" context="/ows:ExceptionReport">
      <sch:assert id="MissingParameterValue.code" 
        test="ows:Exception/@exceptionCode = 'MissingParameterValue'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "MissingParameterValue".
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:pattern id="InvalidParameterValuePattern" name="InvalidParameterValuePattern">
    <sch:p xml:lang="en">Checks for the InvalidParameterValue exception code.</sch:p>
    <sch:rule id="InvalidParameterValue" context="/ows:ExceptionReport">
      <sch:assert id="InvalidParameterValue.code" 
        test="ows:Exception/@exceptionCode = 'InvalidParameterValue'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "InvalidParameterValue".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="InvalidUpdateSequencePattern" name="InvalidUpdateSequencePattern">
    <sch:p xml:lang="en">Checks for the InvalidUpdateSequence exception code.</sch:p>
    <sch:rule id="InvalidUpdateSequence" context="/ows:ExceptionReport">
      <sch:assert id="InvalidUpdateSequence.code" 
        test="ows:Exception/@exceptionCode = 'InvalidUpdateSequence'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "InvalidUpdateSequence".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="OperationNotSupportedPattern" name="OperationNotSupportedPattern">
    <sch:p xml:lang="en">Checks for the OperationNotSupported exception code.</sch:p>
    <sch:rule id="OperationNotSupported" context="/ows:ExceptionReport">
      <sch:assert id="OperationNotSupported.code" 
        test="ows:Exception/@exceptionCode = 'OperationNotSupported'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "OperationNotSupported".
      </sch:assert>
    </sch:rule>
  </sch:pattern>

  <sch:pattern id="NoApplicableCodePattern" name="NoApplicableCodePattern">
    <sch:p xml:lang="en">Checks for the NoApplicableCode exception code.</sch:p>
    <sch:rule id="NoApplicableCode" context="/ows:ExceptionReport">
      <sch:assert id="NoApplicableCode.code" 
        test="ows:Exception/@exceptionCode = 'NoApplicableCode'"
        diagnostics="includedCode">
	The @exceptionCode attribute must have the value "NoApplicableCode".
      </sch:assert>
    </sch:rule>
  </sch:pattern> 
  
  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem">
    The included document element has [local name] = <sch:value-of select="local-name(/*[1])"/> 
    and [namespace name] = <sch:value-of select="namespace-uri(/*[1])"/>.
    </sch:diagnostic>
    <sch:diagnostic id="includedCode">
    The included exception code is: <sch:value-of select="ows:Exception/@exceptionCode"/>.
    </sch:diagnostic>
    <sch:diagnostic id="includedLocator">
    The included locator is: <sch:value-of select="ows:Exception/@locator"/>.
    </sch:diagnostic>    
  </sch:diagnostics>
  
</sch:schema>