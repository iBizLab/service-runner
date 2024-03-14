FROM image.ibizlab.cn/library/openjdk:8-jre-alpine-liuqiubase-v2

ENV TZ=Asia/Shanghai \
    SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    IBIZ_SLEEP=0 \
    JAVA_OPTS=""

ADD entrypoint.sh /entrypoint.sh

RUN chmod u+x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]

EXPOSE 30050

ADD ibizservicerunner-provider.jar /ibizservicerunner-provider.jar

ADD clouddebugger.jar /clouddebugger.jar

