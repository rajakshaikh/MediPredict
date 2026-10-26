package com.diseaseprediction.web.servlet;

import com.diseaseprediction.web.client.ApiException;
import com.diseaseprediction.web.client.BackendApiClient;
import com.diseaseprediction.web.dto.PatientDto;
import com.diseaseprediction.web.dto.PredictionHistoryDto;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ServletTests {

    static class StubApiClient extends BackendApiClient {
        List<PatientDto> patients = new ArrayList<>();
        List<PredictionHistoryDto> predictions = new ArrayList<>();

        public StubApiClient() {
            super("http://localhost:8080");
        }

        @Override
        public List<PatientDto> getAllPatients() {
            return patients;
        }

        @Override
        public List<PredictionHistoryDto> getAllPredictions() {
            return predictions;
        }

        @Override
        public PatientDto registerPatient(com.diseaseprediction.web.dto.PatientRegistrationDto regDto) {
            PatientDto dto = new PatientDto();
            dto.setId(99L);
            dto.setName(regDto.getName());
            dto.setAge(regDto.getAge());
            dto.setGender(regDto.getGender());
            dto.setEmail(regDto.getEmail());
            return dto;
        }
    }

    static class TestRequestDispatcher implements RequestDispatcher {
        String forwardedPath;

        @Override
        public void forward(ServletRequest request, ServletResponse response) {
            // Forward recorded
        }

        @Override
        public void include(ServletRequest request, ServletResponse response) {
        }
    }

    static class MockHttpServletRequest implements HttpServletRequest {
        Map<String, Object> attributes = new HashMap<>();
        Map<String, String[]> parameters = new HashMap<>();
        String servletPath = "";
        String contextPath = "/disease-prediction-web";
        TestRequestDispatcher dispatcher = new TestRequestDispatcher();

        @Override
        public Object getAttribute(String name) { return attributes.get(name); }
        @Override
        public Enumeration<String> getAttributeNames() { return Collections.enumeration(attributes.keySet()); }
        @Override
        public String getCharacterEncoding() { return "UTF-8"; }
        @Override
        public void setCharacterEncoding(String env) { }
        @Override
        public int getContentLength() { return 0; }
        @Override
        public long getContentLengthLong() { return 0; }
        @Override
        public String getContentType() { return "text/html"; }
        @Override
        public ServletInputStream getInputStream() { return null; }
        @Override
        public String getParameter(String name) {
            String[] vals = parameters.get(name);
            return (vals != null && vals.length > 0) ? vals[0] : null;
        }
        @Override
        public Enumeration<String> getParameterNames() { return Collections.enumeration(parameters.keySet()); }
        @Override
        public String[] getParameterValues(String name) { return parameters.get(name); }
        @Override
        public Map<String, String[]> getParameterMap() { return parameters; }
        @Override
        public String getProtocol() { return "HTTP/1.1"; }
        @Override
        public String getScheme() { return "http"; }
        @Override
        public String getServerName() { return "localhost"; }
        @Override
        public int getServerPort() { return 8080; }
        @Override
        public BufferedReader getReader() { return null; }
        @Override
        public String getRemoteAddr() { return "127.0.0.1"; }
        @Override
        public String getRemoteHost() { return "localhost"; }
        @Override
        public void setAttribute(String name, Object o) { attributes.put(name, o); }
        @Override
        public void removeAttribute(String name) { attributes.remove(name); }
        @Override
        public Locale getLocale() { return Locale.ENGLISH; }
        @Override
        public Enumeration<Locale> getLocales() { return Collections.enumeration(Collections.singleton(Locale.ENGLISH)); }
        @Override
        public boolean isSecure() { return false; }
        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            dispatcher.forwardedPath = path;
            return dispatcher;
        }
        @Override
        public int getRemotePort() { return 0; }
        @Override
        public String getLocalName() { return null; }
        @Override
        public String getLocalAddr() { return null; }
        @Override
        public int getLocalPort() { return 0; }
        @Override
        public ServletContext getServletContext() { return null; }
        @Override
        public AsyncContext startAsync() { return null; }
        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) { return null; }
        @Override
        public boolean isAsyncStarted() { return false; }
        @Override
        public boolean isAsyncSupported() { return false; }
        @Override
        public AsyncContext getAsyncContext() { return null; }
        @Override
        public DispatcherType getDispatcherType() { return DispatcherType.REQUEST; }
        @Override
        public String getRequestId() { return "1"; }
        @Override
        public String getProtocolRequestId() { return "1"; }
        @Override
        public ServletConnection getServletConnection() { return null; }
        @Override
        public String getAuthType() { return null; }
        @Override
        public Cookie[] getCookies() { return new Cookie[0]; }
        @Override
        public long getDateHeader(String name) { return 0; }
        @Override
        public String getHeader(String name) { return null; }
        @Override
        public Enumeration<String> getHeaders(String name) { return Collections.emptyEnumeration(); }
        @Override
        public Enumeration<String> getHeaderNames() { return Collections.emptyEnumeration(); }
        @Override
        public int getIntHeader(String name) { return 0; }
        @Override
        public String getMethod() { return "GET"; }
        @Override
        public String getPathInfo() { return null; }
        @Override
        public String getPathTranslated() { return null; }
        @Override
        public String getContextPath() { return contextPath; }
        @Override
        public String getQueryString() { return null; }
        @Override
        public String getRemoteUser() { return null; }
        @Override
        public boolean isUserInRole(String role) { return false; }
        @Override
        public Principal getUserPrincipal() { return null; }
        @Override
        public String getRequestedSessionId() { return null; }
        @Override
        public String getRequestURI() { return contextPath + servletPath; }
        @Override
        public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080" + getRequestURI()); }
        @Override
        public String getServletPath() { return servletPath; }
        @Override
        public HttpSession getSession(boolean create) { return null; }
        @Override
        public HttpSession getSession() { return null; }
        @Override
        public String changeSessionId() { return null; }
        @Override
        public boolean isRequestedSessionIdValid() { return false; }
        @Override
        public boolean isRequestedSessionIdFromCookie() { return false; }
        @Override
        public boolean isRequestedSessionIdFromURL() { return false; }
        @Override
        public boolean authenticate(HttpServletResponse response) { return false; }
        @Override
        public void login(String username, String password) { }
        @Override
        public void logout() { }
        @Override
        public Collection<Part> getParts() { return Collections.emptyList(); }
        @Override
        public Part getPart(String name) { return null; }
        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) { return null; }
    }

    static class MockHttpServletResponse implements HttpServletResponse {
        int status = 200;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        @Override public void addCookie(Cookie cookie) {}
        @Override public boolean containsHeader(String name) { return false; }
        @Override public String encodeURL(String url) { return url; }
        @Override public String encodeRedirectURL(String url) { return url; }
        @Override public void sendError(int sc, String msg) { status = sc; }
        @Override public void sendError(int sc) { status = sc; }
        @Override public void sendRedirect(String location) {}
        @Override public void setDateHeader(String name, long date) {}
        @Override public void addDateHeader(String name, long date) {}
        @Override public void setHeader(String name, String value) {}
        @Override public void addHeader(String name, String value) {}
        @Override public void setIntHeader(String name, int value) {}
        @Override public void addIntHeader(String name, int value) {}
        @Override public void setStatus(int sc) { status = sc; }
        @Override public int getStatus() { return status; }
        @Override public String getHeader(String name) { return null; }
        @Override public Collection<String> getHeaders(String name) { return Collections.emptyList(); }
        @Override public Collection<String> getHeaderNames() { return Collections.emptyList(); }
        @Override public String getCharacterEncoding() { return "UTF-8"; }
        @Override public String getContentType() { return "text/html"; }
        @Override public ServletOutputStream getOutputStream() {
            return new ServletOutputStream() {
                @Override public boolean isReady() { return true; }
                @Override public void setWriteListener(WriteListener writeListener) {}
                @Override public void write(int b) { outputStream.write(b); }
            };
        }
        @Override public PrintWriter getWriter() { return new PrintWriter(outputStream); }
        @Override public void setCharacterEncoding(String charset) {}
        @Override public void setContentLength(int len) {}
        @Override public void setContentLengthLong(long len) {}
        @Override public void setContentType(String type) {}
        @Override public void setBufferSize(int size) {}
        @Override public int getBufferSize() { return 8192; }
        @Override public void flushBuffer() {}
        @Override public void resetBuffer() {}
        @Override public boolean isCommitted() { return false; }
        @Override public void reset() {}
        @Override public void setLocale(Locale loc) {}
        @Override public Locale getLocale() { return Locale.ENGLISH; }
    }

    @Test
    public void testHomeServletDoGet() throws Exception {
        StubApiClient client = new StubApiClient();
        client.patients.add(new PatientDto());
        client.predictions.add(new PredictionHistoryDto());

        HomeServlet servlet = new HomeServlet(client);
        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        servlet.doGet(req, res);

        assertEquals(1, req.getAttribute("totalPatients"));
        assertEquals(1, req.getAttribute("totalPredictions"));
        assertEquals("/WEB-INF/views/home.jsp", req.dispatcher.forwardedPath);
    }

    @Test
    public void testPatientServletValidationFailure() throws Exception {
        StubApiClient client = new StubApiClient();
        PatientServlet servlet = new PatientServlet(client);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        // Empty name and age
        req.parameters.put("name", new String[]{""});
        req.parameters.put("age", new String[]{""});
        req.parameters.put("gender", new String[]{"Male"});

        servlet.doPost(req, res);

        assertNotNull(req.getAttribute("errorMessage"));
        assertTrue(req.getAttribute("errorMessage").toString().contains("name is required"));
        assertEquals("/WEB-INF/views/patient-register.jsp", req.dispatcher.forwardedPath);
    }

    @Test
    public void testPatientServletSuccessfulPost() throws Exception {
        StubApiClient client = new StubApiClient();
        PatientServlet servlet = new PatientServlet(client);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.parameters.put("name", new String[]{"Eleanor"});
        req.parameters.put("age", new String[]{"34"});
        req.parameters.put("gender", new String[]{"Female"});
        req.parameters.put("email", new String[]{"eleanor@example.com"});

        servlet.doPost(req, res);

        assertNotNull(req.getAttribute("registeredPatient"));
        assertNotNull(req.getAttribute("successMessage"));
        PatientDto created = (PatientDto) req.getAttribute("registeredPatient");
        assertEquals(99L, created.getId());
        assertEquals("Eleanor", created.getName());
    }

    @Test
    public void testPredictionServletMissingSymptoms() throws Exception {
        StubApiClient client = new StubApiClient();
        PredictionServlet servlet = new PredictionServlet(client);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();

        req.servletPath = "/predict/general";
        req.parameters.put("predictionType", new String[]{"general"});
        req.parameters.put("patientId", new String[]{"1"});
        // No symptoms parameter provided

        servlet.doPost(req, res);

        assertNotNull(req.getAttribute("errorMessage"));
        assertTrue(req.getAttribute("errorMessage").toString().contains("at least one"));
    }

    @Test
    public void testHistoryServletDoGet() throws Exception {
        StubApiClient client = new StubApiClient();
        HistoryServlet servlet = new HistoryServlet(client);

        MockHttpServletRequest req = new MockHttpServletRequest();
        MockHttpServletResponse res = new MockHttpServletResponse();
        req.servletPath = "/history";

        servlet.doGet(req, res);

        assertEquals("history", req.getAttribute("activeNav"));
        assertEquals("/WEB-INF/views/history.jsp", req.dispatcher.forwardedPath);
    }
}
