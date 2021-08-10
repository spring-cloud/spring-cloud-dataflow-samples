Step 1: Register the applications:

Go to App Registry and bulk import the following:

```
source.clicks-ingest=maven://org.springframework.cloud.dataflow.samples:http-click-ingest:1.2.0-SNAPSHOT
source.regions-ingest=maven://org.springframework.cloud.dataflow.samples:http-region-ingest:1.2.0-SNAPSHOT
processor.clicks-per-region=maven://org.springframework.cloud.dataflow.samples:user-clicks-per-region-processor:1.2.0-SNAPSHOT
sink.clicks-per-region-logger=maven://org.springframework.cloud.dataflow.samples:log-user-clicks-per-region:1.2.0-SNAPSHOT

```

Once added, create a streaming pipeline by using the Stream Editor, as follows:


You can also create a stream by using the following DSL and deploy the stream:

```
clicks-ingest --server.port=9001 || regions-ingest --server.port=9000 || clicks-per-region :userRegions<:regions-ingest.user-regions :userClicks<:clicks-ingest.user-clicks || clicks-per-region-logger :_<:clicks-per-region.clicksPerRegion
```

As you can see the `clicks-per-region` is a KStream application with two inputs and one output.


Note: In order to post the HTTP events, you might either need to port-forward your pods (on minikube) or use the service endpoints(if you have a Service endpoint available) of `click-ingest` and `regions-ingest` deployments. Since this sample is now set up on `minikube`, the following sample data is posted to `http://localhost:9000`  and `http://localhost:9001` after port-forwarding the associated pods.

Post some HTTP events as user region events:

```
curl -X POST http://localhost:9000 -H "username: Glenn" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Soby" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Janne" -d "europe" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: David" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Ilaya" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Mark" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Sabby" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Ilaya" -d "asia" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Chris" -d "americas" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Damien" -d "europe" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Christian" -d "europe" -H "Content-Type: text/plain"
curl -X POST http://localhost:9000 -H "username: Thomas" -d "americas" -H "Content-Type: text/plain"
```

We can also post some HTTP events as user click events:

```
curl -X POST http://localhost:9001 -H "username: Glenn" -d 9 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Soby" -d 15 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Janne" -d 10 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Mark" -d 7 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: David" -d 15 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Sabby" -d 20 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Ilaya" -d 10 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Chris" -d 5 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Damien" -d 21 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Christian" -d 12 -H "Content-Type: text/plain"
curl -X POST http://localhost:9001 -H "username: Thomas" -d 12 -H "Content-Type: text/plain"
```


Once both the user clicks and the user region events are posted, the KStream processor will start processing the user clicks per region and you can see the result at the logger application’s log:

```
2020-08-04 21:53:36.735  INFO 1 --- [container-0-C-1] user-clicks-per-region                   : europe: 139
2020-08-04 21:53:36.751  INFO 1 --- [container-0-C-1] user-clicks-per-region                   : americas: 297
2020-08-04 21:53:36.754  INFO 1 --- [container-0-C-1] user-clicks-per-region                   : asia: 40
2020-08-04 21:53:37.623  INFO 1 --- [container-0-C-1] user-clicks-per-region                   : europe: 172
2020-08-04 21:53:37.625  INFO 1 --- [container-0-C-1] user-clicks-per-region                   : americas: 314

```
