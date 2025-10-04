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

      .setHeader("ce-id").simple("${exchangeId}")
      .setHeader("ce-type").constant("com.homelab.s3.object.created")
      .setHeader("ce-source").simple("urn:aws:s3:::${header.CamelAwsS3BucketName}")
      .setHeader("ce-subject").simple("${header.CamelAwsS3Key}")
      .setHeader("ce-specversion").constant("1.0")

      .setHeader(Exchange.CONTENT_TYPE).constant("application/octet-stream")
      .choice()
        .when(header("CamelAwsS3ContentType").isNotNull())
          .setHeader(Exchange.CONTENT_TYPE).simple("${header.CamelAwsS3ContentType}")
      .end()

      .setHeader("ce-objectsize").simple("${header.CamelAwsS3ContentLength}")
      .setHeader("ce-etag").simple("${header.CamelAwsS3ETag}")

      .toD("${env:K_SINK}");
  }
}
