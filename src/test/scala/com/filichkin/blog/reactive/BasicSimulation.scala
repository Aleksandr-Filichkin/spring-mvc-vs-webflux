package com.filichkin.blog.reactive

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BootLoadSimulation extends Simulation {

  private val baseUrl = "http://localhost:8082"
  private val webFluxEndpoint = "/webflux-webclient?delay=100"
  private val cfEndpoint = "/completable-future-apache-client?delay=100"
  private val contentType = "application/json"
  private val requestCount = 100


  private val httpConf = http.baseUrl(baseUrl)
    .acceptHeader("application/json;charset=UTF-8").shareConnections

  private val webfluxTest = repeat(requestCount) {
    exec(http("webflux-test")
      .get(webFluxEndpoint)
      .header("Content-Type", contentType)
     .check(status.is(200)))
  }
  private val completableFutureTest = repeat(requestCount) {
    exec(http("completable-future-apache-client")
      .get(cfEndpoint)
      .header("Content-Type", contentType)
      .check(status.is(200)))
  }
  private val webfluxScenario = scenario("BootLoadSimulation")
    .exec(webfluxTest)

  private val completableFutureTestScenario = scenario("BootLoadSimulation")
    .exec(completableFutureTest)

  setUp(webfluxScenario.inject(atOnceUsers(1000))).protocols(httpConf)
  setUp(completableFutureTestScenario.inject(atOnceUsers(1000))).protocols(httpConf)
}