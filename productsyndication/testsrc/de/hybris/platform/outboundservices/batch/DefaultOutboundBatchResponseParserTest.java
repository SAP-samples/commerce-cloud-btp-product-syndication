package de.hybris.platform.outboundservices.batch;

import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.odata2services.converter.DefaultResponseEntityToODataResponseConverter;
import de.hybris.platform.odata2services.converter.HttpHeaderToMapConverter;

@UnitTest
public class DefaultOutboundBatchResponseParserTest {

	String body = """
--batch_300b476e-f92b-4622-b628-3c5e0bee69f3\r
Content-Type: application/http\r
Content-Transfer-Encoding: binary\r
\r
HTTP/1.1 400 Bad Request\r
DataServiceVersion: 2.0\r
Location: 300427245|Online|apparelProductCatalog\r
Content-Type: application/json\r
Content-Length: 127\r
\r
{"Required_field_error":"Validation unsuccessful -  ean,mpn values missing for product 300427245|Online|apparelProductCatalog"}\r
--batch_300b476e-f92b-4622-b628-3c5e0bee69f3\r
Content-Type: application/http\r
Content-Transfer-Encoding: binary\r
\r
HTTP/1.1 400 Bad Request\r
DataServiceVersion: 2.0\r
Location: 300427249|Online|apparelProductCatalog\r
Content-Type: application/json\r
Content-Length: 127\r
\r
{"Required_field_error":"Validation unsuccessful -  ean,mpn values missing for product 300427249|Online|apparelProductCatalog"}\r
--batch_300b476e-f92b-4622-b628-3c5e0bee69f3--""";


	de.hybris.platform.odata2services.converter.HttpHeaderToMapConverter headerToMapConverter = new HttpHeaderToMapConverter();

	DefaultResponseEntityToODataResponseConverter converter = new DefaultResponseEntityToODataResponseConverter(headerToMapConverter);

	DefaultOutboundBatchResponseParser testObj = new DefaultOutboundBatchResponseParser(converter);

	@Test(expected = NullPointerException.class)
	public void test_content_type_lowercase_olingo_internal_error_related_to_case_sensitivity_of_http_headers() {
		HttpHeaders headers = new HttpHeaders();

		headers
				.add("cache-control", "no-cache, no-store, max-age=0, must-revalidate");
		headers
				.add("content-type", "multipart/mixed; boundary=batch_300b476e-f92b-4622-b628-3c5e0bee69f3");
		headers
				.add("dataserviceversion", "2.0");
		headers
				.add("x-request-id", "31df36af-7013-4c90-8965-80e4e0c39f0a");
		headers
				.add("x-xss-protection", "1; mode=block");
		ResponseEntity<String> mockResponse = new ResponseEntity<>(body, headers, HttpStatus.ACCEPTED);
		List<ResponseEntity<Map>> result = testObj.parseMultiPartResponse(mockResponse);
		Assert.assertNotNull(result);
	}

	@Test
	public void test_content_type_camelcase_olingo_internal_error_related_to_case_sensitivity_of_http_headers() {
		HttpHeaders headers = new HttpHeaders();

		headers
				.add("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
		headers
				.add("Content-Type", "multipart/mixed; boundary=batch_300b476e-f92b-4622-b628-3c5e0bee69f3");
		headers
				.add("dataserviceversion", "2.0");
		headers
				.add("x-request-id", "31df36af-7013-4c90-8965-80e4e0c39f0a");
		headers
				.add("x-xss-protection", "1; mode=block");
		ResponseEntity<String> mockResponse = new ResponseEntity<>(body, headers, HttpStatus.ACCEPTED);
		List<ResponseEntity<Map>> result = testObj.parseMultiPartResponse(mockResponse);
		Assert.assertNotNull(result);
	}
}
