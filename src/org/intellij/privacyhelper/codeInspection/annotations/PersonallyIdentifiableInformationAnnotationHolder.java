package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;

public class PersonallyIdentifiableInformationAnnotationHolder extends AnnotationHolder {
    public PersonallyIdentifiableInformationAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.PersonallyIdentifiableInformationAnnotation;
    }
    public PersonallyIdentifiableInformationAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.PersonallyIdentifiableInformationAnnotation;
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
