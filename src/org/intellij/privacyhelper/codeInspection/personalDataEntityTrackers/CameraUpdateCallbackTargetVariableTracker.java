package org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;

/**
 * This class specifically tracks variables for callbacks in the camera2.CameraCaptureSession class, looking at two
 * different listeners in the CameraCaptureSession.CaptureCallback subclass.
 *
 * @author elijahneundorfer on 6/3/19
 * @version 6/6/19
 */
public class CameraUpdateCallbackTargetVariableTracker extends PersonalTargetVariableTracker {
    private static String listenerTypeCanonicalText = "android.hardware.camera2.CameraCaptureSession.CaptureCallback";
    private static String onCaptureCompletedCallbackName = "onCaptureCompleted";
    private static int onCaptureCompletedDataInCallbackParameterPosition = 2;
    private static String onCaptureProgressedCallbackName = "onCaptureProgressed";
    private static int onCaptureProgressedDataInCallbackParameterPosition = 2;
    private int listenerParameterPosition;

    public CameraUpdateCallbackTargetVariableTracker(int listenerParameterPosition) {
        this.listenerParameterPosition = listenerParameterPosition;
    }

    /**
     *
     *
     * @param source
     * @return A data element corresponding to the method we're tracking
     */
    @Override
    public PsiElement getResolvedTargetVariable(PsiElement source) {
        PsiClass psiClass = TargetVariableTrackerUtil.getCallbackContainingClass(source, listenerParameterPosition);
        if (psiClass != null) {
            PsiElement onCaptureCompleteDataEntity = TargetVariableTrackerUtil.getDataEntityFromClass(psiClass, listenerTypeCanonicalText, onCaptureCompletedCallbackName, onCaptureCompletedDataInCallbackParameterPosition);
            PsiElement onCaptureProgessedDataEntity = TargetVariableTrackerUtil.getDataEntityFromClass(psiClass, listenerTypeCanonicalText, onCaptureProgressedCallbackName, onCaptureProgressedDataInCallbackParameterPosition);
            if (onCaptureCompleteDataEntity != null) {
                return onCaptureCompleteDataEntity;
            } else if (onCaptureProgessedDataEntity != null) {
                return onCaptureProgessedDataEntity;
            }
        }
        //If the method reaches this statement, it means there is either no relevant PsiClass and/or there are no relevant PsiElements, so there is no corresponding data to be returned
        return null;
    }
    //TODO: Annotate both data entities by switching return element to an array and then adjusting the quickfixes

    @Override
    public PsiElement getTargetVariable(PsiElement source) {
        return getResolvedTargetVariable(source);
    }
}