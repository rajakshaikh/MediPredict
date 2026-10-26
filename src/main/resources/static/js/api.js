/**
 * Centralized API service for communicating exclusively with the Spring Boot backend.
 * Base URL: http://localhost:8080
 */
const BASE_URL = 'http://localhost:8080';

const api = {
  /**
   * Generic Fetch wrapper with JSON serialization & error extraction.
   */
  async request(endpoint, options = {}) {
    const url = `${BASE_URL}${endpoint}`;
    const defaultHeaders = {
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    };

    const config = {
      ...options,
      headers: {
        ...defaultHeaders,
        ...options.headers
      }
    };

    try {
      const response = await fetch(url, config);
      const data = await response.json().catch(() => null);

      if (!response.ok) {
        const errorMsg = (data && (data.message || data.error)) || `HTTP error ${response.status}: ${response.statusText}`;
        throw new Error(errorMsg);
      }

      return data;
    } catch (err) {
      console.error(`API Error [${endpoint}]:`, err);
      throw err;
    }
  },

  /* ==========================================================================
     Patient APIs
     ========================================================================== */
  async registerPatient(patientData) {
    return this.request('/api/patients', {
      method: 'POST',
      body: JSON.stringify(patientData)
    });
  },

  async getPatient(id) {
    return this.request(`/api/patients/${id}`, {
      method: 'GET'
    });
  },

  async getAllPatients() {
    return this.request('/api/patients', {
      method: 'GET'
    });
  },

  /* ==========================================================================
     ML Symptoms & Prediction APIs (Routed via Spring Boot)
     ========================================================================== */
  async getSymptoms() {
    return this.request('/api/ml/symptoms', {
      method: 'GET'
    });
  },

  async predictGeneralDisease(patientId, symptoms) {
    return this.request(`/api/patients/${patientId}/predict`, {
      method: 'POST',
      body: JSON.stringify({ symptoms })
    });
  },

  async predictHeartDisease(patientId, heartData) {
    return this.request(`/api/patients/${patientId}/predict-heart-disease`, {
      method: 'POST',
      body: JSON.stringify(heartData)
    });
  },

  /* ==========================================================================
     Prediction History APIs
     ========================================================================== */
  async getPatientPredictions(patientId) {
    return this.request(`/api/patients/${patientId}/predictions`, {
      method: 'GET'
    });
  },

  async getAllPredictions() {
    return this.request('/api/predictions', {
      method: 'GET'
    });
  },

  /* ==========================================================================
     PDF Medical Report URL
     ========================================================================== */
  getReportUrl(patientId, predictionId) {
    return `${BASE_URL}/api/patients/${patientId}/predictions/${predictionId}/report`;
  }
};
