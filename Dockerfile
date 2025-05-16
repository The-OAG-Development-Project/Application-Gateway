#
# Build stage
#
FROM gradle:8.14-jdk17 AS build

# Copy build files and download dependencies -> allows faster build because this step can be cached
COPY oag/build.gradle oag/settings.gradle /home/app/
RUN mkdir -p /home/app/gradle/wrapper
COPY oag/gradle/wrapper/ /home/app/gradle/wrapper/
COPY oag/gradlew oag/gradlew.bat /home/app/
RUN cd /home/app && ./gradlew dependencies --no-daemon

# Copy rest of the sources and compile
COPY oag/src /home/app/src
COPY oag/*.yaml oag/*.txt /home/app/
RUN cd /home/app && ./gradlew clean build --no-daemon

#
# Package stage
#
FROM amazoncorretto:17.0.14-alpine3.18

# for regular linux 
# RUN useradd --user-group --system --create-home --no-log-init app
# for alpine
RUN adduser --system app

RUN mkdir -p /app
RUN chown app /app

COPY --from=build /home/app/build/libs/oag.jar /home/app/*.yaml /home/app/*.txt /home/app/*.pkcs12 /app/

USER app
WORKDIR /app
ENTRYPOINT ["java","-jar","oag.jar"]

#
# HOW TO:
#
# docker build -t owasp/application-gateway:SNAPSHOT .
# docker run -p 8080:8080 owasp/application-gateway:SNAPSHOT

# Test for docker misconfiguration with dockle
# VERSION=$(curl --silent "https://api.github.com/repos/goodwithtech/dockle/releases/latest" | grep '"tag_name":' | sed -E 's/.*"v([^"]+)".*/\1/') && curl -L -o dockle.tar.gz https://github.com/goodwithtech/dockle/releases/download/v${VERSION}/dockle_${VERSION}_Linux-64bit.tar.gz &&  tar zxvf dockle.tar.gz
# ./dockle --exit-code 1 owasp/application-gateway:SNAPSHOT

# Publish docker image
# docker push owasp/application-gateway:SNAPSHOT
