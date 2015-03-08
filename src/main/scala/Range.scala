package voting
import com.websudos.phantom.iteratee.Iteratee
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext,Future}

import java.util.UUID

import com.websudos.phantom.Implicits._
import com.datastax.driver.core.{ResultSet, Row}

//import Predef.{augmentString => _, refArrayOps => _, _}

case class RangeModel (id: UUID, election: String, amount: Int, candidate: String)
sealed class Range extends CassandraTable[Range, RangeModel] {
  object voteId extends UUIDColumn(this) with PrimaryKey[UUID]
  object election extends StringColumn(this) with PrimaryKey[String]
  object amount extends IntColumn(this)
  object candidate extends StringColumn(this)

  override def fromRow(row: Row): RangeModel = {
    RangeModel(voteId(row), election(row), amount(row), candidate(row) )
  }
}

object Range extends Range with VotingConnector {
  def insertRecord(a: RangeModel) = 
    insert.value( _.voteId , a.id )
          .value( _.election, a.election)
          .value( _.amount, a.amount)  
          .value( _.candidate, a.candidate)  
          .future

  def getVotes( election: String): Future[Seq[RangeModel]] = 
    select.where(_.election eqs election).fetchEnumerator() run Iteratee.collect() 
}

class RangeVoting extends ElectionSystem {
  override def findWinner(election: String ) : Future[String] = {
    val voteAccumulator = new scala.collection.mutable.HashMap[String, Int]
    Range.getVotes(election)
      .map( _.foreach( vote => 
        voteAccumulator += (( vote.candidate
                            , vote.amount + voteAccumulator.get(vote.candidate)
                                                 .getOrElse(0)))))
      .map( _ => if(voteAccumulator.isEmpty) "No votes cast!"
                 else voteAccumulator.maxBy( _._2 )._1) 
  }
  override def vote(electionName: String, vote: String) = { 
    // todo: return better error messages
    val votes = vote.split(",")
    val voteAmounts = refArrayOps(votes).map(_.split(";"))
    // parse to an int; restrict to range 0-100
    def amountify(s: String) = {
      val amount = augmentString(s).toInt
      amount match {
        case toLow if amount < 0 => 0
        case toHigh if amount > 100 => 100
        case _ => amount 
      }
    }
    Future.sequence( 
      refArrayOps(voteAmounts).map( v => Range.insertRecord( RangeModel( UUID.randomUUID
                                                                             , electionName
                                                                             , amountify(v(1))
                                                                             , v(0)))))
  }
}
