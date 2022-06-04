FROM centos:7

USER root

RUN yum install glibc.i686 git bzip2 unzip -y

ARG JAVA8_URL=https://github.com/adoptium/temurin8-binaries/releases/download/jdk8u312-b07/OpenJDK8U-jdk_x64_linux_hotspot_8u312b07.tar.gz
ARG MAVEN_URL=https://archive.apache.org/dist/maven/maven-3/3.8.5/binaries/apache-maven-3.8.5-bin.tar.gz
ARG NODE_URL=https://nodejs.org/dist/v14.17.5/node-v14.17.5-linux-x64.tar.xz
ARG PHANTOMJS_URL=https://bitbucket.org/ariya/phantomjs/downloads/phantomjs-2.1.1-linux-x86_64.tar.bz2
ARG CHROMIUM_URL=https://dl.google.com/linux/direct/google-chrome-stable_current_x86_64.rpm
ARG YUM_DOCKER_URL=https://download.docker.com/linux/centos/docker-ce.repo
ARG WINE_BINARIES_CENTOS7_URL
ARG USE_DOCKER_HOST

#JAVA
RUN mkdir -p /opt/customtools/java && curl -ksSL -o jdk8.tar.gz $JAVA8_URL && tar xvzf jdk8.tar.gz -C /opt/customtools/java && rm jdk8.tar.gz
ENV JAVA_HOME="/opt/customtools/java/jdk8u312-b07/"

#MAVEN
RUN mkdir -p /opt/customtools/maven && curl -sSL -o maven.tar.gz $MAVEN_URL && tar xvzf maven.tar.gz -C /opt/customtools/maven && rm maven.tar.gz
ENV MAVEN_HOME="/opt/customtools/maven/apache-maven-3.8.5/"

#NODE
RUN mkdir -p /opt/customtools/node && curl -sSL -o node.tar.xz $NODE_URL && tar -xf node.tar.xz -C /opt/customtools/node && rm node.tar.xz
ENV NODE_HOME="/opt/customtools/node/node-v14.17.5-linux-x64/"

#PHANTOMJS
RUN mkdir -p /opt/customtools/phantomjs && curl -sSL -o phantomjs.tar.bz2 $PHANTOMJS_URL && tar -xf phantomjs.tar.bz2 -C /opt/customtools/phantomjs \
&& rm phantomjs.tar.bz2 && ln -s /opt/customtools/phantomjs/phantomjs-2.1.1-linux-x86_64/bin/phantomjs /usr/bin/
ENV PHANTOMJS_HOME="/opt/customtools/phantomjs/phantomjs-2.1.1-linux-x86_64/"

#CHROMIUM
RUN curl -sSL -o google-chrome.rpm $CHROMIUM_URL && yum install google-chrome.rpm -y && rm google-chrome.rpm

#WINE
RUN if [[ -n "$WINE_BINARIES_CENTOS7_URL" ]]; then cd /usr && curl -ksSL -o wine-binaries.tar.gz $WINE_BINARIES_CENTOS7_URL \
&& tar xvzf ./wine-binaries.tar.gz && rm wine-binaries.tar.gz; fi

ENV PATH="${JAVA_HOME}bin/:${MAVEN_HOME}bin/:${NODE_HOME}bin:${PHANTOMJS_HOME}bin/:${PATH}"

#GULP & GRUNT
RUN npm install -g grunt-cli@1.3.2 && npm install -g gulp-cli@2.3.0

RUN useradd baseuser

#DOCKER
RUN if [[ "$USE_DOCKER_HOST" == true ]]; then yum install -y yum-utils && yum-config-manager --setopt=sslverify=false --add-repo $YUM_DOCKER_URL \
&& yum install --setopt=sslverify=false -y docker-ce-cli && usermod -aG root baseuser; fi

USER baseuser

WORKDIR /home/baseuser

VOLUME [ "/home/baseuser/data" ]

RUN if [[ -n "$WINE_BINARIES_CENTOS7_URL" ]]; then wineboot; fi
RUN if [[ -n "$WINE_BINARIES_CENTOS7_URL" ]]; then wineserver -d; fi

ENTRYPOINT ["/bin/bash"]