package pdfform;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.XfaForm;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtractorUsingOpenpdf {
    public static void extractAcroformFields(String src, String dest) {
        try (PdfReader reader = new PdfReader(src)) {
            AcroFields fields = reader.getAcroFields();
            
            // AcroFields.getField(fileName) will fetch value from xfa firstly if the pdf contains xfa
            fields.removeXfa(); 
            
            Map<String, AcroFields.Item> sortedMap = new TreeMap<>(fields.getAllFields());
            if (sortedMap.size() == 0) {
                System.out.println("OpenPDF: The pdf document does not contain an Acroform field.");
                return ;
            }
            
            FileWriter writer = new FileWriter(dest);

            for (Map.Entry<String, AcroFields.Item> entry : sortedMap.entrySet()) {
                String fieldName = entry.getKey();
                String fieldValue = fields.getField(fieldName);

                writer.write("Field Name: " + fieldName + "\n");
                writer.write("Field Value: " + fieldValue + "\n");
                writer.write("---------------------------\n");
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void extractXfa(String src, String destPath) {
        try (PdfReader reader = new PdfReader(src)) {

            AcroFields fields = reader.getAcroFields();

            XfaForm xfa = fields.getXfa();

            Document domDoc = xfa.getDomDocument();

            if (domDoc == null) {
                System.out.println("OpenPDF: The pdf document does not contain an XFA form.");
                return ;
            }

            Element rootElement = domDoc.getDocumentElement();

            if (rootElement != null) { 
                System.out.println("Root Element Name: " + rootElement.getNodeName());
                // System.out.println("Root Element Value: " + rootElement.getTextContent());
                
                if (rootElement.hasAttributes()) {
                    System.out.println("Root Element Attr:");
                    for (int i = 0; i < rootElement.getAttributes().getLength(); i++) {
                        System.out.println("  " + rootElement.getAttributes().item(i).getNodeName() + ": "
                                + rootElement.getAttributes().item(i).getNodeValue());
                    }
                }

                // get all first level children
                NodeList childNodes = rootElement.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) { 
                    Node childNode = childNodes.item(i);

                    if (childNode.getNodeType() == Node.ELEMENT_NODE) { 
                        Element childElement = (Element) childNode;
                        String childName = childElement.getNodeName();
                        System.out.println("Child Node: " + childName);
                        childName = childName.replace(':', '_');
                        String output = destPath + "/" + childName + ".xml";

                        try (FileOutputStream fileOutputStream = new FileOutputStream(output)) {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.transform(new DOMSource(childElement), new StreamResult(fileOutputStream));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
