package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class SensorAnnotationHolder extends AnnotationHolder {
    public SensorAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.SensorAnnotation;
    }

    public SensorAnnotationHolder() {
        mAnnotationType = CoconutAnnotationType.SensorAnnotation;
        initAllFields();
    }

    @Override
    public String getDescription() {
        // TODO (long-term) return purpose
        return "";
    }

    @Override
    public String getDescriptionByGroup(PersonalDataGroup group) {
        return getDescription();
    }

    @Override
    public String getDescriptionByPermission(AndroidPermission permission) {
        return getDescription();
    }
}
