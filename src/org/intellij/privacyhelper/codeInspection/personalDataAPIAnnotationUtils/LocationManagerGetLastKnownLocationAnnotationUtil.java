package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.AdaptCodeToAnnotationQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.ModifyFieldValueAndCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 11/14/17.
 */
public class LocationManagerGetLastKnownLocationAnnotationUtil extends PersonalDataAPIAnnotationUtil {

    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.LocationAnnotation);
        if (((PsiMethodCallExpression)source).getArgumentList().getExpressions().length > 0) {
            PsiExpression provider = ((PsiMethodCallExpression) source).getArgumentList().getExpressions()[0];
            LocationUtils.analyzeProvider(provider, annotationHolder);
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
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        return getAdaptCodeToAnnotationQuickfix(methodCallExpression, fieldName, fieldValue);
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
        // FIXME: this is just a temporary hotfix
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        return getModifyFieldValueAndCodeQuickfixList(methodCallExpression, nameValuePair, fieldValue);
    }

}
