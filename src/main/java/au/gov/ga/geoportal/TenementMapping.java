package au.gov.ga.geoportal;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TenementMapping {

	private Map<String, Field> fieldMapping = new HashMap<>();

	private Document document;

	public TenementMapping() {

	}

	public TenementMapping(String filePath) throws SAXException, IOException, ParserConfigurationException {

		readFile(filePath);
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

		NodeList fields = document.getElementsByTagName("field");

		for (int i = 0; i < fields.getLength(); i++) {
			Node fieldNode = fields.item(i);
			if (fieldNode.getNodeType() == Node.ELEMENT_NODE) {
				Field field = new Field();
				Element fieldElement = (Element) fieldNode;
				field.setSource(fieldElement.getAttribute("source"));
				field.setTarget(fieldElement.getAttribute("target"));
				field.setType(fieldElement.getAttribute("type"));

				if (fieldElement.getAttribute("type").equals("date")) {

					field.setFormat(fieldElement.getAttribute("format"));

				}
				
				if (fieldElement.getAttribute("type").equals("uri")) {
					field.setURI(fieldElement.getAttribute("uri"));
				}
				
				NodeList mappings = fieldElement.getElementsByTagName("mapping");

				if (mappings.getLength() > 0) {
					Map<String, String> mappingMap = new HashMap<>();

					for (int j = 0; j < mappings.getLength(); j++) {

						Node mappingNode = mappings.item(j);
						if (mappingNode.getNodeType() == Node.ELEMENT_NODE) {

							Element mappingElement = (Element) mappingNode;
							String source= mappingElement.getElementsByTagName("source").item(0).getTextContent();
							String target = mappingElement.getElementsByTagName("target").item(0).getTextContent();
							mappingMap.put(source, target);

						}

					}

					field.setMappings(mappingMap);

				}

				fieldMapping.put(fieldElement.getAttribute("target"), field);
			}
		}

	}
}
