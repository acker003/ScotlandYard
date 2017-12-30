package persons;

import main.Matchfield;
import main.Node;

public abstract class Person {

	protected Node position;
	protected Matchfield field;
	
	public Person(Node position, Matchfield field) {
		this.position = position;
		this.field = field;
	}
	
	public boolean setPosition(Node newPosition) {
		if (position.getAllNeighbors().contains(newPosition)) {
			for (Detective det : field.getDetectives()) {
				if (det.getPosition().equals(newPosition)) {
					System.out.println("Auf dieser Haltestelle steht bereits jemand!");
					return false;
				}
			}
			position = newPosition;
			return true;
		} else {
			System.out.println("Dieser Zug ist nicht moeglich!");
			return false;
		}
	}
	
	public boolean setPosition(int nodeID) {
		if (nodeID <= field.getNodes().size()) {
			Node newPosition = field.getNodes().get(nodeID - 1);
			return setPosition(newPosition);
		} else {
			System.out.println("Nicht existente Haltestelle gewaehlt!");
			return false;
		}
		
	}
	
	public Node getPosition() {
		return position;
	}
	
	public abstract void ziehe();
	
}
