<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="General Disease Prediction" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">General Disease Prediction</h2>
            <p class="top-bar-subtitle">Random Forest multi-class classification across 132 symptoms and 41 disease profiles</p>
        </div>
    </header>

    <div class="content-body">
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <strong>Error:</strong> <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/predict/general" method="POST" id="general-predict-form">
            <input type="hidden" name="predictionType" value="general">

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

            <!-- Symptoms Selection Card -->
            <div class="card" style="margin-top: 24px;">
                <div class="card-header">
                    <h3>2. Select Observed Symptoms</h3>
                    <div class="selection-metrics">
                        <span class="counter-badge">
                            Selected: <strong id="selected-count">0</strong> / 132
                        </span>
                        <button type="submit" class="btn btn-primary btn-sm">
                            Predict Disease &rarr;
                        </button>
                        <button type="button" class="btn btn-outline btn-sm" id="clear-all-symptoms-btn">
                            Clear All
                        </button>
                    </div>
                </div>

                <!-- Selected Symptoms Chips Bar -->
                <div class="selected-chips-bar" id="selected-chips-bar">
                    <span class="chips-empty-hint" id="chips-empty-hint">
                        No symptoms selected. Click checkboxes below or search to add observed symptoms.
                    </span>
                    <div class="chips-list" id="chips-container"></div>
                </div>

                <!-- Search and Filter Bar -->
                <div class="symptom-search-row">
                    <div class="search-input-wrap">
                        <input type="text" id="symptom-search-box" class="form-control"
                               placeholder="🔍 Search 132 medical symptoms (e.g. fever, rash, headache, cough)..."
                               autocomplete="off">
                        <button type="button" id="clear-search-btn" class="search-clear-btn" style="display: none;">&times;</button>
                    </div>
                </div>

                <!-- Category Filter Pills -->
                <div class="category-pills-bar">
                    <button type="button" class="cat-pill active" data-category="all">All (132)</button>
                    <c:forEach var="catEntry" items="${categorizedSymptoms}">
                        <button type="button" class="cat-pill" data-category="${catEntry.key}">
                            ${catEntry.key} (${catEntry.value.size()})
                        </button>
                    </c:forEach>
                </div>

                <!-- Categorized Symptom Grid -->
                <div class="symptoms-catalog-container" id="symptoms-catalog">
                    <c:forEach var="catEntry" items="${categorizedSymptoms}">
                        <div class="symptom-group-block" data-category="${catEntry.key}">
                            <div class="symptom-group-title">
                                <span>${catEntry.key}</span>
                                <span class="badge badge-pill">${catEntry.value.size()} symptoms</span>
                            </div>
                            <div class="symptom-checkbox-grid">
                                <c:forEach var="symptom" items="${catEntry.value}">
                                    <c:set var="formattedName" value="${fn:replace(symptom, '_', ' ')}" />
                                    <label class="symptom-item" data-symptom-key="${symptom}" data-search="${symptom} ${formattedName}">
                                        <input type="checkbox" name="symptoms" value="${symptom}"
                                               class="symptom-checkbox"
                                               id="sym_${symptom}">
                                        <span class="symptom-label">${formattedName}</span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <!-- Form Bottom Submit Button -->
                <div class="form-actions" style="margin-top: 24px; padding-top: 16px; border-top: 1px solid var(--border-color);">
                    <button type="submit" class="btn btn-primary btn-lg">
                        🧪 Evaluate Symptoms &amp; Predict Disease
                    </button>
                    <button type="button" class="btn btn-outline btn-lg" id="clear-all-symptoms-btn-bottom">
                        Reset Selection
                    </button>
                </div>
            </div>
        </form>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
