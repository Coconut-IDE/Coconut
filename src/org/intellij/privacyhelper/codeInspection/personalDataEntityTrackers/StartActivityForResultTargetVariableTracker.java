package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

/**
 * Created by tianshi on 2/2/18.
 */
public class StartActivityForResultTargetVariableTracker extends PersonalTargetVariableTracker {

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiElement toReturn = getTargetVariable(source);
        return (toReturn != null) ? TargetVariableTrackerUtil.getResolvedVariable(toReturn) : null;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        //Getting the Class
        PsiClass currentClass = PsiTreeUtil.getParentOfType(source, PsiClass.class);
        while (currentClass instanceof PsiAnonymousClass) {
            currentClass = PsiTreeUtil.getParentOfType(currentClass, PsiClass.class);
        }

        //Getting all available methods in the class
        PsiMethod[] methods = PsiTreeUtil.getChildrenOfType(currentClass, PsiMethod.class);

        for(PsiMethod method : methods) {
            if (method.getName().equals("onActivityResult")) {
                PsiParameter[] parameters = (PsiParameter[]) method.getParameters();
                for(PsiParameter parameter : parameters) {
                    if (parameter.getType().getCanonicalText().equals("android.content.Intent")) {
                        return parameter;
                    }
                }
            }
        }
        return null;
    }
}
