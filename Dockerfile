FROM tomcat:10.1-jdk17

LABEL maintainer="Rushi"

# Remove default applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy WAR as ROOT application
COPY target/securebank.war /usr/local/tomcat/webapps/ROOT.war

EXPOSE 8080

CMD ["catalina.sh","run"]
