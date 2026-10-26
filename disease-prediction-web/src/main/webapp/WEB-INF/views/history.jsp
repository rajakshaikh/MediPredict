<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Prediction History & Audit Trail" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Prediction History &amp; Audit Trail</h2>
            <p class="top-bar-subtitle">Immutable clinical evaluation records and OpenPDF medical report downloads</p>
        </div>
    </header>

    <div class="content-body">
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <strong>Error:</strong> <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <!-- Filter Controls Card -->
        <div class="card">
            <div class="card-header">
                <h3>Filter Audit Records</h3>
                <span class="badge badge-info">${filterDescription}</span>
            </div>

            <form action="${pageContext.request.contextPath}/history" method="GET" class="filter-form-row">
                <div class="form-group" style="flex: 1; max-width: 320px;">
                    <label for="filterPatientSelect">Select Patient Profile</label>
                    <select id="filterPatientSelect" class="form-control" onchange="syncHistoryPatientId(this.value)">
                        <option value="">-- All Patients --</option>
                        <c:forEach var="p" items="${patients}">
                            <option value="${p.id}" ${selectedPatientId == p.id.toString() ? 'selected' : ''}>
                                #${p.id} — ${p.name}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group" style="width: 140px;">
                    <label for="historyPatientId">Patient ID</label>
                    <input type="number" id="historyPatientId" name="patientId" min="1" class="form-control"
                           placeholder="e.g. 1" value="<c:out value='${selectedPatientId}' />">
                </div>

                <div class="filter-btn-group">
                    <button type="submit" class="btn btn-primary">Filter History</button>
                    <a href="${pageContext.request.contextPath}/history" class="btn btn-outline">Clear Filter</a>
                </div>
            </form>
        </div>

        <!-- History Records Table Card -->
        <div class="card" style="margin-top: 24px;">
            <div class="card-header">
                <h3>Historical Evaluation Log (Newest First)</h3>
                <span class="badge badge-pill">${historyList.size()} Records Found</span>
            </div>

            <c:choose>
                <c:when test="${empty historyList}">
                    <div class="empty-state">
                        <div class="empty-icon">📂</div>
                        <h4>No Prediction Records Found</h4>
                        <p>No historical evaluation records matched your query criteria.</p>
                        <div style="margin-top: 16px;">
                            <a href="${pageContext.request.contextPath}/predict/general" class="btn btn-primary">
                                Run General Disease Prediction &rarr;
                            </a>
                        </div>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Patient</th>
                                    <th>Evaluation Type</th>
                                    <th>Predicted Diagnosis</th>
                                    <th>Confidence</th>
                                    <th>Input Biomarkers / Symptoms</th>
                                    <th>Timestamp</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${historyList}">
                                    <tr>
                                        <td><strong>#${item.id}</strong></td>
                                        <td>
                                            <strong>#${item.patientId}</strong>
                                            <br><small><c:out value="${item.patientName}" /></small>
                                        </td>
                                        <td>
                                            <span class="badge ${item.predictionType == 'HEART_DISEASE' ? 'badge-danger' : 'badge-primary'}">
                                                ${item.predictionType == 'HEART_DISEASE' ? 'Cardiovascular' : 'General Disease'}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="disease-cell-highlight">
                                                <c:out value="${item.predictedDisease}" />
                                            </span>
                                        </td>
                                        <td>
                                            <span class="probability-badge">
                                                <fmt:formatNumber value="${item.probability}" maxFractionDigits="1" />%
                                            </span>
                                        </td>
                                        <td style="max-width: 260px;">
                                            <div class="input-data-preview" title="<c:out value='${item.inputData}' />">
                                                <c:out value="${item.inputData}" />
                                            </div>
                                        </td>
                                        <td>
                                            <small class="text-muted">${item.createdAt != null ? item.createdAt : '—'}</small>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/history/report?patientId=${item.patientId}&predictionId=${item.id}"
                                               class="btn btn-xs btn-primary" target="_blank" title="Download OpenPDF Report via Servlet">
                                                📄 Download PDF
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
