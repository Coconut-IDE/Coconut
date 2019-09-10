package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiNewExpressionImpl;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by tianshi on 1/28/19.
 */
public class CalendarAnnotationUtil extends PersonalDataAPIAnnotationUtil {
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        // This function returns an AnnotationHolder object which contains field-value pairs that can be inferred by code analysis
        // Ideally this should be constructed based on the code analysis result. But for simplicity, we will begin with placeholders.
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.CalendarAnnotation);

        if (!(source instanceof PsiMethodCallExpression)) {
            return annotationHolder;
        }

        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) source;

        ArrayList<String> dataTypeSpeculations = new ArrayList<>();

        //projection field (selected columns)
        if (methodCallExpression.getArgumentList().getExpressions().length > 1
                && (methodCallExpression.getArgumentList().getExpressions()[1].getType() != null)
                && !methodCallExpression.getArgumentList().getExpressions()[1].getType().toString().contains("PsiType:null")) {
            PsiExpression projection = methodCallExpression.getArgumentList().getExpressions()[1];
            PsiElement projectionString;
            PsiExpression[] projectionArray;

            //TODO: throw exception only if string is null for debugging purposes; change when release Coconut
            if (projection instanceof PsiReferenceExpression) {
                projectionString = QueryUtils.getResolvedValue(projection);
            } else if (projection instanceof PsiNewExpressionImpl) {
                projectionString = projection;
            } else {
                throw new RuntimeException("Projection field of Calendar query defined in a way not currently handled");
            }

            if (projectionString instanceof PsiNewExpressionImpl) {
                PsiArrayInitializerExpression initializerExpression =
                        ((PsiNewExpressionImpl) projectionString).getArrayInitializer();
                if (initializerExpression != null) {
                    projectionArray = initializerExpression.getInitializers();
                    QueryUtils.updateCalendarDataTypeSpeculationsFromStringList(projectionArray, dataTypeSpeculations);
                }
            }
        }

        //selection string
        if (methodCallExpression.getArgumentList().getExpressions().length > 2
                && (methodCallExpression.getArgumentList().getExpressions()[2].getType() != null)
                && !methodCallExpression.getArgumentList().getExpressions()[2].getType().toString().contains("PsiType:null")) {
            PsiExpression selection = methodCallExpression.getArgumentList().getExpressions()[2];
            //selectionArray should only every contain one element
            PsiExpression[] selectionArray = new PsiExpression[1];

            //TODO: throw exception only if string is null for debugging purposes; change when release Coconut
            if (selection instanceof PsiReferenceExpression) {
                selectionArray[0] = (PsiExpression) QueryUtils.getResolvedValue(selection);
            } else if (selection instanceof PsiNewExpressionImpl) {
                selectionArray[0] = selection;
            } else {
                throw new RuntimeException("Selection field of Calendar query defined in a way not currently handled");
            }

            QueryUtils.updateCalendarDataTypeSpeculationsFromStringList(selectionArray, dataTypeSpeculations);
        }

        annotationHolder.put("dataType", dataTypeSpeculations);

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
