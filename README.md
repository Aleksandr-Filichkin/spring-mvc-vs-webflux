# spring-mvc-vs-webflux

https://medium.com/@filia.aleks/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0

Compare Performance for Rest Service that proxies request to underline Rest service and return it back.
<un>
<li> Servlet blocking call with Java 11 client (endpoint /sync) </li>
<li> Servlet with CompletableFuture call with Java 11 client (endpoint /completable-future) </li>
<li> WebFlux call with Java 11 client (endpoint /webflux-java-http-client) </li>
<li> WebFlux call with WebClient (endpoint /webflux-webclient) </li>
<li> WebFlux call with Apache Http Client (endpoint /webflux-apache-client) </li>
</un>
<br>

**Build**

For WebFlux(Netty) use "web-flux" maven profile
**mvn clean install -P web-flux**

For Servlet(Tomcat) use "servlet" maven profile
**mvn clean install -P servlet**
