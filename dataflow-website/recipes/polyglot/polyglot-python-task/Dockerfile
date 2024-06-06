FROM python:3.9.19-slim

RUN apt-get update
RUN apt-get install -y \
        build-essential
RUN apt-get install -y \
        default-libmysqlclient-dev \
        pkg-config

RUN pip install --upgrade pip
RUN pip install mysqlclient
RUN pip install sqlalchemy

ADD python_task.py /
ADD util/* /util/

ENTRYPOINT ["python","/python_task.py"]
CMD []
