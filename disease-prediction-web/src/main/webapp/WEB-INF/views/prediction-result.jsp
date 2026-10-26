<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Prediction Results" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Clinical Diagnostic Outcome</h2>
            <p class="top-bar-subtitle">Model inference completed and registered to patient audit record in MySQL</p>
        </div>
    </header>

    <div class="content-body">
        <c:choose>
            <c:when test="${predictionType == 'GENERAL_DISEASE' && not empty result}">
                <div class="card result-card">
                    <div class="result-card-header">
                        <span class="badge badge-success">General Disease Model Evaluation</span>
                        <span class="record-meta-id">Audit Record ID: <strong>#${result.id}</strong></span>
                    </div>

                    <div class="result-headline-section">
                        <span class="result-sub-title">Predicted Medical Condition:</span>
                        <h2 class="result-disease-name"><c:out value="${result.prediction}" /></h2>
                    </div>

                    <!-- Probability Progress Bar -->
                    <div class="probability-container">
                        <div class="probability-label-row">
                            <span class="prob-title">Confidence Probability</span>
                            <span class="prob-value">
                                <fmt:formatNumber value="${result.probability}" maxFractionDigits="1" />%
                            </span>
                        </div>
                        <div class="probability-bar-track">
                            <div class="probability-bar-fill" style="width: ${result.probability}%;"></div>
                        </div>
                    </div>

                    <!-- Evaluated Symptoms -->
                    <div class="result-details-section">
                        <h4>Symptoms Evaluated (${result.selectedSymptoms.size()}):</h4>
                        <div class="chips-list" style="margin-top: 8px;">
                            <c:forEach var="sym" items="${result.selectedSymptoms}">
                                <span class="chip chip-static">
                                    <c:out value="${fn:replace(sym, '_', ' ')}" />
                                </span>
                            </c:forEach>
                        </div>
                    </div>

                    <!-- Meta & Audit Details -->
                    <div class="result-audit-meta">
                        <div><strong>Target Patient:</strong> #${result.patientId} — <c:out value="${result.patientName}" /></div>
                        <div><strong>Timestamp:</strong> ${result.createdAt != null ? result.createdAt : 'Just now'}</div>
                        <div><strong>Database Persistence:</strong> <span class="badge badge-pill badge-success">Saved in MySQL</span></div>
                    </div>

                    <!-- Action Buttons & PDF Report Download -->
                    <div class="result-actions-row">
                        <a href="${downloadReportUrl}" class="btn btn-primary btn-lg" target="_blank">
                            📄 Download PDF Report (via Servlet)
                        </a>
                        <a href="${directPdfUrl}" class="btn btn-outline btn-lg" target="_blank" title="Direct download from Spring Boot backend">
                            Direct Spring Boot PDF Link
                        </a>
                        <a href="${pageContext.request.contextPath}/predict/general?patientId=${result.patientId}" class="btn btn-secondary btn-lg">
                            🧪 Predict Another Condition
                        </a>
                        <a href="${pageContext.request.contextPath}/history?patientId=${result.patientId}" class="btn btn-outline btn-lg">
                            📋 View History
                        </a>
                    </div>
                </div>
            </c:when>

            <c:when test="${predictionType == 'HEART_DISEASE' && not empty heartResult}">
                <div class="card result-card">
                    <div class="result-card-header">
                        <span class="badge badge-success">Cardiovascular Model Evaluation</span>
                        <span class="record-meta-id">Audit Record ID: <strong>#${heartResult.id}</strong></span>
                    </div>

                    <div class="result-headline-section">
                        <span class="result-sub-title">Cardiovascular Risk Assessment:</span>
                        <h2 class="result-disease-name ${heartResult.prediction == 1 ? 'risk-high' : 'risk-low'}">
                            <c:out value="${heartResult.result}" />
                        </h2>
                    </div>

                    <!-- Probability Progress Bar -->
                    <div class="probability-container">
                        <div class="probability-label-row">
                            <span class="prob-title">Cardiovascular Risk Probability</span>
                            <span class="prob-value">
                                <fmt:formatNumber value="${heartResult.probability}" maxFractionDigits="1" />%
                            </span>
                        </div>
                        <div class="probability-bar-track">
                            <div class="probability-bar-fill ${heartResult.prediction == 1 ? 'fill-danger' : 'fill-success'}"
                                 style="width: ${heartResult.probability}%;"></div>
                        </div>
                    </div>

                    <!-- Meta & Audit Details -->
                    <div class="result-audit-meta">
                        <div><strong>Target Patient:</strong> #${heartResult.patientId} — <c:out value="${heartResult.patientName}" /></div>
                        <div><strong>Binary Classification:</strong> ${heartResult.prediction == 1 ? '1 (Risk Detected)' : '0 (No Risk Detected)'}</div>
                        <div><strong>Timestamp:</strong> ${heartResult.createdAt != null ? heartResult.createdAt : 'Just now'}</div>
                        <div><strong>Database Persistence:</strong> <span class="badge badge-pill badge-success">Saved in MySQL</span></div>
                    </div>

                    <!-- Action Buttons & PDF Report Download -->
                    <div class="result-actions-row">
                        <a href="${downloadReportUrl}" class="btn btn-primary btn-lg" target="_blank">
                             Download PDF Report (via Servlet)
                        </a>
                        <a href="${directPdfUrl}" class="btn btn-outline btn-lg" target="_blank">
                            Direct Spring Boot PDF Link
                        </a>
                        <a href="${pageContext.request.contextPath}/predict/heart?patientId=${heartResult.patientId}" class="btn btn-secondary btn-lg">
                             Run Another Heart Evaluation
                        </a>
                        <a href="${pageContext.request.contextPath}/history?patientId=${heartResult.patientId}" class="btn btn-outline btn-lg">
                             View History
                        </a>
                    </div>
                </div>
            </c:when>

            <c:otherwise>
                <div class="empty-state">
                    <div class="empty-icon"></div>
                    <h4>No Prediction Outcome to Display</h4>
                    <p>No prediction result was found in the current request. Please choose an evaluation workflow below.</p>
                    <div style="margin-top: 16px; display: flex; gap: 12px; justify-content: center;">
                        <a href="${pageContext.request.contextPath}/predict/general" class="btn btn-primary">
                            General Disease Prediction
                        </a>
                        <a href="${pageContext.request.contextPath}/predict/heart" class="btn btn-secondary">
                            Heart Disease Risk
                        </a>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
