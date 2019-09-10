package org.intellij.privacyhelper.codeInspection.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by tianshi on 2/9/18.
 */
public class SetInconsistentFieldValueQuickfix implements LocalQuickFix {
    private static final String inconsistentFieldValueQuickfixName = "(Modify annotation) Use the speculated value for this field";
    private ArrayList<String> fieldValues;

    public SetInconsistentFieldValueQuickfix(ArrayList<String> fieldValues) {
        this.fieldValues = fieldValues;
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
        return inconsistentFieldValueQuickfixName;
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor problemDescriptor) {
        PsiNameValuePair pair = PsiTreeUtil.getParentOfType(problemDescriptor.getPsiElement(), PsiNameValuePair.class);
        if (pair == null) {
            return;
        }
        String valueArrayString = String.format("{%s}", String.join(",", fieldValues));
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        pair.setValue(factory.createExpressionFromText(valueArrayString, null));
    }
}
