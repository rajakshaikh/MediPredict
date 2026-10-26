# MediPredict — Comprehensive Project Defense & Source Code Walkthrough
**Project Title:** Disease Prediction by Symptoms Using Machine Learning  
**Author:** Arfat Patel  
**Repository:** [Patelarfat/disease-prediction-springboot](https://github.com/Patelarfat/disease-prediction-springboot)  
**Date:** October 2026  

---

## Table of Contents
1. [Verified Project Structure & Local Inspection](#1-verified-project-structure--local-inspection)
2. [Step-by-Step Trace of ONE General Disease Prediction Request](#2-step-by-step-trace-of-one-general-disease-prediction-request)
3. [Detailed Walkthrough of Crucial Java Files](#3-detailed-walkthrough-of-crucial-java-files)
4. [Database Schema & Relational Mapping (MySQL)](#4-database-schema--relational-mapping-mysql)
5. [Machine Learning Integration Details](#5-machine-learning-integration-details)
6. [Other Key Feature Workflows](#6-other-key-feature-workflows)
7. [Viva Presentation Script (5 Minutes)](#7-viva-presentation-script-5-minutes)
8. [Top 20 Viva Questions & Model Answers](#8-top-20-viva-questions--model-answers)
9. [Last-Minute Quick Revision Cheat Sheet](#9-last-minute-quick-revision-cheat-sheet)

---

## 1. Verified Project Structure & Local Inspection

Both the **Java Spring Boot backend** and the **Python FastAPI ML microservice** exist locally and have been thoroughly inspected:

### Java Spring Boot Project (`D:\Buildathon2.0\Project2\disease-prediction-springboot`)
```text
disease-prediction-springboot/
├── pom.xml                                           # Maven dependencies (Spring Boot 3.3.4, Java 21, JPA, MySQL, OpenPDF)
├── src/main/resources/
│   ├── application.properties                        # Port 8080, MySQL datasource, ml.service.base-url=http://localhost:8000
│   └── static/                                       # Embedded web dashboard
│       ├── index.html                                # Dashboard interface & symptom checkboxes
│       ├── css/style.css                             # Unified styles & typography
│       └── js/
│           ├── api.js                                # Centralized Fetch API client
│           └── app.js                                # Event listeners, DOM manipulation & state
├── src/main/java/com/diseaseprediction/
│   ├── DiseasePredictionApplication.java             # Spring Boot main class
│   ├── config/
│   │   ├── CorsConfig.java                           # Global Cross-Origin configuration
│   │   └── RestClientConfig.java                     # RestTemplate Bean definition
│   ├── controller/
│   │   ├── PatientController.java                    # /api/patients (Registration, Predict, PDF)
│   │   ├── MlPredictionController.java               # /api/ml (Direct pass-throughs)
│   │   └── PredictionHistoryController.java          # /api/predictions (Audit trail)
│   ├── service/
│   │   ├── PatientService.java                       # Patient CRUD & entity validation
│   │   ├── PredictionService.java                    # Core workflow orchestrator
│   │   ├── MlService.java                            # RestTemplate client to Python FastAPI
│   │   └── PdfReportService.java                     # In-memory OpenPDF report generator
│   ├── model/
│   │   ├── Patient.java                              # Entity: 'patients' table
│   │   └── PredictionHistory.java                    # Entity: 'prediction_history' table
│   ├── repository/
│   │   ├── PatientRepository.java                    # JpaRepository for Patient
│   │   └── PredictionHistoryRepository.java          # JpaRepository for PredictionHistory
│   ├── dto/
│   │   ├── request/                                  # DiseasePredictionRequest, HeartDiseasePredictionRequest, PatientRequest
│   │   └── response/                                 # DiseasePredictionResponse, PatientDiseasePredictionResponse, etc.
│   └── exception/
│       ├── GlobalExceptionHandler.java               # @RestControllerAdvice for unified errors
│       ├── MlServiceException.java                   # Custom runtime exception for ML failures
│       └── ResourceNotFoundException.java            # Custom 404 exception
```

### Python FastAPI ML Project (`D:\Buildathon2.0\Project2\disease-prediction-ml`)
```text
disease-prediction-ml/
├── requirements.txt                                  # fastapi, uvicorn, scikit-learn, pandas, numpy, joblib
├── backend/
│   └── app.py                                        # FastAPI server (Port 8000), /predict, /symptoms, /predict-heart-disease
└── models/
    ├── disease_model.pkl                             # Trained Random Forest general disease model
    ├── label_encoder.pkl                             # Scikit-Learn LabelEncoder (41 disease classes)
    ├── symptoms.pkl                                  # Serialized list of 132 symptom names
    └── heart_disease_model.pkl                       # Trained Random Forest cardiovascular model
```

---

## 2. Step-by-Step Trace of ONE General Disease Prediction Request

Let us trace what happens when the user enters **Target Patient ID: `2`**, selects symptoms **`itching`** and **`skin_rash`**, and clicks **"Predict Disease"**.

```
[Browser UI] 
     │ (Click 'Predict Disease' with Patient ID: 2, Symptoms: ["itching", "skin_rash"])
     ▼
[app.js: handleGeneralPredictSubmit()]
     │
     ▼
[api.js: predictGeneralDisease(2, symptoms)]
     │
     │ HTTP POST http://localhost:8080/api/patients/2/predict
     │ Payload: {"symptoms": ["itching", "skin_rash"]}
     ▼
[PatientController.java: predictGeneralDisease()]
     │
     ▼
[PredictionService.java: predictAndSaveGeneralDisease(2, request)]
     ├─► 1. patientService.getPatientEntity(2) ──► checks MySQL 'patients' table
     │
     ├─► 2. MlService.java: predictGeneralDisease(request)
     │        │
     │        │ HTTP POST http://localhost:8000/predict
     │        │ Payload: {"symptoms": ["itching", "skin_rash"]}
     │        ▼
     │   [Python backend/app.py: predict_disease()]
     │        ├─ Maps 2 symptoms into 132-element binary vector [1, 0, 1, 0, ...]
     │        ├─ disease_model.predict(input_df) ──► class index 15
     │        ├─ label_encoder.inverse_transform([15]) ──► "Fungal infection"
     │        └─ disease_model.predict_proba(input_df).max() ──► 0.82 (82.0%)
     │        │
     │        ▼ Returns JSON: {"prediction": "Fungal infection", "probability": 82.0, ...}
     │
     ├─► 3. Creates PredictionHistory entity (patient_id=2, "GENERAL_DISEASE", "Fungal infection", 82.0)
     │        │
     │        ▼
     │   [PredictionHistoryRepository.java: save(history)]
     │        │
     │        ▼ Executes SQL INSERT into MySQL 'prediction_history' table
     │
     └─► 4. Returns PatientDiseasePredictionResponse DTO
     │
     ▼ HTTP 200 OK with JSON response
[app.js: Update DOM with Disease Name, 82.0% progress bar, configure PDF download button]
```

---

### Step 1: Frontend Event Listener
* **File:** `src/main/resources/static/js/app.js` (Lines 540–554)
* **Code:**
  ```javascript
  const predictBtn = document.getElementById('general-predict-btn');
  if (predictBtn) {
    predictBtn.addEventListener('click', handleGeneralPredictSubmit);
  }
  ```
* **Explanation:**
  * **English:** The browser attaches a click listener to the HTML button with ID `general-predict-btn`. When clicked, it calls `handleGeneralPredictSubmit`.
  * **Hinglish:** *HTML ke Predict button par click hote hi JavaScript function `handleGeneralPredictSubmit` trigger hota hai.*

---

### Step 2: Form Validation & Extraction
* **File:** `src/main/resources/static/js/app.js` (Lines 564–584)
* **Code:**
  ```javascript
  const rawPatientId = patientIdInput.value.trim();
  const targetPatientId = Number(rawPatientId);
  // ... validation checks ...
  const symptomsArray = Array.from(state.selectedSymptoms);
  const res = await api.predictGeneralDisease(targetPatientId, symptomsArray);
  ```
* **Explanation:**
  * **English:** It extracts the target patient ID from the input box (e.g. `2`) as the single source of truth and converts the selected symptoms `Set` into a JavaScript Array. It then calls `api.predictGeneralDisease`.
  * **Hinglish:** *User ne input box me jo Target Patient ID dali hai (jaise `2`), usko read karta hai aur selected symptoms ki list banakar `api.predictGeneralDisease` ko bhejta hai.*

---

### Step 3 & 4: HTTP Request Formulation
* **File:** `src/main/resources/static/js/api.js` (Lines 73–78)
* **Code:**
  ```javascript
  async predictGeneralDisease(patientId, symptoms) {
    return this.request(`/api/patients/${patientId}/predict`, {
      method: 'POST',
      body: JSON.stringify({ symptoms })
    });
  }
  ```
* **Exact HTTP Request:**
  * **Method:** `POST`
  * **URL:** `http://localhost:8080/api/patients/2/predict`
  * **Headers:** `Content-Type: application/json`, `Accept: application/json`
  * **Payload:**
    ```json
    {
      "symptoms": ["itching", "skin_rash"]
    }
    ```

---

### Step 5 & 6: Java Controller Entry Point
* **File:** `src/main/java/com/diseaseprediction/controller/PatientController.java` (Lines 61–67)
* **Code:**
  ```java
  @PostMapping("/{id}/predict")
  public ResponseEntity<PatientDiseasePredictionResponse> predictGeneralDisease(
          @PathVariable Long id,
          @Valid @RequestBody DiseasePredictionRequest request) {
      PatientDiseasePredictionResponse response = predictionService.predictAndSaveGeneralDisease(id, request);
      return ResponseEntity.ok(response);
  }
  ```
* **Explanation:**
  * **English:** `@PostMapping("/{id}/predict")` matches the URL. `@PathVariable Long id` receives `2L`. `@Valid @RequestBody` parses the JSON body into a `DiseasePredictionRequest` object and enforces that the symptom list cannot be empty. It then invokes `predictionService.predictAndSaveGeneralDisease(2L, request)`.
  * **Hinglish:** *Spring Boot ka `PatientController` request receive karta hai, URL se ID `2` nikalta hai, JSON payload ko Java DTO me convert karta hai, aur `predictionService` ko aage handover kar deta hai.*

---

### Step 7 & 8: Patient Verification in Java Service
* **File:** `src/main/java/com/diseaseprediction/service/PredictionService.java` (Lines 55–58)
* **Code:**
  ```java
  public PatientDiseasePredictionResponse predictAndSaveGeneralDisease(Long patientId, DiseasePredictionRequest request) {
      Patient patient = patientService.getPatientEntity(patientId);
      DiseasePredictionResponse mlResponse = mlService.predictGeneralDisease(request);
      // ...
  ```
* **Where `patientService.getPatientEntity` is:**
  `src/main/java/com/diseaseprediction/service/PatientService.java` (Lines 78–82):
  ```java
  public Patient getPatientEntity(Long id) {
      return patientRepository.findById(id)
              .orElseThrow(() -> new ResourceNotFoundException("Patient not found with ID: " + id));
  }
  ```
* **Explanation:**
  * **English:** Before contacting the ML model, it queries MySQL via `patientRepository.findById(2L)`. If patient ID 2 does not exist, it throws `ResourceNotFoundException`, which causes `GlobalExceptionHandler` to immediately return an HTTP 404 response.
  * **Hinglish:** *ML service ko call karne se pehle ye check hota hai ki Patient #2 MySQL database me exist karta hai ya nahi. Agar patient nahi mila toh turant 404 Not Found exception throw ho jata hai.*

---

### Step 9 & 10: Spring Boot Calls Python FastAPI Microservice
* **File:** `src/main/java/com/diseaseprediction/service/MlService.java` (Lines 66–78)
* **Code:**
  ```java
  public DiseasePredictionResponse predictGeneralDisease(DiseasePredictionRequest request) {
      String url = mlBaseUrl + "/predict"; // http://localhost:8000/predict
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<DiseasePredictionRequest> entity = new HttpEntity<>(request, headers);

      ResponseEntity<DiseasePredictionResponse> response =
              restTemplate.postForEntity(url, entity, DiseasePredictionResponse.class);

      DiseasePredictionResponse body = response.getBody();
      // ... validation ...
      return body;
  }
  ```
* **Exact Internal HTTP Request (Java $\rightarrow$ Python):**
  * **Method:** `POST`
  * **URL:** `http://localhost:8000/predict`
  * **Payload:**
    ```json
    {
      "symptoms": ["itching", "skin_rash"]
    }
    ```

---

### Step 11, 12 & 13: Python Machine Learning Inference
* **File:** `backend/app.py` (Lines 192–276 in `disease-prediction-ml`)
* **Code Snippet:**
  ```python
  @app.post("/predict")
  def predict_disease(request: SymptomRequest):
      # 1. Normalize symptoms into a set
      selected_symptoms = {s.strip().lower() for s in request.symptoms}

      # 2. Build 132-element binary feature vector (0 or 1)
      input_data = [1 if s.strip().lower() in selected_symptoms else 0 for s in symptoms]

      # 3. Create single-row pandas DataFrame
      input_df = pd.DataFrame([input_data], columns=symptoms)

      # 4. Predict disease class index using Random Forest
      prediction = disease_model.predict(input_df)

      # 5. Decode numeric class to disease name string
      disease_name = label_encoder.inverse_transform(prediction)[0]

      # 6. Calculate probability percentage
      probabilities = disease_model.predict_proba(input_df)
      probability = round(float(probabilities.max()) * 100, 2)

      return {
          "prediction": disease_name,
          "probability": probability,
          "selected_symptoms": request.symptoms,
          "message": "Prediction generated successfully by the machine learning model."
      }
  ```
* **Python Response JSON received by Java:**
  ```json
  {
    "prediction": "Fungal infection",
    "probability": 82.0,
    "selected_symptoms": ["itching", "skin_rash"],
    "message": "Prediction generated successfully by the machine learning model."
  }
  ```
* **Explanation:**
  * **English:** Python aligns the incoming symptoms against its ordered list of 132 symptom names (`symptoms.pkl`). It sets `1` for matched symptoms and `0` for absent symptoms, passing a $1 \times 132$ vector to `disease_model.predict()`. The numeric label is decoded via `label_encoder.pkl` to `"Fungal infection"`, and `predict_proba()` computes the confidence score of `82.0%`.
  * **Hinglish:** *Python 132 symptoms ki list me se user ke symptoms ko match karke 1 aur 0 ka binary array banata hai. Trained Random Forest model is array ko classify karke disease ka naam aur probability percentage return karta hai.*

---

### Step 14, 15 & 16: Java Backend Saves Result to MySQL Database
* **File:** `src/main/java/com/diseaseprediction/service/PredictionService.java` (Lines 60–89)
* **Code:**
  ```java
  String inputJson = objectMapper.writeValueAsString(request.getSymptoms());

  PredictionHistory history = new PredictionHistory(
          patient,                                  // Linked to Patient entity (id = 2)
          "GENERAL_DISEASE",                        // prediction_type
          inputJson,                                // '["itching","skin_rash"]'
          mlResponse.getPrediction(),               // "Fungal infection"
          mlResponse.getProbability()               // 82.0
  );

  PredictionHistory saved = predictionHistoryRepository.save(history);
  ```
* **SQL Query Executed by Hibernate in MySQL:**
  ```sql
  INSERT INTO prediction_history (patient_id, prediction_type, input_data, predicted_disease, probability, created_at)
  VALUES (2, 'GENERAL_DISEASE', '["itching","skin_rash"]', 'Fungal infection', 82.0, '2026-10-16 20:45:00');
  ```
* **Explanation:**
  * **English:** `PredictionService` uses Jackson's `ObjectMapper` to serialize the symptom array into a JSON string. It creates a `PredictionHistory` entity linked to the `Patient` entity (foreign key `patient_id = 2`). Calling `predictionHistoryRepository.save(history)` executes an SQL `INSERT` statement in MySQL.
  * **Hinglish:** *Java backend user ke symptoms ko JSON string banata hai aur `PredictionHistory` table me Patient ID `2` ke foreign key ke saath record permanently save kar deta hai.*

---

### Step 17: Java Returns Response DTO to Frontend
* **DTO:** `src/main/java/com/diseaseprediction/dto/response/PatientDiseasePredictionResponse.java`
* **HTTP Status:** `200 OK`
* **JSON Payload returned to Browser:**
  ```json
  {
    "id": 15,
    "patientId": 2,
    "patientName": "Arfat",
    "predictionType": "GENERAL_DISEASE",
    "prediction": "Fungal infection",
    "probability": 82.0,
    "selectedSymptoms": ["itching", "skin_rash"],
    "message": "Prediction generated successfully by the machine learning model.",
    "createdAt": "2026-10-16T20:45:00.123"
  }
  ```

---

### Step 18: Frontend Displays the Result
* **File:** `src/main/resources/static/js/app.js` (Lines 593–622)
* **Code:**
  ```javascript
  document.getElementById('general-result-disease').textContent = res.prediction;
  document.getElementById('general-result-prob-text').textContent = `${res.probability}%`;
  document.getElementById('general-result-prob-bar').style.width = `${res.probability}%`;
  document.getElementById('general-result-patient').textContent = `${res.patientName} (#${res.patientId})`;

  const downloadBtn = document.getElementById('general-download-pdf-btn');
  downloadBtn.onclick = () => {
    window.location.href = api.getReportUrl(res.patientId, res.id); // /api/patients/2/predictions/15/report
  };

  resultCard.style.display = 'block';
  resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  ```
* **Explanation:**
  * **English:** `app.js` updates the DOM elements with the disease name ("Fungal infection"), animates the probability meter to 82%, wires the PDF download button specifically to `/api/patients/2/predictions/15/report`, and displays the result card.
  * **Hinglish:** *Browser me JavaScript DOM update karti hai: disease ka naam 'Fungal infection' aur 82% progress bar dikhaya jata hai, aur Download PDF button ko is naye prediction record (#15) se link kar diya jata hai.*

---

## 3. Detailed Walkthrough of Crucial Java Files

### 1. `PredictionService.java`
* **Path:** `src/main/java/com/diseaseprediction/service/PredictionService.java`
* **Purpose:** The central coordinator of the entire application.
* **Key Code Lines:**
  * `@Service`: Tells Spring to create and manage this class as a Singleton Bean in the IoC container.
  * `@Transactional`: Wraps each method in a database transaction. If saving fails, changes roll back automatically.
  * Constructor Injection: Injects `PatientService`, `MlService`, `PredictionHistoryRepository`, `ObjectMapper`, and `PdfReportService`.
* **Viva Question:** *Why did you put the save logic in `PredictionService` instead of `PatientController`?*
  * **Answer:** To follow the **Single Responsibility Principle** and **Layered Architecture**. Controllers should only handle HTTP routing and response formatting; business workflows and database transactions belong in the Service layer.

---

### 2. `MlService.java`
* **Path:** `src/main/java/com/diseaseprediction/service/MlService.java`
* **Purpose:** The outbound HTTP client communicating with Python FastAPI.
* **Key Code Lines:**
  * `@Value("${ml.service.base-url:http://localhost:8000}")`: Reads the URL from `application.properties`.
  * `restTemplate.postForEntity(url, entity, DiseasePredictionResponse.class)`: Sends the HTTP POST request to Python synchronously and deserializes the JSON response into Java DTO.
  * Exception Handling:
    ```java
    catch (ResourceAccessException ex) {
        log.error("ML service unreachable at {}: {}", url, ex.getMessage());
        throw ex; // Caught by GlobalExceptionHandler to return HTTP 503
    }
    ```
* **Viva Question:** *What happens if the Python service is offline?*
  * **Answer:** `RestTemplate` throws a `ResourceAccessException`. Our `GlobalExceptionHandler` intercepts it and returns a clean HTTP 503 Service Unavailable with the message: *"Python ML service is unavailable on port 8000"*.

---

### 3. `Patient.java` (JPA Entity)
* **Path:** `src/main/java/com/diseaseprediction/model/Patient.java`
* **Purpose:** Maps to the MySQL `patients` table.
* **Key Code Lines:**
  * `@Entity` and `@Table(name = "patients")`: Marks this as a database table entity.
  * `@Id @GeneratedValue(strategy = GenerationType.IDENTITY)`: Relies on MySQL auto-increment primary key.
  * `@Column(unique = true)` on `email`: Enforces email uniqueness at the database level.
  * `@OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)`: Defines a One-to-Many relationship with `PredictionHistory`. If a patient is deleted, all their prediction records are automatically deleted.
  * `@PrePersist`: Automatically sets `createdAt = LocalDateTime.now()` before inserting into MySQL.

---

### 4. `PredictionHistory.java` (JPA Entity)
* **Path:** `src/main/java/com/diseaseprediction/model/PredictionHistory.java`
* **Purpose:** Maps to the MySQL `prediction_history` table.
* **Key Code Lines:**
  * `@ManyToOne(fetch = FetchType.LAZY)`: Many prediction records belong to one patient. `LAZY` fetching ensures the patient entity is only loaded from the database when explicitly accessed.
  * `@JoinColumn(name = "patient_id", nullable = false)`: Creates the actual foreign key column in MySQL.
  * `@Column(name = "input_data", columnDefinition = "TEXT")`: Uses SQL `TEXT` data type to store large JSON strings of symptoms or heart biomarker inputs.

---

### 5. `GlobalExceptionHandler.java`
* **Path:** `src/main/java/com/diseaseprediction/exception/GlobalExceptionHandler.java`
* **Purpose:** Centralized, intercepting exception handler for all REST controllers.
* **Key Code Lines:**
  * `@RestControllerAdvice`: Intercepts exceptions thrown anywhere in `@RestController` classes.
  * `@ExceptionHandler(ResourceNotFoundException.class)`: Returns HTTP 404 with timestamp and error message.
  * `@ExceptionHandler(MethodArgumentNotValidException.class)`: Catches bean validation errors (e.g. empty symptom list or negative age) and returns an HTTP 400 Bad Request with field-by-field error maps.
  * `@ExceptionHandler(ResourceAccessException.class)`: Catches network connection failures to Python and returns HTTP 503.

---

### 6. `PdfReportService.java`
* **Path:** `src/main/java/com/diseaseprediction/service/PdfReportService.java`
* **Purpose:** Dynamic, in-memory PDF clinical summary generation using OpenPDF (`com.github.librepdf:openpdf:1.3.40`).
* **Key Code Lines:**
  * `ByteArrayOutputStream out = new ByteArrayOutputStream()`: Generates the PDF in RAM rather than creating temporary files on the hard drive.
  * Adds header with hospital branding and timestamp.
  * Formats a demographic table (Patient Name, Age, Gender, Record ID).
  * Draws a colored probability indicator bar.
  * Appends the mandatory legal **Medical Disclaimer**.
  * Returns `out.toByteArray()`.

---

## 4. Database Schema & Relational Mapping (MySQL)

Hibernate automatically creates and maintains two tables based on `application.properties` (`spring.jpa.hibernate.ddl-auto=update`):

```sql
-- 1. Patients Table
CREATE TABLE patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT,
    gender VARCHAR(50),
    email VARCHAR(255) UNIQUE,
    created_at DATETIME NOT NULL
);

-- 2. Prediction History Table
CREATE TABLE prediction_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    prediction_type VARCHAR(50) NOT NULL,
    input_data TEXT,
    predicted_disease VARCHAR(255) NOT NULL,
    probability DOUBLE,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_prediction_patient 
        FOREIGN KEY (patient_id) REFERENCES patients(id) 
        ON DELETE CASCADE
);
```

### How Referential Integrity Is Enforced:
1. **Foreign Key Constraint:** In `PredictionHistory.java`, `@JoinColumn(name = "patient_id", nullable = false)` creates an explicit foreign key referencing `patients(id)`.
2. **Invalid Patient ID Handling:** If someone submits a prediction for Patient ID `999` which does not exist, `patientRepository.findById(999L)` in `PatientService` throws `ResourceNotFoundException`. The ML model is never invoked, and nothing is inserted into the database.
3. **Cascading Deletion:** If a patient is deleted, Hibernate's `cascade = CascadeType.ALL, orphanRemoval = true` automatically removes all related prediction history records.

---

## 5. Machine Learning Integration Details

### Verified Facts from `backend/app.py`:
* **Framework:** FastAPI running on Uvicorn (Port 8000).
* **Models:**
  * General Disease: Random Forest Classifier (`models/disease_model.pkl`) trained on **132 symptoms** predicting **41 disease classes**.
  * Heart Disease: Random Forest Classifier (`models/heart_disease_model.pkl`) evaluating **13 clinical parameters** (`age`, `sex`, `cp`, `trestbps`, `chol`, `fbs`, `restecg`, `thalach`, `exang`, `oldpeak`, `slope`, `ca`, `thal`).
* **Input Vector Transformation:**
  * Python reads the user's symptoms list.
  * It creates a binary vector of length 132: `1` if symptom is selected, `0` if absent.
  * It passes the vector to `disease_model.predict(input_df)` for the disease name and `disease_model.predict_proba(input_df)` for the maximum confidence probability.
* **Why Java and Python are separated:**
  * **Java** is built for enterprise concurrency, database ACID transactions, relational integrity, type safety, and PDF generation.
  * **Python** is the native ecosystem for Machine Learning libraries (Scikit-Learn, NumPy, Pandas).
  * Running them as decoupled microservices over HTTP REST is standard industry architecture.

---

## 6. Other Key Feature Workflows

### Feature 1: Patient Registration
* **Frontend:** Form submission calls `api.registerPatient({name, age, gender, email})`.
* **API:** `POST /api/patients`
* **Controller:** `PatientController.registerPatient(@Valid @RequestBody PatientRequest request)`.
* **Service:** `PatientService.registerPatient()` checks `patientRepository.existsByEmail(email)`. If email exists, throws `IllegalArgumentException`. Otherwise, saves `new Patient(...)` to MySQL and returns HTTP `201 Created`.

### Feature 2: Heart Disease Prediction
* **Frontend:** User inputs 13 parameters $\rightarrow$ calls `api.predictHeartDisease(patientId, heartData)`.
* **API:** `POST /api/patients/{patientId}/predict-heart-disease`
* **Controller & Service:** `PatientController` delegates to `PredictionService.predictAndSaveHeartDisease()`.
* **ML Service:** Java calls `POST http://localhost:8000/predict-heart-disease`. Python classifies input using `heart_disease_model.pkl`.
* **Persistence:** Saved in `prediction_history` with `prediction_type = "HEART_DISEASE"`.

### Feature 3: Prediction History Retrieval
* **Frontend:** Clicks "Prediction History" $\rightarrow$ calls `api.getPatientPredictions(patientId)`.
* **API:** `GET /api/patients/{patientId}/predictions`
* **Database Query:** `predictionHistoryRepository.findByPatientIdOrderByCreatedAtDesc(patientId)`.
* **Response:** Array of `PredictionHistoryResponse` objects sorted newest first.

### Feature 4: PDF Report Generation
* **Frontend:** Clicks "Download PDF Report" $\rightarrow$ opens `GET /api/patients/{patientId}/predictions/{predictionId}/report`.
* **Controller:** `PatientController.downloadReport()` retrieves `Patient` and `PredictionHistory` records.
* **Service:** `PdfReportService.generatePredictionReport()` draws document tables and probability meter in RAM using OpenPDF.
* **Response:** Streams `byte[]` with header `Content-Disposition: attachment; filename="MedPredict_Report_<patientId>_<predictionId>.pdf"`.

---

## 7. Viva Presentation Script (5 Minutes)

> *(Deliver this with confidence and clarity!)*

"Good morning respected professors and examiners. Today, I am presenting my project: **MediPredict — Disease Prediction & Clinical Decision Support System**.

### 1. Problem Statement
In clinical environments, preliminary diagnosis can be challenging when patients present overlapping symptoms. Most online symptom tools are isolated widgets that provide a prediction and discard the data immediately. There is no audit trail, no patient linkage, and no medical record persistence.

### 2. Our Solution
To solve this, I designed and developed **MediPredict**. It is an enterprise platform combining **Java Spring Boot** with a **Python Machine Learning microservice**.
1. Clinicians register a patient in MySQL.
2. They select observed symptoms from a catalog of 132 medical symptoms, or input 13 cardiovascular biomarkers.
3. The machine learning model analyzes the inputs and predicts the condition with a confidence probability.
4. The prediction is automatically persisted in MySQL under that patient's clinical history.
5. Finally, the clinician can download a publication-grade PDF clinical summary with one click.

### 3. Architecture & Tech Stack
I utilized a decoupled **Microservice Architecture**:
- **Frontend:** Responsive dashboard built using HTML5, CSS3, and modern Vanilla JavaScript without bloated frameworks.
- **Backend:** Java 21 and Spring Boot 3.3.4. It handles data validation, transactional database operations, and PDF generation.
- **Database:** MySQL with Hibernate ORM managing foreign-key relationships between patients and their prediction histories.
- **ML Microservice:** Python FastAPI running Random Forest classifiers (`disease_model.pkl` and `heart_disease_model.pkl`).
- **Communication:** Communication between Java and Python happens over standardized RESTful HTTP APIs exchanging JSON.

### 4. Live Demonstration
*(Show the browser at `http://localhost:8080`)*
- First, we register a patient, 'Arfat', age 22. It is saved directly in MySQL (`POST /api/patients`, HTTP 201).
- Next, on the General Disease Prediction page, we select symptoms: 'itching' and 'skin_rash'.
- We click 'Predict Disease'. Spring Boot receives the request, validates the patient, sends the symptoms to Python FastAPI, gets the Random Forest classification ('Fungal infection', 82%), saves it in MySQL with foreign key linkage, and displays the result.
- We click 'Download PDF Report', which uses OpenPDF to generate a clinical document in RAM and trigger a download.
- Under 'Prediction History', we can inspect the full chronological audit trail.

Thank you, professors. I am now open for any technical questions!"

---

## 8. Top 20 Viva Questions & Model Answers

### Architecture & ML Integration
1. **Q: Why did you separate Java and Python into two services?**
   * **Answer:** Python is the industry leader for Machine Learning (Scikit-Learn, NumPy), while Java Spring Boot is superior for enterprise business logic, relational transactions, type safety, and security. Connecting them via REST HTTP APIs adheres to modern microservice architecture.
2. **Q: How does Java communicate with Python?**
   * **Answer:** Spring Boot's `MlService` uses `RestTemplate` to send an HTTP POST request containing a JSON symptom payload to `http://localhost:8000/predict`. Python returns the predicted disease and probability.
3. **Q: What algorithm is used for disease prediction?**
   * **Answer:** A **Random Forest Classifier**. It creates an ensemble of decision trees and combines their votes to determine the predicted disease and maximum voting probability.
4. **Q: How many symptoms and diseases does the general model support?**
   * **Answer:** It supports **132 symptoms** across **41 disease classes**.
5. **Q: What happens if the Python ML service is offline?**
   * **Answer:** `RestTemplate` throws a `ResourceAccessException`. Our `GlobalExceptionHandler` intercepts it and returns HTTP 503 Service Unavailable with the message: *"Python ML service is unavailable on port 8000"*.

### Spring Boot & Java Concepts
6. **Q: What is the purpose of `@SpringBootApplication`?**
   * **Answer:** It is a convenience annotation that bundles `@SpringBootConfiguration`, `@EnableAutoConfiguration` (configures beans automatically from classpath dependencies), and `@ComponentScan` (scans current and child packages for Spring components).
7. **Q: What is the difference between `@Controller` and `@RestController`?**
   * **Answer:** `@Controller` returns traditional view names (like HTML/JSP). `@RestController` combines `@Controller` and `@ResponseBody`, ensuring return values are automatically serialized into JSON.
8. **Q: Why do you use Constructor Injection instead of `@Autowired`?**
   * **Answer:** Constructor injection makes dependencies immutable (`final`), prevents `NullPointerException` during testing, and makes dependencies explicit without reflection.
9. **Q: What is the role of `@Transactional` in `PredictionService`?**
   * **Answer:** It guarantees that all database operations in a method execute within a database transaction. If an unexpected error occurs, changes roll back to keep the database consistent.
10. **Q: Why do you use DTOs (Data Transfer Objects)?**
    * **Answer:** DTOs decouple external API contracts from internal database entity structures. This prevents over-fetching, protects entities from unauthorized mass-assignment, and allows dedicated validation rules.
11. **Q: How does validation work in your project?**
    * **Answer:** Using Jakarta Validation annotations (like `@NotBlank`, `@Min`, `@Email`) in request DTOs, triggered by `@Valid` in `@RestController` methods.
12. **Q: What does `@RestControllerAdvice` do?**
    * **Answer:** It provides global, centralized exception handling across all controllers using `@ExceptionHandler` methods to return uniform JSON error responses.

### Database & JPA Concepts
13. **Q: What is Hibernate?**
    * **Answer:** Hibernate is an Object-Relational Mapping (ORM) framework that implements the Jakarta Persistence API (JPA), translating Java entity objects into relational database tables and SQL queries.
14. **Q: What is the relationship between `Patient` and `PredictionHistory`?**
    * **Answer:** A **One-to-Many** relationship (`@OneToMany` in `Patient`, `@ManyToOne` in `PredictionHistory`), linked by foreign key `patient_id`.
15. **Q: What does `cascade = CascadeType.ALL, orphanRemoval = true` do?**
    * **Answer:** If a patient is deleted from the database, all associated prediction history records are automatically deleted to prevent orphaned records.
16. **Q: What is the difference between `FetchType.LAZY` and `FetchType.EAGER`?**
    * **Answer:** `EAGER` loads related entities immediately. `LAZY` loads related entities on-demand when accessed, saving memory and query overhead.
17. **Q: What does `spring.jpa.hibernate.ddl-auto=update` do?**
    * **Answer:** On startup, Hibernate inspects entity classes and automatically updates the MySQL database schema (tables, columns, foreign keys) without deleting existing data.
18. **Q: How does `existsByEmail(String email)` work without writing SQL?**
    * **Answer:** Spring Data JPA parses the method name using derived query conventions and automatically generates `SELECT COUNT(*) FROM patients WHERE email = ?`.

### PDF Generation & Security
19. **Q: How is the PDF generated and returned?**
    * **Answer:** `PdfReportService` uses the **OpenPDF** library to draw document tables and probability bars in RAM inside a `ByteArrayOutputStream`. It is streamed over HTTP as a `byte[]` with `Content-Type: application/pdf`.
20. **Q: What is CORS and why is `CorsConfig.java` needed?**
    * **Answer:** Cross-Origin Resource Sharing. Browsers block frontend scripts from making requests to different ports/origins by default. `CorsConfig` configures Spring Security to permit requests from the web dashboard.

---

## 9. Last-Minute Quick Revision Cheat Sheet

* **Port 8080:** Java Spring Boot Backend (`http://localhost:8080`)
* **Port 8000:** Python FastAPI ML Service (`http://localhost:8000`)
* **Port 3306:** MySQL Database (`disease_prediction_db`)
* **General Disease Model:** Random Forest, 132 Symptoms $\rightarrow$ 41 Diseases (`disease_model.pkl`)
* **Heart Disease Model:** Random Forest, 13 Biomarkers $\rightarrow$ Binary Classification (`heart_disease_model.pkl`)
* **PDF Library:** OpenPDF (`com.github.librepdf:openpdf:1.3.40`)
* **Database Tables:** `patients` (PK: `id`), `prediction_history` (PK: `id`, FK: `patient_id`)
* **Testing:** 24 JUnit 5 & Mockito test cases passing with H2 in-memory database (`mvn clean test`)
* **Command to run ML:** `uvicorn backend.app:app --host 0.0.0.0 --port 8000 --reload`
* **Command to run Java:** `$env:DB_PASSWORD="your_password"; mvn spring-boot:run`
