package spiderdrawer.drawable;

import java.awt.Graphics2D;

public class Line implements Drawable {

	Point start;
	Point end;
	
	public Line (Point start, Point end) {
		this.start = start;
		this.end = end;
	}
	
	public Line (int startX, int startY, int endX, int endY) {
		this(new Point(startX, startY), new Point(endX, endY));
	}
	
	public void draw(Graphics2D g2) {
		g2.drawLine(start.getX(), start.getY(), end.getX(), end.getY());
	}

	
	
}
