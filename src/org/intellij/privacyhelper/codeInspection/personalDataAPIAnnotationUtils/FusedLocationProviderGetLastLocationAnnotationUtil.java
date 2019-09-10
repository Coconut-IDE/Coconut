package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 11/14/17.
 */
public class FusedLocationProviderGetLastLocationAnnotationUtil extends PersonalDataAPIAnnotationUtil {

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
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, fieldValue);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        // FIXME: temporary workaround
        return LocationUtils.getChangePermissionQuickfixes((PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement(), fieldValue);
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, nameValuePair, new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude))));
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        // FIXME: this is just a temporary hotfix
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) instances.get(0).getPsiElementPointer().getElement();
        return LocationUtils.getChangePermissionQuickfixes(methodCallExpression, nameValuePair, new ArrayList<>(Arrays.asList(String.format("LocationDataType.%s", coarse_grained_latitude_longitude))));
    }

}
