# spring-mvc-vs-webflux

Compare Performance for Rest Service that proxies request to underline Rest service and return it back.
<un>
<li> Servlet blocking call with Java 11 client (endpoint /sync) </li>
<li> Servlet with CompletableFuture call with Java 11 client (endpoint /completable-future) </li>
<li> WebFlux  call with Java 11 client (endpoint /webflux-java-http-client) </li>
<li> WebFlux  call with WebClient (endpoint /webflux-webclient) </li>
</un>