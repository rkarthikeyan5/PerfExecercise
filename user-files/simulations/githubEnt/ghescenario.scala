package ghe
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


class ghescenario extends Simulation {


	val httpConf = http
		.baseURL("http://52.88.81.144")
		.inferHtmlResources(BlackList(""".*\.js""", """.*\.css""", """.*\.gif""", """.*\.jpeg""", """.*\.jpg""", """.*\.ico""", """.*\.woff""", """.*\.(t|o)tf""", """.*\.png""", """.*\.map""", """aura""", """resource"""), WhiteList())
		.acceptHeader("image/png,image/*;q=0.8,*/*;q=0.5")
		.acceptEncodingHeader("gzip, deflate")
		.acceptLanguageHeader("en-US,en;q=0.5")
		.connection("keep-alive")
		.contentTypeHeader("application/x-www-form-urlencoded; charset=UTF-8")
		.userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.10; rv:41.0) Gecko/20100101 Firefox/41.0")

	
	//CPR - Comment on Pull Req
	//CM - Commit to Master
	//CBCP - Create Branch and create a pull req
	//DR - Download Repo

	val rampUpTimeSecs = Integer.getInteger("rampUpTimeSecs", 1)

	val noofCPRusers = Integer.getInteger("noofCPRusers", 1)
	val noofCMusers = Integer.getInteger("noofCMusers", 1)
	val noofCBCPusers = Integer.getInteger("noofCBCPusers", 1)
	val noofDRusers = Integer.getInteger("noofDRusers", 1)

	val maxDurationSecs = Integer.getInteger("maxDurationSecs", 1)


	setUp(

		CommentOnPullReqSimulation.scn.inject(rampUsers(noofCPRusers) over (rampUpTimeSecs seconds)).protocols(httpConf),
		CommitToMasterSimulation.scn.inject(rampUsers(noofCMusers) over (rampUpTimeSecs seconds)).protocols(httpConf),
		CreateBranchCreatePullReqSimulation.scn.inject(rampUsers(noofCBCPusers) over (rampUpTimeSecs seconds)).protocols(httpConf),
		DownloadRepoSimulation.scn.inject(rampUsers(noofDRusers) over (rampUpTimeSecs seconds)).protocols(httpConf)).maxDuration(maxDurationSecs seconds)

	    

	    }

