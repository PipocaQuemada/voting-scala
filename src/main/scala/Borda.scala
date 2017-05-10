package voting
import com.websudos.phantom.iteratee.Iteratee
import scala.collection.mutable.HashMap
import scala.concurrent.{ExecutionContext,Future}

import java.util.UUID
import java.net.URLDecoder

import com.websudos.phantom.Implicits._
import com.datastax.driver.core.{ResultSet, Row}


class BordaCount extends ElectionSystem {
  def votesToResults( votes: Seq[CondorcetModel]) : scala.collection.mutable.HashMap[String, Int] = {
    val voteAccumulator = new scala.collection.mutable.HashMap[String, Int]
    votes.foreach( v => {
        val prefs = v.vote.split(">") 
        for (i <- 0 until prefs.length - 1) {
          val candidate = prefs(i)
          val numLess = prefs.length - (i + 1)
          voteAccumulator += (( candidate
                              , numLess + voteAccumulator.get(candidate).getOrElse(0)))
        }
      })
    voteAccumulator
  }
  override def findWinner(election: String ) : Future[String] = {
    Condorcet.getVotes(election)
      .map( votesToResults _ )
      .map( voteAcc => if(voteAcc.isEmpty) "No votes cast!"
                       else voteAcc.maxBy( _._2 )._1) 
  }
  override def vote(electionName: String, vote: String) = { 
    Condorcet.insertRecord( CondorcetModel( UUID.randomUUID
                                          , electionName
                                          , URLDecoder.decode(vote, "UTF-8"))) // convert %3e to >
             .map( resultSet => Seq( resultSet )) // massage return type
  }
}
