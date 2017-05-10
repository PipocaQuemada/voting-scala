package voting

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.datastax.driver.core.{ResultSet, Row}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.connectors.SimpleCassandraConnector

import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext,Future}

import java.util.UUID

trait VotingConnector extends SimpleCassandraConnector {
  def keySpace = "voting"
}

/** 
 *  Records votes, and calculates election winners
 *  */
@io.netty.channel.ChannelHandler.Sharable
object Voting extends future.Plan
  with ServerErrorResponse {
  implicit def executionContext = ExecutionContext.Implicits.global
  
  def intent = {
    case req @ GET(Path( Seg( electionSystem :: election :: "result" :: Nil ) )) => 
      ElectionSystem.getElectionSystem(electionSystem)
                    .findWinner(election)
                    .map( winner => ResponseString(winner) )
    case req @ POST(Path( Seg( electionSystem :: election :: "cast" :: vote :: Nil) )) => {
      ElectionSystem.getElectionSystem(electionSystem)
                    .vote(election, vote)
                    .map( _ => ResponseString("vote cast"))
    }
  }
  def view(electionSystem: String, election: String) = {
  }

}

abstract class ElectionSystem {
  def findWinner(election: String ) : Future[String]
  def vote( election: String, vote: String ) : Future[Seq[ResultSet]]
}

object ElectionSystem {
  val approval = new ApprovalVoting()
  val range = new RangeVoting()
  val borda = new BordaCount()
  def getElectionSystem(electionSystem: String) = electionSystem match {
    case "approval" => approval
    case "range" => range
    case "borda" => borda
  }
}


