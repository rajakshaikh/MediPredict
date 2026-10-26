package com.diseaseprediction.service;

import com.diseaseprediction.exception.MlServiceException;
import com.diseaseprediction.model.Patient;
import com.diseaseprediction.model.PredictionHistory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class PdfReportService {

    private static final Logger log = LoggerFactory.getLogger(PdfReportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private final ObjectMapper objectMapper;

    // Theme Colors
    private static final Color PRIMARY_COLOR = new Color(2, 132, 199);      // #0284c7 (Medical Blue)
    private static final Color DARK_NAVY = new Color(15, 23, 42);          // #0f172a
    private static final Color BG_LIGHT = new Color(248, 250, 252);        // #f8fafc
    private static final Color BORDER_COLOR = new Color(226, 232, 240);    // #e2e8f0
    private static final Color TEXT_MUTED = new Color(100, 116, 139);      // #64748b
    private static final Color TEXT_DARK = new Color(30, 41, 59);          // #1e293b
    private static final Color ALERT_BG = new Color(254, 242, 242);        // #fef2f2 (for high risk)

    public PdfReportService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Generate PDF Medical Report for a patient's prediction record.
     */
    public byte[] generateMedicalReport(Patient patient, PredictionHistory prediction) {
        Document document = new Document(PageSize.A4, 40, 40, 40, 40);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // 1. Header Banner
            addHeader(document);

            // Spacing
            document.add(new Paragraph(" "));

            // 2. Patient Information
            addPatientInformation(document, patient);

            document.add(new Paragraph(" "));

            // 3. Prediction Information
            addPredictionInformation(document, prediction);

            document.add(new Paragraph(" "));

            // 4. Input Information (Symptoms or 13 Clinical Parameters)
            addInputInformation(document, prediction);

            document.add(new Paragraph(" "));

            // 5. Medical Disclaimer
            addDisclaimer(document);

            document.add(new Paragraph(" "));

            // 6. Footer
            addFooter(document);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF report for patient {} and prediction {}: {}",
                    patient.getId(), prediction.getId(), e.getMessage(), e);
            throw new MlServiceException("Failed to generate PDF report: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document document) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(DARK_NAVY);
        cell.setPadding(16);
        cell.setBorder(Rectangle.NO_BORDER);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.WHITE);
        Paragraph title = new Paragraph("MEDIPREDICT", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);

        Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(186, 230, 253));
        Paragraph subtitle = new Paragraph("Clinical Decision Support System — Diagnostic Prediction Report", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(title);
        cell.addElement(subtitle);
        headerTable.addCell(cell);

        document.add(headerTable);
    }

    private void addPatientInformation(Document document, Patient patient) throws DocumentException {
        addSectionHeader(document, "Patient Information");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        String reportDate = LocalDateTime.now().format(DATE_FORMATTER);

        addKeyValueCell(table, "Patient ID:", "#" + patient.getId());
        addKeyValueCell(table, "Report Generated:", reportDate);
        addKeyValueCell(table, "Patient Name:", patient.getName() != null ? patient.getName() : "N/A");
        addKeyValueCell(table, "Age / Gender:", (patient.getAge() != null ? patient.getAge() : "N/A") + " / " + (patient.getGender() != null ? patient.getGender() : "N/A"));
        addKeyValueCell(table, "Email Address:", patient.getEmail() != null ? patient.getEmail() : "N/A");
        addKeyValueCell(table, "Registered Date:", patient.getCreatedAt() != null ? patient.getCreatedAt().format(DATE_FORMATTER) : "N/A");

        document.add(table);
    }

    private void addPredictionInformation(Document document, PredictionHistory prediction) throws DocumentException {
        addSectionHeader(document, "Prediction Information");

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        String displayType = "HEART_DISEASE".equalsIgnoreCase(prediction.getPredictionType())
                ? "Heart Disease Risk Assessment"
                : "General Disease Prediction";

        String predDate = prediction.getCreatedAt() != null
                ? prediction.getCreatedAt().format(DATE_FORMATTER)
                : "N/A";

        String probText = prediction.getProbability() != null
                ? String.format("%.1f%%", prediction.getProbability())
                : "N/A";

        addKeyValueCell(table, "Prediction Type:", displayType);
        addKeyValueCell(table, "Prediction ID:", "#" + prediction.getId());
        addKeyValueCell(table, "Predicted Disease / Result:", prediction.getPredictedDisease() != null ? prediction.getPredictedDisease() : "N/A", true);
        addKeyValueCell(table, "Probability / Confidence:", probText);
        addKeyValueCell(table, "Prediction Date:", predDate);
        addKeyValueCell(table, "Validation Status:", "Validated by AI Random Forest Model");

        document.add(table);
    }

    private void addInputInformation(Document document, PredictionHistory prediction) throws DocumentException {
        boolean isHeart = "HEART_DISEASE".equalsIgnoreCase(prediction.getPredictionType());

        if (isHeart) {
            addSectionHeader(document, "Input Information (13 Clinical Parameters)");
            addHeartDiseaseInputTable(document, prediction.getInputData());
        } else {
            addSectionHeader(document, "Input Information (Selected Symptoms)");
            addGeneralDiseaseInputTable(document, prediction.getInputData());
        }
    }

    private void addGeneralDiseaseInputTable(Document document, String inputData) throws DocumentException {
        List<String> symptoms = null;

        if (inputData != null && !inputData.trim().isEmpty()) {
            try {
                symptoms = objectMapper.readValue(inputData, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                log.debug("Could not parse symptoms as JSON array, using fallback split: {}", e.getMessage());
            }
        }

        if (symptoms == null || symptoms.isEmpty()) {
            Font font = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, TEXT_MUTED);
            document.add(new Paragraph("No specific symptoms recorded.", font));
            return;
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        int index = 1;
        for (String sym : symptoms) {
            String formatted = sym.replace("_", " ").trim();
            formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);

            PdfPCell cell = new PdfPCell();
            cell.setPadding(6);
            cell.setBackgroundColor(BG_LIGHT);
            cell.setBorderColor(BORDER_COLOR);

            Font f = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_DARK);
            cell.addElement(new Paragraph("• " + formatted, f));
            table.addCell(cell);
            index++;
        }

        // If odd number of symptoms, pad with an empty cell
        if (symptoms.size() % 2 != 0) {
            PdfPCell empty = new PdfPCell();
            empty.setBorderColor(BORDER_COLOR);
            empty.setBackgroundColor(BG_LIGHT);
            table.addCell(empty);
        }

        document.add(table);
    }

    private void addHeartDiseaseInputTable(Document document, String inputData) throws DocumentException {
        Map<String, Object> params = null;

        if (inputData != null && !inputData.trim().isEmpty()) {
            try {
                params = objectMapper.readValue(inputData, new TypeReference<Map<String, Object>>() {});
            } catch (Exception e) {
                log.debug("Could not parse heart disease input as JSON map: {}", e.getMessage());
            }
        }

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1, 1});

        if (params == null || params.isEmpty()) {
            addKeyValueCell(table, "Input Data:", inputData != null ? inputData : "N/A");
            document.add(table);
            return;
        }

        addKeyValueCell(table, "1. Age:", String.valueOf(params.getOrDefault("age", "N/A")));
        addKeyValueCell(table, "2. Sex:", formatSex(params.get("sex")));
        addKeyValueCell(table, "3. Chest Pain Type (cp):", formatChestPain(params.get("cp")));
        addKeyValueCell(table, "4. Resting Blood Pressure (trestbps):", params.getOrDefault("trestbps", "N/A") + " mm Hg");
        addKeyValueCell(table, "5. Serum Cholesterol (chol):", params.getOrDefault("chol", "N/A") + " mg/dl");
        addKeyValueCell(table, "6. Fasting Blood Sugar > 120 (fbs):", formatFbs(params.get("fbs")));
        addKeyValueCell(table, "7. Resting ECG (restecg):", formatRestEcg(params.get("restecg")));
        addKeyValueCell(table, "8. Maximum Heart Rate (thalach):", String.valueOf(params.getOrDefault("thalach", "N/A")));
        addKeyValueCell(table, "9. Exercise Induced Angina (exang):", formatExang(params.get("exang")));
        addKeyValueCell(table, "10. ST Depression (oldpeak):", String.valueOf(params.getOrDefault("oldpeak", "N/A")));
        addKeyValueCell(table, "11. Slope of Peak ST (slope):", formatSlope(params.get("slope")));
        addKeyValueCell(table, "12. Major Vessels (ca):", String.valueOf(params.getOrDefault("ca", "N/A")));
        addKeyValueCell(table, "13. Thalassemia (thal):", formatThal(params.get("thal")));

        // Pad last cell to balance 2-column layout (13 items -> 14 cells)
        PdfPCell empty = new PdfPCell();
        empty.setBorderColor(BORDER_COLOR);
        empty.setBackgroundColor(BG_LIGHT);
        table.addCell(empty);

        document.add(table);
    }

    private void addDisclaimer(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);

        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(254, 243, 199)); // Warm amber-light
        cell.setBorderColor(new Color(245, 158, 11));     // Amber border
        cell.setPadding(10);

        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new Color(146, 64, 14));
        Paragraph heading = new Paragraph("DISCLAIMER", headingFont);
        heading.setSpacingAfter(4);

        Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 8.5f, new Color(120, 53, 15));
        Paragraph text = new Paragraph(
                "This report is generated by an AI/ML-based prediction system for educational and decision-support " +
                "purposes. It is not a medical diagnosis. Please consult a qualified healthcare professional for medical advice.",
                textFont
        );

        cell.addElement(heading);
        cell.addElement(text);
        table.addCell(cell);

        document.add(table);
    }

    private void addFooter(Document document) throws DocumentException {
        Paragraph p = new Paragraph("MEDIPREDICT — End of Report",
                FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, TEXT_MUTED));
        p.setAlignment(Element.ALIGN_CENTER);
        document.add(p);
    }

    private void addSectionHeader(Document document, String title) throws DocumentException {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, PRIMARY_COLOR);
        Paragraph p = new Paragraph(title, headerFont);
        p.setSpacingBefore(4);
        p.setSpacingAfter(6);
        document.add(p);
    }

    private void addKeyValueCell(PdfPTable table, String label, String value) {
        addKeyValueCell(table, label, value, false);
    }

    private void addKeyValueCell(PdfPTable table, String label, String value, boolean highlight) {
        PdfPCell cell = new PdfPCell();
        cell.setPadding(6);
        cell.setBackgroundColor(BG_LIGHT);
        cell.setBorderColor(BORDER_COLOR);

        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 9, TEXT_MUTED);
        Paragraph labelPara = new Paragraph(label, labelFont);

        Font valueFont = highlight
                ? FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, PRIMARY_COLOR)
                : FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, TEXT_DARK);
        Paragraph valuePara = new Paragraph(value, valueFont);

        cell.addElement(labelPara);
        cell.addElement(valuePara);
        table.addCell(cell);
    }

    // Value formatters for heart clinical parameters
    private String formatSex(Object val) {
        if (val == null) return "N/A";
        double num = ((Number) val).doubleValue();
        return num == 1.0 ? "1 (Male)" : "0 (Female)";
    }

    private String formatChestPain(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return switch (num) {
            case 0 -> "0 (Typical Angina)";
            case 1 -> "1 (Atypical Angina)";
            case 2 -> "2 (Non-anginal Pain)";
            case 3 -> "3 (Asymptomatic)";
            default -> String.valueOf(num);
        };
    }

    private String formatFbs(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return num == 1 ? "1 (True > 120 mg/dl)" : "0 (False <= 120 mg/dl)";
    }

    private String formatRestEcg(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return switch (num) {
            case 0 -> "0 (Normal)";
            case 1 -> "1 (ST-T Wave Abnormality)";
            case 2 -> "2 (Left Ventricular Hypertrophy)";
            default -> String.valueOf(num);
        };
    }

    private String formatExang(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return num == 1 ? "1 (Yes)" : "0 (No)";
    }

    private String formatSlope(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return switch (num) {
            case 0 -> "0 (Upsloping)";
            case 1 -> "1 (Flat)";
            case 2 -> "2 (Downsloping)";
            default -> String.valueOf(num);
        };
    }

    private String formatThal(Object val) {
        if (val == null) return "N/A";
        int num = ((Number) val).intValue();
        return switch (num) {
            case 1 -> "1 (Normal)";
            case 2 -> "2 (Fixed Defect)";
            case 3 -> "3 (Reversible Defect)";
            default -> String.valueOf(num);
        };
    }
}
