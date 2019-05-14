FROM python:3.7.3-slim

RUN pip install kafka-python
RUN pip install flask

ADD /util/* /util/
ADD barista_app.py /
ENTRYPOINT ["python","/barista_app.py"]
CMD []