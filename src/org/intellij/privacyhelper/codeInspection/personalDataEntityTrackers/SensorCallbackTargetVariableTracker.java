package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;


import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

/**
 * @author Elijah Neundorfer 6/17/19
 * @version 6/17/19
 */
public class SensorCallbackTargetVariableTracker extends PersonalTargetVariableTracker{
    private int callbackParameterPosition;
    static private final String[][] callbackTypeCanonicalTextsAndNames = {{"android.hardware.SensorEventListener", "onSensorChanged"}, {"android.hardware.TriggerEventListener", "onTrigger"}};
    public SensorCallbackTargetVariableTracker(int callbackParameterPosition) {
        this.callbackParameterPosition = callbackParameterPosition;
    }

    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiClass psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(source, callbackParameterPosition);
        if (psiClass != null) {
            PsiElement dataElement = null;
            for (String[] pair : callbackTypeCanonicalTextsAndNames) {
                dataElement = TargetVariableTrackerUtil.getDataEntityFromClass(psiClass, pair[0], pair[1]/*, 0*/);
                if (dataElement != null) {
                    break;
                }
            }
            if (dataElement != null) {
                return dataElement;
            }
        }
        //If the method reaches this statement, it means there is either no relevant PsiClass and/or there are no relevant PsiElements, so there is no corresponding data to be returned
        return null;
    }

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}
