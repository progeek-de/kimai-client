# Kimai Client (work-in-progress 👷🔧️👷‍♀️⛏)

Kimai-Client is a multiplatform (Windows, OSX, Linux) desktop client written in Kotlin for Kimai Time Tracker.

Kimai Client offers seamless integration with the Kimai Time Tracker Server, allowing users to manage multiple workspaces simultaneously. This integration facilitates efficient time tracking across multiple projects and clients. All time tracking data is securely stored on the designated Kimai server, which supports both on-premises and SaaS cloud options, ensuring data privacy and easy accessibility.

## Connection

To get started with Kimai Client, make sure you have a working Kimai setup, which can be your own server or a Kimai Cloud account. Generate an API password and login with your credentials.

## Dark mode

For design enthusiasts, Kimai Client's Dark Mode is a stylish option to consider. You can easily switch between the traditional bright interface and the elegant dark theme. This feature not only enhances visual comfort, but also adds a personal touch to your time tracking experience.

## Compose Multiplatform
As mentioned above, this app's UI is completely written in [Compose
Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/).

## Compose for Desktop client
This client is available in compose-desktop module and can be run using ./gradlew :kimai-desktop:run. Note that you need to use appropriate version of JVM when running (works for example with Java 17)

## Testing

Kimai Client has comprehensive test coverage for all business logic layers:

### Running Tests

```bash
# Run all tests
./gradlew :kimai-shared:jvmTest

# Generate coverage report
./gradlew :kimai-shared:jacocoTestReport

# Verify coverage threshold (60% minimum)
./gradlew :kimai-shared:jacocoTestCoverageVerification
```

### Coverage Reports

After running tests with coverage, open the HTML report:
```
kimai-shared/build/reports/jacoco/jacocoTestReport/html/index.html
```

### Test Structure

- **Mappers**: 99% coverage - Data transformation tests
- **Network Clients**: 93% coverage - API interaction tests
- **Models**: 92% coverage - Domain model tests
- **Repositories**: 64-78% coverage - Business logic tests
- **Stores**: MVIKotlin state management tests

### CI/CD

Tests run automatically on every pull request via GitHub Actions. The workflow:
- Executes the full test suite
- Generates coverage reports
- Enforces minimum coverage thresholds
- Comments coverage statistics on PRs

## Contributing

You want to contribute to this repository? This is so great!
The best way to start is to [open a new issue](https://github.com/progeek-de/kimai-client/issues) for bugs or feature requests or a [discussion](https://github.com/progeek-de/kimai-client/discussions) for questions, support and such.

In case you want to contribute, but you wouldn't know how, here are some suggestions:

- Answer questions: You know the answer to another user's problem? Share your knowledge.
- Something can be done better? An essential feature is missing? Create a feature request.
- Report bugs makes Kimai better for everyone.
- You don't have to be programmer, the documentation and translation could always use some attention.
- Sponsor the project: free software costs money to create!

There is one simple rule in our "Code of conduct": Don't be an ass!

