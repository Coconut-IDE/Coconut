package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianshi on 4/27/17.
 */
public abstract class AnnotationHolder {
    public Map<String, ArrayList<String>> plainValueFieldPairs = new HashMap<>();
    public Map<PsiElement, ArrayList<PsiElement>> psiElementFieldPairs = new HashMap<>();
    public CoconutAnnotationType mAnnotationType;

    public String getDescription() {
        String dataType = "Unknown data type";
        String purposeCategory = "Unknown purpose category";
        String purpose = "Unknown purpose";
        if (plainValueFieldPairs.containsKey("dataType")) {
            dataType = String.join(";", plainValueFieldPairs.get("dataType"));
        }
        if (plainValueFieldPairs.containsKey("purpose")) {
            purposeCategory = String.join(";", plainValueFieldPairs.get("purpose"));
        }
        if (plainValueFieldPairs.containsKey("purposeDescription")) {
            purpose = String.join(";", plainValueFieldPairs.get("purposeDescription"));
        }
        return String.format("Data Type: %s. Purpose: (%s) %s", dataType, purposeCategory, purpose);
    }

    public String getPurpose() {
        String purpose = "Unknown purpose";
        if (plainValueFieldPairs.containsKey("purposeDescription")) {
            purpose = String.join(";", plainValueFieldPairs.get("purposeDescription"));
        }
        return purpose;
    }

    public String getDescriptionByGroup(PersonalDataGroup group) {
        return getDescription();
    }

    public String getDescriptionByPermission(AndroidPermission permission) {
        return getDescription();
    }

    public AnnotationHolder() {
    }
    public AnnotationHolder(PsiAnnotation annotation) {
        PsiNameValuePair[] annotationNameValuePairs = annotation.getParameterList().getAttributes();
        for (PsiElement element : annotationNameValuePairs) {
            PsiNameValuePair nameValuePair = (PsiNameValuePair) element;
            if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                continue;
            }
            PsiIdentifier nameIdentifier = nameValuePair.getNameIdentifier();
            String name = nameValuePair.getName();
            PsiElement value = nameValuePair.getValue();
            ArrayList<String> elementArrayList = new ArrayList<>();
            ArrayList<PsiElement> psiElementArrayList = new ArrayList<>();
            for (PsiElement psiElement: value.getChildren()) {
                if ("{".equals(psiElement.getText()) || "}".equals(psiElement.getText())) {
                    continue;
                }
                elementArrayList.add(psiElement.getText());
                psiElementArrayList.add(psiElement);
            }
            plainValueFieldPairs.put(name, elementArrayList);
            psiElementFieldPairs.put(nameIdentifier, psiElementArrayList);
        }
    }
    public void initAllFields() {
        String typename = mAnnotationType.toString();
        assert CodeInspectionUtil.ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.containsKey(typename);
        Map<String, String> field_init_value_mapping = CodeInspectionUtil.ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING.get(typename);
        for (Map.Entry<String, String> field: field_init_value_mapping.entrySet()) {
            add(field.getKey(), field.getValue());
        }
    }

    public void remove(String key) {
        plainValueFieldPairs.remove(key);
    }

    public void put(String key, ArrayList<String> values) {
        plainValueFieldPairs.put(key, values);
    }
    public void put(String key, String value) {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(value);
        plainValueFieldPairs.put(key, arrayList);
    }
    public void add(String key, String value) {
        ArrayList<String> arrayList;
        if (plainValueFieldPairs.containsKey(key)) {
            arrayList = plainValueFieldPairs.get(key);
        } else {
            arrayList = new ArrayList<>();
        }
        arrayList.add(value);
        plainValueFieldPairs.put(key, arrayList);
    }
    public void add(String key, ArrayList<String> values) {
        plainValueFieldPairs.put(key, values);
    }
    public String getAnnotationString() {
        String annotationContentString = "(\n";
        ArrayList<String> generatedAnnotationItems = new ArrayList<>();
        String[] keyList = CodeInspectionUtil.ANNOTATION_FIELD_ORDER.getOrDefault(mAnnotationType.toString(), null);
        if (keyList != null) {
            for (String key : keyList) {
                String valueStr;
                if ("ID".equals(key)) {
                    // plainValueFieldPairs should only contain one element, which is the ID that uniquely identifies
                    // the personal data instance
                    valueStr = plainValueFieldPairs.get(key).get(0);
                } else {
                    valueStr = "{" + String.join(",", plainValueFieldPairs.get(key)) + "}";
                }
                generatedAnnotationItems.add(key + " = " + valueStr);
            }
        }
        annotationContentString += String.join(",\n", generatedAnnotationItems);
        annotationContentString += ")\n";
        return mAnnotationType.toString() + annotationContentString;
    }
    public boolean containsKey(String key) {
        return plainValueFieldPairs.containsKey(key);
    }

    public boolean containsPair(String key, String value) {
        if (!plainValueFieldPairs.containsKey(key)) {
            return false;
        }
        return plainValueFieldPairs.get(key).contains(value);
    }
}
