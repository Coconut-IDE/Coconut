package org.intellij.privacyhelper.codeInspection.annotations;

import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;

/**
 * Created by tianshi on 2/8/18.
 */
public class NotPersonalDataAnnotationHolder extends AnnotationHolder {

    public NotPersonalDataAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.NotPersonalData;
        initAllFields();
    }


    @Override
    public String getDescription() {
        // TODO (urgent) return purpose
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
