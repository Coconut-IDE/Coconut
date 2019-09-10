package org.intellij.privacyhelper.codeInspection.utils;

import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.AdmobAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalDataInMethodCallParameterTargetVariableTracker;

/**
 * Created by tianshi on 2/4/18.
 */
public class ThirdPartyAPIList {
    static PersonalDataAPI[] personalDataAPIs = {
            new TargetVariableInMethodCallParameterAPI("Admob load ad into the AdView",
                    "com\\.google\\.android\\.gms\\.ads\\.AdView\\.loadAd",
                    "void",
                    new AndroidPermission[][]{{AndroidPermission.INTERNET}},
                    new AndroidPermission[][]{{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    true, null,
                    CoconutAnnotationType.AdmobAnnotation,
                    new PersonalDataGroup[] {},
                    new AdmobAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), true, true),
    };

    static public PersonalDataAPI [] getAPIList() {
        return personalDataAPIs;
    }
}
