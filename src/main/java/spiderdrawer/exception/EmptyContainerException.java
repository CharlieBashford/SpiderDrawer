package spiderdrawer.exception;

public class EmptyContainerException extends Exception {

	private String parentClass;
	private String containerType;
	
	public EmptyContainerException(String parentClass, String containerType) {
		super();
		this.parentClass = parentClass;
		this.containerType = containerType;
	}
	
	@Override
	public String getMessage() {
		return "Empty Container for parent: " + parentClass + ", looking for type: " + containerType + ".";
	}
}
