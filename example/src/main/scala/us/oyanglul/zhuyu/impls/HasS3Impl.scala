package us.oyanglul.zhuyu
package impls

import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.s3.{AmazonS3, AmazonS3ClientBuilder}

trait HasS3Impl extends effects.HasS3 {
  val s3Client: AmazonS3 = AmazonS3ClientBuilder
    .standard()
    .withCredentials(new AWSStaticCredentialsProvider(
      new BasicAWSCredentials("xxx", "xxxxxxxx")))
    .withEndpointConfiguration(
      new EndpointConfiguration("http://s3:9000", "us-east-1"))
    .withPathStyleAccessEnabled(true)
    .build()
}
