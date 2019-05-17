FROM python:3.7.3-slim

RUN pip install kafka-python
RUN pip install flask

ADD /util/* /util/
ADD python_router_app.py /
ENTRYPOINT ["python","/python_router_app.py"]
CMD []
