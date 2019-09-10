package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;

/**
 * @author Elijah Neundorfer 7/2/19
 * @version 7/2/19
 */
public class UserInputAnnotationHolder extends AnnotationHolder {
    public UserInputAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.UserInputAnnotation;
    }
    public UserInputAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.UserInputAnnotation;
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
