/*
 * SimulatedEvent:
 *      A class that represents a discrete occurrance that we may want to record
 *      or affects another event that we want to record.
 */

package Ethernet;

import java.util.UUID;

public class SimulatedEvent implements Comparable
{
    public UUID ID;
    public SimEvtType evtType;

    public int origin;
    public double TimeStart;
    public double TimeDuration;

    public SimulatedEvent(UUID inIdent, SimEvtType inType, double inStart,
			  double inDur, int orig)
    {
	origin = orig;
	ID = inIdent;
	evtType = inType;
	
	TimeStart = inStart;
	TimeDuration = inDur;
    }


    
    public int compareTo(Object o) {
	if (o instanceof SimulatedEvent) {
	    SimulatedEvent other = (SimulatedEvent)o;

	    double timeDiff = this.TimeStart - other.TimeStart;

	    if(timeDiff > 0)
		{
		    return 1;
		}
	    else if(timeDiff < 0)
		{
		    return -1;
		}
	    else
		{
		    return 0;
		}
	} else {
	    return 1;
	}
    }
    

    public SimEvtType getEventType()
    {
	return evtType;
    }

    // this set of types corresponds to the node state diagram
    public enum SimEvtType
    {
	PREAMBLE_START,
	PREAMBLE_DONE,

	TRANS_DONE,
	TRANS_START,
	
	SIGNAL_DONE,
	SIGNAL_START,

	GAP_DONE,
	
	COLLISION_START,

	JAMMING_START,
	JAMMING_DONE,

	 R_NOW_IDLE,
	 R_NOW_BUSY,

	BACKOFF_DONE, 
	PACKET_ABORTED,
        PACKET_READY
    }
}