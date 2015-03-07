package voting 

/** embedded server */
object Server {
  def main(args: Array[String]) {
    unfiltered.netty.Server.http(8080)
      .handler(Voting)
      .run { s =>
        unfiltered.util.Browser.open(s.portBindings.head.url + "/approval/dinner/result")
      }
    dispatch.Http.shutdown()
  }
}
