package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class UniqueIdentifierAnnotationHolder extends AnnotationHolder {
    public UniqueIdentifierAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.UniqueIdentifierAnnotation;
//        myPersonalDataTrackedByUID = Arrays.stream(nameValuePairs.get("personalDataTrackedByUID").split("[{, }]+")).filter(s -> !s.isEmpty()).toArray(String[]::new);
//        myResettability = nameValuePairs.get("resettability");
//        myScope = nameValuePairs.get("scope");
//        myUniqueness = nameValuePairs.get("Uniqueness");
    }

    public UniqueIdentifierAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.UniqueIdentifierAnnotation;
        initAllFields();
    }

    @Override
    public String getDescription() {
        String dataType = "Unknown data type";
        String purposeCategory = "Unknown purpose category";
        String purpose = "Unknown purpose";
        if (plainValueFieldPairs.containsKey("uidType")) {
            dataType = String.join(";", plainValueFieldPairs.get("uidType"));
        }
        if (plainValueFieldPairs.containsKey("purpose")) {
            purposeCategory = String.join(";", plainValueFieldPairs.get("purpose"));
        }
        if (plainValueFieldPairs.containsKey("purposeDescription")) {
            purpose = String.join(";", plainValueFieldPairs.get("purposeDescription"));
        }
        return String.format("Data Type: %s. Purpose: (%s) %s", dataType, purposeCategory, purpose);

    }
}
