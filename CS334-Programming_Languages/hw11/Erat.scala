//Nathaniel Lim HW 11

import scala.actors.Actor;
import Actor._;

class Siever() extends Actor {
  var firstVal :Int = 0;

  def act = {
	receive{
	     case i:Int => if(i == -1){this.exit} else {this.firstVal = i}
	}	
	println(this.firstVal)
	var nextS = new Siever()
	while(1==1){
	   receive{
	     case i:Int => if(i == -1){this.exit}else { if( i % this.firstVal != 0) { nextS ! i}}
	   }
	}
  }
  this.start();  
}

object Erat {
  def main(args : Array[String]) : Unit = { 
    val limit = args(0).toInt
    val s = new Siever()
    for (i <- 1 to limit){
	s ! i
    }
    s ! -1
  }
}


