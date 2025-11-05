package com.vanniktech.maven.publish.portal

import com.google.common.truth.Truth.assertThat
import java.io.IOException
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.slf4j.LoggerFactory

class SonatypeCentralPortalTest {
  private lateinit var mockWebServer: MockWebServer
  private lateinit var portal: SonatypeCentralPortal
  private val logger = LoggerFactory.getLogger(SonatypeCentralPortalTest::class.java)

  @BeforeEach
  fun setUp() {
    mockWebServer = MockWebServer()
    mockWebServer.start()

    portal = SonatypeCentralPortal(
      baseUrl = mockWebServer.url("/").toString(),
      usertoken = "dGVzdDp0b2tlbg==", // base64 "test:token"
      userAgentName = "test-agent",
      userAgentVersion = "1.0.0",
      okhttpTimeoutSeconds = 5,
      closeTimeoutSeconds = 30,
      pollIntervalMs = 0L,
      logger = logger,
    )
  }

  @AfterEach
  fun tearDown() {
    mockWebServer.shutdown()
  }

  @Test
  fun `validateDeployment succeeds when deployment reaches VALIDATING state`() {
    // First response: PENDING
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PENDING",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    // Second response: VALIDATING
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "VALIDATING",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    // Third response: VALIDATED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "VALIDATED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    // Fourth response: PUBLISHED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")
    // No exception thrown means success
  }

  @Test
  fun `validateDeployment succeeds when deployment reaches VALIDATED state`() {
    // First response: VALIDATED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "VALIDATED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    // Second response: PUBLISHED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")
    // No exception thrown means success
  }

  @Test
  fun `validateDeployment succeeds when deployment reaches PUBLISHING state`() {
    // First response: PUBLISHING
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHING",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    // Second response: PUBLISHED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")
    // No exception thrown means success
  }

  @Test
  fun `validateDeployment succeeds when deployment reaches PUBLISHED state`() {
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": ["pkg:maven/com.example/test@1.0.0"]
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")
    // No exception thrown means success
  }

  @Test
  fun `validateDeployment fails when deployment enters FAILED state with errors`() {
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "FAILED",
            "purls": [],
            "errors": {
              "pom.xml": [
                "Invalid POM file"
              ],
              "metadata": [
                "Missing required metadata"
              ]
            }
          }
          """.trimIndent(),
        ),
    )

    val exception = assertThrows<IOException> {
      portal.validateDeployment("test-id-123")
    }

    assertThat(exception.message).contains("Deployment test-id-123 failed validation")
    assertThat(exception.message).contains("Publication pom.xml:")
    assertThat(exception.message).contains("* Invalid POM file")
    assertThat(exception.message).contains("Publication metadata:")
    assertThat(exception.message).contains("* Missing required metadata")
  }

  @Test
  fun `validateDeployment fails when deployment enters FAILED state without errors`() {
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "FAILED",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    val exception = assertThrows<IOException> {
      portal.validateDeployment("test-id-123")
    }

    assertThat(exception.message).contains("Deployment test-id-123 failed validation")
    assertThat(exception.message).contains("No error details available")
  }

  @Test
  fun `validateDeployment fails when API returns error response`() {
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(401)
        .setBody("Unauthorized"),
    )

    val exception = assertThrows<IOException> {
      portal.validateDeployment("test-id-123")
    }

    assertThat(exception.message).contains("Failed to check deployment status")
    assertThat(exception.message).contains("401")
  }

  @Test
  fun `validateDeployment times out when validation takes too long`() {
    // Create a portal with very short timeout
    val shortTimeoutPortal = SonatypeCentralPortal(
      baseUrl = mockWebServer.url("/").toString(),
      usertoken = "dGVzdDp0b2tlbg==",
      userAgentName = "test-agent",
      userAgentVersion = "1.0.0",
      okhttpTimeoutSeconds = 5,
      closeTimeoutSeconds = 1, // 1 second timeout
      pollIntervalMs = 100L, // Small interval to allow timeout to trigger
      logger = logger,
    )

    // Always return PENDING
    repeat(20) {
      mockWebServer.enqueue(
        MockResponse()
          .setResponseCode(200)
          .setBody(
            """
            {
              "deploymentId": "test-id-123",
              "deploymentName": "test-deployment",
              "deploymentState": "PENDING",
              "purls": []
            }
            """.trimIndent(),
          ),
      )
    }

    val exception = assertThrows<IOException> {
      shortTimeoutPortal.validateDeployment("test-id-123")
    }

    assertThat(exception.message).contains("Deployment validation timed out")
    assertThat(exception.message).contains("Last known state: PENDING")
  }

  @Test
  fun `validateDeployment polls through multiple states`() {
    // PENDING -> PENDING -> VALIDATED
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PENDING",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PENDING",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "VALIDATED",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")
    // Success - method completed without exception
  }

  @Test
  fun `validateDeployment sends correct authorization header`() {
    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "VALIDATED",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    mockWebServer.enqueue(
      MockResponse()
        .setResponseCode(200)
        .setBody(
          """
          {
            "deploymentId": "test-id-123",
            "deploymentName": "test-deployment",
            "deploymentState": "PUBLISHED",
            "purls": []
          }
          """.trimIndent(),
        ),
    )

    portal.validateDeployment("test-id-123")

    val request = mockWebServer.takeRequest()
    assertThat(request.path).isEqualTo("/api/v1/publisher/status?id=test-id-123")
    assertThat(request.method).isEqualTo("POST")
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer dGVzdDp0b2tlbg==")
  }
}
