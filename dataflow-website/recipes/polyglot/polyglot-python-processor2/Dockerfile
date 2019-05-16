FROM springcloud/openjdk:latest

RUN apt-get update && apt-get install --no-install-recommends -y \
    python-pip \
 && rm -rf /var/lib/apt/lists/*
 
RUN pip install kafka-python

COPY python_processor.py /processor/
COPY util/*.py /processor/util/

ENTRYPOINT ["python", "/processor/python_processor.py", "$@", "--"]
