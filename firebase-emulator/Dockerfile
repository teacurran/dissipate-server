FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

RUN apk add --update nodejs npm

#RUN apt-get install -y curl \
#  && curl -sL https://deb.nodesource.com/setup_9.x | bash - \
#  && apt-get install -y nodejs \
#  && curl -L https://www.npmjs.com/install.sh | sh

# RUN apt-get update -y
# RUN apt-get install -y libvips libvips-dev libvips-tools

RUN npm install -g firebase-tools

# Install app dependencies
#COPY ./functions/package.json ./functions/package-lock.json ./
#
#RUN npm install --unsafe-perm --ignore-scripts=false --verbose
#
#RUN npm rebuild --verbose sharp

ADD ./firebase.json /app/firebase.json
# COPY ./functions/ /app

# RUN npm run build

RUN firebase --version
EXPOSE 4000 5000 9099 9199 8080
