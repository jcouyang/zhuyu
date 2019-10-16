package us.oyanglul.zhuyu
package impls

import effects._
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration

trait HasSQSImpl extends HasSQS {
  val sqsConfig: SQSConfig = SQSConfig(
    sqsQueueUrl = "http://sqs:9324/queue/ExampleEvents",
    longPollSeconds = 10
  )
  val sqsClient = AmazonSQSClientBuilder
    .standard()
    .withCredentials(
      new AWSStaticCredentialsProvider(new BasicAWSCredentials("x", "x")))
    .withEndpointConfiguration(
      new EndpointConfiguration("http://sqs:9324", "us-east-1"))
    .build()
}
