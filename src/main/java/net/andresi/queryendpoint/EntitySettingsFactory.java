package net.andresi.queryendpoint;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class EntitySettingsFactory {

    public static EntitySettings getSettings() {

        File xmlFile = new File("WEB-INF/database_config.xml");
        DocumentBuilder docBuilder = null;
        Document doc = null;

        EntitySettings settings = new EntitySettings();

        try {
            docBuilder = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder()
            ;
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        try {
            doc = docBuilder.parse(xmlFile);
        } catch (SAXException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        NodeList entityNodes = doc.getElementsByTagName("entity");
        for (int i = 0; i < entityNodes.getLength(); i++) {

            Element entityElement = (Element)entityNodes.item(i);
            NodeList propNodes = entityElement.getElementsByTagName("property");
            NodeList valueNodes = entityElement.getElementsByTagName("value");

            for (int j = 0; j < propNodes.getLength(); j++) {
                Element propElement = (Element)propNodes.item(j);
                if (getTagValue("type", propElement) != null) {
                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "type",
                        getTagValue("type", propElement)
                    );
                }
                if (getTagValue("notnull", propElement) != null) {
                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "notnull",
                        getTagValue("notnull", propElement)
                    );
                }
                if (getTagValue("unique", propElement) != null) {
                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "unique",
                        getTagValue("unique", propElement)
                    );
                }
                if (getTagValue("related_entity", propElement) != null) {
                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "related_entity",
                        getTagValue("related_entity", propElement)
                    );
                }
                if (getTagValue("display_property", propElement) != null) {
                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "display_property",
                        getTagValue("display_property", propElement)
                    );
                }
                if (valueNodes.getLength() > 0) {

                    settings.setValue(
                        entityElement.getAttribute("name"),
                        propElement.getAttribute("name"),
                        "value_list",
                        new ArrayList<Object>(valueNodes.getLength())
                    );

                    for (int k = 0; k < valueNodes.getLength(); k++) {

                        NodeList childNodes = valueNodes.item(k).getChildNodes();
                        Element valueElement = (Element)valueNodes.item(k);
                        String tagName = null;

                        ArrayList<Object> values = (ArrayList<Object>) settings.getValue(
                            entityElement.getAttribute("name"),
                            propElement.getAttribute("name"),
                            "value_list"
                        );
                        values.add(getTagValue(propElement.getAttribute("name"), valueElement));
                        settings.setValue(
                            entityElement.getAttribute("name"),
                            propElement.getAttribute("name"),
                            "value_list",
                            values
                        );
                    }
                }
            }
        }

        return settings;
    }

    private static String getTagValue(String tag, Element element) {
         String returnValue = null;
         NodeList nodes = null;
         try {
             nodes = element.getElementsByTagName(tag).item(0).getChildNodes();
         } catch (NullPointerException npe) {
         }
         if (nodes != null && nodes.getLength() > 0) {
             returnValue = nodes.item(0).getNodeValue();
         }
         return returnValue;
    }
}
