# s3proc

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/s3proc-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)



## s3Proc approach

Working...

s3proc is Apachae Camel for Quarkus

Deployed kubernetes resource sa and sinkbinding


Also for env variables to see it tracing through the camel route

```bash
 oc -n s3bucket set env deploy/s3proc \
  CAMEL_MAIN_TRACING=true \
  CAMEL_MAIN_MESSAGE_HISTORY=true \
  CAMEL_MAIN_USE_MDC_LOGGING=true \
  QUARKUS_LOG_CATEGORY__ORG_APACHE_CAMEL_COMPONENT_AWS2_S3__LEVEL=DEBUG \
  QUARKUS_LOG_CATEGORY__ORG_APACHE_CAMEL_COMPONENT_KNATIVE__LEVEL=DEBUG \
  QUARKUS_LOG_CATEGORY__ORG_APACHE_CAMEL_COMPONENT_HTTP__LEVEL=DEBUG \
  QUARKUS_LOG_CATEGORY__ORG_APACHE_CAMEL_PROCESSOR__LEVEL=DEBUG
```

```bash
oc -n s3bucket rollout restart deploy/s3proc
```


```bash
package com.homelab;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.Exchange;

public class S3ToKnativeRoute extends RouteBuilder {
  @Override
  public void configure() {

      from("aws2-s3://{{aws.s3.bucketNameOrArn}}"
        + "?region={{aws.s3.region}}"
        + "&accessKey={{aws.s3.accessKey}}"
        + "&secretKey={{aws.s3.secretKey}}"
        + "&autoCreateBucket=false"
        + "&deleteAfterRead=true"
        + "&includeBody=true"
        + "&delay={{s3.poll.delay:5000}}")
      .routeId("s3-to-knative")

      // Looking at this with prefect of ce-...
      //   I am not sure which are minimally required at this point for kn eventing to accept it
      .setHeader("ce-id").simple("${exchangeId}")
      .setHeader("ce-type").constant("com.homelab.s3.object.created")
      .setHeader("ce-source").simple("urn:aws:s3:::${header.CamelAwsS3BucketName}")
      .setHeader("ce-subject").simple("${header.CamelAwsS3Key}")
      .setHeader("ce-specversion").constant("1.0")

      // This is done in this order to default the content type to application/octet-stream
      //  because upstream examples use otherwise() but that isn't available in this context
      .setHeader(Exchange.CONTENT_TYPE).constant("application/octet-stream")
      .choice()
        .when(header("CamelAwsS3ContentType").isNotNull())
          .setHeader(Exchange.CONTENT_TYPE).simple("${header.CamelAwsS3ContentType}")
      .end()

      // TODO: How optional are these, etag seems like it could be useful
      // https://docs.redhat.com/en/documentation/red_hat_fuse/7.1/html/apache_camel_component_reference/aws-s3-component
      // I need to lookmore into the etag but appears to be optional
      .setHeader("ce-objectsize").simple("${header.CamelAwsS3ContentLength}")
      .setHeader("ce-etag").simple("${header.CamelAwsS3ETag}")

      // TODO: consider .to("knative:event/s3-events-broker?kind=Broker&name=s3-events-broker");
      //  switched this to straight http instead of the knative component
      //  as I was having issues with it and wanted to move forward
      //  the other routine above was working but not producing valid ce messages
      //  so I need to circle back and look at that
      .toD("${env:K_SINK}");

  }
}

```