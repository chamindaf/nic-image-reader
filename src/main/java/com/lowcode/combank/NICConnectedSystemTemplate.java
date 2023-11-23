package com.lowcode.combank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appian.connectedsystems.simplified.sdk.SimpleConnectedSystemTemplate;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;
import com.appian.connectedsystems.simplified.sdk.connectiontesting.SimpleTestableConnectedSystemTemplate;
import com.appian.connectedsystems.templateframework.sdk.ExecutionContext;
import com.appian.connectedsystems.templateframework.sdk.TemplateId;
import com.appian.connectedsystems.templateframework.sdk.connectiontesting.TestConnectionResult;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;

@TemplateId(name = "NICConnectedSystemTemplate")
public class NICConnectedSystemTemplate extends SimpleTestableConnectedSystemTemplate {
	
		private static Logger logger = LoggerFactory.getLogger(NICConnectedSystemTemplate.class);

		public static final String ACCESSS_KEY = "AccessKey";
	    public static final String ACCESS_SECRET = "SecretKey";

	@Override
	protected SimpleConfiguration getConfiguration(SimpleConfiguration simpleConfiguration,
			ExecutionContext executionContext) {

		return simpleConfiguration.setProperties(
				  textProperty(ACCESSS_KEY)
		            .label("Access Key")
		            .isExpressionable(false)
		            .isRequired(true)
		            .instructionText("AWS Access Key")
		            .build(),
		        textProperty(ACCESS_SECRET)
		            .label("Secret Key")
		            .isRequired(true)
		            .isExpressionable(false)
		            .instructionText("AWS Secret Key")
		            .build());
				
	}

	@Override
	protected TestConnectionResult testConnection(SimpleConfiguration simpleConfiguration,
			ExecutionContext executionContext) {
		TextractClient textractClient = null;
		String key = simpleConfiguration.getValue(ACCESSS_KEY);
		String secret = simpleConfiguration.getValue(ACCESS_SECRET);
		
		try {
			Region region = Region.US_EAST_2;
			
			AwsBasicCredentials awsCreds = AwsBasicCredentials.create(key,secret);
			StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);
			textractClient = TextractClient.builder().region(region).credentialsProvider(credentialsProvider).build();
			return TestConnectionResult.success();

		} catch (Exception e) {
			logger.error("Exception in TextractClient" , e );
			return TestConnectionResult.error(e.getMessage());

		} finally {
			textractClient.close();
		}
	}
}
