ARG APP_INSIGHTS_AGENT_VERSION=3.2.11

# Application image

FROM hmctspublic.azurecr.io/base/java:openjdk-11-distroless-1.2

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/wa-workflow-api.jar /opt/app/

EXPOSE 8099
CMD [ "wa-workflow-api.jar" ]
