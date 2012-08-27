package token.server.service;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonFilter;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.introspect.VisibilityChecker;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service("jsonHelper")
@Scope("singleton")
public class JsonHelper {
	
	private final ObjectMapper objectMapper;
	
	public JsonHelper() {
		objectMapper = new ObjectMapper();
		
		initSerializationConfig();
		initDeserializationConfig();
	}
	
	private void initSerializationConfig() {
		// features
		SerializationConfig scfg = objectMapper.getSerializationConfig();
		scfg.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
		
		scfg = scfg.withDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
		
		scfg.enable(SerializationConfig.Feature.AUTO_DETECT_FIELDS);
		scfg.enable(SerializationConfig.Feature.FLUSH_AFTER_WRITE_VALUE);
		scfg.enable(SerializationConfig.Feature.USE_ANNOTATIONS);
		scfg.enable(SerializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		
		scfg.disable(SerializationConfig.Feature.AUTO_DETECT_IS_GETTERS);
		scfg.disable(SerializationConfig.Feature.AUTO_DETECT_GETTERS);
		scfg.disable(SerializationConfig.Feature.WRITE_NULL_MAP_VALUES);
		scfg.disable(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS);
		scfg.disable(SerializationConfig.Feature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS);
		
		// @Access(FIELD)
		scfg = scfg.withVisibilityChecker(new VisibilityChecker.Std(
				Visibility.NONE, Visibility.NONE, Visibility.NONE, Visibility.NONE,
				Visibility.ANY));
		
		// filters
		final String[] sensitiveFields = {
				"id",
				"password",
				"deviceId",
				"userId",
				"cardNumber"
		};
		
		final String[] unusedFields = {
				"originator",
				"type",
				"expiredDate",
				"refStan",
				"refRrn",
				"needAmount",
				"hasDenominal",
				"merchantId",
				"transactionId",
				"postProcessExp",
				"batchId",
				"batchName",
				"batchStan",
				"trxChargeAssigned",
				"scheduleLabel",
				"mti",
				"transmissionDateTime",
				"processingCode",
				"persistResponse",
				"productTreeId",
				"copies",
				"needAreaCode",
				"message",
				"previousStatus",
				"chargeIndicatorForIsoF29",
				"treeId",
				"industry",
				"saveToPreRegister",
				"deliveryChannelId",
				"operationCode"
		};
		
		final Set<String> ignoredFields = new HashSet<String>();
		Collections.addAll(ignoredFields, sensitiveFields);
		Collections.addAll(ignoredFields, unusedFields);
		
		SimpleFilterProvider filters = new SimpleFilterProvider();
		filters.addFilter("ignored-fields", SimpleBeanPropertyFilter.serializeAllExcept(ignoredFields));
		
		scfg = scfg.withFilters(filters);
		
		scfg.addMixInAnnotations(Object.class, FilterEnabled.class);

		// set config
		objectMapper.setSerializationConfig(scfg);
	}
	
	private void initDeserializationConfig() {
		// features
		DeserializationConfig dcfg = objectMapper.getDeserializationConfig();
		
		dcfg.disable(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		dcfg.disable(DeserializationConfig.Feature.AUTO_DETECT_SETTERS);
		dcfg.disable(DeserializationConfig.Feature.FAIL_ON_NULL_FOR_PRIMITIVES);
		dcfg.disable(DeserializationConfig.Feature.FAIL_ON_NUMBERS_FOR_ENUMS);
		dcfg.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
		dcfg.disable(DeserializationConfig.Feature.USE_GETTERS_AS_SETTERS);
		
		dcfg.enable(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
		dcfg.enable(DeserializationConfig.Feature.AUTO_DETECT_FIELDS);
		dcfg.enable(DeserializationConfig.Feature.CAN_OVERRIDE_ACCESS_MODIFIERS);
		dcfg.enable(DeserializationConfig.Feature.USE_ANNOTATIONS);
		
		dcfg = dcfg.withDateFormat(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"));
		
		dcfg = dcfg.withVisibilityChecker(new VisibilityChecker.Std(
				Visibility.NONE, Visibility.NONE, Visibility.NONE, Visibility.NONE,
				Visibility.ANY));
		
		// set config
		objectMapper.setDeserializationConfig(dcfg);
	}
	
	public void sendHttpResponse(HttpServletResponse resp, Object o, boolean cachable) throws IOException {
		resp.setContentType("application/json; charset=UTF-8");
		
		if (!cachable) {
			resp.addDateHeader("Expires", 1L);
			resp.addHeader("Pragma", "no-cache");
			resp.addHeader("Cache-Control", "no-cache, no-store, max-age=0");
		}
		
		OutputStream out = resp.getOutputStream();
		
		JsonFactory jfac = objectMapper.getJsonFactory();
		
		JsonGenerator jgen = jfac.createJsonGenerator(out, JsonEncoding.UTF8);
		objectMapper.writeValue(jgen, o);
		
		out.flush();
	}
	
	public <T> T parseHttpRequest(HttpServletRequest req, Class<T> type) throws IOException {
		JsonFactory jfac = objectMapper.getJsonFactory();
		JsonParser jp = jfac.createJsonParser(req.getReader());
		
		return objectMapper.readValue(jp, type);
	}
	
	@JsonFilter("ignored-fields")
	private static class FilterEnabled {
		//
	}
}
