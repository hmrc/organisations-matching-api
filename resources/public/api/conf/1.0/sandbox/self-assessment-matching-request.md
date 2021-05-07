<table>
    <col width="100%">
    <thead>
    <tr>
        <th>Valid payload</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td>
            <p>{&quot;selfAssessmentUniqueTaxPayerRef&quot;:&quot;1234567890&quot;,
                &quot;taxPayerType&quot;:&quot;Partnership&quot;,
                &quot;taxPayerName&quot;:&quot;Example Partnership&quot;,
                &quot;address&quot;:{
                    &quot;addressLine1&quot;:&quot;123 Long Road&quot;,
                    &quot;addressLine2&quot;:&quot;Some City&quot;,
                    &quot;addressLine3&quot;:&quot;Some County&quot;,
                    &quot;addressLine4&quot;:&quot;&quot;,
                    &quot;postcode&quot;:&quot;AB12 3CD&quot}
                }
            </p>
        </td>
    </tr>
    </tbody>
</table>

<table>
    <col width="25%">
    <col width="35%">
    <col width="40%">
    <thead>
    <tr>
        <th>Scenario</th>
        <th>Payload</th>
        <th>Response</th>
    </tr>
    </thead>
    <tbody>
    <tr>
        <td><p>Successful match</p>
        <td><p>selfAssessmentUniqueTaxPayerRef = &quot;1234567890&quot;
            <br/>taxPayerType = &quot;Partnership&quot;
            <br/>taxPayerName = &quot;Example Partnership&quot;
            <br/>addressLine1 = &quot;123 Long Road&quot;
            <br/>addressLine2 = &quot;Some City&quot;
            <br/>addressLine3 = &quot;Some County&quot;
            <br/>addressLine4 = &quot;&quot;
            <br/>postcode = &quot;AB12 3CD&quot;</p></td>
        <td><p>200 (OK)</p><p>Payload as response example above</p></td>
    </tr>
    <tr>
        <td><p>No match</p></td>
        <td>
            <p>Any details that are not an exact match.</p>
        </td>
        <td><p>403 (Forbidden)</p>
        <p>{ &quot;code&quot; : &quot;MATCHING_FAILED&quot;,<br/>&quot;message&quot; : &quot;There is no match for the information provided&quot; }</p></td>
    </tr>
    <tr>
          <td>
            <p>Missing selfAssessmentUniqueTaxPayerRef &#47; 
                    taxPayerType &#47; 
                    taxPayerName &#47;
                    addressLine1 &#47; 
                    addressLine2 &#47;
                    postcode
            </p>
          </td>
          <td><p>Any field missing</p></td>
          <td><p>400 (Bad Request)</p>
          <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;&#60;field_name&#62; is required&quot; }</p></td>
    </tr>
    <tr>
        <td><p>Malformed selfAssessmentUniqueTaxPayerRef</p></td>
        <td><p>Any SAUTR that does not meet the validation rule</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed SAUTR submitted&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Missing CorrelationId</p></td>
        <td><p>CorrelationId header is missing</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;CorrelationId is required&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Malformed CorrelationId</p></td>
        <td><p>CorrelationId header is malformed</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed CorrelationId&quot; }</p></td>
        </td>
    </tr>
  </tbody>
</table>