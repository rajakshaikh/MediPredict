<%@ page contentType="text/html;charset=UTF-8" language="java" %>
        <!-- Global Medical Disclaimer on every view -->
        <jsp:include page="/WEB-INF/views/common/disclaimer.jsp" />

        <footer class="site-footer">
            <p>&copy; <%= java.time.Year.now().getValue() %> MediPredict — Jakarta EE 10 (Tomcat 10.1) &bull; Spring Boot REST API &bull; FastAPI ML</p>
        </footer>
    </main>
</div>

<script src="${pageContext.request.contextPath}/js/web-app.js"></script>
</body>
</html>
