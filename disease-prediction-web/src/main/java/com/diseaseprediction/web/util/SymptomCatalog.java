package com.diseaseprediction.web.util;

import java.util.*;

public class SymptomCatalog {

    /**
     * Converts snake_case symptom key to Title Case human readable label.
     */
    public static String formatSymptomName(String symptom) {
        if (symptom == null || symptom.isBlank()) {
            return "";
        }
        String cleaned = symptom.replace("_", " ").trim();
        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split("\\s+")) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0)))
                  .append(word.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Categorizes a list of symptoms into anatomical / clinical groups for organized JSP rendering.
     */
    public static Map<String, List<String>> categorizeSymptoms(List<String> rawSymptoms) {
        Map<String, List<String>> categorized = new LinkedHashMap<>();

        categorized.put("General & Systemic", new ArrayList<>());
        categorized.put("Fever & Infection", new ArrayList<>());
        categorized.put("Respiratory & ENT", new ArrayList<>());
        categorized.put("Digestive & Gastrointestinal", new ArrayList<>());
        categorized.put("Skin & Dermatological", new ArrayList<>());
        categorized.put("Pain & Musculoskeletal", new ArrayList<>());
        categorized.put("Neurological & Sensory", new ArrayList<>());
        categorized.put("Cardiovascular & Circulatory", new ArrayList<>());
        categorized.put("Urinary & Renal", new ArrayList<>());
        categorized.put("Other Clinical Signs", new ArrayList<>());

        for (String s : rawSymptoms) {
            String lower = s.toLowerCase();
            if (lower.contains("fever") || lower.contains("chill") || lower.contains("shiver") || lower.contains("sweat") || lower.contains("toxic")) {
                categorized.get("Fever & Infection").add(s);
            } else if (lower.contains("cough") || lower.contains("breath") || lower.contains("throat") || lower.contains("sneez") || lower.contains("sputum") || lower.contains("sinus") || lower.contains("runny") || lower.contains("congestion")) {
                categorized.get("Respiratory & ENT").add(s);
            } else if (lower.contains("stomach") || lower.contains("belly") || lower.contains("vomit") || lower.contains("nausea") || lower.contains("diarrh") || lower.contains("constip") || lower.contains("acidity") || lower.contains("indigest") || lower.contains("appetite") || lower.contains("stool") || lower.contains("bowel") || lower.contains("anus") || lower.contains("gas") || lower.contains("liver") || lower.contains("distention")) {
                categorized.get("Digestive & Gastrointestinal").add(s);
            } else if (lower.contains("skin") || lower.contains("rash") || lower.contains("pimple") || lower.contains("blister") || lower.contains("nail") || lower.contains("peeling") || lower.contains("itching") || lower.contains("spot") || lower.contains("nodal") || lower.contains("bruis") || lower.contains("ooze")) {
                categorized.get("Skin & Dermatological").add(s);
            } else if (lower.contains("pain") || lower.contains("cramp") || lower.contains("stiff") || lower.contains("knee") || lower.contains("hip") || lower.contains("joint") || lower.contains("muscle") || lower.contains("back")) {
                categorized.get("Pain & Musculoskeletal").add(s);
            } else if (lower.contains("headache") || lower.contains("dizz") || lower.contains("spin") || lower.contains("balance") || lower.contains("unsteadi") || lower.contains("sensorium") || lower.contains("speech") || lower.contains("vision") || lower.contains("eye") || lower.contains("smell") || lower.contains("concentrat")) {
                categorized.get("Neurological & Sensory").add(s);
            } else if (lower.contains("heart") || lower.contains("palpitat") || lower.contains("vein") || lower.contains("fluid") || lower.contains("swelling") || lower.contains("swollen")) {
                categorized.get("Cardiovascular & Circulatory").add(s);
            } else if (lower.contains("urin") || lower.contains("micturit") || lower.contains("polyuria") || lower.contains("bladder")) {
                categorized.get("Urinary & Renal").add(s);
            } else if (lower.contains("fatigue") || lower.contains("lethargy") || lower.contains("malaise") || lower.contains("weakness") || lower.contains("weight") || lower.contains("anxiety") || lower.contains("mood") || lower.contains("depress") || lower.contains("irritab") || lower.contains("restless")) {
                categorized.get("General & Systemic").add(s);
            } else {
                categorized.get("Other Clinical Signs").add(s);
            }
        }

        // Remove empty categories
        categorized.entrySet().removeIf(e -> e.getValue().isEmpty());

        return categorized;
    }
}
