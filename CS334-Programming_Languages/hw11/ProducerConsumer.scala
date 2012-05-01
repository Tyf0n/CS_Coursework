//Nathaniel Lim HW 11

import scala.actors.Actor;
import Actor._;

/*
 * A simple Actor that sends two messages to a Consumer
 */
class Producer(cons : Actor) extends Actor {
  def act = {
    println("Producer Starting")
    for (x <- 1 to 100){
    	cons ! x
    }
    cons ! -1
    println("Producer Done")
  }

  this.start();  // start running as soon as created
}


/*
 * Another Actor that recieves three messages of type
 * Int or String and then exits.
 */
class Consumer() extends Actor {
  def act : Unit = {
    println("Consumer Starting")
    while(1==1){    	
	receive {
		case i : Int  => if (i == -1) {this.exit} else {println("  Number " + i)}
		case s : String => println("  String " + s)
    	}
    }
  }

  this.start(); // start running as soon as created
}

/*
 * Create a Producer and Consumer and let them run.
 */
object ProducerConsumer {
  def main(args : Array[String]) : Unit = { 
    println("Main Starting")
    val cons = new Consumer()
    val prod = new Producer(cons)
    println("Main Done")
  }
}
