package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class CalendarAnnotationHolder extends AnnotationHolder {
    public CalendarAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.CalendarAnnotation;
    }
    public CalendarAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.CalendarAnnotation;
        initAllFields();
    }

    @Override
    public String getDescription() {
        // TODO (long-term) purpose
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
