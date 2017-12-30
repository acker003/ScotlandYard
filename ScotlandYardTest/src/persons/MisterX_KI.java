package persons;

import java.util.ArrayList;
import java.util.List;

import helper.Evaluater;
import main.Matchfield;
import main.Node;
import main.Vehicle;

public class MisterX_KI extends Person {

	private boolean isFree = true;
	private List<Node> tempList;
	private int amountOfBlackTickets = 4;
	private String moves;
	private List<Double> lastBestRatings = new ArrayList<Double>();
	
	public MisterX_KI(Node position, Matchfield field) {
		super(position, field);
		moves = "Von " + position.getID() + "\n";
	}
	
	public boolean isFree() {
		return isFree;
	}

	@Override
	public void ziehe() {
		Evaluater.calculateRiskOfBeingCircled(position, lastBestRatings, true);
		List<Node> detectivePositions = new ArrayList<Node>();
		for(Detective det : field.getDetectives()) {
			detectivePositions.add(det.getPosition());
		}
		if (detectivePositions.contains(this.position)) {
			for (Detective det : field.getDetectives()) {
				if (det.getPosition().equals(position)) {
					System.out.println("Detektiv " + det.getID() 
						+ " hat Mister X an der Haltestelle " 
						+ position.getID() 
						+ " gefangen! Herlichen Glückwunsch!");
				}
			}
			isFree = false;
			return;
		}
		double maxRating = Double.NEGATIVE_INFINITY;
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
				// Berechne die Situation
				double rating = Evaluater.evaluateMoveForMisterX(node, i, amountOfBlackTickets);
				if (rating > maxRating) {
					id = i;
					maxRating = rating;
					maxNode = node;
					//System.out.print(" MAX");
				}
				// Rueckgaengig machen
				//System.out.println(node.getID() + " FR:" + rating + " " + field.getPossiblePositionsOfMisterX().size());
				moveBack(nodeID);
			}
		}
		if (maxNode == null) {
			this.isFree = false;
			System.out.println("Mister X wurde gefangen!");
		} else {
			this.position = maxNode;
			double risk = Evaluater.calculateRiskOfBeingCircled(position, lastBestRatings, true);
			if (Evaluater.actualConfig.equals(Evaluater.BRUTAL_ESCAPE)) {
				//System.out.println("Im Risikomodus (" + risk + ")!");
			} else {
				//System.out.println("Im Normalmodus ( " + risk + ")!");
			}
			lastBestRatings.add(maxRating);
			moves += field.getNameToVehicle(Matchfield.idToVehicle(id)) + " nach " + maxNode.getID() + "\n";
			field.misterXMoves(Matchfield.idToVehicle(id), true);
			if (id == 3) amountOfBlackTickets--;
		}
		
	}
	
	public int getAmountOfBlackTickets() {
		return amountOfBlackTickets;
	}
	
	public List<Double> getLastBestRatings() {
		return lastBestRatings;
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
	
	public void printMoves() {
		System.out.println(moves);
		moves = "Von " + position.getID() + "\n";
	}
}
