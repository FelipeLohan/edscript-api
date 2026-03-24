#!/bin/sh
set -e

nginx

exec java -jar /app/app.jar
