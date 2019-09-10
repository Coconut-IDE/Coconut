package org.intellij.privacyhelper.codeInspection.annotations;

import com.intellij.psi.PsiAnnotation;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;

/**
 * Created by tianshi on 2/8/18.
 */
public class UndefinedPersonalDataTypeAnnotationHolder extends AnnotationHolder {
    public UndefinedPersonalDataTypeAnnotationHolder(PsiAnnotation annotation) {
        super(annotation);
        mAnnotationType = CoconutAnnotationType.UndefinedPersonalDataTypeAnnotation;
//        myForm = nameValuePairs.get("myForm");
//        myPrecision = nameValuePairs.get("myPrecision");
    }

    @Override
    public String getDescription() {
        String dataType = "Unknown data type";
        String purpose = "Unknown purpose";
        if (plainValueFieldPairs.containsKey("dataType")) {
            dataType = String.join(";", plainValueFieldPairs.get("dataType"));
        }
        if (plainValueFieldPairs.containsKey("purposeDescription")) {
            purpose = String.join(";", plainValueFieldPairs.get("purposeDescription"));
        }
        return String.format("Data Type: %s. Purpose: %s", dataType, purpose);

    }

    public UndefinedPersonalDataTypeAnnotationHolder() {
        super();
        mAnnotationType = CoconutAnnotationType.UndefinedPersonalDataTypeAnnotation;
        initAllFields();
    }
}
