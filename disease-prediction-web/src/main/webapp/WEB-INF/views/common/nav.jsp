<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<aside class="sidebar">
    <div class="brand">
        <div class="brand-icon">⚕️</div>
        <div class="brand-info">
            <h1 class="brand-title">MediPredict</h1>
            <span class="badge-tech">Servlet 6.0 &bull; JSP 3.1</span>
        </div>
    </div>

    <nav class="nav-menu">
        <a href="${pageContext.request.contextPath}/home" class="nav-item ${activeNav == 'dashboard' ? 'active' : ''}">
            <span class="nav-icon">📊</span>
            <span class="nav-label">Dashboard</span>
        </a>

        <a href="${pageContext.request.contextPath}/patients" class="nav-item ${activeNav == 'patients' ? 'active' : ''}">
            <span class="nav-icon">👤</span>
            <span class="nav-label">Patient Registration</span>
        </a>

        <a href="${pageContext.request.contextPath}/predict/general" class="nav-item ${activeNav == 'general-predict' ? 'active' : ''}">
            <span class="nav-icon">🧪</span>
            <span class="nav-label">Disease Prediction</span>
        </a>

        <a href="${pageContext.request.contextPath}/predict/heart" class="nav-item ${activeNav == 'heart-predict' ? 'active' : ''}">
            <span class="nav-icon">❤️</span>
            <span class="nav-label">Heart Disease Risk</span>
        </a>

        <a href="${pageContext.request.contextPath}/history" class="nav-item ${activeNav == 'history' ? 'active' : ''}">
            <span class="nav-icon">📋</span>
            <span class="nav-label">Prediction History</span>
        </a>
    </nav>

    <div class="sidebar-footer">
        <div class="status-indicator">
            <span class="status-dot"></span>
            <div class="status-text">
                <strong>Spring Boot Backend</strong>
                <small>REST API &bull; Port 8080</small>
            </div>
        </div>
        <div class="status-indicator" style="margin-top: 8px;">
            <span class="status-dot green"></span>
            <div class="status-text">
                <strong>FastAPI ML Service</strong>
                <small>Port 8000 &bull; 2 Models</small>
            </div>
        </div>
    </div>
</aside>
