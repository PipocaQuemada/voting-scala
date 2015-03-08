package voting
import com.websudos.phantom.iteratee.Iteratee
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext,Future}

import java.util.UUID

import com.websudos.phantom.Implicits._
import com.datastax.driver.core.{ResultSet, Row}

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
