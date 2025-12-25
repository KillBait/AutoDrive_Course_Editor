package AutoDriveEditor.Classes.Util_Classes;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.ColorToHexWithAlpha;
import static AutoDriveEditor.Classes.Util_Classes.ColourUtils.HexToColorWithAlpha;
import static AutoDriveEditor.Classes.Util_Classes.LoggerUtils.LOG;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogXMLConfigMenu.bDebugLogXMLInfo;
import static AutoDriveEditor.GUI.Menus.DebugMenu.Logging.LogXMLReaderMenu.bDebugXMLReader;

public class XMLUtils {

    public static Document parseXmlFile(byte[] file) {
        if (file == null) return null;
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            InputStream is = new ByteArrayInputStream(file);
            Document doc = dBuilder.parse(is);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (ParserConfigurationException ex) {
            LOG.error("XMLUtils.parseXmlFile: ParserConfigurationException");
            ex.printStackTrace();
        } catch (SAXException ex) {
            LOG.error("XMLUtils.parseXmlFile: SAXException");
            ex.printStackTrace();
        } catch (IOException ex) {
            LOG.error("XMLUtils.parseXmlFile: IOException");
            ex.printStackTrace();
        }
        return null;
    }

    public static Document parseXmlFile(String filePath)  {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(filePath);
            doc.getDocumentElement().normalize();
            return doc;
        } catch (Exception ex) {
            return null;
        }
    }

    //
    //getters
    //

    public static boolean hasKey(Element doc, String tag) {
        if (doc == null || tag == null || tag.isEmpty()) {
            if (bDebugLogXMLInfo) LOG.info("--------> XMLUtils.hasKey(): supplied doc or tag is null/empty");
            return false;
        } else {
            NodeList nodeList = doc.getElementsByTagName(tag);
            boolean hasKey = nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes();
            if (bDebugLogXMLInfo) LOG.info("--------> XMLUtils.hasKey(): tag <{}> found: {}", tag, hasKey);
            return hasKey;
        }
    }

    private static Object getValue(Element doc, String tag) {
        if (doc == null || tag == null || tag.isEmpty()) {
            if (doc == null) LOG.info("XMLUtils.getValue: supplied doc is null");
            if (tag == null || tag.isEmpty()) LOG.info("XMLUtils.getValue(): supplied tag is null or empty");
            return null;
        } else {
            NodeList nodeList = doc.getElementsByTagName(tag);
            if (nodeList.getLength() > 0 && nodeList.item(0).hasChildNodes()) {
                return nodeList.item(0).getFirstChild().getNodeValue();
            } else {
                if (bDebugLogXMLInfo) LOG.info("--------> XMLUtils.getValue(): tag <{}> not found in supplied Element, returning null", tag);
                return null;
            }
        }
    }

    public static String getStringValue(Element doc, String tag) {
        return (String) getValue(doc, tag);
    }

    public static String getStringValue(Element doc, String tag, String defaultString) {
        String value = getStringValue(doc, tag);
        return value != null ? value : defaultString;
    }

    public static Boolean getBooleanValue(Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        return (value != null) ? Boolean.parseBoolean(value) : null;
    }

    public static Boolean getBooleanValue(Element doc, String tag, boolean defaultBoolean) {
        String value = (String) getValue(doc, tag);
        return value != null ? Boolean.parseBoolean(value) : defaultBoolean;
    }

    public static Byte getByteValue(Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        return (value != null) ? Byte.parseByte(value) : 0;
    }

    public static Byte getByteValue(Element doc, String tag, byte defaultByte) {
        String value = (String) getValue(doc, tag);
        return value != null ? Byte.parseByte(value) : defaultByte;
    }


    public static Integer getIntegerValue(Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        return (value != null) ? Integer.parseInt(value) : 0;
    }

    public static Integer getIntegerValue(Element doc, String tag, int defaultInteger) {
        String value = (String) getValue(doc, tag);
        return value != null ? Integer.parseInt(value) : defaultInteger;
    }

    public static Float getFloatValue(Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        return (value != null) ? Float.parseFloat(value) : 0;
    }

    public static Float getFloatValue(Element doc, String tag, float defaultFloat) {
        String value = (String) getValue(doc, tag);
        return value != null ? Float.parseFloat(value) : defaultFloat;
    }

    public static <E extends Enum<E>> E getEnumValue(Class<?> enumClass, Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        if (value != null) {
            try {
                return Enum.valueOf((Class<E>) enumClass, value);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    public static <E extends Enum<E>> E getEnumValue(Class<E> enumClass, Element doc, String tag, E defaultEnum) {
        String value = (String) getValue(doc, tag);
        if (value != null) {
            try {
                return Enum.valueOf(enumClass, value);
            } catch (IllegalArgumentException e) {
                // Value does not match any of the Enum values
                return defaultEnum;
            }
        }
        return defaultEnum;
    }

    public static Color getColorValue(Element doc, String tag) {
        String value = (String) getValue(doc, tag);
        return value != null ? HexToColorWithAlpha(value) : null;
    }

    public static Color getColorValue(Element doc, String tag, Color defaultColor) {
        String value = (String) getValue(doc, tag);
        return value != null ? HexToColorWithAlpha(value) : defaultColor;
    }

    //

    public static String getElementTextContent(Element element, String tagName) {
        NodeList nodeList = element.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            Node node = nodeList.item(0).getFirstChild();
            if (node != null) {
                return node.getNodeValue();
            }
        }
        return null;
    }

    //
    // Setters
    //

    public static void setTextValue(Document doc, Element element, String tag, String textNode) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(textNode != null ? textNode : ""));
        element.appendChild(newElement);
    }

    public static void setBooleanValue(Document doc, Element element, String tag, Boolean bool) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(bool)));
        element.appendChild(newElement);
    }

    public static void setByteValue(Document doc, Element element, String tag, byte value) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(newElement);
    }

    public static void setIntegerValue(Document doc, Element element, String tag, int value) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(newElement);
    }

    public static void setFloatValue(Document doc, Element element, String tag, float value) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(newElement);
    }

    public static <E extends Enum<E>> void setEnumValue(Document doc, Element element, String tag, E enumValue) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(enumValue.toString()));
        element.appendChild(newElement);
    }

    public static void setColorValue(Document doc, Element element, String tag, Color color) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(ColorToHexWithAlpha(color, false)));
        element.appendChild(newElement);
    }

    // XMLReader Class
    public static class XMLReader {
        private Document xmlDocument;
        private Element xmlRootElement;

        public XMLReader(byte[] file) {
            try {
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                InputStream is = new ByteArrayInputStream(file);
                this.xmlDocument = dBuilder.parse(is);
                this.xmlRootElement = xmlDocument.getDocumentElement();
            } catch (Exception ex) {
                LOG.error("## XMLUtils.XMLReader() ## Exception");
                ex.printStackTrace();
            }
        }

        public XMLReader(Document xmlDocument) {
            this.xmlDocument = xmlDocument;
            this.xmlRootElement = xmlDocument.getDocumentElement();
        }

        public String getRootElementName() {
            return xmlRootElement.getNodeName();
        }

        public NodeList getElementByTag(String tagName) {
            NodeList element = xmlDocument.getElementsByTagName(tagName);
            if (element.getLength() == 0) {
                if (bDebugXMLReader) LOG.info("## XMLReader.getElementByTag() ## Element '{}' length is 0", tagName);
            } else {
                if (bDebugXMLReader) LOG.info("## XMLReader.getElementByTag() ## Element '{}' length is {}", tagName, element.getLength());
            }
            return element;
        }

        public NodeList getChildNode(String parentTagName) {
            Element parentElement = (Element) xmlDocument.getElementsByTagName(parentTagName).item(0);
            return parentElement != null ? parentElement.getChildNodes() : null;
        }

        public NodeList getChildNode(String parentTagName, String childTagName) {
            Element parentElement = (Element) xmlDocument.getElementsByTagName(parentTagName).item(0);
            if (parentElement != null) {
                return parentElement.getElementsByTagName(childTagName);
            }
            return null;
        }

        public String getNodeValue(String tagName) {
            NodeList nodeList = xmlDocument.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                Node node = nodeList.item(0);
                return node.getTextContent();
            }
            return null;
        }

        public String getNestedNodeValue(String parentTagName, String childTagName) {
            Element parentElement = (Element) xmlDocument.getElementsByTagName(parentTagName).item(0);
            if (parentElement != null) {
                NodeList childNodes = parentElement.getElementsByTagName(childTagName);
                if (childNodes.getLength() > 0) {
                    return childNodes.item(0).getTextContent();
                }
            }
            return null;
        }

        public String getAttributeValue(String tagName, String attributeName) {
            NodeList nodeList = xmlDocument.getElementsByTagName(tagName);
            if (nodeList.getLength() > 0) {
                if (bDebugXMLReader) {
                    LOG.info("## XMLReader.getAttributeValue() ## Searching for attribute '{}' in tag '{}'", attributeName, tagName);
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                            if (node.hasAttributes()) {
                                NamedNodeMap attributes = node.getAttributes();
                                for (int j = 0; j < attributes.getLength(); j++) {
                                    Node attribute = attributes.item(j);
                                    if (attribute.getNodeName().equals(attributeName)) LOG.info("## XMLReader.getAttributeValue() ## Fount Attribute '{}' ( {} ) ", attribute.getNodeName(), attribute.getNodeValue());
                                }
                            }
                        }
                    }
                }
                Element element = (Element) nodeList.item(0);
                if (element.hasAttributes()) {
                    String attributeValue = element.getAttribute(attributeName);
                    if (attributeValue.isBlank()) {
                        LOG.info("## XMLReader.getAttributeValue() ## Attribute '{}' not found or empty", attributeName);
                    }
                    return attributeValue;
                } else {
                    LOG.error("## XMLReader.getAttributeValue() ## Tag '{}' has no attributes", tagName);
                    return null;
                }
            }
            LOG.error("## XMLReader.getAttributeValue() ## Tag '{}' not found", tagName);
            return null;
        }

        public Document getDocument() {
            return this.xmlDocument;
        }

        public Element getRootElement() { return this.xmlRootElement; }
    }

    //
    // Custom Exceptions
    //

    public static class EntryTotalException extends Exception {
        private final String errorValue;
        private final String errorMessage;

        public EntryTotalException(String errorValue, String errorMessage) {
            this.errorValue = errorValue;
            this.errorMessage = errorMessage;
        }

        public String getErrorValue() {
            return errorValue;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
