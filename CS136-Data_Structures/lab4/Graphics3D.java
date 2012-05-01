import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 Simple 3D renderer based on Java 2D.

  <P>
  Morgan McGuire
  <BR>morgan@cs.williams.edu
 */
public class Graphics3D {
    private Graphics2D g;

    int width;
    int height;

    /** Center of the screen */
    float cx, cy;

    float cameraZ;

    /** Multiplies all camera space coordinates. */
    float scale;

    /** Projected polygon data used in render() */
    private final static int[] xPoints = new int[3];
    private final static int[] yPoints = new int[3];

    public Graphics3D(BufferedImage framebuffer, float cameraZ) {
        g = framebuffer.createGraphics();
                
        width = framebuffer.getWidth();
        height = framebuffer.getHeight();

        this.cameraZ = cameraZ;

        // Set up a coordinate system where (0, 0) is the center of the screen
        // and the screen is 1x1 (except for aspect ratio)
        cx = width / 2.0f;
        cy = height / 2.0f;
        scale = width;
    }

    /** Clears the screen to the current color */
    public void clear() {
        g.fillRect(0, 0, width, height);
    }


    public void setColor(Color color) {
        g.setColor(color);
    }


    /** Not thread-safe: Assumes all rendering is single-threaded. */
    public void fillTriangle(float[] x, float[] y, float[] z) {    

        // Perspective projection into shared array (assumes that this
        // code is single-threaded)
        for (int v = 0; v < 3; ++v) {
            float temp = scale / (cameraZ - z[v]);

            xPoints[v] = (int)(cx + x[v] * temp);
            // Y-axis is upside down between 2D and 3D
            yPoints[v] = (int)(cy - y[v] * temp);

            //System.out.println("(" + x[v] + ", " + y[v] + ")");
            //System.out.println("(" + xPoints[v] + ", " + yPoints[v] + ")");
        }

        g.fillPolygon(xPoints, yPoints, 3);
    }
}

