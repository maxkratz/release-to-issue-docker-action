FROM debian:12
LABEL maintainer="Max Kratz <github@maxkratz.com>"
ENV DEBIAN_FRONTEND=noninteractive

COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
