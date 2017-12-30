package persons;

import java.util.Scanner;

import main.Matchfield;
import main.Node;

public class Detective extends Person {

	private int id;
	private Scanner in = new Scanner(System.in);
	
	public Detective(int id, Node position, Matchfield field) {
		super(position, field);
		System.out.println("Detektiv " + id + " startet bei Haltestelle " + position.getID());
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	@Override
	public void ziehe() {
		int nodeID;
		do {
			System.out.print("Bitte ziehen Sie fuer Detektiv " + id + ": ");
			nodeID = in.nextInt();
		} while(!this.setPosition(nodeID));
		
	}
	
	
}
