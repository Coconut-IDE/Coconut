package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;

import java.util.*;
import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.accessible_to_other_apps;
import static org.intellij.privacyhelper.codeInspection.utils.Constants.private_to_this_app;

/**
 * Created by tianshi on 2/3/18.
 */
public class StorageUtils {

    static public AnnotationHolder createAPIAnnotationSpeculationByModeParameter(PsiElement source, String [] relevantVariableTypeList, Map<String, Integer> accessModeParameterPositionMap) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.StorageAnnotation);
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        for (Map.Entry<String, Integer> entry: accessModeParameterPositionMap.entrySet()) {
            String methodNamePattern = entry.getKey();
            int accessModeParameterPosition = entry.getValue();
            ArrayList<PsiMethodCallExpression> expressions = AnnotationUtils.getRelevantMethodCallExpression(methodNamePattern, relevantVariableTypeList, sourceMethodCallExp);
            if (expressions.size() == 1) {
                PsiMethodCallExpression expression = expressions.get(0);
                if (accessModeParameterPosition >= 0) {
                    if (expression == null || expression.getArgumentList().getExpressions().length <= accessModeParameterPosition) {
                        continue;
                    }
                    PsiExpression modeExpression = expression.getArgumentList().getExpressions()[accessModeParameterPosition];
                    if (Pattern.compile(".*MODE_WORLD_READABLE.*", Pattern.DOTALL).matcher(modeExpression.getText()).matches() ||
                            Pattern.compile(".*MODE_WORLD_WRITEABLE.*", Pattern.DOTALL).matcher(modeExpression.getText()).matches() ||
                            Pattern.compile("1", Pattern.DOTALL).matcher(modeExpression.getText()).matches() ||
                            Pattern.compile("2", Pattern.DOTALL).matcher(modeExpression.getText()).matches()) {
                        annotationHolder.put("accessControl", String.format("AccessControlOption.%s", accessible_to_other_apps));
                    } else {
                        annotationHolder.put("accessControl", String.format("AccessControlOption.%s", private_to_this_app));
                    }
                } else {
                    if (expression != null && Pattern.compile(methodNamePattern, Pattern.DOTALL).matcher(expression.getMethodExpression().getCanonicalText()).matches()) {
                        annotationHolder.put("accessControl", String.format("AccessControlOption.%s", private_to_this_app));
                    }
                }
            }
        }
        return annotationHolder;
    }

    public static AnnotationHolder createAnnotationSpeculationByAPIName(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.StorageAnnotation);
        final String[] externalStorageRelevantVariableTypeList = new String[] {".*Writer", ".*OutputStream", ".*File", ".*File.*\\[\\]", "String"};
        PsiMethodCallExpression sourceMethodCallExp;
        if (!(source instanceof PsiMethodCallExpression)) {
            sourceMethodCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        } else {
            sourceMethodCallExp = (PsiMethodCallExpression) source;
        }
        String [] publicExternalStorageAPINamePatterns = new String[] {".*getExternalStorageDirectory", ".*getExternalStoragePublicDirectory"};
        ArrayList<PsiMethodCallExpression> publicExternalStorageExpressionList = new ArrayList<>();
        for (String apiNamePattern : publicExternalStorageAPINamePatterns) {
            publicExternalStorageExpressionList.addAll(
                    AnnotationUtils.getRelevantMethodCallExpression(
                            apiNamePattern, externalStorageRelevantVariableTypeList, sourceMethodCallExp));
        }
        String [] privateExternalStorageAPINamePatterns = new String[] {".*getExternalFilesDir", ".*getExternalCacheDirs", ".*getExternalCacheDir", ".*getCacheDir", ".*getFilesDir"};
        ArrayList<PsiMethodCallExpression> privateExternalStorageExpressionList = new ArrayList<>();
        for (String apiNamePattern : privateExternalStorageAPINamePatterns) {
            privateExternalStorageExpressionList.addAll(
                    AnnotationUtils.getRelevantMethodCallExpression(
                            apiNamePattern, externalStorageRelevantVariableTypeList, sourceMethodCallExp));
        }
        if (publicExternalStorageExpressionList.isEmpty() && privateExternalStorageExpressionList.isEmpty()) {
            return annotationHolder;
        }
        if (!publicExternalStorageExpressionList.isEmpty() && !privateExternalStorageExpressionList.isEmpty()) {
            // conflict
            // TODO: (urgent) give explanation to developers in this situation
            return annotationHolder;
        }
        if (!publicExternalStorageExpressionList.isEmpty()) {
            annotationHolder.put("accessControl", String.format("AccessControlOption.%s", accessible_to_other_apps));
        } else if (!privateExternalStorageExpressionList.isEmpty()) {
            annotationHolder.put("accessControl", String.format("AccessControlOption.%s", private_to_this_app));
        }
        return annotationHolder;
    }
}
