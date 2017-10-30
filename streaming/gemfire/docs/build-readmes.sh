#!/usr/bin/env sh
DOCS_DIR=../../../docs
$DOCS_DIR/coalesce.rb http-gemfire/README.adoc > ../http-gemfire/README.adoc
$DOCS_DIR/coalesce.rb gemfire-log/README.adoc > ../gemfire-log/README.adoc
$DOCS_DIR/coalesce.rb gemfire-cq-log/README.adoc > ../gemfire-cq-log/README.adoc
