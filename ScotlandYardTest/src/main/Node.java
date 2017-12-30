package main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Node {

	public static int counter = 0;
	
	private int id = ++counter;
	private List<Node> neighborsTaxi = new ArrayList<Node>();
	private List<Node> neighborsBus = new ArrayList<Node>();
	private List<Node> neighborsSubway = new ArrayList<Node>();
	private double centrality = 0;
	
	public void setNeighbors(int id, List<Node> neighbors) {
		switch (id) {
		case 0:
			neighborsTaxi = neighbors;
			break;
		case 1:
			neighborsBus = neighbors;
			break;
		case 2:
			neighborsSubway = neighbors;
			break;
		default:
			System.err.println("Falsche Eingabe: " + id);
		}
	}
	
	public List<Node> getNeighbors(Vehicle vehicle) {
		switch (vehicle) {
		case TAXI:
			return neighborsTaxi;
		case BUS:
			return neighborsBus;
		case SUBWAY:
			return neighborsSubway;
		case BLACK:
			return getAllNeighbors();
		default:
			System.err.println("Falsche Eingabe: " + id);
			return null;
		}
	}
	
	public List<Node> getAllNeighbors() {
		List<Node> allNodes = new ArrayList<Node>(neighborsTaxi);
		allNodes.addAll(neighborsBus);
		allNodes.addAll(neighborsSubway);
		return new ArrayList<Node>(new HashSet<Node>(allNodes));
	}
	
//	public boolean hasStop(int id) {
//		switch (id) {
//		case 0:
//			return !neighborsTaxi.isEmpty();
//		case 1:
//			return !neighborsBus.isEmpty();
//		case 2:
//			return !neighborsSubway.isEmpty();
//		default:
//			System.err.println("FEHLER BEI ID!");
//			return false;
//		}
//	}
	
	public int getID() {
		return id;
	}

	public double getCentrality() {
		return centrality;
	}

	public void setCentrality(double centrality) {
		this.centrality = centrality;
	}
	
	public String toString() {
		String s = "Knoten " + id + "; Insgesamt: " + getAllNeighbors().size() 
				+ " Nachbarknoten!\n";
		s += "Taxiverbindungen zu: ";
		for (Node n : neighborsTaxi) {
			s += n.getID() + " ";
		}
		s += "\nBusverbindungen zu: ";
		for (Node n : neighborsBus) {
			s += n.getID() + " ";
		}
		s += "\nU-Bahnverbindungen zu: ";
		for (Node n : neighborsSubway) {
			s += n.getID() + " ";
		}
		return s;
	}

}
