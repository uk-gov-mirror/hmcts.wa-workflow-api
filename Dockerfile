ARG APP_INSIGHTS_AGENT_VERSION=3.5.4

# Application image

FROM hmctsprod.azurecr.io/base/java:25-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/wa-workflow-api.jar /opt/app/

EXPOSE 8099
CMD [ "wa-workflow-api.jar" ]
