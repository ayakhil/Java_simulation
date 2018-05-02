/* 
 * Brian Lewis
 * Akhil Yaragangu 
 * Hanin Alshalan
 * 
 * A single queue server processing three different types of messages, each with
 * different amount of complexity.
 */

import java.util.*;

public class Simulation {
	// ********** WORKLOAD VARIABLES ***********
	double arrivalRate1 = 3.0;	// Message Type 1 MEAN inter-arrival
	double arrivalRate2 = 2.2;	// Message Type 2 MEAN inter-arrival
	double arrivalRate3 = 1.5;	// Message Type 3 MEAN inter-arrival
	double serviceRate1 = 1.2;	// Message Type 1 MEAN service time
	double serviceRate2 = .9;	// Message Type 2 MEAN service time
	double serviceRate3 = .5;	// Message Type 3 MEAN service time
	// *****************************************
	
	// Lists to store the next incoming messages of each type
	LinkedList<Message> message1;
	LinkedList<Message> message2;
	LinkedList<Message> message3;
	
	LinkedList<Message> queue; 		// Queue for the incoming message types
	PriorityQueue<Event> eventList; // To order events to simulate
	double clock; 					// Simulation clock
	
	Random rand = new Random();
	
	// Statistics Variables
	int numArrivals;
	int numDepartures;
	double totalWaitTime, avgWaitTime;
	double totalSystemTime, avgSystemTime;
	
	// Initializes variables, Generates 5 of each incoming message stream, schedules first arrival
	void init(){ 
		queue = new LinkedList<Message> ();
		eventList = new PriorityQueue<Event> ();
		clock = 0.0;
		numArrivals = 0;
		numDepartures = 0;
		totalWaitTime = 0.0;
		totalSystemTime = 0.0;
		
		message1 = new LinkedList<Message> ();
		message2 = new LinkedList<Message> ();
		message3 = new LinkedList<Message> ();
		
		double firstMessage1 = randomInterarrivalTime1();
		double firstMessage2 = randomInterarrivalTime2();
		double firstMessage3 = randomInterarrivalTime3();
		
		if((firstMessage1 < firstMessage2) && (firstMessage1 < firstMessage3)){
			firstMessage2 = firstMessage2 - firstMessage1;
			firstMessage3 = firstMessage3 - firstMessage1;
			firstMessage1 = 0.0;
		}
		else if((firstMessage2 < firstMessage1) && (firstMessage2 < firstMessage3)){
			firstMessage1 = firstMessage1 - firstMessage2;
			firstMessage3 = firstMessage3 - firstMessage2;
			firstMessage2 = 0.0;
		}
		else{
			firstMessage1 = firstMessage1 - firstMessage3;
			firstMessage2 = firstMessage2 - firstMessage3;
			firstMessage3 = 0.0;
		}
		
		message1.add(new Message(1, firstMessage1));
		message2.add(new Message(2, firstMessage2));
		message3.add(new Message(3, firstMessage3));
		
		while(message3.size() < 5){
			addMoreMessage1();
			addMoreMessage2();
			addMoreMessage3();	
		}
		
		scheduleArrival();
	}
	
	void simulate(int maxMessages){
		init();
		while(numArrivals < maxMessages){
			Event e = eventList.poll();
			clock = e.eventTime;
			if(e.type == Event.ARRIVAL){
				handleArrival(e);
			}
			else{
				handleDeparture(e);
			}
		}
		stats();
	}
	
	void handleArrival(Event e){
		numArrivals++;
		double timeMessage1 = message1.getFirst().arrivalTime;
		double timeMessage2 = message2.getFirst().arrivalTime;
		double timeMessage3 = message3.getFirst().arrivalTime;
		
		if((timeMessage1 < timeMessage2) && (timeMessage1 < timeMessage3)){
			queue.add(message1.removeFirst());
		}
		else if((timeMessage2 < timeMessage1) && (timeMessage2 < timeMessage3)){
			queue.add(message2.removeFirst());
		}
		else{
			queue.add(message3.removeFirst());
		}
		
		while(message1.size() < 5){ // to maintain list of new incoming messages 1
			addMoreMessage1();
		}
		
		while(message2.size() < 5){ // to maintain list of new incoming messages 2
			addMoreMessage2();
		}
		
		while(message3.size() < 5){ // to maintain list of new incoming messages 3
			addMoreMessage3();
		}
		
		if(queue.size() == 1){	//This is only message, schedule a departure
			scheduleDeparture();
		}
		scheduleArrival();
	}
	
	void handleDeparture(Event e){
		numDepartures++;
		Message m = queue.removeFirst();
		double timeInSystem = clock - m.arrivalTime;
		totalSystemTime = totalSystemTime + timeInSystem;
		if(queue.size() > 0){
			Message waitingMessage = queue.getFirst();
			double waitTime = clock - waitingMessage.arrivalTime;
			totalWaitTime = totalWaitTime + waitTime;
			scheduleDeparture();
		}
	}
	
	void scheduleArrival(){
		double timeMessage1 = message1.getFirst().arrivalTime;
		double timeMessage2 = message2.getFirst().arrivalTime;
		double timeMessage3 = message3.getFirst().arrivalTime;
		
		if((timeMessage1 < timeMessage2) && (timeMessage1 < timeMessage3)){
			eventList.add(new Event(timeMessage1, Event.ARRIVAL));
		}
		else if((timeMessage2 < timeMessage1) && (timeMessage2 < timeMessage3)){
			eventList.add(new Event(timeMessage2, Event.ARRIVAL));
		}
		else{
			eventList.add(new Event(timeMessage3, Event.ARRIVAL));
		}
	}
	
	void scheduleDeparture(){
		double nextDepartureTime;
		int messageType = queue.getFirst().messageType;
		if(messageType == 1){
			nextDepartureTime = clock + randomServiceTime1();
		}
		else if(messageType == 2){
			nextDepartureTime = clock + randomServiceTime2();
		}
		else{
			nextDepartureTime = clock + randomServiceTime3();
		}
		eventList.add(new Event(nextDepartureTime, Event.DEPARTURE));
	}
	
	double exponential(double mean){
		return((-mean) * Math.log(Math.random()));
	}
	
	double randomInterarrivalTime1(){
		return exponential(arrivalRate1);
	}
	
	double randomServiceTime1(){
		return(exponential(serviceRate1));
	}
	
	double randomInterarrivalTime2(){
		return(exponential(arrivalRate2));
	}
	
	double randomServiceTime2(){
		return(exponential(serviceRate2));
	}
	
	double randomInterarrivalTime3(){
		return(exponential(arrivalRate3));
	}
	
	double randomServiceTime3(){
		return(exponential(serviceRate3));
	}
	
	void addMoreMessage1(){
		message1.add(new Message(1, message1.getLast().arrivalTime + randomInterarrivalTime1()));
	}
	
	void addMoreMessage2(){
		message2.add(new Message(2, message2.getLast().arrivalTime + randomInterarrivalTime2()));
	}
	
	void addMoreMessage3(){
		message3.add(new Message(3, message3.getLast().arrivalTime + randomInterarrivalTime3()));
	}
	
	void stats(){
		avgWaitTime = totalWaitTime / numDepartures;
		avgSystemTime = totalSystemTime / numDepartures;
	}
	
	public String toString(){
		String results = "Simulation Results:";
		results += "\n Number of Arrivals: " + numArrivals;
		results += "\n Number of Departures: " + numDepartures;
		results += "\n Average Wait Time: " + avgWaitTime;
		results += "\n Average System Time: " + avgSystemTime;
		results += "\n Current Queue Size: " + queue.size();
		return results;
	}
	
	//************ MAIN ***************
	public static void main(String[] args){
		Simulation sim = new Simulation ();
		sim.simulate(10000);
		System.out.println(sim);
	}
}

// Message class will store each messages time and arrival time (for collecting
// statistics)
class Message {
	int messageType;
	double arrivalTime;

	public Message(int messageType, double arrivalTime) {
		this.messageType = messageType;
		this.arrivalTime = arrivalTime;
	}
}

// Event class
class Event implements Comparable {
	public static int ARRIVAL = 1;
	public static int DEPARTURE = 2;

	int type; // Arrive or Depart
	double eventTime;

	public Event(double eventTime, int type) {
		this.eventTime = eventTime;
		this.type = type;
	}

	public int compareTo(Object obj) {
		Event e = (Event) obj;
		if (eventTime < e.eventTime) {
			return -1;
		} else if (eventTime > e.eventTime) {
			return 1;
		} else {
			return 0;
		}
	}

	public boolean equals(Object obj) {
		return (compareTo(obj) == 0);
	}
}
