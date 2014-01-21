package spiderdrawer.drawable;

import java.awt.Graphics2D;

public class Label implements Drawable {

	Point position;
	char letter;
	
	public Label(char letter, Point position) {
		this.letter = letter;
		this.position = position;
	}
	
	public Label(char letter, int positionX, int positionY) {
		this(letter, new Point(positionX, positionY));
	}
	
	public void draw(Graphics2D g2) {
		char[] array = {letter};
		g2.drawChars(array, 0, 1, position.getX(), position.getY());
	}
}
