package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethodCallExpression;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 4/26/17.
 */
public class TargetValueFromReturnValueAPI extends PersonalDataAPI {

    /**
     *
     * @param displayName
     * @param fullAPIName regex string for the complete method name, including the package name of the library, for template matching
     * @param returnValueTypeCanonicalText regex string for the complete name of the return value type of the method call for template matching
     * @param requiredPermissions permissions required for the method call
     * @param libImplicitVoluntaryPermissions
     * @param isThirdPartyAPI
     * @param thirdPartyName
     * @param annotationType The type of annotation associated with this method call
     * @param groups
     * @param config
     * @param targetVariableTracker
     * @param isPersonalDataSource
     * @param isPersonalDataSink
     */
    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                  AndroidPermission [][] requiredPermissions, AndroidPermission [][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  CoconutAnnotationType annotationType,
                                  PersonalDataGroup [] groups,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalText,
                new String[]{}, new String[]{},
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }

    /**
     *
     * @param displayName string representing the name of the given API
     * @param fullAPIName regex string for the complete method name, including the package name of the library, for template matching
     * @param returnValueTypeCanonicalText regex string for the complete name of the return value type of the method call for template matching
     * @param parameterTypeCanonicalTextRestriction
     * @param parameterValueTextRestriction
     * @param requiredPermissions permissions required for the method call
     * @param libImplicitVoluntaryPermissions
     * @param isThirdPartyAPI
     * @param thirdPartyName
     * @param annotationType The type of annotation associated with this method call
     * @param groups
     * @param config
     * @param targetVariableTracker
     * @param isPersonalDataSource
     * @param isPersonalDataSink
     */
    TargetValueFromReturnValueAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                  String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                  AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions,
                                  boolean isThirdPartyAPI, String thirdPartyName,
                                  CoconutAnnotationType annotationType,
                                  PersonalDataGroup[] groups,
                                  PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                  boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_FROM_RETURN_VALUE,
                displayName, fullAPIName, returnValueTypeCanonicalText, parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }
}