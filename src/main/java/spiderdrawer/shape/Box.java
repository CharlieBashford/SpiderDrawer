package spiderdrawer.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

import spiderdrawer.shape.containers.MultiContainer;
import spiderdrawer.shape.containers.SingleContainer;
import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;
import static spiderdrawer.Parameters.*;

public class Box implements Drawable, Movable, Deletable {

	Point topLeft;
	int width;
	int height;
	private ArrayList<Shape> shapeList;
	MultiContainer<Circle, Box> circles;
	MultiContainer<Circle, Box> overlapCircles;
	MultiContainer<Circle, Box> outerCircles;
	MultiContainer<Line, Box> lines;
	MultiContainer<Line, Box> overlapLines;
	SingleContainer<Connective, Box> connective;
	boolean leftConnective;
	MultiContainer<Connective, Box> innerConnectives;
	MultiContainer<Box, Box> innerBoxes;
	MultiContainer<Box, Box> outerBoxes;
	MultiContainer<Box, Box> overlapBoxes;
	MultiContainer<Label, Box> labels;
	MultiContainer<Point, Box> points;
	ArrayList<Spider> spiders;


	public Box(Point topLeft, int width, int height) {
		this.topLeft = topLeft;
		this.width = width;
		this.height = height;
	}
	
	public Box(int topLeftX, int topLeftY, int width, int height) {
		this(new Point(topLeftX, topLeftY), width, height);
	}
	
	private void createContainers() {
		circles = new MultiContainer<Circle, Box>(this);
		overlapCircles = new MultiContainer<Circle, Box>(this);
		outerCircles = new MultiContainer<Circle, Box>(this);
		lines = new MultiContainer<Line, Box>(this);
		overlapLines = new MultiContainer<Line, Box>(this);
		connective = new SingleContainer<Connective, Box>(this);
		innerConnectives = new MultiContainer<Connective, Box>(this);
		innerBoxes = new MultiContainer<Box, Box>(this);
		outerBoxes = new MultiContainer<Box, Box>(this);
		overlapBoxes = new MultiContainer<Box, Box>(this);
		labels = new MultiContainer<Label, Box>(this);
		points = new MultiContainer<Point, Box>(this);
	}
	
	public static Box create(int topLeftX, int topLeftY, int width, int height, ArrayList<Shape> shapeList) {
		Box box = new Box(topLeftX, topLeftY, width, height);
		box.createContainers();
		box.shapeList = shapeList;
		box.recompute(false);
		return box;
	}
	
	public static Box create(Freeform freeform, ArrayList<Shape> shapeList) {
		int minX = freeform.minX();
		int minY = freeform.minY();
		return create(minX, minY, freeform.maxX() - minX, freeform.maxY() - minY, shapeList);
	}
	
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	protected Point getTopLeft() {
		return topLeft;
	}
	
	protected int getWidth() {
		return width;
	}
	
	protected int getHeight() {
		return height;
	}
	
	protected String asString() {
		return topLeft.x + "," + topLeft.y + "," + width + "," + height;
	}
	
	protected double distance(Point p) {
		if (intersects(p))
			return 0;
		return boundaryDistance(p);
	}
	
	public boolean intersects(Point p) {
		return (topLeft.x <= p.x && p.x <= topLeft.x + width) && (topLeft.y <= p.y && p.y <= topLeft.y + height);
	}
	
	public double boundaryDistance(Point p) {
		Point topRight = topRight();
		Point bottomLeft = bottomLeft();
		Point bottomRight = bottomRight();
		Line top = new Line(topLeft, topRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		Line left = new Line(topLeft, bottomLeft);
		Line right = new Line(topRight, bottomRight);
		return Math.min(Math.min(top.distance(p), bottom.distance(p)), Math.min(left.distance(p), right.distance(p)));
	}
	
	public boolean isOverlapping() {
		return !overlapCircles.isEmpty()  || !overlapLines.isEmpty() || !overlapBoxes.isEmpty(); 
	}
	
	public boolean containsSpider() {
		return !lines.isEmpty() || !circles.isEmpty();
	}
	
	public boolean completeConnectives() {
		if (innerConnectives == null || innerConnectives.size() == 0)
			return innerBoxes.isEmpty();
		int numBoxes = 0;
		for (int i = 0; i < innerConnectives.size(); i++) {
			if (innerConnectives.get(i).isFullyConnected()) {
				if (innerConnectives.get(i).logical == Logical.NEGATION)
					numBoxes += 1;
				else
					numBoxes += 2;
			}
		}
		
		return !innerBoxes.isEmpty() && numBoxes == innerBoxes.size();
	}
	
	public boolean singleInnerConnective() {
		return innerConnectives.size() == 1;
	}
	
	public boolean isValid() {
		return completeConnectives() && !isOverlapping() && !(containsSpider() && !innerBoxes.isEmpty()) && outerCircles.isEmpty() && (singleInnerConnective() || innerConnectives.isEmpty());
	}
	
	@Override
	public void draw(Graphics2D g2) {
		if (!isValid()) {
			g2.setColor(Color.RED);
		} else {
			g2.setColor(Color.BLACK);
		}
		g2.drawRect(topLeft.getX(), topLeft.getY(), width, height);
		g2.setColor(Color.BLACK);
	}
	
	public void move(Point from, Point to, boolean external) {
		if (external) {
			System.out.println("Moving " + this.toString());
			topLeft.move(from, to);
			if ((overlapCircles == null || overlapCircles.size() == 0) && (overlapBoxes == null || overlapBoxes.size() == 0) && (overlapLines == null || overlapLines.size() == 0)) {
				if (innerBoxes == null || innerBoxes.size() == 0) {
					if (circles != null) {
						for (int i = 0; i < circles.size(); i++) {
							circles.get(i).move(from, to, true);
						}
					}
					if (spiders != null) {
						for (int i = 0; i < spiders.size(); i++) {
							spiders.get(i).move(from, to);
						}
					}
				}
				if (circles == null || circles.size() == 0) {
					if (singleInnerConnective() && innerConnectives.get(0).isFullyConnected()) {
						innerConnectives.get(0).move(from, to, true);
					}
				}
			}
			if (connective.get() != null) {
				if ((leftConnective && this.leftDistance(connective.get()) > CONNECTIVE_BOX_DIST) || (!leftConnective && this.rightDistance(connective.get()) > CONNECTIVE_BOX_DIST)) {
					connective.set(null, null);
				}
			}
		} else {
			move(from, to);
		}
	}

	@Override
	public void move(Point from, Point to) {
		Point topRight = topRight();
		Point bottomLeft = bottomLeft();
		Point bottomRight = bottomRight();
		Line right = new Line(topRight, bottomRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		if (right.distanceAlongLine(from) > 1-DIST_LINE_MOVE_END && bottom.distanceAlongLine(from) > 1-DIST_LINE_MOVE_END) {
			width += to.x - from.x;
			height += to.y - from.y;
		} else {
			move(from, to, true);
		}
	}
	
	private void computeBoxes(Box[] boxes) {
		innerBoxes.removeAll();
		outerBoxes.removeAll();
		for (int i = 0; i < boxes.length; i++) {
			if (this.contains(boxes[i])) {
				innerBoxes.add(boxes[i], boxes[i].outerBoxes);
			}
			if (boxes[i].contains(this)) {
				outerBoxes.add(boxes[i], boxes[i].innerBoxes);
			}
		}
	}
	
	private void computeOverlapBoxes(Box[] boxes) {
		overlapBoxes.removeAll();
		for (int i = 0; i < boxes.length; i++) {
			if (this.intersects(boxes[i])) {
				overlapBoxes.add(boxes[i], boxes[i].overlapBoxes);
			}
		}
	}
	
	private void computeCircles(Circle[] circles) {
		outerCircles.removeAll();
		for (int i = 0; i < circles.length; i++) {
			if (circles[i].contains(this)) {
				outerCircles.add(circles[i], circles[i].innerBoxes);
			}
		}
		
		this.circles.removeAll();
		for (int i = 0; i < circles.length; i++) {
			if (this.contains(circles[i]) && !this.innerBoxesContains(circles[i])) {
				this.circles.add(circles[i], circles[i].box);
			}
		}
	}
	
	private void computeOverlapCircles(Circle[] circles) {
		overlapCircles.removeAll();
		for (int i = 0; i < circles.length; i++) {
			if (this.intersects(circles[i])) {
				overlapCircles.add(circles[i], circles[i].overlapBoxes);
			}
		}
	}
	
	private void computeLines(Line[] lines) {
		this.lines.removeAll();
		if (!innerBoxes.isEmpty()) {
			return;
		}
		for (int i = 0; i < lines.length; i++) {
			if (this.contains(lines[i])) {
				this.lines.add(lines[i], lines[i].box);
			}
		}
	}
	
	private void computePoints(Point[] points) {
		this.points.removeAll();
		if (!innerBoxes.isEmpty()) {
			return;
		}
		for (int i = 0; i < points.length; i++) {
			if (this.contains(points[i])) {
				this.points.add(points[i], points[i].box);
			}
		}
	}
	
	private void computeOverlapLines(Line[] lines) {
		overlapLines.removeAll();
		for (int i = 0; i < lines.length; i++) {
			if (this.intersects(lines[i])) {
				overlapLines.add(lines[i], lines[i].overlapBoxes);
			}
		}
	}
	
	private void computeConnectives(Connective[] connectives) {
		int connectivePos = -1;
		double lowestDist = Double.MAX_VALUE;
		boolean left = true;
		for (int i = 0; i < connectives.length; i++) {
			double leftDist = this.leftDistance(connectives[i]);
			double rightDist = this.rightDistance(connectives[i]);
			double minDist = Math.min(leftDist, rightDist);
			if (minDist < lowestDist) {
				if (!connectives[i].isFullyConnected()  && connectives[i].logical != Logical.NEGATION || (minDist != rightDist)) { //Only allow left connective when not a negation.
					lowestDist = minDist;
					connectivePos = i;
					left = (minDist == leftDist);
				}
			}
		}
		if (connective.get() == null && lowestDist <= CONNECTIVE_BOX_DIST && connectivePos != -1) {
			if (left)
				connective.set(connectives[connectivePos], connectives[connectivePos].rightBox);
			else 
				connective.set(connectives[connectivePos], connectives[connectivePos].leftBox);
			leftConnective = left;
		}
	}
	
	protected boolean innerBoxesContains(Connective connective) {
		if (innerBoxes != null) {
			for (int i = 0; i < innerBoxes.size(); i++) {
				if (innerBoxes.get(i).contains(connective)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean innerBoxesContains(Circle circle) {
		if (innerBoxes != null) {
			for (int i = 0; i < innerBoxes.size(); i++) {
				if (innerBoxes.get(i).contains(circle)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected boolean innerBoxesContains(Label label) {
		if (innerBoxes != null) {
			for (int i = 0; i < innerBoxes.size(); i++) {
				if (innerBoxes.get(i).contains(label)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private void computeInnerConnectives(Connective[] connectives) {
		innerConnectives.removeAll();
		for (int i = 0; i < connectives.length; i++) {
			if (this.contains(connectives[i]) && !innerBoxesContains(connectives[i])) {
				innerConnectives.add(connectives[i], connectives[i].outerBox);
			}
		}
	}
	
	private void computeLabels(Label[] labels) {
		this.labels.removeAll();
		for (int i = 0; i < labels.length; i++) {
			if (this.contains(labels[i]) && !innerBoxesContains(labels[i])) {
				this.labels.add(labels[i], labels[i].box);
			}
		}
	}
	
	@Override
	public void recompute(boolean moving) {
		if (shapeList == null)
			return;
		if (!moving) {
			computeBoxes(Arrays.boxArray(shapeList));
			computeCircles(Arrays.circleArray(shapeList));
			for (int i = 0; i < this.circles.size(); i++) {
				this.circles.get(i).computeLabels(Arrays.labelArray(shapeList));
			}
			computeLabels(Arrays.labelArray(shapeList));
			for (int i = 0; i < this.labels.size(); i++) {
				this.labels.get(i).computeLabels(Arrays.labelArray(shapeList));
			}
			computeLines(Arrays.lineArray(shapeList));
			computePoints(Arrays.pointArray(shapeList));
		}
		computeInnerConnectives(Arrays.connectiveArray(shapeList));
		computeConnectives(Arrays.connectiveArray(shapeList));
		computeOverlapCircles(Arrays.circleArray(shapeList));
		computeOverlapLines(Arrays.lineArray(shapeList));
		computeOverlapBoxes(Arrays.boxArray(shapeList));
		computeSpiders();
	}
	
	private void addSpider(Spider spider) {
		if (spiders == null)
			spiders = new ArrayList<Spider>();
		spiders.add(spider);
	}
	
	protected void computeSpiders() {
		if (spiders != null)
			spiders.clear();
		if (lines != null) {
			for (int i = 0; i < lines.size(); i++) {
				boolean found = false;
				if (spiders != null) {
					for (int j = 0; j < spiders.size(); j++) {
						if (spiders.get(j).containsLine(lines.get(i))) {
							found = true;
							break;
						}
					}
					if (found)
						continue;
				}
				Spider spider = Spider.createSpider(lines.get(i));
				if (spider != null)
					addSpider(spider);
			}
		}
		if (points != null) {
			for (int i = 0; i < points.size(); i++) {
				Spider spider = Spider.createSpider(points.get(i));
				if (spider != null)
					addSpider(spider);
			}
		}
	}
	
	protected boolean contains(Box box) {
		if (this.equals(box))
			return false;
		return (this.topLeft.x < box.topLeft.x && this.topLeft.x + this.width > box.topLeft.x + box.width && this.topLeft.y < box.topLeft.y && this.topLeft.y + this.height > box.topLeft.y + box.height);
	}
	
	protected boolean contains(Circle circle) {
		boolean withinXDir = (topLeft.x < circle.center.x - circle.radius) && (circle.center.x + circle.radius < topLeft.x + width);
		boolean withinYDir = (topLeft.y < circle.center.y - circle.radius) && (circle.center.y + circle.radius < topLeft.y + height);
		return withinXDir && withinYDir;
	}
	
	protected boolean contains(Point point) {
		boolean withinXDir = (topLeft.x < point.x) && (point.x < topLeft.x + width);
		boolean withinYDir = (topLeft.y < point.y) && (point.y < topLeft.y + height);
		return withinXDir && withinYDir;
	}
	
	protected boolean contains(Line line) {
		return this.contains(line.start) && this.contains(line.end);
	}
	
	protected boolean contains(Connective connective) {
		Box connectiveBox = new Box(connective.center.x - connective.width/2, connective.center.y - connective.height/2,connective.width, connective.height);
		return this.contains(connectiveBox);
	}
	
	protected boolean contains(Label label) {
		Box box = new Box(label.center.x - label.width/2, label.center.y - label.height/2, label.width, label.height);
		return this.contains(box);
	}
	
	protected Point topRight() {
		return new Point(topLeft.x+width, topLeft.y);
	}
	
	protected Point bottomLeft() {
		return new Point(topLeft.x, topLeft.y+height);
	}
	
	protected Point bottomRight() {
		return new Point(topLeft.x+width, topLeft.y+height);
	}
	
	protected boolean intersects(Box box) {
		if (this.equals(box))
			return false;
		Point topRight = new Point(topLeft.x + width, topLeft.y);
		Point bottomLeft = new Point(topLeft.x, topLeft.y + height);
		Point bottomRight = new Point(topLeft.x + width, topLeft.y + height);
		Line top = new Line(topLeft, topRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		Line left = new Line(topLeft, bottomLeft);
		Line right = new Line(topRight, bottomRight);
		return box.intersects(top) || box.intersects(bottom) || box.intersects(left) || box.intersects(right);
	}
	
	protected boolean intersects(Circle circle) {
		Point topRight = new Point(topLeft.x + width, topLeft.y);
		Point bottomLeft = new Point(topLeft.x, topLeft.y + height);
		Point bottomRight = new Point(topLeft.x + width, topLeft.y + height);
		Line top = new Line(topLeft, topRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		Line left = new Line(topLeft, bottomLeft);
		Line right = new Line(topRight, bottomRight);
		return circle.intersects(top) || circle.intersects(bottom) || circle.intersects(left) || circle.intersects(right);
	}
	
	protected double distance(Connective connective) {
		Point topRight = new Point(topLeft.x + width, topLeft.y);
		Point bottomLeft = new Point(topLeft.x, topLeft.y + height);
		Point bottomRight = new Point(topLeft.x + width, topLeft.y + height);
		Line top = new Line(topLeft, topRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		Line left = new Line(topLeft, bottomLeft);
		Line right = new Line(topRight, bottomRight);
		return Math.min(Math.min(top.distance(connective.center), bottom.distance(connective.center)), Math.min(left.distance(connective.center), right.distance(connective.center)));
	}
	
	protected double leftDistance(Connective connective) {
		Point bottomLeft = new Point(topLeft.x, topLeft.y + height);
		Line left = new Line(topLeft, bottomLeft);
		Point closest = connective.center.plus(new Point(connective.width/2,0));
		if (left.start.x >= closest.x) { //connective is left of line.
			return left.distance(connective.center);
		}
		return Double.MAX_VALUE;
	}

	protected double rightDistance(Connective connective) {
		Point topRight = new Point(topLeft.x + width, topLeft.y);
		Point bottomRight = new Point(topLeft.x + width, topLeft.y + height);
		Line right = new Line(topRight, bottomRight);
		Point closest = connective.center.minus(new Point(connective.width/2,0));
		if (right.start.x <= closest.x) {//connective is right of line. 
			return right.distance(connective.center);
		}
		return Double.MAX_VALUE;
	}
	
	@Override
	public boolean isWithin(Point p) {
		return Math.abs(this.boundaryDistance(p)) < BOX_MIN_DIST;
	}

	@Override
	public boolean intersects(Line line) {
		Point topRight = new Point(topLeft.x + width, topLeft.y);
		Point bottomLeft = new Point(topLeft.x, topLeft.y + height);
		Point bottomRight = new Point(topLeft.x + width, topLeft.y + height);
		Line top = new Line(topLeft, topRight);
		Line bottom = new Line(bottomLeft, bottomRight);
		Line left = new Line(topLeft, bottomLeft);
		Line right = new Line(topRight, bottomRight);
		return (top.intersects(line) || bottom.intersects(line) || left.intersects(line) || right.intersects(line));
	}

	@Override
	public void remove() {
		circles.removeAll();
		innerBoxes.removeAll();
		outerBoxes.removeAll();	
		points.removeAll();
		lines.removeAll();
		overlapLines.removeAll();
		for (int i = 0; i < labels.size(); i++)
			labels.get(i).recompute(false);
		labels.removeAll();
		for (int i = 0; i < innerConnectives.size(); i++)
			innerConnectives.get(i).recompute(false);
		innerConnectives.removeAll();
	}
}
