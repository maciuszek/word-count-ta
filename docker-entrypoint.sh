#!/bin/sh

echo "########################################"
echo "# ARGS=$ARGS"
echo "########################################"

java -jar app.jar $ARGS # expect word splitting to support multiple args
