package com.mnsuk.converter;

import com.ibm.dataexplorer.converter.ByteArrayConverter;
import com.ibm.dataexplorer.converter.ConversionException;
import com.ibm.dataexplorer.converter.ConverterOptions;
import com.ibm.dataexplorer.converter.FatalConverterException;
import com.ibm.ica.api.document.add.TakmiAddedDocument;
import com.ibm.ica.api.document.add.TakmiDocumentAddRequester;
import com.ibm.ica.api.document.add.TakmiDocumentAddRequesterException;
import com.ibm.ica.api.document.add.TakmiDocumentAddResponse;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import static com.ibm.dataexplorer.converter.LoggingConstants.*;

public class ACIndex implements ByteArrayConverter {
	private static final String OPTION_HOSTNAME = "wca-hostname";
	private static final String OPTION_PORT = "wca-port";
	private static final String OPTION_COLLECTION_ID = "collection-id";
	private static final String OPTION_USERNAME = "username";
	private static final String OPTION_PASSWORD = "password";
	private static final String OPTION_FIELDS = "field";

	private static final Logger LOGGER = LoggerFactory.getLogger(ACIndex.class);
	private ConverterOptions options;
	private boolean isAlive;

	public ACIndex(ConverterOptions options) throws FatalConverterException {
		this.options = options;
		isAlive = true;

	}

	@Override
	public byte[] convert(byte[] data) throws ConversionException, FatalConverterException {
		LOGGER.trace(PUBLIC_ENTRY);
		checkIsAlive();
		try {
			String stringData = convertToString(data);
			String hostname = options.getLastOptionValue(OPTION_HOSTNAME);
			String port = options.getLastOptionValue(OPTION_PORT);

			String url = "http://" + hostname
					+ ":" + port + "/api/v10/admin/document?method=add";

			String collectionID = options.getLastOptionValue(OPTION_COLLECTION_ID);

			LOGGER.debug("mns call: " + url + "in collection: " + collectionID);

			String basicHostname = hostname;
			int basicHostport = Integer.parseInt(port);
			String basicAuthId = options.getLastOptionValue(OPTION_USERNAME);
			String basicAuthPw = options.getLastOptionValue(OPTION_PASSWORD);
			List<String> optionFields = options.getOptionValues(OPTION_FIELDS);

			TakmiDocumentAddRequester requester = new TakmiDocumentAddRequester(
					url, collectionID, basicHostname, basicHostport, basicAuthId,
					basicAuthPw);
			LOGGER.debug("mns requester: ");

			TakmiAddedDocument document = new TakmiAddedDocument();
			LOGGER.debug("mns new document: ");
			document.setCrawlSpaceId("apiSpace");
			document.setDocumentDate(new Date());
			document.setDocumentId("" + System.currentTimeMillis());
			document.setLanguage(Locale.ENGLISH.getLanguage());
			LOGGER.debug("mns set documents done. Option fields: " + optionFields.size());
			Boolean doneBody = false;
			for (String f : optionFields) {
				LOGGER.debug("mns optionfield: " + f);
				String[] t = f.split(String.valueOf(":"));
				String contentElement = (t.length >= 1) ? t[0] : null;
				String fieldName = (t.length >= 2) ? t[1] : null;
				if ((contentElement != null && !contentElement.isEmpty()) && (fieldName != null && !fieldName.isEmpty())) {
					String thisContent = getContentToSubmit(stringData, contentElement);
					if (thisContent != null && !thisContent.isEmpty()) {
						LOGGER.debug("mns adding field: "+fieldName + " content: " + thisContent);
						document.addField(fieldName,thisContent);
						if (fieldName.equals("body"))
							doneBody=true; 
					} else {
						LOGGER.info("mns no content element found: "+thisContent);
					}
				}
			}
			if (!doneBody) {
				throw new FatalConverterException("Required Content Analytics index field 'body' is missing.");
			}
			LOGGER.debug("mns done documents: ");
			TakmiDocumentAddResponse response = requester.add(document);
			JSONObject responseJSON = new JSONObject(response.toString());
			if (responseJSON.getInt("returnValue") != 0)
				throw new ConversionException("WCA REST API error: " + responseJSON.getString("message"));
			LOGGER.debug("mns response: " + response.toString());
			return convertToBytes(stringData);
		} 
		catch (TakmiDocumentAddRequesterException e) {
			throw new ConversionException("WCA REST API exception: "+ e.getMessage(), (Throwable) e);
		} catch (Exception e) {
			throw new ConversionException("Error adding to Content Analytics index: " + e.getMessage(), (Throwable) e);
		} 
		finally {
			LOGGER.trace(PUBLIC_EXIT);
		}
	}

	private byte[] convertToBytes(String data) throws UnsupportedEncodingException {
		return data == null ? null : data.getBytes("UTF-8");
	}

	private String convertToString(byte[] data) throws UnsupportedEncodingException {
		return data == null ? "" : new String(data, "UTF-8");
	}

	@Override
	public void terminate() {
		checkIsAlive();

		LOGGER.debug("Terminating");
		isAlive = false;

		// Nothing to terminate
	}

	private void checkIsAlive() {
		if (!isAlive()) {
			LOGGER.error("I've already been terminated");
			throw new IllegalStateException("The object has already been terminated");
		}
	}

	boolean isAlive() {
		return isAlive;
	}

	private String getContentToSubmit(String stringData, String elementToSubmit) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException{
		XPathFactory xPathFactory = XPathFactory.newInstance();
		XPath xpath = xPathFactory.newXPath();
		org.w3c.dom.Document inputDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(stringData)));
		NodeList nl = (NodeList) xpath.evaluate("//content", inputDoc, XPathConstants.NODESET);

		StringBuilder sb = new StringBuilder();
		LOGGER.debug("mns nodes: " + nl.getLength());
		for (int i=0; i < nl.getLength(); i++) {
			Node childNode = (Node) nl.item(i);
			LOGGER.debug("mns nodes: " + childNode.getAttributes().getNamedItem("name").getNodeValue() + " looking: " + elementToSubmit);
			if (childNode.getAttributes().getNamedItem("name").getNodeValue().matches(elementToSubmit)) {
				LOGGER.debug("mns match: ");
				if (childNode.getFirstChild() != null) {
					sb.append(childNode.getFirstChild().getTextContent()).append(" ");
				}
			}
		}
		return sb.toString();
	}

}
