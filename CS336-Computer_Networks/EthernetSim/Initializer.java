/*
 * Initializer :
 *      This class is responsible for starting the main execution loop. It also
 *      will initialize the logging classes in addition to the SimulatedTime
 *      main class.
 */



//import EthernetSimulator.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.NDC;

import java.net.URL;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;
import java.util.UUID;

import java.util.Date;

//import java.sql.*;

//import SQLiteLogger;
//import SimulatedEvent;

import Ethernet.*;

public class Initializer
{
    static Logger logger = Logger.getLogger(Initializer.class);
    static String propFile = "/l4j.properties";

    private SQLiteLogger sqlEvtLogger;

    private static int MAX_EXPERIMENT_TIME = 150000000; // 15 seconds * 10^7 Mbits/second, which is 10 seconds + 5 seconds of stabilization
    private static int MIN_MEASUREMENT_TIME = 50000000; // 5 seconds * 10^7 MBits/second to stabilize
    
    //private static int STDOUT_REPORT_INTERVAL = 5000000; // print something to standard out every half second of simulation elapse
    

    // an ArrayList of packet sizes in bytes as ints
    private static ArrayList<Integer> PACKET_SIZE_LIST = new ArrayList<Integer>(Arrays.asList(64, 128, 256, 512, 768, 1024, 1536, 2048, 3072, 4000));

    // number of times to run a simulation for a set of transmitting host numbers and packet sizes
    private static int NUM_TRIALS_PER_CONFIG = 1;

    public Initializer(String dbFileLoc)
    {
	try
	    {
		sqlEvtLogger = new SQLiteLogger(dbFileLoc);
	    }
	catch(Exception e)
	    {
		logger.error(e.getMessage());
	    }
    }

    public SQLiteLogger getEvtLogger()
    {
	return sqlEvtLogger;
    }

    public static void main(String[] args)
    {
	URL confResource = Initializer.class.getResource(propFile);
	PropertyConfigurator.configure(confResource);

	logger.info("Initializing");

	System.out.println("EthernetSimulator by Nathaniel Lim and Lee Wang");

	System.out.println("Type \"quit\" at any time to exit this program.");

	String curInput = "";

	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	Initializer initThis = new Initializer("ESData.s3db");

	SQLiteLogger curLogger = initThis.getEvtLogger();


	// record the current time in unix-time and store it
	long curUnixTime = System.currentTimeMillis() / 1000L;
	// keep track of the standard representation of current datetime
	Date curDate = new Date();

	// new experiment with 24 hosts
	int newExperimentID = curLogger.CreateNewExperiment(curUnixTime, curDate.toString(), 24);

	logger.info("New experiment ID: " + newExperimentID);

	do
	    {
		try
		    {
			System.out.println("Run a new experiment? (y/n)");

			curInput = in.readLine();

			if(curInput.equals("y"))
			    {
				//System.out.println("Enter a packet size in bytes");

				//curInput = in.readLine();

				//for(int activeHosts = 2; activeHosts < 25; activeHosts++)
				//    { 

				int activeHosts = 6;

					for(int packSize = 0; packSize < PACKET_SIZE_LIST.size(); packSize++)
					    {

						for(int numTrial = 0; numTrial < NUM_TRIALS_PER_CONFIG; numTrial++)
						    {
							System.out.println("Initializing simulation with " + activeHosts + " transmitting hosts and " + PACKET_SIZE_LIST.get(packSize) + " byte sized packets. Trial " + numTrial);
							
							RunSimulationWithHostNumAndPacketSize(activeHosts, PACKET_SIZE_LIST.get(packSize), curLogger, newExperimentID);
						    }
					    }
					//    }

			    }
		    }
		catch(NullPointerException nulle)
		    {
			logger.error(nulle.getMessage());
		    }
		catch(Exception e)
		    {
			logger.error(e.getMessage());
		    }
	    }
	while(!curInput.equals("quit"));


	curLogger.close();


	logger.info("Done");
    }



    public static void RunSimulationWithHostNumAndPacketSize(int activeHosts, int packSizeForThisExperiment,
							     SQLiteLogger curLogger, int newExperimentID)
    {
	// number of hosts to run experiment on, 24 from paper where 0-5, 6-11, 12-17, 18-23
	//     where on the same repeater so where 20 feet * 2 from each other, but inbetween
	//     the groups of hosts, there were 1000 feet so you need to take the 
	//     abs((floor(host id) / 6) - floor(other host id) / 6) * 1000 feet to find the
	//     distance between them in feet, then its 65.62 feet / bit so divide by that
	//     to find the number of bits between any two hosts
	int numHosts = 24;
	
	if(newExperimentID > 0)
	    {
		// create SimulatedTime for this experiment
		SimulatedTime stime = new SimulatedTime();
		
		ArrayList<Host> hostList = new ArrayList<Host>();
		ArrayList<Integer> hostIndices = new ArrayList<Integer>();
		
		// create hosts
		for(int i = 0; i < numHosts; i++)
		    {
			Host hostToAdd = new Host(stime, packSizeForThisExperiment, i);
			
			hostList.add(hostToAdd);
			
			hostIndices.add(i);
			
			stime.addHost(hostToAdd);
			
			// debugging, log creation of each host
			//curLogger.CreateNewHost(newExperimentID, i);
		    }
		
		Random r = new Random();
		boolean evenTopology = true;
		if (evenTopology){
			for(int k = 0; k < activeHosts; k++){
				Host hostToActivate = hostList.get(hostIndices.get(k));			
				hostToActivate.scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_READY, hostToActivate.RandomProcessTime(), true);
		    	}
		} else {
			// randomly remove all but activeHosts number of indices from the array containing the all of the host id's
			for(int j = 0; j < (numHosts - activeHosts); j++) {
				hostIndices.remove(r.nextInt(hostIndices.size()));
		    	}
		
			// schedule initialization events in the indices that are still active
			for(int k = 0; k < hostIndices.size(); k++){
				Host hostToActivate = hostList.get(hostIndices.get(k));			
				hostToActivate.scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_READY, hostToActivate.RandomProcessTime(), true);
		    	}			
		}
		
			
		//logger.info("Beginning elapse loop.");
				
		//int curEvtCount = 0;
		
		boolean beganMeasuring = false;
		
		//int elapseInterval = STDOUT_REPORT_INTERVAL;
		
		
		
		// run elapse();
		while(stime.elapse() && stime.getCurrentTime() < MAX_EXPERIMENT_TIME)
		    {
			//if(stime.getCurrentTime() > elapseInterval)
			//{
			//	System.out.println(stime.getCurrentTime() + " bit times have elapsed; " + stime.GetTimelineSize() + " events in priority queue;");
				
			//	elapseInterval += STDOUT_REPORT_INTERVAL;
			//  }
			
			//logger.info("Elapsing");
			
			//SimulatedEvent evtToLog = stime.CurrentEvent;
			
			//SimulatedEvent.SimEvtType curEvtType = evtToLog.getEventType();
			
			// it's working, don't log anything in terms of individual events right now
			
			/*
			  if(!evtToLog.justMyself || curEvtType == SimulatedEvent.SimEvtType.BACKOFF_DONE
			  || curEvtType == SimulatedEvent.SimEvtType.PACKET_READY || curEvtType == 
			  SimulatedEvent.SimEvtType.PACKET_ABORTED)
			  {
			  curLogger.CreateNewEvent(
			  newExperimentID,
			  evtToLog.TimeStart,
			  evtToLog.TimeDuration,
			  curEvtType.name(),
			  evtToLog.hostCreated,
			  (evtToLog.justMyself ? 1 : 0)
			  );
			  }
			*/
			
			//curEvtCount++;
			
			
			
			
			
			// if we have passed the minimum threshold for beginning to record statistics, and we have not called
			//    hosts.StartMeasuring yet, then do so
			if(!beganMeasuring && stime.getCurrentTime() > MIN_MEASUREMENT_TIME)
			    {
				// schedule initialization events in the indices that are still active
				for(int m = 0; m < hostIndices.size(); m++)
				    {
					Host hostToStartMeasure = hostList.get(hostIndices.get(m));
					
					hostToStartMeasure.StartMeasuring();
				    }
				
				beganMeasuring = true;
			    }
		    }
		
		
		
		// afterwards, get all of the values for collision periods and slots
		int totalBitsSent = 0;
		int totalPacketsSent = 0;
		
		double avgTransDelayNumerator = 0;


		ArrayList<Double> throughputs = new ArrayList<Double>();

		for(Host h : hostList)
		    {
			System.out.println("Host " + h.hostId + ": AvgSlotVal = " + h.reportAverageWaitSlots() +
					   "; NumCollisionPeriods = " + h.reportTotalCollisionPeriods() + "; Sent " + 
					   h.reportTotalPacketsSent() + " packets; Dropped " + h.reportTotalPacketsDropped() 
					   + " packets;");
			
			totalBitsSent += h.reportTotalBitsSent();
			totalPacketsSent += h.reportTotalPacketsSent();

			// to keep the squared numbers manageable, lets divide by some amount, all that matters is their
			//    ratio in the end anyways. Let's say bits / millisecond.
			throughputs.add(new Double(h.reportTotalBitsSent() / 1000));

			// if this host was active, sum its transmission delay for division later
			if(h.reportTotalPacketsSent() > 0)
			    {
				avgTransDelayNumerator += h.reportAverageTransDelay();
			    }
		    }

		double fairnessIndex = CalculateFairness(throughputs);

		double avgTransDelay = avgTransDelayNumerator / activeHosts;

		double experimentDuration = stime.getCurrentTime() - MIN_MEASUREMENT_TIME;
		
		System.out.println("Bits of packet data sent:" + totalBitsSent + "; Total bit times: " + experimentDuration);

		System.out.println("Average transmission delay: " + avgTransDelay + "; Fairness Index: " + fairnessIndex);


		curLogger.CreateNewExperimentSummary(newExperimentID, activeHosts, packSizeForThisExperiment, totalPacketsSent, totalBitsSent, experimentDuration, avgTransDelay, fairnessIndex);
		
	    }
	else
	    {
		System.out.println("Database unavailable, quitting.");
		
		logger.info("Unable to create new Experiment row in database, ID returned:" + newExperimentID);	
	    }	
    }
    

    public static double CalculateFairness(ArrayList<Double> throughputs)
    {
	double numerator = 0;
	double denominator = 0;

	int numActive = 0;

	for(Double thru : throughputs)
	    {
		if(thru > 0)
		    {
			numActive++;

			numerator += thru;

			denominator += Math.pow(thru, 2.0);
		    }
	    }

	numerator = Math.pow(numerator, 2.0);

	return numerator / (numActive * denominator);
    }
}