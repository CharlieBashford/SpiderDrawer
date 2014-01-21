package spiderdrawer.drawable;

import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

public class Freeform implements Drawable {

	ArrayList<Point> points;
	
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
	
	public void draw(Graphics2D g2) {
		for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i+1);

            g2.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
	}
}
