import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
// Needed for XML parser
import javax.xml.parsers.*;
import org.w3c.dom.*;
import java.io.File;

/**
   3D Mesh that parses XML mesh files

  <P>
  Morgan McGuire
  <BR>morgan@cs.williams.edu   
 */
public class Mesh {
    
    /** 
        All of the triangles in this mesh, in no particular order.
        Triangles are stored with vertices in camera space.
     */
    protected Array<Triangle>   triArray = new Array<Triangle>();

    /**
       @param filename Name of a mesh .xml file

       Based on code by Kiran Pai from http://www.developerfusion.co.uk/show/2064/
     */
    public Mesh(String filename) {
        loadXML(filename);
    }

    /** Sorts all triangles from smallest to largest average z-value. */
    protected void sort() {
        triArray.radixSort(200, new TriangleComparator(), new TriangleEvaluator());
    }
    
    /** Draws this mesh on the 3D surface. */
    public void render(Graphics3D g) {
        sort();

        //for (Triangle t : triArray) {
        Iterator it = triArray.iterator();
        while (it.hasNext()){
            Triangle t = (Triangle)it.next();
            //End of what I added.
            t.render(g);
        }
    }
    
    /** Rotates by angle radians about the Y-axis. */
    public void rotate(float angle) {
        float c = (float)Math.cos(angle);
        float s = (float)Math.sin(angle);

        //for (Triangle t : triArray) {
        Iterator it = triArray.iterator();
        while (it.hasNext()){
            Triangle t = (Triangle)it.next();
            //End what I inserted
            for (int v = 0; v < 3; ++v) {
                float x = t.x[v];
                float z = t.z[v];
                t.x[v] = c * x + s * z;
                t.z[v] = c * z - s * x;
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    ///                          All Parsing Code Below Here                                ///
    ///////////////////////////////////////////////////////////////////////////////////////////


    private void loadXML(String filename) {
        try {
            System.out.println("Loading " + filename);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File(filename));
            
            // normalize text representation
            doc.getDocumentElement().normalize();
            String rootName = doc.getDocumentElement().getNodeName();
            assert rootName.equals("mesh") : "Not a mesh file: root = " + rootName;

            // Extract all faces
            NodeList faceList = doc.getElementsByTagName("face");
            int n = faceList.getLength();

            System.out.print("  Loading " + n + " faces...");

            triArray.clear();
            for (int i = 0; i < n; ++i) {
                triArray.add(readTriangle(faceList.item(i)));
            }

            System.out.println("done.");
            
        } catch (Exception e) {
            System.err.println("Error while parsing '" + filename + "': " + e);
        }
    }

    /** Extracts the color = "..." attribute from an XML node and turns
       it into a java.awt.Color. */
    private Color readColor(Node node) {
        NamedNodeMap map = node.getAttributes();
        return Color.decode(map.getNamedItem("color").getNodeValue());
    }

    /** Reads the floating point value whose name is attr from an XML named map.*/
    private float readFloat(NamedNodeMap map, String attr) {
        return Float.parseFloat(map.getNamedItem(attr).getNodeValue());
    }


    /** Reads a triangle from an XML <face> node */
    private Triangle readTriangle(Node faceNode) {
        Triangle tri = new Triangle();        

        if (faceNode.getNodeType() == Node.ELEMENT_NODE) {
            tri.color = readColor(faceNode);
            
            Element faceElement = (Element)faceNode;
            
            float scale = 3.0f;

            NodeList vertexList = faceElement.getElementsByTagName("vertex");
            for (int v = 0; v < vertexList.getLength(); ++v) {
                NamedNodeMap attributes = vertexList.item(v).getAttributes();
                tri.x[v] = readFloat(attributes, "x") * scale;
                tri.y[v] = readFloat(attributes, "y") * scale;
                tri.z[v] = readFloat(attributes, "z") * scale;
            }
        }

        return tri;
    }

}
