package spiderdrawer;

import java.util.ArrayList;

import spiderdrawer.shape.Point;
import spiderdrawer.shape.interfaces.Deletable;
import spiderdrawer.shape.interfaces.Movable;
import spiderdrawer.shape.interfaces.Shape;

public class Action {
	
	private enum ActionType {
        NULL, MOVE, DELETE, CREATE;
    }
	
	private ArrayList<Shape> shapeList;
	private ActionType type;
	private Movable mShape;
	private Point from;
	private Point to;
	private ArrayList<Deletable> deleted;
	private Shape created;
	
	public Action(ArrayList<Shape> shapeList) {
		this.shapeList = shapeList;
		type = ActionType.NULL;
	}
	
	public void setMove(Movable mShape, Point from, Point to) {
		type = ActionType.MOVE;
		this.mShape = mShape;
		this.from = from;
		this.to = to;
	}
	
	public void setDelete() {
		type = ActionType.DELETE;
		this.deleted = new ArrayList<Deletable>();
	}
	
	public void add(Deletable deleted) {
		this.deleted.add(deleted);
	}
	
	public void setCreate(Shape created) {
		type = ActionType.CREATE;
		this.created = created;
	}
	
	public void undo() {
		switch (type) {
			case MOVE: undoMove(); break;
			case DELETE: undoDelete(); break;
			case CREATE: undoCreate(); break;
		}
		type = ActionType.NULL;
	}
	
	private void undoMove() {
		mShape.move(to, from);
		mShape.recompute(false);
	}
	
	private void undoDelete() {
		shapeList.addAll(deleted);
		for (int i = 0; i < deleted.size(); i++)
			if (deleted.get(i) instanceof Movable)
				((Movable) deleted.get(i)).recompute(false);
	}
	
	private void undoCreate() {
		shapeList.remove(created);
		created = null;
	}
	
}
