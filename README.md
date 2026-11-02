# Airline Management System

Beginner-friendly Java 17 + Spring Boot + Eureka + Gateway + 3 microservices + MySQL + Angular 19 demo.

Customers can update their display name and review their own bookings. Admins can manage flights and use read-only, searchable customer and booking reports; these features stay inside the existing Auth and Booking services.

## Project guides

- [Complete architecture map](docs/PROJECT_ARCHITECTURE.md) — diagrams, service boundaries, database, API routes, Angular pages, and project folders.
- [Beginner's code walkthrough](docs/CODE_WALKTHROUGH.md) — patterns, source-file explanations, validation, security, interview questions, and a study order.
- [Setup and demo guide](docs/SETUP_AND_DEMO_GUIDE.md) — pinned tools, MySQL, Eclipse, VS Code, Command Prompt, startup, and the live demo flow.
- [Implementation plan](docs/IMPLEMENTATION_PLAN.md) — scope, schema, endpoints, team responsibilities, acceptance criteria, and project limitations.

## Quick start

1. Run `powershell -ExecutionPolicy Bypass -File scripts/setup-local-tools.ps1` to install the pinned JDK, Node/npm, and Maven under `.tools` without changing machine-wide settings. Load them in the current PowerShell session with `. .\scripts\use-local-tools.ps1`.
2. If you need the database on this machine, run `powershell -ExecutionPolicy Bypass -File scripts/setup-local-mysql.ps1`. In a separate PowerShell window, run `powershell -ExecutionPolicy Bypass -File scripts/start-local-mysql.ps1`. On first start, set the classroom password once with the command printed by the setup script.
3. `java tools/GenerateKeys.java` (creates local RSA keys, only once).
4. `mvn clean package` using Java 17 and Maven 3.9.x.
5. Start the five jars in the order listed in the guide, from this repository root.
6. In `airline-frontend`, run `npm ci`, then `npm start`; open <http://localhost:4200>.
7. Swagger: <http://localhost:8080/swagger-ui/index.html>. Eureka: <http://localhost:8761>.

For the no-Node demonstration build on Windows, run `powershell -ExecutionPolicy Bypass -File scripts/package-demo.ps1` before starting the five services. This bundles the Angular production app into the Gateway JAR, to be served on port 8080. See the setup guide for exact Java versions, separate terminal commands, and IDE instructions.

## Seeded classroom accounts

| Account | Password |
|---|---|
| `admin@airline.test` | `Admin@12345` |
| `customer@airline.test` | `Customer@12345` |

Eight example flights are automatically inserted in an empty flight database. All seed data is for classroom/demo use only. Review the detailed guide before deployment.
