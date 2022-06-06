package AutoDriveEditor.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XMLUtils {

    // Setters

    public static void setTextValue(String tag, Document doc, String textNode, Element element) {
        Element e;
        e = doc.createElement(tag);
        if (textNode == null) {
            textNode = "";
        }
        e.appendChild(doc.createTextNode(textNode));
        element.appendChild(e);
    }

    public static void setBooleanValue(String tag, Document doc, Boolean bool, Element element) {
        Element e;
        e = doc.createElement(tag);
        e.appendChild(doc.createTextNode(String.valueOf(bool)));
        element.appendChild(e);
    }

    public static void setIntegerValue(String tag, Document doc, int value, Element element) {
        Element e;
        e = doc.createElement(tag);
        e.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(e);
    }

    public static void setFloatValue(String tag, Document doc, float value, Element element) {
        Element e;
        e = doc.createElement(tag);
        e.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(e);
    }

    //getters

    public static String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

    public static Boolean getBooleanValue(Boolean def, Element doc, String tag) {
        Boolean value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = Boolean.valueOf(nl.item(0).getFirstChild().getNodeValue());
        }
        return value;
    }

    public static Integer getIntegerValue(int def, Element doc, String tag) {
        int value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
        }
        return value;
    }

    public static Float getFloatValue(float def, Element doc, String tag) {
        float value = def;
        NodeList nl;
        nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = Float.parseFloat(nl.item(0).getFirstChild().getNodeValue());
        }
        return value;
    }
}
