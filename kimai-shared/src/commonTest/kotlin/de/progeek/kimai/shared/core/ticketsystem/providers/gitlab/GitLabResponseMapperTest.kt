package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

import de.progeek.kimai.shared.core.ticketsystem.models.IssueInsertFormat
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Test suite for GitLabResponseMapper.
 *
 * Tests the mapping of GitLab API response models to unified ticket models.
 */
class GitLabResponseMapperTest {

    private val testConfig = TicketSystemConfig(
        id = "config-uuid-789",
        displayName = "Work GitLab",
        provider = TicketProvider.GITLAB,
        enabled = true,
        baseUrl = "https://gitlab.com",
        credentials = TicketCredentials.GitLabToken(
            token = "glpat-test_token",
            projectIds = listOf("123", "456")
        ),
        syncIntervalMinutes = 15,
        issueFormat = IssueInsertFormat.DEFAULT_FORMAT
    )

    private val testProjectPath = "group/project"

    // ============================================================
    // mapProject() Tests
    // ============================================================

    @Test
    fun `mapProject maps pathWithNamespace as key`() {
        val response = GitLabProjectResponse(
            id = 12345L,
            name = "project",
            pathWithNamespace = "group/project",
            description = "A test project",
            webUrl = "https://gitlab.com/group/project"
        )

        val result = GitLabResponseMapper.mapProject(response)

        assertEquals("group/project", result.key)
    }

    @Test
    fun `mapProject maps name correctly`() {
        val response = GitLabProjectResponse(
            id = 12345L,
            name = "my-project",
            pathWithNamespace = "group/my-project",
            description = null,
            webUrl = "https://gitlab.com/group/my-project"
        )

        val result = GitLabResponseMapper.mapProject(response)

        assertEquals("my-project", result.name)
    }

    @Test
    fun `mapProject maps description when present`() {
        val response = GitLabProjectResponse(
            id = 12345L,
            name = "project",
            pathWithNamespace = "group/project",
            description = "Project description",
            webUrl = "https://gitlab.com/group/project"
        )

        val result = GitLabResponseMapper.mapProject(response)

        assertEquals("Project description", result.description)
    }

    @Test
    fun `mapProject handles null description`() {
        val response = GitLabProjectResponse(
            id = 12345L,
            name = "project",
            pathWithNamespace = "group/project",
            description = null,
            webUrl = "https://gitlab.com/group/project"
        )

        val result = GitLabResponseMapper.mapProject(response)

        assertNull(result.description)
    }

    @Test
    fun `mapProject handles nested group path`() {
        val response = GitLabProjectResponse(
            id = 12345L,
            name = "project",
            pathWithNamespace = "org/team/subgroup/project",
            description = null,
            webUrl = "https://gitlab.com/org/team/subgroup/project"
        )

        val result = GitLabResponseMapper.mapProject(response)

        assertEquals("org/team/subgroup/project", result.key)
    }

    // ============================================================
    // mapIssue() Tests - Basic Fields
    // ============================================================

    @Test
    fun `mapIssue maps id as string correctly`() {
        val response = createGitLabIssueResponse(id = 123456L)

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("123456", result.id)
    }

    @Test
    fun `mapIssue maps key with hash prefix from iid`() {
        val response = createGitLabIssueResponse(iid = 42)

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("#42", result.key)
    }

    @Test
    fun `mapIssue maps title as summary`() {
        val response = createGitLabIssueResponse(title = "Fix authentication bug")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Fix authentication bug", result.summary)
    }

    @Test
    fun `mapIssue sets provider to GITLAB`() {
        val response = createGitLabIssueResponse()

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals(TicketProvider.GITLAB, result.provider)
    }

    @Test
    fun `mapIssue sets sourceId from config`() {
        val response = createGitLabIssueResponse()

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("config-uuid-789", result.sourceId)
    }

    // ============================================================
    // mapIssue() Tests - Project
    // ============================================================

    @Test
    fun `mapIssue sets projectKey from projectPath`() {
        val response = createGitLabIssueResponse()

        val result = GitLabResponseMapper.mapIssue(response, testConfig, "group/myproject")

        assertEquals("group/myproject", result.projectKey)
    }

    @Test
    fun `mapIssue extracts projectName from projectPath`() {
        val response = createGitLabIssueResponse()

        val result = GitLabResponseMapper.mapIssue(response, testConfig, "group/myproject")

        assertEquals("myproject", result.projectName)
    }

    @Test
    fun `mapIssue handles nested group path for project name`() {
        val response = createGitLabIssueResponse()

        val result = GitLabResponseMapper.mapIssue(response, testConfig, "org/team/subgroup/project")

        assertEquals("org/team/subgroup/project", result.projectKey)
        assertEquals("project", result.projectName)
    }

    // ============================================================
    // mapIssue() Tests - State Mapping
    // ============================================================

    @Test
    fun `mapIssue maps opened state to Open`() {
        val response = createGitLabIssueResponse(state = "opened")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Open", result.status)
    }

    @Test
    fun `mapIssue maps closed state to Closed`() {
        val response = createGitLabIssueResponse(state = "closed")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Closed", result.status)
    }

    @Test
    fun `mapIssue maps merged state to Merged`() {
        val response = createGitLabIssueResponse(state = "merged")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Merged", result.status)
    }

    @Test
    fun `mapIssue maps locked state to Locked`() {
        val response = createGitLabIssueResponse(state = "locked")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Locked", result.status)
    }

    @Test
    fun `mapIssue handles OPENED uppercase state`() {
        val response = createGitLabIssueResponse(state = "OPENED")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Open", result.status)
    }

    @Test
    fun `mapIssue handles unknown state by capitalizing first letter`() {
        val response = createGitLabIssueResponse(state = "reopened")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Reopened", result.status)
    }

    // ============================================================
    // mapIssue() Tests - Issue Type from Labels
    // ============================================================

    @Test
    fun `mapIssue derives Bug type from bug label`() {
        val response = createGitLabIssueResponse(labels = listOf("bug"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue derives Bug type from bug label case insensitive`() {
        val response = createGitLabIssueResponse(labels = listOf("Bug"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue derives Feature type from feature label`() {
        val response = createGitLabIssueResponse(labels = listOf("feature"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Feature", result.issueType)
    }

    @Test
    fun `mapIssue derives Enhancement type from enhancement label`() {
        val response = createGitLabIssueResponse(labels = listOf("enhancement"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Enhancement", result.issueType)
    }

    @Test
    fun `mapIssue derives Documentation type from documentation label`() {
        val response = createGitLabIssueResponse(labels = listOf("documentation"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Documentation", result.issueType)
    }

    @Test
    fun `mapIssue derives Incident type from incident label`() {
        val response = createGitLabIssueResponse(labels = listOf("incident"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Incident", result.issueType)
    }

    @Test
    fun `mapIssue defaults to Issue type when no matching labels`() {
        val response = createGitLabIssueResponse(labels = listOf("priority::high"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Issue", result.issueType)
    }

    @Test
    fun `mapIssue defaults to Issue type when empty labels`() {
        val response = createGitLabIssueResponse(labels = emptyList())

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Issue", result.issueType)
    }

    @Test
    fun `mapIssue prioritizes bug over other labels`() {
        val response = createGitLabIssueResponse(labels = listOf("enhancement", "bug"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Bug", result.issueType)
    }

    @Test
    fun `mapIssue handles label containing bug keyword`() {
        val response = createGitLabIssueResponse(labels = listOf("type::bug"))

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("Bug", result.issueType)
    }

    // ============================================================
    // mapIssue() Tests - Assignee
    // ============================================================

    @Test
    fun `mapIssue maps assignee username correctly`() {
        val response = createGitLabIssueResponse(
            assignee = GitLabUserResponse(
                id = 123L,
                username = "johndoe",
                name = "John Doe",
                email = "john@example.com",
                webUrl = null
            )
        )

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("johndoe", result.assignee)
    }

    @Test
    fun `mapIssue uses first assignee from assignees list when assignee is null`() {
        val response = createGitLabIssueResponse(
            assignee = null,
            assignees = listOf(
                GitLabUserResponse(
                    id = 123L,
                    username = "firstuser",
                    name = "First User",
                    email = null,
                    webUrl = null
                ),
                GitLabUserResponse(
                    id = 456L,
                    username = "seconduser",
                    name = "Second User",
                    email = null,
                    webUrl = null
                )
            )
        )

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("firstuser", result.assignee)
    }

    @Test
    fun `mapIssue handles null assignee and empty assignees`() {
        val response = createGitLabIssueResponse(
            assignee = null,
            assignees = emptyList()
        )

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertNull(result.assignee)
    }

    @Test
    fun `mapIssue prefers assignee over assignees list`() {
        val response = createGitLabIssueResponse(
            assignee = GitLabUserResponse(
                id = 123L,
                username = "primary",
                name = "Primary User",
                email = null,
                webUrl = null
            ),
            assignees = listOf(
                GitLabUserResponse(
                    id = 456L,
                    username = "secondary",
                    name = "Secondary User",
                    email = null,
                    webUrl = null
                )
            )
        )

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("primary", result.assignee)
    }

    // ============================================================
    // mapIssue() Tests - Updated Timestamp
    // ============================================================

    @Test
    fun `mapIssue parses ISO 8601 timestamp correctly`() {
        val response = createGitLabIssueResponse(updatedAt = "2024-01-15T10:30:00.000Z")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals(Instant.parse("2024-01-15T10:30:00.000Z"), result.updated)
    }

    @Test
    fun `mapIssue handles invalid timestamp with epoch 0`() {
        val response = createGitLabIssueResponse(updatedAt = "invalid-timestamp")

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals(Instant.fromEpochMilliseconds(0), result.updated)
    }

    // ============================================================
    // mapIssue() Tests - Web URL
    // ============================================================

    @Test
    fun `mapIssue uses webUrl directly`() {
        val response = createGitLabIssueResponse(
            webUrl = "https://gitlab.com/group/project/-/issues/42"
        )

        val result = GitLabResponseMapper.mapIssue(response, testConfig, testProjectPath)

        assertEquals("https://gitlab.com/group/project/-/issues/42", result.webUrl)
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createGitLabIssueResponse(
        id: Long = 123456L,
        iid: Int = 42,
        title: String = "Test Issue",
        state: String = "opened",
        labels: List<String> = emptyList(),
        assignee: GitLabUserResponse? = null,
        assignees: List<GitLabUserResponse> = emptyList(),
        updatedAt: String = "2024-01-15T10:30:00.000Z",
        webUrl: String = "https://gitlab.com/group/project/-/issues/42"
    ): GitLabIssueResponse {
        return GitLabIssueResponse(
            id = id,
            iid = iid,
            title = title,
            description = null,
            state = state,
            author = null,
            assignee = assignee,
            assignees = assignees,
            labels = labels,
            webUrl = webUrl,
            updatedAt = updatedAt,
            projectId = 12345L,
            references = null
        )
    }
}
