# ghcr.io/whitebox-co/liquibase-mariadb-tools:latest
# docker build -t ghcr.io/whitebox-co/liquibase-mariadb-tools:latest -f ./docker/Dockerfile .
FROM liquibase/liquibase:4.6.2

WORKDIR /liquibase

RUN lpm add mysql --global

USER root

COPY ../target/liquibase-mariadb-tools.jar /liquibase/lib/

# we need these files to be able to connect to any database which requires an ssl connection
COPY ../skysql_chain.pem /liquibase
COPY ../.my.cnf /home/liquibase
# this is likely not needed for production but is really useful for development since
# you need to exec into the container as root to install/update things
COPY ../.my.cnf /root


RUN apt-get update \
    && apt-get -y install perl curl apt-transport-https \
    && curl -LsS https://r.mariadb.com/downloads/mariadb_repo_setup | bash -s -- --skip-server --skip-maxscale \
    && apt-get -y install mariadb-tools \
    && keytool -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -alias mariadb -import -file skysql_chain.pem --noprompt

USER liquibase
