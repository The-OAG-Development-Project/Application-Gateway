FROM openjdk:13.0.2-oracle
RUN useradd --user-group --system --create-home --no-log-init app

RUN mkdir -p /app
RUN chown app /app
COPY nellygateway/target/artifact/* /app/

USER app
WORKDIR /app
ENTRYPOINT ["java","-jar","nellygateway.jar"]
# Build and run docker image
# docker build -t nellygateway:latest --no-cache .
# docker run -p 8080:8080 --env-file env.list  nellygateway:latest

# Test for docker misconfiguration with dockle
# VERSION=$(curl --silent "https://api.github.com/repos/goodwithtech/dockle/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/') && curl -L -o dockle.tar.gz https://github.com/goodwithtech/dockle/releases/download/v${VERSION}/dockle_${VERSION}_Linux-64bit.tar.gz &&  tar zxvf dockle.tar.gz
# ./dockle --exit-code 1 nellygateway:latest

# Publish to azure container registry
# docker tag nellygateway nellygateway.azurecr.io/nellygateway
# docker push nellygateway.azurecr.io/nellygateway