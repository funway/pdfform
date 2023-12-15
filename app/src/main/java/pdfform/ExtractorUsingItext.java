package pdfform;

import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.forms.fields.PdfFormField;
import com.itextpdf.forms.xfa.XfaForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;

import java.io.FileWriter;
import java.io.FileOutputStream;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ExtractorUsingItext {
    public static String getGreeting() {
        // return this.getClass().getName();
        return ExtractorUsingItext.class.getName();
    }

    /**
     * Extract Acroform fields from a PDF file, and save as a text file. 
     * @param src the path of source PDF file
     * @param dest the output file
     */
    public static void extractAcroformFields(String src, String dest) {
        try (PdfReader reader = new PdfReader(src)) {
            
            reader.setUnethicalReading(true);
            
            PdfDocument pdfDoc = new PdfDocument(reader);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
            
            if (acroForm != null) {
                Map<String, PdfFormField> fields = acroForm.getFormFields();
                
                if (fields.size() == 0) {
                    System.out.println("iText: The pdf document does not contain an Acroform field.");
                    return ;
                }
                
                FileWriter writer = new FileWriter(dest);
                for (Map.Entry<String, PdfFormField> entry : fields.entrySet()) {
                    String fieldName = entry.getKey();
                    PdfFormField field = entry.getValue();
                    String fieldValue = field.getValueAsString();

                    writer.write("Field Name: " + fieldName + "\n");
                    writer.write("Field Value: " + fieldValue + "\n");
                    writer.write("---------------------------\n");
                }

                writer.close();
            } else {
                System.out.println("iText: The pdf document does not contain an Acroform.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extract all level-1 subnodes from XFA structure in a XFA PDF.
     * @param src src the path of source PDF file
     * @param destPath the dest folder to save subnodes' XML data
     */
    public static void extractXfa(String src, String destPath) {

        try (PdfReader reader = new PdfReader(src)) {
            
            reader.setUnethicalReading(true);
            
            PdfDocument pdfDoc = new PdfDocument(reader);
            PdfAcroForm acroForm = PdfAcroForm.getAcroForm(pdfDoc, false);
            
            if (acroForm != null && acroForm.getXfaForm() != null) {
                XfaForm xfa = acroForm.getXfaForm();
                Document domDoc = xfa.getDomDocument();

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
                                // byte[] bytes = childElement.getTextContent().getBytes();
                                // fileOutputStream.write(bytes);

                                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                                transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                                transformer.transform(new DOMSource(childElement), new StreamResult(fileOutputStream));
                            }
                        }
                    }
                }

            } else {
                System.out.println("iText: The pdf document does not contain an XFA form.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}