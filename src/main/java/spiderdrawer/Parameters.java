package spiderdrawer;

public class Parameters {

	
	public final static double DELTA = 1e-6; //If two doubles are equal.
	
	/* Used in within(Point) method */
	public final static int BOX_MIN_DIST = 10;
	public final static int CIRCLE_MIN_DIST = 10;
	public final static int POINT_MIN_DIST = 5;
	public final static int LABEL_MIN_WIDTH = 14;

	/* Used to check if connected */ 
	public final static int POINT_LINE_DIST = 20;
	public final static int LABEL_CIRCLE_DIST = 40;
	public final static int CONNECTIVE_BOX_DIST = 40;
	
	public final static int MIN_CIRCLE_RADIUS = 30;
	
	public final static int FONT_SIZE = 20;
	public final static int CONNECTIVE_FONT_SIZE = 40;
		
	public final static float DIST_LINE_MOVE_END = 0.15f;
	
	public final static int LETTER_RECOGNITION_WAIT = 1000;
	
	public final static int LABEL_CIRCLE_DISIRED_DIST = 20;
}
