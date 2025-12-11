package de.progeek.kimai.shared.core.ticketsystem.providers.github

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

/**
 * Test suite for GitHubResponseMapper.
 *
 * Tests the mapping of GitHub API response models to unified ticket models.
 */
class GitHubResponseMapperTest {

    private val testConfig = TicketSystemConfig(
        id = "config-uuid-456",
        displayName = "Work GitHub",
        provider = TicketProvider.GITHUB,
        enabled = true,
        baseUrl = "https://api.github.com",
        credentials = TicketCredentials.GitHubToken(
            token = "ghp_test_token",
            owner = "testowner",
            repositories = listOf("repo1", "repo2")
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val testRepoFullName = "testowner/testrepo"

    // ============================================================
    // mapRepository() Tests
    // ============================================================

    @Test
    fun `mapRepository maps fullName as key`() {
        val response = GitHubRepoResponse(
            id = 12345L,
            name = "testrepo",
            fullName = "owner/testrepo",
            description = "A test repository",
            owner = GitHubOwner(login = "owner")
        )

        val result = GitHubResponseMapper.mapRepository(response)

        assertEquals("owner/testrepo", result.key)
    }

    @Test
    fun `mapRepository maps name correctly`() {
        val response = GitHubRepoResponse(
            id = 12345L,
            name = "testrepo",
            fullName = "owner/testrepo",
            description = null,
            owner = GitHubOwner(login = "owner")
        )

        val result = GitHubResponseMapper.mapRepository(response)

        assertEquals("testrepo", result.name)
    }

    @Test
    fun `mapRepository maps description when present`() {
        val response = GitHubRepoResponse(
            id = 12345L,
            name = "testrepo",
            fullName = "owner/testrepo",
            description = "Repository description",
            owner = GitHubOwner(login = "owner")
        )

        val result = GitHubResponseMapper.mapRepository(response)

        assertEquals("Repository description", result.description)
    }

    @Test
    fun `mapRepository handles null description`() {
        val response = GitHubRepoResponse(
            id = 12345L,
            name = "testrepo",
            fullName = "owner/testrepo",
            description = null,
            owner = GitHubOwner(login = "owner")
        )

        val result = GitHubResponseMapper.mapRepository(response)

        assertNull(result.description)
    }

    // ============================================================
    // mapIssue() Tests - Basic Fields
    // ============================================================

    @Test
    fun `mapIssue maps id as string correctly`() {
        val response = createGitHubIssueResponse(id = 123456L)

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("123456", result.id)
    }

    @Test
    fun `mapIssue maps key with hash prefix`() {
        val response = createGitHubIssueResponse(number = 42)

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("#42", result.key)
    }

    @Test
    fun `mapIssue maps title as summary`() {
        val response = createGitHubIssueResponse(title = "Fix authentication bug")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Fix authentication bug", result.summary)
    }

    @Test
    fun `mapIssue sets provider to GITHUB`() {
        val response = createGitHubIssueResponse()

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals(TicketProvider.GITHUB, result.provider)
    }

    @Test
    fun `mapIssue sets sourceId from config`() {
        val response = createGitHubIssueResponse()

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("config-uuid-456", result.sourceId)
    }

    // ============================================================
    // mapIssue() Tests - Project
    // ============================================================

    @Test
    fun `mapIssue sets projectKey from repoFullName`() {
        val response = createGitHubIssueResponse()

        val result = GitHubResponseMapper.mapIssue(response, testConfig, "owner/myrepo")

        assertEquals("owner/myrepo", result.projectKey)
    }

    @Test
    fun `mapIssue extracts projectName from repoFullName`() {
        val response = createGitHubIssueResponse()

        val result = GitHubResponseMapper.mapIssue(response, testConfig, "owner/myrepo")

        assertEquals("myrepo", result.projectName)
    }

    @Test
    fun `mapIssue handles complex repo full name`() {
        val response = createGitHubIssueResponse()

        val result = GitHubResponseMapper.mapIssue(response, testConfig, "organization/project-name")

        assertEquals("organization/project-name", result.projectKey)
        assertEquals("project-name", result.projectName)
    }

    // ============================================================
    // mapIssue() Tests - State Mapping
    // ============================================================

    @Test
    fun `mapIssue maps open state to Open`() {
        val response = createGitHubIssueResponse(state = "open")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Open", result.status)
    }

    @Test
    fun `mapIssue maps closed state to Closed`() {
        val response = createGitHubIssueResponse(state = "closed")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Closed", result.status)
    }

    @Test
    fun `mapIssue handles OPEN uppercase state`() {
        val response = createGitHubIssueResponse(state = "OPEN")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Open", result.status)
    }

    @Test
    fun `mapIssue handles unknown state by capitalizing first letter`() {
        val response = createGitHubIssueResponse(state = "reopened")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Reopened", result.status)
    }

    // ============================================================
    // mapIssue() Tests - Issue Type from Labels
    // ============================================================

    @Test
    fun `mapIssue derives Bug type from bug label`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "bug", color = "d73a4a"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue derives Bug type from bug label case insensitive`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "Bug", color = "d73a4a"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue derives Feature type from feature label`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "feature", color = "a2eeef"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Feature", result.issueType)
    }

    @Test
    fun `mapIssue derives Enhancement type from enhancement label`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "enhancement", color = "84b6eb"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Enhancement", result.issueType)
    }

    @Test
    fun `mapIssue derives Documentation type from documentation label`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "documentation", color = "0075ca"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Documentation", result.issueType)
    }

    @Test
    fun `mapIssue derives Question type from question label`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "question", color = "d876e3"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Question", result.issueType)
    }

    @Test
    fun `mapIssue defaults to Issue type when no matching labels`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "priority-high", color = "ff0000"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Issue", result.issueType)
    }

    @Test
    fun `mapIssue defaults to Issue type when empty labels`() {
        val response = createGitHubIssueResponse(labels = emptyList())

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Issue", result.issueType)
    }

    @Test
    fun `mapIssue prioritizes bug over other labels`() {
        val response = createGitHubIssueResponse(
            labels = listOf(
                GitHubLabel(name = "enhancement", color = "84b6eb"),
                GitHubLabel(name = "bug", color = "d73a4a")
            )
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue handles label containing bug keyword`() {
        val response = createGitHubIssueResponse(
            labels = listOf(GitHubLabel(name = "critical-bug-fix", color = "d73a4a"))
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("Bug", result.issueType)
    }

    // ============================================================
    // mapIssue() Tests - Assignee
    // ============================================================

    @Test
    fun `mapIssue maps assignee login correctly`() {
        val response = createGitHubIssueResponse(
            assignee = GitHubUserResponse(
                id = 123L,
                login = "johndoe",
                name = "John Doe",
                email = "john@example.com"
            )
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("johndoe", result.assignee)
    }

    @Test
    fun `mapIssue handles null assignee`() {
        val response = createGitHubIssueResponse(assignee = null)

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertNull(result.assignee)
    }

    // ============================================================
    // mapIssue() Tests - Updated Timestamp
    // ============================================================

    @Test
    fun `mapIssue parses ISO 8601 timestamp correctly`() {
        val response = createGitHubIssueResponse(
            updatedAt = "2024-01-15T10:30:00Z"
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals(Instant.parse("2024-01-15T10:30:00Z"), result.updated)
    }

    @Test
    fun `mapIssue handles invalid timestamp with epoch 0`() {
        val response = createGitHubIssueResponse(updatedAt = "invalid-timestamp")

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    // ============================================================
    // mapIssue() Tests - Web URL
    // ============================================================

    @Test
    fun `mapIssue uses htmlUrl directly`() {
        val response = createGitHubIssueResponse(
            htmlUrl = "https://github.com/owner/repo/issues/42"
        )

        val result = GitHubResponseMapper.mapIssue(response, testConfig, testRepoFullName)

        assertEquals("https://github.com/owner/repo/issues/42", result.webUrl)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createGitHubIssueResponse(
        id: Long = 123456L,
        number: Int = 42,
        title: String = "Test Issue",
        state: String = "open",
        labels: List<GitHubLabel> = emptyList(),
        assignee: GitHubUserResponse? = null,
        updatedAt: String = "2024-01-15T10:30:00Z",
        htmlUrl: String = "https://github.com/owner/repo/issues/42"
    ): GitHubIssueResponse {
        return GitHubIssueResponse(
            id = id,
            number = number,
            title = title,
            body = null,
            state = state,
            user = null,
            assignee = assignee,
            labels = labels,
            htmlUrl = htmlUrl,
            updatedAt = updatedAt,
            repositoryUrl = null
        )
    }
}
