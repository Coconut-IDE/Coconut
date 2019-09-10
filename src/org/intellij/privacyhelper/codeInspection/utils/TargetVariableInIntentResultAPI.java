package org.intellij.privacyhelper.codeInspection.utils;

import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

import javax.annotation.Nullable;

public class TargetVariableInIntentResultAPI extends PersonalDataAPI {

    TargetVariableInIntentResultAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                           String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           @Nullable CoconutAnnotationType annotationType, @Nullable PersonalDataGroup[] groups,
                                           PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_FROM_INTENT,
                displayName, fullAPIName, returnValueTypeCanonicalText,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }
}
