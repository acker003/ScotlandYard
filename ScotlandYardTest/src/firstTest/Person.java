package firstTest;

public abstract class Person {

	protected Node position;
	protected Matchfield field;
	
	public Person(Node position, Matchfield field) {
		this.position = position;
		this.field = field;
	}
	
	public void setPosition(Node newPosition) {
		position = newPosition;
	}
	
	public void setPosition(int nodeID) {
		this.position = field.getNodes().get(nodeID - 1);
	}
	
	public Node getPosition() {
		return position;
	}
	
	public abstract void ziehe();
	
}
