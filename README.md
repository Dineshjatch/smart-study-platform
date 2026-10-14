# Smart Study Platform

A cloud-native study management web application built with a microservices architecture. The project combines a web interface, Spring Boot services, PostgreSQL databases, containerized deployment, Kubernetes orchestration, CI/CD automation, and application monitoring.

**Repository:** https://github.com/Dineshjatch/smart-study-platform

**Project report title:** *Smart Study Platform*

## Overview

The Smart Study Platform helps users organize study activities and monitor their progress. Users can manage subjects, record study sessions, review study history, take quizzes, and view quiz scores and attempt history. The Quiz Service also integrates with Azure OpenAI for AI-assisted quiz functionality.

The project demonstrates a practical DevOps workflow: containerizing application components, deploying them in a local Kubernetes cluster and on AWS EC2, automating delivery through Jenkins, and monitoring services with Prometheus and Grafana.

## Features

- User registration, login, and JWT-based authentication.
- Subject management and study-session tracking.
- Study history and statistics.
- Quiz management, answer submission, score calculation, and attempt history.
- Azure OpenAI integration for AI-assisted quiz functionality.
- Separate PostgreSQL databases for user, study, and quiz data.
- Docker-based packaging and Docker Compose deployment.
- Kubernetes Deployments, Services, configuration resources, persistent-volume claims, and Horizontal Pod Autoscalers.
- Jenkins pipeline for backend testing, Docker image build/publish, and deployment.
- Prometheus metrics collection and Grafana dashboards.
- k6-based load testing for authenticated API Gateway requests.
- Terraform configuration for AWS infrastructure resources.

## Architecture

```text
                      ┌──────────────────────┐
                      │   Web Browser/User   │
                      └──────────┬───────────┘
                                 │ HTTP
                      ┌──────────▼───────────┐
                      │ Nginx Frontend       │
                      └──────────┬───────────┘
                                 │ /api/
                      ┌──────────▼───────────┐
                      │ Spring Boot API      │
                      │ Gateway :8080        │
                      └─────┬──────┬─────┬───┘
                            │      │     │
                 ┌──────────▼┐ ┌───▼────┐ ┌▼──────────┐
                 │ User      │ │ Study  │ │ Quiz      │
                 │ Service   │ │ Service│ │ Service   │
                 │ :8081     │ │ :8082  │ │ :8083     │
                 └─────┬─────┘ └───┬────┘ └────┬─────┘
                       │           │           │
                 ┌─────▼─────┐ ┌───▼─────┐ ┌───▼─────┐
                 │ user_db   │ │ study_db│ │ quiz_db │
                 │ PostgreSQL│ │PostgreSQL│ │PostgreSQL│
                 └───────────┘ └─────────┘ └─────────┘

       Jenkins ──> Test/Build/Deploy
       Prometheus ──> Metrics collection ──> Grafana dashboards
       Kubernetes HPA ──> API Gateway replica scaling
```

The diagram summarizes the core runtime path. The exact deployment layout and accessible ports depend on whether the application is running through Docker Compose, local Kubernetes, or AWS EC2.

## Technology Stack

| Area | Technologies |
|---|---|
| Frontend | HTML, CSS, JavaScript, Nginx |
| Backend | Java 21, Spring Boot |
| API entry point | Spring Boot API Gateway |
| Database | PostgreSQL 17 |
| AI integration | Azure OpenAI |
| Containers | Docker, Docker Compose |
| Orchestration | Kubernetes (Docker Desktop) |
| Autoscaling | Kubernetes Horizontal Pod Autoscaler (HPA) |
| CI/CD | Jenkins, GitHub |
| Cloud | AWS EC2, EBS, S3, IAM, Systems Manager (SSM) |
| Infrastructure as Code | Terraform |
| Monitoring | Prometheus, Grafana, cAdvisor, Node Exporter |
| Load testing | k6 |

## Main Services and Ports

| Component | Container/service port | Purpose |
|---|---:|---|
| Frontend | 80 | Serves the web interface through Nginx |
| API Gateway | 8080 | Entry point for API requests and routing |
| User Service | 8081 | User registration, login, and profile-related operations |
| Study Service | 8082 | Subjects, study sessions, and study statistics |
| Quiz Service | 8083 | Quizzes, submissions, scoring, and attempt history |
| PostgreSQL | 5432 inside each database container | Stores service-specific data |

In the local Kubernetes configuration, the frontend is exposed through NodePort `30000` and the API Gateway through NodePort `30080`. Backend services and databases use internal ClusterIP Services. These Kubernetes NodePorts are not necessarily the same as local Docker Compose host ports.

## Repository Layout

The repository includes application services and deployment/operations resources. Key areas include:

```text
smart-study-platform/
├── frontend/                 # Web interface and Nginx configuration
├── services/
│   ├── api-gateway/           # Spring Boot API Gateway
│   ├── user-service/          # User and authentication operations
│   ├── study-service/         # Subjects and study sessions
│   └── quiz-service/          # Quiz operations and AI integration
├── kubernetes/                # Kubernetes manifests and load-test Job
├── k6/                        # k6 load-testing script(s)
├── monitoring/                # Prometheus/monitoring configuration
├── terraform/                 # AWS infrastructure as code
├── Jenkinsfile                # CI/CD pipeline definition
└── docker-compose.aws.yml     # AWS Compose file, if kept at repository root
```

*The listed paths are a guide to the principal project areas; consult the repository for the exact current file layout.*

## Prerequisites

Install the tools required for the workflow you intend to run:

- Git
- Docker Desktop with Docker Compose
- Java 21 and Maven (for running backend tests locally)
- `kubectl` and a working Kubernetes cluster (for Kubernetes deployment)
- AWS CLI configured with appropriate access (for AWS/SSM operations)
- Terraform (for infrastructure changes)

Azure OpenAI functionality requires valid endpoint/deployment configuration and an API key. Do not commit real API keys, passwords, JWT secrets, or AWS credentials to Git.

## Run with Docker Compose

From the repository root in PowerShell, first inspect the Compose file and required environment variables. If an example environment file is provided, copy it to a local, untracked environment file and fill in your own values.

```powershell
cd C:\dev\smart-study-platform

docker compose config
docker compose up --build -d
docker compose ps
```

Use `docker compose logs -f` to follow logs. Stop the local Compose stack with:

```powershell
docker compose down
```

These commands assume the repository-root Compose configuration and its required environment settings are in place. If you are working with the AWS-specific Compose file, use that file explicitly rather than assuming it is the local configuration.

## Run Backend Tests

Run the tests for an individual service from that service's directory. For example, for the API Gateway:

```powershell
cd C:\dev\smart-study-platform\services\api-gateway
mvn -B clean test
```

The service may require test-specific environment variables, such as a local JWT secret. Use a test-only value and never paste real secrets into logs or the README.

## Kubernetes

The local Kubernetes environment uses the `smart-study` namespace. Common inspection commands are:

```powershell
kubectl config current-context
kubectl get nodes -o wide
kubectl get pods -n smart-study
kubectl get services -n smart-study
kubectl get hpa -n smart-study
```

To inspect the API Gateway autoscaler and its events:

```powershell
kubectl describe hpa api-gateway-hpa -n smart-study
```

The API Gateway HPA is configured with a minimum of two and a maximum of four replicas, targeting average CPU utilization of 60%. The User, Study, and Quiz service HPAs are configured with both minimum and maximum replicas set to two. HPA operation depends on the cluster being able to obtain resource metrics.

## AWS Deployment

The AWS environment runs the application on an Amazon EC2 instance using Docker Compose. The deployment configuration is maintained separately from the local Kubernetes deployment. On the instance, the deployment directory used by the project is `/opt/smart-study`, with the AWS Compose configuration and `.env.aws` file.

The web application has been accessed at `http://34.206.7.102` in the recorded deployment. Availability of this address depends on the EC2 instance and its network configuration being active.

For administration, the project uses AWS Systems Manager Session Manager where configured. Monitoring ports should remain private; do not expose Grafana or Prometheus publicly just to access the dashboards. The `.env.aws` file contains deployment secrets and must not be shared or committed.

## CI/CD and Monitoring

The Jenkins pipeline includes stages for source checkout, backend tests, Docker image build/publish, Kubernetes deployment, AWS EC2 deployment, and post-build actions. The exact stage execution result should be checked in the relevant Jenkins build.

Prometheus is configured to collect metrics from the backend services and infrastructure exporters. Grafana presents selected metrics such as JVM heap memory, service availability, HTTP request rate, and HTTP response/error panels. A healthy Prometheus target confirms that the target is scrapeable; it does not alone prove that every application workflow succeeds.

## Testing and Known Results

The captured API Gateway Maven run recorded **21 tests with zero failures and zero errors**. A recorded Quiz Service run reported **39 tests with zero failures and zero errors**. These results refer to the captured test runs, not a guarantee that all code paths are defect-free.

The recorded second k6 run reached **30 virtual users** and reported:

| Metric | Result |
|---|---:|
| Total checks | 7,869 |
| Checks passed | 7,828 (99.47%) |
| Checks failed | 41 (0.52%, rounded) |
| Average HTTP duration | 28.48 ms |
| Median HTTP duration | 9.75 ms |
| 95th-percentile HTTP duration | 89.31 ms |
| Maximum HTTP duration | 18.14 s |
| Reported request rate | 56.94 requests/s |

The run completed, but it was **not entirely error-free**: connection-refused errors were reported for some authenticated API requests. The figures above are from the recorded run and should not be interpreted as a production capacity benchmark.

## Security Notes

- Keep `.env`, `.env.aws`, credentials, API keys, database passwords, and JWT secrets out of version control.
- Store only placeholder values in example configuration files.
- Keep Prometheus and Grafana behind private access controls or secure port forwarding.
- Review AWS security-group rules before deployment.
- Avoid including account identifiers, tokens, passwords, or other sensitive information in screenshots and public documentation.

## Future Improvements

- Investigate the connection-refused errors observed during k6 load testing.
- Add and verify suitable application health probes in Kubernetes.
- Expand automated functional and integration test coverage.
- Repeat load tests after corrective changes and compare results.
- Improve security and operational hardening, including HTTPS/TLS and more detailed tracing/alerting where appropriate.

## Author

**Dinesh Choudhary**  
B.Tech Computer Science and Engineering  
Lovely Professional University

---

*This README describes the project based on the recorded implementation and test evidence. Confirm environment variables, Compose file names, and current deployment addresses against the checked-out repository before using it as an operational runbook.*
