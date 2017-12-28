package firstTest;

import java.util.HashMap;
import java.util.Map;

public final class Evaluater {

	private static Matchfield field;
	
	private Evaluater() {}
	
	public static void setMatchfield(Matchfield f) {
		field = f;
	}
	
	public static double sigmoid(double value, double min, double max) {
		double mean = (max + min) / 2;
		double base = Math.pow(9, 1/(max-mean));
		return 1/(1 + Math.pow(base, -(value - mean)));
	}
	
	public static double getChaos() {
		double chaos = -1;
		
		// Distance to possible locations of Mister X
		chaos += getDistanceToPossibilities();
		
		// Mean Evaluation per Possible Position
		double sumEval = 0;
		for (Node node : field.getPossiblePositionsOfMisterX()) {
			sumEval += evaluatePosition(node, 2, 2, 4);
		}
		chaos += sumEval / field.getPossiblePositionsOfMisterX().size();
		if (Double.isNaN(chaos)) chaos = 0;
		return chaos;
	}
	
	public static double evaluatePosition(Node node) {
		return evaluatePosition(node, 1, 1, 5);
	}
	
	/**
	 * Diese Methode bewertet eine moegliche Position fuer Mister X hinsichtlich
	 * verschiedener Kriterien.<br>
	 * <b>Centrality</b>: Wie zentral liegt dieser Knoten? Der Wert ist der 
	 * Kehrwert der durchschnittlichen Distanz von diesem Knoten zu allen 
	 * anderen Knoten auf der Map. Je zentraler ein Knoten liegt, desto geringer
	 * ist die Distanz und desto groesser ist der Kehrwert. Die Zentralitaet
	 * liegt demnach immer zwischen 0 und 1. Bei groesseren Graphen ist eine 
	 * durchschnittliche Distanz von mindestens 3 zu erwarten, die Zentralitaet
	 * betraegt also maximal 1/3.<br>
	 * <b>Degree</b>: Welcher Grad hat der Knoten bzw. wie viele Fluchtwege 
	 * stehen theoretisch zur Verfügung? Je hoeher dieser Wert ist, desto 
	 * besser ist dieser Knoten. Allerdings sind Knoten mit hohem Grad i.d.R.
	 * auch schneller durch die Detektive erreichbar (vgl. Distance).<br>
	 * <b>Distance</b>: Wie weit sind die Detektive von diesem Knoten entfernt?
	 * Das ist nur eine grobe Schaetzung, hier wird die durchschnittliche 
	 * Entfernung in Zuegen 1:1 mit der kuerzesten Entfernung eines Detektivs
	 * gewichtet. Sind die Detektive also 3, 5 und 8 Zuege entfernt, ist das
	 * Minimum 3, der Mittelwert 5 und die gewichtete Distanz 4.
	 * @param node
	 * @param centralityFactor
	 * @param degreeFactor
	 * @param distanceFactor
	 * @return
	 */
	public static double evaluatePosition(Node node, double centralityFactor, 
			double degreeFactor, double distanceFactor) {
		// Centrality
		double centrality = sigmoid(node.getCentrality(), 0, 0.4);
		// Degree of Node = Amount of Ways to get out
		double degree = sigmoid(node.getAllNeighbors().size(), 2, 6);
		
		// Distance of the detectives to the nodes
		double min = Double.POSITIVE_INFINITY;
		double distance = 0;
		for (Detective det : field.getDetectives()) {
			double dist = field.getDistance(det, node);
			if (dist < min) min = dist;
			distance += dist;
		}
		// Mean distance per Detective
		distance /= field.getDetectives().size();
		// Weighted by min distance
		distance += min;
		distance /= 2;
		distance = sigmoid(distance, 1, 6);
		// Rating
		double rating = centrality * centralityFactor + degree * degreeFactor 
				+ Math.pow(distance - 1, 2) * distanceFactor;
		return rating;
	}

	private static double getDistanceToPossibilities() {
		/* Berechnet die durchschnittlich Distanz von den Detektiven
		 * zu Mister X. Dabei wird beruecksichtigt, dass nicht ein Detektiv
		 * alleine alle Moeglichkeiten abdecken kann. Es wird zunaechst die 
		 * kuerzeste und zweitkuerzeste Distanz berechnet und gezaehlt, welcher
		 * Detektiv wie haeufig am naechsten dran war. Wenn einer doppelt so 
		 * haeufig am naechsten dran war wie der zweitnaechste, dann wird die
		 * kuerzeste Distanz mit der des zweitnaechsten verrechnet (aktuell 2:1)
		 * Am Ende wird die Gesamt-Distanz berechnet und durch Teilen durch die
		 * Anzahl an Moeglichkeiten gewichtet und zurueckgegeben.
		 * Gewichtung notwendig weil gelegentlich Moeglichkeiten ausgeschlossen
		 * werden koennen und Wurzel weil so immer noch mehr Moeglichkeiten
		 * besser sind.
		 */
		// TODO
		/*
		 * Man sollte die Distanz zu hoch bewerteten Standorten bevorzugt
		 * bewerten. Denn schlechte Alternativen werden auch von den Detektiven
		 * ermittelt und ihnen wird weniger Beachtung geschenkt.
		 */
		
		// Anzahl moeglicher Standorte
		int amountOfPossibilities = field.getPossiblePositionsOfMisterX().size();
		System.out.println("Moeglichkeiten: " + amountOfPossibilities);
		// Map mit Detektiv und Haeufigkeit am naechsten dran zu sein
		Map<Detective, Integer> detMap = new HashMap<Detective, Integer>();
		for (Detective det : field.getDetectives()) {
			detMap.put(det, 0);
		}
		
		// Distanzwerte
		double distanceMin = 0;
		double distanceMed = 0;
		double totalDistance = 0;
		
		// Fuer jede moegliche Position wird die Distanz berechnet
		for (Node node : field.getPossiblePositionsOfMisterX()) {
			// Temp-Distanzen fuer die Detektive
			double minDistance = Double.POSITIVE_INFINITY;
			double medDistance = Double.POSITIVE_INFINITY;
			Detective minDetective = null;
			for (Detective det : field.getDetectives()) {
				double dist = field.getDistance(det, node);
				if (dist < minDistance) {
					minDetective = det;
					medDistance = minDistance;
					minDistance = dist;
				} else if (dist < medDistance) {
					medDistance = dist;
				}
			}
			// MinDetektiv updaten
			detMap.put(minDetective, detMap.get(minDetective) + 1);
			// Distanzen updaten
			distanceMin += minDistance;
			distanceMed += medDistance;
		}
		
		// Auswertung
		int max = 0;
		int med = 0;
		for (Integer i : detMap.values()) {
			if (i > max) {
				med = max;
				max = i;
			}
			else if (i > med) med = i;
		}
		System.out.println(max + " " + med + " " + distanceMin + " " + distanceMed);
		// Wenn die Max-Anzahl mehr als das Doppelte von zweiten betraegt
		// Wird die Gesamtanzahl 2:1 gewichtet
		if (max > 2 * med) totalDistance = (2 * distanceMin + distanceMed / 3);
		// Sonst bleibt es bei der kuerzesten
		else totalDistance = distanceMin;
		// Skalierung des Chaos durch teilen durch die Wurzel
		totalDistance /= Math.sqrt(amountOfPossibilities);
		if (Double.isNaN(totalDistance)) totalDistance = -1;
		return totalDistance;
	}
	
}
