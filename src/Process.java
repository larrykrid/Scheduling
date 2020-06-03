import java.util.*;
import java.lang.StringBuilder;

public class Process {
	private int number;
	private int arrivalTime;
	private int priority;
	private ArrayList<String> eventType;
	private ArrayList<Integer> eventTime;
	public Process (int count, int at, int pri, ArrayList<String> type, ArrayList<Integer> time) {
		this.number = count;
		this.arrivalTime = at;
		this.priority = pri;
		this.eventType = new ArrayList <String>(type);
		this.eventTime = new ArrayList <Integer>(time);
	}
	
	public int getNumber() {
		return this.number;
	}
	
	public int getArrivalTime() {
		return this.arrivalTime;
	}
	
	public int getPriority() {
		return this.priority;
	}
	public void setPriority(int pri) {
		this.priority = pri;
	}
	public ArrayList<String> getEventTypes(){
		return this.eventType;
	}
	public void removeEventType() {
		this.eventType.remove(0);
	}
	public void addEventType(String type) {
		this.eventType.add(0, type);
	}
	public ArrayList<Integer> getEventTime(){
		return this.eventTime;
	}
	public void removeEventTime() {
		this.eventTime.remove(0);
	}
	public void addEventTime(int time) {
		this.eventTime.add(0, time);
	}
	
	public String printProcess() {
		StringBuilder sb1 = new StringBuilder();
		sb1.append("P" + number + " ");
		sb1.append(arrivalTime + " ");
		sb1.append(priority + " ");
		for(int i = 0; i < eventType.size(); i++) {
			sb1.append(eventType.get(i) + " " + eventTime.get(i) + " ");
		}
		return sb1.toString();
	}
}
