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
public class LocationManagerRequestSingleUpdate1AnnotationUtil extends PersonalDataAPIAnnotationUtil {

    // requestSingleUpdate(String provider, ...)
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.LocationAnnotation);
        if (!(source instanceof PsiMethodCallExpression)) {
            source = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        }
        PsiExpression provider = ((PsiMethodCallExpression)source).getArgumentList().getExpressions()[0];
        LocationUtils.analyzeProvider(provider, annotationHolder);
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
        String value = fieldValue.get(0);
        if (String.format("LocationDataType.%s", coarse_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use NETWORK_PROVIDER (>100m accuracy)", methodCallExpression,
                    (callExpression) -> LocationUtils.providerBasedChangeCodeFunction(0, "LocationManager.NETWORK_PROVIDER", callExpression)));
        } else if (String.format("LocationDataType.%s", fine_grained_latitude_longitude).equals(value)) {
            localQuickFixes.add(new AdaptCodeToAnnotationQuickfix("(Modify code) use GPS_PROVIDER (~10m accuracy)", methodCallExpression,
                    (callExpression) -> LocationUtils.providerBasedChangeCodeFunction(0, "LocationManager.GPS_PROVIDER", callExpression)));
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
        localQuickFixes.add(new ModifyFieldValueAndCodeQuickfix("(Modify code) use NETWORK_PROVIDER (>100m accuracy)", methodCallExpression, nameValuePair, fieldValue,
                (callExpression)->LocationUtils.providerBasedChangeCodeFunction(0, "LocationManager.NETWORK_PROVIDER", callExpression)));
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
