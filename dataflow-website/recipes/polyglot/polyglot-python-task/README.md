
Build docker image and push to Docker Hub.
```bash
docker build -t tzolov/python_task_with_status:0.1 .
docker push tzolov/python_task_with_status:0.1
```
NOTE: replace `tzolov` with your docker hub prefix.

Register the docker image as SCDF `task` application:
```bash
dataflow:>app register --type task  --name python-task-with-status --uri docker://tzolov/python_task_with_status:0.1
```

Create task instance and launch it:
```bash
dataflow:>app register --type task  --name python-task-with-status --uri docker://tzolov/python_task_with_status:0.1
dataflow:>task create --name python-task --definition "python-task-with-status"
dataflow:>task launch --name python-task
```

TIP: if `--error.message=<Some Text>` is passed as a launch argument, the task will throw an error with the specified text.