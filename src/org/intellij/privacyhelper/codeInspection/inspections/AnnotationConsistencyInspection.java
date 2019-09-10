package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.NavigateToCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by tianshi on 11/25/17.
 */
public class AnnotationConsistencyInspection extends BaseJavaLocalInspectionTool {

    @NotNull
    @Override
    public String getShortName() { return "AnnotationConsistencyInspection"; }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitAnnotation(PsiAnnotation annotation) {
                super.visitAnnotation(annotation);
                SmartPsiElementPointer newAnnotationSmartPointer =
                        SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation);
                // Here we first combine the meta info of the same annotation from different sources
                CoconutAnnotationType annotationType = CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation);
                if (annotationType == null) {
                    return;
                }
                ArrayList<AnnotationHolder> speculatedAnnotationHolders = new ArrayList<>();
                ArrayList<PersonalDataInstance> instances = new ArrayList<>();
                Project openProject = newAnnotationSmartPointer.getProject();
                for (PersonalDataInstance instance : PersonalDataHolder.getInstance(openProject).getAllPersonalDataInstances()) {
                    for (AnnotationMetaData metaData : instance.annotationMetaDataList) {
                        if (newAnnotationSmartPointer.equals(metaData.psiAnnotationPointer)) {
                            instances.add(instance);
                            if (metaData.annotationSpeculation != null) {
                                speculatedAnnotationHolders.add(metaData.annotationSpeculation);
                            }
                        }
                    }
                }

                AnnotationHolder speculation = CodeInspectionUtil.combineVirtualAnnotationHolders(speculatedAnnotationHolders);
                CodeInspectionUtil.checkAnnotationConsistency(annotation, speculation, holder, instances);
            }
        };
    }

}
