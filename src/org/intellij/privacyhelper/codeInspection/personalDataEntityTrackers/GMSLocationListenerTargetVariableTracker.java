package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

import static org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil.getDataEntityFromClass;

/**
 * Created by tianshi on 1/21/18.
 */
public class GMSLocationListenerTargetVariableTracker extends PersonalTargetVariableTracker {
    private int listenerParameterPosition;
    static private final String listenerTypeCanonicalText = "com.google.android.gms.location.LocationListener";
    static private final String callbackName = "onLocationChanged";

    public GMSLocationListenerTargetVariableTracker(int listenerParameterPosition) {
        this.listenerParameterPosition = listenerParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiClass psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(source, listenerParameterPosition);
        if (psiClass == null) {
            return null;
        }
        return getDataEntityFromClass(psiClass, listenerTypeCanonicalText,callbackName);
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
