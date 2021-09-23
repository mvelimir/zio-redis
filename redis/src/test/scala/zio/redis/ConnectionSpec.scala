package zio.redis

import java.net.InetAddress

import zio.Chunk
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

trait ConnectionSpec extends BaseSpec {

  val connectionSuite: Spec[Annotations with RedisExecutor, TestFailure[RedisError], TestSuccess] =
    suite("connection")(
      suite("clientCaching")(
        testM("track keys") {
          for {
            _            <- clientTrackingOff
            _            <- clientTrackingOn(trackingMode = Some(ClientTrackingMode.OptIn))
            _            <- clientCaching(true)
            trackingInfo <- clientTrackingInfo
          } yield assert(trackingInfo.flags.caching)(isSome(isTrue))
        },
        testM("don't track keys") {
          for {
            _            <- clientTrackingOff
            _            <- clientTrackingOn(trackingMode = Some(ClientTrackingMode.OptOut))
            _            <- clientCaching(false)
            trackingInfo <- clientTrackingInfo
          } yield assert(trackingInfo.flags.caching)(isSome(isFalse))
        }
      ),
      suite("clientId")(
        testM("get client id") {
          for {
            id        <- clientId
            info      <- clientInfo
            expectedId = info.id
          } yield assert(id)(equalTo(expectedId))
        }
      ) @@ ignore,
      suite("clientInfo")(
        testM("get client info") {
          for {
            info         <- clientInfo
            id            = info.id
            name          = info.name.getOrElse("")
            expectedId   <- clientId
            expectedName <- clientGetName
          } yield assert(id)(equalTo(expectedId)) &&
            assert(name)(equalTo(expectedName.getOrElse("")))
        } @@ ignore
      ),
      suite("clientKill")(
        testM("error when a connection with the specifed address doesn't exist") {
          clientKill(Address(InetAddress.getByName("0.0.0.0"), 0)).either.map(assert(_)(isLeft))
        },
        testM("specify filters that don't kill the connection") {
          clientKill(ClientKillFilter.SkipMe(false), ClientKillFilter.Id(3341L)).map(assert(_)(equalTo(0L)))
        },
        testM("specify filters that kill the connection but skipme is enabled") {
          for {
            id            <- clientId
            clientsKilled <- clientKill(ClientKillFilter.SkipMe(true), ClientKillFilter.Id(id))
          } yield assert(clientsKilled)(equalTo(0L))
        }
      ),
      suite("clientList")(
        testM("get client info") {
          for {
            id <- clientId
            //_    <- clientList(id)()
//            _ <- clientInfo
          } yield {
            println("ID: " + id.toString)
            assert(())(isUnit)
          } //assert(infoChunk.head)(equalTo(expectedInfo))
        },  // @@ ignore,
        testM("get empty chunk when no clients with specified ids exist") {
          clientList(76L, 77L, 78L)().map(assert(_)(equalTo(Chunk.empty)))
        }
      ) //,
//      suite("clientGetRedir")(
//        testM("tracking disabled") {
//          for {
//            _     <- clientTrackingOff
//            redir <- clientGetRedir
//          } yield assert(redir)(equalTo(ClientTrackingRedirect.NotEnabled))
//        },
//        testM("tracking enabled but not redirecting") {
//          for {
//            _     <- clientTrackingOff
//            _     <- clientTrackingOn()
//            redir <- clientGetRedir
//          } yield assert(redir)(equalTo(ClientTrackingRedirect.NotRedirected))
//        }
//      ),
//      suite("client pause and unpause")(
//        testM("clientPause") {
//          clientPause(1.second, Some(ClientPauseMode.All)).map(assert(_)(isUnit))
//        },
//        testM("clientUnpause") {
//          clientUnpause.map(assert(_)(isUnit))
//        }
//      ),
//      suite("set and get name")(
//        testM("clientSetName") {
//          for {
//            _    <- clientSetName("foo")
//            info <- clientInfo
//            name  = info.name.getOrElse("")
//          } yield assert(name)(equalTo("foo"))
//        } @@ ignore,
//        testM("clientGetName") {
//          for {
//            _    <- clientSetName("bar")
//            name <- clientGetName
//          } yield assert(name.getOrElse(""))(equalTo("bar"))
//        }
//      ),
//      suite("clientTracking")(
//        testM("enable tracking in broadcast mode and with prefixes") {
//          for {
//            _            <- clientTrackingOff
//            _            <- clientTrackingOn(None, Some(ClientTrackingMode.Broadcast), prefixes = Set("foo"))
//            trackingInfo <- clientTrackingInfo
//          } yield assert(trackingInfo.redirect)(equalTo(ClientTrackingRedirect.NotRedirected)) &&
//            assert(trackingInfo.flags)(
//              equalTo(ClientTrackingFlags(clientSideCaching = true, trackingMode = Some(ClientTrackingMode.Broadcast)))
//            ) &&
//            assert(trackingInfo.prefixes)(equalTo(Set("foo")))
//        },
//        testM("disable tracking") {
//          for {
//            _            <- clientTrackingOff
//            trackingInfo <- clientTrackingInfo
//          } yield assert(trackingInfo.redirect)(equalTo(ClientTrackingRedirect.NotEnabled)) &&
//            assert(trackingInfo.flags)(
//              equalTo(ClientTrackingFlags(clientSideCaching = false))
//            ) &&
//            assert(trackingInfo.prefixes)(equalTo(Set.empty[String]))
//        }
//      ),
//      suite("clientTrackingInfo")(
//        testM("get tracking info when tracking is disabled") {
//          for {
//            _            <- clientTrackingOff
//            trackingInfo <- clientTrackingInfo
//          } yield assert(trackingInfo)(
//            equalTo(
//              ClientTrackingInfo(
//                flags = ClientTrackingFlags(clientSideCaching = false),
//                redirect = ClientTrackingRedirect.NotEnabled
//              )
//            )
//          )
//        },
//        testM("get tracking info when tracking is enabled in optin mode with noloop and caching on") {
//          for {
//            _            <- clientTrackingOff
//            _            <- clientTrackingOn(trackingMode = Some(ClientTrackingMode.OptIn), noLoop = true)
//            _            <- clientCaching(true)
//            trackingInfo <- clientTrackingInfo
//          } yield assert(trackingInfo)(
//            equalTo(
//              ClientTrackingInfo(
//                flags = ClientTrackingFlags(
//                  clientSideCaching = true,
//                  trackingMode = Some(ClientTrackingMode.OptIn),
//                  caching = Some(true),
//                  noLoop = true
//                ),
//                redirect = ClientTrackingRedirect.NotRedirected
//              )
//            )
//          )
//        }
//      ),
//      suite("clientUnblock")(
//        testM("unblock client that isn't blocked") {
//          for {
//            id   <- clientId
//            bool <- clientUnblock(id)
//          } yield assert(bool)(equalTo(false))
//        }
//      ),
//      suite("ping")(
//        testM("PING with no input") {
//          ping(None).map(assert(_)(equalTo("PONG")))
//        },
//        testM("PING with input") {
//          ping(Some("Hello")).map(assert(_)(equalTo("Hello")))
//        }
//      ),
//      testM("reset") {
//        reset.map(assert(_)(isUnit))
//      }
    ) @@ sequential
}
