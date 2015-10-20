package ghe
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import gheheaders._

object CommitToMasterSimulation {

	val rnd = new scala.util.Random
	val formatter = "ddMMyyyyhh:mm:ss"
	val testDuration = Integer.getInteger("testDuration", 1)
	val minWaitMs      = 10000 milliseconds
  	val maxWaitMs      = 30000 milliseconds

	val scn = scenario("RecordedSimulation")


		.during(testDuration) {
		feed(csv("userLogins.csv").random)
		.pause(minWaitMs, maxWaitMs)
		.exec(http("HomePage")
			.get("/")
			.check(regex("""authenticity_token\" type=\"hidden\" value=\"(.+)\" \/><""").find.saveAs("""authenticity_token""")))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("Login_To_GHE")
			.post("/session")
			.headers(headers_CTM_0)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authenticity_token}")
			.formParam("login", "${user_name}")
			.formParam("password", "${password}")
			.formParam("return_to", "http://52.88.81.144/"))

		.pause(minWaitMs, maxWaitMs)

		.exec(http("Load_Org_Repo")
			.get("/first-org/docker")
			.headers(headers_CTM_1)
			.check(regex(""""\/first-org\/docker\/tree\/master\/(\w+)" class""").findAll.saveAs("""folder"""))
			.resources(http("Load_Org_Repo_1")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list"),
            http("Load_Org_Repo_2")
			.get("/first-org/docker/issues/counts")
			.headers(headers_CTM_4)))

		.exec((session: Session) => { // use a simple action
				val folder = session("folder").as[List[String]] // retrieve the keywords saved by the previous check
				session.set("folder_random", folder(math.max(rnd.nextInt(folder.length), 0))) // store in the session a keywork randomly picked
			})


		.exec( session => {
		  println( "Value of folder_random is------------------------------------------>------------------------------------------>:" )
		  println( session( "folder_random" ).as[String] )
		  session
		})
		
		.pause(minWaitMs, maxWaitMs)


		.exec(http("Load_Repo_Folder")
			//replace folder builder with randome folder  
			.get("/first-org/docker/tree/master/${folder_random}?_pjax=%23js-repo-pjax-container")
			.headers(headers_CTM_5)
			.check(regex(""""authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s\s\s\s\s\s<button class="btn-link tooltipped tooltipped-e""").find.saveAs("""pjax_authtoken"""))
			.check(regex("""\/first-org\/docker\/tree-commit\/(.+)\/""").find.saveAs("""commit_key"""))
			.resources(http("Load_Repo_Folder_1")
			.get("/first-org/docker/tree-commit/${commit_key}/${folder_random}"),
            http("Load_Repo_Folder_2")
			.get("/first-org/docker/file-list/master/${folder_random}")))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("Click_Create_File")
			.post("/first-org/docker/new/master/${folder_random}")
			.headers(headers_CTM_0)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${pjax_authtoken}")
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s\s\s\s\s<div class=\"breadcrumb js-zeroclipboard-container\">""").find.saveAs("""create_authtoken"""))
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n          <button class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken")))

		.pause(minWaitMs, maxWaitMs)
		.exec(http("Create_File_And_Commit")
			.post("/first-org/docker/create/master/${folder_random}")
			.headers(headers_CTM_0)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${create_authtoken}")
			//replace the name of the file with session => formatter.format(now())  20151015
			.formParam("filename", System.currentTimeMillis + ".txt")
			.formParam("new_filename", "${folder_random}/" + System.currentTimeMillis + ".txt")
			.formParam("commit", "${commit_key}")
			.formParam("same_repo", "1")
			.formParam("pr", "")
			.formParam("content_changed", "true")
			.formParam("value", "Commit Test ")
			.formParam("message", "Perf Excercise _ Commit To Master")
			.formParam("placeholder_message", "Create " + System.currentTimeMillis + ".txt")
			.formParam("description", "")
			.formParam("commit-choice", "direct")
			.formParam("target_branch", "master")
			.formParam("quick_pull", "")
			.resources(http("Create_File_And_Commit_1")
			.get("/first-org/docker/tree-commit/${commit_key}/${folder_random}"),
            http("Create_File_And_Commit_2")
			.get("/first-org/docker/file-list/master/${folder_random}")))


		.pause(minWaitMs, maxWaitMs)


		.exec(http("request_14")
			.get("/first-org/docker?_pjax=%23js-repo-pjax-container")
			.headers(headers_CTM_5)
			.resources(http("request_15")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list"),
            http("request_16")
			.get("/first-org/docker/tree-commit/${commit_key}"),
            http("request_18")
			.get("/first-org/docker/issues/counts")
			.headers(headers_CTM_4)))

		.pause(minWaitMs, maxWaitMs)
		.exec(http("Logout")
			.post("/logout")
			.headers(headers_CTM_0)
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${logout_authtoken}"))
	}

}