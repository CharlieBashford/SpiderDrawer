package spiderdrawer.shape;

import static spiderdrawer.Parameters.*;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;

import javax.swing.UIManager;

import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;

public class Connective implements Drawable, Movable, Deletable {
	
	Logical logical;
	Point center;
	int width;
	int height;
	private ArrayList<Shape> shapeList;
	Box leftBox;
	Box rightBox;
	Box outerBox;
	
	public Connective(Logical logical, Point center) {
		this.logical = logical;
		this.center = center;
		setHeightAndWidth();
	}
	
	public static Connective create(Logical logical, int x, int y, ArrayList<Shape> shapeList) {
		Connective connective = new Connective(logical, new Point(x,y));
		connective.shapeList = shapeList;
		connective.recompute(false);
		return connective;
	}
	
	private char asChar() {
		switch (logical) {
			case EQUIVALENCE: return '\u2194';
			case IMPLICATION: return '\u2192';
			case DISJUNCTION: return '\u2228';
			case CONJUNCTION: return '\u2227';
			case NEGATION:	  return '\u00AC';
			default: return '\0';
		}
	}

	
	private void setHeightAndWidth() {
		Font font = new Font(UIManager.getDefaults().getFont("TabbedPane.font").getFontName(), Font.PLAIN, CONNECTIVE_FONT_SIZE);
		Canvas c = new Canvas();
		width = c.getFontMetrics(font).stringWidth(Character.toString(this.asChar())) - 3;
		height = c.getFontMetrics(font).getAscent()/2;
	}
	
	protected void setLeftBox(Box box) {
		System.out.println("setting left box " + box);
		Box oldBox = leftBox;
		leftBox = box;
		if (oldBox != null) {
			oldBox.setConnective(null, false);
		}
		if (leftBox != null && !this.equals(leftBox.getConnective())) {
			leftBox.setConnective(this, false);
		}
	}
	
	protected void setRightBox(Box box) {
		System.out.println("setting right box " + box);
		Box oldBox = rightBox;
		rightBox = box;
		if (oldBox != null) {
			oldBox.setConnective(null, true);
		}
		if (rightBox != null && !this.equals(rightBox.getConnective())) {
			rightBox.setConnective(this, true);
		}
	}
	
	protected void setOuterBox(Box box) {
		Box oldBox = outerBox;
		outerBox = box;
		if (oldBox != null) {
			oldBox.removeInnerConnective(this);
		}
		if (outerBox != null && !outerBox.containsInnerConnective(this)) {
			outerBox.addInnerConnective(this);
		}
	}
	
	protected Box getLeftBox() {
		return leftBox;
	}
	
	protected Box getRightBox() {
		return rightBox;
	}
	
	protected Box getOuterBox() {
		return outerBox;
	}
	
	protected boolean isFullyConnected() {
		if (logical == Logical.NEGATION) {
			return (rightBox != null);
		}
		return (leftBox != null) && (rightBox != null);
	}

	@Override
	public void draw(Graphics2D g2) {
		char[] array = new char[1];
		array[0] = this.asChar();
		if (!isFullyConnected()) {
			g2.setColor(Color.RED);
		} else {
			g2.setColor(Color.BLACK);
		}
			
		g2.drawRect(center.x - width/2, center.y - height/2, width, height);
		g2.setFont(new Font(g2.getFont().getFontName(), Font.PLAIN, CONNECTIVE_FONT_SIZE));
		if (logical == Logical.EQUIVALENCE || logical == Logical.IMPLICATION || logical == Logical.NEGATION) //Shift it down by 5, due to dimensions bug.
			g2.drawChars(array, 0, 1, center.getX() - width/2, center.getY() + 5 + height/2);
		else
			g2.drawChars(array, 0, 1, center.getX() - width/2, center.getY() + height/2);
		
		g2.setColor(Color.BLACK);
	}

	
	public void move(Point from, Point to, boolean external) {
		if (external) {
			center.move(from, to);
			if (leftBox != null)
				leftBox.move(from, to, true);
			rightBox.move(from, to, true);
		} else {
			move(from, to);
		}
	}
	
	@Override
	public void move(Point from, Point to) {
		center.move(from, to);
		if (leftBox != null && this.leftDistance(leftBox) > CONNECTIVE_BOX_DIST) {
			setLeftBox(null);
		}
		if (rightBox != null && this.rightDistance(rightBox) > CONNECTIVE_BOX_DIST) {
			setRightBox(null);
		}
	}

	private Box[] boxArray(Shape[] shapes) {
		ArrayList<Box> boxList = new ArrayList<Box>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Box) {
				boxList.add((Box) shapes[i]);
			}
		}
		return boxList.toArray(new Box[0]);
 	}
	
	public void computeBoxes(Box[] boxes) {
		int leftBoxPos = -1;
		int rightBoxPos = -1;
		double lowestLeftDist = Double.MAX_VALUE;
		double lowestRightDist = Double.MAX_VALUE;
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].connective != null)
				continue;
			double leftDist = this.leftDistance(boxes[i]);
			double rightDist = this.rightDistance(boxes[i]);
			if (leftDist < lowestLeftDist) {
				lowestLeftDist = leftDist;
				leftBoxPos = i;
			}
			if (rightDist < lowestRightDist) {
				lowestRightDist = rightDist;
				rightBoxPos = i;
			}
		}
		
		if (this.logical == Logical.NEGATION) //If negation, then can only have right box.
			leftBoxPos = -1;
		
		if (leftBox == null) {
			if (lowestLeftDist <= CONNECTIVE_BOX_DIST && leftBoxPos != -1) {
				System.out.println("close to leftBox");
				this.setLeftBox(boxes[leftBoxPos]);
			}
		}
		if (rightBox == null) {
			if (lowestRightDist <= CONNECTIVE_BOX_DIST && rightBoxPos != -1) {
				System.out.println("close to rightBox");
				this.setRightBox(boxes[rightBoxPos]);
			}
		}
	}
	
	public void computeOuterBoxes(Box[] boxes) {
		if (outerBox != null)
			this.setOuterBox(null);
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].contains(this) && !boxes[i].innerBoxesContains(this)) {
				this.setOuterBox(boxes[i]);
				break;
			}
		}
	}

	@Override
	public void recompute(boolean moving) {
		if (shapeList == null)
			return;
		Shape[] shapes = shapeList.toArray(new Shape[0]);
		computeBoxes(boxArray(shapes));
		computeOuterBoxes(boxArray(shapes));
	}
	
	public double boundaryDistance(Point p) {
		Box box = new Box(center.x - width/2, center.y - height/2, width, height);
		return box.distance(p);
	}
	
	protected double distance(Box box) {
		return box.distance(this);
	}
	
	protected double leftDistance(Box box) { //Swap left and right because change of perspective.
		return box.rightDistance(this);
	}
	
	protected double rightDistance(Box box) { //Swap left and right because change of perspective.
		return box.leftDistance(this);
	}

	@Override
	public boolean isWithin(Point p) {
		return ((Math.abs(p.getX() - center.getX()) < (double)width/2) && (Math.abs(p.getY() - center.getY()) < (double)height/2));
	}

	@Override
	public boolean intersects(Line line) {
		Box box = new Box(center.x - width/2, center.y - height/2, width, height);
		return box.intersects(line);
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}
	
	
}
