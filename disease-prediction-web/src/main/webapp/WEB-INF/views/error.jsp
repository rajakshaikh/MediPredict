<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<jsp:include page="/WEB-INF/views/common/header.jsp">
    <jsp:param name="pageTitle" value="System Error" />
</jsp:include>

<jsp:include page="/WEB-INF/views/common/nav.jsp" />

<main class="main-content">
    <header class="top-bar">
        <div>
            <h2 class="top-bar-title">Application Notice</h2>
            <p class="top-bar-subtitle">An unexpected condition or service interruption was encountered</p>
        </div>
    </header>

    <div class="content-body">
        <div class="card error-card">
            <div class="error-icon">⚠️</div>
            <h2>Service Request Error</h2>
            <p class="error-message">
                <c:choose><c:when test="${not empty errorMessage}"><c:out value="${errorMessage}" /></c:when><c:when test="${not empty exception}"><c:out value="${exception.message}" /></c:when><c:otherwise>The requested clinical resource could not be found or the backend service is currently unreachable.</c:otherwise></c:choose>
            </p>

            <div class="safe-handling-notice">
                <small>
                    🔒 <strong>Patient Safety Protocol:</strong> MediPredict does not generate or display fabricated
                    clinical predictions when backend validation or model inference fails.
                </small>
            </div>

            <div style="margin-top: 24px; display: flex; gap: 12px; justify-content: center;">
                <a href="${pageContext.request.contextPath}/home" class="btn btn-primary">
                    Return to Dashboard
                </a>
                <button type="button" class="btn btn-outline" onclick="window.history.back()">
                    Go Back
                </button>
            </div>
        </div>
    </div>

<jsp:include page="/WEB-INF/views/common/footer.jsp" />
