#!/bin/sh
set -eu

envsubst '${ANGULAR_API_BASE_URL}' \
  < /usr/share/nginx/html/assets/config/runtime-config.template.json \
  > /usr/share/nginx/html/assets/config/runtime-config.json
