/**
 * This is the outline of the ethernet simulation program to be written for CS 336 by Nathaniel Lim and Lee Wang.
 *
 * First a list of necessary classes:
 *
 * SimulatedTime // Nathaniel
 *    - Timeline: a binary heap implemented as an array for the priority queue of events O(1) time for looking up the closest event +
 *      0(log n) for removing an event.
 *    - Elapse(): method, called by the main program loop, it removes the event from the priority queue with the minimum value of
 *      time left, and then removes it from the priority queue and loops through all Hosts telling them to react to it, would be
 *      really nice if it could return boolean so we can make a loop of while(SimulatedTime.Elapse())
 *    - Schedule(Event): method, called by Hosts to add an event to the Timeline
 *    - DeSchedule(eventID): method, called by Hosts to remove a previously registered event in the Timeline
 *    - HostsList: List of hosts duh
 *
 *
 * Host // Nathaniel
 *    - Transmitter: enumeration that represents the transmitter state of the host (per example state diagram from project handout)
 *    - Receiver: enumeration that represents the receiver state of the host (also from example state diagram)
 *    - UpcomingEvents: A hashtable of <eventId, Event> that have been registered with the SimulatedTime class
 *    - ReactToEvent(Event): The Host takes the event, and sets the values of its transmitter and receiver enums according
 *      to specified behavior. It then looks at the upcoming events that it owns, and if they become no longer valid (ie a transmit
 *      occurs from another host when it is transmitting preamble, it then removes the upcoming event for packet transmission and
 *      adds a new preamble transmit event set to occur at a chosen exponentially backed off time)
 *
 *
 * SimulatedEvent // Lee
 *    - ID: a UUID: http://download.oracle.com/javase/1.5.0/docs/api/java/util/UUID.html (guaranteed uniqueness)
 *    - Type: enumeration that contains the specific type of the event, aka preamble transmit, packet transmit, jamming transmit etc.
 *    - TimeStart: beginning value of the event
 *    - TimeDuration: how long the event lasts in the conventional time units we are using
 *
 *
 * SQLiteLogger  // I'll definitely take this one, Lee
 *    - A class that will talk to an underlying SQLite database file via the SQLiteJDBC wrapper
 *    - SimulationSession: UUID that keeps track of one run of the Simulator
 *    - LogPacketTransmissionTime()
 *    - LogNumCollissions()
 *    - LogNumPacketsSent()
 *    - TimeSpentInCollissions()
 *    - LogSessionData(TotalIdleTime, AveragePacketSize, TotalTimeElapsed)
 *
 *
 * Initializer // Lee
 *    - Launches everything but according to a file of input commands, like example:
 *      one command on each line by convention, following pattern [hostNumber][Packet][Time]
 */