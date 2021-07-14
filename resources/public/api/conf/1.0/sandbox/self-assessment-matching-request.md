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
        <td>As outlined in the valid payload.</td>
        <td><p>200 (OK)</p><p>Payload as response example above</p></td>
    </tr>
    <tr>
        <td><p>No match</p></td>
        <td>
            <p>There is no match for the information provided.</p>
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
                    postcode
            </p>
          </td>
          <td><p>There is at least one required field missing.</p></td>
          <td><p>400 (Bad Request)</p>
          <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;&#60;field_name&#62; is required&quot; }</p></td>
    </tr>
    <tr>
        <td><p>Malformed selfAssessmentUniqueTaxPayerRef</p></td>
        <td><p>Any selfAssessmentUniqueTaxPayerRef that is not in the correct format.</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed SAUTR submitted&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Missing CorrelationId</p></td>
        <td><p>CorrelationId is missing. Check the request headers section for what should be included.</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;CorrelationId is required&quot; }</p></td>
        </td>
    </tr>
    <tr>
        <td><p>Malformed CorrelationId</p></td>
        <td><p>CorrelationId is not in the correct format. Check the request headers section for the correct format.</p></td>
        <td>
            <p>400 (Bad Request)</p>
            <p>{ &quot;code&quot; : &quot;INVALID_REQUEST&quot;,<br/>&quot;message&quot; : &quot;Malformed CorrelationId&quot; }</p></td>
        </td>
    </tr>
  </tbody>
</table>
