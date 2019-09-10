package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class PrivacyNoticeAnnotationHolder extends AnnotationHolder {
    public PrivacyNoticeAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.PrivacyNoticeAnnotation;
    }
    public PrivacyNoticeAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.PrivacyNoticeAnnotation;
        initAllFields();
    }

    @Override
    public String getDescription() {
        // intentionally return null because getDescription shouldn't be called on this class
        return null;
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
