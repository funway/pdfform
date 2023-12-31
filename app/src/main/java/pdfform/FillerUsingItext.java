package pdfform;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.xfa.XfaForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FillerUsingItext {

    public static void fillXfaData(String src, String dest, InputStream is) throws Exception {
        System.out.println("Fillout by Itext");
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
            // XPath xPath = XPathFactory.newInstance().newXPath();
            // String expression = "/datasets/data/IMM_5645/page1/Subform1/Student";
            // Node node = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
            // if (node != null) {
            //     System.out.println("Found node: " + node.getNodeName() + " = " + node.getTextContent());
            //     node.setTextContent("1");
            // } else {
            //     System.out.println("Cannot found node: " + expression);
            // }

            fillXfaData(src, dest, dataNode);
        } catch (Exception e) {
            throw e;
        }

    }

    public static void fillXfaData(String src, String dest, Node node) throws Exception {
        System.out.println("Node name: " + node.getNodeName());
        if (node.getNodeName() != "xfa:data") {
            throw new IllegalArgumentException("Node is not <xfa:data>.");
        }

        // Get first ELEMENT_NODE (exclude TEXT_NODE, COMMENT_NODE ...)
        Node dataNode = getFirstElementNode(node);
        if (dataNode == null) {
            throw new Exception("No valid element found under the given node.");
        }

        try (PdfReader reader = new PdfReader(src);
            PdfWriter writer = new PdfWriter(dest)) {

            reader.setUnethicalReading(true);

            StampingProperties sp = new StampingProperties();
            sp.useAppendMode();
            sp.preserveEncryption();
            
            // using StampingProperties to keep old pdf's encryption and other attribute. 
            PdfDocument pdfDoc = new PdfDocument(reader, writer, sp);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, true);
            XfaForm xfa = acroForm.getXfaForm();

            xfa.fillXfaForm(dataNode);
            xfa.write(pdfDoc);

            pdfDoc.close();
        } catch (Exception e) {
            throw e;
        }
    }

    private static Node getFirstElementNode(Node node) {
        NodeList nodeList = node.getChildNodes();

        for(int i=0; i<nodeList.getLength(); i++) {
            Node chilNode = nodeList.item(i);
            if (chilNode.getNodeType() == Node.ELEMENT_NODE) {
                return chilNode;
            }
        }
        return null;
    }
    
}
