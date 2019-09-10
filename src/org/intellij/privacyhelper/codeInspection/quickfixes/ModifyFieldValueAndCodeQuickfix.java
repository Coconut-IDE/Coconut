package org.intellij.privacyhelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tianshi on 3/11/18.
 */
public class ModifyFieldValueAndCodeQuickfix implements LocalQuickFix{
    private final String modifyFieldValueAndCodeQuickfixName;
    private HashMap<SmartPsiElementPointer, ArrayList<String>> annotationFieldChangePointerMap = new HashMap<>();
    private SmartPsiElementPointer methodCallExpressionPointer;
    private ChangeCodeFunction changeCodeFunction;

    public ModifyFieldValueAndCodeQuickfix(String quickfixName,
                                           PsiMethodCallExpression methodCallExpression, PsiNameValuePair nameValuePair,
                                           ArrayList<String> targetFieldValues,
                                           ChangeCodeFunction changeCodeFunction) {
        this.modifyFieldValueAndCodeQuickfixName = quickfixName;
        annotationFieldChangePointerMap.put(
                SmartPointerManager.getInstance(nameValuePair.getProject()).createSmartPsiElementPointer(nameValuePair),
                targetFieldValues);
        if (methodCallExpression != null) {
            this.methodCallExpressionPointer = SmartPointerManager.getInstance(methodCallExpression.getProject()).createSmartPsiElementPointer(methodCallExpression);
        } else {
            this.methodCallExpressionPointer = null;
        }
        this.changeCodeFunction = changeCodeFunction;
    }

    public ModifyFieldValueAndCodeQuickfix(String quickfixName,
                                           PsiMethodCallExpression methodCallExpression,
                                           HashMap<PsiNameValuePair, ArrayList<String>> annotationFieldChangeMap,
                                           ChangeCodeFunction changeCodeFunction) {
        this.modifyFieldValueAndCodeQuickfixName = quickfixName;
        for (Map.Entry<PsiNameValuePair, ArrayList<String>> entry : annotationFieldChangeMap.entrySet()) {
            PsiNameValuePair nameValuePair = entry.getKey();
            ArrayList<String> targetFieldValues = entry.getValue();
            annotationFieldChangePointerMap.put(
                    SmartPointerManager.getInstance(nameValuePair.getProject()).createSmartPsiElementPointer(nameValuePair),
                    targetFieldValues);
        }
        if (methodCallExpression != null) {
            this.methodCallExpressionPointer = SmartPointerManager.getInstance(methodCallExpression.getProject()).createSmartPsiElementPointer(methodCallExpression);
        } else {
            this.methodCallExpressionPointer = null;
        }
        this.changeCodeFunction = changeCodeFunction;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Coconut quick-fixes";
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return modifyFieldValueAndCodeQuickfixName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        if (methodCallExpressionPointer == null) {
            return;
        }
        for (Map.Entry<SmartPsiElementPointer, ArrayList<String>> entry : annotationFieldChangePointerMap.entrySet()) {
            PsiNameValuePair nameValuePair = (PsiNameValuePair) entry.getKey().getElement();
            ArrayList<String> targetFieldValues = entry.getValue();
            String valueArrayString = String.format("{%s}", String.join(",", targetFieldValues));
            PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
            if (nameValuePair != null) {
                nameValuePair.setValue(factory.createExpressionFromText(valueArrayString, null));
            }
        }
        PsiMethodCallExpression methodCallExpression = (PsiMethodCallExpression) methodCallExpressionPointer.getElement();
        if (methodCallExpression != null) {
            // Call some helper function defined in api object
            changeCodeFunction.change(methodCallExpression);
        }
    }
}
