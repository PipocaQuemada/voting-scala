package voting

import org.scalacheck._
import org.scalacheck.Prop.forAll

import java.util.UUID

// TODO: add Gen for ApprovalModel, etc.
final case class TestVoting() extends Properties("Voting") {

  property("approval one candidate election works") = Prop forAll { (candidate: String) => 
    (new ApprovalVoting).votesToResults(Seq(ApprovalModel( UUID.randomUUID, "foo", candidate)))
                        .maxBy( _._2)._1 == candidate
  }
  property("approval two candidate election works") = Prop forAll { (candidate: String, candidate2: String) => 
    (new ApprovalVoting).votesToResults(Seq( ApprovalModel( UUID.randomUUID, "foo", candidate)
                                           , ApprovalModel( UUID.randomUUID, "foo", candidate2)
                                           , ApprovalModel( UUID.randomUUID, "foo", candidate2)
                                           , ApprovalModel( UUID.randomUUID, "foo", candidate)
                                           , ApprovalModel( UUID.randomUUID, "foo", candidate)
                                           ))
                        .maxBy( _._2)._1 == candidate
  }
  property("range one candidate election works") = Prop forAll { (candidate: String) => 
    (new RangeVoting).votesToResults(Seq(RangeModel( UUID.randomUUID, "foo", 100, candidate)))
                        .maxBy( _._2)._1 == candidate
  }
  property("range two candidate election works") = Prop forAll { (candidate: String, candidate2: String) => 
    (new RangeVoting).votesToResults(Seq( RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 100, candidate2)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        , RangeModel( UUID.randomUUID, "foo", 10, candidate)
                                        ))
                        .maxBy( _._2)._1 == candidate
  }
}
