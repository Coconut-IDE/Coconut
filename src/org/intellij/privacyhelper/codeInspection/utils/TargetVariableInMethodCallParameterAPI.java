package org.intellij.privacyhelper.codeInspection.utils;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.PersonalDataAPIAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalTargetVariableTracker;

/**
 * Created by tianshi on 2/2/18.
 */
public class TargetVariableInMethodCallParameterAPI extends PersonalDataAPI {

    TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           CoconutAnnotationType annotationType, PersonalDataGroup[] groups,
                                           PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
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
     * @param parameterTypeCanonicalTextRestriction the types that must be present in a given parameter for this API
     * @param parameterValueTextRestriction any text that must be present in a given parameter for this API
     * @param requiredPermissions permissions required for the method call
     * @param libImplicitVoluntaryPermissions
     * @param isThirdPartyAPI
     * @param thirdPartyName
     * @param annotationType the type of annotation associated with this method call
     * @param groups
     * @param config
     * @param targetVariableTracker
     * @param isPersonalDataSource
     * @param isPersonalDataSink
     */
    TargetVariableInMethodCallParameterAPI(String displayName, String fullAPIName, String returnValueTypeCanonicalText,
                                           String[] parameterTypeCanonicalTextRestriction, String[] parameterValueTextRestriction,
                                           AndroidPermission[][] requiredPermissions, AndroidPermission[][] libImplicitVoluntaryPermissions, boolean isThirdPartyAPI, String thirdPartyName,
                                           CoconutAnnotationType annotationType, PersonalDataGroup[] groups,
                                           PersonalDataAPIAnnotationUtil config, PersonalTargetVariableTracker targetVariableTracker,
                                           boolean isPersonalDataSource, boolean isPersonalDataSink) {
        super(annotationType, PersonalDataAPIType.PERSONAL_DATA_IN_METHOD_CALL_PARAMETER,
                displayName, fullAPIName, returnValueTypeCanonicalText,
                parameterTypeCanonicalTextRestriction, parameterValueTextRestriction,
                requiredPermissions, libImplicitVoluntaryPermissions, isThirdPartyAPI, groups, config, targetVariableTracker,
                isPersonalDataSource, isPersonalDataSink);
    }
}
