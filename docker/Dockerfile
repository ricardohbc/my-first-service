FROM sd1pgo01lx.saksdirect.com/rhel7:2

# Add and unzip the Play app
ADD service-0.1.zip /opt/service-0.1.zip
RUN unzip /opt/service-0.1.zip -d /opt

# Give exec permission to the start script (unzip doesn't preserve permissions)
RUN chmod 755 /opt/service-0.1/bin/service

# Play port to expose
EXPOSE 9000

CMD nohup /opt/service-0.1/bin/service -Dhttp.port=9000 -Dpidfile.path=/dev/null -Dconfig.file=${CONFIG_FILE}
