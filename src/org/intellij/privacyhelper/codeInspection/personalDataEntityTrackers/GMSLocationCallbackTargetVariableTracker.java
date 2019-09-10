package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

import static org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil.getDataEntityFromClass;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSLocationCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    private int callbackParameterPosition;
    static private final String callbackTypeCanonicalText = "com.google.android.gms.location.LocationCallback";
    static private final String callbackName = "onLocationResult";


    public GMSLocationCallbackTargetVariableTracker(int callbackParameterPosition) {
        this.callbackParameterPosition = callbackParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiClass psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(source, callbackParameterPosition);
        if (psiClass == null) {
            return null;
        }
        return getDataEntityFromClass(psiClass, callbackTypeCanonicalText,callbackName);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
