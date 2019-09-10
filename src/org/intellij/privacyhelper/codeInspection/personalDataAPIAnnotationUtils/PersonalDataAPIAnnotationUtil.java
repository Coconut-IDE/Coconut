package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by tianshi on 11/14/17.
 */
public abstract class PersonalDataAPIAnnotationUtil {
    protected PersonalDataAPI api;

    /**
     * Ideally this should be constructed based on the code analysis result
     *
     * @param source
     * @return an AnnotationHolder object which contains field-value pairs that can be inferred by code analysis.
     */
    // TODO: (urgent) change this to get Annotation by type
    public abstract AnnotationHolder createAnnotationInferenceFromSource(PsiElement source);

    public abstract AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source);

    public void setAPI(PersonalDataAPI api) {
        this.api = api;
    }

    @Nullable
    public abstract LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue);

    @Nullable
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(ArrayList<PersonalDataInstance> instances, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    public abstract LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue);

    @Nullable
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(ArrayList<PersonalDataInstance> instances, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        // TODO: temporary overload
        return null;
    }

    @Nullable
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, HashMap<PsiNameValuePair, ArrayList<String>> annotationFieldChangeMap) {
        // TODO: temporary overload
        return null;
    }
}
