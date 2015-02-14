package Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class GenericXMLHelper {
	
	// XML document
			protected Document maindoc;

			// is true, if document was loaded
			private boolean loadrez = false;

			// root element of loaded document
			protected Element root;

			public GenericXMLHelper(String Filepath) {
				loadModel(Filepath);
			}

			private boolean loadModel(String Filepath) {
				try {
					File newfile = new File(Filepath);
					DocumentBuilderFactory docFactory = DocumentBuilderFactory
							.newInstance();
					DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
					this.maindoc = docBuilder.parse(newfile);
					this.root = maindoc.getDocumentElement();
					this.loadrez = true;
				} catch (Exception e) {
					//e.getMessage();
					e.printStackTrace();
					loadrez = false;
				}
				return loadrez;
			}


			
			public List<Element> getElements(String ThisType) {
				List<Element> res = new ArrayList<Element>();
				if (!loadrez)
					return res;
				NodeList list = this.root.getElementsByTagName(ThisType);
				for (int i = 0; i < list.getLength(); i++)
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE)
						res.add((Element) list.item(i));
				return res;
			}
			
			public List<Element> getElements(Element element, String ThisType) {
				List<Element> res = new ArrayList<Element>();
				if (!loadrez)
					return res;
				NodeList list = element.getElementsByTagName(ThisType);
				for (int i = 0; i < list.getLength(); i++)
					if (list.item(i).getNodeType() == Node.ELEMENT_NODE)
						res.add((Element) list.item(i));
				return res;
			}

}
