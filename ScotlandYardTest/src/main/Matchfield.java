package main;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;

import helper.CSVHelper;
import helper.Evaluater;
import persons.Detective;
import persons.MisterX_KI;

public class Matchfield {

	public static final int AMOUNT_OF_NODES = 199;
	
	private List<Node> nodes = new ArrayList<Node>();
	private List<Detective> detectives = new ArrayList<Detective>();
	private MisterX_KI misterX;
	private double[][] distanceTable;
	private int round = 3;
	private static List<Integer> sightsAtRound = Arrays.asList(3, 8, 13, 18, 24);
	private static List<Integer> startNodes = new ArrayList<Integer>(Arrays.asList(13,26,29,34,50,53,91,94,103,112,117,132,138,141,155,174,197,198));
	
	private Node lastPositionOfMisterX;
	private List<Node> possiblePositionsOfMisterX = new ArrayList<Node>();
	
	public void play() {
		//possiblePositionsOfMisterX = new ArrayList<Node>(nodes);
		// TODO GANZ WICHTIG!!! AENDERN!!!
		possiblePositionsOfMisterX.add(misterX.getPosition());
		Evaluater.setMatchfield(this);
		// Start
		while(misterX.isFree() && round < 24) {
			round++;
			System.out.println("Runde " + round + "!");
			long milli = System.currentTimeMillis();
			misterX.ziehe();
			System.out.println(System.currentTimeMillis() - milli);
			if (misterX.isFree()) {
				for (Detective det : detectives) {
					det.ziehe();
				}
			}
			for (Detective det : detectives) {
				if (possiblePositionsOfMisterX.contains(det.getPosition())) {
					possiblePositionsOfMisterX.remove(det.getPosition());
				}
			}
			// Wie viele Moeglichkeiten
			int sum = 0;
			List<Node> belegtePlaetze = new ArrayList<Node>();
			for (Node n1 : detectives.get(0).getPosition().getAllNeighbors()) {
				belegtePlaetze.add(n1);
				for (Node n2 : detectives.get(1).getPosition().getAllNeighbors()) {
					if (belegtePlaetze.contains(n2)) continue;
					belegtePlaetze.add(n2);
					for (Node n3 : detectives.get(2).getPosition().getAllNeighbors()) {
						if (belegtePlaetze.contains(n3)) continue;
						belegtePlaetze.add(n3);
						for (Node n4 : detectives.get(3).getPosition().getAllNeighbors()) {
							if (belegtePlaetze.contains(n4)) continue;
							sum++;
						}
						belegtePlaetze.remove(n3);
					}
					belegtePlaetze.remove(n2);
				}
				belegtePlaetze.remove(n1);
			}
			System.out.println(sum + " Moeglichkeiten");
		}
		if (misterX.isFree()) {
			System.out.println("Mister X wurde nicht gefunden! "
					+ "Er steht jetzt an der Haltestelle " + misterX.getPosition().getID());
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
	
	public double getMinDistance(Node node) {
		double min = Double.POSITIVE_INFINITY;
		for (Detective det : detectives) {
			double value = getDistance(det, node);
			if (value < min) min = value;
		}
		return min;
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
	
	public int getMovesToNextSight() {
		for (Integer i : sightsAtRound) {
			if (i - round > 0) {
				return i - round + 1;
			}
		}
		return 0;
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
		if (print) {
			System.out.println("Mister X ist " + getNameToVehicle(vehicle) + " gefahren.");
			if (sightsAtRound.contains(round)) {
				System.out.println("Mister X ist jetzt auf dem Feld " 
						+ misterX.getPosition().getID() + "!");
				System.out.println("Diesen Weg hat er genommen:");
				misterX.printMoves();
				possiblePositionsOfMisterX.clear();
				possiblePositionsOfMisterX.add(misterX.getPosition());
			}
		}
	}
	
	public String getNameToVehicle(Vehicle vehicle) {
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
		
		// Create Detectives
		List<Detective> detectives = new ArrayList<Detective>();
//		detectives.add(new Detective(1, nodes.get(getRandomStartPosition()), field)); 
//		detectives.add(new Detective(2, nodes.get(getRandomStartPosition()), field));
//		detectives.add(new Detective(3, nodes.get(getRandomStartPosition()), field));
//		detectives.add(new Detective(4, nodes.get(getRandomStartPosition()), field));
		
		detectives.add(new Detective(1, nodes.get(66), field)); 
		detectives.add(new Detective(2, nodes.get(87), field));
		detectives.add(new Detective(3, nodes.get(127), field));
		detectives.add(new Detective(4, nodes.get(139), field));
		
		field.setDetectives(detectives);
		
		// Create MisterX
		//MisterX_KI misterX = new MisterX_KI(nodes.get(getRandomStartPosition()), field);
		MisterX_KI misterX = new MisterX_KI(nodes.get(115), field);
		
		field.setMisterX(misterX);
		field.setLastPositionOfMisterX(misterX.getPosition());
		
		// Start
		field.play();
	}
	
	private static int getRandomStartPosition() {
		int rand = (int)(Math.random() * startNodes.size());
		int index = startNodes.get(rand) - 1;
		startNodes.remove(rand);
		return index;
	}

}
