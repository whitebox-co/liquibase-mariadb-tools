# liquibase-mariadb-tools
FROM liquibase/liquibase:4.6.2

RUN lpm add mysql --global

USER root

RUN apt-get update \
    && apt-get -y install perl curl apt-transport-https \
    && curl -LsS https://r.mariadb.com/downloads/mariadb_repo_setup | bash -s -- --skip-server --skip-maxscale \
    && apt-get -y install mariadb-tools

COPY ./target/liquibase-mariadb-tools-1.0.0-SNAPSHOT.jar /liquibase/lib/

USER liquibase
