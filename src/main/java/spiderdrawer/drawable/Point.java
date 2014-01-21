package spiderdrawer.drawable;

import java.awt.Graphics2D;

public class Point implements Drawable {

	int x;
	int y;
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void draw(Graphics2D g2) {
		g2.drawLine(x, y, x, y);
	}

}
