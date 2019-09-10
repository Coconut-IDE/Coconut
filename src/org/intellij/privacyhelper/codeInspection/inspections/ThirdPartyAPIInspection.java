package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.quickfixes.AddPreFilledAnnotationByTypeQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.DeclareVariableQuickfix;
import org.intellij.privacyhelper.codeInspection.quickfixes.NavigateToCodeQuickfix;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataAPI;
import org.intellij.privacyhelper.codeInspection.utils.ThirdPartyAPIList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;
import static org.intellij.privacyhelper.codeInspection.utils.Constants.INCONSISTENT_ANNOTATION;

/**
 * Created by tianshi on 2/4/18.
 */
public class ThirdPartyAPIInspection extends BaseJavaLocalInspectionTool {
    static private PersonalDataAPI[] APIList;

    public ThirdPartyAPIInspection () {
        super();
        APIList = ThirdPartyAPIList.getAPIList();
    }

    @NotNull
    @Override
    public String getShortName() { return "ThirdPartyAPIInspection"; }

    static public void MyVisitMethodCallExpression(@Nullable ProblemsHolder holder, PsiMethodCallExpression expression) {
        Project openProject = expression.getProject();
        for (PersonalDataAPI api : APIList) {
            if (api.psiElementMethodCallMatched(expression)) {
                PsiElement resolvedTargetVariable = api.getResolvedTargetVariable(expression);
                if (resolvedTargetVariable == null) {
                    String type = null;
                    PsiElement targetVariable = api.getTargetVariable(expression);
                    if (targetVariable == null) {
                        return;
                    }
                    if (targetVariable instanceof PsiExpression && ((PsiExpression) targetVariable).getType() != null) {
                        type = ((PsiExpression) targetVariable).getType().getCanonicalText();
                    }
                    if (type != null) {
                        if (holder != null) {
                            holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, new DeclareVariableQuickfix(type, "temp_var"));
                        }
                    } else {
                        if (holder != null) {
                            holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, null);
                        }
                    }
                    PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression, api);
                    return;
                }

                AnnotationHolder [] annotationsSpeculatedFromAPICall = api.createAnnotationInferencesFromSource(expression);
                PsiAnnotation [] annotationInstances = CodeInspectionUtil.getAllAnnotations(resolvedTargetVariable);

                ArrayList<PsiAnnotation> annotationInstanceArrayList = new ArrayList<>();
                ArrayList<AnnotationHolder> annotationHolderInstanceArrayList = new ArrayList<>();
                ArrayList<AnnotationHolder> annotationSpeculationArrayList = new ArrayList<>();
                Collections.addAll(annotationSpeculationArrayList, annotationsSpeculatedFromAPICall);

                for (AnnotationHolder speculatedAnnotation : annotationsSpeculatedFromAPICall) {
                    PsiAnnotation annotationInstanceOfType = null;
                    AnnotationHolder annotationInstanceHolderOfType = null;
                    for (PsiAnnotation annotationInstance : annotationInstances) {
                        if (speculatedAnnotation.mAnnotationType == CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotationInstance)) {
                            annotationInstanceOfType = annotationInstance;
                            annotationInstanceHolderOfType = CodeInspectionUtil.parseAnnotation(annotationInstance);
                        }
                    }
                    if (annotationInstanceHolderOfType == null) {
                        annotationHolderInstanceArrayList.add(annotationInstanceHolderOfType);
                        annotationInstanceArrayList.add(annotationInstanceOfType);
                        // If one type of annotation in the speculation is missing, then this is treated as an error (Because we can have very high confidence in our speculation for third party libraries)
                        if (holder != null) {
                            holder.registerProblem(expression.getMethodExpression(), String.format(ANNOTATION_REQUIRED, speculatedAnnotation.mAnnotationType.toString()),
                                    ProblemHighlightType.GENERIC_ERROR,
                                    new AddPreFilledAnnotationByTypeQuickfix(api, speculatedAnnotation.mAnnotationType));
                        }
                    } else {
                        annotationHolderInstanceArrayList.add(annotationInstanceHolderOfType);
                        annotationInstanceArrayList.add(annotationInstanceOfType);
                        boolean completeSinkAnnotation = CodeInspectionUtil.checkAnnotationCompletenessByType(annotationInstanceOfType, null);
                        boolean consistentSinkAnnotation = CodeInspectionUtil.checkAnnotationConsistency(annotationInstanceOfType, speculatedAnnotation, null);
                        SmartPsiElementPointer annotationInstanceOfTypePointer =
                                SmartPointerManager.getInstance(annotationInstanceOfType.getProject()).createSmartPsiElementPointer(annotationInstanceOfType);

                        if (!completeSinkAnnotation) {
                            if (holder != null) {
                                holder.registerProblem(expression.getMethodExpression(), INVALID_ANNOTATION, ProblemHighlightType.GENERIC_ERROR,
                                        new NavigateToCodeQuickfix(annotationInstanceOfTypePointer, String.format("the %s annotation", speculatedAnnotation.mAnnotationType.toString())));
                            }
                        }
                        if (!consistentSinkAnnotation) {
                            if (completeSinkAnnotation) {
                                if (holder != null) {
                                    holder.registerProblem(expression.getMethodExpression(), INCONSISTENT_ANNOTATION, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            new NavigateToCodeQuickfix(annotationInstanceOfTypePointer, String.format("the %s annotation", speculatedAnnotation.mAnnotationType.toString())));
                                }
                            } else {
                                if (holder != null) {
                                    holder.registerProblem(expression.getMethodExpression(), INCONSISTENT_ANNOTATION, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            null);
                                }
                            }
                        }
                    }
                }

                PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression,
                        annotationInstanceArrayList.toArray(new PsiAnnotation[0]),
                        annotationSpeculationArrayList.toArray(new AnnotationHolder[0]),
                        annotationHolderInstanceArrayList.toArray(new AnnotationHolder[0]),
                        api);

            }
        }
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {
            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                MyVisitMethodCallExpression(holder, expression);
            }
        };
    }
}
