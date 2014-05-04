package spiderdrawer.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;

import spiderdrawer.exception.EmptyContainerException;
import spiderdrawer.exception.InvalidShapeException;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;

public class Spider implements Movable, Drawable {
	
	Line[] lines;
	Point point;
	Label label;
	
	public Spider(Line[] lines) {
		this.lines = lines;
	}
	
	public Spider(Point point) {
		this.point = point;
	}
	
	public void setLabel(char letter, int number) {
		if (isSinglePoint()) {
			setLabel(point, letter, number);
		} else {
			if (lines.length > 0) {
				Line line = lines[0];
				Point point;
				if (line.start.isFullyConnected()) {
					point = line.end;
				} else {
					point = line.start;
				}
				setLabel(point, letter, number);
			}
		}
	}
	
	public void removeLabel() {
		if (isSinglePoint()) {
			removeLabel(point);
		} else {
			if (lines.length > 0) {
				Line line = lines[0];
				Point point;
				if (line.end.label != null) {
					point = line.end;
				} else {
					point = line.start;
				}
				removeLabel(point);
			}
		}
	}
	
	public String asString() throws EmptyContainerException, InvalidShapeException {
		if (!isValid())
			throw new InvalidShapeException("Spider");
		String result = "(\"" + label.letter + label.number + "\",[";
		if (isSinglePoint()) {
			result += point.asString();
		} else {
			for (int i = 0; i < lines.length; i++) {
				result += lines[i].asString();
				if (i != lines.length -1)
					result += ",";
			}
		}
		return result + "])";
	}
	
	private void setLabel(Point point, char letter, int number) {
		label = new Label(letter, number, point.x - 15, point.y - 25);
		point.label = label;
	}
	
	private void removeLabel(Point point) {
		point.label = null;
		label = null;
	}
	
	protected boolean isSinglePoint() { //Assume either lines or point set.
		return (point != null);
	}
	
	@Override
	public boolean isValid() {
		if (label == null)
			return false;
		
		if (isSinglePoint()) {
			return point.isValid();
		} else {
			for (int i = 0; i < lines.length; i++) {
				if (!lines[i].isValid())
					return false;
			}		
		}
		return true;
	}

	@Override
	public void draw(Graphics2D g2) {
		if (isSinglePoint()) {
			point.draw(g2);
		} else {
			for (int i = 0; i < lines.length; i++) {
				lines[i].draw(g2);
			}		
		}
		if (label != null) {
			label.draw(g2);
		}
	}
	
	public boolean containsLine(Line line) {
		if (isSinglePoint())
			return false;
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].equals(line)) {
				return true;
			}
		}
		return false;
	}

	public boolean intersects(Line line) {
		if (isSinglePoint()) {
			return point.intersects(line);
		} else {
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].intersects(line)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void move(Point from, Point to) {
		if (isSinglePoint()) {
			point.move(from, to);
		} else {
			Line previous = null;
			for (int i = 0; i < lines.length; i++) {
				if (previous == null) {
					lines[i].move(from, to, true);
					previous = lines[i];
				} else {
					if (lines[i].start.whichLine(previous) == 0) {
						lines[i].start.move(from, to);
					} else {
						lines[i].end.move(from, to);
					}
					previous = lines[i];
				}
			}
		}
	}

	@Override
	public void recompute(boolean moving) {
		if (isSinglePoint()) {
			point.recompute(moving);
		} else {
			for (int i = 0; i < lines.length; i++) {
				lines[i].recompute(moving);
			}
		}
	}

	@Override
	public boolean isWithin(Point p) {
		if (isSinglePoint()) {
			return point.isWithin(p);
		} else {
			for (int i = 0; i < lines.length; i++) {
				if (lines[i].isWithin(p)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public double boundaryDistance(Point p) {
		if (isSinglePoint()) {
			return point.boundaryDistance(p);
		} else {
			double minDist = Double.MAX_VALUE;
			for (int i = 0; i < lines.length; i++) {
				double dist = lines[i].boundaryDistance(p);
				if (dist < minDist)
					minDist = dist;
			}
			return minDist;
		}
	}
	
	protected static Spider createSpider(Point point) {
		if (point.line1 != null || point.line2 != null)
			return null;
		return new Spider(point);
	}
	
	protected static Spider createSpider(Line line) {
		Line currentLine = line;
		if (!currentLine.hasBothEnds())
			return null;
		
		ArrayList<Line> lineList = new ArrayList<Line>();
		lineList.add(currentLine);
		Line iterLine = currentLine;
		Point iterPoint = currentLine.start;
		while (iterPoint.otherLine(iterLine) != null) {
			iterLine = iterPoint.otherLine(iterLine);
			if (!iterLine.hasBothEnds())
				break;
			lineList.add(0, iterLine);
			iterPoint = iterLine.otherEnd(iterPoint);
			
		}
		if (!iterLine.hasBothEnds())
			return null;
		iterLine = currentLine;
		iterPoint = currentLine.end;
		while (iterPoint.otherLine(iterLine) != null) {
			iterLine = iterPoint.otherLine(iterLine);
			if (!iterLine.hasBothEnds())
				break;
			lineList.add(iterLine);
			iterPoint = iterLine.otherEnd(iterPoint);
			
		}
		if (!iterLine.hasBothEnds())
			return null;
		return new Spider(lineList.toArray(new Line[0]));
	}
	
	protected void remove() {
		removeLabel();
	}
}
