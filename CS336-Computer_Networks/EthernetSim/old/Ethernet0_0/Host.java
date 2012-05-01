package Ethernet;

import java.util.HashMap;

public class Host {

	int hostId;
	Transmitter tstate;
	Receiver rstate;
	SimulatedTime stime;

    int packetSize;

    int position; // define this to be the time units from the left-most edge
    // of the network



    public Host(SimulatedTime parentTime, int pSize, int curPos, int inID)
    {
	hostId = inID;

	tstate = PREPARING;
	rstate = IDLE;

	stime = parentTime;

	packetSize = pSize;

	position = curPos;
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


	HashMap<Integer, SimulatedEvent> upcomingEvents;

	public void reactToEvent(SimulatedEvent e) {
		switch (tstate) {
			case EAGER: 
				if (e.getEventType() ==  ) {
					tstate = Transmitter.PREAMBLE;
				}
			case PREAMBLE:
				if (e.getEventType() == preamble complete) {
					if (rstate == Receiever.IDLE) {
						tstate = Transmitter.SENDING;
					} else {
						tstate = Transmitter.JAMMING;
					}
				}
			case JAMMING:
				if (e.getEventType() == jamming complete) {
					tstate = Transmitter.WAITING;
				}
			case SENDING:
				if (e.getEventType() == receiver becomes busy) {
					tstate = Transmitter.JAMMING;
				} else if (e.getEventType() == transmission complete) {
					tstate = Transmitter.PREPARING;
				}
			case PREPARING:
				if (e.getEventType() == packet ready ) {
					if (rstate == Receiever.IDLE) {
						tstate = Transmitter.PREAMBLE;
					} else {
						tstate = Transmitter.EAGER;
					}
				}
			case WAITING:
				if (e.getEventType() == packet aborted) {
					tstate = Transmitter.PREPARING;
				} else if (e.getEventType() == Backoff complete) {
					if (rstate == Receiever.IDLE) {
						tstate = Transmitter.PREAMBLE;
					} else {
						tstate = Transmitter.EAGER;
					}
				}
		}

		switch (rstate) {
			case BUSY:
				if (e.getEventType() == end of signal detected )  {
					rstate = Receiver.GAP;
				}
			case GAP:
				if (e.getEventType() == period of 9.6 microseconds elapsed)  {
					rstate = Receiver.IDLE;
				}
			case IDLE:
				if (e.getEventType() == incoming signal detected )  {
					rstate = Receiver.GAP;
				}
		
		}	
		
	}



	public enum Transmitter {
		EAGER, PREPARING, PREAMBLE, SENDING, JAMMING, WAITING
	}

	public enum Receiver {
		BUSY, GAP, IDLE
	}
}
