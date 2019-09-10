package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.Constants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 1/18/18.
 */
public class AnnotationTooltipInspection extends BaseJavaLocalInspectionTool {
    @NotNull
    @Override
    public String getShortName() { return "AnnotationTooltipInspection"; }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                SmartPsiElementPointer newAnnotationSmartPointer =
                        SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation);
                Project openProject = newAnnotationSmartPointer.getProject();
                for (PersonalDataInstance instance : PersonalDataHolder.getInstance(openProject).getAllPersonalDataInstances()) {
                    for (AnnotationMetaData metaData : instance.annotationMetaDataList) {
                        if (newAnnotationSmartPointer.equals(metaData.psiAnnotationPointer)) {
                            for (PsiNameValuePair nameValuePair : annotation.getParameterList().getAttributes()) {
                                if (nameValuePair == null || nameValuePair.getNameIdentifier() == null || nameValuePair.getName() == null || nameValuePair.getValue() == null) {
                                    continue;
                                }
                                PsiExpression[] expressionList = PsiTreeUtil.getChildrenOfType(nameValuePair.getValue(), PsiExpression.class);
                                if (expressionList != null) {
                                    for (PsiExpression exp : expressionList) {
                                        if (Constants.DESCRIPTION_MAPPING.containsKey(exp.getText())) {
                                            holder.registerProblem(exp, Constants.DESCRIPTION_MAPPING.get(exp.getText()), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
                                        }
                                    }
                                }
                                if (Constants.DESCRIPTION_MAPPING.containsKey(nameValuePair.getName())) {
                                    if (PersonalDataHolder.getInstance(openProject).getAnnotationFieldIsComplete(nameValuePair.getNameIdentifier())) {
                                        holder.registerProblem(nameValuePair.getNameIdentifier(), Constants.DESCRIPTION_MAPPING.get(nameValuePair.getName()), ProblemHighlightType.GENERIC_ERROR_OR_WARNING, null);
                                    } else {
                                        // If this field is incomplete, then we should set the error severity here to the same level as the incomplete annotation error, which is ProblemHighlightType.GENERIC_ERROR
                                        holder.registerProblem(nameValuePair.getNameIdentifier(), Constants.DESCRIPTION_MAPPING.get(nameValuePair.getName()), ProblemHighlightType.GENERIC_ERROR, null);
                                    }
                                }
                            }
                            return;
                        }
                    }
                }

            }
        };
    }
}
