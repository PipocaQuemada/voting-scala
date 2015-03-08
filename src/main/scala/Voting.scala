package voting

import unfiltered.request._
import unfiltered.response._
import unfiltered.netty._
import com.datastax.driver.core.{ResultSet, Row}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.connectors.SimpleCassandraConnector
import com.websudos.phantom.iteratee.Iteratee

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
  def getElectionSystem(electionSystem: String) = electionSystem match {
    case "approval" => approval
  }
}

case class ApprovalModel (id: UUID, election: String, candidate: String)
sealed class Approval extends CassandraTable[Approval, ApprovalModel] {
  object voteId extends UUIDColumn(this) with PrimaryKey[UUID]
  object election extends StringColumn(this) with PrimaryKey[String]
  object candidate extends StringColumn(this)

  override def fromRow(row: Row): ApprovalModel = {
    ApprovalModel(voteId(row), election(row), candidate(row) )
  }
}

object Approval extends Approval with VotingConnector {
  def insertRecord(a: ApprovalModel) = 
    insert.value( _.voteId , a.id )
          .value( _.election, a.election)
          .value( _.candidate, a.candidate)  
          .future

  def getVotes( election: String): Future[Seq[ApprovalModel]] = 
    select.where(_.election eqs election).fetchEnumerator() run Iteratee.collect() 
}

class ApprovalVoting extends ElectionSystem {
  override def findWinner(election: String ) : Future[String] = {
    val voteAccumulator = new scala.collection.mutable.HashMap[String, Int]
    Approval.getVotes(election)
      .map( _.foreach( vote => 
        voteAccumulator += (( vote.candidate
                            , 1 + voteAccumulator.get(vote.candidate)
                                                 .getOrElse(0)))))
      .map( _ => if(voteAccumulator.isEmpty) "No votes cast!"
                 else voteAccumulator.maxBy( _._2 )._1) 
  }
  override def vote(electionName: String, vote: String) = { 
    val votes = vote.split(",")
    Future.sequence( 
      refArrayOps(votes).map( v => Approval.insertRecord( ApprovalModel( UUID.randomUUID
                                                          , electionName
                                                          , v))))
  }
}
