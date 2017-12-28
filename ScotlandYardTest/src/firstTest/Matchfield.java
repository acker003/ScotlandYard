package firstTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Matchfield {

	public static final int AMOUNT_OF_NODES = 26;
	
	private List<Node> nodes = new ArrayList<Node>();
	private List<Detective> detectives = new ArrayList<Detective>();
	private MisterX_KI misterX;
	private double[][] distanceTable;
	private int round = 0;
	
	private Node lastPositionOfMisterX;
	private List<Node> possiblePositionsOfMisterX = new ArrayList<Node>();
	
	public void play() {
		System.out.println(nodes.size());
		//possiblePositionsOfMisterX = new ArrayList<Node>(nodes);
		possiblePositionsOfMisterX.add(misterX.position);
		Evaluater.setMatchfield(this);
		// Start
		while(misterX.isFree()) {
			round++;
			misterX.ziehe();
			if (misterX.isFree()) {
				//if (runde % 2 == 0) 
					System.out.println("MisterX nun auf: " + misterX.getPosition().getID());
				for (Detective det : detectives) {
					det.ziehe();
				}
			}
		}
	}
	
	public void addNode(Node node) {
		nodes.add(node);
	}
	
	public void addNodes(List<Node> nodes) {
		this.nodes.addAll(nodes);
	}
	
	public List<Detective> getDetectives() {
		return detectives;
	}

	public void setDetectives(List<Detective> detectives) {
		this.detectives = detectives;
	}

	public MisterX_KI getMisterX() {
		return misterX;
	}

	public void setMisterX(MisterX_KI misterX) {
		this.misterX = misterX;
	}
	
	public List<Node> getNodes() {
		return nodes;
	}
	
	public Node getLastPositionOfMisterX() {
		return lastPositionOfMisterX;
	}

	public void setLastPositionOfMisterX(Node lastPositionOfMisterX) {
		this.lastPositionOfMisterX = lastPositionOfMisterX;
	}

	public double[][] getDistanceTable() {
		return distanceTable;
	}
	
	public void setDistanceTable(double[][] distanceTable) {
		this.distanceTable = distanceTable;
	}
	
	public double getDistance(Detective det, Node node) {
		double distance = -1;
		Node detNode = det.getPosition();
		int index1 = nodes.indexOf(detNode);
		int index2 = nodes.indexOf(node);
		distance = distanceTable[index1][index2];
		if (distance == -1) System.err.println("FEHLER IN DISTANCE!");
		return distance;
	}
	
	public List<Node> getPossiblePositionsOfMisterX() {
		return possiblePositionsOfMisterX;
	}

	public void setPossiblePositionsOfMisterX(List<Node> possiblePositionsOfMisterX) {
		this.possiblePositionsOfMisterX = possiblePositionsOfMisterX;
	}
	
	public int getRound() {
		return round;
	}
	
	public void misterXMoves(Vehicle vehicle, boolean print) {
		// Berechnet was passiert, wenn Mister X sich mit einem bestimmten
		// Verkehrsmittel bewegt
		List<Node> newPositions = new ArrayList<Node>();
		for (Node node : possiblePositionsOfMisterX) {
			for (Node n : node.getNeighbors(vehicle)) {
				if (!newPositions.contains(n)) newPositions.add(n);
			}
		}
		possiblePositionsOfMisterX = new ArrayList<Node>(newPositions);
		if (print) System.out.println("Mister X ist " + getNameToVehicle(vehicle) + " gefahren.");
	}
	
	private String getNameToVehicle(Vehicle vehicle) {
		switch (vehicle) {
		case TAXI:
			return "Taxi";
		case BUS:
			return "Bus";
		case SUBWAY:
			return "U-Bahn";
		case BLACK:
			return "Black";
		default:
			System.err.println("FEHLER BEI ID!");
			return "was Dummes";
		}
	}
	
	public static Vehicle idToVehicle(int id) {
		switch (id) {
		case 0:
			return Vehicle.TAXI;
		case 1:
			return Vehicle.BUS;
		case 2:
			return Vehicle.SUBWAY;
		case 3:
			return Vehicle.BLACK;
		default:
			System.err.println("FEHLER BEI ID!");
			return null;
		}
	}

	public static void main(String[] args) {
		// Create Nodes
		List<Node> nodes = new ArrayList<Node>();
		for (int i = 0; i < AMOUNT_OF_NODES; i++) {
			nodes.add(new Node());
		}
		
		// Create Matchfield
		Matchfield field = new Matchfield();
		
		// Create and Set DistanceTable
		field.setDistanceTable(CSVHelper.getDistanceTable(nodes));
		
		// Set NeighborNodes
		for (int i = 0; i < 3; i++) {
			Map<Node, List<Node>> neighborNodes = CSVHelper.getNeighborNodes(i, nodes);
			for (Node node : neighborNodes.keySet()) {
				node.setNeighbors(i, neighborNodes.get(node));
			}
		}
		
		// Add Nodes to Field
		field.addNodes(nodes);
		
		Node node = nodes.get(5);
		System.out.println(node);
		
		// Create Detectives
		List<Detective> detectives = new ArrayList<Detective>();
		detectives.add(new Detective(1, nodes.get(0), field));
		detectives.add(new Detective(2, nodes.get(25), field));
		detectives.add(new Detective(3, nodes.get(12), field));
		field.setDetectives(detectives);
		
		// Create MisterX
		MisterX_KI misterX = new MisterX_KI(nodes.get(18), field);
		field.setMisterX(misterX);
		field.setLastPositionOfMisterX(misterX.getPosition());
		
		// Start
		field.play();
	}

}
