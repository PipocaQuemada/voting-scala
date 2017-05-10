package voting
import com.websudos.phantom.iteratee.Iteratee
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext,Future}

import java.util.UUID

import com.websudos.phantom.Implicits._
import com.datastax.driver.core.{ResultSet, Row}

case class CondorcetModel (id: UUID, election: String, vote: String)
sealed class Condorcet extends CassandraTable[Condorcet, CondorcetModel] {
  object voteId extends UUIDColumn(this) with PrimaryKey[UUID]
  object election extends StringColumn(this) with PrimaryKey[String]
  object vote extends StringColumn(this)

  override def fromRow(row: Row): CondorcetModel = {
    CondorcetModel(voteId(row), election(row), vote(row) )
  }
}

object Condorcet extends Condorcet with VotingConnector {
  def insertRecord(a: CondorcetModel) = 
    insert.value( _.voteId , a.id )
          .value( _.election, a.election)
          .value( _.vote, a.vote)  
          .future

  def getVotes( election: String): Future[Seq[CondorcetModel]] = 
    select.where(_.election eqs election).fetchEnumerator() run Iteratee.collect() 
}


case class CondorcetCandidateModel (election: String, candidate: String)
sealed class CondorcetCandidate extends CassandraTable[CondorcetCandidate, CondorcetCandidateModel] {
  object election extends StringColumn(this) with PrimaryKey[String]
  object candidate extends StringColumn(this)

  override def fromRow(row: Row): CondorcetCandidateModel = {
    CondorcetCandidateModel(election(row), candidate(row) )
  }
}

object CondorcetCandidate extends CondorcetCandidate with VotingConnector {
  def insertRecord(a: CondorcetCandidateModel) = 
    insert.value( _.election, a.election)
          .value( _.candidate, a.candidate)  
          .future

  def getVotes( election: String): Future[Seq[CondorcetCandidateModel]] = 
    select.where(_.election eqs election).fetchEnumerator() run Iteratee.collect() 
}

