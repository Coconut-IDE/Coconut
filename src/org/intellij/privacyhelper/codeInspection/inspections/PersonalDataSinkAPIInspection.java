package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.BaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiEmptyExpressionImpl;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.instances.AnnotationMetaData;
import org.intellij.privacyhelper.codeInspection.instances.PersonalDataInstance;
import org.intellij.privacyhelper.codeInspection.quickfixes.*;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * Created by tianshi on 2/2/18.
 */
public class PersonalDataSinkAPIInspection extends BaseJavaLocalInspectionTool {
    static private PersonalDataAPI[] APIList;

    public PersonalDataSinkAPIInspection () {
        super();
        APIList = PersonalDataSinkAPIList.getAPIList();
    }

    @NotNull
    @Override
    public String getShortName() { return "PersonalDataSinkAPIInspection"; }

    static public void MyVisitMethodCallExpression(@Nullable ProblemsHolder holder, PsiMethodCallExpression expression) {
        Project openProject = expression.getProject();

        for (PersonalDataAPI api : APIList) {
            if (api.psiElementMethodCallMatched(expression)) {
                PsiElement dataInstance = api.getResolvedTargetVariable(expression);
                if (dataInstance == null) {
                    String type = null;
                    PsiElement targetVariable = api.getTargetVariable(expression);
                    if (targetVariable == null) {
                        // When there is no target variable available, which is usually because the corresponding parameter has not been passed in the API call, ignore and return directly.
                        return;
                    }
                    if (targetVariable instanceof PsiExpression && ((PsiExpression) targetVariable).getType() != null) {
                        type = ((PsiExpression) targetVariable).getType().getCanonicalText();
                    }
                    try {
                        if (type != null) {
                            if (holder != null && !(targetVariable instanceof PsiEmptyExpressionImpl)) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, new DeclareVariableQuickfix(type, "temp_var"));
                            }
                        } else {
                            if (holder != null && !(targetVariable instanceof PsiEmptyExpressionImpl)) {
                                holder.registerProblem(targetVariable, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, null);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression, api);
                    return;
                }
                AnnotationHolder sinkAnnotationSpeculatedFromAPICall = api.createAnnotationInferenceFromSource(expression);
                PsiAnnotation sinkAnnotation = CodeInspectionUtil.getAnnotationByType(dataInstance, api.getAnnotationType());

                if (sinkAnnotation == null) {
                    if (holder != null) {
                        holder.registerProblem(expression.getMethodExpression(), String.format(ANNOTATION_REQUIRED, api.getAnnotationType().toString()),
                                ProblemHighlightType.GENERIC_ERROR, new AddPreFilledAnnotationByTypeQuickfix(api));
                    }

                    PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression, null, sinkAnnotationSpeculatedFromAPICall, null, api);
                } else {
                    // sinkAnnotation is not null
                    SmartPsiElementPointer sinkAnnotationPointer = SmartPointerManager.getInstance(sinkAnnotation.getProject()).createSmartPsiElementPointer(sinkAnnotation);
                    PsiAnnotation [] allAnnotations = CodeInspectionUtil.getAllAnnotations(dataInstance);
                    ArrayList<PsiAnnotation> personalDataAnnotations = new ArrayList<>();
                    boolean hasNotPersonalDataAnnotation = false;
                    ArrayList<LocalQuickFix> pasteExistingAnnotationQuickfixList = new ArrayList<>();
                    ArrayList<AnnotationHolder> existingAnnotationList = new ArrayList<>();
                    for (PersonalDataInstance instance : PersonalDataHolder.getInstance(openProject).getSourceAPICallInstances()) {
                        for (AnnotationMetaData annotationMetaData : instance.getAnnotationMetaDataList()) {
                            if (annotationMetaData.annotationInstance != null &&
                                    !existingAnnotationList.contains(annotationMetaData.annotationInstance) &&
                                    CodeInspectionUtil.isPersonalDataSourceAPIAnnotationType(annotationMetaData.getAnnotationType())) {
                                existingAnnotationList.add(annotationMetaData.annotationInstance);
                            }
                        }
                    }
                    for (AnnotationHolder annotationHolder : existingAnnotationList) {
                        pasteExistingAnnotationQuickfixList.add(new PasteExistingAnnotationQuickfix(annotationHolder, api));
                    }
                    for (PsiAnnotation annotation : allAnnotations) {
                        if (CodeInspectionUtil.isPersonalDataAnnotation(annotation)) {
                            personalDataAnnotations.add(annotation);
                        } else if (CodeInspectionUtil.isNotPersonalDataAnnotation(annotation)) {
                            hasNotPersonalDataAnnotation = true;
                        }
                    }
                    if (hasNotPersonalDataAnnotation && !personalDataAnnotations.isEmpty()) {
                        SmartPsiElementPointer dataInstancePointer = SmartPointerManager.getInstance(dataInstance.getProject()).createSmartPsiElementPointer(dataInstance);
                        if (holder != null) {
                            holder.registerProblem(expression.getMethodExpression(), CONFLICT_ANNOTATION, ProblemHighlightType.GENERIC_ERROR,
                                    new NavigateToCodeQuickfix(dataInstancePointer, "the variable holds the data to be stored/egressed"));
                        }
                        PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression, sinkAnnotation, sinkAnnotationSpeculatedFromAPICall, CodeInspectionUtil.parseAnnotation(sinkAnnotation), api);
                        return;
                    }
                    if (hasNotPersonalDataAnnotation) {
                        PersonalDataHolder.getInstance(openProject).removePersonalDataInstance(expression);
                        return;
                    } else if (personalDataAnnotations.isEmpty()) {
                        ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
                        quickFixes.addAll(pasteExistingAnnotationQuickfixList);
                        quickFixes.add(new AddPreFilledAnnotationByTypeQuickfix(api, CoconutAnnotationType.NotPersonalData));
                        quickFixes.add(new AddPreFilledAnnotationByTypeQuickfix(api, CoconutAnnotationType.UndefinedPersonalDataTypeAnnotation));
                        quickFixes.add(new LaunchDataOverviewQuickfix());
                        quickFixes.add(new NavigateToCodeQuickfix(sinkAnnotationPointer, String.format("the %s annotation", api.getAnnotationType().toString())));
                        if (holder != null) {
                            holder.registerProblem(expression.getMethodExpression(), MISSING_PERSONAL_DATA_ANNOTATION, ProblemHighlightType.GENERIC_ERROR,
                                    quickFixes.toArray(new LocalQuickFix[0]));
                        }
                        ArrayList<PsiAnnotation> psiAnnotationArrayList = new ArrayList<>();
                        ArrayList<AnnotationHolder> annotationInstanceArrayList = new ArrayList<>();
                        ArrayList<AnnotationHolder> annotationSpeculationArrayList = new ArrayList<>();
                        psiAnnotationArrayList.add(sinkAnnotation);
                        annotationInstanceArrayList.add(CodeInspectionUtil.parseAnnotation(sinkAnnotation));
                        annotationSpeculationArrayList.add(sinkAnnotationSpeculatedFromAPICall);

                        PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression,
                                psiAnnotationArrayList.toArray(new PsiAnnotation[0]),
                                annotationSpeculationArrayList.toArray(new AnnotationHolder[0]),
                                annotationInstanceArrayList.toArray(new AnnotationHolder[0]), api);
                    } else {
                        boolean completeSinkAnnotation = CodeInspectionUtil.checkAnnotationCompletenessByType(sinkAnnotation, null);
                        boolean consistentSinkAnnotation = CodeInspectionUtil.checkAnnotationConsistency(sinkAnnotation, sinkAnnotationSpeculatedFromAPICall, null);

                        if (!completeSinkAnnotation) {
                            ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
                            quickFixes.addAll(pasteExistingAnnotationQuickfixList);
                            quickFixes.add(new LaunchDataOverviewQuickfix());
                            quickFixes.add(new NavigateToCodeQuickfix(sinkAnnotationPointer, String.format("the %s annotation", api.getAnnotationType().toString())));
                            if (holder != null) {
                                holder.registerProblem(expression.getMethodExpression(), INVALID_ANNOTATION, ProblemHighlightType.GENERIC_ERROR,
                                        quickFixes.toArray(new LocalQuickFix[0]));
                            }
                        }
                        if (!consistentSinkAnnotation) {
                            if (completeSinkAnnotation) {
                                ArrayList<LocalQuickFix> quickFixes = new ArrayList<>();
                                quickFixes.addAll(pasteExistingAnnotationQuickfixList);
                                quickFixes.add(new LaunchDataOverviewQuickfix());
                                quickFixes.add(new NavigateToCodeQuickfix(sinkAnnotationPointer, String.format("the %s annotation", api.getAnnotationType().toString())));
                                if (holder != null) {
                                    holder.registerProblem(expression.getMethodExpression(), INCONSISTENT_ANNOTATION, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            quickFixes.toArray(new LocalQuickFix[0]));
                                }
                            } else {
                                if (holder != null) {
                                    holder.registerProblem(expression.getMethodExpression(), INCONSISTENT_ANNOTATION, ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                            null);
                                }
                            }
                        }

                        for (PsiAnnotation annotation : personalDataAnnotations) {
                            SmartPsiElementPointer annotationPointer = SmartPointerManager.getInstance(annotation.getProject()).createSmartPsiElementPointer(annotation);
                            boolean completeAnnotation = CodeInspectionUtil.checkAnnotationCompletenessByType(annotation, null);
                            if (!completeAnnotation) {
                                if (holder != null) {
                                    holder.registerProblem(expression.getMethodExpression(), INVALID_ANNOTATION, ProblemHighlightType.GENERIC_ERROR,
                                            new NavigateToCodeQuickfix(annotationPointer,
                                                    String.format("the %s annotation", CodeInspectionUtil.getAnnotationTypeFromPsiAnnotation(annotation).toString())));
                                }
                            }
                        }
                        ArrayList<PsiAnnotation> psiAnnotationArrayList = new ArrayList<>();
                        ArrayList<AnnotationHolder> annotationInstanceArrayList = new ArrayList<>();
                        ArrayList<AnnotationHolder> annotationSpeculationArrayList = new ArrayList<>();
                        psiAnnotationArrayList.add(sinkAnnotation);
                        annotationInstanceArrayList.add(CodeInspectionUtil.parseAnnotation(sinkAnnotation));
                        annotationSpeculationArrayList.add(sinkAnnotationSpeculatedFromAPICall);
                        for (PsiAnnotation annotation : personalDataAnnotations) {
                            psiAnnotationArrayList.add(annotation);
                            annotationInstanceArrayList.add(CodeInspectionUtil.parseAnnotation(annotation));
                            // So far we can only make speculation for the sink annotation
                            annotationSpeculationArrayList.add(null);
                        }

                        PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression,
                                psiAnnotationArrayList.toArray(new PsiAnnotation[0]),
                                annotationSpeculationArrayList.toArray(new AnnotationHolder[0]),
                                annotationInstanceArrayList.toArray(new AnnotationHolder[0]), api);
                    }
                }
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
