package ghe
import scala.concurrent.duration._

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.jdbc.Predef._
import scala.util.Random
import gheheaders._

object CreateBranchCreatePullReqSimulation {

  	val rnd = new scala.util.Random
  	val range = 0 to 99999
  	val rand_gen = (range(rnd.nextInt(range length)))
	val testDuration = Integer.getInteger("testDuration", 1)
	val minWaitMs      = 10000 milliseconds
  	val maxWaitMs      = 30000 milliseconds



	val scn = scenario("CreateBranchCreatePullReq")


		.during(testDuration) {

		feed(csv("userLogins.csv").random)
		.pause(minWaitMs, maxWaitMs)
		
		.exec(http("CBCP_Launch")
			.get("/")
			.check(regex("""authenticity_token\" type=\"hidden\" value=\"(.+)\" \/><""").find.saveAs("""authenticity_token""")))
		.pause(minWaitMs, maxWaitMs)
		// click login
		.exec(http("CBCP_Login")
			.post("/session")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${authenticity_token}")
			.formParam("login", "${user_name}")
			.formParam("password", "${password}")
			.formParam("return_to", "http://52.88.81.144/"))
		.pause(minWaitMs, maxWaitMs)
		// select docker repo
		.exec(http("CBCP_LoadOrg")
			.get("/first-org/docker")
			.check(regex(""""\/first-org\/docker\/tree\/master\/(\w+)" class""").findAll.saveAs("""folder"""))
			.check(regex("""authenticity_token" type="hidden" value="(.+)" \/><\/div>\n            <span class="octicon octicon-git-branc""").find.saveAs("""branch_token"""))
			.resources(http("request_3")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_CBCP_3),
            http("CBCP_LoadOrg_1")
			.get("/first-org/docker/issues/counts")
			.headers(headers_CBCP_4)))


		.exec((session: Session) => { 
				val folder = session("folder").as[List[String]] 
				session.set("folder_random", folder(math.max(rnd.nextInt(folder.length), 0))) // store in the session a keywork randomly picked
			})
		.pause(minWaitMs, maxWaitMs)
		// click on master to create a branch
		// hit create new branch
		.exec(http("CBCP_CreateNewBranch")
			.post("/first-org/docker/branches")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${branch_token}")
			.formParam("name", "NewBranch_PerfExecercise" + rand_gen)
			.formParam("branch", "master")
			.formParam("path", "")
			.resources(http("CBCP_CreateNewBranch_1")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_CBCP_3),
            http("CBCP_CreateNewBranch_2")
			.get("/first-org/docker/issues/counts")
			.headers(headers_CBCP_4)))


		.pause(minWaitMs, maxWaitMs)

		.exec(http("CBCP_RecentlyTouchedBranches")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_CBCP_10))

		.pause(minWaitMs, maxWaitMs)
		// click on a folder inside the branch repo

		.exec(http("CBCP_Load_Repo_Folder")
			//replace folder builder with randome folder  
			.get("/first-org/docker/tree/NewBranch_PerfExecercise" + rand_gen + "/${folder_random}?_pjax=%23js-repo-pjax-container")
			.headers(headers_CBCP_5)
			.check(regex(""""authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s\s\s\s\s\s<button class="btn-link tooltipped tooltipped-e""").find.saveAs("""pjax_authtoken"""))
			.check(regex("""\/first-org\/docker\/tree-commit\/(.+)\/""").find.saveAs("""commit_key"""))
			)





		.pause(minWaitMs, maxWaitMs)
		// create a new file inside the folder
		.exec(http("CBCP_CreateNewFile")
			.post("/first-org/docker/new/NewBranch_PerfExecercise" + rand_gen + "/${folder_random}")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${pjax_authtoken}")
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n\s\s\s\s\s<div class=\"breadcrumb js-zeroclipboard-container\">""").find.saveAs("""create_authtoken"""))
			.check(regex("""name="authenticity_token" type="hidden" value="(.+)" \/><\/div>\n          <button class="dropdown-item dropdown-signout""").find.saveAs("logout_authtoken")))


		.pause(minWaitMs, maxWaitMs)
		// Actual Create File and Commit
		.exec(http("CBCP_CreateNewFile_CommitFile")
			.post("/first-org/docker/create/NewBranch_PerfExecercise" + rand_gen + "/${folder_random}")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${create_authtoken}")
			.formParam("filename", "NewPerfExecerFile" + rand_gen)
			.formParam("new_filename", "${folder_random}/NewPerfExecerFile" + rand_gen)
			.formParam("commit", "${commit_key}")
			.formParam("same_repo", "1")
			.formParam("pr", "")
			.formParam("content_changed", "false")
			.formParam("value", "")
			.formParam("message", "Create NewPerfExecFile")
			.formParam("placeholder_message", "Create NewPerfExecerFile" + rand_gen)
			.formParam("description", "")
			.formParam("commit-choice", "direct")
			.formParam("target_branch", "NewBranch_PerfExecercise" + rand_gen)
			.formParam("quick_pull", "")
			.resources(http("request_17")
			.get("/first-org/docker/tree-commit/${commit_key}/${folder_random}")
			.headers(headers_CBCP_3),
            http("CBCP_CreateNewFile_CommitFile_1")
			.get("/first-org/docker/file-list/NewBranch_PerfExecercise"+ rand_gen + "/${folder_random}")
			.headers(headers_CBCP_3)))


		.pause(minWaitMs, maxWaitMs)
		// Click on pull REq
		.exec(http("CBCP_ClickPullReq")
			.get("/first-org/docker/pulls?_pjax=%23js-repo-pjax-container")
			.headers(headers_CBCP_19))


		.pause(minWaitMs, maxWaitMs)
		// Click Create New Pull Req
		.exec(http("CBCP_CreatePullReq")
			.get("/first-org/docker/compare/?_pjax=%23js-repo-pjax-container")
			.headers(headers_CBCP_19))

		.pause(minWaitMs, maxWaitMs)
		// Select branch that was just created in the previous steps

		.exec(http("CBCP_ClickCompareBranch")
			.get("/first-org/docker/compare/master...NewBranch_PerfExecercise" + rand_gen)
			.check(regex("""authenticity_token" type="hidden" value="(.+)" \/><\/div>\n    <div class="discussion-timeline""").find.saveAs("""pr_auth_token"""))
			.resources(http("CBCP_ClickCompareBranch_1")
			.get("/first-org/docker/compare/master...NewBranch_PerfExecercise" + rand_gen),
            http("CBCP_ClickCompareBranch_2")
			.get("/first-org/docker/branches/pre_mergeable/master...NewBranch_PerfExecercise" + rand_gen)))


		.pause(minWaitMs, maxWaitMs)
		// Click on Create Pull REq
		.exec((http("CBCP_ClickCreatePullReq")
			.get("/first-org/docker/pulls?_pjax=%23js-repo-pjax-container")))

		.pause(minWaitMs, maxWaitMs)
		// Enter comment and hit Create Pull Req
		.exec(http("CBCP_EnterComment_ClickCreatePullReq")
			.get("/first-org/docker/suggestions/pull_request")
			.headers(headers_CBCP_25))


		.pause(minWaitMs, maxWaitMs)
		.exec(http("CBCP_SubmitPullReq")
			.post("/first-org/docker/pull/create")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${pr_auth_token}")
			.formParam("pull_request[title]", "Create NewPerfExecFile")
			.formParam("pull_request[body]", "Comment1")
			.formParam("quick_pull", "")
			.formParam("base", "first-org:master")
			.formParam("head", "first-org:NewBranch_PerfExecercise" + rand_gen)
			.resources(http("CBCP_SubmitPullReq_1")
			.get("/first-org/docker/pull/6/merge-button")))


		.pause(minWaitMs, maxWaitMs)
		// Click on main Repo
		.exec(http("CBCP_SelectMainRepo")
			.get("/first-org/docker?_pjax=%23js-repo-pjax-container")
			.headers(headers_CBCP_19)
			.resources(http("CBCP_SelectMainRepo_1")
			.get("/first-org/docker/issues/counts")
			.headers(headers_CBCP_4),
            http("CBCP_SelectMainRepo_2")
			.get("/first-org/docker/show_partial?partial=tree%2Frecently_touched_branches_list")
			.headers(headers_CBCP_3)))


		.pause(minWaitMs, maxWaitMs)
		// From master click on pull req
		.exec(http("CBCP_FromMaster_ClickPullReq")
			.get("/first-org/docker/pulls?_pjax=%23js-repo-pjax-container")
			.headers(headers_CBCP_19))


		.pause(minWaitMs, maxWaitMs)
		// use this step to verify a new pull req is created
		// logout
		.exec(http("CBCP_Logout")
			.post("/logout")
			.formParam("utf8", "✓")
			.formParam("authenticity_token", "${logout_authtoken}"))

	}
}