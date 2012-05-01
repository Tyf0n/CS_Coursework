import java.awt.Color;

/**
  A simple representation of a triangle in 3D.
  
  <P>
  Morgan McGuire
  <BR>morgan@cs.williams.edu
*/
public class Triangle {

    /** x[i] is the position of vertex[i]; there are exactly 3
        vertices in a triangle. */
    public final float[]  x = new float[3];
    public final float[]  y = new float[3];
    public final float[]  z = new float[3];
    public       Color    color;

    /** Constructs a black triangle at (0,0,0) */
    public Triangle() {
        color = Color.black;
    }

    /** Sum of the z-values.  Used for sorting.*/
    public float zSum() {
        return (z[0] + z[1] + z[2]);
    }

    /** 
        Used methods on g to set the view scale and offset.

        @param cameraZ Position of the center of projection
        along the Z axis.
    */
    public void render(Graphics3D g) {
        g.setColor(color);
        g.fillTriangle(x, y, z);
    }
}
