package spiderdrawer.drawable;

import java.awt.Graphics2D;

public class Circle implements Drawable {

	Point center;
	int radius;
	
	public Circle(Point center, int radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public Circle(int centerX, int centerY, int radius) {
		this(new Point(centerX, centerY), radius);
	}

	public void draw(Graphics2D g2) {
		g2.drawOval((int)(center.getX()-radius*(1/Math.sqrt(2))), (int)(center.getY()-radius*(1/Math.sqrt(2))), radius*2, radius*2);
	}
}
