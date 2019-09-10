package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.ModifyFieldValueAndCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.coarse_grained_latitude_longitude;
import static org.intellij.privacyhelper.codeInspection.utils.Constants.fine_grained_latitude_longitude;

/**
 * Created by tianshi on 1/21/18.
 */
public class LocationManagerRequestLocationUpdate2AnnotationUtil extends PersonalDataAPIAnnotationUtil {

    // requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.LocationAnnotation);
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        PsiExpression minTime = sourceMethodCallExp.getArgumentList().getExpressions()[0];
        PsiExpression minDistance = sourceMethodCallExp.getArgumentList().getExpressions()[1];
        PsiExpression criteria = sourceMethodCallExp.getArgumentList().getExpressions()[2];
        LocationUtils.analyzeCriteria(sourceMethodCallExp, criteria, annotationHolder);
        LocationUtils.analyzeFrequency(minTime, minDistance, annotationHolder);
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
        PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[2];
        if (criteria == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String criteriaName = criteria.getText();
        if (criteriaName == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
            String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use ACCURACY_COARSE (>100m accuracy)", methodCallExpression,
                    (callExpression)-> LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_COARSE);\n", callExpression)));
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use ACCURACY_MEDIUM (100m~500m accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);\n", callExpression)));
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use ACCURACY_LOW (>500m accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setHorizontalAccuracy(Criteria.ACCURACY_LOW);\n", callExpression)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use ACCURACY_FINE (~10m accuracy)", methodCallExpression,
                    (callExpression)->LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_FINE);\n", callExpression)));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        // FIXME: this is just a temporary hotfix
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use coarse location (>100m accuracy)", methodCallExpression,
                    (callExpression) -> LocationUtils.combineMultipleRequestLocationUpdateChangeCodeFunction(instances, value)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use fine location (~10m accuracy)", methodCallExpression,
                    (callExpression) -> LocationUtils.combineMultipleRequestLocationUpdateChangeCodeFunction(instances, value)));
        }
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        PsiExpression criteria = methodCallExpression.getArgumentList().getExpressions()[2];
        if (criteria == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        String criteriaName = criteria.getText();
        if (criteriaName == null) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use ACCURACY_COARSE (>100m accuracy)", methodCallExpression, nameValuePair, fieldValue,
                (callExpression)-> LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setAccuracy(Criteria.ACCURACY_COARSE);\n", callExpression)));
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use ACCURACY_MEDIUM (100m~500m accuracy)", methodCallExpression, nameValuePair, fieldValue,
                (callExpression)->LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setHorizontalAccuracy(Criteria.ACCURACY_MEDIUM);\n", callExpression)));
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use ACCURACY_LOW (>500m accuracy)", methodCallExpression, nameValuePair, fieldValue,
                (callExpression)->LocationUtils.criteriaBasedChangeCodeFunction(criteriaName, "%s.setHorizontalAccuracy(Criteria.ACCURACY_LOW);\n", callExpression)));
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        ArrayList<LocalQuickFix> localQuickFixes = new ArrayList<>();
        // FIXME: this is just a temporary hotfix
        if (fieldValue.size() == 0) {
            return localQuickFixes.toArray(new LocalQuickFix[0]);
        }

        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use coarse location (~100m accuracy)", methodCallExpression, nameValuePair,
                new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude))),
                (callExpression)-> LocationUtils.combineMultipleRequestLocationUpdateChangeCodeFunction(instances, String.format("LocationDataType.%s", coarse_grained_latitude_longitude))));
        return localQuickFixes.toArray(new LocalQuickFix[0]);
    }

}
