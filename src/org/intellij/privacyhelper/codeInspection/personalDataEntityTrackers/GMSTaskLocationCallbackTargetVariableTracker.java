package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.regex.Pattern;

import static org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil.getDataEntityFromClass;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSTaskLocationCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    static private final String onSuccessListenerTypeCanonicalText = "com.google.android.gms.tasks.OnSuccessListener<android.location.Location>";
    static private final String onSuccessCallbackName = "onSuccess";
    static private final String onCompleteListenerTypeCanonicalText = "com.google.android.gms.tasks.OnCompleteListener<android.location.Location>";
    static private final String onCompleteCallbackName = "onComplete";

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiElement locationTask = TargetVariableTrackerUtil.getResolvedVariable(source);
        // First check if the return value task is assigned to a variable
        if (locationTask != null) {
            PsiReference [] referenceCollections = ReferencesSearch.search(locationTask).findAll().toArray(new PsiReference[0]);
            for (int i = referenceCollections.length - 1 ; i >= 0 ; i--) {
                PsiElement psiElement = referenceCollections[i].getElement();
                PsiMethodCallExpression methodCallExpression = PsiTreeUtil.getParentOfType(psiElement, PsiMethodCallExpression.class);
                while (methodCallExpression != null) {
                    if (Pattern.compile(".*addOnSuccessListener", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches() ||
                            Pattern.compile(".*addOnCompleteListener", Pattern.DOTALL).matcher(methodCallExpression.getMethodExpression().getText()).matches()) {
                        PsiClass psiClass = null;
                        if (methodCallExpression.getArgumentList().getExpressions().length == 2) {
                            psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(methodCallExpression, 1);
                        } else if (methodCallExpression.getArgumentList().getExpressions().length == 1) {
                            psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(methodCallExpression, 0);
                        }
                        if (psiClass != null) {
                            PsiElement onSuccessDataEntity = getDataEntityFromClass(psiClass, onSuccessListenerTypeCanonicalText, onSuccessCallbackName);
                            PsiElement onCompleteDataEntity = getDataEntityFromClass(psiClass, onCompleteListenerTypeCanonicalText, onCompleteCallbackName);
                            if (onSuccessDataEntity != null) {
                                return onSuccessDataEntity;
                            } else if (onCompleteDataEntity != null) {
                                return onCompleteDataEntity;
                            }
                        }
                    }
                    methodCallExpression = PsiTreeUtil.getParentOfType(methodCallExpression, PsiMethodCallExpression.class);
                }
            }
        }
        // If it can reach this point, it means no valid callback has been detected, then we check whether the callback is directly attached to the method call exp.
        PsiMethodCallExpression addListenerCallExp = PsiTreeUtil.getParentOfType(source, PsiMethodCallExpression.class);
        while (addListenerCallExp != null) {
            if (Pattern.compile(".*addOnSuccessListener", Pattern.DOTALL).matcher(addListenerCallExp.getMethodExpression().getText()).matches() ||
                    Pattern.compile(".*addOnCompleteListener", Pattern.DOTALL).matcher(addListenerCallExp.getMethodExpression().getText()).matches()) {
                PsiClass psiClass = null;
                if (addListenerCallExp.getArgumentList().getExpressions().length == 2) {
                    psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(addListenerCallExp, 1);
                } else if (addListenerCallExp.getArgumentList().getExpressions().length == 1) {
                    psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(addListenerCallExp, 0);
                }
                if (psiClass != null) {
                    PsiElement onSuccessDataEntity = getDataEntityFromClass(psiClass, onSuccessListenerTypeCanonicalText, onSuccessCallbackName);
                    PsiElement onCompleteDataEntity = getDataEntityFromClass(psiClass, onCompleteListenerTypeCanonicalText, onCompleteCallbackName);
                    if (onSuccessDataEntity != null) {
                        return onSuccessDataEntity;
                    } else if (onCompleteDataEntity != null) {
                        return onCompleteDataEntity;
                    }
                }
            }
            addListenerCallExp = PsiTreeUtil.getParentOfType(addListenerCallExp, PsiMethodCallExpression.class);
        }
        return null;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
