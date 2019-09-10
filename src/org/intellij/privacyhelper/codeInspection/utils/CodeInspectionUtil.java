/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.*;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.NavigateToCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.SetInconsistentFieldValueQuickfix;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 3/26/17.
 */
public class CodeInspectionUtil {

    private static final String INCONSISTENT_ANNOTATION_FIELD = "The values of this field are inconsistent with the code. The values speculated from the code is %s. (Quick-fixes provided)";
    private static String EMPTY_FIELD = "The field needs to be filled";
    private static String UNCOMPLETE_FIELD_VALUE = "This value is blank";

    /**
     * Relates each Coconut Annotation Type to a Personal Data Group
     */
    public static final Map<CoconutAnnotationType, PersonalDataGroup> annotationTypeToPersonalDataGroupMap =
            Collections.unmodifiableMap(new HashMap<CoconutAnnotationType, PersonalDataGroup>() {{
                put(CoconutAnnotationType.UniqueIdentifierAnnotation, PersonalDataGroup.UNIQUE_IDENTIFIER);
                put(CoconutAnnotationType.LocationAnnotation, PersonalDataGroup.LOCATION);
                put(CoconutAnnotationType.CameraAnnotation, PersonalDataGroup.CAMERA);
                put(CoconutAnnotationType.ContactsAnnotation, PersonalDataGroup.CONTACTS);
                put(CoconutAnnotationType.CalendarAnnotation, PersonalDataGroup.CALENDAR);
                put(CoconutAnnotationType.CallLogsAnnotation, PersonalDataGroup.CALL_LOG);
                put(CoconutAnnotationType.SMSAnnotation, PersonalDataGroup.SMS);
                put(CoconutAnnotationType.SensorAnnotation, PersonalDataGroup.SENSORS);
                put(CoconutAnnotationType.MicrophoneAnnotation, PersonalDataGroup.MICROPHONE);
                put(CoconutAnnotationType.PersonallyIdentifiableInformationAnnotation, PersonalDataGroup.PERSONALLY_IDENTIFIABLE_INFORMATION);
                put(CoconutAnnotationType.UserFileAnnotation, PersonalDataGroup.USER_FILE);
                put(CoconutAnnotationType.UserInputAnnotation, PersonalDataGroup.USER_INPUT);
                put(CoconutAnnotationType.UndefinedPersonalDataTypeAnnotation, PersonalDataGroup.OTHER_PERSONAL_DATA);
            }});

    /**
     * Sets the fields and order for annotations of each type. This determines which fields must be completed for each annotation by the user.
     */
    public static final Map<String, String []> ANNOTATION_FIELD_ORDER =
            Collections.unmodifiableMap(new HashMap<String, String []>() {{
                put("CallLogsAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("SMSAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("CalendarAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("CameraAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("ContactsAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("LocationAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility", "frequency"});
                put("UniqueIdentifierAnnotation", new String[] {"ID", "purpose", "purposeDescription", "uidType", "scope", "resettability"});
                put("MicrophoneAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("SensorAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("PersonallyIdentifiableInformationAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("UserFileAnnotation", new String[] {"ID", "purpose", "purposeDescription", "dataType", "visibility"});
                put("UserInputAnnotation", new String[] {"ID", "purposeDescription", "dataType", "visibility"});
                put("StorageAnnotation", new String[] {"purposeDescription", "accessControl", "retentionTime"});
                put("NetworkAnnotation", new String[] {"purposeDescription", "destination", "encryptedInTransmission", "retentionTime"});
                put("NotPersonalData", new String[] {});
                put("AdmobAnnotation", new String[] {"isLocationDataEnabledInAppSettings"});
                put("UndefinedPersonalDataTypeAnnotation", new String[] {"ID", "purposeDescription", "dataType", "visibility", "frequency"});
            }});
    // For enum class fields, the default value should be *.UNKNOWN (like the purpose field)
    // For string class fields, the default value should be the string of an empty string (like the purposeDescription field)
    // An example of the annotation:
    // @CalendarAnnotation(purpose=CalendarPurpose.UNKNOWN, purposeDescription="", dataType=CalendarDataType.UNKNOWN, visibility=Visibility.UNKNOWN)
    public static final Map<String, Map<String, String>> ANNOTATION_TYPE_FIELDS_INIT_VALUE_MAPPING =
            Collections.unmodifiableMap(new HashMap<String, Map<String,String>> () {{
                put("CallLogsAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "CallLogsPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "CallLogsDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("SMSAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "SMSPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "SMSDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("CalendarAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "CalendarPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "CalendarDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("CameraAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "CameraPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "CameraDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("ContactsAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "ContactsPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "ContactsDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("LocationAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "LocationPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "LocationDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                            put("frequency", "\"\"");
                        }}));
                put("UniqueIdentifierAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "UIDPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("uidType", "UIDType.UNKNOWN");
                            put("scope", "UIDScope.UNKNOWN");
                            put("resettability", "UIDResettability.UNKNOWN");
                        }}));
                put("MicrophoneAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "MicrophonePurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "MicrophoneDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("SensorAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "SensorPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "SensorDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("PersonallyIdentifiableInformationAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "PersonallyIdentifiableInformationPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "PersonallyIdentifiableInformationDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("UserFileAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purpose", "UserDataPurpose.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("dataType", "UserDataDataType.UNKNOWN");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("UserInputAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purposeDescription", "\"\"");
                            put("dataType", "\"\"");
                            put("visibility", "Visibility.UNKNOWN");
                        }}));
                put("UndefinedPersonalDataTypeAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\"\"");
                            put("purposeDescription", "\"\"");
                            put("dataType", "\"\"");
                            put("visibility", "Visibility.UNKNOWN");
                            put("frequency", "\"\"");
                        }}));
                put("StorageAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("accessControl", "AccessControlOption.UNKNOWN");
                            put("purposeDescription", "\"\"");
                            put("retentionTime", "\"\"");
                        }}));
                put("NetworkAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("destination", "\"\"");
                            put("encryptedInTransmission", "");
                            put("purposeDescription", "\"\"");
                            put("retentionTime", "\"\"");
                        }}));
                put("NotPersonalData",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                        }}));
                put("AdmobAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("isLocationDataEnabledInAppSettings", "");
                        }}));
            }});

    public static final Map<String, Map<String, String>> ANNOTATION_TYPE_FIELDS_INIT_VALUE_PATTERN_MAPPING =
            Collections.unmodifiableMap(new HashMap<String, Map<String,String>> () {{
                put("CallLogsAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "CallLogsPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "CallLogsDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("SMSAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "SMSPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "SMSDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("CalendarAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "CalendarPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "CalendarDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("CameraAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "CameraPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "CameraDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("ContactsAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "ContactsPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "ContactsDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("LocationAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "LocationPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "LocationDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                            put("frequency", "\" *\"");
                        }}));
                put("UniqueIdentifierAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "UIDPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("uidType", "UIDType.UNKNOWN");
                            put("scope", "UIDScope.UNKNOWN");
                            put("resettability", "UIDResettability.UNKNOWN");
                        }}));
                put("MicrophoneAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "MicrophonePurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "MicrophoneDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("SensorAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "SensorPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "SensorDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("PersonallyIdentifiableInformationAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "PersonallyIdentifiableInformationPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "PersonallyIdentifiableInformationDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("UserFileAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purpose", "UserDataPurpose\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "UserDataDataType\\.UNKNOWN");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("UserInputAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "\" *\"");
                            put("visibility", "Visibility\\.UNKNOWN");
                        }}));
                put("UndefinedPersonalDataTypeAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("ID", "\" *\"");
                            put("purposeDescription", "\" *\"");
                            put("dataType", "\" *\"");
                            put("visibility", "Visibility\\.UNKNOWN");
                            put("frequency", "\" *\"");
                        }}));
                put("StorageAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("accessControl", "AccessControlOption\\.UNKNOWN");
                            put("purposeDescription", "\" *\"");
                            put("retentionTime", "\" *\"");
                        }}));
                put("NetworkAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("destination", "\" *\"");
                            put("encryptedInTransmission", " *");
                            put("purposeDescription", "\" *\"");
                            put("retentionTime", "\" *\"");
                        }}));
                put("NotPersonalData",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                        }}));
                put("AdmobAnnotation",
                        Collections.unmodifiableMap(new HashMap<String, String>() {{
                            put("isLocationDataEnabledInAppSettings", " *");
                        }}));
            }});
    private static boolean checkAnnotationCompletenessByType(PsiAnnotation annotation, ProblemsHolder holder, ArrayList<LocalQuickFix> quickFixes) {
        String annotationTypeString = CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation).toString();
        boolean validAnnotation = true;
        Project openProject = annotation.getProject();
        Map<String, String> annotationFieldInitValuePatternMapping = ANNOTATION_TYPE_FIELDS_INIT_VALUE_PATTERN_MAPPING.get(annotationTypeString);
        PsiNameValuePair[] annotationNameValuePairs = annotation.getParameterList().getAttributes();
        if (annotationNameValuePairs.length != annotationFieldInitValuePatternMapping.size()) {
            return false;
        }
        for (PsiElement element : annotationNameValuePairs) {
            PsiNameValuePair nameValuePair = (PsiNameValuePair) element;
            if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                validAnnotation = false;
                break;
            }
            String name = nameValuePair.getName();
            if (!annotationFieldInitValuePatternMapping.containsKey(name)) {
                validAnnotation = false;
                break;
            }
            PsiElement nameIdentifier = nameValuePair.getNameIdentifier();
            PsiElement value = nameValuePair.getValue();
            boolean isCurrentFieldComplete = true;
            ArrayList<PsiElement> elementArrayList = new ArrayList<>();
            for (PsiElement psiElement: value.getChildren()) {
                if ("{".equals(psiElement.getText()) || "}".equals(psiElement.getText())) {
                    continue;
                }
                elementArrayList.add(psiElement);
            }
            if (elementArrayList.isEmpty()) {
                validAnnotation = false;
                isCurrentFieldComplete = false;
                if (holder != null) {
                    holder.registerProblem(nameIdentifier, EMPTY_FIELD, ProblemHighlightType.GENERIC_ERROR, quickFixes.toArray(new LocalQuickFix[0]));
                }
            } else {
                boolean unknownfield = false;
                for (PsiElement exp : elementArrayList) {
                    if (Pattern.matches(annotationFieldInitValuePatternMapping.get(name), exp.getText())) {
                        validAnnotation = false;
                        unknownfield = true;
                        isCurrentFieldComplete = false;
                    }
                }
                if (unknownfield) {
                    if (holder != null) {
                        holder.registerProblem(nameIdentifier, UNCOMPLETE_FIELD_VALUE, ProblemHighlightType.GENERIC_ERROR, quickFixes.toArray(new LocalQuickFix[0]));
                    }
                }
            }
            PersonalDataHolder.getInstance(openProject).setAnnotationFieldIsComplete(nameIdentifier, isCurrentFieldComplete);
        }
        return validAnnotation;
    }

    public static boolean checkAnnotationCompletenessByType(PsiAnnotation annotation, ProblemsHolder holder) {
        return checkAnnotationCompletenessByType(annotation, holder, null);
    }

    public static boolean checkAnnotationConsistency(PsiAnnotation annotation, AnnotationHolder speculation, ProblemsHolder holder, ArrayList<PersonalDataInstance> instances) {
        ArrayList<LocalQuickFix> navigationQuickfixes = new ArrayList<>();
        if (instances != null) {
            for (PersonalDataInstance instance : instances) {
                if (instance.getPsiElementPointer() != null && instance.getPsiElementPointer().getElement() != null) {
                    navigationQuickfixes.add(new NavigateToCodeQuickfix(instance.getPsiElementPointer(), instance.getPsiElementPointer().getElement().getText()));
                }
            }
        }

        boolean isAnnotationConsistent = true;
        if (speculation == null) {
            // If there is no valid speculation, then consider this annotation as consistent with the speculation.
            return true;
        }
        AnnotationHolder annotationHolder = parseAnnotation(annotation);
        for (Map.Entry<PsiElement, ArrayList<PsiElement>> pair : annotationHolder.psiElementFieldPairs.entrySet()) {
            ArrayList<LocalQuickFix> quickfixes = new ArrayList<>(navigationQuickfixes);
            String key = pair.getKey().getText();
            if (!speculation.plainValueFieldPairs.containsKey(key)) {
                // We will skip fields that don't have a speculated value
                continue;
            }
            boolean speculationUnknown = false;
            for (String value : speculation.plainValueFieldPairs.get(key)) {
                if (isIncompleteValue(value)) {
                    speculationUnknown = true;
                    break;
                }
            }
            if (speculationUnknown) {
                continue;
            }

            // Iterate through the speculated values. If the speculated values are not the same as the actual list, then report an inconsistency.
            boolean isConsistent = true;
            if (speculation.plainValueFieldPairs.size() != annotationHolder.plainValueFieldPairs.size()) {
                isConsistent = false;
            } else {
                for (String value : speculation.plainValueFieldPairs.get(key)) {
                    if (!annotationHolder.plainValueFieldPairs.get(key).contains(value)) {
                        isConsistent = false;
                    }
                }
            }
            SetInconsistentFieldValueQuickfix setInconsistentFieldValueQuickfix = new SetInconsistentFieldValueQuickfix(speculation.plainValueFieldPairs.get(key));
            quickfixes.add(setInconsistentFieldValueQuickfix);

            if (!isConsistent) {
                // TODO: (urgent) quickfixes?
                if (holder != null) {
                    if (instances != null) {
                        for (PersonalDataInstance instance : instances) {
                            if (instance.getPsiElementPointer() != null && instance.getPsiElementPointer().getElement() != null) {
                                if (annotationHolder.plainValueFieldPairs.get(key).size() == 1) {
                                    // TODO: (long-term) to support more complex combination of field values
                                    // Now we only handle this situation for UID use cases, and in a very ad-hoc way
                                    LocalQuickFix[] adaptCodeToAnnotationQuickfixes = null;
                                    if ("uidType".equals(key)) {
                                        String targetUIDType = annotationHolder.plainValueFieldPairs.get(key).get(0);
                                        HashMap<PsiNameValuePair, ArrayList<String>> InstanceIDAnnotationFieldChangeMap = new HashMap<>();
                                        HashMap<PsiNameValuePair, ArrayList<String>> UUIDAnnotationFieldChangeMap = new HashMap<>();

                                        for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                                            if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                                                continue;
                                            }
                                            if ("scope".equals(nameValuePair.getName())) {
                                                InstanceIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singletonList(String.format("UIDScope.%s", per_app_scope))));
                                                UUIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singleton("UIDScope.UNKNOWN")));

                                            } else if ("resettability".equals(nameValuePair.getName())) {
                                                InstanceIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singletonList(String.format("UIDResettability.%s", reset_when_reinstall))));
                                                UUIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singleton("UIDResettability.UNKNOWN")));
                                            } else if ("uidType".equals(nameValuePair.getName())) {
                                                InstanceIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singletonList(String.format("UIDType.%s", instance_id))));
                                                UUIDAnnotationFieldChangeMap.put(nameValuePair, new ArrayList<>(Collections.singleton(String.format("UIDType.%s", self_generated_uid))));
                                            }
                                        }
                                        if (String.format("UIDType.%s", instance_id).equals(targetUIDType)) {
                                            adaptCodeToAnnotationQuickfixes = instance.getPersonalDataAPI().getModifyFieldValueAndCodeQuickfixList(
                                                    (PsiMethodCallExpression) instance.getPsiElementPointer().getElement(),
                                                    InstanceIDAnnotationFieldChangeMap);
                                        } else if (String.format("UIDType.%s", self_generated_uid).equals(targetUIDType)) {
                                            adaptCodeToAnnotationQuickfixes = instance.getPersonalDataAPI().getModifyFieldValueAndCodeQuickfixList(
                                                    (PsiMethodCallExpression) instance.getPsiElementPointer().getElement(),
                                                    UUIDAnnotationFieldChangeMap);
                                        }
                                    } else if ("dataType".equals(key)) {
                                        adaptCodeToAnnotationQuickfixes = instance.getPersonalDataAPI().getAdaptCodeToAnnotationQuickfix(
                                                instances,
                                                key,
                                                annotationHolder.plainValueFieldPairs.get(key));
                                    } else {
                                        adaptCodeToAnnotationQuickfixes = instance.getPersonalDataAPI().getAdaptCodeToAnnotationQuickfix(
                                                (PsiMethodCallExpression) instance.getPsiElementPointer().getElement(),
                                                key,
                                                annotationHolder.plainValueFieldPairs.get(key));
                                    }
                                    if (adaptCodeToAnnotationQuickfixes != null) {
                                        Collections.addAll(quickfixes, adaptCodeToAnnotationQuickfixes);
                                    }
                                }
                            }
                        }
                    }
                    holder.registerProblem(pair.getKey(), String.format(INCONSISTENT_ANNOTATION_FIELD,
                            String.join(",", speculation.plainValueFieldPairs.get(key))), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, quickfixes.toArray(new LocalQuickFix[0]));
                }
                isAnnotationConsistent = false;
            }
        }
        return isAnnotationConsistent;
    }

    public static boolean checkAnnotationConsistency(PsiAnnotation annotation, AnnotationHolder speculation, ProblemsHolder holder) {
        return checkAnnotationConsistency(annotation, speculation, holder, null);
    }

    public static boolean isPersonalDataAnnotation(PsiAnnotation annotation) {
        CoconutAnnotationType annotationType = getAnnotationTypeFromPsiAnnotation(annotation);
        if (annotationType == null) {
            return false;
        }
        switch (annotationType) {
            case LocationAnnotation:
            case SMSAnnotation:
            case CalendarAnnotation:
            case CameraAnnotation:
            case CallLogsAnnotation:
            case ContactsAnnotation:
            case SensorAnnotation:
            case MicrophoneAnnotation:
            case PersonallyIdentifiableInformationAnnotation:
            case UniqueIdentifierAnnotation:
            case UserFileAnnotation:
            case UserInputAnnotation:
            case UndefinedPersonalDataTypeAnnotation:
                return true;
        }
        return false;
    }

    public static boolean isNotPersonalDataAnnotation(PsiAnnotation annotation) {
        CoconutAnnotationType annotationType = getAnnotationTypeFromPsiAnnotation(annotation);
        return (annotationType == CoconutAnnotationType.NotPersonalData);
    }

    //
  // Examples of checkMatchedMethodCall:
  //
  //if (checkMatchedMethodCall(expression, "java.lang.String", "android.provider.Settings.Secure.getString")) {
  //  LOG.info("String android.provider.Settings.Secure.getString is called");
  //}
  //if (checkMatchedMethodCall(expression, "double", "me.tianshili.stepcounter.MainActivity.calculateMedian")) {
  //  LOG.info("double me.tianshili.stepcounter.MainActivity.calculateMedian is called");
  //}
  //if (checkMatchedMethodCall(expression, "com.google.android.gms.ads.AdRequest", "com.google.android.gms.ads.AdRequest.Builder.build")) {
  //  LOG.info("com.google.android.gms.ads.AdRequeset com.google.android.gms.ads.AdRequest.Builder.build is called");
  //}
  //if (checkMatchedMethodCall(expression, "void", "com.google.android.gms.ads.AdView.loadAd")) {
  //  LOG.info("void com.google.android.gms.ads.AdView.loadAd is called");
  //}
    static class MethodCallStructure {
        String myReturnValueTypeCanonicalText;
        String myFullMethodName;
        PsiType [] myParameterTypeList;
        PsiExpression [] myParameterValueExpressionList;

        MethodCallStructure() {
            myReturnValueTypeCanonicalText = null;
            myFullMethodName = null;
            myParameterValueExpressionList = null;
        }
        MethodCallStructure(String returnValueTypeCanonicalText,
                            String fullMethodName,
                            PsiType [] parameterTypeCanonicalTextList,
                            PsiExpression [] parameterValueTextList) {
            myReturnValueTypeCanonicalText = returnValueTypeCanonicalText;
            myFullMethodName = fullMethodName;
            myParameterTypeList = parameterTypeCanonicalTextList;
            myParameterValueExpressionList = parameterValueTextList;
        }
        boolean isValid() {
            return myReturnValueTypeCanonicalText != null && myFullMethodName != null && myParameterValueExpressionList != null && myParameterTypeList != null;
        }
        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern) {
            if (!isValid()) {
                return false;
            }
            return Pattern.matches(returnValueTypeCanonicalTextPattern, myReturnValueTypeCanonicalText) &&
                    Pattern.matches(fullMethodNamePattern, myFullMethodName);
        }
        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern,
                                    String[] parameterTypeCanonicalTextPatternList) {
            if (!isValid()) {
                return false;
            }
            if (!isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern)) {
                return false;
            }
            // parameterTypeCanonicalTextPatternList could be a sublist of the full list
            if (parameterTypeCanonicalTextPatternList.length > myParameterTypeList.length) {
                return false;
            }
            for (int i = 0 ; i < parameterTypeCanonicalTextPatternList.length ; ++i) {
                if (myParameterTypeList[i] == null) {
                    continue;
                }
                if (!checkForParameterTypeMatch(parameterTypeCanonicalTextPatternList[i], myParameterTypeList[i])) {
                    return false;
                }
            }
            return true;
        }

        /**
         *
         *
         * @param parameterTypeCanonicalTextPattern
         * @param myParameterType
         * @return
         */
        boolean checkForParameterTypeMatch(String parameterTypeCanonicalTextPattern, PsiType myParameterType) {
            boolean match = false;
            if (Pattern.compile(parameterTypeCanonicalTextPattern, Pattern.DOTALL).matcher(myParameterType.getCanonicalText()).matches()) {
                match = true;
            } else {
                for (PsiType superType : myParameterType.getSuperTypes()) {
                    if (checkForParameterTypeMatch(parameterTypeCanonicalTextPattern, superType)) {
                        match = true;
                    }
                }
            }
            return match;
        }

        boolean isMatchedMethodCall(String returnValueTypeCanonicalTextPattern,
                                    String fullMethodNamePattern,
                                    String[] parameterValueTypePatternList,
                                    String[] parameterValueTextPatternList) {
            if (!isValid()) {
                return false;
            }
            if (!isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern, parameterValueTypePatternList)) {
                return false;
            }
            // parameterValueTextPatternList could be a sublist of the full list
            if (parameterValueTextPatternList.length > myParameterValueExpressionList.length) {
                return false;
            }
            for (int i = 0 ; i < parameterValueTextPatternList.length ; ++i) {
                Pattern p = Pattern.compile(parameterValueTextPatternList[i], Pattern.DOTALL);
                if (!p.matcher(myParameterValueExpressionList[i].getText()).matches()) {
                    return false;
                }
            }
            return true;
        }
    }

    static private MethodCallStructure parseMethodCall(PsiMethodCallExpression expression) {
        String mReturnValueTypeCanonicalText;
        String mPackageAndClassName;
        String mMethodInvocationText;
        String mFullMethodName;
        if (expression.getType() != null) {
            mReturnValueTypeCanonicalText = expression.getType().getCanonicalText();
        } else {
            return new MethodCallStructure();
        }
        // method call expression should have two children: the reference expression of the method and the expression list of the parameter list
        if (expression.getChildren().length != 2) {
            return new MethodCallStructure();
        }
        PsiElement caller = expression.getChildren()[0].getChildren()[0];
        if (caller instanceof PsiReferenceExpressionImpl) {
            PsiReferenceExpressionImpl callObject = (PsiReferenceExpressionImpl)caller;
            if (callObject.getType() != null) {
                mPackageAndClassName = callObject.getType().getCanonicalText();
            }
            else {
                // if it's static class
                assert (!callObject.getCanonicalText().equals(""));
                mPackageAndClassName = callObject.getCanonicalText();
            }
        } else if (caller instanceof PsiSuperExpressionImpl) { // e.g. super.onCreate()
            PsiSuperExpressionImpl callSuperExpression = (PsiSuperExpressionImpl)caller;
            assert (callSuperExpression.getType() != null);
            mPackageAndClassName = callSuperExpression.getType().getCanonicalText();
        } else if (caller instanceof PsiMethodCallExpressionImpl) { // called by method call return val
            PsiMethodCallExpressionImpl callMethodReturnValue = (PsiMethodCallExpressionImpl)caller;
            assert (callMethodReturnValue.getType() != null);
            mPackageAndClassName = callMethodReturnValue.getType().getCanonicalText();
        } else if (caller instanceof PsiNewExpressionImpl) { // called by new expression return val
            PsiNewExpressionImpl callNewExpression = (PsiNewExpressionImpl) caller;
            assert (callNewExpression.getType() != null);
            mPackageAndClassName = callNewExpression.getType().getCanonicalText();
        } else if (caller instanceof PsiParenthesizedExpressionImpl) {
            PsiParenthesizedExpressionImpl callParenthesizedExpression = (PsiParenthesizedExpressionImpl) caller;
            assert (callParenthesizedExpression.getType() != null);
            mPackageAndClassName = callParenthesizedExpression.getType().getCanonicalText();
        } else {
            //If the method call doesn't have an object or a static class it's being called on (such as if it's
            //defined within the class), enter these branches
            if (expression.getChildren()[0].getChildren().length == 2 &&
                    PsiTreeUtil.getParentOfType(expression, PsiClass.class) != null &&
                    PsiTreeUtil.getParentOfType(expression, PsiClass.class).getName() != null) {
                //Gets the full package and class name of the containing class (such as "com.coconuttest.tyu91.coconuttest.CameraByIntentTestActivity")
                mPackageAndClassName = ((PsiJavaFile)(PsiTreeUtil.getParentOfType(expression, PsiFile.class))).getPackageName() + "." +
                        PsiTreeUtil.getParentOfType(expression, PsiClass.class).getName();
            //If the method is called from a subfield from within the class (such as if we declare a View.OnClickListener
            //and call the method within the listener, we have to get out of the listener to find the correct package and class
            } else if (expression.getChildren()[0].getChildren().length == 2 &&
                    PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class) != null &&
                    PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class).getName() != null) {
                mPackageAndClassName = ((PsiJavaFile) (PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiFile.class))).getPackageName() + "." +
                        PsiTreeUtil.getParentOfType(PsiTreeUtil.getParentOfType(expression, PsiClass.class), PsiClass.class).getName();
            } else {
                return new MethodCallStructure();
            }
        }
        //One thing we don't handle is if the method is defined in a super class. If a method is defined in the super class
        //and then called in the subclass without using an object or static call (so like just calling startActivityForResult()
        //instead of Activity.startActivityForResult(), which is common), then we don't identify that.
        //TODO: Add in the feature described by the above comment
        mMethodInvocationText = ((PsiReferenceExpressionImpl)expression.getChildren()[0]).getCanonicalText();
        String[] mMethodInvocationTokens = mMethodInvocationText.split("\\.");
        mFullMethodName = mPackageAndClassName + "." + mMethodInvocationTokens[mMethodInvocationTokens.length - 1];
        PsiExpression[] parameterValueExpressionList = expression.getArgumentList().getExpressions();
        PsiType[] parameterTypeList = expression.getArgumentList().getExpressionTypes();
        return new MethodCallStructure(mReturnValueTypeCanonicalText, mFullMethodName,
                parameterTypeList, parameterValueExpressionList);
    }

    // also compare parameter types and values
    static public boolean checkMatchedMethodCall(PsiMethodCallExpression expression,
                                                 String returnValueTypeCanonicalTextPattern,
                                                 String fullMethodNamePattern,
                                                 String [] parameterTypeCanonicalTextPatternList,
                                                 String [] parameterValueTextPatternList) {
        MethodCallStructure methodCallStructure = parseMethodCall(expression);
        return methodCallStructure.isMatchedMethodCall(returnValueTypeCanonicalTextPattern, fullMethodNamePattern,
                parameterTypeCanonicalTextPatternList, parameterValueTextPatternList);
    }


  static public PsiAnnotation getAnnotationByType(PsiElement targetVariable, CoconutAnnotationType type) {
      PsiModifierList prevModifierList = PsiTreeUtil.getChildOfType(targetVariable, PsiModifierList.class);
      if (prevModifierList == null) {
          return null;
      }
      for (PsiAnnotation annotation : prevModifierList.getAnnotations()) {
          if (type == getAnnotationTypeFromPsiAnnotation(annotation)) {
              return annotation;
          }
      }
      return null;
  }

  static public PsiAnnotation [] getAllAnnotations(PsiElement targetVariable) {
      PsiModifierList prevModifierList = PsiTreeUtil.getChildOfType(targetVariable, PsiModifierList.class);
      if (prevModifierList == null) {
          return new PsiAnnotation[0];
      }
      return prevModifierList.getAnnotations();
  }

  static public CoconutAnnotationType getAnnotationTypeFromPsiAnnotation(PsiAnnotation annotation) {
      String typeString = annotation.getChildren()[1].getText();
      CoconutAnnotationType annotationType = null;
      try {
          annotationType = CoconutAnnotationType.valueOf(typeString);
      } catch (IllegalArgumentException ignored) {
      }
      return annotationType;
  }

    /**
     * @param annotation
     * @return
     */
  @NotNull
  static public AnnotationHolder parseAnnotation(PsiAnnotation annotation) {
      CoconutAnnotationType type = getAnnotationTypeFromPsiAnnotation(annotation);
      switch (type) {
          case UniqueIdentifierAnnotation:
              return new UniqueIdentifierAnnotationHolder(annotation);
          case LocationAnnotation:
              return new LocationAnnotationHolder(annotation);
          case PrivacyNoticeAnnotation:
              return new PrivacyNoticeAnnotationHolder(annotation);
          case AdmobAnnotation:
              return new AdmobAnnotationHolder(annotation);
          case ContactsAnnotation:
              return new ContactsAnnotationHolder(annotation);
          case CalendarAnnotation:
              return new CalendarAnnotationHolder(annotation);
          case CameraAnnotation:
              return new CameraAnnotationHolder(annotation);
          case CallLogsAnnotation:
              return new CallLogsAnnotationHolder(annotation);
          case SMSAnnotation:
              return new SMSAnnotationHolder(annotation);
          case SensorAnnotation:
              return new SensorAnnotationHolder(annotation);
          case StorageAnnotation:
              return new StorageAnnotaionHolder(annotation);
          case MicrophoneAnnotation:
              return new MicrophoneAnnotationHolder(annotation);
          case NetworkAnnotation:
              return new NetworkAnnotationHolder(annotation);
          case PersonallyIdentifiableInformationAnnotation:
              return new PersonallyIdentifiableInformationAnnotationHolder(annotation);
          case UserFileAnnotation:
              return new UserFileAnnotationHolder(annotation);
          case UserInputAnnotation:
              return new UserInputAnnotationHolder(annotation);
          case UndefinedPersonalDataTypeAnnotation:
              return new UndefinedPersonalDataTypeAnnotationHolder(annotation);
      }
      // Can't reach here
      return null;
  }

  public static boolean isIncompleteValue(String value) {
      return Pattern.matches(".*UNKNOWN", value) || Pattern.matches("\" *\"", value) || Pattern.matches(" *", value);
  }

  public static AnnotationHolder combineVirtualAnnotationHolders(ArrayList<AnnotationHolder> annotationHolders) {
      CoconutAnnotationType annotationType = null;
      for (AnnotationHolder annotationHolder : annotationHolders) {
          if (annotationType == null) {
              annotationType = annotationHolder.mAnnotationType;
          } else if (annotationHolder.mAnnotationType != annotationType) {
              return null;
          }
      }
      if (annotationType == null) {
          return null;
      }
      AnnotationHolder combinedAnnotationHolder = createEmptyAnnotationHolderByType(annotationType);
      for (AnnotationHolder annotationHolder : annotationHolders) {
          for (Map.Entry<String, ArrayList<String>> field: annotationHolder.plainValueFieldPairs.entrySet()) {
              if (combinedAnnotationHolder.containsKey(field.getKey())) {
                  for (String value : field.getValue()) {
                      if (!combinedAnnotationHolder.containsPair(field.getKey(), value)) {
                          combinedAnnotationHolder.add(field.getKey(), value);
                      }
                  }
              } else {
                  combinedAnnotationHolder.add(field.getKey(), field.getValue());
              }
          }
      }
      // remove unknown or empty value if there exists other values
      for (Map.Entry<String, ArrayList<String>> field : combinedAnnotationHolder.plainValueFieldPairs.entrySet()) {
          ArrayList<String> values = field.getValue();
          if (values != null && values.size() > 1) {
              boolean containUnemptyValue = false;
              for (String value : values) {
                  if (!isIncompleteValue(value)) {
                      containUnemptyValue = true;
                      break;
                  }
              }
              if (containUnemptyValue) {
                  values.removeIf(CodeInspectionUtil::isIncompleteValue);
              }
          }
      }
      return combinedAnnotationHolder;
  }

    /**
     * Creates an empty annotation holder based on the given parameters
     *
     * @param type The type of annotation a holder is needed for
     * @return An empty annotation holder for the given type
     */
    @NotNull
    public static AnnotationHolder createEmptyAnnotationHolderByType(CoconutAnnotationType type) {
        switch (type) {
            case UniqueIdentifierAnnotation:
                return new UniqueIdentifierAnnotationHolder();
            case LocationAnnotation:
                return new LocationAnnotationHolder();
            case PrivacyNoticeAnnotation:
                return new PrivacyNoticeAnnotationHolder();
            case AdmobAnnotation:
                return new AdmobAnnotationHolder();
            case ContactsAnnotation:
                return new ContactsAnnotationHolder();
            case CalendarAnnotation:
                return new CalendarAnnotationHolder();
            case CallLogsAnnotation:
                return new CallLogsAnnotationHolder();
            case CameraAnnotation:
                return new CameraAnnotationHolder();
            case SMSAnnotation:
                return new SMSAnnotationHolder();
            case SensorAnnotation:
                return new SensorAnnotationHolder();
            case StorageAnnotation:
                return new StorageAnnotaionHolder();
            case MicrophoneAnnotation:
                return new MicrophoneAnnotationHolder();
            case NetworkAnnotation:
                return new NetworkAnnotationHolder();
            case NotPersonalData:
                return new NotPersonalDataAnnotationHolder();
            case PersonallyIdentifiableInformationAnnotation:
                return new PersonallyIdentifiableInformationAnnotationHolder();
            case UserFileAnnotation:
                return new UserFileAnnotationHolder();
            case UserInputAnnotation:
                return new UserInputAnnotationHolder();
            case UndefinedPersonalDataTypeAnnotation:
                return new UndefinedPersonalDataTypeAnnotationHolder();
        }
        // Can't reach here
        return null;
    }

    /**
     * @param type A given annotation type
     * @return true if the given annotation type is a personal data source
     */
    public static boolean isPersonalDataSourceAPIAnnotationType(CoconutAnnotationType type) {
        switch (type) {
            case UniqueIdentifierAnnotation:
            case LocationAnnotation:
            case ContactsAnnotation:
            case CalendarAnnotation:
            case CallLogsAnnotation:
            case CameraAnnotation:
            case SMSAnnotation:
            case SensorAnnotation:
            case MicrophoneAnnotation:
            case PersonallyIdentifiableInformationAnnotation:
            case UserFileAnnotation:
            case UserInputAnnotation:
            case UndefinedPersonalDataTypeAnnotation:
                return true;
            //ALL annotations above this are personal data source API annotations. All below are not.
            case StorageAnnotation:
            case NetworkAnnotation:
            case NotPersonalData:
            case PrivacyNoticeAnnotation:
            case AdmobAnnotation:
                return false;
            default:
                return false;
        }
    }

    public static ArrayList<PsiElement> getGlobalAndLocalRefExpsBeforeMethodExp(PsiReferenceExpression expression,
                                                                                PsiMethodCallExpression targetMethodExpression) {
        PsiElement expressionDeclaration = expression.resolve();
        if (expressionDeclaration == null) {
            return new ArrayList<>();
        }
        ArrayList<PsiElement> matchedReferenceExpressions = new ArrayList<>();
        matchedReferenceExpressions.add(expressionDeclaration);
        Collection<PsiReference> allReferences = ReferencesSearch.search(expressionDeclaration).findAll();
        for (PsiReference reference : allReferences) {
            if (PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class) != null) {
                PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(reference.getElement(), PsiMethodCallExpression.class);
                // Only consider set expressions before the location API call that uses this criteria
                if (targetMethodExpression.equals(methodCallExpression)) {
                    break;
                }
            }
            matchedReferenceExpressions.add(reference.getElement());
        }
        return matchedReferenceExpressions;
    }
}
