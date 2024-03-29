package Ethernet;
import java.util.UUID;
import java.util.HashMap;
import java.lang.Math;
import java.util.Random;

public class Host {

    public static boolean FAIRNESS_MODE = false;

    public int hostId;	
    int repeaterIndex;

    Transmitter tstate;
    Receiver rstate;
    SimulatedTime stime;
    SimulatedEvent expectedTransDone;

    Random r = new Random();
    int transAttempt = 0;
    int processTime;
    //int packetsToSend = 2;
    int packetSize;

    int packetsSent = 0;
    int packetsDropped = 0;

    double totalCollisionSlots = 0;
    //double avgCollisionSlotVal = 0;
    int numCollisionPeriods = 0;
    double totalTransDelay = 0;
    //double avgTransDelay = 0;
    double packRdyTime = 0;

    //HashMap<UUID, SimulatedEvent> upcomingEvents;

    // obsolete measure
    //int position; // define this to be the time units from the left-most edge
    // of the network

    //int repeaterIndex;

    public static double PREAMBLE_TIME = 64.0;
    public static double GAP_TIME = 96.0;
    public static double JAMMING_TIME = 32.0;
    public static double SLOT_TIME = 512.0;


    // number of feet per bit
    public static double FEET_PER_BIT = 65.6167979;


    // distance to nearest repeater in feet
    public static double DISTANCE_TO_REPEATER = 20;
    // distance between two hosts excluding the distance of their repeaters
    public static double BIT_DISTANCE_BETWEEN_TWO_HOSTS = 2 * DISTANCE_TO_REPEATER / FEET_PER_BIT;

    // accounts for the preamble of each packet, and interpacket gap, but we are excluding the CRC since that is part of the header which
    //   is accounted for in the packet size
    public static int OVERHEAD_BITS_PER_PACKET = 20 * 8;
    

    public static int MAX_PROCESS_TIME = 1250;
    public static int MIN_PROCESS_TIME = 1000;


    public Host(SimulatedTime parentTime, int pSize, int inID)
    {
	hostId = inID;

	// For our fairness test, set up an extremely unfair topology.
	if (FAIRNESS_MODE) {
	    // hosts 0-22 are on repeater 0, host 23 is on repeater 10, 10,000 feet away
	    repeaterIndex = (inID < 4) ? 0 : 10;
	} else {
	    //Set up the topology normally.
	    Double grpOfSix = new Double(Math.floor(inID / 6.0));
	    repeaterIndex = grpOfSix.intValue();
	}

	


	tstate = Transmitter.PREPARING;
	rstate = Receiver.IDLE;

	stime = parentTime;
	
	// packetSize in host is in bits, but it comes in as bytes
	packetSize = pSize * 8;

	//position = curPos;
	processTime = 10;

	//upcomingEvents = new HashMap<UUID, SimulatedEvent>();
    }

    public void StartMeasuring()
    {
	//avgTransDelay = 0;

	totalTransDelay = 0;

	//avgCollisionSlotVal = 0;

	totalCollisionSlots = 0;

        numCollisionPeriods = 0;

	packetsSent = 0;

	packetsDropped = 0;
    }

    public double RandomProcessTime()
    {
	return (double)(MIN_PROCESS_TIME + r.nextInt(MAX_PROCESS_TIME - MIN_PROCESS_TIME));
    }

    public double BitDistanceFromAnotherRepeaterIndex(int otherRptInd)
    {
	return Math.abs(repeaterIndex - otherRptInd) * 1000.0 / FEET_PER_BIT;
    }

    public boolean isMyEvent(SimulatedEvent e){
	return e.hostCreated == this.hostId;
    }

    public void scheduleMyEvent(SimulatedEvent.SimEvtType type, double startoffset, boolean justMyself){
       
	    
		UUID id = UUID.randomUUID();
		double currentTime = stime.getCurrentTime();
		SimulatedEvent event = new SimulatedEvent(id, type, currentTime + startoffset, startoffset, this.hostId, repeaterIndex, justMyself);
		
		stime.schedule(event);	

		if(type == SimulatedEvent.SimEvtType.TRANS_DONE) {
		    expectedTransDone = event;
		}
	    
	
    }


    public double reportAverageWaitSlots()
    {
	return totalCollisionSlots / numCollisionPeriods;
    }

    public int reportTotalCollisionPeriods()
    {
	return numCollisionPeriods;
    }

    public int reportTotalPacketsSent()
    {
	return packetsSent;
    }

    public int reportTotalPacketsDropped()
    {
	return packetsDropped;
    }

    public int reportTotalBitsSent()
    {
	// including overhead
	return (packetSize + OVERHEAD_BITS_PER_PACKET) * packetsSent;
    }

    public double reportAverageTransDelay()
    {
	return totalTransDelay / packetsSent;
    }

	public void reactToEvent(SimulatedEvent e) {

            double relpos = BIT_DISTANCE_BETWEEN_TWO_HOSTS + BitDistanceFromAnotherRepeaterIndex(e.RepeaterIndexOfHost);

	    //Determine the state changes of the receiver
	    switch (rstate) {

			case BUSY:				
				//Line is busy.  If we see the end of a transmission or jamming.
				if (e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_DONE || 
				    e.getEventType() == SimulatedEvent.SimEvtType.TRANS_DONE ){

					//If:   The END I see came from another host AND I am supposed to see it.
					//	Reschedule the END so it's my END and only I can see it.
					//Else: Take this rescheduled event, now I experience the event, Go to Gap state, 
					//	and schedule for myself when the gap is over   
					
					if ( !isMyEvent(e) && !e.justMyself ){
					    scheduleMyEvent(e.getEventType(), relpos, true);
					} else if (e.justMyself && isMyEvent(e)){
						rstate = Receiver.GAP;
						scheduleMyEvent(SimulatedEvent.SimEvtType.GAP_DONE, GAP_TIME, true);
					}

				}

				break;
			case GAP:
				//If I see that MY Gap time is over, then schedule a notification to myself that 
				//My receiver is now idle, and move to the idle state.

				if (isMyEvent(e) && e.getEventType() == SimulatedEvent.SimEvtType.GAP_DONE)  {
					
				    //scheduleMyEvent(SimulatedEvent.SimEvtType.R_NOW_IDLE, 0, true);
				    rstate = Receiver.IDLE;
				}

				break;
			case IDLE:				

				//If:	The line is idle and we see the start of a signal
					
				if ((e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_START  ||
				     e.getEventType() == SimulatedEvent.SimEvtType.PREAMBLE_START ||
				     e.getEventType() == SimulatedEvent.SimEvtType.TRANS_START    ))  {
					
					//If:   The START I see came from another host AND I am supposed to see it.
					//	Reschedule the START so it's my START and only I can see it.
					//Else: Take this rescheduled event, now I experience the event, Go to BUSY state, 
					//	and schedule a notification for myself that the receiver has become busy.

					if ( !isMyEvent(e) && !e.justMyself ) {
					    scheduleMyEvent(e.getEventType(),relpos, true);
					} else if (e.justMyself && isMyEvent(e)){
					    //scheduleMyEvent(SimulatedEvent.SimEvtType.R_NOW_BUSY, 0, true);
					    rstate = Receiver.BUSY;
					}
				}

				// if we see a trans_done and it's my own and it is not a pseudo-event, wait gap
				else if(e.getEventType() == SimulatedEvent.SimEvtType.TRANS_DONE && isMyEvent(e) && !e.justMyself)
				    {
					scheduleMyEvent(SimulatedEvent.SimEvtType.GAP_DONE, GAP_TIME, true);
					rstate = Receiver.GAP;
				    }
				
				break;
	    }
	    

	    // determine state changes of the transmitter
	    switch (tstate) {
			case EAGER: 
				//I am eager to send packets, and I see that MY receiver is idle, I start my preamble, and schedule when I end.

			    //if (e.getEventType() == SimulatedEvent.SimEvtType.R_NOW_IDLE && isMyEvent(e)){
			    if(rstate == Receiver.IDLE){
				//(rstate == Receiver.IDLE) {
				    if(transAttempt == 0)
					{
					    packRdyTime = stime.getCurrentTime();
					}

				    scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, false);
				    scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, true);
				    tstate = Transmitter.PREAMBLE;
				}

				break;
			case PREAMBLE:
				// I am transmitting my preamble, when MY preamble is done, 
				// If:   Receiver is idle, start packet, schedule packet end, go to sending state
				// Else: Collision occurred so start jamming, schedule jamming end, go to jamming state.

				if (e.getEventType() == SimulatedEvent.SimEvtType.PREAMBLE_DONE && isMyEvent(e) ) {
				    if (rstate == Receiver.IDLE) {
					    scheduleMyEvent(SimulatedEvent.SimEvtType.TRANS_START, 0, false);
					    scheduleMyEvent(SimulatedEvent.SimEvtType.TRANS_DONE, packetSize, false);
					    tstate = Transmitter.SENDING;
					} else {
					    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_START, 0, false);
					    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_DONE, JAMMING_TIME, false);
					    tstate = Transmitter.JAMMING;
					}
				}

				break;
			case JAMMING:
				//If my jamming is done.  Schedule when my backoff will be over
				//Increment the transAttempt.  Go to WAITING backoff slots state.  
				
				if (e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_DONE && isMyEvent(e)) {

				    int maxWaitSlots = 1023; // 2 ^ 10 - 1

				    if(transAttempt < 10)
					{
					    Double power = new Double(Math.pow(2.0, transAttempt));
					    maxWaitSlots = power.intValue();
					}

				    int numK = r.nextInt(maxWaitSlots);

				    //int avgCollisionSlotVal = 0;
				    //int numCollisionPeriods = 0;

				    // new average is the old average times number of prior periods + current val / number of periods
				    //avgCollisionSlotVal = (numCollisionPeriods * avgCollisionSlotVal + numK) / (numCollisionPeriods + 1);

				    // alternatively, just add up the numerator to divide later, duh.
				    totalCollisionSlots += numK;
				    numCollisionPeriods++;
				
				    scheduleMyEvent(SimulatedEvent.SimEvtType.BACKOFF_DONE,SLOT_TIME *((double)numK), true);
				 
				    transAttempt++;
				    tstate = Transmitter.WAITING;
				}

				break;
			case SENDING:
				//I am sending packets.  
				//If my receiver has become busy, start jamming, schedule jamming end, go to jamming state
				//Else: If the transmission is complete, If I have no more to send, I'm done 
				//	Else: schedule when the next packet will be ready, go to preparing state	   
			    // if (e.getEventType() == SimulatedEvent.SimEvtType.R_NOW_BUSY && isMyEvent(e)) {
			    if (rstate == Receiver.BUSY){
				    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_START, 0, false);
				    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_DONE, JAMMING_TIME, false);
				    tstate = Transmitter.JAMMING;
				    stime.deschedule(expectedTransDone);

				} else if (e.getEventType() == SimulatedEvent.SimEvtType.TRANS_DONE && isMyEvent(e)) {
				    // measure delay as current time minus packRdyTime, averaged out over packetsSent
				    //avgTransDelay = ((avgTransDelay * packetsSent) + (stime.getCurrentTime() - packRdyTime)) / (packetsSent + 1);

				// alternatively, sum up transDelay to divide later, duh
				totalTransDelay += stime.getCurrentTime() - packRdyTime;

				    // successfully sent packet, record it, reset k to 0
				    packetsSent++;
				    transAttempt = 0;

				    scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_READY, RandomProcessTime(), true);
				    tstate = Transmitter.PREPARING; 										
				}

				break;
			case PREPARING:
	
			       // I'm preparing the next packet.  Once MY packet is ready, 
			       // If the receiver is idle: start preamble, schedule preamble done, go to preamble 
			       // Else: Go to Eager state
			       if (e.getEventType() == SimulatedEvent.SimEvtType.PACKET_READY && isMyEvent(e)) {
					if (rstate == Receiver.IDLE) {
					    if(transAttempt == 0)
						{
						    packRdyTime = stime.getCurrentTime();
						}

					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, false);
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, true);
					     tstate = Transmitter.PREAMBLE;
					} else {
						tstate = Transmitter.EAGER;
					}
				}

			       break;
			case WAITING:

			    //If I'm waiting the backoff slots, and I've tried way too many times, abort and move on to the next packet, schedule when its done, prepare
			    if (e.getEventType() == SimulatedEvent.SimEvtType.BACKOFF_DONE && isMyEvent(e)) {

				// if we have attempted to transmit this packet more than 15 times, it's time to abort it
				if (transAttempt >= 15) {

				    packetsDropped++;

				    // Packet is Aborted
				    scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_ABORTED, 0, true);

				    //packetsToSend--;
				    transAttempt = 0;
				    //if (packetsToSend == 0) {
				    //	tstate = Transmitter.DONE;
				    //} else {

				    scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_READY, RandomProcessTime(), true);
				    tstate = Transmitter.PREPARING;
					//}
				} 
				// if the receiver is idle, then start the preamble. Otherwise go to the eager transmitter state
				else
				    {
					if (rstate == Receiver.IDLE) {
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, false);
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, true);
					     tstate = Transmitter.PREAMBLE;
					} else {
					     tstate = Transmitter.EAGER;
					}
				    }
				}

				break;
		}
	}



	public enum Transmitter {
		EAGER, PREPARING, PREAMBLE, SENDING, JAMMING, WAITING, DONE
	}

	public enum Receiver {
		BUSY, GAP, IDLE
	}
}
