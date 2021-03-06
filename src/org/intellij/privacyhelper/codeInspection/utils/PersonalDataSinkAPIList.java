package org.intellij.privacyhelper.codeInspection.utils;

import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.FileOutputStreamAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.NetworkAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.SMSAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.SharedPreferenceAnnotationUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.PersonalDataInMethodCallParameterTargetVariableTracker;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.ReturnValueTargetVariableTracker;

/**
 * Created by tianshi on 2/3/18.
 */
public class PersonalDataSinkAPIList {
    static PersonalDataAPI[] personalDataAPIs = {
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putBoolean",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putFloat",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putInt",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putLong",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putString",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Shared Preferences",
                    "android\\.content\\.SharedPreferences\\.Editor\\.putStringSet",
                    "android\\.content\\.SharedPreferences\\.Editor",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new SharedPreferenceAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),false, true),
            new TargetVariableInMethodCallParameterAPI("Internal/External Storage",
                    "java\\.io\\..*OutputStream\\.write",
                    "void",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new FileOutputStreamAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),false, true),
            new TargetVariableInMethodCallParameterAPI("Internal/External Storage",
                    "java\\.io\\..*Writer\\.write",
                    "void",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new FileOutputStreamAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),false, true),
            new TargetVariableInMethodCallParameterAPI("Internal/External Storage",
                    "java\\.io\\..*Writer\\.append",
                    ".*Writer",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.StorageAnnotation,
                    new PersonalDataGroup[] {},
                    new FileOutputStreamAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),false, true),
            new TargetVariableInMethodCallParameterAPI("Network traffic",
                    "com\\.android\\.volley\\.RequestQueue\\.add",
                    ".*",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[]{},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request\\.Get",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request\\.Post",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request\\.Put",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request\\.Delete",
                    "org\\.apache\\.http\\.client\\.fluent\\.Request",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "java\\.net\\.URLConnection\\.getOutputStream",
                    ".OutputStream",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "org\\.apache\\.http\\.client\\.HttpClient\\.execute",
                    ".Response",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Traffic",
                    "okhttp3\\.Request\\.Builder\\.url",
                    "okhttp3\\.Request\\.Builder",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Remote Storage",
                    "com\\.google\\.firebase\\.storage\\.StorageReference\\.putBytes",
                    "com\\.google\\.firebase\\.storage\\.UploadTask",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Remote Storage",
                    "com\\.google\\.firebase\\.storage\\.StorageReference\\.putFile",
                    "com\\.google\\.firebase\\.storage\\.UploadTask",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Remote Storage",
                    "com\\.google\\.firebase\\.storage\\.StorageReference\\.putStream",
                    "com\\.google\\.firebase\\.storage\\.UploadTask",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Request",
                    "org\\.springframework\\.web\\.client\\.RestTemplate\\.execute",
                    ".*",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Request",
                    "org\\.springframework\\.web\\.client\\.RestTemplate\\.exchange",
                    ".*",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            new TargetVariableInMethodCallParameterAPI("Network Request",
                    "org\\.springframework\\.web\\.client\\.RestTemplate\\.post.*",
                    ".*",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {}, false, null,
                    CoconutAnnotationType.NetworkAnnotation,
                    new PersonalDataGroup[] {},
                    new NetworkAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0), false, true),
            /*
            new TargetVariableInMethodCallParameterAPI(
                    "SmsManager.sendTextMessage",
                    "android\\.telephony\\.SmsManager\\.sendTextMessage",
                    "void",
                    new String[]{".*String", ".*String", ".*String", ".*PendingIntent", ".*PendingIntent"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.SEND_SMS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),false, true
            ), // SmsManager.sendTextMessage(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent)
            new TargetVariableInMethodCallParameterAPI(
                    "SmsManager.sendDataMessage",
                    "android\\.telephony\\.SmsManager\\.sendDataMessage",
                    "void",
                    new String[]{".*String", ".*String", ".*short", ".*byte[]", ".*PendingIntent", ".*PendingIntent"},
                    new String[]{".*", ".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.SEND_SMS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),false, true
            ), // SmsManager.sendDataMessage(String destinationAddress, String scAddress, short destinationPort, byte[] data, PendingIntent sentIntent, PendingIntent deliveryIntent)
            new TargetVariableInMethodCallParameterAPI(
                    "SmsManager.sendMultimediaMessage",
                    "android\\.telephony\\.SmsManager\\.sendMultimediaMessage",
                    "void",
                    new String[]{".*Context", ".*Uri", ".*String", ".*Bundle", ".*PendingIntent"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{}, //Does not use the SEND_SMS permission
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),false, true
            ), // SmsManager.sendMultimediaMessage(Context context, Uri contentUri, String locationUrl, Bundle configOverrides, PendingIntent sentIntent)
            new TargetVariableInMethodCallParameterAPI(
                    "SmsManager.sendMultipartTextMessage",
                    "android\\.telephony\\.SmsManager\\.sendMultipartTextMessage",
                    "void",
                    new String[]{".*String", ".*String", ".*ArrayList<String>", ".*ArrayList<PendingIntent>", ".*ArrayList<PendingIntent>"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.SEND_SMS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),false, true
            ), // SmsManager.sendMultipartTextMessage(String destinationAddress, String scAddress, ArrayList<String> parts, ArrayList<PendingIntent> sentIntents, ArrayList<PendingIntent> deliveryIntents)
            new TargetVariableInMethodCallParameterAPI(
                    "SmsManager.sendTextMessageWithoutPersisting",
                    "android\\.telephony\\.SmsManager\\.sendTextMessageWithoutPersisting",
                    "void",
                    new String[]{".*String", ".*String", ".*String", ".*PendingIntent", ".*PendingIntent"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{}, //Does not use the SEND_SMS permission
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),false, true
            ), // SmsManager.sendTextMessageWithoutPersisting(String destinationAddress, String scAddress, String text, PendingIntent sentIntent, PendingIntent deliveryIntent)
            */
    };

    static public PersonalDataAPI [] getAPIList() {
        return personalDataAPIs;
    }
}