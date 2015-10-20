package ghe
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import gheheaders._

object DownloadRepoSimulation {

	val testDuration = Integer.getInteger("testDuration", 1)
	val minWaitMs      = 10000 milliseconds
  	val maxWaitMs      = 30000 milliseconds

	val scn = scenario("DownloadRepo")


		.during(testDuration) {

		feed(csv("userLogins.csv").random)
		.pause(minWaitMs, maxWaitMs)

		.exec(http("DR_Home")
			.get("/")
			.headers(headers_1)
			.check(regex("""authenticity_token\" type=\"hidden\" value=\"(.+)\" \/><""").find.saveAs("""authenticity_token""")))

		.pause(minWaitMs, maxWaitMs)
		.exec(http("DR_Login")
			.post("/session")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authenticity_token}")
			.formParam("login", "${user_name}")
			.formParam("password", "${password}")
			.formParam("return_to", "http://52.88.81.144/first-org/rails")
			.resources(http("request_1")
			.get("/first-org/rails/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_DR_1),
            http("request_2")
			.get("/first-org/rails/issues/counts")
			.headers(headers_DR_2)))
		
		.pause(minWaitMs, maxWaitMs)
		.exec(http("DR_LaunchOrg")
			.get("/first-org")
			.check(regex(""""repo-list-name">\n      <a href="\/first-org\/(.+)" itemprop""").findAll.saveAs("""Random_Repo"""))
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n          <button class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken"))
			)
		

		.pause(minWaitMs, maxWaitMs)
		.exec(http("DR_Select_Random_Repo")
			.get("/first-org/${Random_Repo.random()}")
			.resources(http("request_10")
			.get("/first-org/${Random_Repo.random()}/issues/counts")
			.headers(headers_DR_2),
            http("DR_Select_Random_Repo_1")
			.get("/first-org/${Random_Repo.random()}/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_DR_1)))
		
		.pause(minWaitMs, maxWaitMs)
		.exec(http("DR_Download_Repo")
			.get("/first-org/${Random_Repo.random()}/archive/master.zip"))
		
		.pause(minWaitMs, maxWaitMs)
		.exec(http("DR_Logout")
			.post("/logout")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${logout_authtoken}"))
	}

}