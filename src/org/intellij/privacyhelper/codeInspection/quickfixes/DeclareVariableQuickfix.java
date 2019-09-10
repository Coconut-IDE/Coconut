package org.intellij.privacyhelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * Created by tianshi on 5/10/17.
 */
public class DeclareVariableQuickfix implements LocalQuickFix {
    private String declarationTypeText;
    private String declarationNameText;
    final static private String DeclareVariableQuickfixName = "Generate a declaration for this value";
    final static private String DeclareVariableQuickfixFamilyName = "Declare variable quickfixes";

    public DeclareVariableQuickfix(String declarationTypeText, String declarationNameText) {
        super();
        this.declarationTypeText = declarationTypeText;
        this.declarationNameText = declarationNameText;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return DeclareVariableQuickfixName;
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return DeclareVariableQuickfixFamilyName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiElement element = problemDescriptor.getPsiElement();
        String expressionText = element.getText();
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        assert PsiTreeUtil.getParentOfType(element, PsiStatement.class) != null;
        PsiStatement currentStatement;
        if (element instanceof PsiStatement) {
            currentStatement = (PsiStatement) element;
        } else {
            currentStatement = PsiTreeUtil.getParentOfType(element, PsiStatement.class);
        }
        PsiStatement declarationStatement = factory.createStatementFromText(
                "TYPE NAME = ".replace("TYPE", declarationTypeText).replace("NAME", declarationNameText)
                        + expressionText + ";",
                null);
        currentStatement.getParent().addBefore(declarationStatement, currentStatement);
        element.replace(factory.createExpressionFromText(declarationNameText, null));
    }
}
