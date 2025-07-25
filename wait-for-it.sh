#!/usr/bin/env bash

host="$1"
port="$2"
shift 2
cmd="$@"

echo "⏳ Waiting for $host:$port to be ready..."

while ! nc -z $host $port; do
  sleep 1
done

echo "✅ $host:$port is ready. Starting app..."
exec $cmd