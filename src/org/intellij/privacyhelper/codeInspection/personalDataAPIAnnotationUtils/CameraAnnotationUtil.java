package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiNameValuePair;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.CodeInspectionUtil;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import static org.intellij.privacyhelper.codeInspection.utils.Constants.*;

/**
 * @author elijahneundorfer on 6/3/19
 * @version 6/17/19
 */
public class CameraAnnotationUtil extends PersonalDataAPIAnnotationUtil  {
    @Override
    public AnnotationHolder createAnnotationInferenceFromSource(PsiElement source) {
        AnnotationHolder annotationHolder = CodeInspectionUtil.createEmptyAnnotationHolderByType(CoconutAnnotationType.CameraAnnotation);

        try {
            String methodCallName = source.getChildren()[0].getChildren()[3].getText();
            if (methodCallName.contains("acquireNextImage") ||
                    methodCallName.contains("acquireLatestImage")) {
                annotationHolder.add("dataType", String.format("CameraDataType.%s", image));

            }
            if (methodCallName.contains("capture") ||
                    methodCallName.contains("captureBurst") ||
                    methodCallName.contains("captureBurstRequests") ||
                    methodCallName.contains("captureSingleRequest") ||
                    methodCallName.contains("setRepeatingBurst") ||
                    methodCallName.contains("setRepeatingBurstRequests") ||
                    methodCallName.contains("setRepeatingRequest") ||
                    methodCallName.contains("setSingleRepeatingRequest")) {
                annotationHolder.add("dataType", String.format("CameraDataType.%s", metadata));

            }
            if (methodCallName.contains("setOutputFile")) {
                annotationHolder.add("dataType", String.format("CameraDataType.%s", video));
            }
        } catch (IndexOutOfBoundsException ignore) {
        }

        return annotationHolder;
    }

    @Override
    public AnnotationHolder[] createAnnotationInferencesFromSource(PsiElement source) {
        return new AnnotationHolder[] {createAnnotationInferenceFromSource(source)};
    }

    @Nullable
    @Override
    public LocalQuickFix[] getAdaptCodeToAnnotationQuickfix(PsiMethodCallExpression methodCallExpression, String fieldName, ArrayList<String> fieldValue) {
        return null;
    }

    @Nullable
    @Override
    public LocalQuickFix[] getModifyFieldValueAndCodeQuickfixList(PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair, ArrayList<String> fieldValue) {
        return null;
    }
}
