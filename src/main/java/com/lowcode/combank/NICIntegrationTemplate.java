package com.lowcode.combank;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.appian.connectedsystems.simplified.sdk.SimpleIntegrationTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.IntegrationError;
import com.appian.connectedsystems.templateframework.sdk.IntegrationResponse;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.configuration.BooleanDisplayMode;
import com.appian.connectedsystems.templateframework.sdk.configuration.Document;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyPath;
import com.appian.connectedsystems.templateframework.sdk.configuration.PropertyState;
import com.appian.connectedsystems.templateframework.sdk.configuration.SystemType;
import com.appian.connectedsystems.templateframework.sdk.diagnostics.IntegrationDesignerDiagnostic;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateRequestPolicy;
import com.appian.connectedsystems.templateframework.sdk.metadata.IntegrationTemplateType;

// Must provide an integration id. This value need only be unique for this connected system
@TemplateId(name = "NICIntegrationTemplate")
// Set template type to READ since this integration does not have side effects

@IntegrationTemplateType(IntegrationTemplateRequestPolicy.READ)
public class NICIntegrationTemplate extends SimpleIntegrationTemplate {

	static String FILE_CONTENT = "file";
	public static final String INTEGRATION_PROP_KEY = "intProp";
	private static Logger logger = LoggerFactory.getLogger(NICIntegrationTemplate.class);

	@Override
	protected SimpleConfiguration getConfiguration(SimpleConfiguration integrationConfiguration,
			SimpleConfiguration connectedSystemConfiguration, PropertyPath propertyPath,
			ExecutionContext executionContext) {
		
		return integrationConfiguration.setProperties(
				
				 textProperty(INTEGRATION_PROP_KEY).label("Text Property")
		            .isRequired(true)
		            .description("This will be concatenated with the connected system text property on execute")
		            .build(),
		            
				listTypeProperty(FILE_CONTENT)
				.label("Upload NIC Front Page")
				.instructionText("Upload NIC Front Page.")
				.itemType(SystemType.DOCUMENT)
				.isRequired(true)
				.isExpressionable(true).build());
	}

	@Override
	protected IntegrationResponse execute(SimpleConfiguration integrationConfiguration,
			SimpleConfiguration connectedSystemConfiguration, ExecutionContext executionContext) {

		Map<String, Object> requestDiagnostic = new HashMap<>();
		Map<String, Object> result = new HashMap<>();
		final long start = System.currentTimeMillis();
		List<PropertyState> values = integrationConfiguration.getValue(FILE_CONTENT);
       
		// get the uploaded documents
		ArrayList<Document> documentArray = new ArrayList<Document>();
		
		for (PropertyState doc : values) {
			Document document = (Document) doc.getValue();
			documentArray.add(document);
		}
		// validations
		if (documentArray.isEmpty()) {
			IntegrationError error = templateError("Files - No NIC files atatched.");
			return IntegrationResponse.forError(error).build();
		}
		String nic = "";
		
		DetectDocumentText obj = new DetectDocumentText();
		nic = obj.doOCR(connectedSystemConfiguration,documentArray.get(0).getInputStream());
		
		logger.debug("NIC extracted "+ nic );

		result.put("NIC", nic);
		final long end = System.currentTimeMillis();
		final long executionTime = end - start;
		final IntegrationDesignerDiagnostic diagnostic = IntegrationDesignerDiagnostic.builder()
				.addExecutionTimeDiagnostic(executionTime).addRequestDiagnostic(requestDiagnostic).build();

		return IntegrationResponse
				.forSuccess(result)
				.withDiagnostic(diagnostic)
				.build();
	}

	private IntegrationError templateError(String errorMessage) {
		return IntegrationError.builder()
				.title("Something went wrong")
				.message("An error occurred in the IntegrationTemplate - " + errorMessage)
				.build();
	}

	
}
