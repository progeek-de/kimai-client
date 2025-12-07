# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
- **Jira Integration**: Added comprehensive Jira integration for time tracking workflow
  - Configure Jira connection in Settings (supports both Jira Cloud and Server/Data Center)
  - Search and select Jira issues directly from timesheet input
  - Automatic caching of issues with 15-minute background sync
  - Manual refresh option in issue picker dialog
  - Offline support with local cache fallback
  - Encrypted credential storage for Jira API tokens and Personal Access Tokens
  - Dual authentication support with automatic detection based on Jira URL
  - Formatted text insertion ("KEY: Summary") into timesheet description field

### Technical Details
- New database table `JiraIssue` for caching (separate from Timesheet table)
- Store5-based caching with 15-minute expiration policy
- Background sync scheduler with configurable interval (5-120 minutes)
- Comprehensive error handling for connection, authentication, and rate limiting
- MVI architecture using MVIKotlin for state management
- Decompose-based navigation for issue picker dialog

### Database Migrations
- Added `JiraIssue` table with indexes on projectKey, assignee, and updated fields
- No changes to existing Timesheet table (Jira references stored as plain text in description)