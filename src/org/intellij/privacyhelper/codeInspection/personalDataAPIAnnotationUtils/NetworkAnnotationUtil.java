package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by tianshi on 2/3/18.
 */
public class NetworkAnnotationUtil extends PersonalDataAPIAnnotationUtil {

    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.NetworkAnnotation);
        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix [] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return null;
    }
}
