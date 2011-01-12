<?xml version="1.0" encoding="UTF-8"?>
<sch:schema xmlns:sch="http://www.ascc.net/xml/schematron" 
  defaultPhase="DefaultPhase" 
  version="1.5">
  
  <sch:title>Rules for WFS-1.1.0 TransactionResponse entities.</sch:title>
  
  <sch:ns prefix="wfs" uri="http://www.opengis.net/wfs"/>
  
  <sch:phase id="DefaultPhase">
    <sch:active pattern="TransactionResponsePattern"/>
  </sch:phase>
  
  <sch:pattern id="TransactionResponsePattern" name="TransactionResponsePattern">
    <sch:p xml:lang="en">Checks that the document element is wfs:TransactionResponse.</sch:p>
    <sch:rule id="docElement" context="/">
      <sch:assert id="docElement.infoset" 
        test="wfs:TransactionResponse"
        diagnostics="includedDocElem">
	The document element must have [local name] = "TransactionResponse" and [namespace name] = "http://www.opengis.net/wfs".
      </sch:assert>
       <sch:assert id="summary" 
        test="count(wfs:TransactionResponse/wfs:TransactionSummary/*) > 0">
	The wfs:TransactionSummary element must not be empty.
      </sch:assert>
      <sch:assert id="totalInserted" 
        test="count(wfs:TransactionResponse/wfs:InsertResults/*) = wfs:TransactionResponse/wfs:TransactionSummary/wfs:totalInserted"
        diagnostics="insertCount">
	The number of child elements of wfs:InsertResults does not equal the reported value of wfs:totalInserted.
      </sch:assert>
    </sch:rule>
  </sch:pattern>
  
  <sch:diagnostics>
    <sch:diagnostic id="includedDocElem">
    The included document element has [local name] = <sch:value-of select="local-name(/*[1])"/> 
    and [namespace name] = <sch:value-of select="namespace-uri(/*[1])"/>.
    </sch:diagnostic>
    <sch:diagnostic id="insertCount">
    Reported total number of features inserted: <sch:value-of select="wfs:TransactionResponse/wfs:TransactionSummary/wfs:totalInserted"/>.
    </sch:diagnostic>
    <sch:diagnostic id="updateCount">
    Reported total number of features updated: <sch:value-of select="wfs:TransactionSummary/wfs:totalUpdated"/>.
    </sch:diagnostic>
    <sch:diagnostic id="deleteCount">
    Reported total number of features deleted: <sch:value-of select="wfs:TransactionSummary/wfs:totalDeleted"/>.
    </sch:diagnostic>
  </sch:diagnostics>
</sch:schema>
