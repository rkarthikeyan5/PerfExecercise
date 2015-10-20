Motivation
The following is a very high level test strategy and approach that discusses how the given user flows can be tested to analyze the performance, scalability and reliability of the system under test

Assumptions

- The plan deliberately does not say the user load and it is kept generic for whatever it is assumed as a peak user utilization of the system
- User Types are also not differentiated.  Even though several user types were given, all of them are kept to emulate the same flows
- Admin operation here assumed to be out of scope
- Distribution of load among user flows is not defined and the scenario will provide the option to specify the value as a start up argument
- Accepted SLAs (FE and BE) are not discussed

Frontend Execution Strategy
The idea would be to measure the different times based on the navigation timing api.  Some key examples would be TTFB, Render Start, DomComplete etc., We can use a front end profiling tool like Web Page Test, or some vendor solutions like catchpoint to simulate the enterprise users from different geographic areas to understand the end user impact.

A sample load of the org for the first time user is below -
http://www.webpagetest.org/result/151020_FQ_ACG/
The film strip view -
http://www.webpagetest.org/video/compare.php?tests=151020_FQ_ACG-r:1-c:0

We also have to test for Single Point of Failures (SPOF) typically more appropriate for a customer facing site.  The idea is to make sure that no single request should single handedly distrust the overall user experience (increase the time to pain the page etc.,).

Backend Execution Strategy

- Baseline Scenario

We would want to collect the system response and behavior under normal load.  A normal load would be presumed as some % of the actual peak load, if known.

- Stress Scenario

This is where we would stress the system (either with more users or with the same users with reduced wait time between the simulated requests) to 2X / 3X the accepted peak load.

- Increasing User / Breakpoint Scenario

A test where a step-up scenario is performed till a break point is observed either at the system resource or some contentions that might not have caught during the previous tests.

- Longevity Scenario

Typically running for 48 hours under baseline load to mimic a possible memory contention that our short timed tests would not catch.

- Spike Scenario

A test to emulate a sudden burst in request, something similar to emulating a release time pull requests every day/week or the likes.

- LDAP Stress Scenario

If this has not been separately tested, would be good to test bind, search, connect and other specific transactions.

- Failover Scenario (Each Component)

Simulating the what-if scenarios.  What if puma stops responding in half the servers.   What impact does redis failure have on the end user.  What happens to the DB response times when memcache fails.  What is the response time impact

Profiling Strategy -
Assuming we find one user flow to show high response times what what is expected, then we dig into breaking down the response times across the layers.  If the resulting analysis points us to the code, then we remodel the useless to focus on that particular problem transaction to find out how the code breaks down through a APM solution of using a standard profiling tool.

Tools -

- Front End Performance -

Web page test, Catchpoint, Gomez, RUM data from New Relic, Google Page Speed, Chrome Dev Tools etc.,

- Back End Performance -

APM for Rails/PHP/JAVA profiling, Server monitors, openTSDB/Graphite etc., icinga/Nagios for alerting

- Load Simulation -

Gatling (used here).  Other options include Grinder(Python), Jmeter(Java) and other open source tools.

Invoking the scenario -

JAVA_OPTS="-DtestDuration=1200 -DrampUpTimeSecs=100 -DnoofCPRusers=1 -DnoofCMusers=1 -DnoofCBCPusers=1 -DnoofDRusers=1 -DmaxDurationSecs=1300" ./gatling.sh -s ghe.ghescenario

Sample Test Results -

View it from the results folder -
../ghe_perf/results/ghescenario-1445322963360/index.html
