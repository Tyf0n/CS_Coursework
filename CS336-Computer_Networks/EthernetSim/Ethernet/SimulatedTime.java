/*
 * Nathaniel Lim 11/3
 * 
 */

package Ethernet;

import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.Iterator;

public class SimulatedTime {

    private double currentTime = 0;

	private PriorityQueue<SimulatedEvent> timeline;
	private ArrayList<Host> hostlist;

    public SimulatedEvent CurrentEvent;

	public SimulatedTime () {
	    timeline = new PriorityQueue<SimulatedEvent>();
	    hostlist = new ArrayList<Host>();
	}

    public double getCurrentTime() {
	return currentTime;
    }

    public void addHost(Host someHost)
    {
	hostlist.add(someHost);
    }


    public boolean elapse(){
	CurrentEvent = timeline.poll();
	
	if (CurrentEvent == null){
		return false;
	} else {
	        currentTime = CurrentEvent.TimeStart;

		if(!CurrentEvent.justMyself)
		    {
			for (Host h : hostlist){
			    h.reactToEvent(CurrentEvent);
			}
		    }
		else
		    {
			hostlist.get(CurrentEvent.hostCreated).reactToEvent(CurrentEvent);
		    }


		return true;
	}
    }

    public int GetTimelineSize()
    {
	return timeline.size();
    }

    public String ExtractTimelineEvents()
    {
	String retStr = "";
	Iterator<SimulatedEvent> allEvtsLeft = timeline.iterator();

	while(allEvtsLeft.hasNext())
	    {
		retStr += allEvtsLeft.next() + System.getProperty("line.separator");
	    }

	return retStr;
    }

	public boolean schedule(SimulatedEvent e){
		return timeline.add(e);
	}

	public boolean deschedule(SimulatedEvent e){
		return timeline.remove(e);
	}

   




}
