package org.intellij.privacyhelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 3/16/18.
 */
public class PasteExistingAnnotationQuickfix implements LocalQuickFix {
    private String PASTE_EXISTING_ANNOTATION_QUICKFIX_NAME;
    private AnnotationHolder annotationHolder;
    private PersonalDataAPI api;

    public PasteExistingAnnotationQuickfix(AnnotationHolder annotationHolder, PersonalDataAPI api) {
        String purpose = annotationHolder.getPurpose();
        String annotationType = annotationHolder.mAnnotationType.toString();
        PASTE_EXISTING_ANNOTATION_QUICKFIX_NAME = String.format("paste the @%s for %s", annotationType, purpose);
        this.annotationHolder = annotationHolder;
        this.api = api;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return PASTE_EXISTING_ANNOTATION_QUICKFIX_NAME;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "personal data API inspection quickfixes";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        if (annotationHolder == null) {
            return;
        }
        PsiElement element = problemDescriptor.getPsiElement();
        if (! (element instanceof PsiMethodCallExpression)) {
            element = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        }
        if (element == null) {
            return;
        }
        PsiElement dataEntity = api.getResolvedTargetVariable(element);
        PsiModifierList modifierList = PsiTreeUtil.getChildOfType(dataEntity, PsiModifierList.class);
        if (modifierList != null) {
            modifierList.addAnnotation(annotationHolder.getAnnotationString());
            modifierList.addAfter(CoconutUIUtil.nl(element), modifierList.getAnnotations()[0]);
        }
        CoconutUIUtil.navigateMainEditorToPsiElement(modifierList);
    }
}
