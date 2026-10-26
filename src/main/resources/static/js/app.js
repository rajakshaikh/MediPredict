/**
 * Application Controller for Disease Prediction & Patient Management System
 */

// Application State
const state = {
  activePatientId: null,
  activePatientName: null,
  allSymptoms: [],
  selectedSymptoms: new Set()
};

// DOM Ready initialization
document.addEventListener('DOMContentLoaded', () => {
  initNavigation();
  initDashboard();
  initPatientRegistration();
  initGeneralPrediction();
  initHeartPrediction();
  initHistory();
});

/* ==========================================================================
   Navigation Handling
   ========================================================================== */
function initNavigation() {
  const navItems = document.querySelectorAll('.nav-item');
  const viewSections = document.querySelectorAll('.view-section');

  navItems.forEach(item => {
    item.addEventListener('click', () => {
      const targetView = item.getAttribute('data-view');
      navigateToView(targetView);
    });
  });

  // Global Quick Action Buttons on Dashboard
  document.querySelectorAll('[data-navigate]').forEach(btn => {
    btn.addEventListener('click', () => {
      const target = btn.getAttribute('data-navigate');
      navigateToView(target);
    });
  });
}

function navigateToView(viewId) {
  // Update nav item active states
  document.querySelectorAll('.nav-item').forEach(el => {
    el.classList.toggle('active', el.getAttribute('data-view') === viewId);
  });

  // Switch visible section
  document.querySelectorAll('.view-section').forEach(el => {
    el.classList.toggle('active', el.id === viewId);
  });

  // View specific refreshes
  if (viewId === 'dashboard-view') {
    refreshDashboardStats();
  } else if (viewId === 'general-predict-view') {
    const genInput = document.getElementById('targetPatientId') || document.getElementById('general-patient-id');
    if (genInput && !genInput.value && state.activePatientId) {
      genInput.value = state.activePatientId;
    }
    loadSymptomsOnce();
  } else if (viewId === 'heart-predict-view') {
    const heartInput = document.getElementById('heart-patient-id');
    if (heartInput && !heartInput.value && state.activePatientId) {
      heartInput.value = state.activePatientId;
    }
  } else if (viewId === 'history-view') {
    const histInput = document.getElementById('history-patient-id');
    if (histInput && !histInput.value && state.activePatientId) {
      histInput.value = state.activePatientId;
      loadPatientHistory(state.activePatientId);
    }
  }
}

function setActivePatient(id, name) {
  state.activePatientId = id;
  state.activePatientName = name || `Patient #${id}`;

  const badge = document.getElementById('active-patient-badge');
  const valueSpan = document.getElementById('active-patient-name');
  if (badge && valueSpan) {
    badge.style.display = 'flex';
    valueSpan.textContent = `#${id} - ${state.activePatientName}`;
  }

  // Prepopulate patient ID inputs across prediction & history views if empty
  const genInput = document.getElementById('targetPatientId') || document.getElementById('general-patient-id');
  const heartInput = document.getElementById('heart-patient-id');
  const histInput = document.getElementById('history-patient-id');
  if (genInput && !genInput.value) genInput.value = id;
  if (heartInput && !heartInput.value) heartInput.value = id;
  if (histInput && !histInput.value) histInput.value = id;
}

/* ==========================================================================
   Dashboard Logic
   ========================================================================== */
function initDashboard() {
  refreshDashboardStats();
}

async function refreshDashboardStats() {
  try {
    const [patients, predictions] = await Promise.allSettled([
      api.getAllPatients(),
      api.getAllPredictions()
    ]);

    const patientCountEl = document.getElementById('stat-total-patients');
    const predictionCountEl = document.getElementById('stat-total-predictions');

    if (patientCountEl) {
      patientCountEl.textContent = patients.status === 'fulfilled' && Array.isArray(patients.value)
        ? patients.value.length
        : '0';
    }

    if (predictionCountEl) {
      predictionCountEl.textContent = predictions.status === 'fulfilled' && Array.isArray(predictions.value)
        ? predictions.value.length
        : '0';
    }
  } catch (err) {
    console.warn('Failed to load dashboard metrics:', err);
  }
}

/* ==========================================================================
   Patient Registration
   ========================================================================== */
function initPatientRegistration() {
  const form = document.getElementById('patient-form');
  const alertContainer = document.getElementById('patient-reg-alert');

  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    alertContainer.innerHTML = '';

    const name = document.getElementById('patient-name').value.trim();
    const age = parseInt(document.getElementById('patient-age').value, 10);
    const gender = document.getElementById('patient-gender').value;
    const email = document.getElementById('patient-email').value.trim();

    const submitBtn = form.querySelector('button[type="submit"]');
    setButtonLoading(submitBtn, true, 'Registering...');

    try {
      const patient = await api.registerPatient({
        name,
        age,
        gender,
        email: email || null
      });

      showAlert(alertContainer, 'success',
        `<strong>Patient Registered Successfully!</strong> Generated Patient ID: <strong>#${patient.id}</strong> (${patient.name}). This ID is now set active for predictions.`
      );

      setActivePatient(patient.id, patient.name);
      form.reset();
      refreshDashboardStats();
    } catch (err) {
      showAlert(alertContainer, 'danger', `Registration Failed: ${err.message}`);
    } finally {
      setButtonLoading(submitBtn, false, 'Register Patient');
    }
  });
}

/* ==========================================================================
   Symptom Categories Mapping (All 132 symptoms uniquely mapped)
   ========================================================================== */
const SYMPTOM_CATEGORIES = {
  'General': [
    'fatigue', 'weight_gain', 'weight_loss', 'lethargy', 'sweating', 'dehydration',
    'loss_of_appetite', 'malaise', 'obesity', 'excessive_hunger', 'increased_appetite'
  ],
  'Fever / Infection': [
    'chills', 'shivering', 'high_fever', 'mild_fever', 'swelled_lymph_nodes',
    'toxic_look_(typhos)'
  ],
  'Respiratory': [
    'continuous_sneezing', 'cough', 'breathlessness', 'phlegm', 'throat_irritation',
    'sinus_pressure', 'runny_nose', 'congestion', 'patches_in_throat', 'mucoid_sputum',
    'rusty_sputum', 'blood_in_sputum', 'loss_of_smell'
  ],
  'Digestive': [
    'stomach_pain', 'acidity', 'ulcers_on_tongue', 'vomiting', 'indigestion',
    'nausea', 'constipation', 'abdominal_pain', 'diarrhoea', 'swelling_of_stomach',
    'pain_during_bowel_movements', 'pain_in_anal_region', 'bloody_stool', 'irritation_in_anus',
    'passage_of_gases', 'belly_pain', 'stomach_bleeding', 'distention_of_abdomen'
  ],
  'Skin': [
    'itching', 'skin_rash', 'nodal_skin_eruptions', 'yellowish_skin', 'bruising',
    'internal_itching', 'red_spots_over_body', 'dischromic _patches', 'pus_filled_pimples',
    'blackheads', 'scurring', 'skin_peeling', 'silver_like_dusting', 'small_dents_in_nails',
    'inflammatory_nails', 'blister', 'red_sore_around_nose', 'yellow_crust_ooze', 'brittle_nails'
  ],
  'Pain': [
    'joint_pain', 'headache', 'back_pain', 'neck_pain', 'knee_pain', 'hip_joint_pain',
    'chest_pain', 'muscle_pain', 'painful_walking'
  ],
  'Neurological': [
    'anxiety', 'mood_swings', 'restlessness', 'dizziness', 'cramps',
    'slurred_speech', 'spinning_movements', 'loss_of_balance', 'unsteadiness',
    'weakness_of_one_body_side', 'depression', 'irritability', 'altered_sensorium',
    'lack_of_concentration', 'coma'
  ],
  'Eye / Vision': [
    'sunken_eyes', 'pain_behind_the_eyes', 'yellowing_of_eyes',
    'blurred_and_distorted_vision', 'redness_of_eyes', 'puffy_face_and_eyes',
    'watering_from_eyes', 'visual_disturbances'
  ],
  'Urinary': [
    'burning_micturition', 'spotting_ urination', 'dark_urine', 'yellow_urine',
    'bladder_discomfort', 'foul_smell_of urine', 'continuous_feel_of_urine', 'polyuria'
  ],
  'Heart / Circulation': [
    'cold_hands_and_feets', 'fast_heart_rate', 'swollen_legs', 'swollen_blood_vessels',
    'swollen_extremeties', 'prominent_veins_on_calf', 'palpitations'
  ],
  'Musculoskeletal': [
    'muscle_wasting', 'weakness_in_limbs', 'muscle_weakness', 'stiff_neck',
    'swelling_joints', 'movement_stiffness'
  ],
  'Other': [
    'irregular_sugar_level', 'acute_liver_failure', 'fluid_overload', 'enlarged_thyroid',
    'extra_marital_contacts', 'drying_and_tingling_lips', 'abnormal_menstruation',
    'family_history', 'receiving_blood_transfusion', 'receiving_unsterile_injections',
    'history_of_alcohol_consumption', 'fluid_overload.1'
  ]
};

// Common recognizable symptoms for the Quick Filter
const COMMON_SYMPTOMS = new Set([
  'high_fever', 'mild_fever', 'chills', 'shivering', 'headache', 'fatigue',
  'cough', 'continuous_sneezing', 'breathlessness', 'nausea', 'vomiting',
  'sweating', 'diarrhoea', 'joint_pain', 'muscle_pain', 'back_pain',
  'stomach_pain', 'indigestion', 'skin_rash', 'itching', 'loss_of_appetite',
  'dizziness', 'chest_pain', 'abdominal_pain'
]);

// Track category expansion state across the session (default first 3 open)
const categoryExpandedState = new Set(['General', 'Fever / Infection', 'Respiratory']);

let currentActiveFilter = 'all';
let currentSearchTerm = '';
let symptomsLoaded = false;

function initGeneralPrediction() {
  const searchInput = document.getElementById('symptom-search');
  const clearSearchBtn = document.getElementById('clear-search-btn');
  const resetSearchBtn = document.getElementById('reset-search-btn');
  const clearBtn = document.getElementById('clear-symptoms-btn');
  const clearBtnBottom = document.getElementById('clear-symptoms-btn-bottom');
  const predictBtn = document.getElementById('predict-general-btn');
  const filterPills = document.querySelectorAll('.filter-pill');

  // Search input event
  if (searchInput) {
    searchInput.addEventListener('input', (e) => {
      currentSearchTerm = e.target.value.toLowerCase().trim();
      if (clearSearchBtn) {
        clearSearchBtn.style.display = currentSearchTerm ? 'block' : 'none';
      }
      renderCategorizedSymptoms();
    });
  }

  // Clear search button inside input
  if (clearSearchBtn) {
    clearSearchBtn.addEventListener('click', () => {
      searchInput.value = '';
      currentSearchTerm = '';
      clearSearchBtn.style.display = 'none';
      searchInput.focus();
      renderCategorizedSymptoms();
    });
  }

  // Reset search button inside empty state
  if (resetSearchBtn) {
    resetSearchBtn.addEventListener('click', () => {
      if (searchInput) searchInput.value = '';
      currentSearchTerm = '';
      if (clearSearchBtn) clearSearchBtn.style.display = 'none';
      renderCategorizedSymptoms();
    });
  }

  // Quick filter pills
  filterPills.forEach(pill => {
    pill.addEventListener('click', () => {
      filterPills.forEach(p => p.classList.remove('active'));
      pill.classList.add('active');
      currentActiveFilter = pill.getAttribute('data-filter');
      renderCategorizedSymptoms();
    });
  });

  // Clear all symptoms buttons
  const handleClearAll = () => {
    state.selectedSymptoms.clear();
    renderSelectedChips();
    updateAllSymptomButtonStates();
  };

  if (clearBtn) clearBtn.addEventListener('click', handleClearAll);
  if (clearBtnBottom) clearBtnBottom.addEventListener('click', handleClearAll);

  if (predictBtn) {
    predictBtn.addEventListener('click', handleGeneralPrediction);
  }
  const predictBtnTop = document.getElementById('predict-general-btn-top');
  if (predictBtnTop) {
    predictBtnTop.addEventListener('click', handleGeneralPrediction);
  }
}

async function loadSymptomsOnce() {
  if (symptomsLoaded) return;
  const container = document.getElementById('symptoms-categories-container');
  if (!container) return;

  container.innerHTML = '<div style="padding: 16px; color: #64748b;">Loading symptoms from ML service...</div>';

  try {
    const data = await api.getSymptoms();
    state.allSymptoms = (data && data.symptoms) || [];

    // Ensure any dynamically returned symptoms not in SYMPTOM_CATEGORIES are caught in 'Other'
    const categorizedSet = new Set(Object.values(SYMPTOM_CATEGORIES).flat());
    state.allSymptoms.forEach(sym => {
      if (!categorizedSet.has(sym)) {
        if (!SYMPTOM_CATEGORIES['Other']) SYMPTOM_CATEGORIES['Other'] = [];
        SYMPTOM_CATEGORIES['Other'].push(sym);
      }
    });

    symptomsLoaded = true;
    renderCategorizedSymptoms();
    renderSelectedChips();
  } catch (err) {
    container.innerHTML = `<div style="padding: 16px; color: #ef4444;">Failed to load symptoms: ${err.message}. Make sure Python FastAPI ML service and Spring Boot are running.</div>`;
  }
}

/**
 * Render the categorized symptom cards based on search and quick filter.
 */
function renderCategorizedSymptoms() {
  const container = document.getElementById('symptoms-categories-container');
  const emptyState = document.getElementById('no-symptoms-found');
  const searchQueryDisplay = document.getElementById('search-query-display');
  const matchingBadge = document.getElementById('matching-count-badge');
  if (!container) return;

  container.innerHTML = '';
  let totalMatchingSymptoms = 0;
  let visibleCategoriesCount = 0;

  Object.entries(SYMPTOM_CATEGORIES).forEach(([categoryName, symptoms]) => {
    // 1. Check quick filter
    if (currentActiveFilter !== 'all') {
      if (currentActiveFilter === 'common') {
        // Only keep common symptoms
        symptoms = symptoms.filter(s => COMMON_SYMPTOMS.has(s));
      } else if (currentActiveFilter.toLowerCase() !== categoryName.toLowerCase()) {
        // Filter is set to a specific category
        return;
      }
    }

    // 2. Check search term
    if (currentSearchTerm) {
      symptoms = symptoms.filter(s => {
        const raw = s.toLowerCase();
        const formatted = formatSymptomName(s).toLowerCase();
        return raw.includes(currentSearchTerm) || formatted.includes(currentSearchTerm);
      });
    }

    if (symptoms.length === 0) return;

    totalMatchingSymptoms += symptoms.length;
    visibleCategoriesCount++;

    // When actively searching, automatically expand matching categories
    const isExpanded = currentSearchTerm
      ? true
      : categoryExpandedState.has(categoryName);

    // Count how many in this category are currently selected
    const selectedInCategory = symptoms.filter(s => state.selectedSymptoms.has(s)).length;

    // Create Category Card
    const card = document.createElement('div');
    card.className = `category-card ${isExpanded ? 'expanded' : ''}`;
    card.setAttribute('data-category', categoryName);

    // Category Header
    const header = document.createElement('div');
    header.className = 'category-header';
    header.innerHTML = `
      <div class="category-title-group">
        <span class="category-arrow">►</span>
        <span class="category-name">${categoryName}</span>
        <span class="category-count">(${symptoms.length})</span>
      </div>
      <div style="display: flex; align-items: center; gap: 8px;">
        ${selectedInCategory > 0 ? `<span class="category-selected-tag">${selectedInCategory} selected</span>` : ''}
      </div>
    `;

    // Toggle collapse on header click
    header.addEventListener('click', () => {
      const willExpand = !card.classList.contains('expanded');
      card.classList.toggle('expanded', willExpand);
      if (willExpand) {
        categoryExpandedState.add(categoryName);
      } else {
        categoryExpandedState.delete(categoryName);
      }
    });

    // Category Body (Grid of symptom buttons)
    const body = document.createElement('div');
    body.className = 'category-body';

    symptoms.forEach(sym => {
      const isSelected = state.selectedSymptoms.has(sym);
      const btn = document.createElement('button');
      btn.type = 'button';
      btn.className = `symptom-btn ${isSelected ? 'selected' : ''}`;
      btn.setAttribute('data-symptom', sym);
      btn.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
      btn.innerHTML = `
        <span class="symptom-check-icon">✓</span>
        <span class="symptom-text">${formatSymptomName(sym)}</span>
      `;

      btn.addEventListener('click', (e) => {
        e.stopPropagation();
        toggleSymptom(sym);
      });

      body.appendChild(btn);
    });

    card.appendChild(header);
    card.appendChild(body);
    container.appendChild(card);
  });

  // Handle empty state
  if (visibleCategoriesCount === 0) {
    if (emptyState) {
      emptyState.style.display = 'block';
      if (searchQueryDisplay) searchQueryDisplay.textContent = currentSearchTerm || currentActiveFilter;
    }
    if (matchingBadge) matchingBadge.style.display = 'none';
  } else {
    if (emptyState) emptyState.style.display = 'none';
    if (matchingBadge) {
      if (currentSearchTerm || currentActiveFilter !== 'all') {
        matchingBadge.style.display = 'inline-block';
        matchingBadge.textContent = `${totalMatchingSymptoms} symptoms visible`;
      } else {
        matchingBadge.style.display = 'none';
      }
    }
  }
}

function toggleSymptom(sym) {
  if (state.selectedSymptoms.has(sym)) {
    state.selectedSymptoms.delete(sym);
  } else {
    state.selectedSymptoms.add(sym);
  }
  renderSelectedChips();
  updateAllSymptomButtonStates();
  updateCategorySelectedBadges();
}

function updateAllSymptomButtonStates() {
  const buttons = document.querySelectorAll('.symptom-btn');
  buttons.forEach(btn => {
    const sym = btn.getAttribute('data-symptom');
    const isSelected = state.selectedSymptoms.has(sym);
    btn.classList.toggle('selected', isSelected);
    btn.setAttribute('aria-pressed', isSelected ? 'true' : 'false');
  });
  updateCategorySelectedBadges();
}

function updateCategorySelectedBadges() {
  document.querySelectorAll('.category-card').forEach(card => {
    const catName = card.getAttribute('data-category');
    const symptoms = SYMPTOM_CATEGORIES[catName] || [];
    const count = symptoms.filter(s => state.selectedSymptoms.has(s)).length;
    const badgeContainer = card.querySelector('.category-header > div:last-child');
    if (badgeContainer) {
      badgeContainer.innerHTML = count > 0 ? `<span class="category-selected-tag">${count} selected</span>` : '';
    }
  });
}

function renderSelectedChips() {
  const chipsContainer = document.getElementById('selected-chips');
  const countSpan = document.getElementById('selected-count');
  if (!chipsContainer) return;

  chipsContainer.innerHTML = '';
  if (countSpan) countSpan.textContent = state.selectedSymptoms.size;

  if (state.selectedSymptoms.size === 0) {
    chipsContainer.innerHTML = '<span class="no-selection-msg">No symptoms selected. Click symptoms below or search to add.</span>';
    return;
  }

  state.selectedSymptoms.forEach(sym => {
    const chip = document.createElement('span');
    chip.className = 'chip';
    chip.innerHTML = `${formatSymptomName(sym)} <button type="button" class="chip-remove" aria-label="Remove ${formatSymptomName(sym)}">&times;</button>`;

    chip.querySelector('.chip-remove').addEventListener('click', (e) => {
      e.stopPropagation();
      state.selectedSymptoms.delete(sym);
      renderSelectedChips();
      updateAllSymptomButtonStates();
    });

    chipsContainer.appendChild(chip);
  });
}

function formatSymptomName(str) {
  if (!str) return '';
  return str.replace(/_/g, ' ').replace(/\s+/g, ' ').trim().replace(/\b\w/g, l => l.toUpperCase());
}

async function handleGeneralPrediction() {
  const alertContainer = document.getElementById('general-predict-alert');
  const resultCard = document.getElementById('general-result-card');
  const predictBtn = document.getElementById('predict-general-btn');
  const patientIdInput = document.getElementById('targetPatientId') || document.getElementById('general-patient-id');

  alertContainer.innerHTML = '';
  resultCard.style.display = 'none';

  if (!patientIdInput) {
    showAlert(alertContainer, 'danger', 'Target Patient ID input element was not found.');
    return;
  }

  // Obtain Target Patient ID directly from input as the single source of truth
  const rawPatientId = patientIdInput.value.trim();
  const targetPatientId = Number(rawPatientId);

  if (!rawPatientId || isNaN(targetPatientId) || targetPatientId <= 0 || !Number.isInteger(targetPatientId)) {
    showAlert(alertContainer, 'danger', 'Please enter a valid numeric Target Patient ID before running prediction.');
    patientIdInput.focus();
    return;
  }

  if (state.selectedSymptoms.size === 0) {
    showAlert(alertContainer, 'warning', 'Please select at least one symptom to generate a prediction.');
    return;
  }

  const symptomsArray = Array.from(state.selectedSymptoms);
  setButtonLoading(predictBtn, true, 'Analyzing Symptoms...');

  try {
    // Send request strictly to POST /api/patients/{targetPatientId}/predict (NOT global activePatientId)
    const res = await api.predictGeneralDisease(targetPatientId, symptomsArray);

    // Extract exact patient ID and prediction ID from response/target
    const predictionPatientId = (res && res.patientId != null) ? res.patientId : targetPatientId;
    const predictionId = res.id;

    // Synchronize active patient context to the patient of this prediction
    setActivePatient(predictionPatientId, res.patientName || `Patient #${predictionPatientId}`);

    // Update result card
    document.getElementById('general-result-disease').textContent = res.prediction || 'Unknown';
    const prob = res.probability !== null && res.probability !== undefined ? res.probability : 0;
    document.getElementById('general-result-prob-text').textContent = `${prob}%`;
    document.getElementById('general-result-prob-bar').style.width = `${Math.min(prob, 100)}%`;
    document.getElementById('general-result-id').textContent = predictionId;
    document.getElementById('general-result-patient').textContent = `${res.patientName || 'Patient'} (#${predictionPatientId})`;

    const symptomsListEl = document.getElementById('general-result-symptoms');
    symptomsListEl.innerHTML = '';
    (res.selectedSymptoms || symptomsArray).forEach(s => {
      const badge = document.createElement('span');
      badge.className = 'badge badge-general';
      badge.style.marginRight = '6px';
      badge.style.marginBottom = '4px';
      badge.textContent = formatSymptomName(s);
      symptomsListEl.appendChild(badge);
    });

    // Construct Download PDF Report button URL strictly using the patient ID and prediction ID of THIS prediction
    const downloadBtn = document.getElementById('general-download-pdf-btn');
    if (downloadBtn) {
      downloadBtn.onclick = () => {
        window.location.href = api.getReportUrl(predictionPatientId, predictionId);
      };
    }

    resultCard.style.display = 'block';
    resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    refreshDashboardStats();
  } catch (err) {
    showAlert(alertContainer, 'danger', `Prediction Failed: ${err.message}`);
  } finally {
    setButtonLoading(predictBtn, false, 'Predict Disease');
  }
}

/* ==========================================================================
   Heart Disease Prediction Logic
   ========================================================================== */
function initHeartPrediction() {
  const form = document.getElementById('heart-disease-form');
  const alertContainer = document.getElementById('heart-predict-alert');
  const resultCard = document.getElementById('heart-result-card');

  if (!form) return;

  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    alertContainer.innerHTML = '';
    resultCard.style.display = 'none';

    const patientIdInput = document.getElementById('heart-patient-id');
    const rawVal = patientIdInput ? patientIdInput.value.trim() : '';
    const targetPatientId = Number(rawVal);
    if (!rawVal || isNaN(targetPatientId) || targetPatientId <= 0 || !Number.isInteger(targetPatientId)) {
      showAlert(alertContainer, 'danger', 'Please enter a valid numeric Target Patient ID.');
      if (patientIdInput) patientIdInput.focus();
      return;
    }

    const payload = {
      age: parseFloat(document.getElementById('heart-age').value),
      sex: parseFloat(document.getElementById('heart-sex').value),
      cp: parseFloat(document.getElementById('heart-cp').value),
      trestbps: parseFloat(document.getElementById('heart-trestbps').value),
      chol: parseFloat(document.getElementById('heart-chol').value),
      fbs: parseFloat(document.getElementById('heart-fbs').value),
      restecg: parseFloat(document.getElementById('heart-restecg').value),
      thalach: parseFloat(document.getElementById('heart-thalach').value),
      exang: parseFloat(document.getElementById('heart-exang').value),
      oldpeak: parseFloat(document.getElementById('heart-oldpeak').value),
      slope: parseFloat(document.getElementById('heart-slope').value),
      ca: parseFloat(document.getElementById('heart-ca').value),
      thal: parseFloat(document.getElementById('heart-thal').value)
    };

    const submitBtn = form.querySelector('button[type="submit"]');
    setButtonLoading(submitBtn, true, 'Evaluating Clinical Features...');

    try {
      const res = await api.predictHeartDisease(targetPatientId, payload);
      const predictionPatientId = (res && res.patientId != null) ? res.patientId : targetPatientId;
      const predictionId = res.id;

      setActivePatient(predictionPatientId, res.patientName || `Patient #${predictionPatientId}`);

      const isPositive = res.prediction === 1 || (res.result && res.result.toLowerCase().includes('detected') && !res.result.toLowerCase().includes('no'));

      resultCard.className = 'result-card ' + (isPositive ? 'danger' : '');
      const badge = document.getElementById('heart-result-badge');
      badge.className = 'result-badge ' + (isPositive ? 'danger' : '');
      badge.textContent = isPositive ? 'High Risk' : 'Low Risk';

      document.getElementById('heart-result-title').textContent = res.result || (isPositive ? 'Heart Disease Detected' : 'No Heart Disease Detected');
      const prob = res.probability !== null && res.probability !== undefined ? res.probability : 0;
      document.getElementById('heart-result-prob-text').textContent = `${prob}%`;
      const fillBar = document.getElementById('heart-result-prob-bar');
      fillBar.style.width = `${Math.min(prob, 100)}%`;
      fillBar.className = 'probability-bar-fill ' + (isPositive ? 'danger' : '');

      document.getElementById('heart-result-id').textContent = predictionId;
      document.getElementById('heart-result-patient').textContent = `${res.patientName || 'Patient'} (#${predictionPatientId})`;

      const downloadBtn = document.getElementById('heart-download-pdf-btn');
      if (downloadBtn) {
        downloadBtn.onclick = () => {
          window.location.href = api.getReportUrl(predictionPatientId, predictionId);
        };
      }

      resultCard.style.display = 'block';
      resultCard.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      refreshDashboardStats();
    } catch (err) {
      showAlert(alertContainer, 'danger', `Heart Disease Evaluation Failed: ${err.message}`);
    } finally {
      setButtonLoading(submitBtn, false, 'Evaluate Heart Disease Risk');
    }
  });
}

/* ==========================================================================
   Prediction History Logic
   ========================================================================== */
function initHistory() {
  const fetchBtn = document.getElementById('fetch-history-btn');
  const viewAllBtn = document.getElementById('view-all-history-btn');

  if (fetchBtn) {
    fetchBtn.addEventListener('click', () => {
      const patientId = document.getElementById('history-patient-id').value.trim();
      const alertContainer = document.getElementById('history-alert');
      alertContainer.innerHTML = '';

      if (!patientId) {
        showAlert(alertContainer, 'danger', 'Please enter a Patient ID to view their history.');
        return;
      }
      loadPatientHistory(patientId);
    });
  }

  if (viewAllBtn) {
    viewAllBtn.addEventListener('click', () => {
      loadAllSystemHistory();
    });
  }
}

async function loadPatientHistory(patientId) {
  const tbody = document.getElementById('history-table-body');
  const alertContainer = document.getElementById('history-alert');
  const emptyState = document.getElementById('history-empty-state');
  const tableContainer = document.getElementById('history-table-container');

  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px;">Loading prediction records...</td></tr>';
  emptyState.style.display = 'none';
  tableContainer.style.display = 'block';

  try {
    const list = await api.getPatientPredictions(patientId);
    renderHistoryTable(list);
  } catch (err) {
    showAlert(alertContainer, 'danger', `Failed to load history for Patient #${patientId}: ${err.message}`);
    tbody.innerHTML = '';
  }
}

async function loadAllSystemHistory() {
  const tbody = document.getElementById('history-table-body');
  const alertContainer = document.getElementById('history-alert');
  const emptyState = document.getElementById('history-empty-state');
  const tableContainer = document.getElementById('history-table-container');

  tbody.innerHTML = '<tr><td colspan="8" style="text-align: center; padding: 20px;">Loading all system records...</td></tr>';
  emptyState.style.display = 'none';
  tableContainer.style.display = 'block';

  try {
    const list = await api.getAllPredictions();
    renderHistoryTable(list);
  } catch (err) {
    showAlert(alertContainer, 'danger', `Failed to load all predictions: ${err.message}`);
    tbody.innerHTML = '';
  }
}

function renderHistoryTable(records) {
  const tbody = document.getElementById('history-table-body');
  const emptyState = document.getElementById('history-empty-state');
  const tableContainer = document.getElementById('history-table-container');

  tbody.innerHTML = '';

  if (!records || records.length === 0) {
    tableContainer.style.display = 'none';
    emptyState.style.display = 'block';
    return;
  }

  tableContainer.style.display = 'block';
  emptyState.style.display = 'none';

  records.forEach(rec => {
    const tr = document.createElement('tr');

    const isHeart = rec.predictionType === 'HEART_DISEASE';
    const typeBadge = isHeart
      ? '<span class="badge badge-heart">Heart Disease</span>'
      : '<span class="badge badge-general">General Disease</span>';

    const probText = rec.probability !== null && rec.probability !== undefined
      ? `${rec.probability}%`
      : 'N/A';

    const formattedDate = rec.createdAt ? new Date(rec.createdAt).toLocaleString() : 'N/A';

    let inputSummary = rec.inputData || '';
    if (inputSummary.length > 50) {
      inputSummary = inputSummary.substring(0, 47) + '...';
    }

    const downloadUrl = api.getReportUrl(rec.patientId, rec.id);

    tr.innerHTML = `
      <td><strong>#${rec.id}</strong></td>
      <td>${rec.patientName ? `${rec.patientName} (#${rec.patientId})` : `#${rec.patientId}`}</td>
      <td>${typeBadge}</td>
      <td><strong>${rec.predictedDisease || 'N/A'}</strong></td>
      <td>${probText}</td>
      <td><span style="font-family: monospace; font-size: 0.8rem; background: #f1f5f9; padding: 2px 6px; border-radius: 4px;" title="${escapeHtml(rec.inputData)}">${escapeHtml(inputSummary)}</span></td>
      <td>${formattedDate}</td>
      <td><a href="${downloadUrl}" class="btn btn-secondary" style="padding: 4px 10px; font-size: 0.78rem; text-decoration: none;" download>📄 Download PDF</a></td>
    `;

    tbody.appendChild(tr);
  });
}

/* ==========================================================================
   Utility Helpers
   ========================================================================== */
function showAlert(container, type, message) {
  if (!container) return;
  container.innerHTML = `
    <div class="alert alert-${type}">
      <div>${message}</div>
    </div>
  `;
}

function setButtonLoading(button, isLoading, text) {
  if (!button) return;
  button.disabled = isLoading;
  if (isLoading) {
    button.innerHTML = `<span class="spinner"></span> ${text}`;
  } else {
    button.innerHTML = text;
  }
}

function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;');
}
