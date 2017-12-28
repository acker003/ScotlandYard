package firstTest;

import java.util.Scanner;

public class Detective extends Person {

	private int id;
	private Scanner in = new Scanner(System.in);
	
	public Detective(int id, Node position, Matchfield field) {
		super(position, field);
		this.id = id;
	}
	
	public int getID() {
		return id;
	}
	
	@Override
	public void ziehe() {
		System.out.print("Bitte ziehen Sie fuer Detektiv " + id + ": ");
		int nodeID = in.nextInt();
		this.setPosition(nodeID);
	}
	
	
}
