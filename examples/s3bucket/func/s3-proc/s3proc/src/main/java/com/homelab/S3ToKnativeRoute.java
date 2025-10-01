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
