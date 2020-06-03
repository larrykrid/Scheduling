import java.io.File;
import java.util.*;
import java.lang.StringBuilder;

//https://www.geeksforgeeks.org/program-for-fcfs-cpu-scheduling-set-1/
//https://www.google.com/search?safe=active&sxsrf=ALeKk03beUUuJoLCH4aDj1syYxBOPGclxw%3A1587071908792&ei=pMuYXrL9L4HAsQWxg4LQDw&q=scheduling+algorithms+coding+examples&oq=scheduling+algorithms+coding+examples&gs_lcp=CgZwc3ktYWIQAzIFCCEQqwI6BAgAEEc6BwgjELACECc6BAgAEA06CAgAEAgQBxAeOggIABANEAUQHkoJCBcSBTEyLTQ3SggIGBIEMTItOVDzpQVY4KsFYMOzBWgAcAJ4AIABPYgBlAOSAQE3mAEAoAEBqgEHZ3dzLXdpeg&sclient=psy-ab&ved=0ahUKEwiy3-zK7-3oAhUBYKwKHbGBAPoQ4dUDCAw&uact=5
//https://www.geeksforgeeks.org/round-robin-scheduling-with-different-arrival-times/

public class Driver {
	public static void main(String [] args) {
		//integer and boolean values that will be read in from file
		int quantum = 0;
		int num_priorities = 0;
		boolean service_given = false;
		
		//integer values used for creating the processes
		int at; //arrival time
		int pr; //priority
		String schedule_type = "";
		Process process = null;
		ArrayList<String> type = new ArrayList<String>();
		ArrayList<Integer> time = new ArrayList<Integer>();
		ArrayList<Process> processes = new ArrayList<Process>();
		Scanner pf;
		Scanner sf;
		
		if(args.length != 2) {
			System.out.print("Error incorrect arguments: " + Arrays.toString(args));
			System.exit(0);
		}
		try {
			File process_file = new File(args[0]);
			pf = new Scanner(process_file);
			File schedule_file = new File(args[1]);
			sf = new Scanner(schedule_file);
			int count = 0;
			while(pf.hasNextLine()) {
				String inputLine = pf.nextLine();
				String[] arguments = inputLine.split(" ");
				at = Integer.parseInt(arguments[0]);
				pr = Integer.parseInt(arguments[1]);
				for (int i = 2; i < arguments.length; i++) {
					if (i %2 == 0) {
						type.add(arguments[i]);
					}
					else {
						time.add(Integer.parseInt(arguments[i]));
					}
				}
				process = new Process(count, at, pr, type, time);
				processes.add(process);
				//Make sure the list are empty
				type.clear();
				time.clear(); 
				count++;
			}

			//Find out what schedule algorithm we are using
			schedule_type = sf.nextLine();
			System.out.println("This is the Scheduling algorithm: " + schedule_type);
			if(schedule_type.equals("FCFS")) {
				firstCome(processes);
				
			}
			else if(schedule_type.equals("PRIORITY")) {
				//I am making an assumption here that this is how the input comes in
				String inputLine = sf.nextLine();
				String[] arguments = inputLine.split("=");
				num_priorities = Integer.parseInt(arguments[1]);
				//Debug to check it got read in
				System.out.println("This is the service_given: " + num_priorities);
				priority(processes, num_priorities);
			}
			else if(schedule_type.equals("RR")) {
				String inputLine = sf.nextLine();
				String[] arguments = inputLine.split("=");
				quantum = Integer.parseInt(arguments[1]);
				//Debug to check it got read in
				System.out.println("This is the quantum: " + quantum);
				roundRobin(processes, quantum);
				
			}
			else if(schedule_type.equals("SRT")) {
				String inputLine = sf.nextLine();
				String[] arguments = inputLine.split("=");
				service_given = Boolean.parseBoolean(arguments[1]);
				//Debug to check it got read in
				System.out.println("This is the service_given: " + service_given);
				if (service_given) {
					shortestRemaining(processes);
				}
				else {
					System.out.println("I did not know how to do this for false");
				}
			}
			else if(schedule_type.equals("FEEDBACK")) {
				String inputLine = sf.nextLine();
				String[] arguments = inputLine.split("=");
				quantum = Integer.parseInt(arguments[1]);
				//Debug to check it got read in
				System.out.println("This is the quantum: " + quantum);
				//I am making an assumption here that this is how the input comes in
				inputLine = sf.nextLine();
				arguments = inputLine.split("=");
				num_priorities = Integer.parseInt(arguments[1]);
				//Debug to check it got read in
				System.out.println("This is the service_given: " + num_priorities);
				feedback(processes, quantum, num_priorities);
			}
			else {
				System.out.println("Schedule algorithm does not exist");
			}
			
		}
		catch(Exception e) {
			System.out.println("Exception: " + e.getMessage());
		}
	}
	
	public static void firstCome (ArrayList<Process> pr) {
		int tracker = 0; //time tracker
		int burstTime = 0;
		int blockTime = 0;
		int processComp = 0;
		int addedPrc = 0;
		
		String event ="";
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		ArrayList<Process> processBlock = new ArrayList<Process>();
		Process workingProcess = null;
		Process blockedProcess = null;
		
		//Used to calculate the stats
		ArrayList<Integer> arrivalTime = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> timeStamp = new ArrayList<ArrayList<Integer>>();
		int [] finishTime = new int [pr.size()];
		int [] serviceTime = new int [pr.size()];
		int [] startTime = new int [pr.size()];
		float [] turnTime;
		float [] normTime;
		float [] avgResTime;
		//find the arrival time for each process
		for(int i = 0; i< pr.size(); i++) {
			arrivalTime.add(pr.get(i).getArrivalTime());
		}
		
		//find the sum of the time spent in the cpu
		for (int i = 0; i < pr.size(); i++) {
			//get the size for each process, did not want to create another for loop to do this step
			//Same case for these, I just need a forloop the size of the process arraylist
			ArrayList<Integer> temp = new ArrayList<Integer>();
	    	timeStamp.add(temp);
			//add up the sum
			for (int j = 0; j < pr.get(i).getEventTime().size(); j++) {
				if(pr.get(i).getEventTypes().get(j).equals("CPU")) {
					serviceTime[i] += pr.get(i).getEventTime().get(j);
				}
			}
		}
		
		while(processComp < pr.size()) {
			//Adding processes to ready queue
			for(int i = 0; i < pr.size(); i++) {
				if(pr.get(i).getArrivalTime() == tracker) {
					processQueue.add(pr.get(i));
					//First time a process is added
					timeStamp.get(pr.get(i).getNumber()).add(tracker);
					addedPrc++;
				}
			}
			
			while(true) {
				//Check if there is a process ready in the process queue
				if (workingProcess == null && processQueue.size() != 0) {
					//This is to see if it's the first time it's worked on
					workingProcess = processQueue.get(0);
					//This is when the process is pulled out
					timeStamp.get(workingProcess.getNumber()).add(tracker);
					processQueue.remove(0);
				}
				
				//Check if there is a blocked process in the queue and we are not using one
				if(blockedProcess == null && processBlock.size() != 0) {
					blockedProcess = processBlock.get(0);
					processBlock.remove(0);
				}
				
				//Check to see if we need to get a block time
				if(blockedProcess != null) {
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							blockTime = blockedProcess.getEventTime().get(0);
							blockedProcess.removeEventType();
							blockedProcess.removeEventTime();
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							processQueue.add(blockedProcess);
							timeStamp.get(blockedProcess.getNumber()).add(tracker);
							blockedProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
				}
				//Check to see if we need to get a burst time
				if(workingProcess != null) {
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							burstTime = workingProcess.getEventTime().get(0);
							workingProcess.removeEventType();
							workingProcess.removeEventTime();
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							processBlock.add(workingProcess);
							workingProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
				}
				if(burstTime > 0 && blockTime > 0) {
					break;
				}
				else if (burstTime > 0 && processBlock.size() == 0) {
					break;
				}
				else if (blockTime > 0 && processQueue.size() == 0) {
					break;
				}
				else if (processComp == pr.size()) {
					break;
				}
			}
			
			//######################################################
			//Debug
			System.out.println("tracker: " + tracker);
			if (workingProcess != null) {
				System.out.println("Working process: " + workingProcess.getNumber());
				System.out.println("           burst time: " + burstTime);
			}
			
			if (blockedProcess != null) {
				System.out.println("blocked process: " + blockedProcess.getNumber());
				System.out.println("           block time: " + blockTime);
			}
			//######################################################
			
			//This is where the time starts to be calculated
			if(burstTime > 0 && blockTime == 0) {
				if(addedPrc != pr.size()) {
					//This will get the next process in the process arraylist
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
					}
					else if (burstTime <= nextArive) {
						tracker += burstTime;
						burstTime -= burstTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += burstTime;
					burstTime -= burstTime;
				}
				else {
					System.out.println("Error occured in burst time calculations");
				}
			}
			else if(blockTime > 0 && burstTime == 0) {
				if(addedPrc != pr.size()) {
					//This will find if there is a process that needs to arrive 
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(blockTime > nextArive) {
						tracker += nextArive;
						blockTime -= nextArive;
					}
					else if (blockTime <= nextArive) {
						tracker += blockTime;
						blockTime -= blockTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += blockTime;
					blockTime -= blockTime;
				}
				else {
					System.out.println("Error occured in block time calculations");
				}
			}
			else if (blockTime > 0 && burstTime > 0) {
				if(addedPrc != pr.size()) {
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime <= nextArive && blockTime <= nextArive) {
						if(burstTime <= blockTime) {
							tracker += burstTime;
							blockTime -=  burstTime;
							burstTime -= burstTime;
						}
						else if(burstTime > blockTime) {
							tracker += blockTime;
							burstTime -= blockTime;
							blockTime -= blockTime;
						}
					}
					else if (burstTime > nextArive && blockTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
						blockTime -= nextArive;
					}
				}
				else if (addedPrc == pr.size()) {
					if(burstTime <= blockTime) {
						tracker += burstTime;
						blockTime -=  burstTime;
						burstTime -= burstTime;
					}
					else if(burstTime > blockTime) {
						tracker += blockTime;
						burstTime -= blockTime;
						blockTime -= blockTime;
					}
				}
				else {
					System.out.println("Error occured in block and burst time calculations");
				}
			}
		}
		//Get the start times
		for(int i = 0; i < pr.size(); i++) {
			//The first time a process is worked with is always at the 2nd timestamp because that's the first time it got pulled out
			startTime[i] = timeStamp.get(i).get(1);
		}
		
		//Print the processes here and call stat functions
		turnTime = turnTime(startTime, finishTime);
		normTime = normTurnTime(turnTime, serviceTime);
		avgResTime = avgRespTime(timeStamp);
		
		for(int i = 0; i < pr.size(); i++) {
			printProcessStats(pr.get(i), serviceTime[i], startTime[i], finishTime[i], 
					turnTime[i], normTime[i], avgResTime[i]);
		}
		
		//Calc the mean of the 3 functions
		float meanTurnTime = 0;
		float meanNormTime = 0;
		float meanAvgResTime = 0;
		meanTurnTime = meanCalc(pr, turnTime);
		meanNormTime = meanCalc(pr, normTime);
		meanAvgResTime = meanCalc(pr, avgResTime);
		//print mean times
		printMeanStats(meanTurnTime, meanNormTime, meanAvgResTime);
	}
	
	public static void priority(ArrayList<Process> pr, int max) {
		int tracker = 0; //time tracker
		int burstTime = 0;
		int blockTime = 0;
		int processComp = 0;
		int addedPrc = 0;
		
		String event ="";
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		ArrayList<Process> processBlock = new ArrayList<Process>();
		Process workingProcess = null;
		Process blockedProcess = null;
		
		//Used to calculate the stats
		ArrayList<Integer> arrivalTime = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> timeStamp = new ArrayList<ArrayList<Integer>>();
		int [] finishTime = new int [pr.size()];
		int [] serviceTime = new int [pr.size()];
		int [] startTime = new int [pr.size()];
		float [] turnTime;
		float [] normTime;
		float [] avgResTime;
		//find the arrival time for each process
		for(int i = 0; i< pr.size(); i++) {
			arrivalTime.add(pr.get(i).getArrivalTime());
		}
		
		//find the sum of the time spent in the cpu
		for (int i = 0; i < pr.size(); i++) {
			//Same case for these, I just need a forloop the size of the process arraylist
			ArrayList<Integer> temp = new ArrayList<Integer>();
	    	timeStamp.add(temp);
			//add up the sum
			for (int j = 0; j < pr.get(i).getEventTime().size(); j++) {
				if(pr.get(i).getEventTypes().get(j).equals("CPU")) {
					serviceTime[i] += pr.get(i).getEventTime().get(j);
				}
			}
		}
		
		while(processComp < pr.size()) {
			//Adding processes to ready queue
			for(int i = 0; i < pr.size(); i++) {
				if(pr.get(i).getArrivalTime() == tracker) {
					//check to see if the priorty is higher than the max priority number
					//If so, then set to lowest priority
					if(pr.get(i).getPriority() > max) {
						pr.get(i).setPriority(0);
					}
					//This is making sure we base the placement inside the queue on the priority 
					if(workingProcess != null) {
						if(pr.get(i).getPriority() < workingProcess.getPriority()) {
							if (burstTime > 0 ) {
								workingProcess.addEventType("CPU");
								workingProcess.addEventTime(burstTime);
								if(processQueue.size() > 0) {
									for (int j = 0; j < processQueue.size(); j++) {
										if(processQueue.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size() -1){
											//Do nothing
											//highest priority is in that place and we can still evaluate next process priority
										}
										else if(workingProcess.getPriority() < processQueue.get(j).getPriority()) {
											//Might not work
											processQueue.add(j, workingProcess);
											
											break;
										}
										else {
											processQueue.add(workingProcess);
											break;
										}
									}
								}
								else {
									processQueue.add(workingProcess);
								}
								timeStamp.get(workingProcess.getNumber()).add(tracker);
								workingProcess = null;
								burstTime = 0;
							}
						}
					}
					if (processQueue.size() > 0) {
						for (int j = 0; j < processQueue.size(); j++) {
							if(processQueue.get(j).getPriority() < pr.get(i).getPriority() && j != processQueue.size() -1){
								//Do nothing
								//highest priority is in that place and we can still evaluate next process priority
							}
							else if(pr.get(i).getPriority() < processQueue.get(j).getPriority()) {
								processQueue.add(j, pr.get(i));
								//First time a process is added
								timeStamp.get(pr.get(i).getNumber()).add(tracker);
								addedPrc++;
								break;
							}
							else {
								processQueue.add(pr.get(i));
								//First time a process is added
								timeStamp.get(pr.get(i).getNumber()).add(tracker);
								addedPrc++;
								break;
							}
						}
					}
					else {
						processQueue.add(pr.get(i));
						//First time a process is added
						timeStamp.get(pr.get(i).getNumber()).add(tracker);
						addedPrc++;
					}
				}
			}
			
			//This is a test/////////////////////
			while(true) {
				//Check if there is a process ready in the process queue
				if (workingProcess == null && processQueue.size() != 0) {
					//This is to see if it's the first time it's worked on
					workingProcess = processQueue.get(0);
					//This is when the process is pulled out
					timeStamp.get(workingProcess.getNumber()).add(tracker);
					processQueue.remove(0);
				}
				
				//Check if there is a blocked process in the queue and we are not using one
				if(blockedProcess == null && processBlock.size() != 0) {
					blockedProcess = processBlock.get(0);
					processBlock.remove(0);
				}
				
				if(blockedProcess != null) {
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							blockTime = blockedProcess.getEventTime().get(0);
							blockedProcess.removeEventType();
							blockedProcess.removeEventTime();
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							//This is making sure we base the placement inside the queue on the priority 
							if (processQueue.size() > 0) {
								for (int j = 0; j < processQueue.size(); j++) {
									if(processQueue.get(j).getPriority() < blockedProcess.getPriority() && j != processQueue.size()-1){
										//Do nothing
										//highest priority is in that place and we can still evaluate next process priority
									}
									else if (processQueue.get(j).getPriority() < blockedProcess.getPriority() && j == processQueue.size()-1) {
										//We are at the end and still don't have higher priority. So add to the end
										processQueue.add(blockedProcess);
										timeStamp.get(blockedProcess.getNumber()).add(tracker);
										blockedProcess = null;
										break;
									}
									else if(blockedProcess.getPriority() < processQueue.get(j).getPriority()) {
										processQueue.add(j, blockedProcess);
										timeStamp.get(blockedProcess.getNumber()).add(tracker);
										blockedProcess = null;
										break;
									}
								}
							}
							else {
								processQueue.add(blockedProcess);
								blockedProcess = null;
							}
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
				}
				//Check to see if we need to get a burst time
				if(workingProcess != null) {
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							burstTime = workingProcess.getEventTime().get(0);
							workingProcess.removeEventType();
							workingProcess.removeEventTime();
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							//This is making sure we base the placement inside the queue on the priority 
							if (processBlock.size() > 0) {
								for (int j = 0; j < processBlock.size(); j++) {
									if(processBlock.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size()){
										//Do nothing
										//highest priority is in that place and we can still evaluate next process priority
									}
									else if(workingProcess.getPriority() < processBlock.get(j).getPriority()) {
										processBlock.add(j, workingProcess);
										timeStamp.get(workingProcess.getNumber()).add(tracker);
										workingProcess = null;
										break;
									}
								}
							}
							else {
								processBlock.add(workingProcess);
								workingProcess = null;
							}
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
					else if(burstTime > 0 && processQueue.size() > 0) {
						//This section is needed to check to see if there is a process with shorter time in the process queue 
						//There can be a process that is running with a longer time but this will stop it if a shorter time comes
						if (workingProcess.getPriority() < processQueue.get(0).getPriority()) {
							//We have the lowest priority
						}
						else if(workingProcess.getPriority() > processQueue.get(0).getPriority()) {
							workingProcess.addEventType("CPU");
							workingProcess.addEventTime(burstTime);
							for (int j = 0; j < processQueue.size(); j++) {
								if(processQueue.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size() -1){
									//Do nothing
									//highest priority is in that place and we can still evaluate next process priority
								}
								else if(workingProcess.getPriority() < processQueue.get(j).getPriority()) {
									//Might not work
									processQueue.add(j, workingProcess);	
									break;
								}
								else {
									processQueue.add(workingProcess);
									break;
								}
							}
							timeStamp.get(workingProcess.getNumber()).add(tracker);
							workingProcess = null;
							burstTime = 0;
						}
					}
				}
				if(burstTime > 0 && blockTime > 0) {
					break;
				}
				else if (burstTime > 0 && processBlock.size() == 0) {
					break;
				}
				else if (blockTime > 0 && processQueue.size() == 0) {
					break;
				}
				else if (processComp == pr.size()) {
					break;
				}
			}
			//######################################################
			//Debug
			System.out.println("tracker: " + tracker);
			if (workingProcess != null) {
				System.out.println("Working process: " + workingProcess.getNumber());
				System.out.println("           burst time: " + burstTime);
				System.out.println("           Process queue size: " + processQueue.size());
				System.out.println("           block queue size: " + processBlock.size());
			}
			
			if (blockedProcess != null) {
				System.out.println("blocked process: " + blockedProcess.getNumber());
				System.out.println("           block time: " + blockTime);
				System.out.println("           Process queue size: " + processQueue.size());
				System.out.println("           block queue size: " + processBlock.size());
			}
			//######################################################
			
			//This is where the time starts to be calculated
			if(burstTime > 0 && blockTime == 0) {
				if(addedPrc != pr.size()) {
					//This will get the next process in the process arraylist
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
					}
					else if (burstTime <= nextArive) {
						tracker += burstTime;
						burstTime -= burstTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += burstTime;
					burstTime -= burstTime;
				}
				else {
					System.out.println("Error occured in burst time calculations");
				}
			}
			else if(blockTime > 0 && burstTime == 0) {
				if(addedPrc != pr.size()) {
					//This will find if there is a process that needs to arrive 
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(blockTime > nextArive) {
						tracker += nextArive;
						blockTime -= nextArive;
					}
					else if (blockTime <= nextArive) {
						tracker += blockTime;
						blockTime -= blockTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += blockTime;
					blockTime -= blockTime;
				}
				else {
					System.out.println("Error occured in block time calculations");
				}
			}
			else if (blockTime > 0 && burstTime > 0) {
				if(addedPrc != pr.size()) {
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime <= nextArive && blockTime <= nextArive) {
						if(burstTime <= blockTime) {
							tracker += burstTime;
							blockTime -=  burstTime;
							burstTime -= burstTime;
						}
						else if(burstTime > blockTime) {
							tracker += blockTime;
							burstTime -= blockTime;
							blockTime -= blockTime;
						}
					}
					else if (burstTime > nextArive && blockTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
						blockTime -= nextArive;
					}
				}
				else if (addedPrc == pr.size()) {
					if(burstTime <= blockTime) {
						tracker += burstTime;
						blockTime -=  burstTime;
						burstTime -= burstTime;
					}
					else if(burstTime > blockTime) {
						tracker += blockTime;
						burstTime -= blockTime;
						blockTime -= blockTime;
					}
				}
				else {
					System.out.println("Error occured in block and burst time calculations");
				}
			}
		}
		//Get the start times
		for(int i = 0; i < pr.size(); i++) {
			//The first time a process is worked with is always at the 2nd timestamp because that's the first time it got pulled out
			startTime[i] = timeStamp.get(i).get(1);
		}
		
		//Print the processes here and call stat functions
		turnTime = turnTime(startTime, finishTime);
		normTime = normTurnTime(turnTime, serviceTime);
		avgResTime = avgRespTime(timeStamp);
		
		for(int i = 0; i < pr.size(); i++) {
			printProcessStats(pr.get(i), serviceTime[i], startTime[i], finishTime[i], 
					turnTime[i], normTime[i], avgResTime[i]);
		}
		
		//Calc the mean of the 3 functions
		float meanTurnTime = 0;
		float meanNormTime = 0;
		float meanAvgResTime = 0;
		meanTurnTime = meanCalc(pr, turnTime);
		meanNormTime = meanCalc(pr, normTime);
		meanAvgResTime = meanCalc(pr, avgResTime);
		//print mean times
		printMeanStats(meanTurnTime, meanNormTime, meanAvgResTime);
	}
	
	public static void roundRobin(ArrayList<Process> pr, int quantum) {
		int tracker = 0; //time tracker
		int burstTime = 0;
		int blockTime = 0;
		int processComp = 0;
		int addedPrc = 0;
		int quant = quantum;
		
		String event ="";
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		ArrayList<Process> processBlock = new ArrayList<Process>();
		Process workingProcess = null;
		Process blockedProcess = null;
		
		//Used to calculate the stats
		ArrayList<Integer> arrivalTime = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> timeStamp = new ArrayList<ArrayList<Integer>>();
		int [] finishTime = new int [pr.size()];
		int [] serviceTime = new int [pr.size()];
		int [] startTime = new int [pr.size()];
		float [] turnTime;
		float [] normTime;
		float [] avgResTime;
		//find the arrival time for each process
		for(int i = 0; i< pr.size(); i++) {
			arrivalTime.add(pr.get(i).getArrivalTime());
		}
		
		//find the sum of the time spent in the cpu
		for (int i = 0; i < pr.size(); i++) {
			//Same case for these, I just need a forloop the size of the process arraylist
			ArrayList<Integer> temp = new ArrayList<Integer>();
	    	timeStamp.add(temp);
	    	startTime[i] = -1; // Because it's RR we can't have the values be 0
			//add up the sum
			for (int j = 0; j < pr.get(i).getEventTime().size(); j++) {
				if(pr.get(i).getEventTypes().get(j).equals("CPU")) {
					serviceTime[i] += pr.get(i).getEventTime().get(j);
				}
			}
		}
		
		while(processComp < pr.size()) {
			//Make sure if quantum is equal to 0. Initialize back to quantum time
			if (quant == 0) {
				System.out.println("Check");
				//If it is equal to 0, put any working and blocked processes to the end of their respective queue
				if(workingProcess != null) {
					//Need to add the remaining time if there is any
					//Case for if there is no remaining time
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							processQueue.add(workingProcess);
							timeStamp.get(workingProcess.getNumber()).add(tracker);
							workingProcess = null;
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							processBlock.add(workingProcess);
							workingProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type in quantum time end");
						}
					}
					//Case for if there is no remaining time and the process should be done
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
					//Case for if there is remaining time
					//Need to readd event and time to the front
					//Then put process to the end of its queue
					else if (burstTime > 0 ) {
						workingProcess.addEventType("CPU");
						workingProcess.addEventTime(burstTime);
						processQueue.add(workingProcess);
						//////////////////////////////////
						//////////////////////////////////
						timeStamp.get(workingProcess.getNumber()).add(tracker); //This might not be needed
						//////////////////////////////////
						//////////////////////////////////
						burstTime = 0;
						workingProcess = null;
					}
				}
				
				if (blockedProcess != null) {
					//Need to add the remaining time if there is any
					//Case for if there is no remaining time
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							processBlock.add(blockedProcess);
							blockedProcess = null;
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							processQueue.add(blockedProcess);
							timeStamp.get(blockedProcess.getNumber()).add(tracker);
							blockedProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type in quantum time end");
						}
					}
					//Case for if there is no remaining time and the process should complete
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
					//Case for if there is remaining time
					//Need to readd event and time to the front
					//Then put process to the end of its queue
					else if (blockTime > 0 ) {
						blockedProcess.addEventType("IO");
						blockedProcess.addEventTime(burstTime);
						processBlock.add(blockedProcess);
						blockTime = 0;
						blockedProcess = null;
					}
				}
				//Reset quantum time
				quant = quantum;
			}
			//Adding processes to ready queue
			for(int i = 0; i < pr.size(); i++) {
				if(pr.get(i).getArrivalTime() == tracker) {
					processQueue.add(pr.get(i));
					//First time a process is added
					timeStamp.get(pr.get(i).getNumber()).add(tracker);
					addedPrc++;
				}
			}
			
			while (true) {
				//Check if there is a process ready in the process queue
				if (workingProcess == null && processQueue.size() != 0) {
					//This is to see if it's the first time it's worked on
					workingProcess = processQueue.get(0);
					//This is when the process is pulled out
					timeStamp.get(workingProcess.getNumber()).add(tracker);
					processQueue.remove(0);
				}
				
				//Check if there is a blocked process in the queue and we are not using one
				if(blockedProcess == null && processBlock.size() != 0) {
					blockedProcess = processBlock.get(0);
					processBlock.remove(0);
				}
				
				//Check to see if we need to get a block time
				if(blockedProcess != null) {
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							blockTime = blockedProcess.getEventTime().get(0);
							blockedProcess.removeEventType();
							blockedProcess.removeEventTime();
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							processQueue.add(blockedProcess);
							timeStamp.get(blockedProcess.getNumber()).add(tracker);
							blockedProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
				}
				//Check to see if we need to get a burst time
				if(workingProcess != null) {
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							burstTime = workingProcess.getEventTime().get(0);
							workingProcess.removeEventType();
							workingProcess.removeEventTime();
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							processBlock.add(workingProcess);
							workingProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
				}
				if(burstTime > 0 && blockTime > 0) {
					break;
				}
				else if (burstTime > 0 && processBlock.size() == 0) {
					break;
				}
				else if (blockTime > 0 && processQueue.size() == 0) {
					break;
				}
				else if (processComp == pr.size()) {
					break;
				}
			}
			
			//######################################################
			//Debug
			System.out.println("tracker: " + tracker);
			System.out.println("quantum: " + quant);
			if (workingProcess != null) {
				System.out.println("Working process: " + workingProcess.getNumber());
				System.out.println("           burst time: " + burstTime);
			}
			
			if (blockedProcess != null) {
				System.out.println("blocked process: " + blockedProcess.getNumber());
				System.out.println("           block time: " + blockTime);
			}
			//######################################################
			
			//This is where the time starts to be calculated
			if(burstTime > 0 && blockTime == 0) {
				if(addedPrc != pr.size()) {
					//This will get the next process in the process arraylist
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime > nextArive) {
						if(quant > nextArive) {
							tracker += nextArive;
							burstTime -= nextArive;
							quant -= nextArive;
						}
						else if (quant <= nextArive) {
							tracker += quant;
							burstTime -= quant;
							quant -= quant;
						}
					}
					else if (burstTime <= nextArive) {
						if(quant <= burstTime) {
							tracker += quant;
							burstTime -= quant;
							quant -= quant;
						}
						else if (quant > burstTime) {
							tracker += burstTime;
							quant -= burstTime;
							burstTime -= burstTime;
						}
					}
				}
				else if (addedPrc == pr.size()){
					if (burstTime > quant) {
						tracker += quant;
						burstTime -= quant;
						quant -= quant;
					}
					else if (burstTime <= quant){
						tracker += burstTime;
						quant -= burstTime;
						burstTime -= burstTime;
					}
				}
				else {
					System.out.println("Error occured in burst time calculations");
				}
			}
			else if(blockTime > 0 && burstTime == 0) {
				if(addedPrc != pr.size()) {
					//This will find if there is a process that needs to arrive 
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(blockTime > nextArive) {
						if(quant > nextArive) {
							tracker += nextArive;
							blockTime -= nextArive;
							quant -= nextArive;
						}
						else if (quant <= nextArive) {
							tracker += quant;
							blockTime -= quant;
							quant -= quant;
						}
					}
					else if (blockTime <= nextArive) {
						if(quant <= blockTime) {
							tracker += quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > blockTime) {
							tracker += blockTime;
							quant -= blockTime;
							blockTime -= blockTime;
						}
					}
				}
				else if (addedPrc == pr.size()){
					if (blockTime > quant) {
						tracker += quant;
						blockTime -= quant;
						quant -= quant;
					}
					else if (blockTime <= quant){
						tracker += blockTime;
						quant -= blockTime;
						blockTime -= blockTime;
					}
				}
				else {
					System.out.println("Error occured in block time calculations");
				}
			}
			else if (blockTime > 0 && burstTime > 0) {
				if(addedPrc != pr.size()) {
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime <= nextArive && blockTime <= nextArive) {
						if(burstTime <= blockTime) {
							if (quant <= burstTime) {
								tracker += quant;
								blockTime -= quant;
								burstTime -= quant;
								quant -= quant;
							}
							else if (quant > burstTime) {
								tracker += burstTime;
								quant -= burstTime;
								blockTime -=  burstTime;
								burstTime -= burstTime;
							}
						}
						else if (burstTime > blockTime) {
							if (quant <= blockTime) {
								tracker += quant;
								burstTime -= quant;
								blockTime -= quant;
								quant -= quant;
							}
							else if (quant > blockTime) {
								tracker += blockTime;
								quant -= blockTime;
								burstTime -= blockTime;
								blockTime -= blockTime;
							}
						}
					}
					else if (burstTime > nextArive && blockTime > nextArive) {
						if(quant <= nextArive) {
							tracker += quant;
							burstTime -= quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > nextArive) {
							tracker += nextArive;
							burstTime -= nextArive;
							blockTime -= nextArive;
							quant -= nextArive;
						}
					}
				}
				else if (addedPrc == pr.size()) {
					if(burstTime <= blockTime) {
						if(quant <= burstTime) {
							tracker += quant;
							blockTime -= quant;
							burstTime -= quant;
							quant -= quant;
						}
						else if (quant > burstTime) {
							tracker += burstTime;
							quant -= burstTime;
							blockTime -=  burstTime;
							burstTime -= burstTime;
						}
					}
					else if(burstTime > blockTime) {
						if(quant <= blockTime) {
							tracker += quant;
							burstTime -= quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > blockTime) {
							tracker += blockTime;
							quant -= blockTime;
							burstTime -= blockTime;
							blockTime -= blockTime;
						}
					}
				}
				else {
					System.out.println("Error occured in block and burst time calculations");
				}
			}
		}
		//Get the start times
		for(int i = 0; i < pr.size(); i++) {
			//The first time a process is worked with is always at the 2nd timestamp because that's the first time it got pulled out
			startTime[i] = timeStamp.get(i).get(1);
		}
		
		//Print the processes here and call stat functions
		turnTime = turnTime(startTime, finishTime);
		normTime = normTurnTime(turnTime, serviceTime);
		avgResTime = avgRespTime(timeStamp);
		
		for(int i = 0; i < pr.size(); i++) {
			printProcessStats(pr.get(i), serviceTime[i], startTime[i], finishTime[i], 
					turnTime[i], normTime[i], avgResTime[i]);
		}
		
		//Calc the mean of the 3 functions
		float meanTurnTime = 0;
		float meanNormTime = 0;
		float meanAvgResTime = 0;
		meanTurnTime = meanCalc(pr, turnTime);
		meanNormTime = meanCalc(pr, normTime);
		meanAvgResTime = meanCalc(pr, avgResTime);
		//print mean times
		printMeanStats(meanTurnTime, meanNormTime, meanAvgResTime);
	}
	
	//I do not know how to do if there is not a given service. This only works for if servic_given is true
	public static void shortestRemaining(ArrayList<Process> pr) {
		int tracker = 0; //time tracker
		int burstTime = 0;
		int blockTime = 0;
		int processComp = 0;
		int addedPrc = 0;
		
		String event ="";
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		ArrayList<Process> processBlock = new ArrayList<Process>();
		Process workingProcess = null;
		Process blockedProcess = null;
		
		//Used to calculate the stats
		ArrayList<Integer> arrivalTime = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> timeStamp = new ArrayList<ArrayList<Integer>>();
		int [] finishTime = new int [pr.size()];
		int [] serviceTime = new int [pr.size()];
		int [] cst = new int [pr.size()]; //Copy of service time
		int [] startTime = new int [pr.size()];
		float [] turnTime;
		float [] normTime;
		float [] avgResTime;
		//find the arrival time for each process
		for(int i = 0; i< pr.size(); i++) {
			arrivalTime.add(pr.get(i).getArrivalTime());
		}
		
		//find the sum of the time spent in the cpu
		for (int i = 0; i < pr.size(); i++) {
			//Same case for these, I just need a forloop the size of the process arraylist
			ArrayList<Integer> temp = new ArrayList<Integer>();
	    	timeStamp.add(temp);
			//add up the sum
			for (int j = 0; j < pr.get(i).getEventTime().size(); j++) {
				if(pr.get(i).getEventTypes().get(j).equals("CPU")) {
					serviceTime[i] += pr.get(i).getEventTime().get(j);
				}
			}
			//Copy service time
			cst[i] = serviceTime [i];
		}
		
		while(processComp < pr.size()) {
			//Adding processes to ready queue
			for(int i = 0; i < pr.size(); i++) {
				if(pr.get(i).getArrivalTime() == tracker) {
					//check to see if the priorty is higher than the max priority number
					//If so, then set to lowest priority
					//This is making sure we base the placement inside the queue on the priority 
					if(workingProcess != null) {
						if(cst[i] < cst[workingProcess.getNumber()]) {
							if (burstTime > 0 ) {
								workingProcess.addEventType("CPU");
								workingProcess.addEventTime(burstTime);
								if(processQueue.size() > 0) {
									for (int j = 0; j < processQueue.size(); j++) {
										if(cst[processQueue.get(j).getNumber()] < cst[workingProcess.getNumber()] && j != processQueue.size() -1){
											//Do nothing
											//highest priority is in that place and we can still evaluate next process priority
										}
										else if(cst[workingProcess.getNumber()] < cst[processQueue.get(j).getNumber()]) {
											//Might not work
											processQueue.add(j, workingProcess);
											
											break;
										}
										else {
											processQueue.add(workingProcess);
											break;
										}
									}
								}
								else {
									processQueue.add(workingProcess);
								}
								timeStamp.get(workingProcess.getNumber()).add(tracker);
								workingProcess = null;
								burstTime = 0;
							}
						}
					}
					if (processQueue.size() > 0) {
						for (int j = 0; j < processQueue.size(); j++) {
							if(cst[processQueue.get(j).getNumber()] < cst[pr.get(i).getNumber()] && j != processQueue.size() -1){
								//Do nothing
								//highest priority is in that place and we can still evaluate next process priority
							}
							else if(cst[pr.get(i).getNumber()] < cst[processQueue.get(j).getNumber()]) {
								processQueue.add(j, pr.get(i));
								//First time a process is added
								timeStamp.get(pr.get(i).getNumber()).add(tracker);
								addedPrc++;
								break;
							}
							else {
								processQueue.add(pr.get(i));
								//First time a process is added
								timeStamp.get(pr.get(i).getNumber()).add(tracker);
								addedPrc++;
								break;
							}
						}
					}
					else {
						processQueue.add(pr.get(i));
						//First time a process is added
						timeStamp.get(pr.get(i).getNumber()).add(tracker);
						addedPrc++;
					}
				}
			}
			
			//This is a test/////////////////////
			while(true) {
				//Check if there is a process ready in the process queue
				if (workingProcess == null && processQueue.size() != 0) {
					//This is to see if it's the first time it's worked on
					workingProcess = processQueue.get(0);
					//This is when the process is pulled out
					timeStamp.get(workingProcess.getNumber()).add(tracker);
					processQueue.remove(0);
				}
				
				//Check if there is a blocked process in the queue and we are not using one
				if(blockedProcess == null && processBlock.size() != 0) {
					blockedProcess = processBlock.get(0);
					processBlock.remove(0);
				}
				
				if(blockedProcess != null) {
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							blockTime = blockedProcess.getEventTime().get(0);
							blockedProcess.removeEventType();
							blockedProcess.removeEventTime();
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							//This is making sure we base the placement inside the queue on the priority 
							if (processQueue.size() > 0) {
								for (int j = 0; j < processQueue.size(); j++) {
									if(cst[processQueue.get(j).getNumber()] < cst[blockedProcess.getNumber()] && j != processQueue.size()-1){
										//Do nothing
										//highest priority is in that place and we can still evaluate next process priority
									}
									else if(cst[blockedProcess.getNumber()] < cst[processQueue.get(j).getNumber()]) {
										processQueue.add(j, blockedProcess);
										timeStamp.get(blockedProcess.getNumber()).add(tracker);
										blockedProcess = null;
										break;
									}
								}
							}
							else {
								processQueue.add(blockedProcess);
								blockedProcess = null;
							}
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
				}
				//Check to see if we need to get a burst time
				if(workingProcess != null) {
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							burstTime = workingProcess.getEventTime().get(0);
							workingProcess.removeEventType();
							workingProcess.removeEventTime();
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							//This is making sure we base the placement inside the queue on the priority 
							if (processBlock.size() > 0) {
								for (int j = 0; j < processBlock.size(); j++) {
									if(processBlock.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size()){
										//Do nothing
										//highest priority is in that place and we can still evaluate next process priority
									}
									else if(workingProcess.getPriority() < processBlock.get(j).getPriority()) {
										processBlock.add(j, workingProcess);
										timeStamp.get(workingProcess.getNumber()).add(tracker);
										workingProcess = null;
										break;
									}
								}
							}
							else {
								processBlock.add(workingProcess);
								workingProcess = null;
							}
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
					else if(burstTime > 0 && processQueue.size() > 0) {
						//This section is needed to check to see if there is a process with shorter time in the process queue 
						//There can be a process that is running with a longer time but this will stop it if a shorter time comes
						if (cst[workingProcess.getNumber()] < cst[processQueue.get(0).getNumber()]) {
							//We have the shortest cpu time already working
						}
						else if(cst[workingProcess.getNumber()] > cst[processQueue.get(0).getNumber()]) {
							workingProcess.addEventType("CPU");
							workingProcess.addEventTime(burstTime);
							for (int j = 0; j < processQueue.size(); j++) {
								if(cst[processQueue.get(j).getNumber()] < cst[workingProcess.getNumber()] && j != processQueue.size() -1){
									//Do nothing
									//highest priority is in that place and we can still evaluate next process priority
								}
								else if(cst[workingProcess.getNumber()] < cst[processQueue.get(j).getNumber()]) {
									//Might not work
									processQueue.add(j, workingProcess);	
									break;
								}
								else {
									processQueue.add(workingProcess);
									break;
								}
							}
							timeStamp.get(workingProcess.getNumber()).add(tracker);
							workingProcess = null;
							burstTime = 0;
						}
					}
				}
				if(burstTime > 0 && blockTime > 0) {
					break;
				}
				else if (burstTime > 0 && processBlock.size() == 0) {
					break;
				}
				else if (blockTime > 0 && processQueue.size() == 0) {
					break;
				}
				else if (processComp == pr.size()) {
					break;
				}
			}
			//######################################################
			//Debug
			//if (tracker == 174) {
				//System.out.println("HERE");
			//}
			System.out.println("tracker: " + tracker);
			if (workingProcess != null) {
				System.out.println("Working process: " + workingProcess.getNumber());
				System.out.println("           burst time: " + burstTime);
				System.out.println("           Process queue size: " + processQueue.size());
				System.out.println("           block queue size: " + processBlock.size());
			}
			
			if (blockedProcess != null) {
				System.out.println("blocked process: " + blockedProcess.getNumber());
				System.out.println("           block time: " + blockTime);
				System.out.println("           Process queue size: " + processQueue.size());
				System.out.println("           block queue size: " + processBlock.size());
			}
			//######################################################
			
			//This is where the time starts to be calculated
			if(burstTime > 0 && blockTime == 0) {
				if(addedPrc != pr.size()) {
					//This will get the next process in the process arraylist
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
						//Need to decrease the burstTime from the copied service time
						cst[workingProcess.getNumber()] -= nextArive;
					}
					else if (burstTime <= nextArive) {
						tracker += burstTime;
						//Need to decrease the burstTime from the copied service time
						cst[workingProcess.getNumber()] -= burstTime;
						burstTime -= burstTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += burstTime;
					//Need to decrease the burstTime from the copied service time
					cst[workingProcess.getNumber()] -= burstTime;
					burstTime -= burstTime;
				}
				else {
					System.out.println("Error occured in burst time calculations");
				}
			}
			else if(blockTime > 0 && burstTime == 0) {
				if(addedPrc != pr.size()) {
					//This will find if there is a process that needs to arrive 
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(blockTime > nextArive) {
						tracker += nextArive;
						blockTime -= nextArive;
					}
					else if (blockTime <= nextArive) {
						tracker += blockTime;
						blockTime -= blockTime;
					}
				}
				else if (addedPrc == pr.size()){
					tracker += blockTime;
					blockTime -= blockTime;
				}
				else {
					System.out.println("Error occured in block time calculations");
				}
			}
			else if (blockTime > 0 && burstTime > 0) {
				if(addedPrc != pr.size()) {
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime <= nextArive && blockTime <= nextArive) {
						if(burstTime <= blockTime) {
							tracker += burstTime;
							blockTime -=  burstTime;
							//Need to decrease the burstTime from the copied service time
							cst[workingProcess.getNumber()] -= burstTime;
							burstTime -= burstTime;
						}
						else if(burstTime > blockTime) {
							tracker += blockTime;
							burstTime -= blockTime;
							//Need to decrease the burstTime from the copied service time
							cst[workingProcess.getNumber()] -= blockTime;
							blockTime -= blockTime;
						}
					}
					else if (burstTime > nextArive && blockTime > nextArive) {
						tracker += nextArive;
						burstTime -= nextArive;
						//Need to decrease the burstTime from the copied service time
						cst[workingProcess.getNumber()] -= nextArive;
						blockTime -= nextArive;
					}
				}
				else if (addedPrc == pr.size()) {
					if(burstTime <= blockTime) {
						tracker += burstTime;
						blockTime -=  burstTime;
						//Need to decrease the burstTime from the copied service time
						cst[workingProcess.getNumber()] -= burstTime;
						burstTime -= burstTime;
					}
					else if(burstTime > blockTime) {
						tracker += blockTime;
						burstTime -= blockTime;
						//Need to decrease the burstTime from the copied service time
						cst[workingProcess.getNumber()] -= blockTime;
						blockTime -= blockTime;
					}
				}
				else {
					System.out.println("Error occured in block and burst time calculations");
				}
			}
		}
		//Get the start times
		for(int i = 0; i < pr.size(); i++) {
			//The first time a process is worked with is always at the 2nd timestamp because that's the first time it got pulled out
			startTime[i] = timeStamp.get(i).get(1);
		}
		//Print the processes here and call stat functions
		turnTime = turnTime(startTime, finishTime);
		normTime = normTurnTime(turnTime, serviceTime);
		avgResTime = avgRespTime(timeStamp);
		
		for(int i = 0; i < pr.size(); i++) {
			printProcessStats(pr.get(i), serviceTime[i], startTime[i], finishTime[i], 
					turnTime[i], normTime[i], avgResTime[i]);
		}
		
		//Calc the mean of the 3 functions
		float meanTurnTime = 0;
		float meanNormTime = 0;
		float meanAvgResTime = 0;
		meanTurnTime = meanCalc(pr, turnTime);
		meanNormTime = meanCalc(pr, normTime);
		meanAvgResTime = meanCalc(pr, avgResTime);
		//print mean times
		printMeanStats(meanTurnTime, meanNormTime, meanAvgResTime);
	}
	
	public static void feedback (ArrayList<Process> pr, int quantum, int max) {
		int tracker = 0; //time tracker
		int burstTime = 0;
		int blockTime = 0;
		int processComp = 0;
		int addedPrc = 0;
		int quant = quantum;
		
		String event ="";
		
		ArrayList<Process> processQueue = new ArrayList<Process>();
		ArrayList<Process> processBlock = new ArrayList<Process>();
		Process workingProcess = null;
		Process blockedProcess = null;
		
		//Used to calculate the stats
		ArrayList<Integer> arrivalTime = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> timeStamp = new ArrayList<ArrayList<Integer>>();
		int [] finishTime = new int [pr.size()];
		int [] serviceTime = new int [pr.size()];
		int [] startTime = new int [pr.size()];
		float [] turnTime;
		float [] normTime;
		float [] avgResTime;
		//find the arrival time for each process
		for(int i = 0; i< pr.size(); i++) {
			arrivalTime.add(pr.get(i).getArrivalTime());
		}
		
		//find the sum of the time spent in the cpu
		for (int i = 0; i < pr.size(); i++) {
			//Same case for these, I just need a forloop the size of the process arraylist
			ArrayList<Integer> temp = new ArrayList<Integer>();
	    	timeStamp.add(temp);
			//add up the sum
			for (int j = 0; j < pr.get(i).getEventTime().size(); j++) {
				if(pr.get(i).getEventTypes().get(j).equals("CPU")) {
					serviceTime[i] += pr.get(i).getEventTime().get(j);
				}
			}
		}
		
		while(processComp < pr.size()) {
			//Make sure if quantum is equal to 0. Initialize back to quantum time
			if (quant == 0) {
				//If it is equal to 0, put any working and blocked processes to the end of their respective queue
				if(workingProcess != null) {
					//Need to add the remaining time if there is any
					//Case for if there is no remaining time
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							//This will be used  for when add a process into the cpu queue
							//Won't change priorty unless it's being added to the ready qeueue again
							//Need to change the priority by 1
							if(workingProcess.getPriority() < max) {
								workingProcess.setPriority(workingProcess.getPriority()+1);
								//add process to queue
								if(processQueue.size() > 0) {
									for (int j = 0; j < processQueue.size(); j++) {
										if(processQueue.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size() -1){
											//Do nothing
											//highest priority is in that place and we can still evaluate next process priority
										}
										else if(workingProcess.getPriority() < processQueue.get(j).getPriority()) {
											processQueue.add(j, workingProcess);
											break;
										}
										else {
											processQueue.add(workingProcess);
											break;
										}
									}
								}
								else {
									processQueue.add(workingProcess);
								}
							}
							else {
								//Priority is at max so it's at the end of the que
								processQueue.add(workingProcess);
							}
							timeStamp.get(workingProcess.getNumber()).add(tracker);
							workingProcess = null;
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							processBlock.add(workingProcess);
							workingProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type in quantum time end");
						}
					}
					//Case for if there is no remaining time and the process should be done
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
					//Case for if there is remaining time
					//Need to readd event and time to the front
					//Then put process to the end of its queue
					else if (burstTime > 0 ) {
						workingProcess.addEventType("CPU");
						workingProcess.addEventTime(burstTime);
						//Need to change the priority by 1
						if(workingProcess.getPriority() < max) {
							workingProcess.setPriority(workingProcess.getPriority()+1);
							//add process to queue
							if(processQueue.size() > 0) {
								for (int j = 0; j < processQueue.size(); j++) {
									if(processQueue.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size() -1){
										//Do nothing
										//highest priority is in that place and we can still evaluate next process priority
									}
									else if(workingProcess.getPriority() < processQueue.get(j).getPriority()) {
										processQueue.add(j, workingProcess);
										break;
									}
									else {
										processQueue.add(workingProcess);
										break;
									}
								}
							}
						}
						else {
							processQueue.add(workingProcess);
						}
						timeStamp.get(workingProcess.getNumber()).add(tracker);
						burstTime = 0;
						workingProcess = null;
					}
				}
				
				if (blockedProcess != null) {
					//Need to add the remaining time if there is any
					//Case for if there is no remaining time
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							processBlock.add(blockedProcess);
							blockedProcess = null;
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							//Need to change the priority by 1
							if(blockedProcess.getPriority() < max) {
								blockedProcess.setPriority(blockedProcess.getPriority()+1);
								//add process to queue
								if(processQueue.size() > 0) {
									for (int j = 0; j < processQueue.size(); j++) {
										if(processQueue.get(j).getPriority() < blockedProcess.getPriority() && j != processQueue.size() -1){
											//Do nothing
											//highest priority is in that place and we can still evaluate next process priority
										}
										else if(blockedProcess.getPriority() < processQueue.get(j).getPriority()) {
											processQueue.add(j, blockedProcess);
											break;
										}
										else {
											processQueue.add(blockedProcess);
											break;
										}
									}
								}
								else {
									processQueue.add(blockedProcess);
								}
							}
							else {
								//Priority is at max so it's at the end of the queue
								processQueue.add(blockedProcess);
							}
							timeStamp.get(blockedProcess.getNumber()).add(tracker);
							blockedProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type in quantum time end");
						}
					}
					//Case for if there is no remaining time and the process should complete
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
					//Case for if there is remaining time
					//Need to readd event and time to the front
					//Then put process to the end of its queue
					else if (blockTime > 0 ) {
						blockedProcess.addEventType("IO");
						blockedProcess.addEventTime(blockTime);
						processBlock.add(blockedProcess);
						blockTime = 0;
						blockedProcess = null;
					}
				}
				//Reset quantum time
				quant = quantum;
			}
			//Adding processes to ready queue
			for(int i = 0; i < pr.size(); i++) {
				if(pr.get(i).getArrivalTime() == tracker) {
					//Change the priority to 0
					pr.get(i).setPriority(0);
					processQueue.add(pr.get(i));
					//First time a process is added
					timeStamp.get(pr.get(i).getNumber()).add(tracker);
					addedPrc++;
				}
			}
			
			while (true) {
				//Check if there is a process ready in the process queue
				if (workingProcess == null && processQueue.size() != 0) {
					//This is to see if it's the first time it's worked on
					workingProcess = processQueue.get(0);
					//This is when the process is pulled out
					timeStamp.get(workingProcess.getNumber()).add(tracker);
					processQueue.remove(0);
				}
				
				//Check if there is a blocked process in the queue and we are not using one
				if(blockedProcess == null && processBlock.size() != 0) {
					blockedProcess = processBlock.get(0);
					processBlock.remove(0);
				}
				
				//Check to see if we need to get a block time
				if(blockedProcess != null) {
					if(blockTime == 0 && blockedProcess.getEventTime().size() > 0) {
						event = blockedProcess.getEventTypes().get(0);
						if(event.equals("IO")) {
							blockTime = blockedProcess.getEventTime().get(0);
							blockedProcess.removeEventType();
							blockedProcess.removeEventTime();
						}
						//Add blocked process into process queue
						else if (event.equals("CPU")) {
							//Need to change the priority by 1
							if(blockedProcess.getPriority() < max) {
								blockedProcess.setPriority(blockedProcess.getPriority()+1);
								//add process to queue
								if(processQueue.size() > 0) {
									for (int j = 0; j < processQueue.size(); j++) {
										if(processQueue.get(j).getPriority() < blockedProcess.getPriority() && j != processQueue.size() -1){
											//Do nothing
											//highest priority is in that place and we can still evaluate next process priority
										}
										else if (processQueue.get(j).getPriority() < blockedProcess.getPriority() && j == processQueue.size() -1) {
											//We are at the end and still don't have higher priority. So add to the end
											processQueue.add(blockedProcess);
											break;
										}
										else if(blockedProcess.getPriority() < processQueue.get(j).getPriority()) {
											processQueue.add(j, blockedProcess);
											break;
										}
										else {
											processQueue.add(blockedProcess);
											break;
										}
									}
								}
								else {
									processQueue.add(blockedProcess);
								}
							}
							else {
								//Priority is at max so it's at the end of the queue
								processQueue.add(blockedProcess);
							}
							timeStamp.get(blockedProcess.getNumber()).add(tracker);
							blockedProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(blockTime == 0 && blockedProcess.getEventTime().size() == 0) {
						finishTime[blockedProcess.getNumber()] = tracker;
						System.out.println("process " + blockedProcess.getNumber() + " complete at: " + tracker); //debug
						blockedProcess = null;
						processComp++;
					}
				}
				//Check to see if we need to get a burst time
				if(workingProcess != null) {
					if(burstTime == 0 && workingProcess.getEventTime().size() > 0) {
						event = workingProcess.getEventTypes().get(0);
						if(event.equals("CPU")) {
							burstTime = workingProcess.getEventTime().get(0);
							workingProcess.removeEventType();
							workingProcess.removeEventTime();
						}
						//Add process into block queue
						else if (event.equals("IO")) {
							processBlock.add(workingProcess);
							workingProcess = null;
						}
						else {
							System.out.println("There was an error reading in event type");
						}
					}
					else if(burstTime == 0 && workingProcess.getEventTime().size() == 0) {
						finishTime[workingProcess.getNumber()] = tracker;
						System.out.println("process " + workingProcess.getNumber() + " complete at: " + tracker); //debug
						workingProcess = null;
						processComp++;
					}
					else if(burstTime > 0 && processQueue.size() > 0) {
						//This section is needed to check to see if there is a process with shorter time in the process queue 
						//There can be a process that is running with a longer time but this will stop it if a shorter time comes
						if (workingProcess.getPriority() < processQueue.get(0).getPriority()) {
							//We have the lowest priority
						}
						else if(workingProcess.getPriority() > processQueue.get(0).getPriority()) {
							workingProcess.addEventType("CPU");
							workingProcess.addEventTime(burstTime);
							for (int j = 0; j < processQueue.size(); j++) {
								if(processQueue.get(j).getPriority() < workingProcess.getPriority() && j != processQueue.size() -1){
									//Do nothing
									//highest priority is in that place and we can still evaluate next process priority
								}
								else if(workingProcess.getPriority() < processQueue.get(j).getPriority()) {
									//Might not work
									processQueue.add(j, workingProcess);	
									break;
								}
								else {
									processQueue.add(workingProcess);
									break;
								}
							}
							timeStamp.get(workingProcess.getNumber()).add(tracker);
							workingProcess = null;
							burstTime = 0;
						}
					}
				}
				if(burstTime > 0 && blockTime > 0) {
					break;
				}
				else if (burstTime > 0 && processBlock.size() == 0) {
					break;
				}
				else if (blockTime > 0 && processQueue.size() == 0) {
					break;
				}
				else if (processComp == pr.size()) {
					break;
				}
			}
			
			//######################################################
			//Debug
			System.out.println("tracker: " + tracker);
			System.out.println("quantum: " + quant);
			if (workingProcess != null) {
				System.out.println("Working process: " + workingProcess.getNumber());
				System.out.println("           burst time: " + burstTime);
			}
			
			if (blockedProcess != null) {
				System.out.println("blocked process: " + blockedProcess.getNumber());
				System.out.println("           block time: " + blockTime);
			}
			System.out.println("\n");
			//######################################################
			
			//This is where the time starts to be calculated
			if(burstTime > 0 && blockTime == 0) {
				if(addedPrc != pr.size()) {
					//This will get the next process in the process arraylist
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime > nextArive) {
						if(quant > nextArive) {
							tracker += nextArive;
							burstTime -= nextArive;
							quant -= nextArive;
						}
						else if (quant <= nextArive) {
							tracker += quant;
							burstTime -= quant;
							quant -= quant;
						}
					}
					else if (burstTime <= nextArive) {
						if(quant <= burstTime) {
							tracker += quant;
							burstTime -= quant;
							quant -= quant;
						}
						else if (quant > burstTime) {
							tracker += burstTime;
							quant -= burstTime;
							burstTime -= burstTime;
						}
					}
				}
				else if (addedPrc == pr.size()){
					if (burstTime > quant) {
						tracker += quant;
						burstTime -= quant;
						quant -= quant;
					}
					else if (burstTime <= quant){
						tracker += burstTime;
						quant -= burstTime;
						burstTime -= burstTime;
					}
				}
				else {
					System.out.println("Error occured in burst time calculations");
				}
			}
			else if(blockTime > 0 && burstTime == 0) {
				if(addedPrc != pr.size()) {
					//This will find if there is a process that needs to arrive 
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(blockTime > nextArive) {
						if(quant > nextArive) {
							tracker += nextArive;
							blockTime -= nextArive;
							quant -= nextArive;
						}
						else if (quant <= nextArive) {
							tracker += quant;
							blockTime -= quant;
							quant -= quant;
						}
					}
					else if (blockTime <= nextArive) {
						if(quant <= blockTime) {
							tracker += quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > blockTime) {
							tracker += blockTime;
							quant -= blockTime;
							blockTime -= blockTime;
						}
					}
				}
				else if (addedPrc == pr.size()){
					if (blockTime > quant) {
						tracker += quant;
						blockTime -= quant;
						quant -= quant;
					}
					else if (blockTime <= quant){
						tracker += blockTime;
						quant -= blockTime;
						blockTime -= blockTime;
					}
				}
				else {
					System.out.println("Error occured in block time calculations");
				}
			}
			else if (blockTime > 0 && burstTime > 0) {
				if(addedPrc != pr.size()) {
					int nextArive = pr.get(addedPrc).getArrivalTime() - tracker;
					if(burstTime <= nextArive && blockTime <= nextArive) {
						if(burstTime <= blockTime) {
							if (quant <= burstTime) {
								tracker += quant;
								blockTime -= quant;
								burstTime -= quant;
								quant -= quant;
							}
							else if (quant > burstTime) {
								tracker += burstTime;
								quant -= burstTime;
								blockTime -=  burstTime;
								burstTime -= burstTime;
							}
						}
						else if (burstTime > blockTime) {
							if (quant <= blockTime) {
								tracker += quant;
								burstTime -= quant;
								blockTime -= quant;
								quant -= quant;
							}
							else if (quant > blockTime) {
								tracker += blockTime;
								quant -= blockTime;
								burstTime -= blockTime;
								blockTime -= blockTime;
							}
						}
					}
					else if (burstTime > nextArive && blockTime > nextArive) {
						if(quant <= nextArive) {
							tracker += quant;
							burstTime -= quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > nextArive) {
							tracker += nextArive;
							burstTime -= nextArive;
							blockTime -= nextArive;
							quant -= nextArive;
						}
					}
				}
				else if (addedPrc == pr.size()) {
					if(burstTime <= blockTime) {
						if(quant <= burstTime) {
							tracker += quant;
							blockTime -= quant;
							burstTime -= quant;
							quant -= quant;
						}
						else if (quant > burstTime) {
							tracker += burstTime;
							quant -= burstTime;
							blockTime -=  burstTime;
							burstTime -= burstTime;
						}
					}
					else if(burstTime > blockTime) {
						if(quant <= blockTime) {
							tracker += quant;
							burstTime -= quant;
							blockTime -= quant;
							quant -= quant;
						}
						else if (quant > blockTime) {
							tracker += blockTime;
							quant -= blockTime;
							burstTime -= blockTime;
							blockTime -= blockTime;
						}
					}
				}
				else {
					System.out.println("Error occured in block and burst time calculations");
				}
			}
		}
		//Get the start times
		for(int i = 0; i < pr.size(); i++) {
			//The first time a process is worked with is always at the 2nd timestamp because that's the first time it got pulled out
			startTime[i] = timeStamp.get(i).get(1);
		}
		
		//Print the processes here and call stat functions
		turnTime = turnTime(startTime, finishTime);
		normTime = normTurnTime(turnTime, serviceTime);
		avgResTime = avgRespTime(timeStamp);
		
		for(int i = 0; i < pr.size(); i++) {
			printProcessStats(pr.get(i), serviceTime[i], startTime[i], finishTime[i], 
					turnTime[i], normTime[i], avgResTime[i]);
		}
		
		//Calc the mean of the 3 functions
		float meanTurnTime = 0;
		float meanNormTime = 0;
		float meanAvgResTime = 0;
		meanTurnTime = meanCalc(pr, turnTime);
		meanNormTime = meanCalc(pr, normTime);
		meanAvgResTime = meanCalc(pr, avgResTime);
		//print mean times
		printMeanStats(meanTurnTime, meanNormTime, meanAvgResTime);
		
	}
	
	//Functions to calculate statistics
	public static float meanCalc (ArrayList<Process> pr, float [] values) {
		float total = 0;
		for(int i = 0; i < pr.size(); i++) {
			total += values[i];
		}
		total = total / pr.size();
		return total;
	}
	public static float [] turnTime (int [] start, int [] finish) {
		float [] turn = new float[start.length];
		
		for (int i = 0; i < start.length; i++) {
			int s = start[i];
			int f = finish[i];
			turn[i] = f - s;
		}
		return turn;
	}
	
	public static float [] normTurnTime (float [] turnTime, int [] servTime) {
		float [] normTurn = new float[turnTime.length];
		for (int i = 0; i < turnTime.length; i++) {
			float tt = turnTime[i];
			int st = servTime[i];
			normTurn[i] = tt / st;
		}
		return normTurn;
	}
	
	public static float [] avgRespTime (ArrayList<ArrayList<Integer>> stampTime) {
		ArrayList<ArrayList<Integer>> waitTime = new ArrayList<ArrayList<Integer>>();
		float [] avgResp = new float[stampTime.size()];
		//calculate wait time
		for (int i = 0; i < stampTime.size(); i++) {
			ArrayList<Integer> temp = new ArrayList<Integer>();
			waitTime.add(temp);
			int out = 1;
			int wait = 0;
			for(int j = 0; j < stampTime.get(i).size() - 1; j += 2) {
				wait = stampTime.get(i).get(out) - stampTime.get(i).get(j);
				waitTime.get(i).add(wait);
				out += 2;
			}
		}
		//calculate avg
		int length = 0;
		for(int i = 0; i < waitTime.size(); i++) {
			length = waitTime.get(i).size();
			float total = 0;
			for(int j = 0; j < length; j++){
				total += waitTime.get(i).get(j);
			}
			total = total/length;
			avgResp[i] = total;
		}
		return avgResp;
	}
	
	//Send one process to this method at a time
	//Print function
	public static void printProcessStats(Process pr, int service, int start,
			int finish, float turnaround, float normalized, float response) {
		StringBuilder sb = new StringBuilder();
		sb.append("Process: "+ pr.getNumber() + "\n");
		sb.append("    priority: " + pr.getPriority() + "\n");
		sb.append("    arrival: " + pr.getArrivalTime() + "\n");
		sb.append("    service time: " + service + "\n");
		sb.append("    start time: " + start + "\n");
		sb.append("    finish time: " + finish + "\n");
		sb.append("    turnaround time: " + turnaround + "\n");
		sb.append("    normalized turnaround time: " + normalized + "\n");
		sb.append("    average response time: " + response + "\n");
		System.out.println(sb);
	}
	
	public static void printMeanStats(float turnAround, float normalized, float average) {
		StringBuilder sb = new StringBuilder();
		sb.append("mean turnaround time: " + turnAround + "\n");
		sb.append("mean normalized time: " + normalized + "\n");
		sb.append("mean average response time: " + average + "\n");
		System.out.println(sb);
	}
}