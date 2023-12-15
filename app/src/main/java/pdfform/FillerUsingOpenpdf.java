package pdfform;

import java.io.InputStream;
import java.io.FileOutputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.XfaForm;


public class FillerUsingOpenpdf {
    public static void fillXfaData(String src, String dest, InputStream is) {
        System.out.println("src: " + src);
        System.out.println("dest: " + dest);

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(is);
            
            Node rootNode = document.getDocumentElement();
            System.out.println("Root Node: <" + rootNode.getNodeName() + ">");

            Node dataNode = document.getElementsByTagName("xfa:data").item(0);
            System.out.println("Data Node: <" + dataNode.getNodeName() + ">");

            // Edit the Dom elements
            XPath xPath = XPathFactory.newInstance().newXPath();
            String expression = "/datasets/data/IMM_5645/page1/Subform1/Student";
            Node node = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
            if (node != null) {
                System.out.println("Found node: " + node.getNodeName() + " = " + node.getTextContent());
                node.setTextContent("1");
            } else {
                System.out.println("Cannot found node: " + expression);
            }

            fillXfaData(src, dest, dataNode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void fillXfaData(String src, String dest, Node node) {
        System.out.println("Node name: " + node.getNodeName());
        if (node.getNodeName() != "xfa:data") {
            throw new IllegalArgumentException("Node is not <xfa:data>.");
        }

        try (PdfReader reader = new PdfReader(src)) {
            // using append mode to keep old pdf's encryption and some other attribute.
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest), '\0', true);

            AcroFields fields = stamper.getAcroFields();
            XfaForm xfa = fields.getXfa();

            xfa.fillXfaForm(node);
            stamper.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
