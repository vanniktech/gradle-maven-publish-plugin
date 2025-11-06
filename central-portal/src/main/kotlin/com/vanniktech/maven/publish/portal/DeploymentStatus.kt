package com.vanniktech.maven.publish.portal

/**
 * Response from the Central Portal API status endpoint.
 * See https://central.sonatype.org/publish/publish-portal-api/#verify-status-of-the-deployment
 */
internal data class DeploymentStatusResponse(
  val deploymentId: String,
  val deploymentName: String,
  val deploymentState: DeploymentState,
  val purls: List<String>? = null,
  val errors: Map<String, List<String>>? = null,
)

/**
 * Possible states of a deployment in Maven Central.
 */
internal enum class DeploymentState {
  /**
   * A deployment is uploaded and waiting for processing by the validation service
   */
  PENDING,

  /**
   * A deployment is being processed by the validation service
   */
  VALIDATING,

  /**
   * A deployment has passed validation and is waiting on a user to manually publish via the Central Portal UI
   */
  VALIDATED,

  /**
   * A deployment has been either automatically or manually published and is being uploaded to Maven Central
   */
  PUBLISHING,

  /**
   * A deployment has successfully been uploaded to Maven Central
   */
  PUBLISHED,

  /**
   * A deployment has encountered an error (additional context will be present in an errors field)
   */
  FAILED,
}
