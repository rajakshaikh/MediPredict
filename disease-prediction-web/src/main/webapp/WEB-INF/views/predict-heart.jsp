<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Heart Disease Risk Assessment" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Heart Disease Risk Assessment</h2>
            <p class="top-bar-subtitle">Cardiovascular risk evaluation using 13 clinical biomarkers and laboratory metrics</p>
        </div>
    </header>

    <div class="content-body">
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <strong>Error:</strong> <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/predict/heart" method="POST" id="heart-predict-form" class="styled-form">
            <input type="hidden" name="predictionType" value="heart">

            <!-- Target Patient Selector Card -->
            <div class="card patient-selector-card">
                <div class="card-header">
                    <h3>1. Select Target Patient Profile</h3>
                    <span class="field-hint">Prediction history will be permanently saved to this patient in MySQL</span>
                </div>
                <div class="patient-selector-controls">
                    <div class="form-group" style="flex: 1; max-width: 400px;">
                        <label for="patientIdSelect">Choose Registered Patient *</label>
                        <select id="patientIdSelect" class="form-control" onchange="syncPatientId(this.value)">
                            <option value="">-- Select Registered Patient --</option>
                            <c:forEach var="p" items="${patients}">
                                <option value="${p.id}" ${selectedPatientId == p.id.toString() ? 'selected' : ''}>
                                    #${p.id} — ${p.name} (${p.gender}, ${p.age} yrs)
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="form-group" style="width: 140px;">
                        <label for="patientId">Or Patient ID *</label>
                        <input type="number" id="patientId" name="patientId" min="1" class="form-control"
                               placeholder="e.g. 1" required value="<c:out value='${selectedPatientId}' />">
                    </div>

                    <div style="align-self: flex-end; padding-bottom: 2px;">
                        <a href="${pageContext.request.contextPath}/patients" class="btn btn-outline btn-sm">
                            + Enroll New Patient
                        </a>
                    </div>
                </div>
            </div>

            <!-- 13 Clinical Parameters Form Card -->
            <div class="card" style="margin-top: 24px;">
                <div class="card-header">
                    <h3>2. Enter 13 Clinical Diagnostic Biomarkers</h3>
                    <span class="field-hint">Validated clinical parameters required by Random Forest cardiovascular model</span>
                </div>

                <div class="form-grid">
                    <!-- 1. Age -->
                    <div class="form-group">
                        <label for="age">1. Age (years) *</label>
                        <input type="number" id="age" name="age" min="1" max="120" value="55" class="form-control" required>
                        <small class="form-hint">Patient clinical age in years</small>
                    </div>

                    <!-- 2. Sex -->
                    <div class="form-group">
                        <label for="sex">2. Biological Sex *</label>
                        <select id="sex" name="sex" class="form-control" required>
                            <option value="1" selected>1 - Male</option>
                            <option value="0">0 - Female</option>
                        </select>
                        <small class="form-hint">Biological gender parameter</small>
                    </div>

                    <!-- 3. Chest Pain Type -->
                    <div class="form-group">
                        <label for="cp">3. Chest Pain Type (cp) *</label>
                        <select id="cp" name="cp" class="form-control" required>
                            <option value="0" selected>0 - Typical Angina</option>
                            <option value="1">1 - Atypical Angina</option>
                            <option value="2">2 - Non-anginal Pain</option>
                            <option value="3">3 - Asymptomatic</option>
                        </select>
                        <small class="form-hint">Symptom classification of angina</small>
                    </div>

                    <!-- 4. Resting Blood Pressure -->
                    <div class="form-group">
                        <label for="trestbps">4. Resting Blood Pressure (trestbps) *</label>
                        <input type="number" id="trestbps" name="trestbps" min="50" max="250" value="130" class="form-control" required>
                        <small class="form-hint">In mm Hg upon admission</small>
                    </div>

                    <!-- 5. Serum Cholesterol -->
                    <div class="form-group">
                        <label for="chol">5. Serum Cholesterol (chol) *</label>
                        <input type="number" id="chol" name="chol" min="80" max="600" value="240" class="form-control" required>
                        <small class="form-hint">In mg/dl fasting test</small>
                    </div>

                    <!-- 6. Fasting Blood Sugar -->
                    <div class="form-group">
                        <label for="fbs">6. Fasting Blood Sugar > 120 mg/dl (fbs) *</label>
                        <select id="fbs" name="fbs" class="form-control" required>
                            <option value="0" selected>0 - False (&le; 120 mg/dl)</option>
                            <option value="1">1 - True (&gt; 120 mg/dl)</option>
                        </select>
                        <small class="form-hint">Elevated fasting blood sugar indicator</small>
                    </div>

                    <!-- 7. Resting ECG -->
                    <div class="form-group">
                        <label for="restecg">7. Resting ECG Results (restecg) *</label>
                        <select id="restecg" name="restecg" class="form-control" required>
                            <option value="0" selected>0 - Normal</option>
                            <option value="1">1 - ST-T Wave Abnormality</option>
                            <option value="2">2 - Left Ventricular Hypertrophy</option>
                        </select>
                        <small class="form-hint">Resting electrocardiographic findings</small>
                    </div>

                    <!-- 8. Maximum Heart Rate -->
                    <div class="form-group">
                        <label for="thalach">8. Max Heart Rate Achieved (thalach) *</label>
                        <input type="number" id="thalach" name="thalach" min="60" max="250" value="150" class="form-control" required>
                        <small class="form-hint">Highest recorded exercise heart rate</small>
                    </div>

                    <!-- 9. Exercise Induced Angina -->
                    <div class="form-group">
                        <label for="exang">9. Exercise Induced Angina (exang) *</label>
                        <select id="exang" name="exang" class="form-control" required>
                            <option value="0" selected>0 - No</option>
                            <option value="1">1 - Yes</option>
                        </select>
                        <small class="form-hint">Angina triggered during stress test</small>
                    </div>

                    <!-- 10. Oldpeak -->
                    <div class="form-group">
                        <label for="oldpeak">10. ST Depression (oldpeak) *</label>
                        <input type="number" id="oldpeak" name="oldpeak" step="0.1" min="0" max="10" value="1.2" class="form-control" required>
                        <small class="form-hint">Exercise relative to rest (e.g. 1.2)</small>
                    </div>

                    <!-- 11. Slope -->
                    <div class="form-group">
                        <label for="slope">11. Slope of Peak Exercise ST (slope) *</label>
                        <select id="slope" name="slope" class="form-control" required>
                            <option value="0">0 - Upsloping</option>
                            <option value="1" selected>1 - Flat</option>
                            <option value="2">2 - Downsloping</option>
                        </select>
                        <small class="form-hint">Morphology of peak ST segment</small>
                    </div>

                    <!-- 12. CA -->
                    <div class="form-group">
                        <label for="ca">12. Major Vessels Colored (ca) *</label>
                        <select id="ca" name="ca" class="form-control" required>
                            <option value="0" selected>0</option>
                            <option value="1">1</option>
                            <option value="2">2</option>
                            <option value="3">3</option>
                        </select>
                        <small class="form-hint">Fluoroscopy major vessel count (0–3)</small>
                    </div>

                    <!-- 13. Thal -->
                    <div class="form-group">
                        <label for="thal">13. Thalassemia (thal) *</label>
                        <select id="thal" name="thal" class="form-control" required>
                            <option value="1">1 - Normal</option>
                            <option value="2" selected>2 - Fixed Defect</option>
                            <option value="3">3 - Reversible Defect</option>
                        </select>
                        <small class="form-hint">Blood perfusion imaging result</small>
                    </div>
                </div>

                <div class="form-actions" style="margin-top: 24px;">
                    <button type="submit" class="btn btn-primary btn-lg">
                        💓 Evaluate Heart Disease Risk
                    </button>
                    <button type="reset" class="btn btn-outline btn-lg">
                        Reset Defaults
                    </button>
                </div>
            </div>
        </form>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
