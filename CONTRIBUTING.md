# Contributing to MediPredict

Thank you for your interest in contributing to MediPredict!

## Development Workflow

1. **Fork the Repository**: Create a personal fork on GitHub.
2. **Clone Locally**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/disease-prediction-springboot.git
   cd disease-prediction-springboot
   ```
3. **Create a Feature Branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Adhere to Code Standards**:
   - Follow standard Java naming and formatting conventions.
   - Maintain the existing REST API conventions and error response format (`ErrorResponse`).
   - Keep controllers thin and delegate business rules to service layers.
   - Do not commit credentials, hardcoded passwords, or environment-specific connection strings.
5. **Run the Test Suite**:
   Ensure all existing and new tests pass before submitting changes:
   ```bash
   mvn clean test
   ```
6. **Commit & Push**:
   Write clear, concise commit messages following standard conventions.
   ```bash
   git commit -m "feat: describe your change"
   git push origin feature/your-feature-name
   ```
7. **Submit a Pull Request**:
   Open a pull request describing the rationale and implementation details of your change.

## Reporting Issues

If you identify a bug or have a feature proposal, please open an Issue on GitHub providing:
- Clear description of the behavior.
- Steps to reproduce.
- Relevant log output or stack traces.
- Your runtime environment (Java, OS, MySQL version).
