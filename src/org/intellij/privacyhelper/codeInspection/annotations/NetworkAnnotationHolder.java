package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;

/**
 * Created by tianshi on 2/4/18.
 */
public class NetworkAnnotationHolder extends AnnotationHolder {
    public NetworkAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.NetworkAnnotation;
    }

    public NetworkAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.NetworkAnnotation;
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
