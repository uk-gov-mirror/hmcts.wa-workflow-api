# wa-workflow-api

[![Build Status](https://travis-ci.org/hmcts/wa-workflow-api.svg?branch=master)](https://travis-ci.org/hmcts/wa-workflow-api)

#### What does this app do?

API with endpoints that:

- Evaluate DMN configuration deployed on Camunda
- Triggers Camunda workflows

<!--
    Sequence Diagram Source:
    http://www.plantuml.com/plantuml/uml/dP91Qzmm48NFgryXw7dnkJc4U7VfAT2557gUI6FlO5cZZaPnIyX_ht3AsavX2_N9lFVccmUZXwmWMiPWBkJHTipwp3-DoGF510AZpHVmF57ihKh1ZOC_2aQ7zjNiMX6UZXnOx0anFGs_3g5WrPso75WyohlhGORdsNga3XyfuZzSS4ClNA9_JtngFv-EfozcPtPd42L72Q8kZCt-RUO3QRgGedap1eeoEAKNKBGSJLfcx34GHcrJsgZVMOOkyDGcsgPXh7WufF4SG3kMqPWhmUxlruFspfw_FdsxiytqHsmyEih4SU-neqXQVvjNycyDla0ee6ZC6b1vVozSi1XxYhWNpBe0RFq4jKYqrIX1Fo0hyrfurNr_Vxlv_8hchlzTSSISeTyqpCMvgjweEASzNg-tFim3Hb_6y1i0
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
