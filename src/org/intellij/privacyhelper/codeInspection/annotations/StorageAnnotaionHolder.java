package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.AndroidPermission;
import org.intellij.privacyhelper.codeInspection.utils.PersonalDataGroup;

/**
 * Created by tianshi on 2/2/18.
 */
public class StorageAnnotaionHolder extends AnnotationHolder {

    public StorageAnnotaionHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.StorageAnnotation;
    }

    public StorageAnnotaionHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.StorageAnnotation;
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
