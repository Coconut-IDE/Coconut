package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class CreateEmptyAnnotationByTypeUtil extends PersonalDataAPIAnnotationUtil {
    CoconutAnnotationType annotationType;

    CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(annotationType);
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return new LocalQuickFix[0];
    }
}
