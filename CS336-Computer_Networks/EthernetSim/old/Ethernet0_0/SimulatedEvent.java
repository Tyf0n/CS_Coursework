/*
 * SimulatedEvent:
 *      A class that represents a discrete occurrance that we may want to record
 *      or affects another event that we want to record.
 */

package Ethernet;

import java.util.UUID;

public class SimulatedEvent
{
    public UUID ID;
    public SimEvtType evtType;

    public double TimeStart;
    public double TimeDuration;

    public SimulatedEvent(UUID inIdent, SimEvtType inType, double inStart,
			  double inDur)
    {
	ID = inIdent;
	evtType = inType;

	TimeStart = inStart;
	TimeDuration = inDur;
    }

    public SimEvtType getEventType()
    {
	return evtType;
    }

    // this set of types corresponds to the node state diagram
    public enum SimEvtType
    {
	PREAMBLE_TRANSMIT_0,     // first packet transmission attempt, k = 0;
	PREAMBLE_TRANSMIT_1,
	PREAMBLE_TRANSMIT_2,
	PREAMBLE_TRANSMIT_3,
	PREAMBLE_TRANSMIT_4,
        PREAMBLE_TRANSMIT_5,
	PREAMBLE_TRANSMIT_6,
	PREAMBLE_TRANSMIT_7,
	PREAMBLE_TRANSMIT_8,
	PREAMBLE_TRANSMIT_9,
	PREAMBLE_TRANSMIT_10,
	PREAMBLE_TRANSMIT_11,
	PREAMBLE_TRANSMIT_12,
        PREAMBLE_TRANSMIT_13,
	PREAMBLE_TRANSMIT_14,
        PREAMBLE_TRANSMIT_15,    // last packet transmission attempt, k = 15;

	PACKET_DONE,

	    //INTERPACKET_WAIT,
	
	COLLISION_START,

	JAMMING_START,

	    //EXPONENTIAL_BACKOFF_WAIT     // wait based on random val i: 0 < i < k time
    }
}