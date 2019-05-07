FROM python:3.7.3-slim
RUN apt-get update
RUN apt-get install build-essential -y
RUN apt-get install default-libmysqlclient-dev -y
RUN pip install mysqlclient
RUN pip install sqlalchemy
ADD python_task.py /
ADD util/* /util/
ENTRYPOINT ["python","/python_task.py"]
CMD []
