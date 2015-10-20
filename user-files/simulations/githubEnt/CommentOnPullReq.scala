
package ghe
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import gheheaders._

object CommentOnPullReqSimulation {


	val testDuration = Integer.getInteger("testDuration", 1)
	val minWaitMs      = 10000 milliseconds
  	val maxWaitMs      = 30000 milliseconds


	val scn = scenario("CommentOnPullReq")
		// homepage

		.during(testDuration) {

		feed(csv("userLogins.csv").random)
		.pause(minWaitMs, maxWaitMs)
		.exec(http("CPR_LaunchPage")
			.get("/")
			.headers(headers_1)
			.check(regex("""authenticity_token\" type=\"hidden\" value=\"(.+)\" \/><""").find.saveAs("""authenticity_token""")))



		.pause(minWaitMs, maxWaitMs)
		// login
		.exec(http("CPR_Login")
			.post("/session")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authenticity_token}")
			.formParam("login", "${user_name}")
			.formParam("password", "${password}")
			.formParam("return_to", "http://52.88.81.144/"))
		


		.pause(minWaitMs, maxWaitMs)
		// click on main repo
		.exec(http("CPR_SelectMainRepo")
			.get("/first-org/docker")
			.headers(headers_1)
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n          <button class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken"))
			.resources(http("request_5")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_5),
            http("CPR_SelectMainRepo_1")
			.get("/first-org/docker/issues/counts")
			.headers(headers_6)))
		


		.pause(minWaitMs, maxWaitMs)


		// click on pull requests
		.exec(http("CPR_ClickonPullReq")
			.get("/first-org/docker/pulls?_pjax=%23js-repo-pjax-container")
			.headers(headers_7)
			.check(regex("""<div class="table-list-cell issue-title">\n\n\n    <a href="\/first-org\/docker\/pull\/(\d+)" class""").findAll.saveAs("""Random_Pull_Req"""))
			.resources(http("CPR_ClickonPullReq_1")
			.get("/first-org/docker/issues/show_menu_content?partial=issues%2Ffilters%2Fassigns_content&q=is%3Apr+is%3Aopen")
			.headers(headers_8)))
		

		.pause(minWaitMs, maxWaitMs)


		// select an existing pull req <from a list>
		.exec(http("CPR_SelectExistingPullREq")
			.get("/first-org/docker/pull/${Random_Pull_Req.random()}?_pjax=%23js-repo-pjax-container")
			.headers(headers_7)
			.check(regex("""authenticity_token" type="hidden" value="(.+)" \/><\/div>\n    <div class="timeline-comment">""").find.saveAs("""Comment_auth_token"""))
			.resources(http("request_42")
			.get("/notifications/header")
			.headers(headers_8)))
		

		.pause(minWaitMs, maxWaitMs)


		// Write a comment and save
		.exec(http("CPR_SubmitCommentonPR")
			.get("/first-org/docker/suggestions/pull_request/${Random_Pull_Req.random()}")
			.headers(headers_43))
		

		.pause(minWaitMs, maxWaitMs)


		.exec(http("CPR_SubmitCommentonPR_1")
			.get("/notifications/header")
			.headers(headers_44)
			.resources(http("request_45")
			.get("/first-org/docker/pull/${Random_Pull_Req.random()}/show_partial?partial=pull_requests%2Ftitle")
			.headers(headers_44),
            http("request_46")
			.post("/first-org/docker/pull/${Random_Pull_Req.random()}/comment")
			.headers(headers_46)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${Comment_auth_token}")
			.formParam("issue", "${Random_Pull_Req.random()}")
			.formParam("comment[body]", "Comment2" + System.currentTimeMillis),
            http("request_47")
			.get("/notifications/thread_subscription?repository_id=4&thread_class=Issue&thread_id=8")
			.headers(headers_44)))
		

		.pause(minWaitMs, maxWaitMs)


		// logout
		.exec(http("CPR_Logout")
			.post("/logout")
			.headers(headers_1)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${logout_authtoken}"))

	}
}