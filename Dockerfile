FROM tomcat:10.1-jdk17

LABEL maintainer="Rushi"

# Remove default Tomcat applications
RUN rm -rf /usr/local/tomcat/webapps/*

# Deploy SecureBank WAR as the ROOT application
COPY target/securebank.war /usr/local/tomcat/webapps/ROOT.war

# SecureBank runs on Tomcat 8080
EXPOSE 8080

# Start Tomcat in foreground mode
CMD ["catalina.sh", "run"]
