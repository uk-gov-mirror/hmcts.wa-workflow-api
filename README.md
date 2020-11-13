# wa-workflow-api

[![Build Status](https://travis-ci.org/hmcts/wa-workflow-api.svg?branch=master)](https://travis-ci.org/hmcts/wa-workflow-api)

#### What does this app do?

Provides endpoints to:

- Evaluate DMN configuration deployed in Camunda
- Correlate messages to Camunda that will either initiate or cancel Camunda BPMN processes

<!--
    Sequence Diagram Source:
    http://www.plantuml.com/plantuml/uml/dP51Qzmm48NFgryXwDdut1n2lBlq5EX22ZrFfB5tiAnHHwEu9UI_LpJ5gqqXXUWatfltpOEvomgYPGdcJ1xjoAoF_DEOF5DC8B3OV0RFAyN9gXIySl17mUZGFflT8CBhWGEBFIAcX_dNFXIikbEMFOj7UNKzoFpunxgd4UyfudzSSFCaNA9_dVWqV3uUZb_CpknE84gE4aHj6Ut-TkOjQJsJedan1eeoEAKdKBGSJRfax24GHXrIkbA_iynZu7L8T3qDIyN3FYsd03goBCPi1DlTja5xOqzV7xxTs7xzA_OU7MNYs7aSAPBMR_zC_gN79mW558rf0PNtxy8B5kD44Jl1h1U0hKzGYqHRZHBq3R8ojy9Dz-xs_UOBvWpJX_jj-QPUBivLzRr-jZwFs-dQgznXz6K9lm40
    See: https://plantuml.com/ docs for reference
-->

![workflow api](workflow-api.png)


## Notes

Since Spring Boot 2.1 bean overriding is disabled. If you want to enable it you will need to set `spring.main.allow-bean-definition-overriding` to `true`.

JUnit 5 is now enabled by default in the project. Please refrain from using JUnit4 and use the next generation

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/wa-workflow-api` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8099` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8099/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
