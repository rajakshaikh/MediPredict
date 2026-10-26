<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="Patient Registration" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Patient Registration &amp; Registry</h2>
            <p class="top-bar-subtitle">Add patient demographic records to MySQL hospital database via Spring Boot REST API</p>
        </div>
    </header>

    <div class="content-body">
        <!-- Error / Success Alerts -->
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <strong>Error:</strong> <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <strong>Success:</strong> <c:out value="${successMessage}" />
            </div>
        </c:if>

        <!-- Newly Registered Patient Quick Actions -->
        <c:if test="${not empty registeredPatient}">
            <div class="card highlighted-card" style="margin-bottom: 24px;">
                <div class="card-header">
                    <h3>Active Patient Profile</h3>
                    <span class="badge badge-success">Saved in MySQL</span>
                </div>
                <div class="patient-summary-grid">
                    <div><strong>ID:</strong> #<c:out value="${registeredPatient.id}" /></div>
                    <div><strong>Name:</strong> <c:out value="${registeredPatient.name}" /></div>
                    <div><strong>Age:</strong> <c:out value="${registeredPatient.age}" /> yrs</div>
                    <div><strong>Gender:</strong> <c:out value="${registeredPatient.gender}" /></div>
                    <div><strong>Email:</strong> <c:out value="${registeredPatient.email != null ? registeredPatient.email : 'N/A'}" /></div>
                </div>
                <div style="margin-top: 16px; display: flex; gap: 12px; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/predict/general?patientId=${registeredPatient.id}" class="btn btn-primary">
                        🧪 Run Disease Prediction &rarr;
                    </a>
                    <a href="${pageContext.request.contextPath}/predict/heart?patientId=${registeredPatient.id}" class="btn btn-secondary">
                        💓 Assess Heart Risk &rarr;
                    </a>
                    <a href="${pageContext.request.contextPath}/history?patientId=${registeredPatient.id}" class="btn btn-outline">
                        📋 View Patient History
                    </a>
                </div>
            </div>
        </c:if>

        <!-- Registration Form Card -->
        <div class="card">
            <div class="card-header">
                <h3>Enter Patient Demographics</h3>
                <span class="field-hint">* Required fields</span>
            </div>

            <form action="${pageContext.request.contextPath}/patients" method="POST" class="styled-form">
                <div class="form-grid">
                    <div class="form-group">
                        <label for="name">Full Name *</label>
                        <input type="text" id="name" name="name" class="form-control"
                               placeholder="e.g. Eleanor Vance" required
                               value="<c:out value='${inputName}' />">
                    </div>

                    <div class="form-group">
                        <label for="age">Age (years) *</label>
                        <input type="number" id="age" name="age" min="0" max="150" class="form-control"
                               placeholder="e.g. 42" required
                               value="<c:out value='${inputAge}' />">
                    </div>

                    <div class="form-group">
                        <label for="gender">Gender *</label>
                        <select id="gender" name="gender" class="form-control" required>
                            <option value="">-- Select Gender --</option>
                            <option value="Male" ${inputGender == 'Male' ? 'selected' : ''}>Male</option>
                            <option value="Female" ${inputGender == 'Female' ? 'selected' : ''}>Female</option>
                            <option value="Other" ${inputGender == 'Other' ? 'selected' : ''}>Other</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="email">Email Address</label>
                        <input type="email" id="email" name="email" class="form-control"
                               placeholder="e.g. eleanor.vance@example.com"
                               value="<c:out value='${inputEmail}' />">
                        <small class="form-hint">Optional, must be unique if specified</small>
                    </div>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">
                        Register Patient
                    </button>
                    <button type="reset" class="btn btn-outline">
                        Clear Fields
                    </button>
                </div>
            </form>
        </div>

        <!-- Registered Patients Table Card -->
        <div class="card" style="margin-top: 24px;">
            <div class="card-header">
                <h3>Hospital Patient Registry</h3>
                <span class="badge badge-info">${patients.size()} Patients Enrolled</span>
            </div>

            <c:choose>
                <c:when test="${empty patients}">
                    <div class="empty-state">
                        <div class="empty-icon">👥</div>
                        <h4>No Registered Patients Found</h4>
                        <p>Fill out the form above to enroll the first patient in the database.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-container">
                        <table class="data-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Full Name</th>
                                    <th>Age</th>
                                    <th>Gender</th>
                                    <th>Email</th>
                                    <th>Registered On</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="p" items="${patients}">
                                    <tr>
                                        <td><strong>#${p.id}</strong></td>
                                        <td><c:out value="${p.name}" /></td>
                                        <td>${p.age}</td>
                                        <td><span class="badge badge-pill">${p.gender}</span></td>
                                        <td><c:out value="${not empty p.email ? p.email : '—'}" /></td>
                                        <td><small class="text-muted">${p.createdAt != null ? p.createdAt : '—'}</small></td>
                                        <td>
                                            <div class="table-btn-group">
                                                <a href="${pageContext.request.contextPath}/predict/general?patientId=${p.id}"
                                                   class="btn btn-xs btn-primary" title="Predict Disease">
                                                    🧪 Predict
                                                </a>
                                                <a href="${pageContext.request.contextPath}/predict/heart?patientId=${p.id}"
                                                   class="btn btn-xs btn-secondary" title="Assess Heart Risk">
                                                    💓 Heart
                                                </a>
                                                <a href="${pageContext.request.contextPath}/history?patientId=${p.id}"
                                                   class="btn btn-xs btn-outline" title="View History">
                                                    📋 History
                                                </a>
                                            </div>
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
