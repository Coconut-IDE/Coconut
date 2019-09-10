package org.intellij.privacyhelper.codeInspection.instances;

import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;

import java.util.HashMap;

/**
 * Created by tianshi on 2/3/18.
 */
public class AnnotationMetaData {
    public AnnotationHolder annotationInstance;
    public AnnotationHolder annotationSpeculation;
    public SmartPsiElementPointer psiAnnotationPointer;
    private CoconutAnnotationType annotationType;

    public AnnotationMetaData(AnnotationHolder annotationInstance, AnnotationHolder annotationSpeculation, SmartPsiElementPointer psiAnnotationPointer) {
        this.annotationInstance = annotationInstance;
        this.annotationSpeculation = annotationSpeculation;
        this.psiAnnotationPointer = psiAnnotationPointer;
        if (annotationInstance != null) {
            this.annotationType = annotationInstance.mAnnotationType;
        } else if (annotationSpeculation != null) {
            this.annotationType = annotationSpeculation.mAnnotationType;
        }
    }

    public CoconutAnnotationType getAnnotationType() {
        return annotationType;
    }

}
