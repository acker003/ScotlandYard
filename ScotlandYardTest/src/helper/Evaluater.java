package helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import config.Config;
import main.Matchfield;
import main.Node;
import persons.Detective;

public final class Evaluater {

	private static Matchfield field;
	
	// Config Explanation
	// 1: 2 Werte fuer Verhaeltnis Chaos - Situation
	// 2: 5 Werte fuer Verhaeltnis Centrality - Degree - EscapePotential - Distance - RiskBeingCircled
	// 3: 2 Werte fuer Verhaeltnis bei Chaosbewertung Distance - Situation
	/**
	 * NORMAL-Config legt Wert auf gute Position, weniger auf Chaos. Geachtet
	 * wird sowohl auf gutes Verschwinden als auch auf gute Entfernung. Chaos
	 * entsteht sowohl bei großer Distanz als auch bei guter Fluchtsituation.
	 */
	public static final Config NORMAL = new Config(1, 2, 1, 1, 3, 3, 2, 1, 1);
	
	/**
	 * Diese Config wird gewaehlt wenn man eingekreist wurde. Hier wird mehr
	 * Wert auf Chaos gelegt, wobei sich Chaos ueberwiegend durch gute 
	 * Vernetzung auszeichnet. Nebenrangig ist die gute Situation die brutal
	 * auf Flucht ausgelegt ist, waehrend Distanz egal ist.
	 */
	public static final Config BRUTAL_ESCAPE = new Config(2, 1, 2, 2, 4, 1, 4, 1, 3);
	
	/**
	 * Detektive sind immer um moeglichst wenig Chaos bemueht, allerdings wird
	 * etwas beruecksichtigt, dass gute Fluchtwege schlecht sind. Chaos ist 
	 * fast ausschliesslich durch gute Fluchtwege definiert, da das Ziel
	 * einkreisen ist. 
	 */
	private static final Config TYPICAL_DETECTIVE_CONFIG = new Config(2, 1, 3, 2, 4, 1, 5, 1, 3);
	
	private static final double THRESHOLD_FOR_BRUTAL_ESCAPE = 0.7;
	public static Config actualConfig = NORMAL;
	
	private Evaluater() {}
	
	public static void setMatchfield(Matchfield f) {
		field = f;
	}
	
	public static double sigmoid(double value, double min, double max) {
		double mean = (max + min) / 2;
		double base = Math.pow(9, 1/(max-mean));
		return 1/(1 + Math.pow(base, -(value - mean)));
	}
	
	/**
	 * Berechnet eine Wahrscheinlichkeit zwischen 0 und 1 die das Risiko
	 * angibt, dass man gerade eingekreist wird. Wenn dieser Wert ausreichend
	 * hoch ist, wird der BRUTAL_ESCAPE-Modus aktiviert um die Situation zu 
	 * retten.
	 * @return Risiko einer Einkreisung
	 */
	public static double calculateRiskOfBeingCircled(Node actualPosition, List<Double> lastBestRatings, boolean withActualSituation) {
		double risk = 0.1;
		// Berechnung
		// Suche Nachbarknoten die im Minimalfall am weitesten weg sind und maximiere
		// diese Distanz. 
		double minDistanceToActualPosition = field.getMinDistance(actualPosition);
		double minDistanceToNeighborNodes = Double.NEGATIVE_INFINITY;
		double minDistanceToNeighborsOfNeighbors = Double.NEGATIVE_INFINITY;
		for (Node n : actualPosition.getAllNeighbors()) {
			double value = field.getMinDistance(n);
			if (value > minDistanceToNeighborNodes) minDistanceToNeighborNodes = value;
			for (Node n2 : n.getAllNeighbors()) {
				double value2 = field.getMinDistance(n2);
				if (value > minDistanceToNeighborsOfNeighbors) minDistanceToNeighborsOfNeighbors = value2;
			}
		}
		if (minDistanceToNeighborNodes <= minDistanceToActualPosition) {
			// Wenn wechseln nichts bringt, sieht es schlecht aus
			risk = 0.8;
		}
		if (minDistanceToNeighborsOfNeighbors - 1 <= minDistanceToActualPosition) {
			// Wenn auch die Nachbarknoten der Nachbarknoten nicht besser sind
			// Sind wir definitiv am Arsch
			risk = 1;
		}
		// Gewichtung mit der Anzahl der MinZuege -> Radius des Kreises
		risk *= 2 / minDistanceToActualPosition;
		// Gewichtung mit aktueller Situation (ab 70 ist schlecht)
		if (withActualSituation) risk *= (100 - Evaluater.evaluateSituation(actualPosition)) / 30;
		else if (lastBestRatings.size() > 0) risk *= (100 - lastBestRatings.get(lastBestRatings.size() - 1)) / 30;
		// Gewichtung mit der Historie der letzten Zuege
		if (lastBestRatings.size() >= 3) {
			double meanImprovement = 0;
			for (int i = 1; i < 2; i++) {
				double rating1 = lastBestRatings.get(lastBestRatings.size() - i);
				double rating2 = lastBestRatings.get(lastBestRatings.size() - i - 1);
				// Verbesserung durch den letzten Zug
				double improvement = rating1 - rating2;
				meanImprovement += improvement < 0 ? improvement : 0.5 * improvement;
			}
			meanImprovement /= 2;
			if (meanImprovement < 0) {
				risk *= 1.4;
			}
		}
		risk = sigmoid(risk, 0.1, 1.5);
		
		// Auswertung
		if (risk > THRESHOLD_FOR_BRUTAL_ESCAPE) {
			actualConfig = BRUTAL_ESCAPE;
		} else {
			actualConfig = NORMAL;
		}
		return risk;
	}
	
	/**
	 * Diese Methode berechnet die Bewertung eines Zuges fuer Mister X.<br>
	 * Enthalten sind eine Situationsbewertung und eine Chaosbewertung. Die
	 * Situationsbewertung enthaelt die Bewertung von Fluchtwegen und die Distanz
	 * zu den Detektiven. Berechnet wird diese mit <i>evaluateSituation</i>.
	 * Die Chaosbewertung enthaelt die Bewertung des entstehenden Chaos und fusst
	 * auf der Anzahl der moeglichen Positionen sowie ihrer Relevanz fuer die 
	 * Detektive hinsichtlich Fluchtpotenzial und Abdeckungsmoeglichkeit.
	 * Berechnet wird diese mit <i>evaluateChaos</i>.<br>
	 * Wie stark Situation und Chaos bewertet werden, haengt von der aktuellen
	 * Konfiguration ab und ist in <i>SITUATION_FACTOR</i> und <i>CHAOS_FACTOR</i>
	 * hinterlegt.
	 * @param node Der Knoten auf den gezogen wurde / gezogen werden kann
	 * @param i Das Verkehrsmittel (0 = Taxi, 1 = Bus, 2 = U-Bahn, 3 = Black)
	 * @param amountOfBlackTickets Anzahl noch vorhandener Black-Tickets
	 * @return Eine Zugbewertung zwischen 0 und 100 (100 = bester Zug)
	 */
	public static double evaluateMoveForMisterX(Node node, int i, int amountOfBlackTickets) {
		// Berechne das Rating fuer diesen Zug fuer Mister X 
		// Bestandteile: Situationsbewertung und entstehendes Chaos
		
		// Situationsbewertung
		double situationRating 
			= actualConfig.SITUATION_FACTOR * Evaluater.evaluateSituation(node);
		// Chaosbewertung
		// Chaosrelevanz hinsichtlich Zuegen bis zum naechsten zeigen
		double chaosWeight = 0.1 * Math.sqrt(20*field.getMovesToNextSight());
		double chaosRating = actualConfig.CHAOS_FACTOR 
				* chaosWeight * Evaluater.evaluateChaos();
		// Kombination der beiden Ratings
		double finalRating = (situationRating + chaosRating) 
				/ (actualConfig.SITUATION_FACTOR + actualConfig.CHAOS_FACTOR);
		
		// Bestrafung fuer Black-Tickets
		if (i == 3) {
			finalRating *= 1 - 0.05 * Math.pow(2, (4 - amountOfBlackTickets)/3);
			if (field.getMovesToNextSight() <= 1) finalRating *= 0.7;
			if (amountOfBlackTickets == 0) finalRating = Double.NEGATIVE_INFINITY;
		}
		return finalRating;
	}
	
	/**
	 * Berechnet das Chaos fuer die Detektive laut aktuellen Informationen.<br>
	 * Beruecksichtigt wird die (komplexe) Distanz der Detektive zu den
	 * moeglichen Positionen von Mister X. Diese wird gewichtet mit der
	 * durchschnittlichen Bewertung der Standorte. Je besser die moeglichen
	 * Standorte fuer Mister X sind, desto groesser ist das Chaos.
	 * @return Chaos-Bewertung zwischen 0 und 100 (100 ist grosses Chaos)
	 */
	public static double evaluateChaos() {
		// Distance to possible locations of Mister X
		double distanceToPossibilities = getDistanceToPossibilities();
		
		// Mean Evaluation per Possible Position
		double sumEval = 0;
		for (Node node : field.getPossiblePositionsOfMisterX()) {
			// Evaluation of Situation with TypicalDetectiveConfig
			sumEval += evaluateSituation(node, TYPICAL_DETECTIVE_CONFIG.CENTRALITY_FACTOR, 
					TYPICAL_DETECTIVE_CONFIG.DEGREE_FACTOR, TYPICAL_DETECTIVE_CONFIG.DISTANCE_FACTOR, 
					true, TYPICAL_DETECTIVE_CONFIG.ESCAPE_POTENTIAL_FACTOR, TYPICAL_DETECTIVE_CONFIG.RISK_OF_BEING_CIRCLED_FACTOR);
		}
		double evaluationOfPossibilities = sigmoid(sumEval 
				/ Math.sqrt(field.getPossiblePositionsOfMisterX().size()), 0, 400) * 100;
		
		// Chaos Rating with Actual Config to weight Distance and Evaluation
		double chaos = (distanceToPossibilities * actualConfig.CHAOS_DISTANCE_FACTOR 
				+ evaluationOfPossibilities * actualConfig.CHAOS_EVALUATION_FACTOR)
				/ (actualConfig.CHAOS_DISTANCE_FACTOR + actualConfig.CHAOS_EVALUATION_FACTOR);
		return chaos;
	}
	
	public static double evaluateSituation(Node node) {
		return evaluateSituation(node, actualConfig.CENTRALITY_FACTOR, actualConfig.DEGREE_FACTOR, 
				actualConfig.DISTANCE_FACTOR, true, actualConfig.ESCAPE_POTENTIAL_FACTOR, actualConfig.RISK_OF_BEING_CIRCLED_FACTOR);
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
	 * Minimum 3, der Mittelwert 5 und die gewichtete Distanz 4.<br>
	 * <b>EscapePotential</b>: Wie effektiv sind die moeglichen Fluchtwege?
	 * Dafuer werden die Positionen aller Fluchtwege bewertet und gemittelt.
	 * Um eine Endlosrekursion zu vermeiden wird angegeben, ob dieser Faktor
	 * einbezogen werden soll oder nicht.
	 * @param node Der zu untersuchende Knoten
	 * @param centralityFactor Der Faktor fuer die Gewichtung der Zentralitaet
	 * @param degreeFactor Der Faktor fuer die Gewichtung des Grades und der
	 * Fluchtwege
	 * @param distanceFactor Der Faktor fuer die Gewichtung der Distanz
	 * @param isEscapePotential Soll das Fluchtpotenzial ueber die Nachbarknoten
	 * einbezogen werden (Rekursionsgefahr wenn true)
	 * @return Positionsbewertung zwischen 0 und 100
	 */
	public static double evaluateSituation(Node node, double centralityFactor, 
			double degreeFactor, double distanceFactor, boolean isEscapePotential, 
			double escapePotentialFactor, double riskBeingCircledFactor) {
		// Centrality
		double centrality = sigmoid(node.getCentrality(), 0.15, 0.30);
		// Degree of Node = Amount of Ways to get out
		double degree = sigmoid(node.getAllNeighbors().size(), 2, 12);
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
		distance = 2 * distance + min;
		distance /= 3;
		distance = sigmoid(distance, 1, 6);
		// Risk Being Circled by the detectives
		double risk = Evaluater.calculateRiskOfBeingCircled(node, field.getMisterX().getLastBestRatings(), false);
		
		// Rating
		// Centrality and Degree with factor
		// Distance 0.5 is normal, all above is good, all below is bad
		// Low Risk is good
		double rating = centrality * centralityFactor + degree * degreeFactor 
				+ Math.pow(distance + 0.5, 2) * distanceFactor + (1 - risk) * riskBeingCircledFactor;
		// Escape Potential
		if (isEscapePotential) {
			double sum = 0;
			for (Node n : node.getAllNeighbors()) {
				sum += evaluateSituation(n, centralityFactor, degreeFactor, distanceFactor, false, 0, riskBeingCircledFactor);
			}
			double mean = 0.01 * sum / node.getAllNeighbors().size();
			rating += mean * escapePotentialFactor;
			rating /= centralityFactor + 2 * degreeFactor + distanceFactor + riskBeingCircledFactor;
		} else {
			rating /= centralityFactor + degreeFactor + distanceFactor + riskBeingCircledFactor;
		}
		
		// Final rating
		rating = sigmoid(rating, 0.1, 0.9) * 100;
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
		// Wenn die Max-Anzahl mehr als das Doppelte von zweiten betraegt
		// Wird die Gesamtanzahl 2:1 gewichtet
		if (max > 2 * med) totalDistance = (2 * distanceMin + distanceMed / 3);
		// Sonst bleibt es bei der kuerzesten
		else totalDistance = distanceMin;
		// Skalierung des Chaos durch teilen durch die Wurzel
		totalDistance /= Math.sqrt(amountOfPossibilities);
		return sigmoid(totalDistance, 2, 15) * 100;
	}
	
}
