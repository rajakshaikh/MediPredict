# MediPredict — Jakarta Servlet & JSP Web Application

A standalone Java Servlet and JSP web tier for the **MediPredict** Clinical Decision Support System, engineered for deployment on **Apache Tomcat 10.1+ (Jakarta EE 10)**.

---

## 1. Architectural Overview

```text
[ Browser Client ]
        │  HTTP (HTML5 Forms & JSPs)
        ▼
[ disease-prediction-web (Tomcat 10.1 on Port 8081) ]
   ├── HomeServlet       (/home)
   ├── PatientServlet    (/patients, /patients/register)
   ├── PredictionServlet (/predict, /predict/general, /predict/heart)
   └── HistoryServlet    (/history, /history/report)
        │
        │  Java 11+ HttpClient (No direct MySQL, No duplicate business logic)
        ▼
[ disease-prediction-springboot (Spring Boot on Port 8080) ]
   ├── /api/ml/symptoms
   ├── /api/patients
   ├── /api/patients/{id}/predict
   ├── /api/patients/{id}/predict-heart-disease
   ├── /api/predictions
   └── /api/patients/{id}/predictions/{id}/report (OpenPDF)
        │
        ▼
[ disease-prediction-ml (Python FastAPI on Port 8000) ]
   └── Scikit-Learn Random Forest Models
```

### Key Principles Followed:
- **Zero Business Logic Duplication**: All ML inference, database persistence, and OpenPDF report generation remain exclusively in the Spring Boot backend and Python ML microservice.
- **Pure HTTP Inter-Service Communication**: The Servlets communicate via `java.net.http.HttpClient` against the Spring Boot REST endpoints.
- **No Direct Database Connection**: The web module does not bundle JDBC drivers or JPA entities, maintaining strict tier decoupling.
- **Safe Patient Handling**: If the backend API encounters an error or returns a non-200 code, the web tier displays user-friendly clinical warnings and never fabricates predictions.
- **Jakarta EE 10 Standard**: Built using Jakarta Servlet 6.0, Jakarta JSP 3.1, and Jakarta JSTL 3.0.

---

## 2. Implemented Servlets & JSP Views

| Servlet Name | URL Patterns | Methods | Associated JSP View | Description |
|---|---|---|---|---|
| `HomeServlet` | `/home` | `GET` | `/WEB-INF/views/home.jsp` | Live dashboard showing registered patients, total predictions, and system connectivity. |
| `PatientServlet` | `/patients`, `/patients/register` | `GET`, `POST` | `/WEB-INF/views/patient-register.jsp` | Demographic enrollment form, validation, and table of registered patients. |
| `PredictionServlet` | `/predict`, `/predict/general` | `GET`, `POST` | `/WEB-INF/views/predict-general.jsp` &rarr; `prediction-result.jsp` | Searchable 132-symptom catalog with categories and chips, general disease prediction. |
| `PredictionServlet` | `/predict/heart` | `GET`, `POST` | `/WEB-INF/views/predict-heart.jsp` &rarr; `prediction-result.jsp` | 13 clinical biomarkers risk evaluation with confidence gauges. |
| `HistoryServlet` | `/history` | `GET` | `/WEB-INF/views/history.jsp` | Filterable prediction audit trail by patient or all system records. |
| `HistoryServlet` | `/history/report` | `GET` | Stream (`application/pdf`) | Direct binary streaming of the official OpenPDF Medical Report. |
| — | Error Pages | — | `/WEB-INF/views/error.jsp` | Safe error handling page (no fabricated predictions). |

---

## 3. Technology Stack & Dependencies

- **Java**: 17+ (Compiled with `--release 17`, fully compatible with JDK 17, 21, and 25)
- **Servlet Specification**: Jakarta Servlet API 6.0 (`jakarta.servlet:jakarta.servlet-api:6.0.0`, scope `provided`)
- **JSP Specification**: Jakarta Server Pages 3.1 (`jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1`, scope `provided`)
- **JSTL**: Jakarta Standard Tag Library 3.0 (`jakarta.servlet.jsp.jstl-api` + `org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1`)
- **JSON Serialization**: Jackson Databind 2.17.2 + JSR310 Date/Time Module
- **HTTP Client**: Built-in `java.net.http.HttpClient`
- **Testing**: JUnit 5.10.2

---

## 4. Build & Deployment Instructions

### Prerequisites
1. **Spring Boot Backend**: Running on `http://localhost:8080`
2. **Python FastAPI ML Service**: Running on `http://localhost:8000`
3. **Apache Tomcat 10.1+**: Installed locally (e.g. `C:\apache-tomcat-10.1.60\apache-tomcat-10.1.60`)
4. **Apache Maven**: 3.8+

### Step 1: Configure Tomcat HTTP Port (Avoid Port 8080 Conflict)
Because the Spring Boot backend occupies port `8080`, configure Tomcat to run on port `8081`:
1. Open Tomcat configuration file:
   ```text
   <TOMCAT_HOME>/conf/server.xml
   ```
2. Locate the HTTP Connector:
   ```xml
   <Connector port="8080" protocol="HTTP/1.1"
              connectionTimeout="20000"
              redirectPort="8443" />
   ```
3. Change `port="8080"` to `port="8081"`:
   ```xml
   <Connector port="8081" protocol="HTTP/1.1"
              connectionTimeout="20000"
              redirectPort="8443" />
   ```

### Step 2: Build the WAR Package
Navigate to the `disease-prediction-web` directory and run:
```bash
mvn clean package
```
Upon a successful build, the WAR file is generated at:
```text
disease-prediction-web/target/disease-prediction-web.war
```

### Step 3: Deploy to Tomcat 10.1
1. Copy the generated `disease-prediction-web.war` to Tomcat's `webapps/` folder:
   ```bash
   cp target/disease-prediction-web.war <TOMCAT_HOME>/webapps/
   ```
2. Start Apache Tomcat:
   - **Windows**:
     ```powershell
     <TOMCAT_HOME>\bin\startup.bat
     ```
   - **Linux / macOS**:
     ```bash
     <TOMCAT_HOME>/bin/startup.sh
     ```
3. Access the web application in your browser:
   ```text
   http://localhost:8081/disease-prediction-web/
   ```

---

## 5. Verification & Testing

The application includes automated JUnit 5 tests covering:
- Live endpoint connectivity and symptoms retrieval against Spring Boot.
- Patient registration request formatting and response handling.
- Input validation: blank patient names, invalid ages, and missing symptom selections.
- History retrieval and PDF streaming headers.

Run tests anytime with:
```bash
mvn test
```

---

## 6. Mandatory Medical Disclaimer

> **DISCLAIMER**: MediPredict is an educational software engineering and research project. The predictions and probability estimates generated by this system are produced by statistical machine learning models and are intended solely for academic demonstration and clinical decision support research. This software is **not** a certified medical diagnostic device and must **not** be used as a substitute for professional clinical judgment, medical diagnosis, or patient treatment planning.
