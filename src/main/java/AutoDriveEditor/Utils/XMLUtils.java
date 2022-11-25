package AutoDriveEditor.Utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.awt.*;

import static AutoDriveEditor.Utils.ConversionUtils.ColorToHex;
import static AutoDriveEditor.Utils.ConversionUtils.HexToColor;

public class XMLUtils {

    //
    // Setters
    //

    public static void setTextValue(String tag, Document doc, String textNode, Element element) {
        Element newElement = doc.createElement(tag);
        if (textNode == null) {
            textNode = "";
        }
        newElement.appendChild(doc.createTextNode(textNode));
        element.appendChild(newElement);
    }

    public static void setBooleanValue(String tag, Document doc, Boolean bool, Element element) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(bool)));
        element.appendChild(newElement);
    }

    public static void setIntegerValue(String tag, Document doc, int value, Element element) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(newElement);
    }

    public static void setFloatValue(String tag, Document doc, float value, Element element) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(String.valueOf(value)));
        element.appendChild(newElement);
    }

    public static void setColorValue(String tag, Document doc, Color color, Element element) {
        Element newElement = doc.createElement(tag);
        newElement.appendChild(doc.createTextNode(ColorToHex(color, false)));
        element.appendChild(newElement);
    }

    //
    //getters
    //

    public static String getTextValue(String def, Element doc, String tag) {
        String value = def;
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = nl.item(0).getFirstChild().getNodeValue();
        }
        return value;
    }

    public static Boolean getBooleanValue(boolean def, Element doc, String tag) {
        boolean result = def;
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            result = Boolean.parseBoolean(nl.item(0).getFirstChild().getNodeValue());
        }
        return result;
    }

    public static Integer getIntegerValue(int def, Element doc, String tag) {
        int value = def;
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = Integer.parseInt(nl.item(0).getFirstChild().getNodeValue());
        }
        return value;
    }

    public static Float getFloatValue(Element doc, String tag, float defaultValue) {
        float value = defaultValue;
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            value = Float.parseFloat(nl.item(0).getFirstChild().getNodeValue());
        }
        return value;
    }

    public static Color getColorValue(Element doc, String tag, Color defaultColor) {
        NodeList nl = doc.getElementsByTagName(tag);
        if (nl.getLength() > 0 && nl.item(0).hasChildNodes()) {
            defaultColor = HexToColor(nl.item(0).getFirstChild().getNodeValue());
        }
        return defaultColor;
    }
}
