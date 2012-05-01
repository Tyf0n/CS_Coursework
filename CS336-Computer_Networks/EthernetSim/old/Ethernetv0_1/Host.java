package Ethernet;
import java.util.UUID;
import java.util.HashMap;
import java.lang.Math;
import java.util.Random;

public class Host {

    int hostId;	
    Transmitter tstate;
    Receiver rstate;
    SimulatedTime stime;
    Random r = new Random();
    int transAttempt = 0;
    int processTime;
    
    int packetSize;

    HashMap<UUID, SimulatedEvent> upcomingEvents;

    int position; // define this to be the time units from the left-most edge
    // of the network

    public static double PREAMBLE_TIME = 64.0;
    public static double GAP_TIME = 96.0;
    public static double JAMMING_TIME = 32.0;


    public Host(SimulatedTime parentTime, int pSize, int curPos, int inID)
    {
	hostId = inID;

	tstate = Transmitter.PREPARING;
	rstate = Receiver.IDLE;

	stime = parentTime;

	packetSize = pSize;

	position = curPos;
	processTime = 10;//Should be random later
    }


    public void AddFutureEvent(SimulatedEvent simEvt)
    {
	upcomingEvents.put(simEvt.ID, simEvt);
    }

    /*
      whenever you need to cycle through all events that are in the future
      call

      while(upcomingEvents.values().Iterator().hasNext())
      {
      }
     */




    public void scheduleMyEvent(SimulatedEvent.SimEvtType type, double startoffset, double dur){
	UUID id = UUID.randomUUID();
	double currentTime = stime.getCurrentTime();
	SimulatedEvent event = new SimulatedEvent(id, type, currentTime + startoffset, dur, this.position);
	AddFutureEvent(event);
	stime.schedule(event);	
    }

	public void reactToEvent(SimulatedEvent e) {
	    int currentTime;
	    UUID id;
            int relpos = Math.abs(this.position - e.origin);
	    
	    switch (rstate) {
			case BUSY:
				if (e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_DONE ||
				    e.getEventType() == SimulatedEvent.SimEvtType.TRANS_DONE )  {      
					

				    if (relpos == 0){
					rstate = Receiver.GAP;
					scheduleMyEvent(SimulatedEvent.SimEvtType.GAP_DONE, GAP_TIME, 0);
				    }
				    scheduleMyEvent(SimulatedEvent.SimEvtType.R_NOW_BUSY,relpos , 0);
				       
				    
				    rstate = Receiver.GAP;
				}

				break;
			case GAP:
				if (e.getEventType() == SimulatedEvent.SimEvtType.GAP_DONE)  {
					
				    scheduleMyEvent(SimulatedEvent.SimEvtType.R_NOW_IDLE, 0, 0);
				    rstate = Receiver.IDLE;
				}

				break;
			case IDLE:				

				if (e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_START ||
				    e.getEventType() == SimulatedEvent.SimEvtType.PREAMBLE_START )  {
				     
				    if (relpos == 0){
					rstate = Receiver.BUSY;
				    }
				    scheduleMyEvent(SimulatedEvent.SimEvtType.R_NOW_BUSY,relpos , 0);
				}

				break;
	    }
	    
	    switch (tstate) {
			case EAGER: 
				if (e.getEventType() == SimulatedEvent.SimEvtType.R_NOW_IDLE ) {
				    scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, 0);
				    scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, 0);
				    tstate = Transmitter.PREAMBLE;
				}

				break;
			case PREAMBLE:
				if (e.getEventType() == SimulatedEvent.SimEvtType.PREAMBLE_DONE) {
					if (rstate == Receiver.IDLE) {
					    scheduleMyEvent(SimulatedEvent.SimEvtType.SIGNAL_START, 0, 0);
					    scheduleMyEvent(SimulatedEvent.SimEvtType.SIGNAL_DONE, 0, 0);
					    tstate = Transmitter.SENDING;
					} else {
					    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_START, 0, 0);
					    scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_DONE, JAMMING_TIME, 0);
					    tstate = Transmitter.JAMMING;
					}
				}

				break;
			case JAMMING:
				if (e.getEventType() == SimulatedEvent.SimEvtType.JAMMING_DONE) {

				    int maxWaitSlots = 1023; // 2 ^ 10 - 1

				    if(transAttempt < 10)
					{
					    Double power = new Double(Math.pow(2.0, transAttempt));

					    maxWaitSlots = power.intValue() - 1;
					}

				    if (transAttempt <= 10) {
				   	 scheduleMyEvent(SimulatedEvent.SimEvtType.BACKOFF_DONE,((double)r.nextInt(maxWaitSlots)), 0);
				    } else {
					 scheduleMyEvent(SimulatedEvent.SimEvtType.BACKOFF_DONE,((double)r.nextInt(maxWaitSlots)), 0);
				    }
				    transAttempt++;
				    tstate = Transmitter.WAITING;
				}

				break;
			case SENDING:
				if (e.getEventType() == SimulatedEvent.SimEvtType.R_NOW_BUSY) {

					scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_START, 0, 0);
					scheduleMyEvent(SimulatedEvent.SimEvtType.JAMMING_DONE, JAMMING_TIME, 0);
					tstate = Transmitter.JAMMING;

				} else if (e.getEventType() == SimulatedEvent.SimEvtType.SIGNAL_DONE) {

				        scheduleMyEvent(SimulatedEvent.SimEvtType.PACKET_READY, processTime, 0);
				        tstate = Transmitter.PREPARING;					
				}

				break;
			case PREPARING:
			       if (e.getEventType() == SimulatedEvent.SimEvtType.PACKET_READY) {
					if (rstate == Receiver.IDLE) {
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, 0);
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, 0);
					     tstate = Transmitter.PREAMBLE;
					} else {
						tstate = Transmitter.EAGER;
					}
				}

			       break;
			case WAITING:
				if (transAttempt == 15) {
				    // Packet is Aborted
				    transAttempt =0;
				    tstate = Transmitter.PREPARING;
				} else if (e.getEventType() == SimulatedEvent.SimEvtType.BACKOFF_DONE) {
					if (rstate == Receiver.IDLE) {
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_START, 0, 0);
					     scheduleMyEvent(SimulatedEvent.SimEvtType.PREAMBLE_DONE, PREAMBLE_TIME, 0);
					     tstate = Transmitter.PREAMBLE;
					} else {
						tstate = Transmitter.EAGER;
					}
				}

				break;
		}		
	}



	public enum Transmitter {
		EAGER, PREPARING, PREAMBLE, SENDING, JAMMING, WAITING
	}

	public enum Receiver {
		BUSY, GAP, IDLE
	}
}
