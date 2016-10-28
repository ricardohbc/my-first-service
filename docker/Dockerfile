FROM sd1pgo01lx.saksdirect.com/rhel7:3

# Add and unzip the Play app
COPY microservice-template-0.1.zip /opt/microservice-template-0.1.zip
COPY newrelic.* /opt/newrelic/
RUN unzip /opt/microservice-template-0.1.zip -d /opt

# Give exec permission to the start script (unzip doesn't preserve permissions)

COPY containerbuddy /opt/containerbuddy

RUN chmod 755 /opt/microservice-template-0.1/bin/microservice-template
RUN chmod 775 /opt/containerbuddy/*

# Play port to expose
EXPOSE 9000

CMD nohup /opt/microservice-template-0.1/bin/microservice-template -Dhttp.port=9000 -Dpidfile.path=/dev/null -Dconfig.file=${CONFIG_FILE} -Dlogger.file=conf/docker-logback.xml
