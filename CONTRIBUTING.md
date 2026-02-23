# Contributing to Sentinel SIEM

Thank you for your interest in contributing! To maintain a high-quality codebase, please follow these guidelines.

## 🌿 Branching Strategy
- `main`: Stable release branch.
- `feature/*`: New features and enhancements.
- `bugfix/*`: Bug repairs.

## 💬 Commit Messages
We follow the **Conventional Commits** specification. Please format your messages as follows:
- `feat: ...` for new features.
- `fix: ...` for bug fixes.
- `docs: ...` for documentation changes.
- `chore: ...` for maintenance tasks.

## 🧪 Testing
Before submitting a pull request, ensure that:
1. All modules build successfully: `./mvnw clean install`.
2. All unit tests pass.
3. Your code matches the project's architectural patterns.

## 🛠️ Tech Stack Constraints
- Avoid using Lombok to maintain compatibility with modern JDK versions (21+).
- Use the manually implemented Builder pattern for domain objects.
- Ensure all API endpoints are documented in the code.
