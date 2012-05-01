/*
 * Nathaniel Lim 11/3
 * 
 */

package Ethernet;


import java.util.PriorityQueue;
import java.util.ArrayList;



public class SimulatedTime {

    private double currentTime =0;

	private PriorityQueue<SimulatedEvent> timeline;
	private ArrayList<Host> hostlist;

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
		SimulatedEvent currentevent = timeline.poll();
		currentTime = currentevent.TimeStart;
		if (currentevent == null){
			return false;
		} else {
			for (Host h : hostlist){
				h.reactToEvent(currentevent);			
			}
			return true;
		}
	}


	public boolean schedule(SimulatedEvent e){
		return timeline.add(e);
	}

	public boolean deschedule(SimulatedEvent e){
		return timeline.remove(e);
	}

    //public static void main (String [] args) {
			
    //}

	





}