package spiderdrawer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import spiderdrawer.drawable.Circle;
import spiderdrawer.drawable.Drawable;
import spiderdrawer.drawable.Freeform;
import spiderdrawer.drawable.Line;
import spiderdrawer.drawable.Label;

import java.util.ArrayList;
import java.util.Random;

/**
 *
 * @author charliebashford
 */
public class DrawingPanel extends JPanel {
    
    private ArrayList<Drawable> drawableList = new ArrayList<Drawable>();
    private Freeform currentFreeform;
    /**
     * Creates new form DrawFrame
     */
    public DrawingPanel() {
        initComponents();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	currentFreeform = new Freeform(e.getX(), e.getY());
            	drawableList.add(currentFreeform);
            	repaint();
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                currentFreeform.addPoint(e.getX(), e.getY());
                repaint();
            }
        });
    }
    
    public void addCircle() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int radius = 50 + generator.nextInt(51);
    	drawableList.add(new Circle(x, y, radius));
    	repaint();
    }
    
    public void addLine() {
    	Random generator = new Random();
    	int startX = generator.nextInt(getWidth());
    	int startY = generator.nextInt(getHeight());
    	int endX = generator.nextInt(getWidth());
    	int endY = generator.nextInt(getHeight());
    	drawableList.add(new Line(startX, startY, endX, endY));
    	repaint();
    }
    
    public void addLabel() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int letterNumber = generator.nextInt(26);
    	char letter = (char) ('A' + letterNumber);
    	drawableList.add(new Label(letter, x, y));
    	repaint();
    }
    
    public void clearDrawable() {
    	drawableList.clear();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setFont(new Font(g2.getFont().getFontName(),Font.PLAIN,20));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < drawableList.size(); i++) {
            drawableList.get(i).draw(g2);
        }
    }

    private void initComponents() {
        //setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    	setBackground(Color.WHITE);
    }
    
    public BufferedImage createImage() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        paint(g);
        return bufferedImage;
    }
}
