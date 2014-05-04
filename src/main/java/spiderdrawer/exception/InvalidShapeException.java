package spiderdrawer.exception;

public class InvalidShapeException extends Exception {

	private String shape;
	
	public InvalidShapeException(String shape) {
		super();
		this.shape = shape;
	}
	
	@Override
	public String getMessage() {
		return "Invalid shape: " + shape + ".";
	}
	
}
