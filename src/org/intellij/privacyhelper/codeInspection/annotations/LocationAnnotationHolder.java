package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class LocationAnnotationHolder extends AnnotationHolder {
    public LocationAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.LocationAnnotation;
    }

    public LocationAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.LocationAnnotation;
        initAllFields();
    }

}
