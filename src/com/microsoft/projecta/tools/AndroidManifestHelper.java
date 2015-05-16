
package com.microsoft.projecta.tools;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AndroidManifestHelper {
    public static String parsePackageName(Path androidManifest) throws SAXException, IOException,
            ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(androidManifest.toFile());
        return doc.getDocumentElement().getAttributes().getNamedItem("package").getNodeValue();
    }

    public static String parseMainActivity(Path androidManifest) throws SAXException, IOException,
            ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(androidManifest.toFile());
        NodeList nodeList = doc.getElementsByTagName("activity");
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n.getFirstChild() != null
                    && n.getFirstChild().getNodeName().equals("intent-filter")) {
                NodeList subnodeList = n.getFirstChild().getChildNodes();
                for (int j = 0; j < nodeList.getLength(); j++) {
                    Node a = subnodeList.item(j);
                    if (a.getNodeName().equals("action")
                            && a.getAttributes().getNamedItem("android:name").getNodeValue()
                                    .equals("android.intent.action.MAIN")) {
                        return n.getAttributes().getNamedItem("android:name").getNodeValue();
                    }
                }
            }
        }
        return null;
    }

    public static List<String> parseActivity(Path androidManifest) throws SAXException,
            IOException, ParserConfigurationException {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(androidManifest.toFile());
        NodeList nodeList = doc.getElementsByTagName("activity");
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            result.add(n.getAttributes().getNamedItem("android:name").getNodeValue());
        }
        return result;
    }
}
