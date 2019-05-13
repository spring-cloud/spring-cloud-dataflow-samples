
Build docker image and push to Docker Hub.
```bash
docker build -t tzolov/scdf_python_app:0.1 .
docker push tzolov/scdf_python_app:0.1
```
NOTE: replace `tzolov` with your docker hub prefix.

Register the docker image as SCDF `app` application:
```bash
app register --type app --name barista-app --uri docker://tzolov/scdf_python_app:0.1
```

Build the Bar pipelines:
```bash
stream create --name orders --definition "customer: time > :orders" --deploy
stream create --name cold-drink-line --definition ":coldDrinks > cold-drinks: log" --deploy
stream create --name hot-drink-line --definition ":hotDrinks > hot-drinks: log" --deploy
stream create --name bar --definition "barista-app"
stream deploy --name bar --properties app.barista-app.spring.cloud.stream.bindings.orders.destination=orders,app.barista-app.spring.cloud.stream.bindings.hot.drink.destination=hotDrinks,app.barista-app.spring.cloud.stream.bindings.cold.drink.destination=coldDrinks
```

![alt text](./scdf-barista-python-polyglot.png "Logo Title Text 1")


