package spiderdrawer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.uoa.cs.ink.Packet;
import com.uoa.cs.ink.PacketProperty;
import com.uoa.cs.ink.Stroke;
import com.uoa.cs.recognizer.DataStructures.MyLibrary;
import com.uoa.cs.recognizer.utilities.Converters;
import com.uoa.cs.recognizer.weka.WekaClassifier;
import com.hp.hpl.inkml.Brush;
import com.hp.hpl.inkml.InkElement;
import com.hp.hpl.inkml.Trace;
import com.hp.hpl.inkml.Ink;

import net.sourceforge.tess4j.TessAPI1;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.TessAPI.TessPageSegMode;
import spiderdrawer.recognizer.RataRecognizer;
import spiderdrawer.recognizer.TessRecognizer;
import spiderdrawer.shape.Arrays;
import spiderdrawer.shape.Box;
import spiderdrawer.shape.Circle;
import spiderdrawer.shape.Connective;
import spiderdrawer.shape.Freeform;
import spiderdrawer.shape.Label;
import spiderdrawer.shape.Line;
import spiderdrawer.shape.Logical;
import spiderdrawer.shape.Point;
import spiderdrawer.shape.Shading;
import spiderdrawer.shape.Spider;
import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;
import static spiderdrawer.Parameters.LETTER_RECOGNITION_WAIT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author charliebashford
 */
public class DrawingPanel extends JPanel {
    
    private final ArrayList<Shape> shapeList = new ArrayList<Shape>();
    private Freeform currentFreeform;
    private boolean recognition;
    private boolean shadingRecognition;
    private boolean connectiveRecognition;
    private RataRecognizer rataRecognizer;
    private TessRecognizer tessRecognizer;
    private Movable toMove;
    private Point from = null;
    private Box drawingBox;
    
    
    /**
     * Creates new form DrawFrame
     */
    public DrawingPanel() {
        initComponents();
        rataRecognizer = new RataRecognizer("lib/test10.model");
        tessRecognizer = new TessRecognizer();
        drawingBox = Box.create(0, 0, getWidth(), getHeight(), shapeList);
        shapeList.add(drawingBox);
        recognition = true;
        shadingRecognition = false;
        connectiveRecognition = false;
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
               drawingBox.resize(getWidth(), getHeight());
            }

         });
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
	        	from = new Point(e.getX(), e.getY());
            	if (SwingUtilities.isRightMouseButton(e)) {
            		System.out.println("Right clicking...");
            		return;
            	}
	        	double minDist = Double.MAX_VALUE;
	        	for (int i = 0; i < shapeList.size(); i++) {
	        		if (shapeList.get(i) instanceof Movable) {
	        			Movable movable = (Movable) shapeList.get(i);
	        			if (movable.equals(drawingBox)) {
	        				continue;
	        			}
	        			double dist = movable.boundaryDistance(from);
	        			if (movable.isWithin(from) && dist < minDist) {
	        				toMove = movable;
	        				minDist = dist;
	        			}
	        		}
	        	}
	        	if (toMove != null) {
	        		return;
	        	}
	        	currentFreeform = new Freeform(e.getX(), e.getY());
	        	shapeList.add(currentFreeform);
            	repaint();
            }
            
            public void mouseReleased(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e)) {
            		//System.out.println("Right click while release...");
            		Line line = new Line(from, new Point(e.getX(), e.getY()));
            		for (int i = 0; i < shapeList.size(); i++) {
            			if (shapeList.get(i) instanceof Deletable) {
            				Deletable delShape = (Deletable) shapeList.get(i);
            				if (delShape.equals(drawingBox)) {
    	        				continue;
    	        			}
	            			if (delShape.intersects(line)) {
	            				shapeList.remove(i);
	            				delShape.remove();
	            			}
            			}
            		}
            	} else {
	            	if (toMove != null) {
	            		toMove.move(from, new Point(e.getX(), e.getY()));
	            		toMove.recompute(false);
	            		toMove = null;
	            		from = null;
	            		repaint();
	            		return;
	            	}
	            	
	            	if (recognition || connectiveRecognition) {
			        	Thread timeThread = new Thread() {
			                public void run() {
			            		Freeform initial = currentFreeform;
			                    try {
			                        Thread.sleep(LETTER_RECOGNITION_WAIT);
			                    } catch (InterruptedException e) {
			                        e.printStackTrace();
			                    }
			                    
			                    if (recognition && initial != null && initial.equals(currentFreeform)) {
			                    	if (!currentFreeform.getOverlappingFreeforms(Arrays.freeformList(shapeList)).isEmpty()) {
			                    		checkText(true, true);
			                    	} else {
				                    	String resultingClass = rataRecognizer.classify(currentFreeform);
				                    	System.out.println("Result:" + resultingClass);
				                    	shapeList.remove(currentFreeform);
				                    	switch(resultingClass) {
				                    		case "Text": checkText(false, true); break;
				                    		case "Box": shapeList.add(Box.create(currentFreeform, shapeList)); break;
				                    		case "Line": shapeList.add(Line.create(currentFreeform, shapeList)); break;
				                    		case "Circle": shapeList.add(Circle.create(currentFreeform, shapeList)); break;
				                    		case "Dot": shapeList.add(Point.create(currentFreeform, shapeList)); break;
				                    		case "Shading": shapeList.add(Shading.create(currentFreeform, shapeList.toArray(new Shape[0]))); break;
				                    		case "Connective": checkText(true, false); break;
				                    	}
				                    	
			                    	}
			                    } else if (connectiveRecognition && initial != null && initial.equals(currentFreeform)) {
			                    	checkText(true, true);
			                    }
			                    repaint();
			                }
			            };
			
			            timeThread.setDaemon(true);
			            timeThread.start();
	            	} else if (shadingRecognition) {
	            		if (currentFreeform != null) {
	            			shapeList.remove(currentFreeform);
	            			shapeList.add(Shading.create(currentFreeform, shapeList.toArray(new Shape[0])));
	            		}
	            		currentFreeform = null;
	            		repaint();
	            	}
            	}
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
            	if (SwingUtilities.isRightMouseButton(e)) {
            		//System.out.println("Right click while dragging...");
            		Point to = new Point(e.getX(), e.getY());
            		Line line = new Line(from, to);
            		for (int i = 0; i < shapeList.size(); i++) {
            			if (shapeList.get(i) instanceof Deletable) {
            				Deletable delShape = (Deletable) shapeList.get(i);
            				if (delShape.equals(drawingBox)) {
    	        				continue;
    	        			}
	            			if (delShape.intersects(line)) {
	            				shapeList.remove(i);
	            				delShape.remove();
	            			}
            			}
            		}
            		from = to;
            	} else {
	            	if (toMove != null) {
	            		Point to = new Point(e.getX(), e.getY());
	            		toMove.move(from, to);
	            		toMove.recompute(true);
	            		from = to;
	            	} else {
	            		currentFreeform.addPoint(e.getX(), e.getY());
	            	}
            	}
                repaint();
            }
        });
    }
    
    public void addCircle() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int radius = 30;
    	shapeList.add(Circle.create(x, y, radius, shapeList));
    	repaint();
    }
    
    public void addLine() {
    	Random generator = new Random();
    	int startX = generator.nextInt(getWidth());
    	int startY = generator.nextInt(getHeight());
    	int endX = generator.nextInt(getWidth());
    	int endY = generator.nextInt(getHeight());
    	shapeList.add(Line.create(startX, startY, endX, endY, shapeList));
    	repaint();
    }
    
    public void addPoint() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	shapeList.add(Point.create(x, y, shapeList));
    	repaint();
    }
    
    public void addLabel() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int letterNumber = generator.nextInt(26);
    	char letter = (char) ('A' + letterNumber);
    	shapeList.add(Label.create(letter, new Point(x, y), shapeList));
    	repaint();
    }
    
    public void addLabel(char letter) {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	shapeList.add(Label.create(letter, new Point(x, y), shapeList));
    	repaint();
    }
    
    public void addBox() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int height = 50 + generator.nextInt(51);
    	int width = 50 + generator.nextInt(51);
    	shapeList.add(Box.create(x, y, width, height, shapeList));
    	repaint();
    }
    
    public void addConnective() {
    	Random generator = new Random();
    	int x = generator.nextInt(getWidth());
    	int y = generator.nextInt(getHeight());
    	int type = generator.nextInt(5);
    	shapeList.add(Connective.create(Logical.create(type), x, y, shapeList));
    	repaint();
    }
    
    public void turnOffRecognition() {
    	recognition = false;
    }
    
    public void turnOnRecognition() {
    	recognition = true;
    }
    
    public boolean isRecognition() {
    	return recognition;
    }
    
    public void turnOffShadingRecognition() {
    	shadingRecognition = false;
    }
    
    public void turnOnShadingRecognition() {
    	shadingRecognition = true;
    }
    
    public boolean isShadingRecognition() {
    	return shadingRecognition;
    }
    
    public void turnOffConnectiveRecognition() {
    	connectiveRecognition = false;
    }
    
    public void turnOnConnectiveRecognition() {
    	connectiveRecognition = true;
    }
    
    public boolean isConnectiveRecognition() {
    	return connectiveRecognition;
    }
    
    public void clearDrawable() {
    	shapeList.clear();
    }
    
    public String drawablesAsString() {
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < shapeList.size(); i++) {
    		sb.append(shapeList.get(i).getClass().getName() + "\n");
    		if (shapeList.get(i) instanceof Freeform) {
    			sb.append(((Freeform) shapeList.get(i)).pointsAsString());
    		} else if (shapeList.get(i) instanceof Label) {
    			sb.append(((Label) shapeList.get(i)).asString());
    		} else if (shapeList.get(i) instanceof Circle) {
    			sb.append(((Circle) shapeList.get(i)).asString());
    		} 
    		if (i != shapeList.size() - 1) {
    			sb.append('\n');
    		}
    	}
    	return sb.toString();
    }

    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public boolean loadDrawablesString(String str) {
    	shapeList.clear();
    	if (str == null) {
    		return false;
    	}
    	String[] line = str.split("\n");
    	if (line.length == 0 || line.length % 2 == 1)
    		return false;
    	for (int i = 0; i < line.length; i += 2) {	
    		String[] parameters = line[i+1].split(",");
			try {
				Class newDrawableClass = Class.forName(line[i]);
				
				if (line[i].equals(Freeform.class.getCanonicalName())) {
					Class[] types = {ArrayList.class};
					ArrayList<Point> points = new ArrayList<Point>();
					for (int j = 0; j < parameters.length; j += 2) {
						points.add(new Point(Integer.parseInt(parameters[j]), Integer.parseInt(parameters[j+1])));
					}
					Constructor constructor = newDrawableClass.getConstructor(types);
	    			Object[] params = {points};
	    			shapeList.add((Drawable)constructor.newInstance(params));
				} else if (line[i].equals(Label.class.getCanonicalName())) {
					Class[] types = {Character.TYPE, Integer.TYPE, Integer.TYPE};
	    			Constructor constructor = newDrawableClass.getConstructor(types);
	    			Object[] params = {parameters[0].charAt(0), Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2])};
	    			shapeList.add((Drawable)constructor.newInstance(params));
				} else if (line[i].equals(Circle.class.getCanonicalName())) {
					Class[] types = {Integer.TYPE, Integer.TYPE, Integer.TYPE};
	    			Constructor constructor = newDrawableClass.getConstructor(types);
	    			Object[] params = {Integer.parseInt(parameters[0]), Integer.parseInt(parameters[1]), Integer.parseInt(parameters[2])};
	    			shapeList.add((Drawable)constructor.newInstance(params));
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

    	}
    	return true;
    }
    
    private Freeform[] intersectingFreeforms(Freeform f) {
    	Set<Freeform> intersectingFreeforms = f.overlappingFreeforms(Arrays.freeformList(shapeList), new HashSet<Freeform>());
    	return intersectingFreeforms.toArray(new Freeform[0]);
    }
    
    private void checkText(boolean connective, boolean letter) {
    	Freeform[] freeforms = intersectingFreeforms(currentFreeform);
		Rectangle rect = surroundingRectangle(freeforms);
		Character character = (connective)? ((letter)? tessRecognizer.classifyText(freeforms) : tessRecognizer.classifyConnective(freeforms)) : tessRecognizer.classifyLetter(freeforms);
		if (character != null) {
			Point center = new Point((int)rect.getCenterX(), (int)rect.getCenterY());
			Shape shape;
			if (Character.isAlphabetic((int) character))
				shape = Label.create(character, center, shapeList);
			else 
				shape = Connective.create(Logical.create(character), center, shapeList);
			shapeList.add(shape);
			for (int i = 0; i < freeforms.length; i++)
				shapeList.remove(freeforms[i]);
		}
    }
    
    public static Rectangle surroundingRectangle(Freeform[] freeforms) {
		int minX = Integer.MAX_VALUE;
    	int minY = Integer.MAX_VALUE;
    	int maxX = Integer.MIN_VALUE;
    	int maxY = Integer.MIN_VALUE;
    	for (int i = 0; i < freeforms.length; i++) {
    		int currentMinX = freeforms[i].minX();
    		if (currentMinX < minX) {
    			minX = currentMinX; 
    		}
    		int currentMinY = freeforms[i].minY();
    		if (currentMinY < minY) {
    			minY = currentMinY; 
    		}
    		int currentMaxX = freeforms[i].maxX();
    		if (currentMaxX > maxX) {	
    			maxX = currentMaxX; 
    		}
    		int currentMaxY = freeforms[i].maxY();
    		if (currentMaxY > maxY) {
    			maxY = currentMaxY; 
    		}
    	}    
    	return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(2));
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        for (int i = 0; i < shapeList.size(); i++) {
        	if (shapeList.get(i) instanceof Circle) {
        		Circle circle = (Circle) shapeList.get(i);
        		circle.draw(g2);
        	}
        }
        for (int i = 0; i < shapeList.size(); i++) {
        	if (shapeList.get(i) instanceof Drawable && !(shapeList.get(i) instanceof Circle)) {
        		Drawable drawable = (Drawable) shapeList.get(i);
        		if (drawable.equals(drawingBox)) {
        			if (drawingBox.isValid())
        			continue;
        		}
        		drawable.draw(g2);
        	}
        }
    }

    private void initComponents() {
    	setBackground(Color.WHITE);
    }
    
    public BufferedImage createImage() {
        BufferedImage bufferedImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bufferedImage.createGraphics();
        paint(g);
        return bufferedImage;
    }
}
