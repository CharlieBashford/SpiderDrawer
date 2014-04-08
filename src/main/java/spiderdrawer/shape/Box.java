package spiderdrawer.shape;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;

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
	ArrayList<Circle> circles;
	ArrayList<Circle> overlapCircles;
	ArrayList<Circle> outerCircles;
	ArrayList<Line> lines;
	ArrayList<Line> overlapLines;
	Connective connective;
	boolean leftConnective;
	ArrayList<Connective> innerConnectives;
	ArrayList<Box> innerBoxes;
	ArrayList<Box> overlapBoxes;
	ArrayList<Box> outerBoxes;
	ArrayList<Spider> spiders;
	ArrayList<Label> labels;
	ArrayList<Point> points;


	public Box(Point topLeft, int width, int height) {
		this.topLeft = topLeft;
		this.width = width;
		this.height = height;
	}
	
	public Box(int topLeftX, int topLeftY, int width, int height) {
		this(new Point(topLeftX, topLeftY), width, height);
	}
	
	public static Box create(int topLeftX, int topLeftY, int width, int height, ArrayList<Shape> shapeList) {
		Box box = new Box(topLeftX, topLeftY, width, height);
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
	
	protected void addCircle(Circle circle) {
		if (this.circles == null)
			this.circles = new ArrayList<Circle>();
		circles.add(circle);
		if (circle != null && !this.equals(circle.getBox())) {
			circle.setBox(this);
		}
	}
	
	protected void removeCircle(Circle circle) {
		circles.remove(circle);
		if (circle != null && this.equals(circle.getBox())) {
			circle.setBox(null);
		}
	}
	
	protected boolean containsCircle(Circle circle) {
		if (circles != null)
			return circles.contains(circle);
		return false;
	}
	
	protected void removeAllCircles() {
		while(circles.size() > 0) {
			removeCircle(circles.get(0));
		}
	}
	
	protected boolean containsCircle() {
		return (circles != null) && (circles.size() > 0);
	}
	
	protected void addLabel(Label label) {
		if (this.labels == null)
			this.labels = new ArrayList<Label>();
		labels.add(label);
		if (label != null && !this.equals(label.getBox())) {
			label.setBox(this);
		}
	}
	
	protected void removeLabel(Label label) {
		labels.remove(label);
		if (label != null && this.equals(label.getBox())) {
			label.setBox(null);
		}
	}
	
	protected boolean containsLabel(Label label) {
		if (labels != null)
			return labels.contains(label);
		return false;
	}
	
	protected void removeAllLabels() {
		while(labels.size() > 0) {
			removeLabel(labels.get(0));
		}
	}
	
	protected void addLine(Line line) {
		if (this.lines == null)
			this.lines = new ArrayList<Line>();
		lines.add(line);
		if (line != null && !this.equals(line.getBox())) {
			line.setBox(this);
		}
	}
	
	protected void removeLine(Line line) {
		lines.remove(line);
		if (line != null && this.equals(line.getBox())) {
			line.setBox(null);
		}
	}
	
	protected boolean containsLine(Line line) {
		if (lines != null)
			return lines.contains(line);
		return false;
	}
	
	protected void removeAllLines() {
		while(lines.size() > 0) {
			removeLine(lines.get(0));
		}
	}
	
	protected boolean containsLine() {
		return (lines != null) && (lines.size() > 0);
	}
	
	protected void addPoint(Point point) {
		if (this.points == null)
			this.points = new ArrayList<Point>();
		points.add(point);
		if (point != null && !this.equals(point.getBox())) {
			point.setBox(this);
		}
	}
	
	protected void removePoint(Point point) {
		points.remove(point);
		if (point != null && this.equals(point.getBox())) {
			point.setBox(null);
		}
	}
	
	protected boolean containsPoint(Point point) {
		if (points != null)
			return points.contains(point);
		return false;
	}
	
	protected void removeAllPoints() {
		while(points.size() > 0) {
			removePoint(points.get(0));
		}
	}
	
	protected void addOverlapCircle(Circle circle) {
		if (this.overlapCircles == null)
			this.overlapCircles = new ArrayList<Circle>();
		overlapCircles.add(circle);
		if (circle != null && !circle.containsOverlapBox(this)) {
			circle.addOverlapBox(this);
		}
	}
	
	protected void removeOverlapCircle(Circle circle) {
		overlapCircles.remove(circle);
		if (circle != null && circle.containsOverlapBox(this)) {
			circle.removeOverlapBox(this);
		}
	}
	
	protected boolean containsOverlapCircle(Circle circle) {
		if (overlapCircles != null)
			return overlapCircles.contains(circle);
		return false;
	}
	
	protected void removeAllOverlapCircles() {
		while(overlapCircles.size() > 0) {
			removeOverlapCircle(overlapCircles.get(0));
		}
	}
	
	protected boolean isOverlapCircles() {
		return (overlapCircles != null) && (overlapCircles.size() > 0);
	}
	
	protected void addOuterCircle(Circle circle) {
		if (this.outerCircles == null)
			this.outerCircles = new ArrayList<Circle>();
		outerCircles.add(circle);
		if (circle != null && !circle.containsInnerBox(this)) {
			circle.addInnerBox(this);
		}
	}
	
	protected void removeOuterCircle(Circle circle) {
		outerCircles.remove(circle);
		if (circle != null && circle.containsInnerBox(this)) {
			circle.removeInnerBox(this);
		}
	}
	
	protected boolean containsOuterCircle(Circle circle) {
		if (outerCircles != null)
			return outerCircles.contains(circle);
		return false;
	}
	
	protected boolean containsOuterCircle() {
		return (outerCircles != null) && (outerCircles.size() > 0);
	}
	
	protected void removeAllOuterCircles() {
		while(outerCircles.size() > 0) {
			removeOuterCircle(outerCircles.get(0));
		}
	}
	
	protected void addOverlapLine(Line line) {
		if (this.overlapLines == null)
			this.overlapLines = new ArrayList<Line>();
		overlapLines.add(line);
		if (line != null && !line.containsOverlapBox(this)) {
			line.addOverlapBox(this);
		}
	}
	
	protected void removeOverlapLine(Line line) {
		overlapLines.remove(line);
		if (line != null && line.containsOverlapBox(this)) {
			line.removeOverlapBox(this);
		}
	}
	
	protected boolean containsOverlapLine(Line line) {
		if (overlapLines != null)
			return overlapLines.contains(line);
		return false;
	}
	
	protected void removeAllOverlapLines() {
		while (overlapLines.size() > 0) {
			removeOverlapLine(overlapLines.get(0));
		}
	}
	
	protected boolean isOverlapLines() {
		return (overlapLines != null) && (overlapLines.size() > 0);
	}
	
	protected void addInnerBox(Box box) {
		if (this.innerBoxes == null)
			this.innerBoxes = new ArrayList<Box>();
		innerBoxes.add(box);
		if (box != null && !box.containsOuterBox(this)) {
			box.addOuterBox(this);
		}
	}
	
	protected void removeInnerBox(Box box) {
		innerBoxes.remove(box);
		if (box != null && box.containsOuterBox(this)) {
			box.removeOuterBox(this);
		}
	}
	
	protected boolean containsInnerBox(Box box) {
		if (innerBoxes != null)
			return innerBoxes.contains(box);
		return false;
	}
	
	protected void removeAllInnerBoxes() {
		while(innerBoxes.size() > 0) {
			removeInnerBox(innerBoxes.get(0));
		}
	}
	
	protected boolean containsInnerBoxes() {
		return (innerBoxes != null) && (innerBoxes.size() > 0);
	}
	
	protected void addOuterBox(Box box) {
		if (this.outerBoxes == null)
			this.outerBoxes = new ArrayList<Box>();
		outerBoxes.add(box);
		if (box != null && !box.containsInnerBox(this)) {
			box.addInnerBox(this);
		}
	}
	
	protected void removeOuterBox(Box box) {
		outerBoxes.remove(box);
		if (box != null && box.containsInnerBox(this)) {
			box.removeInnerBox(this);
		}
	}
	
	protected boolean containsOuterBox(Box box) {
		if (outerBoxes != null)
			return outerBoxes.contains(box);
		return false;
	}
	
	protected void removeAllOuterBoxes() {
		while(outerBoxes.size() > 0) {
			removeOuterBox(outerBoxes.get(0));
		}
	}
	
	protected boolean containsOuterBoxes() {
		return (outerBoxes != null) && (outerBoxes.size() > 0);
	}
	
	protected void addOverlapBox(Box box) {
		if (this.overlapBoxes == null)
			this.overlapBoxes = new ArrayList<Box>();
		overlapBoxes.add(box);
		if (box != null && !box.containsOverlapBox(this)) {
			box.addOverlapBox(this);
		}
	}
	
	protected void removeOverlapBox(Box box) {
		overlapBoxes.remove(box);
		if (box != null && box.containsOverlapBox(this)) {
			box.removeOverlapBox(this);
		}
	}
	
	protected boolean containsOverlapBox(Box box) {
		if (overlapBoxes != null)
			return overlapBoxes.contains(box);
		return false;
	}
	
	protected void removeAllOverlapBoxes() {
		while(overlapBoxes.size() > 0) {
			removeOverlapBox(overlapBoxes.get(0));
		}
	}
	
	protected boolean isOverlapBoxes() {
		return (overlapBoxes != null) && (overlapBoxes.size() > 0);
	}

	protected void setConnective(Connective connective, boolean left) {
		Connective oldConnective = this.connective;
		boolean oldLeftConnective = this.leftConnective;
		this.connective = connective;
		this.leftConnective = left;
		if (oldConnective != null) {
			if (oldLeftConnective) { //Left and right are swapped due to perspective.
				oldConnective.setRightBox(null);
			} else {
				oldConnective.setLeftBox(null);
			}
		}	
		if (connective != null) {
			if (left) { //Left and right are swapped due to perspective.
				Box box = connective.getRightBox();
				if (!this.equals(box)) {
					connective.setRightBox(this);
				}
			} else {
				Box box = connective.getLeftBox();
				if (!this.equals(box)) {
					connective.setLeftBox(this);
				}
			}
		}
	}
	
	public Connective getConnective() {
		return connective;
	}
	
	protected void addInnerConnective(Connective connective) {
		if (this.innerConnectives == null)
			this.innerConnectives = new ArrayList<Connective>();
		innerConnectives.add(connective);
		if (connective != null && !this.equals(connective.getOuterBox())) {
			connective.setOuterBox(this);
		}
	}
	
	protected void removeInnerConnective(Connective connective) {
		innerConnectives.remove(connective);
		if (connective != null && this.equals(connective.getOuterBox())) {
			connective.setOuterBox(null);
		}
	}
	
	protected boolean containsInnerConnective(Connective connective) {
		if (innerConnectives != null)
			return innerConnectives.contains(connective);
		return false;
	}
	
	protected void removeAllInnerConnectives() {
		while(innerConnectives.size() > 0) {
			removeInnerConnective(innerConnectives.get(0));
		}
	}
	
	protected boolean containsInnerConnnective() {
		return (innerConnectives != null) && (innerConnectives.size() > 0);
	}
	
	protected boolean singleInnerConnective() {
		return innerConnectives != null && innerConnectives.size() == 1;
	}
	
	public boolean isOverlapping() {
		return isOverlapCircles()  || isOverlapLines() || isOverlapBoxes(); 
	}
	
	public boolean containsSpider() {
		return containsLine() || containsCircle();
	}
	
	public boolean isValid() {
		return !isOverlapping() && !(containsSpider() && containsInnerBoxes()) && !containsOuterCircle() && (singleInnerConnective() || !containsInnerConnnective());
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
							circles.get(i).move(from, to);
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
			if (connective != null) {
				if ((leftConnective && this.leftDistance(connective) > CONNECTIVE_BOX_DIST) || (!leftConnective && this.rightDistance(connective) > CONNECTIVE_BOX_DIST)) {
					setConnective(null, leftConnective);
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
	
	private Circle[] circleArray(Shape[] shapes) {
		ArrayList<Circle> circleList = new ArrayList<Circle>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Circle) {
				circleList.add((Circle) shapes[i]);
			}
		}
		return circleList.toArray(new Circle[0]);
 	}
	
	private Line[] lineArray(Shape[] shapes) {
		ArrayList<Line> lineList = new ArrayList<Line>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Line) {
				lineList.add((Line) shapes[i]);
			}
		}
		return lineList.toArray(new Line[0]);
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
	
	private void computeBoxes(Box[] boxes) {
		if (this.innerBoxes == null) {
			this.innerBoxes = new ArrayList<Box>();
		} else {
			removeAllInnerBoxes();
		}
		if (this.outerBoxes == null) {
			this.outerBoxes = new ArrayList<Box>();
		} else {
			removeAllOuterBoxes();
		}
		for (int i = 0; i < boxes.length; i++) {
			if (this.contains(boxes[i])) {
				addInnerBox(boxes[i]);
			}
			if (boxes[i].contains(this)) {
				addOuterBox(boxes[i]);
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
			if (this.intersects(boxes[i])) {
				addOverlapBox(boxes[i]);
			}
		}
	}
	
	private void computeCircles(Circle[] circles) {
		if (this.outerCircles == null) {
			this.outerCircles = new ArrayList<Circle>();
		} else {
			removeAllOuterCircles();
		}
		for (int i = 0; i < circles.length; i++) {
			if (circles[i].contains(this)) {
				addOuterCircle(circles[i]);
			}
		}
		
		if (this.circles == null) {
			this.circles = new ArrayList<Circle>();
		} else {
			removeAllCircles();
		}
		for (int i = 0; i < circles.length; i++) {
			if (this.contains(circles[i]) && !this.innerBoxesContains(circles[i])) {
				addCircle(circles[i]);
			}
		}
	}
	
	private void computeOverlapCircles(Circle[] circles) {
		if (this.overlapCircles == null) {
			this.overlapCircles = new ArrayList<Circle>();
		} else {
			removeAllOverlapCircles();
		}
		for (int i = 0; i < circles.length; i++) {
			if (this.intersects(circles[i])) {
				addOverlapCircle(circles[i]);
			}
		}
	}
	
	private void computeLines(Line[] lines) {
		if (this.lines == null) {
			this.lines = new ArrayList<Line>();
		} else {
			removeAllLines();
		}
		if (this.containsInnerBoxes()) {
			return;
		}
		for (int i = 0; i < lines.length; i++) {
			if (this.contains(lines[i])) {
				addLine(lines[i]);
			}
		}
	}
	
	private void computePoints(Point[] points) {
		if (this.points == null) {
			this.points = new ArrayList<Point>();
		} else {
			removeAllPoints();
		}
		if (this.containsInnerBoxes()) {
			return;
		}
		for (int i = 0; i < points.length; i++) {
			if (this.contains(points[i])) {
				addPoint(points[i]);
			}
		}
	}
	
	private void computeOverlapLines(Line[] lines) {
		if (this.overlapLines == null) {
			this.overlapLines = new ArrayList<Line>();
		} else {
			removeAllOverlapLines();
		}
		for (int i = 0; i < lines.length; i++) {
			if (this.intersects(lines[i])) {
				addOverlapLine(lines[i]);
			}
		}
	}
	
	private Label[] labelArray(Shape[] shapes) {
		ArrayList<Label> labelList = new ArrayList<Label>();
		Label label;
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Label) {
				label = (Label) shapes[i];
				//if (!label.hasCircle()) {
					labelList.add(label);
				//}
			}
		}
		return labelList.toArray(new Label[0]);
 	}
	
	private Connective[] connectiveArray(Shape[] shapes) {
		ArrayList<Connective> connectiveList = new ArrayList<Connective>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Connective) {
				connectiveList.add((Connective) shapes[i]);
			}
		}
		return connectiveList.toArray(new Connective[0]);
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
		if (connective == null && lowestDist <= CONNECTIVE_BOX_DIST && connectivePos != -1) {
			this.setConnective(connectives[connectivePos], left);
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
		if (this.innerConnectives == null) {
			this.innerConnectives = new ArrayList<Connective>();
		} else {
			removeAllInnerConnectives();
		}
		for (int i = 0; i < connectives.length; i++) {
			if (this.contains(connectives[i]) && !innerBoxesContains(connectives[i])) {
				addInnerConnective(connectives[i]);
			}
		}
	}
	
	private void computeLabels(Label[] labels) {
		if (this.labels == null) {
			this.labels = new ArrayList<Label>();
		} else {
			removeAllLabels();
		}
		for (int i = 0; i < labels.length; i++) {
			if (this.contains(labels[i]) && !innerBoxesContains(labels[i])) {
				addLabel(labels[i]);
			}
		}
	}
	
	@Override
	public void recompute(boolean moving) {
		if (shapeList == null)
			return;
		Shape[] shapes = shapeList.toArray(new Shape[0]);
		if (!moving) {
			computeBoxes(boxArray(shapes));
			computeCircles(circleArray(shapes));
			for (int i = 0; i < this.circles.size(); i++) {
				this.circles.get(i).computeLabels(labelArray(shapes));
			}
			computeLabels(labelArray(shapes));
			for (int i = 0; i < this.labels.size(); i++) {
				this.labels.get(i).computeLabels(labelArray(shapes));
			}
			computeLines(lineArray(shapes));
		}
		computeInnerConnectives(connectiveArray(shapes));
		computeConnectives(connectiveArray(shapes));
		computeOverlapCircles(circleArray(shapes));
		computeOverlapLines(lineArray(shapes));
		computeOverlapBoxes(boxArray(shapes));
		computeSpiders();
	}
	
	private void addSpider(Spider spider) {
		if (spiders == null)
			spiders = new ArrayList<Spider>();
		spiders.add(spider);
	}
	
	private void computeSpiders() {
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
		removeAllInnerBoxes();
		removeAllOuterBoxes();	
		removeAllCircles();
		removeAllLines();
		if (labels != null) {
			for (int i = 0; i < labels.size(); i++)
				labels.get(i).recompute(false);
		}
		removeAllLabels();
	}
}
