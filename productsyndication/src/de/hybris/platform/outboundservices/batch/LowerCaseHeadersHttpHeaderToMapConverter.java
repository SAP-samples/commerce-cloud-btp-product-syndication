package de.hybris.platform.outboundservices.batch;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import de.hybris.platform.odata2services.converter.HttpHeaderToMapConverter;
import io.vavr.control.Try;

/**
 * Converter to fix lower-case Http headers caused by Istio in our u-service landscape.
 * Istio for some reason lower-cases all http param headers with no reason.
 * As an explanation they wrote, that standard says these params are case-insensitive.
 * That is a fix, to make it work again with odata Apache Olingo v2 library.
 * 
 * replace @spring.bean {@link httpHeaderToMapConverter}
 * @spring.bean {@link lowerCaseHeadersHttpHeaderToMapConverter}
 *
 * @author i303764
 */
public class LowerCaseHeadersHttpHeaderToMapConverter extends HttpHeaderToMapConverter {

	private final Set<String> headerConstants;

	public LowerCaseHeadersHttpHeaderToMapConverter() throws IllegalAccessException {
		this.headerConstants = LowerCaseHeadersHttpHeaderToMapConverter.getDeclaredFields();
	}

	/**
	 * Method havests <b>nice</b> HTTP headers
	 *
	 * @return
	 * @throws IllegalAccessException
	 */
	public static Set<String> getDeclaredFields() throws IllegalAccessException {
		Class<HttpHeaders> cls = HttpHeaders.class;
		Set<String> httpValues = new HashSet<>();
		Field[] fields = cls.getDeclaredFields();
		Set<Field> httpFields = Arrays.stream(fields)
									  .filter(f -> String.class.equals(f.getType()))
									  .filter(f -> Modifier.isPublic(f.getModifiers()))
									  .filter(f -> Modifier.isStatic(f.getModifiers()))
									  .collect(Collectors.toSet());
		for (Field i : httpFields) {
			String http = Try.of(() -> (String) i.get(null))
							 .getOrElse(() -> null);
			if (http != null) {
				httpValues.add(http);
			}
		}
		return httpValues;
	}

	/**
	 * method checks if given header param is in list of constant params, if it is, it returns camel case version, otherwise return input value.
	 *
	 * @param inputHeader which has to be checked if that is one of initial http headers from documentation.
	 * @return
	 */
	public String camelCaseHttpHeader(String inputHeader) {
		return headerConstants.stream()
							  .filter(a -> a.equalsIgnoreCase(inputHeader))
							  .findFirst()
							  .orElse(inputHeader);
	}

	public HttpHeaders camelCaseHttpHeaders(HttpHeaders inputHeaders) {
		LinkedMultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		inputHeaders.forEach((key, value) -> headerMap.putIfAbsent(camelCaseHttpHeader(key), value));
		return new HttpHeaders(headerMap);
	}

	@Override
	public Map<String, String> convert(HttpHeaders headers) {
		return super.convert(camelCaseHttpHeaders(headers));
	}
}
