package org.intellij.privacyhelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutUIUtil;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by tianshi on 12/27/17.
 */
public class AddPreFilledAnnotationByTypeQuickfix implements LocalQuickFix {
    private String ADD_LOCATION_USAGE_ANNOTATION_QUICKFIX;
    private PersonalDataAPI api;
    private CoconutAnnotationType annotationType;

    public AddPreFilledAnnotationByTypeQuickfix(PersonalDataAPI api) {
        this.api = api;
        this.annotationType = api.getAnnotationType();
        this.ADD_LOCATION_USAGE_ANNOTATION_QUICKFIX = String.format("Add %s annotation", annotationType.toString());
    }

    public AddPreFilledAnnotationByTypeQuickfix(PersonalDataAPI api, CoconutAnnotationType annotationType) {
        this.api = api;
        this.annotationType = annotationType;
        this.ADD_LOCATION_USAGE_ANNOTATION_QUICKFIX = String.format("Add %s annotation", annotationType.toString());
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return ADD_LOCATION_USAGE_ANNOTATION_QUICKFIX;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "personal data API inspection quickfixes";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiElement element = problemDescriptor.getPsiElement();
        if (! (element instanceof PsiMethodCallExpression)) {
            element = PsiTreeUtil.getParentOfType(element, PsiMethodCallExpression.class);
        }
        if (element == null) {
            return;
        }
        PsiElement dataEntity = api.getResolvedTargetVariable(element);
        PsiModifierList modifierList = PsiTreeUtil.getChildOfType(dataEntity, PsiModifierList.class);
        if (annotationType == CoconutAnnotationType.NotPersonalData) {
            if (modifierList != null) {
                modifierList.addAnnotation(CoconutAnnotationType.NotPersonalData.toString());
            }
        } else {
            AnnotationHolder emptyAnnotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(annotationType);
            PsiElement targetVariable = api.getTargetVariable(element);
            if (targetVariable == null) {
                return;
            }
            ArrayList<AnnotationHolder> annotationHolders = new ArrayList<>();
            annotationHolders.add(emptyAnnotationHolder);
            for (PersonalDataInstance instance : PersonalDataHolder.getInstance(project).getAllPersonalDataInstances()) {
                PsiElement myTargetVariable = instance.getPersonalDataAPI().getTargetVariable(instance.getPsiElementPointer().getElement());
                if (targetVariable.equals(myTargetVariable)) {
                    if (instance.getAnnotationMetadataByType(annotationType) == null) {
                        continue;
                    }
                    AnnotationHolder speculationAnnotationHolder = instance.getAnnotationMetadataByType(annotationType).annotationSpeculation;
                    if (speculationAnnotationHolder != null) {
                        annotationHolders.add(speculationAnnotationHolder);
                    }
                }
            }
            String mergedAnnotation;
            mergedAnnotation = CodeInspectionUtil.combineVirtualAnnotationHolders(annotationHolders).getAnnotationString();
            // TODO: (double-check) figure out when this will be null
            if (modifierList != null) {
                modifierList.addAnnotation(mergedAnnotation);
                modifierList.addAfter(CoconutUIUtil.nl(element), modifierList.getAnnotations()[0]);
            }
        }
        CoconutUIUtil.navigateMainEditorToPsiElement(modifierList);
    }
}
