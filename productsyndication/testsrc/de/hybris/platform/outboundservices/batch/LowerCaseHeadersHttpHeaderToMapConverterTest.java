package de.hybris.platform.outboundservices.batch;


import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import de.hybris.bootstrap.annotations.UnitTest;

@UnitTest
public class LowerCaseHeadersHttpHeaderToMapConverterTest  {

    @Test
    public void testCamelCaseHttpHeader() throws IllegalAccessException {
        LowerCaseHeadersHttpHeaderToMapConverter testObj = new LowerCaseHeadersHttpHeaderToMapConverter();
        String a = "custom-one";
        String ret = testObj.camelCaseHttpHeader(a);
        Assert.assertEquals(a, ret);
		a = "custom";
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(a, ret);
		a = HttpHeaders.ACCEPT;
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(a, ret);
		a = HttpHeaders.ACCEPT.toLowerCase();
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(HttpHeaders.ACCEPT, ret);
		a = HttpHeaders.ACCEPT.toUpperCase();
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(HttpHeaders.ACCEPT, ret);
    }

	@Test
	public void testCurrentState() throws IllegalAccessException {
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
		LowerCaseHeadersHttpHeaderToMapConverter testObj = new LowerCaseHeadersHttpHeaderToMapConverter();
		HttpHeaders improvedHeaders = testObj.camelCaseHttpHeaders(headers);
		Assert.assertNotNull(improvedHeaders);
		Assert.assertTrue(improvedHeaders.containsKey(HttpHeaders.CACHE_CONTROL));
		Assert.assertTrue(improvedHeaders.containsKey(HttpHeaders.CONTENT_TYPE));
		Assert.assertTrue(improvedHeaders.containsKey("dataserviceversion"));
		Assert.assertTrue(improvedHeaders.containsKey("x-request-id"));
		Assert.assertTrue(improvedHeaders.containsKey("x-xss-protection"));

	}

	@Test
	public void testParseMultiPartResponse() throws IllegalAccessException {
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
		LowerCaseHeadersHttpHeaderToMapConverter testObj = new LowerCaseHeadersHttpHeaderToMapConverter();
		HttpHeaders improvedHeaders = testObj.camelCaseHttpHeaders(headers);
		Assert.assertNotNull(improvedHeaders);
		Assert.assertTrue(improvedHeaders.containsKey(HttpHeaders.CACHE_CONTROL));
		Assert.assertTrue(improvedHeaders.containsKey(HttpHeaders.CONTENT_TYPE));
		Assert.assertTrue(improvedHeaders.containsKey("dataserviceversion"));
		Assert.assertTrue(improvedHeaders.containsKey("x-request-id"));
		Assert.assertTrue(improvedHeaders.containsKey("x-xss-protection"));
	}

	@Test
	public void testCamelCaseHttpHeaders() throws IllegalAccessException {
		LowerCaseHeadersHttpHeaderToMapConverter testObj = new LowerCaseHeadersHttpHeaderToMapConverter();
		String a = "custom-one";
		String ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(a, ret);
		a = "custom";
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(a, ret);
		a = HttpHeaders.ACCEPT;
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(a, ret);
		a = HttpHeaders.ACCEPT.toLowerCase();
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(HttpHeaders.ACCEPT, ret);
		a = HttpHeaders.ACCEPT.toUpperCase();
		ret = testObj.camelCaseHttpHeader(a);
		Assert.assertEquals(HttpHeaders.ACCEPT, ret);
	}

}
