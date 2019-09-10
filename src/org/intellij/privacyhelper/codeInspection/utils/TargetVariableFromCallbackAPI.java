package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 11/13/17.
 */
public class TargetVariableFromCallbackAPI extends PersonalDataAPI {
    TargetVariableFromCallbackAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                  CoconutAnnotationType annotationType, PersonalDataGroup[] groups,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK,
                displayName, fullAPIName, returnValueTypeCanonicalText,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }


    TargetVariableFromCallbackAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                  CoconutAnnotationType annotationType, PersonalDataGroup[] groups,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_FROM_CALLBACK,
                displayName, fullAPIName, returnValueTypeCanonicalText,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }
}
