package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class CallLogsAnnotationHolder extends AnnotationHolder {
    public CallLogsAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.CallLogsAnnotation;
    }
    public CallLogsAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.CallLogsAnnotation;
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
