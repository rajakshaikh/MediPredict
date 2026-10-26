<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Clinical Intelligence Dashboard" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Clinical Intelligence Dashboard</h2>
            <p class="top-bar-subtitle">Jakarta Servlet &amp; JSP Presentation Tier communicating with Spring Boot REST API</p>
        </div>
        <div class="server-badge">
            <span class="pulse-indicator"></span>
            <span>Tomcat 10.1 &bull; Active</span>
        </div>
    </header>

    <div class="content-body">
        <c:if test="${!backendConnected}">
            <div class="alert alert-warning">
                <strong>Backend Warning:</strong> ${backendStatusMessage}
            </div>
        </c:if>

        <!-- Metric Cards -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon icon-blue">👥</div>
                <div class="stat-details">
                    <span class="stat-number">${totalPatients}</span>
                    <span class="stat-label">Registered Patients</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-emerald">📈</div>
                <div class="stat-details">
                    <span class="stat-number">${totalPredictions}</span>
                    <span class="stat-label">Predictions Generated</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-purple">🤖</div>
                <div class="stat-details">
                    <span class="stat-number">2 ML Models</span>
                    <span class="stat-label">132 Symptoms &bull; 13 Heart Metrics</span>
                </div>
            </div>

            <div class="stat-card">
                <div class="stat-icon icon-amber">📄</div>
                <div class="stat-details">
                    <span class="stat-number">PDF Reports</span>
                    <span class="stat-label">Official OpenPDF Summaries</span>
                </div>
            </div>
        </div>

        <!-- Quick Navigation Cards -->
        <div class="section-container">
            <h3 class="section-title">Clinical Actions &amp; Workflows</h3>
            <div class="cards-grid">
                <div class="action-card">
                    <div class="action-card-header">
                        <span class="action-icon">📝</span>
                        <h4>Patient Registration</h4>
                    </div>
                    <p class="action-desc">
                        Register a new patient into MySQL hospital database before generating disease predictions or heart risk audits.
                    </p>
                    <a href="${pageContext.request.contextPath}/patients" class="btn btn-outline">
                        Register Patient &rarr;
                    </a>
                </div>

                <div class="action-card">
                    <div class="action-card-header">
                        <span class="action-icon">🔬</span>
                        <h4>General Disease Prediction</h4>
                    </div>
                    <p class="action-desc">
                        Select from 132 known clinical symptoms to evaluate probability across 41 disease classes via Random Forest ML.
                    </p>
                    <a href="${pageContext.request.contextPath}/predict/general" class="btn btn-primary">
                        Predict Disease &rarr;
                    </a>
                </div>

                <div class="action-card">
                    <div class="action-card-header">
                        <span class="action-icon">💓</span>
                        <h4>Heart Disease Risk</h4>
                    </div>
                    <p class="action-desc">
                        Assess cardiovascular risk using 13 validated diagnostic biomarkers and generate risk percentage scores.
                    </p>
                    <a href="${pageContext.request.contextPath}/predict/heart" class="btn btn-primary">
                        Assess Heart Risk &rarr;
                    </a>
                </div>

                <div class="action-card">
                    <div class="action-card-header">
                        <span class="action-icon">📋</span>
                        <h4>Audit Trail &amp; Reports</h4>
                    </div>
                    <p class="action-desc">
                        Inspect immutable prediction records, verify input biomarkers, and download official medical PDF reports.
                    </p>
                    <a href="${pageContext.request.contextPath}/history" class="btn btn-secondary">
                        View History &rarr;
                    </a>
                </div>
            </div>
        </div>

        <!-- Architecture Flow Card -->
        <div class="card" style="margin-top: 24px;">
            <div class="card-header">
                <h3>MediPredict System Architecture</h3>
                <span class="badge badge-info">Decoupled Multi-Tier</span>
            </div>
            <div class="architecture-diagram">
                <div class="arch-box client-box">
                    <strong>Browser Client</strong>
                    <small>JSP &bull; HTML5 Forms</small>
                </div>
                <div class="arch-arrow">&rarr; HTTP &rarr;</div>
                <div class="arch-box web-box">
                    <strong>disease-prediction-web</strong>
                    <small>Tomcat 10.1 &bull; Servlets</small>
                </div>
                <div class="arch-arrow">&rarr; REST API &rarr;</div>
                <div class="arch-box backend-box">
                    <strong>Spring Boot Backend</strong>
                    <small>Port 8080 &bull; JPA/MySQL</small>
                </div>
                <div class="arch-arrow">&rarr; Fast-IPC &rarr;</div>
                <div class="arch-box ml-box">
                    <strong>FastAPI ML Service</strong>
                    <small>Port 8000 &bull; Scikit-Learn</small>
                </div>
            </div>
        </div>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
