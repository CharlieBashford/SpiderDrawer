package spiderdrawer.ui;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import java.util.ArrayList;

/**
 *
 * @author charliebashford
 */
public class DrawingPanel extends JPanel {
    
    private ArrayList<ArrayList<Point>> pointsList = new ArrayList<ArrayList<Point>>();
    
    /**
     * Creates new form DrawFrame
     */
    public DrawingPanel() {
        initComponents();
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	pointsList.add(new ArrayList<Point>());
                draw(e.getX(), e.getY());

            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                draw(e.getX(), e.getY());
            }
        });
    }
    
    public ArrayList<ArrayList<Point>> getPointsList() {
    	return pointsList;
    }
    
    private void draw(int x, int y) {
        pointsList.get(pointsList.size()-1).add(new Point(x,y));
        repaint();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        
        for (int i = 0; i < pointsList.size(); i++) {
            for (int j = 0; j < pointsList.get(i).size() - 2; j++) {
                Point p1 = pointsList.get(i).get(j);
                Point p2 = pointsList.get(i).get(j+1);

                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    private void initComponents() {
        //setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
    }
    
    public BufferedImage createImage() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        paint(g);
        return bufferedImage;
    }
}
