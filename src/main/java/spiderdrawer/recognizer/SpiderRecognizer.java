package spiderdrawer.recognizer;

import java.util.ArrayList;

import spiderdrawer.shape.Arrays;
import spiderdrawer.shape.Box;
import spiderdrawer.shape.Circle;
import spiderdrawer.shape.Freeform;
import spiderdrawer.shape.Point;
import spiderdrawer.shape.interfaces.Shape;
import static spiderdrawer.Parameters.*;


public class SpiderRecognizer {

    private final ArrayList<Shape> shapeList;
    
    public SpiderRecognizer(ArrayList<Shape> shapeList) {
    	this.shapeList = shapeList;
    }
	
	public boolean closeToUnconnectedBox(Freeform[] freeforms) {
		System.out.println("Testing closeToUnconnectedBox");
		Freeform freeform = new Freeform(freeforms);
		Box[] boxes = Arrays.boxArray(shapeList);
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].getConnective() != null)
				continue;
			System.out.println("i" + i);
			System.out.println("Left dist" + freeform.leftDistance(boxes[i]));
			System.out.println("Right dist" + freeform.rightDistance(boxes[i]));
			if (freeform.leftDistance(boxes[i]) < CONNECTIVE_BOX_DIST) {
				return true;
			}
			if (freeform.rightDistance(boxes[i]) < CONNECTIVE_BOX_DIST) {
				return true;
			}
		}
		return false;
	}
	
	public boolean closeToUnlabeledCircle(Freeform[] freeforms) {
		Freeform freeform = new Freeform(freeforms);
		Point center = new Point((freeform.minX()+freeform.maxX())/2, (freeform.minY()+freeform.maxY())/2);
		Circle[] circles = Arrays.circleArray(shapeList);
		for (int i = 0; i < circles.length; i++) {
			if (circles[i].hasLabel())
				continue;
			double dist = circles[i].signedDistance(center);
			if (dist < LABEL_CIRCLE_DIST && dist >= 0) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isTextSize(Freeform freeform) {
		if (freeform.maxX() - freeform.minX() > MAX_TEXT_WIDTH)
			return false;
		if (freeform.maxY() - freeform.minY() > MAX_TEXT_HEIGHT)
			return false;
		return true;
			
	}
	
}
