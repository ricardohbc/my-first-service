FROM hd1cutl01lx.saksdirect.com:5000/library/jvm-base:centos-6.6

# Add and unzip the Play app
ADD hbc-microservice-template-0.1.zip /opt/hbc-microservice-template-0.1.zip
RUN unzip /opt/hbc-microservice-template-0.1.zip -d /opt

# Give exec permission to the start script (unzip doesn't preserve permissions)
RUN chmod 755 /opt/hbc-microservice-template-0.1/bin/hbc-microservice-template

# Play port to expose
EXPOSE 9000

ENV CONFIG_RESOURCE application.conf

CMD nohup /opt/hbc-microservice-template-0.1/bin/hbc-microservice-template -Dhttp.port=9000 -Dpidfile.path=/dev/null -Dconfig.resource=${CONFIG_RESOURCE}
