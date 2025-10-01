/*
 * doc references:
 * https://docs.redhat.com/en/documentation/red_hat_fuse/7.1/html/apache_camel_component_reference/aws-s3-component
 * https://knative.dev/blog/articles/consuming_s3_data_with_knative/#conclusion
 * https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#event-data
 */

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

      // content-type for the event body (fallback if S3 didn't set one)
      .choice()
        .when(header(Exchange.CONTENT_TYPE).isNull())
          .setHeader(Exchange.CONTENT_TYPE).constant("application/octet-stream")
      .end()

      // https://docs.redhat.com/en/documentation/red_hat_fuse/7.1/html/apache_camel_component_reference/aws-s3-component
      // I need to lookmore into the etag but appears to be optional
      .setHeader("ce-objectsize").simple("${header.CamelAwsS3ObjectLength}")
      .setHeader("ce-etag").simple("${header.CamelAwsS3ETag}")

      // I need to look more into creating this route instead of setting deleteAfterRead=true
      // to avoid re-processing the same object multiple times.
      /*
      .idempotentConsumer(
        header("CamelAwsS3ETag"),
        MemoryIdempotentRepository.memoryIdempotentRepository(10000))
      */

      .to("knative:event/s3-events-broker?kind=Broker&name=s3-events-broker");
  }
}
