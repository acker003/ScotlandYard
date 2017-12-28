package firstTest;

import java.util.ArrayList;
import java.util.List;

public class MisterX_KI extends Person {

	private boolean isFree = true;
	private List<Node> tempList;
	private int amountOfBlackTickets = 4;
	
	public MisterX_KI(Node position, Matchfield field) {
		super(position, field);
	}
	
	public boolean isFree() {
		return isFree;
	}

	@Override
	public void ziehe() {
		List<Node> detectivePositions = new ArrayList<Node>();
		for(Detective det : field.getDetectives()) {
			detectivePositions.add(det.getPosition());
		}
		if (detectivePositions.contains(this.position)) {
			isFree = false;
			return;
		}
		double maxChaos = Double.NEGATIVE_INFINITY;
		Node maxNode = null;
		int id = 0;
		for (int i = 0; i < 4; i++) {
			List<Node> possibleNodes = this.position.getNeighbors(Matchfield.idToVehicle(i));
			for (Node node : possibleNodes) {
				// Wenn einer der Knoten von einem Detektiv belegt ist
				if (detectivePositions.contains(node)) continue;
				// Tue als wuerdest du ziehen
				int nodeID = this.position.getID();
				simulateMove(node, Matchfield.idToVehicle(i));
				// Berechne sonst das resultierende Chaos
				double chaos = Evaluater.getChaos();
				if (i == 3) {
					chaos *= 2 - Math.pow(1.1, amountOfBlackTickets/2);
					if (amountOfBlackTickets == 0) chaos = Double.NEGATIVE_INFINITY;
				}
				if (chaos > maxChaos) {
					id = i;
					maxChaos = chaos;
					maxNode = node;
				}
				// Rueckgaengig machen
				moveBack(nodeID);
				System.out.println(node.getID() + " " + chaos);
			}
		}
		if (maxNode == null) {
			this.isFree = false;
			System.out.println("Mister X wurde gefangen!");
		} else {
			this.position = maxNode;
			field.misterXMoves(Matchfield.idToVehicle(id), true);
			if (id == 3) amountOfBlackTickets--;
		}
		
	}
	
	private void simulateMove(Node nextPosition, Vehicle vehicle) {
		this.position = nextPosition;
		tempList = new ArrayList<Node>(field.getPossiblePositionsOfMisterX());
		field.misterXMoves(vehicle, false);
	}
	
	private void moveBack(int nodeID) {
		this.position = field.getNodes().get(nodeID - 1);
		field.setPossiblePositionsOfMisterX(tempList);
	}
}
