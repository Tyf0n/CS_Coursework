import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.awt.image.BufferedImage;

public class View extends JFrame {
    
    /** Underlying array for framebuffer */
    protected int[] screen;
    protected BufferedImage framebuffer;

    private final int WIDTH  = 512;
    private final int HEIGHT = 512;

    private Mesh mesh;

    float cameraZ = 5;

    public View(String filename) {
        super("3D");

        mesh = new Mesh(filename);

        framebuffer = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        screen = new int[WIDTH * HEIGHT];

        renderScene();
        setSize(WIDTH, HEIGHT);
        setResizable(false);

        int fps = 30;
        new java.util.Timer().schedule(new java.util.TimerTask() {
                public void run() {
                    oneFrame();
                }}, 0, (int)(1000 / fps));
    }

    /** Process one frame of animation */
    protected void oneFrame() {
        renderScene();
        mesh.rotate((float)Math.toRadians(7));
        repaint();
    }

    /** Called from oneFrame */
    synchronized private void renderScene() {
        Graphics3D renderer = new Graphics3D(framebuffer, cameraZ); 

        renderer.setColor(Color.white);
        renderer.clear();

        mesh.render(renderer);
    }
    
    synchronized public void paint(Graphics _g) {
        Graphics2D g = (Graphics2D)_g;
        g.drawImage(framebuffer, 
                    0, 0, getWidth(), getHeight(), 
                    0, 0, framebuffer.getWidth(), framebuffer.getHeight(), 
                    null);
    }

    static public void main(String[] arg) {
        String filename = "cube.xml";

        if (arg.length > 0) {
            filename = arg[0];
        }

        new View(filename).setVisible(true);
    }
}
