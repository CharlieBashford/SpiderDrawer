package spiderdrawer.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;

import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;

public class Freeform implements Drawable, Deletable {

	ArrayList<Point> points;
	
	public Freeform() {
		this.points = new ArrayList<Point>();
	}
	
	public Freeform(ArrayList<Point> points) {
		this.points = points;
	}
	
	public Freeform(Point point) {
		this(new ArrayList<Point>());
		points.add(point);
	}
	
	public Freeform(int pointX, int pointY) {
		this(new Point(pointX, pointY));
	}
	
	public void addPoint(Point point) {
		points.add(point);
	}
	
	public void addPoint(int pointX, int pointY) {
		points.add(new Point(pointX, pointY));
	}
	
	public ArrayList<Point> getPoints() {
		return points;
	}
	
	public String pointsAsString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < points.size(); i++) {
			sb.append(points.get(i).x + "," + points.get(i).y);
			if (i != points.size() - 1) {
				sb.append(',');
			}
		}
		return sb.toString();
	}
	
	/*
	 * Whether the freeform object is within distance of this freeform.
	 * @Returns boolean.
	 */
	public boolean overlaps(Freeform freeform, int distance) {
		ArrayList<Point> comparedPoints = freeform.getPoints();
		for (int i = 0; i < points.size() - 1; i++) {
			Line pLine = new Line(points.get(i), points.get(i+1));
			for (int j = 0; j < comparedPoints.size() - 1; j++) {
				if (pLine.distance(new Line(comparedPoints.get(j), comparedPoints.get(j+1))) <= distance) {
					return true;
				}
			}
		}
		return false;
	}
	
    public ArrayList<Freeform> getOverlappingFreeforms(ArrayList<Freeform> freeforms) {
    	ArrayList<Freeform> overlappingFreeforms = new ArrayList<Freeform>();
    	for (int i = 0; i < freeforms.size(); i++) {
			Freeform currentFF = (Freeform) freeforms.get(i);
			if (!this.equals(currentFF) && this.overlaps(currentFF, 10)) {
				overlappingFreeforms.add(currentFF);
			}
    	}
    	return overlappingFreeforms;
    }
	
	public int minX() {
		int result = Integer.MAX_VALUE;
		for(int i = 0; i < points.size(); i++) {
			int value = points.get(i).getX();
			if (value < result) {
				result = value;
			}
		}
		return result;
	}
	
	public int maxX() {
		int result = Integer.MIN_VALUE;
		for(int i = 0; i < points.size(); i++) {
			int value = points.get(i).getX();
			if (value > result) {
				result = value;
			}
		}
		return result;
	}
	
	public int minY() {
		int result = Integer.MAX_VALUE;
		for(int i = 0; i < points.size(); i++) {
			int value = points.get(i).getY();
			if (value < result) {
				result = value;
			}
		}
		return result;
	}
	
	public int maxY() {
		int result = Integer.MIN_VALUE;
		for(int i = 0; i < points.size(); i++) {
			int value = points.get(i).getY();
			if (value > result) {
				result = value;
			}
		}
		return result;
	}
	
	public void draw(Graphics2D g2) {
		for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
	}

	@Override
	public boolean intersects(Line line) {
		boolean overlaps = false;
		for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);
            Line currentLine = new Line(p1, p2);
            if (currentLine.intersects(line)) {
            	overlaps = true;
            	break;
            }
		}
		return overlaps;
	}

	@Override
	public void remove() {
		return;//Nothing to remove.
	}
}
