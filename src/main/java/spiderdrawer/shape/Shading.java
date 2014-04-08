package spiderdrawer.shape;

import java.awt.Graphics2D;
import java.util.ArrayList;

import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Drawable;
import spiderdrawer.shape.interfaces.Shape;


public class Shading implements Deletable {
	
	/*
	 *  included\excluded
	 */
	ArrayList<Circle> included;
	ArrayList<Circle> excluded;
	
	public Shading() {
	}
	
	public static Shading create(Freeform freeform, Shape[] shapes) {
		Shading shading = new Shading();
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
	
	protected void addIncluded(Circle circle) {
		if (this.included == null)
			this.included = new ArrayList<Circle>();
		included.add(circle);
		if (circle != null && !circle.containsShading(this)) {
			circle.addShading(this);
		}
	}
	
	protected boolean containsIncluded(Circle circle) {
		if (circle != null)
			return included.contains(circle);
		return false;
	}
	
	protected void removeIncluded(Circle circle) {
		included.remove(circle);
		if (circle != null && circle.containsShading(this)) {
			circle.removeShading(this);
		}
	}
	
	private void computeIncluded(Freeform freeform, Circle[] circles) {
		for (int i = 0; i < circles.length; i++) {
			if (circles[i].contains(freeform))
				addIncluded(circles[i]);
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
		for (int i = 0; i < included.size(); i++)
			removeIncluded(included.get(i));
	}
}
