package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class ContactsAnnotationHolder extends AnnotationHolder {

    public ContactsAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.ContactsAnnotation;
//        myContactInfoType = Arrays.stream(nameValuePairs.get("contactInfoTypeClass").split("[{, }]+")).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
    public ContactsAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.ContactsAnnotation;
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
