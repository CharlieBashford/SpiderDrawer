package spiderdrawer.shape;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;
import static spiderdrawer.Parameters.*;

public class Circle implements Drawable, Movable, Deletable {

	Point center;
	int radius;
	private ArrayList<Shape> shapeList;
	Label label;
	boolean moveLabel;
	ArrayList<Point> points;
	ArrayList<Shading> shadings;
	Box box;
	ArrayList<Box> innerBoxes;
	ArrayList<Box> overlapBoxes;
	
	public Circle(Point center, int radius) {
		this.center = center;
		this.radius = Math.max(radius,MIN_CIRCLE_RADIUS);
	}
	
	public Circle(int centerX, int centerY, int radius) {
		this(new Point(centerX, centerY), radius);
	}
	
	public static Circle create(int centerX, int centerY, int radius, ArrayList<Shape> shapeList) {
		Circle circle = new Circle(centerX, centerY, radius);
		circle.shapeList = shapeList;
		circle.recompute(false);
		return circle;
	}
	
	public static Circle create(Freeform freeform, ArrayList<Shape> shapeList) {
		int x = (freeform.minX() + freeform.maxX())/2;
		int y = (freeform.minY() + freeform.maxY())/2;
		int r = (x - freeform.minX() + y - freeform.minY())/2;
		return create(x, y, r, shapeList);
	}
	
	public Point getCenter() {
		return center;
	}
	
	public int getRadius() {
		return radius;
	}
	
	protected boolean hasLabel() {
		return label != null;
	}
	
	protected void setLabel(Label label) {
		Label oldLabel = this.label;
		this.label = label;
		if (oldLabel != null) {
			oldLabel.setCircle(null);
		}
		if (label != null && !this.equals(label.getCircle())) {
			label.setCircle(this);
		}
		
		
	}
	
	protected void addPoint(Point point) {
		if (this.points == null)
			this.points = new ArrayList<Point>();
		points.add(point);
		if (point != null && !point.containsCircle(this)) {
			point.addCircle(this);
		}
	}
	
	protected void removePoint(Point point) {
		points.remove(point);
		if (point != null && point.containsCircle(this)) {
			point.removeCircle(this);
		}
	}
	
	protected boolean containsPoint(Point point) {
		if (points != null)
			return points.contains(point);
		return false;
	}
	
	protected void removeAllPoints() {
		for (int i = 0; i < points.size(); i++) {
			removePoint(points.get(0));
		}
	}
	
	protected void addInnerBox(Box box) {
		if (this.innerBoxes == null)
			this.innerBoxes = new ArrayList<Box>();
		innerBoxes.add(box);
		if (box != null && !box.containsOuterCircle(this)) {
			box.addOuterCircle(this);
		}
	}
	
	protected void removeInnerBox(Box box) {
		innerBoxes.remove(box);
		if (box != null && box.containsOuterCircle(this)) {
			box.removeOuterCircle(this);
		}
	}
	
	protected boolean containsInnerBox(Box box) {
		if (box != null)
			return innerBoxes.contains(box);
		return false;
	}
	
	protected boolean containsInnerBox() {
		return (innerBoxes != null) && (innerBoxes.size() > 0);
	}
	
	protected void removeAllInnerBoxes() {
		while(innerBoxes.size() > 0) {
			removeInnerBox(innerBoxes.get(0));
		}
	}
	
	protected void setBox(Box box) {	
		Box oldBox = this.box;
		this.box = box;
		if (oldBox != null) {
			oldBox.removeCircle(this);
		}
		if (box != null && !box.containsCircle(this)) {
			box.addCircle(this);
		}
	}
	
	protected Box getBox() {
		return box;
	}
	
	protected void addOverlapBox(Box box) {
		if (this.overlapBoxes == null)
			this.overlapBoxes = new ArrayList<Box>();
		overlapBoxes.add(box);
		if (box != null && !box.containsOverlapCircle(this)) {
			box.addOverlapCircle(this);
		}
	}
	
	protected void removeOverlapBox(Box box) {
		overlapBoxes.remove(box);
		if (box != null && box.containsOverlapCircle(this)) {
			box.removeOverlapCircle(this);
		}
	}
	
	protected boolean containsOverlapBox(Box box) {
		if (overlapBoxes != null)
			return overlapBoxes.contains(box);
		return false;
	}
	
	protected boolean containsOverlapBox() {
		return (overlapBoxes != null) && (overlapBoxes.size() > 0);
	}
	
	protected void removeAllOverlapBoxes() {
		for (int i = 0; i < overlapBoxes.size(); i++) {
			removeOverlapBox(overlapBoxes.get(0));
		}
	}
	
	protected void addShading(Shading shading) {
		if (this.shadings == null)
			this.shadings = new ArrayList<Shading>();
		shadings.add(shading);
		if (shading != null && !shading.containsIncluded(this)) {
			shading.addIncluded(this);
		}
	}
	
	protected void removeShading(Shading shading) {
		shadings.remove(shading);
		if (shading != null && shading.containsIncluded(this)) {
			shading.removeIncluded(this);
		}
	}
	
	protected boolean containsShading(Shading shading) {
		if (shadings != null)
			return shadings.contains(shading);
		return false;
	}
	
	protected boolean contains(Freeform freeform) {
		if (freeform.points.size() < 2)
			return false;
		
		for (int i = 0; i < freeform.points.size()-1; i++) {
			Point p1 = freeform.points.get(i);
			Point p2 = freeform.points.get(i+1);
			if (this.distance(new Line(p1, p2)) > 0) {
				return false;
			}
		}
		
		return true;
	}
	
	protected boolean contains(Point point) {
		return (center.distance(point) < radius);
	}
	
	protected boolean contains(Box box) {
		return this.contains(box.getTopLeft()) && this.contains(box.topRight()) && this.contains(box.bottomLeft()) && this.contains(box.bottomRight());
		
	}
	
	protected double distance(Freeform freeform) {
		double minDist = Double.MAX_VALUE;
		for (int i = 0; i < freeform.points.size()-1; i++) {
			Point p1 = freeform.points.get(i);
			Point p2 = freeform.points.get(i+1);
			double dist = this.distance(new Line(p1, p2));
			if (dist < minDist) {
				minDist = dist;
			}
		}
		return minDist;
	}
	
	protected double distance(Line l) {
		return l.distance(this);
	}
	
	protected double distance(Point p) {
		return Math.max(0, signedDistance(p));
	}
	
	public double boundaryDistance(Point p) {
		return Math.abs(signedDistance(p));
	}
	
	protected double signedDistance(Point p) {
		return center.distance(p) - radius;
	}
	
	public double distance(Label label) {
		return label.distance(this);
	}
	
	public double signedDistance(Label label) {
		return label.signedDistance(this);
	}
		
	protected Label getLabel() {
		return label;
	}
	
	public String asString() {
		return center.x + "," + center.y + "," + radius;
	}
	
	@Override
	public void draw(Graphics2D g2) {
		if (shadings != null) {
			for (int i = 0; i < shadings.size(); i++) {
				if (shadings.get(i).included != null && shadings.get(i).included.size() > 0 && shadings.get(i).included.get(0).equals(this)) {
					g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
			    	g2.setColor(Color.BLUE);
					Area area = new Area(new Ellipse2D.Float(center.x-radius, center.y-radius, radius*2, radius*2));
					for (int j = 1; j < shadings.get(i).included.size(); j++) {
						Circle circle = shadings.get(i).included.get(j);
						area.intersect(new Area(new Ellipse2D.Float(circle.center.x-circle.radius, circle.center.y-circle.radius, circle.radius*2, circle.radius*2)));
					}
					if (shadings.get(i).excluded != null) {
						for (int j = 0; j < shadings.get(i).excluded.size(); j++) {
							Circle circle = shadings.get(i).excluded.get(j);
							area.subtract(new Area(new Ellipse2D.Float(circle.center.x-circle.radius, circle.center.y-circle.radius, circle.radius*2, circle.radius*2)));
						}
					}
					g2.fill(area);
				    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
				}
			}
		}
		if (!hasLabel() || containsOverlapBox() || containsInnerBox()) {
			g2.setColor(Color.RED);
		} else {
			g2.setColor(Color.BLACK);
		}
		g2.drawOval((int)(center.getX()-radius), (int)(center.getY()-radius), radius*2, radius*2);
		g2.setColor(Color.BLACK);
	}

	@Override
	public void move(Point from, Point to) {
		center.move(from, to);
		if (label != null) {
			if (moveLabel) {
				label.move(from, to);
			} else if (label.distance(this) > LABEL_CIRCLE_DIST) {
				setLabel(null);
			}		
		}
		
	}
	
	private Label[] labelArray(Shape[] shapes) {
		ArrayList<Label> labelList = new ArrayList<Label>();
		Label label;
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Label) {
				label = (Label) shapes[i];
				if (!label.hasCircle()) {
					labelList.add(label);
				}
			}
		}
		return labelList.toArray(new Label[0]);
 	}
	
	private Point[] pointArray(Shape[] shapes) {
		ArrayList<Point> pointList = new ArrayList<Point>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Point) {
				pointList.add((Point) shapes[i]);
			}
		}
		return pointList.toArray(new Point[0]);
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
	
	protected void computeLabels(Label[] labels) {
		int labelPos = -1;
		double lowestDist = Double.MAX_VALUE;
		for (int i = 0; i < labels.length; i++) {
			if (labels[i].hasCircle())
				continue;
			double dist = this.distance(labels[i]);
			if (dist < lowestDist) {
				lowestDist = dist;
				labelPos = i;
			}
		}
		if ((label == null || lowestDist < label.distance(this)) && lowestDist <= LABEL_CIRCLE_DIST && labelPos != -1) {
			this.setLabel(labels[labelPos]);
			moveLabel = false;
		}
	}
	
	private void computePoints(Point[] points) {
		if (this.points == null) {
			this.points = new ArrayList<Point>();
		} else {
			removeAllPoints();
		}
		for (int i = 0; i < points.length; i++) {
			if (this.distance(points[i]) == 0) {
				addPoint(points[i]);
			}
		}
	}
	
	private void computeBoxes(Box[] boxes) {
		int boxPos = -1;
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].contains(this) && !boxes[i].innerBoxesContains(this)) {
				boxPos = i;
				break;
			}
		}
		
		if (boxPos != -1 && !boxes[boxPos].equals(box)) {
			this.setBox(boxes[boxPos]);
		}
		/* Inner Boxes */
		if (this.innerBoxes == null) {
			this.innerBoxes = new ArrayList<Box>();
		} else {
			removeAllInnerBoxes();
		}
		for (int i = 0; i < boxes.length; i++) {
			if (this.contains(boxes[i])) {
				addInnerBox(boxes[i]);
			}
		}
	}
	
	
	private void computeOverlapBoxes(Box[] boxes) {
		if (this.overlapBoxes == null) {
			this.overlapBoxes = new ArrayList<Box>();
		} else {
			removeAllOverlapBoxes();
		}
		for (int i = 0; i < boxes.length; i++) {
			if (boxes[i].intersects(this)) {
				addOverlapBox(boxes[i]);
			}
		}
	}

	@Override
	public void recompute(boolean moving) {
		if (shapeList == null)
			return;
		Shape[] shapes = shapeList.toArray(new Shape[0]);
		computeLabels(labelArray(shapes));
		computePoints(pointArray(shapes));
		computeOverlapBoxes(boxArray(shapes));
		computeBoxes(boxArray(shapes));
		if (!moving) {
			moveLabel = true;
		}
		if (!moving && hasLabel()) {
			label.snapToCircle(this);
		}
	}

	@Override
	public boolean isWithin(Point p) {
		return Math.abs(center.distance(p)-radius) < CIRCLE_MIN_DIST;
	}
	/*
	 * Method: solve t^2(d.d)+2t(d.(s-c)) + (s-c).(s-c) - r^2 = 0
	 *	check t1 or t2 between 0 and 1.
	 */
	@Override
	public boolean intersects(Line line) {
		Point d = line.end.minus(line.start);
		if (d.x == 0 && d.y == 0) {
			return line.start.distance(center) == radius;
		}
		Point sMc = line.start.minus(center);
		double a = d.dot(d);
		double b = 2*d.dot(sMc);
		double c = sMc.dot(sMc) - radius*radius;
		
		double disc = b*b-4*a*c;
		if (disc < 0)	//Imaginary solutions
			return false;
		
		double sqrt_disc = Math.sqrt(disc);
		
		double t1 = (-b + sqrt_disc)/(2*a);
		if (t1 >= 0 && t1 <= 1)
			return true;
		
		double t2 = (-b - sqrt_disc)/(2*a);
		if (t2 >= 0 && t2 <= 1)
			return true;
		
		return false;
	}
	
	public boolean intersects(Circle circle) {
		return (center.distance(circle.center) <= radius + circle.radius);
	}

	@Override
	public void remove() {
		this.setLabel(null);
		if (points != null) {
			for (int i = 0; i < points.size(); i++) {
				points.get(i).recompute(false);
			}
		}
		removeAllPoints();
		removeAllInnerBoxes();
		removeAllOverlapBoxes();
		this.setBox(null);
	}
}
