<p><b>Motivation</b>.</p>

The following is a very high level test strategy and approach that discusses how the given user flows can be tested to analyze the performance, scalability and reliability of the system under test


<p><b>Assumptions</b>.</p>

The plan deliberately does not say the user load and it is kept generic for whatever it is assumed as a peak user utilization of the system
User Types are also not differentiated. Even though several user types were given, all of them are kept to emulate the same flows
Admin operation here assumed to be out of scope
Distribution of load among user flows is not defined and the scenario will provide the option to specify the value as a start up argument
Accepted SLAs (FE and BE) are not discussed
Frontend Execution Strategy The idea would be to measure the different times based on the navigation timing api. Some key examples would be TTFB, Render Start, DomComplete etc., We can use a front end profiling tool like Web Page Test, or some vendor solutions like catchpoint to simulate the enterprise users from different geographic areas to understand the end user impact.

A sample load of the org for the first time user is below - http://www.webpagetest.org/result/151020_FQ_ACG/ The film strip view - http://www.webpagetest.org/video/compare.php?tests=151020_FQ_ACG-r:1-c:0

We also have to test for Single Point of Failures (SPOF) typically more appropriate for a customer facing site. The idea is to make sure that no single request should single handedly distrust the overall user experience (increase the time to pain the page etc.,).

<p><b>Backend Execution Strategy</b>.</p>

<p><b>Baseline Scenario</b>.</p>
We would want to collect the system response and behavior under normal load. A normal load would be presumed as some % of the actual peak load, if known.

<p><b>Stress Scenario</b>.</p>
This is where we would stress the system (either with more users or with the same users with reduced wait time between the simulated requests) to 2X / 3X the accepted peak load.

<p><b>Increasing User / Breakpoint Scenario</b>.</p>
A test where a step-up scenario is performed till a break point is observed either at the system resource or some contentions that might not have caught during the previous tests.

<p><b>Longevity Scenario</b>.</p>
Typically running for 48 hours under baseline load to mimic a possible memory contention that our short timed tests would not catch.

<p><b>Spike Scenario</b>.</p>
A test to emulate a sudden burst in request, something similar to emulating a release time pull requests every day/week or the likes.

<p><b>LDAP Stress Scenario</b>.</p>
If this has not been separately tested, would be good to test bind, search, connect and other specific transactions.

<p><b>Failover Scenario (Each Component)</b>.</p>
Simulating the what-if scenarios. What if puma stops responding in half the servers. What impact does redis failure have on the end user. What happens to the DB response times when memcache fails. What is the response time impact

<p><b>Profiling Strategy </b>.</p> 

Assuming we find one user flow to show high response times what what is expected, then we dig into breaking down the response times across the layers. If the resulting analysis points us to the code, then we remodel the useless to focus on that particular problem transaction to find out how the code breaks down through a APM solution of using a standard profiling tool.

<p><b>Tools </b>.</p>

<p><b>Front End Performance </b>.</p>
Web page test, Catchpoint, Gomez, RUM data from New Relic, Google Page Speed, Chrome Dev Tools etc.,

<p><b>Back End Performance </b>.</p>
APM for Rails/PHP/JAVA profiling, Server monitors, openTSDB/Graphite etc., icinga/Nagios for alerting

<p><b>Load Simulation </b>.</p>
Gatling (used here). Other options include Grinder(Python), Jmeter(Java) and other open source tools.

<p><b>Invoking the scenario </b>.</p>

The following start command will run 1 user per workflow for 1200 seconds with a rampup of 100 seconds with a max duration (hard stop) of around 1300 seconds.  We can change the parameters with the think time to simulate the other scenarios that we have mentioned earlier.  

JAVA_OPTS="-DtestDuration=1200 -DrampUpTimeSecs=100 -DnoofCPRusers=1 -DnoofCMusers=1 -DnoofCBCPusers=1 -DnoofDRusers=1 -DmaxDurationSecs=1300" ./gatling.sh -s ghe.ghescenario

<p><b>Sample Test Results </b>.</p>

View it from the results folder - ../ghe_perf/results/ghescenario-1445322963360/index.html

<p><b>Folder Tree Details </b>.</p>

Userflow simulation files - /user-files/simulations/githubEnt
Scenario Definition file -  /user-files/simulations/githubEnt/ghescenario.scala
Header file - /user-files/simulations/githubEnt/gheheaders.scala
Data file - /user-files/data/userLogins.csv
Results file - /results/ghescenario-1445322963360
