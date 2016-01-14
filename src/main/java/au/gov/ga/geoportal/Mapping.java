package au.gov.ga.geoportal;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Mapping {
	
	private Document document;
	
	public Mapping(String filePath) throws SAXException, IOException, ParserConfigurationException {
		File file = new File(filePath);
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		this.document = documentBuilder.parse(file);
		this.document.getDocumentElement().normalize();
	}

	public void readMappingFile(String filePath) throws ParserConfigurationException, SAXException, IOException {
		
		
		
		NodeList stateList = document.getElementsByTagName("staff");
		for (Node node : stateList) {
			
		}
	}

}
