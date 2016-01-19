package au.gov.ga.geoportal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("unused")
public class TenementMapping {

	private Map<String, Map<?, ?>> mapping;
	
	private Map<String, Field> fieldMapping;

	private Document document;

	public TenementMapping() {

	}

	public TenementMapping(String filePath) throws SAXException, IOException, ParserConfigurationException {

		readMappingFile(filePath);
	}

	public void readMappingFile(String filePath) throws ParserConfigurationException, SAXException, IOException {

		File file = new File(filePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		document = documentBuilder.parse(file);
		document.getDocumentElement().normalize();
		mapping = new HashMap<String, Map<?, ?>>();
		NodeList stateList = document.getElementsByTagName("state");

		for (int i = 0; i < stateList.getLength(); i++) {
			if (stateList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				HashMap<String, String> keyValuePair = new HashMap<>();
				Element stateElement = (Element) stateList.item(i);

				String state = stateElement.getAttribute("name");

				NodeList mappingList = stateElement.getElementsByTagName("mapping");

				for (int j = 0; j < mappingList.getLength(); j++) {
					Node mappingNode = mappingList.item(j);
					if (mappingNode.getNodeType() == Node.ELEMENT_NODE) {
						Element mappingElement = (Element) mappingNode;

						String source = mappingElement.getElementsByTagName("source").item(0).getTextContent();
						String target = mappingElement.getElementsByTagName("target").item(0).getTextContent();

						keyValuePair.put(source, target);
					}
				}
				mapping.put(state, keyValuePair);

			}

		}
	}

	public Map<String, Field> getMapping() {
		return fieldMapping;
	}

	public void readFile(String filePath) throws ParserConfigurationException, SAXException, IOException {
		File file = new File(filePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		document = documentBuilder.parse(file);
		document.getDocumentElement().normalize();
		mapping = new HashMap<String, Map<?, ?>>();

		Map<String, Field> fieldMapping = new HashMap<String, Field>();

		NodeList fields = document.getElementsByTagName("field");

		for (int i = 0; i < fields.getLength(); i++) {
			Node fieldNode = fields.item(i);
			if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
				Field field = new Field();
				Element fieldElement = (Element) fieldNode;
				field.setSource(fieldElement.getAttribute("source"));
				field.setTarget(fieldElement.getAttribute("target"));
				field.setType(fieldElement.getAttribute("type"));
				if (fieldElement.getAttribute("type") == "date") {
					field.setFormat(fieldElement.getAttribute("format"));
				}

				NodeList mappings = fieldElement.getElementsByTagName("mapping");
				if (mappings.getLength() > 0) {
					List<Mapping> mappingList = new ArrayList<Mapping>();
					for (int j = 0; j < mappings.getLength(); j++) {

						Node mappingNode = mappings.item(i);
						if (mappingNode.getNodeType() == Node.ELEMENT_NODE) {
							Mapping mapping = new Mapping();
							Element mappingElement = (Element) mappingNode;
							mapping.setSource(mappingElement.getElementsByTagName("source").item(0).getTextContent());
							mapping.setTarget(mappingElement.getElementsByTagName("target").item(0).getTextContent());
							mappingList.add(mapping);
						}

					}
					field.setMappings(mappingList);
				}
				fieldMapping.put(fieldElement.getAttribute("target"), field);
			}
		}

	}
}
