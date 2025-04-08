# Base image and maintainer
FROM debian:12
LABEL maintainer="Max Kratz <github@maxkratz.com>"
ENV DEBIAN_FRONTEND=noninteractive

# Install necessary dependencies
RUN apt update -q
RUN apt install -yq curl jq

# Copy and define the entrypoint script
COPY entrypoint.sh /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]
