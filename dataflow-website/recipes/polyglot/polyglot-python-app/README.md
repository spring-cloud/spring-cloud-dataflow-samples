
Build docker image and push to Docker Hub.
```bash
docker build -t tzolov/scdf_python_app:0.1 .
docker push tzolov/scdf_python_app:0.1
```
NOTE: replace `tzolov` with your docker hub prefix.

Register the docker image as SCDF `app` application:
```bash
app register --type app --name python-router --uri docker://tzolov/scdf_python_app:0.1
```


