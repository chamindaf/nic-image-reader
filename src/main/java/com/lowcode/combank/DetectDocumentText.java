package com.lowcode.combank;

import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.appian.connectedsystems.simplified.sdk.configuration.SimpleConfiguration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;
import software.amazon.awssdk.services.textract.model.Document;
import software.amazon.awssdk.services.textract.model.TextractException;

public class DetectDocumentText {
	
	private static Logger logger = LoggerFactory.getLogger(DetectDocumentText.class);

	public String doOCR(SimpleConfiguration connectedSystemConfiguration,InputStream sourceStream) {
		

		String key = connectedSystemConfiguration.getValue(NICConnectedSystemTemplate.ACCESSS_KEY);
		String secret = connectedSystemConfiguration.getValue(NICConnectedSystemTemplate.ACCESS_SECRET);

		Region region = Region.US_EAST_2;
		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(key,secret);
		StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);
		TextractClient textractClient = TextractClient.builder().region(region).credentialsProvider(credentialsProvider)
				.build();
		String ocrExtarcted = detectDocText(textractClient, sourceStream);
		textractClient.close();
		return ocrExtarcted;
	}

	private String detectDocText(TextractClient textractClient, InputStream sourceStream) {

		Pattern pattern = Pattern.compile("\\d{8,}");
		String NIC = "ERROR";

		try {
			// InputStream sourceStream = new FileInputStream(new File(sourceDoc));
			SdkBytes sourceBytes = SdkBytes.fromInputStream(sourceStream);

			// Get the input Document object as bytes
			Document myDoc = Document.builder().bytes(sourceBytes).build();

			DetectDocumentTextRequest detectDocumentTextRequest = DetectDocumentTextRequest.builder().document(myDoc)
					.build();

			// Invoke the Detect operation
			DetectDocumentTextResponse textResponse = textractClient.detectDocumentText(detectDocumentTextRequest);
			List<Block> docInfo = textResponse.blocks();
			for (Block block : docInfo) {

				String word = block.text();
				if (word != null) {
					Matcher matcher = pattern.matcher(block.text());
					if (matcher.find()) {
						NIC = matcher.group();
						break;
					}
				}
			}

		} catch (TextractException e) {
			logger.error("Exception in detectDocumentText" , e );
			System.exit(1);
		}
		return NIC;
	}

}