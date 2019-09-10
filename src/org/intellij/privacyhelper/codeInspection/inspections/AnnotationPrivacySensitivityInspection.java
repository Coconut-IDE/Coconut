package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.Constants;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 11/28/17.
 */
public class AnnotationPrivacySensitivityInspection extends BaseJavaLocalInspectionTool {

    @NotNull
    @Override
    public String getShortName() { return "AnnotationPrivacySensitivityInspection"; }

    // TODO: (urgent) Additionally, your app's privacy policy may need to be updated to reflect the use of personalized advertising (formerly known as interest-based advertising) served via the Google Mobile Ads SDK.
    // TODO: (urgent) How to display the code part in the warning explanation using a different font or just italicizing it?

    private void checkField(PsiNameValuePair nameValuePair, ProblemsHolder holder,
                            String[] recommendedPatterns, String[] recommendedValueDescription,
                            PsiExpression[] expressionList, String purpose, ArrayList<LocalQuickFix> changeUniqueIDquickFixes,
                            String warning_message_pattern) {
        if (expressionList.length == 1 && Pattern.compile(".*UNKNOWN", Pattern.DOTALL).matcher(expressionList[0].getText()).matches()) { // If the value is unknown, it will be handled by completeness checking (in AnnotationCompletenessInspection)
            return;
        }
        if (expressionList.length > 0 && recommendedPatterns.length > 0) {
            for (PsiExpression exp : expressionList) {
                boolean match = false;
                for (String recommendedPattern : recommendedPatterns) {
                    if (Pattern.compile(recommendedPattern, Pattern.DOTALL).matcher(exp.getText()).matches()) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    holder.registerProblem(exp, String.format(warning_message_pattern,
                            purpose, String.join(",", recommendedValueDescription)),
                            ProblemHighlightType.GENERIC_ERROR_OR_WARNING, changeUniqueIDquickFixes.toArray(new LocalQuickFix[0]));
                }
            }
        } else if (expressionList.length == 0 && nameValuePair.getNameIdentifier() != null) {
            holder.registerProblem(nameValuePair.getNameIdentifier(), String.format(warning_message_pattern,
                    purpose, String.join(",", recommendedValueDescription)),
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING,  changeUniqueIDquickFixes.toArray(new LocalQuickFix[0]));
        } else if (recommendedPatterns.length == 0 && nameValuePair.getNameIdentifier() != null) {
            holder.registerProblem(nameValuePair.getNameIdentifier(), "This purpose is not recommended to be achieved by using unique identifiers.",
                    ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
        }
    }

    String[] transformToRegexPattern(String[] recommendedList) {
        ArrayList<String> recommendedFieldValueArrayList = new ArrayList<>();
        for (String recommended : recommendedList) {
            recommendedFieldValueArrayList.add(String.format(".*%s", recommended));
        }
        return recommendedFieldValueArrayList.toArray(new String[0]);
    }

    private void recommendUIDForPurpose(PersonalDataInstance instance, ArrayList<PsiNameValuePair> nameValuePairs, ProblemsHolder holder,
                                        String purpose,
                                        String[] recommendedScopes,
                                        String[] recommendedResettabilities,
                                        String[] recommendedUIDTypes, String[] recommendedUIDDescriptions) {
        //ArrayList<String> recommendedScopeValuesArrayList = new ArrayList<>();
        HashMap<PsiNameValuePair, ArrayList<String>> InstanceIDAnnotationFieldChangeMap = new HashMap<>();
        HashMap<PsiNameValuePair, ArrayList<String>> UUIDAnnotationFieldChangeMap = new HashMap<>();
        for (PsiNameValuePair nameValuePair : nameValuePairs) {
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
        ArrayList<LocalQuickFix> changeUniqueIDquickFixes = new ArrayList<>();
        LocalQuickFix[] InstanceIDQuickfixList = instance.getPersonalDataAPI().getModifyFieldValueAndCodeQuickfixList(
                (PsiMethodCallExpression) instance.getPsiElementPointer().getElement(),
                InstanceIDAnnotationFieldChangeMap);
        LocalQuickFix[] UUIDQuickfixList = instance.getPersonalDataAPI().getModifyFieldValueAndCodeQuickfixList(
                (PsiMethodCallExpression) instance.getPsiElementPointer().getElement(),
                UUIDAnnotationFieldChangeMap);

        if (InstanceIDQuickfixList != null) {
            Collections.addAll(changeUniqueIDquickFixes, InstanceIDQuickfixList);
        }
        if (UUIDQuickfixList != null) {
            Collections.addAll(changeUniqueIDquickFixes, UUIDQuickfixList);
        }
        for (PsiNameValuePair nameValuePair : nameValuePairs) {
            PsiExpression[] expressionList = PsiTreeUtil.getChildrenOfType(nameValuePair.getValue(), PsiExpression.class);
            if ("scope".equals(nameValuePair.getName())) {
                checkField(nameValuePair, holder,
                        transformToRegexPattern(recommendedScopes),
                        recommendedScopes, expressionList, purpose, changeUniqueIDquickFixes, unique_id_scope_privacy_warning_message_pattern);
            } else if ("resettability".equals(nameValuePair.getName())) {
                checkField(nameValuePair, holder,
                        transformToRegexPattern(recommendedResettabilities),
                        recommendedResettabilities, expressionList, purpose, changeUniqueIDquickFixes, unique_id_resettability_privacy_warning_message_pattern);
            } else if ("uidType".equals(nameValuePair.getName())) {
                checkField(nameValuePair, holder,
                        transformToRegexPattern(recommendedUIDTypes),
                        recommendedUIDDescriptions, expressionList, purpose, changeUniqueIDquickFixes, unique_id_type_privacy_warning_message_pattern);
            }
        }
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                SmartPsiElementPointer newAnnotationSmartPointer =
                        SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation);
                ArrayList<PersonalDataInstance> instances = new ArrayList<>();
                Project openProject = newAnnotationSmartPointer.getProject();
                for (PersonalDataInstance instance : PersonalDataHolder.getInstance(openProject).getAllPersonalDataInstances()) {
                    for (AnnotationMetaData metaData : instance.annotationMetaDataList) {
                        if (newAnnotationSmartPointer.equals(metaData.psiAnnotationPointer)) {
                            instances.add(instance);
                            boolean isUID = (metaData.getAnnotationType() == CoconutAnnotationType.UniqueIdentifierAnnotation);
                            ArrayList<String> purposeList = new ArrayList<>();

                            if (isUID) {
                                ArrayList<PsiNameValuePair> nameValuePairs = new ArrayList<>();
                                for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                                    if (nameValuePair == null) {
                                        continue;
                                    }
                                    if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                                        continue;
                                    }
                                    PsiExpression[] expressionList = PsiTreeUtil.getChildrenOfType(nameValuePair.getValue(), PsiExpression.class);
                                    if (expressionList == null) {
                                        continue;
                                    }
                                    if ("scope".equals(nameValuePair.getName()) || "resettability".equals(nameValuePair.getName()) || "uidType".equals(nameValuePair.getName())) {
                                        nameValuePairs.add(nameValuePair);
                                    }
                                    if ("purpose".equals(nameValuePair.getName())) {
                                        for (PsiExpression exp : expressionList) {
                                            purposeList.add(exp.getText());
                                        }
                                    }
                                }
                                for (String purpose : purposeList) {
                                    if (Pattern.compile(String.format(".*%s", purpose_uid_tracking_user_data_collected_from_multiple_apps_on_this_device), Pattern.DOTALL).matcher(purpose).matches()) {
                                        recommendUIDForPurpose(instance, nameValuePairs, holder, purpose,
                                                new String[]{per_app_group_scope, per_device_scope},
                                                new String[]{user_resettable_in_system_settings, reset_when_reinstall},
                                                new String[]{advertising_id},
                                                new String[]{advertising_id_description}
                                        );
                                    } else if (Pattern.compile(String.format(".*%s", purpose_uid_managing_telephony_and_carrier_functionality), Pattern.DOTALL).matcher(purpose).matches()) {
                                        recommendUIDForPurpose(instance, nameValuePairs, holder, purpose,
                                                new String[]{per_device_scope},
                                                new String[]{persist_after_factory_reset},
                                                new String[]{telephony_device_id, line1_phone_number},
                                                new String[]{telephony_device_id_description, line1_phone_number_description});
                                    } else if (Pattern.compile(String.format(".*%s", purpose_uid_identifying_bots_and_DDOS_attacks), Pattern.DOTALL).matcher(purpose).matches()) {
                                        recommendUIDForPurpose(instance, nameValuePairs, holder, purpose,
                                                new String[]{},
                                                new String[]{},
                                                new String[]{}, new String[]{safetynet_description});
                                    } else if (Pattern.compile(String.format(".*%s", purpose_uid_detecting_high_value_stolen_credentials), Pattern.DOTALL).matcher(purpose).matches()) {
                                        recommendUIDForPurpose(instance, nameValuePairs, holder, purpose,
                                                new String[]{per_device_scope},
                                                new String[]{persist_after_factory_reset},
                                                new String[]{telephony_device_id},
                                                new String[]{telephony_device_id_description});
                                    } else if (!Pattern.compile(".*UNKNOWN", Pattern.DOTALL).matcher(purpose).matches()) {
                                        recommendUIDForPurpose(instance, nameValuePairs, holder, purpose,
                                                new String[]{per_app_scope},
                                                new String[]{user_resettable_in_system_settings, reset_when_reinstall},
                                                new String[]{instance_id, self_generated_uid},
                                                new String[]{instance_id_description, self_generated_uid_description});
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
                if (instances.isEmpty()) {
                    return;
                }
                for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                    if (nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                        continue;
                    }
                    PsiExpression[] expressionList = PsiTreeUtil.getChildrenOfType(nameValuePair.getValue(), PsiExpression.class);
                    if (expressionList != null) {
                        for (PsiExpression exp : expressionList) {
                            if (exp.getText() == null) {
                                continue;
                            }
                            // TODO: (urgent) add quickfixes that are corresponding to the source API to mitigate these sensitive data access
                            if (Constants.SENSITIVE_FIELD_VALUES.containsKey(exp.getText())) {
                                String sensitive_field_value = exp.getText();
                                if ("Visibility.IN_BACKGROUND".equals(sensitive_field_value)) {
                                    holder.registerProblem(exp, Constants.SENSITIVE_FIELD_VALUES.get(sensitive_field_value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
                                } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(sensitive_field_value)) {
                                    holder.registerProblem(exp, Constants.SENSITIVE_FIELD_VALUES.get(sensitive_field_value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            instances.get(0).getPersonalDataAPI().getModifyFieldValueAndCodeQuickfixList(
                                                    instances,
                                                    nameValuePair,
                                                    new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude)))));
                                } else if (String.format("LocationPurpose.%s", purpose_location_data_collection_for_analytics).equals(sensitive_field_value)) {
                                    holder.registerProblem(exp, Constants.SENSITIVE_FIELD_VALUES.get(sensitive_field_value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            null);
                                } else if (String.format("LocationPurpose.%s", purpose_location_data_collection_for_advertising).equals(sensitive_field_value)) {
                                    holder.registerProblem(exp, Constants.SENSITIVE_FIELD_VALUES.get(sensitive_field_value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            null);
                                } else if (String.format("LocationPurpose.%s", purpose_location_recording).equals(sensitive_field_value)) {
                                    holder.registerProblem(exp, Constants.SENSITIVE_FIELD_VALUES.get(sensitive_field_value), ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            null);
                                }
                            }
                        }
                    }
                }

            }
        };
    }
}
