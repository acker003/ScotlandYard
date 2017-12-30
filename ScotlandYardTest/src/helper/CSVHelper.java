package helper;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import main.Matchfield;
import main.Node;

public class CSVHelper {
	
	private static Iterable<CSVRecord> records;

	private static void readCSV (String fName) {
		try {
			Reader in = new FileReader(fName);
			records = CSVFormat.EXCEL.withFirstRecordAsHeader().withDelimiter(',').parse(in);
		} catch (FileNotFoundException e) {
			System.out.println("Keine CSV mit dem angegebenen Namen gefunden!");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Es kann nicht auf das Dateisystem zugegriffen werden!");
			e.printStackTrace();
		} 	
    }	
	
	public static double[][] getDistanceTable(List<Node> nodeList) {
		readCSV("DistanceMatrix.csv");
		double[][] distanceTable = new double[Matchfield.AMOUNT_OF_NODES][Matchfield.AMOUNT_OF_NODES];
		int row = 0;
		for (CSVRecord rec : records) {
			for (int i = 0; i < rec.size() - 1; i++) {
				distanceTable[row][i] = Double.parseDouble(rec.get(i + 1));
			}
			row++;
		}
		for (int i = 0; i < distanceTable.length; i++) {
			double sum = 0;
			for (int j = 0; j < distanceTable[i].length; j++) {
				sum += distanceTable[i][j];
			}
			double mean = sum / distanceTable[i].length;
			nodeList.get(i).setCentrality(1/mean);
		}
		return distanceTable;
	}
	
	public static Map<Node, List<Node>> getNeighborNodes(int id, List<Node> nodeList) {
		switch (id) {
		case 0:
			// Taxi
			readCSV("TaxiGraph.csv");
			break;
		case 1:
			// Bus
			readCSV("BusGraph.csv");
			break;
		case 2:
			// Subway
			readCSV("SubwayGraph.csv");
			break;
		default:
			System.err.println("Fehlerhafte Eingabe: " + id);
			break;
		}
		
		HashMap<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
		int index = 0;
		for (CSVRecord rec : records) {
			Node node = nodeList.get(index);
			List<Node> neighbors = new ArrayList<Node>();
			for (int i = 1; i < rec.size(); i++) {
				if (!rec.get(i).equals("Inf")) {
					neighbors.add(nodeList.get(i - 1));
				}
			}
			nodeMap.put(node, neighbors);
			index++;
		}
		
		return nodeMap;
	}
}
