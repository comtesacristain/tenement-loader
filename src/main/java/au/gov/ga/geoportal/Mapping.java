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
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("unused")
public class Mapping{
	

	
	private Map<String, Map<?,?>> mapping;

	private Document document;

	public Mapping(String filePath) throws SAXException, IOException, ParserConfigurationException {
		File file = new File(filePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		this.document = documentBuilder.parse(file);
		this.document.getDocumentElement().normalize();
		this.mapping = new HashMap<String, Map<?,?>>();
	}

	public void readMappingFile(String filePath) throws ParserConfigurationException, SAXException, IOException {
		
		NodeList stateList = document.getElementsByTagName("state");
		for (int i = 0; i < stateList.getLength(); i++) {
			if (stateList.item(i).getNodeType() == Node.ELEMENT_NODE) {
				HashMap<String, String> keyValuePair = new HashMap<>();
				Element stateElement =  (Element) stateList.item(i);
			
				String state = stateElement.getAttribute("name");
				NodeList mappingList = stateElement.getElementsByTagName("mapping");
				for (int j=0; j < mappingList.getLength(); j++){
					Node mappingNode = mappingList.item(j);
					String source = mappingNode.getFirstChild().getTextContent();
					String target = mappingNode.getLastChild().getTextContent();
					keyValuePair.put(source, target);
				}
				mapping.put(state, keyValuePair);

				
			}
		
		}
	}
	

	public Map<String, Map<?, ?>> getMapping(){
		return mapping;
	}
}
