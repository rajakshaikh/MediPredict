/**
 * MediPredict Web Tier Client-Side Script
 * Interactive behaviors for JSP views (symptom search, chips, filters, sync)
 */

document.addEventListener('DOMContentLoaded', function () {
    initPatientIdSync();
    initSymptomsManager();
});

/**
 * Synchronize patient dropdown with numeric input
 */
function syncPatientId(selectedVal) {
    const input = document.getElementById('patientId');
    if (input && selectedVal) {
        input.value = selectedVal;
    }
}

function syncHistoryPatientId(selectedVal) {
    const input = document.getElementById('historyPatientId');
    if (input) {
        input.value = selectedVal || '';
    }
}

function initPatientIdSync() {
    const select = document.getElementById('patientIdSelect');
    const input = document.getElementById('patientId');
    if (select && input && input.value) {
        select.value = input.value;
    }
}

/**
 * 132-Symptom Manager for predict-general.jsp
 */
function initSymptomsManager() {
    const catalog = document.getElementById('symptoms-catalog');
    if (!catalog) return; // Not on general predict page

    const searchBox = document.getElementById('symptom-search-box');
    const clearSearchBtn = document.getElementById('clear-search-btn');
    const categoryPills = document.querySelectorAll('.cat-pill');
    const checkboxes = document.querySelectorAll('.symptom-checkbox');
    const chipsContainer = document.getElementById('chips-container');
    const chipsEmptyHint = document.getElementById('chips-empty-hint');
    const counterBadge = document.getElementById('selected-count');
    const clearAllBtn = document.getElementById('clear-all-symptoms-btn');
    const clearAllBtnBottom = document.getElementById('clear-all-symptoms-btn-bottom');
    const form = document.getElementById('general-predict-form');

    // Update chips and counter on change
    function updateSelection() {
        const checkedBoxes = Array.from(checkboxes).filter(cb => cb.checked);
        if (counterBadge) {
            counterBadge.textContent = checkedBoxes.length;
        }

        // Highlight label items
        checkboxes.forEach(cb => {
            const item = cb.closest('.symptom-item');
            if (item) {
                if (cb.checked) {
                    item.classList.add('checked');
                } else {
                    item.classList.remove('checked');
                }
            }
        });

        // Re-render chips
        if (chipsContainer) {
            chipsContainer.innerHTML = '';
            if (checkedBoxes.length === 0) {
                if (chipsEmptyHint) chipsEmptyHint.style.display = 'inline';
            } else {
                if (chipsEmptyHint) chipsEmptyHint.style.display = 'none';
                checkedBoxes.forEach(cb => {
                    const chip = document.createElement('span');
                    chip.className = 'chip';
                    const labelText = cb.value.replace(/_/g, ' ');
                    chip.innerHTML = labelText + ' <span class="chip-remove" title="Remove symptom" data-target="' + cb.id + '">&times;</span>';
                    chipsContainer.appendChild(chip);
                });
            }
        }
    }

    // Attach checkbox change listeners
    checkboxes.forEach(cb => {
        cb.addEventListener('change', updateSelection);
    });

    // Remove chip click delegation
    if (chipsContainer) {
        chipsContainer.addEventListener('click', function (e) {
            if (e.target && e.target.classList.contains('chip-remove')) {
                const targetId = e.target.getAttribute('data-target');
                const cb = document.getElementById(targetId);
                if (cb) {
                    cb.checked = false;
                    updateSelection();
                }
            }
        });
    }

    // Clear all symptoms
    function clearAll() {
        checkboxes.forEach(cb => {
            cb.checked = false;
        });
        updateSelection();
    }

    if (clearAllBtn) clearAllBtn.addEventListener('click', clearAll);
    if (clearAllBtnBottom) clearAllBtnBottom.addEventListener('click', clearAll);

    // Live search filter
    if (searchBox) {
        searchBox.addEventListener('input', function () {
            const query = searchBox.value.trim().toLowerCase();
            if (clearSearchBtn) {
                clearSearchBtn.style.display = query.length > 0 ? 'block' : 'none';
            }
            filterSymptoms(query, getActiveCategory());
        });
    }

    if (clearSearchBtn) {
        clearSearchBtn.addEventListener('click', function () {
            searchBox.value = '';
            clearSearchBtn.style.display = 'none';
            filterSymptoms('', getActiveCategory());
            searchBox.focus();
        });
    }

    // Category pills filter
    categoryPills.forEach(pill => {
        pill.addEventListener('click', function () {
            categoryPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            const category = pill.getAttribute('data-category');
            const query = searchBox ? searchBox.value.trim().toLowerCase() : '';
            filterSymptoms(query, category);
        });
    });

    function getActiveCategory() {
        const activePill = document.querySelector('.cat-pill.active');
        return activePill ? activePill.getAttribute('data-category') : 'all';
    }

    function filterSymptoms(query, category) {
        const groups = document.querySelectorAll('.symptom-group-block');
        groups.forEach(group => {
            const groupCategory = group.getAttribute('data-category');
            const matchesCategory = (category === 'all' || category === groupCategory);

            if (!matchesCategory) {
                group.style.display = 'none';
                return;
            }

            let visibleInGroup = 0;
            const items = group.querySelectorAll('.symptom-item');
            items.forEach(item => {
                const searchData = item.getAttribute('data-search') || '';
                const matchesQuery = !query || searchData.toLowerCase().includes(query);
                if (matchesQuery) {
                    item.style.display = 'flex';
                    visibleInGroup++;
                } else {
                    item.style.display = 'none';
                }
            });

            group.style.display = visibleInGroup > 0 ? 'block' : 'none';
        });
    }

    // Prevent submitting without symptoms
    if (form) {
        form.addEventListener('submit', function (e) {
            const checkedCount = Array.from(checkboxes).filter(cb => cb.checked).length;
            if (checkedCount === 0) {
                e.preventDefault();
                alert('Please select at least one medical symptom to run the disease prediction.');
            }
        });
    }

    // Initial run
    updateSelection();
}
