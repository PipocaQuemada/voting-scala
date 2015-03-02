package voting

import unfiltered.request._
import unfiltered.response._

import unfiltered.netty._

import scala.concurrent.{ExecutionContext,Future}

/** 
 *  Records votes, and calculates election winners
 *  */
@io.netty.channel.ChannelHandler.Sharable
object Voting extends future.Plan
  with ServerErrorResponse {
  implicit def executionContext = ExecutionContext.Implicits.global
  
  val data : HashMap[String, HashMap[String, Int]] = HashMap()
  
  def intent = {
    case req @ GET(Path( Seg( election :: "result" :: Nil ) )) => 
      Future.successful( view(election) )
      
    case req @ POST(Path( Seg(election :: "cast" :: candidate :: Nil) )) => {
      Future.successful(ResponseString("vote cast"))
    }
  }
  def view(election: String) = {
       "The current winner is:" + findWinner(election)
   )
  }

 def findWinner(election: String): String = {
   "me"
 }
}
