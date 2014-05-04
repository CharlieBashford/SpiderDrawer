package spiderdrawer.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import spiderdrawer.exception.EmptyContainerException;
import spiderdrawer.shape.containers.MultiContainer;
import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Shape;


public class Shading implements Deletable {
	
	/*
	 *  included\excluded
	 */
	MultiContainer<Circle, Shading> included;
	ArrayList<Circle> excluded;
	
	public Shading() {
	}
	
	private void createContainers() {
		included = new MultiContainer<Circle, Shading>(this);
	}
	
	public static Shading create(Freeform freeform, Shape[] shapes) {
		Shading shading = new Shading();
		shading.createContainers();
		shading.compute(freeform, shapes);
		return shading;
	}
	
	
	private void compute(Freeform freeform, Shape[] shapes) {
		Circle[] circles = circleArray(shapes);
		computeIncluded(freeform, circles);
		excluded = computeExcluded(freeform, circles);
	}
	
	private static Circle[] circleArray(Shape[] shapes) {
		ArrayList<Circle> circleList = new ArrayList<Circle>();
		for (int i = 0; i < shapes.length; i++) {
			if (shapes[i] instanceof Circle) {
				circleList.add((Circle) shapes[i]);
			}
		}
		return circleList.toArray(new Circle[0]);
 	}
	
	public String asString() throws EmptyContainerException {
		String result = "([";
		for (int i = 0; i < included.size(); i++) {
			result += "\"" + included.get(i).label.getWExc("Label").getChar() + "\"";
			if (i != included.size()-1)
				result += ", ";
		}
		result += "],[";
		if (excluded != null) {
			for (int i = 0; i < excluded.size(); i++) {
				result += "\"" + excluded.get(i).label.getWExc("Label").getChar() + "\"";
				if (i != excluded.size()-1)
					result += ", ";
			}
		}
		return result + "])";
	}
	
	private void computeIncluded(Freeform freeform, Circle[] circles) {
		for (int i = 0; i < circles.length; i++) {
			if (circles[i].contains(freeform))
				included.add(circles[i], circles[i].shadings);
		}
 	}
	
	private ArrayList<Circle> computeExcluded(Freeform freeform, Circle[] circles) {
		ArrayList<Circle> excludedList = new ArrayList<Circle>();
		if (included != null && included.size() > 0) {
			for (int i = 0; i < circles.length; i++) {
				if (!included.contains(circles[i])) {
					if (circles[i].distance(freeform) > 0) {
						for (int j = 0; j < included.size(); j++) {
							if (!circles[i].intersects(included.get(j))) {
								break;
							}
							if (j == included.size() - 1)
								excludedList.add(circles[i]);
						}
					}
				}
			}
		}
		return excludedList;
 	}
	
	@Override
	public boolean intersects(Line line) {
		if ((included == null || included.size() == 0) && (excluded == null || excluded.size() == 0)) //Shouldn't exist.
			return false;
		
		if (included != null) {
			for (int i = 0; i < included.size(); i++) {
				if (!included.get(i).intersects(line))
					return false;
			}
		}
		if (excluded != null) {
			for (int i = 0; i < excluded.size(); i++) {
				if (excluded.get(i).intersects(line))
					return false;
			}
		}
		return true;
	}
	
	@Override
	public void remove() {
		included.removeAll();
	}
}
