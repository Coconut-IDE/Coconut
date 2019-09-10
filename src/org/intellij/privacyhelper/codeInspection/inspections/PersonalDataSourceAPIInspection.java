package org.intellij.privacyhelper.codeInspection.inspections;

import com.intellij.codeInspection.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.quickfixes.*;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;


/**
 * Created by tianshi on 4/10/17.
 */
// Only for internal
public class PersonalDataSourceAPIInspection extends BaseJavaLocalInspectionTool {

    static private PersonalDataAPI[] APIList;

    /**
     * Default constructor for objects of type PersonalDataSourceAPIInspection
     */
    public PersonalDataSourceAPIInspection() {
        super();
        //Get an array of APIs that are not third party and are part of the personal data groups in the parameter.
        APIList = PersonalDataSourceAPIList.getAPIList(false);
    }

    @NotNull
    @Override
    public String getShortName() { return "PersonalDataSourceAPIInspection"; }

    /**
     *
     *
     * @param holder
     * @param expression
     */
    static public void MyVisitMethodCallExpression(@Nullable ProblemsHolder holder, PsiMethodCallExpression expression) {
        Project openProject = expression.getProject();
        for (PersonalDataAPI api : APIList) {
            if (api.psiElementMethodCallMatched(expression)) {
                // First try to extract the target variable that receives the return value
                PsiElement dataEntity = api.getResolvedTargetVariable(expression);
                // If the target variable does not exist, require the developer to create one, or use the quickfixes to generate one.
                if (dataEntity == null) {
                    String type = null;
                    if (!api.targetVariableFromCallback() && !api.targetVariableFromIntent()) {
                        // This is only possible when the data is used in an expression
                        if (expression.getType() != null) {
                            type = expression.getType().getCanonicalText();
                        }
                        if (type != null) {
                            if (holder != null) {
                                holder.registerProblem(expression, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, new DeclareVariableQuickfix(type, "temp_var"));
                            }
                        } else {
                            if (holder != null) {
                                holder.registerProblem(expression, DEFINE_VARIABLE, ProblemHighlightType.GENERIC_ERROR, null);
                            }
                        }
                    }
                    PersonalDataHolder.getInstance(openProject).addPersonalDataInstance(expression, api);
                    return;
                }
                AnnotationHolder [] annotationsSpeculatedFromAPICall = api.createAnnotationInferencesFromSource(expression);
                PsiAnnotation [] annotationInstances = CodeInspectionUtil.getAllAnnotations(dataEntity);

                if (annotationsSpeculatedFromAPICall.length == 0) {
                    // If there isn't an annotation expected for this API, it means this API doesn't collect any
                    // personal data. This may happen for APIs like startActivityForResult that are not always used for
                    // collecting personal data.
                    return;
                }

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
            // TODO (long-term) compare with other apps
        }

    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                MyVisitMethodCallExpression(holder, expression);
            }
        };
    }
}
