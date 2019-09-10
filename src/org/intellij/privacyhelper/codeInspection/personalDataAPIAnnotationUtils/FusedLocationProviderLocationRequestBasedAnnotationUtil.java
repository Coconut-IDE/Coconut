package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.ModifyFieldValueAndCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 1/21/18.
 */
public class FusedLocationProviderLocationRequestBasedAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    private int locationRequestParameterPosition;

    public FusedLocationProviderLocationRequestBasedAnnotationUtil(int locationRequestParameterPosition) {
        this.locationRequestParameterPosition = locationRequestParameterPosition;
    }

    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        Project openProject = source.getProject();
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.LocationAnnotation);
        boolean finegrained = false;
        boolean coarsegrained = false;
        if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_COARSE_LOCATION)) {
            coarsegrained = true;
        }
        if (PersonalDataHolder.getInstance(openProject).hasPermissionDeclared(AndroidPermission.ACCESS_FINE_LOCATION)) {
            finegrained = true;
            coarsegrained = true;
        }
        if (finegrained || coarsegrained) {
            annotationHolder.remove("dataType");
        }
        if (finegrained) {
            annotationHolder.add("dataType", String.format("LocationDataType.%s", fine_grained_latitude_longitude));
        } else if (coarsegrained) {
            annotationHolder.add("dataType", String.format("LocationDataType.%s", coarse_grained_latitude_longitude));
        }
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        if (sourceMethodCallExp != null && sourceMethodCallExp.getArgumentList().getExpressions().length > locationRequestParameterPosition) {
            PsiExpression locationRequest = sourceMethodCallExp.getArgumentList().getExpressions()[locationRequestParameterPosition];
            LocationUtils.analyzeLocationRequest(sourceMethodCallExp, locationRequest, annotationHolder);
        }
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        if (methodCallExpression.getArgumentList().getExpressions().length <= locationRequestParameterPosition) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        PsiExpression locationRequest = methodCallExpression.getArgumentList().getExpressions()[locationRequestParameterPosition];
        if (locationRequest == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String locationRequestName = locationRequest.getText();
        if (locationRequestName == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", block_level_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use block-level location (~100m accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, "%s.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);\n", callExpression)));
        } else if (String.format("LocationDataType.%s", city_level_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use city-level location (~10km accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, "%s.setPriority(LocationRequest.PRIORITY_LOW_POWER);\n", callExpression)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use PRIORITY_HIGH_ACCURACY (~10m accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, "%s.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);\n", callExpression)));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        // FIXME: this is just a temporary hotfix
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();

        if (String.format("LocationDataType.%s", block_level_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use block-level location (~100m accuracy)", methodCallExpression,
                    (callExpression)->combineMultipleLocationRequestBasedChangeCodeFunction(instances, "%s.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);\n")));
        } else if (String.format("LocationDataType.%s", city_level_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use city-level location (~10km accuracy)", methodCallExpression,
                    (callExpression)->combineMultipleLocationRequestBasedChangeCodeFunction(instances, "%s.setPriority(LocationRequest.PRIORITY_LOW_POWER);\n")));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use PRIORITY_HIGH_ACCURACY (~10m accuracy)", methodCallExpression,
                    (callExpression)->combineMultipleLocationRequestBasedChangeCodeFunction(instances, "%s.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);\n")));
        }

        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= locationRequestParameterPosition) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        PsiExpression locationRequest = methodCallExpression.getArgumentList().getExpressions()[locationRequestParameterPosition];
        if (locationRequest == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String locationRequestName = locationRequest.getText();
        if (locationRequestName == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use city-level location (~10km accuracy)", methodCallExpression, nameValuePair,
                new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", city_level_latitude_longitude))),
                (callExpression)->LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, "%s.setPriority(LocationRequest.PRIORITY_LOW_POWER);\n", callExpression)));
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use block-level location (~100m accuracy)", methodCallExpression, nameValuePair,
                new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", block_level_latitude_longitude))),
                (callExpression)->LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, "%s.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);\n", callExpression)));
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    // FIXME: This is just a temporary hotfix
    private void combineMultipleLocationRequestBasedChangeCodeFunction(ArrayList<PersonalDataInstance> instances, String statementPattern) {
        for (PersonalDataInstance instance : instances) {
            if (!(instance.getPsiElementPointer().getElement() instanceof PsiMethodCallExpression)) {
                continue;
            }
            PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instance.getPsiElementPointer().getElement();
            if (methodCallExpression == null || methodCallExpression.getArgumentList().getExpressions().length <= locationRequestParameterPosition) {
                continue;
            }
            PsiExpression locationRequest = methodCallExpression.getArgumentList().getExpressions()[locationRequestParameterPosition];
            if (locationRequest == null) {
                continue;
            }
            String locationRequestName = locationRequest.getText();
            if (locationRequestName == null) {
                continue;
            }
            LocationUtils.locationRequestBasedChangeCodeFunction(locationRequestName, statementPattern, methodCallExpression);
        }
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        // FIXME: this is just a temporary hotfix
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use city-level location (~10km accuracy)", methodCallExpression, nameValuePair,
                new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", city_level_latitude_longitude))),
                (callExpression)->combineMultipleLocationRequestBasedChangeCodeFunction(instances, "%s.setPriority(LocationRequest.PRIORITY_LOW_POWER);\n")));
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use block-level location (~100m accuracy)", methodCallExpression, nameValuePair,
                new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", block_level_latitude_longitude))),
                (callExpression)->combineMultipleLocationRequestBasedChangeCodeFunction(instances, "%s.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);\n")));
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }
}
