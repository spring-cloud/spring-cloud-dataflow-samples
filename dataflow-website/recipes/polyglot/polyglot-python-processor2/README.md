Build docker image:

`$ docker build -t chrisjs/python-processor:latest .`

Push to Docker Hub if desired:

`$ docker push chrisjs/python-processor:latest`

NOTE: replace `chrisjs` with your docker hub user/org


Register a stream using this processor in between `http` and `log`:

```
dataflow:>app import --uri http://bit.ly/Einstein-SR2-stream-applications-kafka-docker
dataflow:>app register --type processor --name python-processor --uri docker://chrisjs/python-processor:latest
```

NOTE: replace `chrisjs` with your docker hub user/org

This example uses minikube as the target k8s env.

Get IP:

```
$ minikube ip
192.168.99.104
```

Watch logs:

```
$ kubectl logs -f <log pod name>
```

Post and log a string:

```
dataflow:>stream create --name test --definition "http --server.port=32123 | python-processor | log"
dataflow:>stream deploy test --properties "deployer.http.kubernetes.createNodePort=32123"
dataflow:>http post --target http://192.168.99.104:32123 --data "hello world"
> POST (text/plain) http://192.168.99.104:32123 hello world
> 202 ACCEPTED
```

Inspect logs for posted message:

```
INFO 1 --- [container-0-C-1] log-sink                                 : hello world
```

Post and log a reversed string:

```
dataflow:>stream create --name test --definition "http --server.port=32123 | python-processor --reverestring=true  | log"
dataflow:>stream deploy test --properties "deployer.http.kubernetes.createNodePort=32123"
dataflow:>http post --target http://192.168.99.104:32123 --data "hello world"
> POST (text/plain) http://192.168.99.104:32123 hello world
> 202 ACCEPTED
```

Inspect logs for posted message:

```
INFO 1 --- [container-0-C-1] log-sink                                 : dlrow olleh
```

