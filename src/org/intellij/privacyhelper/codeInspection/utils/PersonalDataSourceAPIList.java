package org.intellij.privacyhelper.codeInspection.utils;

import android.content.Intent;
import com.intellij.psi.PsiElement;
import org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils.*;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.*;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * Contains an array of all Personal Data Source API calls that are monitered by Coconut.
 *
 * The list is accessible through two get methods that each return an array of type PersonalDataAPI
 * based on given parameters.
 *
 * Created by tianshi on 4/27/17.
 */
public class  PersonalDataSourceAPIList {

    static PersonalDataAPI[] personalDataAPIs = {
            // TODO: (long-term) for all resources accessed via android\.content\.ContentResolver\.query, check the first parameter to know what type it accessed exactly.
            // TODO: (long-term) automatically extract/require the dev to fill in what field they actually use (e.g. for SMSAnnotation list, they can get data, data sent, smsId, address, type, content, seen, read)

            // TODO (Tiffany): add an entry to capture any access to Calendar data:
            // Example: context.getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null)
            // Use TargetValueFromReturnValueAPI class to define the API.
            // The second (fullAPIName) and the third (returnValueTypeCanonicalText) are regex strings for template
            // matching. The former represents the complete method name, including the package name of the library, and
            // the latter represents the complete name of the return value type.
            // Because we also needs the first parameter (CalendarContract.Events.CONTENT_URI) to determine that it is
            // accessing the calendar data, you'll need to specify some constraints for the parameters with regex. See
            // how the Android ID is handled as a reference.
            // Use CalendarAnnotationUtil for the annotation config field. No need to modify that class for now.
            // Use ReturnValueTargetVariableTracker for the targetVariableTracker.
            new TargetValueFromReturnValueAPI(
                    "CalendarContract.Calendar",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"CalendarContract.*CONTENT_URI", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_CALENDAR}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.CalendarAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CALENDAR},
                    new CalendarAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),

            new TargetValueFromReturnValueAPI(
                    "Telephony.Sms",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"(Telephony.*CONTENT_URI)|(Uri\\.parse\\((.*)sms(.*))", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_SMS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.SMSAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.SMS},
                    new SMSAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),

            new TargetValueFromReturnValueAPI(
                    "ContactsContract.Contacts",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{ "android\\.net\\.Uri", ".*", ".*", ".*", ".*"},
                    new String[]{"ContactsContract.*CONTENT_URI", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.READ_CONTACTS}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.ContactsAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CONTACTS},
                    new ContactsAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),


            new TargetValueFromReturnValueAPI(
                    "LocationManager.getLastKnownLocation",
                    "android\\.location\\.LocationManager\\.getLastKnownLocation",
                    "android\\.location\\.Location",
                    new AndroidPermission[][]{{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.LOCATION},
                    new LocationManagerGetLastKnownLocationAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),
                    true, false
            ),
            new TargetValueFromReturnValueAPI(
                    "FusedLocationProviderApi.getLastLocation",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.getLastLocation",
                    "android\\.location\\.Location",
                    new AndroidPermission[][]{{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.LOCATION},
                    new FusedLocationProviderGetLastLocationAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),
                    true, false
            ),
            new TargetVariableFromCallbackAPI(
                    "FusedLocationProviderClient.getLastLocation",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderClient\\.getLastLocation",
                    ".*",
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.LOCATION},
                    new FusedLocationProviderGetLastLocationAnnotationUtil(),
                    new GMSTaskLocationCallbackTargetVariableTracker(),
                    true, false
            ), // public abstract Location getLastLocation (GoogleApiClient client) The best accuracy available while respecting the location permissions will be returned.
            // TODO: (urgent) Android ID has different scope and resettability before/after Android 8.0
            new TargetVariableFromCallbackAPI("request location update",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{"java\\.lang\\.String", ".*", ".*", "android\\.location\\.LocationListener"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new LocationManagerRequestLocationUpdate1AnnotationUtil(),
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false
            ), // requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener)
            new TargetVariableFromCallbackAPI("request location update",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{".*", ".*", "android\\.location\\.Criteria", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new LocationManagerRequestLocationUpdate2AnnotationUtil(),
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false
            ), // requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("request location update",
                    "android\\.location\\.LocationManager\\.requestLocationUpdates",
                    "void",
                    new String[]{"java\\.lang\\.String", ".*", ".*", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new LocationManagerRequestLocationUpdate1AnnotationUtil(),
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(3),true, false
            ), // requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("request location single update",
                    "android\\.location\\.LocationManager\\.requestSingleUpdate",
                    "void",
                    new String[]{"java\\.lang\\.String", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new LocationManagerRequestSingleUpdate1AnnotationUtil(),
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(1),true, false
            ), // requestSingleUpdate(String provider, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("request location single update",
                    "android\\.location\\.LocationManager\\.requestSingleUpdate",
                    "void",
                    new String[]{"android\\.location\\.Criteria", "android\\.location\\.LocationListener", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new LocationManagerRequestSingleUpdate2AnnotationUtil(),
                    new LMRequestLocationUpdateCallbackTargetVariableTracker(1),true, false
            ), // requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper)
            new TargetVariableFromCallbackAPI("request location update",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderClient\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*LocationRequest", ".*LocationCallback", ".*"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new FusedLocationProviderLocationRequestBasedAnnotationUtil(0),
                    new GMSLocationCallbackTargetVariableTracker(1),true, false
            ), // FusedLocationProviderClient.requestLocationUpdates(LocationRequest request, LocationCallback callback, Looper looper)
//            new TargetVariableFromCallbackAPI(), // FusedLocationProviderClient.requestLocationUpdates(LocationRequest request, PendingIntent callbackIntent)
            new TargetVariableFromCallbackAPI("request location update",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationListener"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new FusedLocationProviderLocationRequestBasedAnnotationUtil(1),
                    new GMSLocationListenerTargetVariableTracker(2),true, false
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener)
            new TargetVariableFromCallbackAPI("request location update",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationCallback", ".*"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new FusedLocationProviderLocationRequestBasedAnnotationUtil(1),
                    new GMSLocationCallbackTargetVariableTracker(2),true, false
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationCallback callback, Looper looper)
            new TargetVariableFromCallbackAPI("request location update",
                    "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                    ".*",
                    new String[]{".*GoogleApiClient", ".*LocationRequest", ".*LocationListener", ".*"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.LocationAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                    new FusedLocationProviderLocationRequestBasedAnnotationUtil(1),
                    new GMSLocationListenerTargetVariableTracker(2),true, false
            ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, LocationListener listener, Looper looper)
            /*
        new TargetVariableFromCallbackAPI(
                "request location update",
                "com\\.google\\.android\\.gms\\.location\\.FusedLocationProviderApi\\.requestLocationUpdates",
                ".*",
                new String[]{".*GoogleApiClient", ".*LocationRequest", ".*PendingIntent"},
                new String[]{".*", ".*", ".*"},
                new AndroidPermission[][] {{AndroidPermission.ACCESS_COARSE_LOCATION, AndroidPermission.ACCESS_FINE_LOCATION}},
                new AndroidPermission[][]{}, false, null,
                CoconutAnnotationType.LocationAnnotation,
                new PersonalDataGroup[] {PersonalDataGroup.LOCATION},
                new FusedLocationProviderLocationRequestBasedAnnotationUtil(1),
                new GMSLocationListenerTargetVariableTracker(2),true, false
        ), // FusedLocationProviderApi.requestLocationUpdates(GoogleApiClient client, LocationRequest request, PendingIntent callbackIntent)
        */
            //TODO: (long-term) String key = LocationManager.KEY_PROXIMITY_ENTERING; Boolean entering = intent.getBooleanExtra(key, false); when addProximityAlert is called



            new TargetValueFromReturnValueAPI(
                    "Android ID",
                    "android\\.provider\\.Settings\\.Secure\\.getString",
                    "java\\.lang\\.String",
                    new String[]{ ".*", "java\\.lang\\.String"},
                    new String[]{".*", ".*ANDROID_ID"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getString(Secure.ANDROID_ID)
            new TargetValueFromReturnValueAPI(
                    "Android ID",
                    "android\\.provider\\.Settings\\.Secure\\.getString",
                    "java\\.lang\\.String",
                    new String[]{ ".*", "java\\.lang\\.String"},
                    new String[]{".*", "\"android_id\""},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getString("android_id")
            new TargetValueFromReturnValueAPI(
                    "UUID",
                    "java\\.util\\.UUID\\.randomUUID",
                    "java\\.util\\.UUID",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // randomUUID()
            new TargetValueFromReturnValueAPI(
                    "GUID (Customed globally unique ID)",
                    ".*\\.GUIDUtil\\.obtainGUID",
                    "java\\.lang\\.String",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            new TargetValueFromReturnValueAPI( // Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones
                    "IMEI for GSM phone, MEID for CDMA phones",
                    "android\\.telephony\\.TelephonyManager\\.getDeviceId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getDeviceId ()
            new TargetValueFromReturnValueAPI( // Returns the unique device ID, for example, the IMEI for GSM and the MEID or ESN for CDMA phones
                    "IMEI for GSM phone, MEID for CDMA phones",
                    "android\\.telephony\\.TelephonyManager\\.getDeviceId",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getDeviceId (int slotIndex)
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not available
                    "IMEI",
                    "android\\.telephony\\.TelephonyManager\\.getImei",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getImei()
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the IMEI (International Mobile Equipment Identity). Return null if IMEI is not available
                    "IMEI",
                    "android\\.telephony\\.TelephonyManager\\.getImei",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getImei(int slotIndex)
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the MEID (Mobile Equipment Identifier). Return null if MEID is not available.
                    "MEID",
                    "android\\.telephony\\.TelephonyManager\\.getMeid",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getMeid()
            new TargetValueFromReturnValueAPI( // added since API level 26: Returns the MEID (Mobile Equipment Identifier). Return null if MEID is not available.
                    "MEID",
                    "android\\.telephony\\.TelephonyManager\\.getMeid",
                    "java\\.lang\\.String",
                    new String[] {"int"},
                    new String[] {".*"},
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), // getMeid(int slotIndex)
            new TargetValueFromReturnValueAPI( // now return a constant value of 02:00:00:00:00:00 after Android 6.0
                    "Wi-Fi MAC Address",
                    "android\\.net\\.wifi\\.WifiInfo\\.getMacAddress",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.ACCESS_WIFI_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            new TargetValueFromReturnValueAPI(
                    "Bluetooth MAC Address",
                    "android\\.bluetooth\\.BluetoothAdapter\\.getAddress",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.BLUETOOTH}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            new TargetValueFromReturnValueAPI(
                    "Line1(Phone) number",
                    "android\\.telephony\\.TelephonyManager\\.getLine1Number", // phone number
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {{AndroidPermission.READ_PHONE_STATE}},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            new TargetValueFromReturnValueAPI(
                    "Google Instance ID",
                    "com\\.google\\.android\\.gms\\.iid\\.InstanceID\\.getId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            new TargetValueFromReturnValueAPI(
                    "Google Advertising ID",
                    "com\\.google\\.android\\.gms\\.ads\\.identifier\\.AdvertisingIdClient\\.Info\\.getId",
                    "java\\.lang\\.String",
                    new AndroidPermission[][] {},
                    new AndroidPermission[][] {},
                    false,
                    null,
                    CoconutAnnotationType.UniqueIdentifierAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.UNIQUE_IDENTIFIER},
                    new UniqueIdentifierAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ),
            /**
             * Items in the below section are based around any type of audio recording.
             *
             * Currently implemented APIs:
             * AudioRecord (captures audio)
             *
             * Note: MediaRecorder (captures audio) can be used to collect audio data simultaneously, so we coded a
             * separate annotation util for it in a later section.
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*short\\[\\]", ".*int", ".*int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.MicrophoneAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.MICROPHONE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.MicrophoneAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts) and AudioRecord.read(short[] audioData, int offsetInShorts, int sizeInShorts, int readMode). readMode specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*float\\[\\]", ".*int", ".*int", ".*int"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.MicrophoneAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.MICROPHONE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.MicrophoneAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: AudioRecord.read(float[] audioData, int offsetInFloats, int sizeInFloats, int readMode)
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*ByteBuffer", ".*int"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.MicrophoneAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.MICROPHONE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.MicrophoneAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: AudioRecord.read(ByteBuffer audioBuffer, int sizeInBytes) and AudioRecord.read(ByteBuffer audioBuffer, int sizeInBytes, int readMode)
            //If you need to handle readMode specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI(
                    "AudioRecord.read",
                    "android\\.media\\.AudioRecord\\.read",
                    "int",
                    new String[]{".*byte\\[\\]", "int", "int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.MicrophoneAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.MICROPHONE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.MicrophoneAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: AudioRecord.read(byte[] audioData, int offsetInBytes, int sizeInBytes) and AudioRecord.read(byte[] audioData, int offsetInBytes, int sizeInBytes, int readMode)
            //If you need to handle readMode specifically, create a new, seperate entry for the longer method.



            /**
             * Items in the below section are based around the APIs used to capture images from the camera
             *
             * Currently implemented APIs:
             * CameraCaptureSession (captures metadata, including location data of pictures)
             * ImageReader (captures images)
             *
             * Note: MediaRecorder (captures video) can be used to collect audio data simultaneously, so we coded a
             * separate annotation util for it in a later section.
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.capture",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.capture",
                    "int",
                    new String[]{".*CaptureRequest", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false
            ), //Method(s) handled: CameraCaptureSession.capture(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureBurst",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureBurst",
                    "int",
                    new String[]{".*", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"}, //TODO: "List<CaptureBurst>" does not work as a parameter regex here. Neither does "List\\<CaptureBurst\\>" Having this parameter is not essential to the code function, but it would be nice to know how to do this. This applies to other CameraCaptureSession methods as well
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false
            ), //Method(s) handled: CameraCaptureSession.captureBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureBurstRequests",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureBurstRequests",
                    "int",
                    new String[]{".*", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false
            ), //Method(s) handled: CameraCaptureSession.captureBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.captureSingleRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.captureSingleRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false
            ), //Method(s) handled: CameraCaptureSession.captureSingleRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingBurst",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingBurst",
                    "int",
                    new String[]{".*", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false
            ), //Method(s) handled: CameraCaptureSession.setRepeatingBurst(List<CaptureRequest> requests, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingBurstRequests",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingBurstRequests",
                    "int",
                    new String[]{".*", ".*Executor", ".*CameraCaptureSession\\.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false
            ), //Method(s) handled: CameraCaptureSession.setRepeatingBurstRequests(List<CaptureRequest> requests, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setRepeatingRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setRepeatingRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*CameraCaptureSession\\.CaptureCallback", ".*Handler"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(1),true, false
            ), //Method(s) handled: CameraCaptureSession.setRepeatingRequest(CaptureRequest request, CameraCaptureSession.CaptureCallback listener, Handler handler)
            new TargetVariableFromCallbackAPI(
                    "CameraCaptureSession.setSingleRepeatingRequest",
                    "android\\.hardware\\.camera2\\.CameraCaptureSession\\.setSingleRepeatingRequest",
                    "int",
                    new String[]{".*CaptureRequest", ".*Executor", ".*CameraCaptureSession.CaptureCallback"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{{AndroidPermission.CAMERA}},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new CameraUpdateCallbackTargetVariableTracker(2),true, false
            ), //Method(s) handled: CameraCaptureSession.setSingleRepeatingRequest(CaptureRequest request, Executor executor, CameraCaptureSession.CaptureCallback listener)
            new TargetValueFromReturnValueAPI(
                    "ImageReader.acquireNextImage",
                    "android\\.media\\.ImageReader\\.acquireNextImage",
                    "android.media.Image",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ImageReader.acquireNextImage()
            new TargetValueFromReturnValueAPI(
                    "ImageReader.acquireLatestImage",
                    "android\\.media\\.ImageReader\\.acquireLatestImage",
                    "android.media.Image",
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.CameraAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.CAMERA},
                    new CameraAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ImageReader.acquireLatestImage()

            /**
             * This section handles all sensor API calls.
             *
             * Current APIs implemented -
             * SensorManager
             *
             */
            new TargetVariableFromCallbackAPI("SensorManager.registerListener",
                    "android\\.hardware\\.SensorManager\\.registerListener",
                    "boolean",
                    new String[]{".*SensorEventListener", ".*Sensor", ".*int"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.SensorAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.SENSORS},
                    new SensorAnnotationUtil(),
                    new SensorCallbackTargetVariableTracker(0),true, false
            ), //Method(s) handled: SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, Handler handler)
            //and SensorManager.registerListener(SensorEventListener listener, Sensor sensor, int samplingPeriodUs, int maxReportLatencyUs, Handler handler)
            new TargetVariableFromCallbackAPI("SensorManager.requestTriggerSensor",
                    "android\\.hardware\\.SensorManager\\.requestTriggerSensor",
                    "boolean",
                    new String[]{".*TriggerEventListener", ".*Sensor"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.SensorAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.SENSORS},
                    new SensorAnnotationUtil(),
                    new SensorCallbackTargetVariableTracker(0),true, false
            ), //Method(s) handled: SensorManager.requestTriggerSensor(TriggerEventListener listener, Sensor sensor)
            new TargetVariableInMethodCallParameterAPI("SensorManager.createDirectChannel",
                    "android\\.hardware\\.SensorManager\\.createDirectChannel",
                    "android\\.hardware\\.SensorDirectChannel",
                    new String[]{"android\\.os\\.MemoryFile"},
                    new String[]{".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.SensorAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.SENSORS},
                    new SensorAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: SensorManager.createDirectChannel(MemoryFile mem)
            new TargetVariableInMethodCallParameterAPI("SensorManager.createDirectChannel",
                    "android\\.hardware\\.SensorManager\\.createDirectChannel",
                    "android\\.hardware\\.SensorDirectChannel",
                    new String[]{"android\\.hardware\\.HardwareBuffer"},
                    new String[]{".*"},
                    new AndroidPermission[][] {},
                    new AndroidPermission[][]{}, false, null,
                    CoconutAnnotationType.SensorAnnotation,
                    new PersonalDataGroup[] {PersonalDataGroup.SENSORS},
                    new SensorAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: SensorManager.createDirectChannel(HardwareBuffer mem)


            /**
             * This section handles all User File API calls.
             *
             * Current APIs implemented -
             * ShareCompat.IntentReader
             * MediaPlayer
             * ContentResolver
             *
             * Note: Activity.startActivityForResult can be used to access multiple types of data and is handled
             * separately in a later section.
            */
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getEmailBcc",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getEmailBcc",
                    "java\\.lang\\.String\\[\\]",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getEmailBcc()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getEmailCc",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getEmailCc",
                    "java\\.lang\\.String\\[\\]",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getEmailCc()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getEmailTo",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getEmailTo",
                    "java\\.lang\\.String\\[\\]",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getEmailTo()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getHtmlText",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getHtmlText",
                    "java\\.lang\\.String",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getHtmlText()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getStream",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getStream",
                    "android\\.net\\.Uri",
                    new String[]{"int"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getStream(int index)
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getStream",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getStream",
                    "android\\.net\\.Uri",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getStream()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getSubject",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getSubject",
                    "java\\.lang\\.String",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getSubject()
            new TargetValueFromReturnValueAPI("ShareCompat.IntentReader.getText",
                    "android\\.support\\.v4\\.app\\.ShareCompat\\.IntentReader\\.getText",
                    "java\\.lang\\.String",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: ShareCompat.IntentReader.getText()
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.addTimedTextSource",
                    "android\\.media\\.MediaPlayer\\.addTimedTextSource",
                    "void",
                    new String[]{".*FileDescriptor", ".*String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.addTimedTextSource(FileDescriptor fd, String mimeType)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.addTimedTextSource",
                    "android\\.media\\.MediaPlayer\\.addTimedTextSource",
                    "void",
                    new String[]{".*String", ".*String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.addTimedTextSource(String path, String mimeType)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.addTimedTextSource",
                    "android\\.media\\.MediaPlayer\\.addTimedTextSource",
                    "void",
                    new String[]{".*FileDescriptor", "long", "long", ".*String"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.addTimedTextSource(FileDescriptor fd, long offset, long length, String mime)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.addTimedTextSource",
                    "android\\.media\\.MediaPlayer\\.addTimedTextSource",
                    "void",
                    new String[]{"android\\.content\\.Context", "android\\.net\\.Uri", ".*String"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),true, false
            ), //Method(s) handled: MediaPlayer.addTimedTextSource(Context context, Uri uri, String mimeType)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.create",
                    "android\\.media\\.MediaPlayer\\.create",
                    "android\\.media\\.MediaPlayer",
                    new String[]{"android\\.content\\.Context", "int"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),true, false
            ), //Method(s) handled: MediaPlayer.create(Context context, int resid) and MediaPlayer.create(Context context, int resid, AudioAttributes audioAttributes, int audioSessionId)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.create",
                    "android\\.media\\.MediaPlayer\\.create",
                    "android\\.media\\.MediaPlayer",
                    new String[]{"android\\.content\\.Context", "android\\.net\\.Uri"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),true, false
            ), //Method(s) handled: MediaPlayer.create(Context context, Uri uri) and MediaPlayer.create(Context context, Uri uri, SurfaceHolder holder)
            //and MediaPlayer.create(Context context, Uri uri, SurfaceHolder holder, AudioAttributes audioAttributes, int audioSessionId)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.setDataSource",
                    "android\\.media\\.MediaPlayer\\.setDataSource",
                    "void",
                    new String[]{"android\\.content\\.res\\.AssetFileDescriptor"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.setDataSource(AssetFileDescriptor afd)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.setDataSource",
                    "android\\.media\\.MediaPlayer\\.setDataSource",
                    "void",
                    new String[]{"java\\.io\\.FileDescriptor"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.setDataSource(FileDescriptor fd) and MediaPlayer.setDataSource(FileDescriptor fd, long offset, long length)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.setDataSource",
                    "android\\.media\\.MediaPlayer\\.setDataSource",
                    "void",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.setDataSource(String path)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.setDataSource",
                    "android\\.media\\.MediaPlayer\\.setDataSource",
                    "void",
                    new String[]{"android\\.media\\.MediaDataSource"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaPlayer.setDataSource(MediaDataSource dataSource)
            new TargetVariableInMethodCallParameterAPI("MediaPlayer.setDataSource",
                    "android\\.media\\.MediaPlayer\\.setDataSource",
                    "void",
                    new String[]{"android\\.content\\.Context", "android\\.net\\.Uri"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(1),true, false
            ), //Method(s) handled: MediaPlayer.setDataSource(Context context, Uri uri) and MediaPlayer.setDataSource(Context context, Uri uri, Map<String, String> headers)
            //and MediaPlayer.setDataSource(Context context, Uri uri, Map<String, String> headers, List<HttpCookie> cookies)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openAssetFile",
                    "android\\.content\\.ContentResolver\\.openAssetFile",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openAssetFile(Uri uri, String mode, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openAssetFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openAssetFileDescriptor",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openAssetFileDescriptor(Uri uri, String mode) and ContentResolver.openAssetFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openFile",
                    "android\\.content\\.ContentResolver\\.openFile",
                    "android\\.os\\.ParcelFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openFile(Uri uri, String mode, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openFileDescriptor",
                    "android\\.os\\.ParcelFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openFileDescriptor(Uri uri, String mode) and ContentResolver.openFileDescriptor(Uri uri, String mode, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openInputStream",
                    "android\\.content\\.ContentResolver\\.openInputStream",
                    "java\\.io\\.InputStream",
                    new String[]{"android\\.net\\.Uri"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openInputStream(Uri uri)
            new TargetValueFromReturnValueAPI("ContentResolver.openOutputStream",
                    "android\\.content\\.ContentResolver\\.openOutputStream",
                    "java\\.io\\.OutputStream",
                    new String[]{"android\\.net\\.Uri"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openOutputStream(Uri uri) and ContentResolver.openOutputStream(Uri uri, String mode)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.openTypedAssetFile",
                    "android\\.content\\.ContentResolver\\.openTypedAssetFile",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.Bundle", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openTypedAssetFile(Uri uri, String mimeTypeFilter, Bundle opts, CancellationSignal signal)
            new TargetValueFromReturnValueAPI("ContentResolver.openTypedAssetFileDescriptor",
                    "android\\.content\\.ContentResolver\\.openTypedAssetFileDescriptor",
                    "android\\.content\\.res\\.AssetFileDescriptor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String", "android\\.os\\.Bundle"},
                    new String[]{".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts) and ContentResolver.openTypedAssetFileDescriptor(Uri uri, String mimeType, Bundle opts, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetValueFromReturnValueAPI("ContentResolver.query",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String\\[\\]", "android\\.os\\.Bundle", "android\\.os\\.CancellationSignal"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.query(Uri uri, String[] projection, Bundle queryArgs, CancellationSignal cancellationSignal)
            new TargetValueFromReturnValueAPI("ContentResolver.query",
                    "android\\.content\\.ContentResolver\\.query",
                    "android\\.database\\.Cursor",
                    new String[]{"android\\.net\\.Uri", "java\\.lang\\.String\\[\\]", "java\\.lang\\.String", "java\\.lang\\.String\\[\\]", "java\\.lang\\.String"},
                    new String[]{".*", ".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new ReturnValueTargetVariableTracker(),true, false
            ), //Method(s) handled: ContentResolver.query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder)
            //and ContentResolver.query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder, CancellationSignal cancellationSignal)
            //If you need to handle the longer method specifically, create a new, seperate entry for the longer method.
            new TargetVariableInMethodCallParameterAPI("ContentResolver.query",
                    "android\\.content\\.ContentResolver\\.query",
                    "int",
                    new String[]{"android\\.net\\.Uri", "android\\.content\\.ContentValues", "java\\.lang\\.String", "java\\.lang\\.String\\[\\]"},
                    new String[]{".*", ".*", ".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserFileAnnotation),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: ContentResolver.update(Uri uri, ContentValues values, String where, String[] selectionArgs)

            /**
             * This section handles all Personally identifiable information (PII) API calls.
             *
             * Current APIs implemented -
             *
            */

            /**
             * This section handles all User Input API calls
             *
             * Current APIs implemented -
             * EditText
             *
            */
            new TargetValueFromReturnValueAPI("EditText.getText",
                    "android\\.widget\\.EditText\\.getText",
                    "android\\.text\\.Editable",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserInputAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_INPUT},
                    new CreateEmptyAnnotationByTypeUtil(CoconutAnnotationType.UserInputAnnotation),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: EditText.getText()

            /**
             * These APIs were originally handled as UserFile API calls, but since we've decided that they should not be included.
             * All APIs are calls on an Intent object
             * I'm listing them here for future reference.
             *
             * Elijah
            */
            /*
            new TargetValueFromReturnValueAPI("Intent.describeContents",
                    "android\\.content\\.Intent\\.describeContents",
                    "int",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.describeContents()
            new TargetValueFromReturnValueAPI("Intent.getBooleanArrayExtra",
                    "android\\.content\\.Intent\\.getBooleanArrayExtra",
                    "boolean\\[\\]", //TODO: This won't register. We need to determine how to handle return types of arrays
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getBooleanArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getBooleanExtra",
                    "android\\.content\\.Intent\\.getBooleanExtra",
                    "android\\.os\\.Bundle",
                    new String[]{"java\\.lang\\.String", "boolean"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getBooleanExtra(String name, boolean defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getBundleExtra",
                    "android\\.content\\.Intent\\.getBundleExtra",
                    "android\\.os\\.Bundle",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getBundleExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getByteArrayExtra",
                    "android\\.content\\.Intent\\.getByteArrayExtra",
                    "byte\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getByteArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getByteExtra",
                    "android\\.content\\.Intent\\.getByteExtra",
                    "byte",
                    new String[]{"java\\.lang\\.String", "byte"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getByteExtra(String name, byte defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getCharArrayExtra",
                    "android\\.content\\.Intent\\.getCharArrayExtra",
                    "char\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getCharArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getCharExtra",
                    "android\\.content\\.Intent\\.getCharExtra",
                    "char",
                    new String[]{"java\\.lang\\.String", "char"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getCharExtra(String name, char defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getCharSequenceArrayExtra",
                    "android\\.content\\.Intent\\.getCharSequenceArrayExtra",
                    "java\\.lang\\.CharSequence",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getCharSequenceArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getCharSequenceArrayListExtra",
                    "android\\.content\\.Intent\\.getCharSequenceArrayListExtra",
                    "java\\.util\\.ArrayList<java\\.lang\\.CharSequence>",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getCharSequenceArrayListExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getCharSequenceExtra",
                    "android\\.content\\.Intent\\.getCharSequenceExtra",
                    "java\\.lang\\.CharSequence",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getCharSequenceExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getClipData",
                    "android\\.content\\.Intent\\.getClipData",
                    "android\\.content\\.ClipData",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getClipData()
            new TargetValueFromReturnValueAPI("Intent.getData",
                    "android\\.content\\.Intent\\.getData",
                    "android\\.net\\.Uri",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getData()
            new TargetValueFromReturnValueAPI("Intent.getDataString",
                    "android\\.content\\.Intent\\.getDataString",
                    "java\\.lang\\.String",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getDataString()
            new TargetValueFromReturnValueAPI("Intent.getDoubleArrayExtra",
                    "android\\.content\\.Intent\\.getDoubleArrayExtra",
                    "double\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getDoubleArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getDoubleExtra",
                    "android\\.content\\.Intent\\.getDoubleExtra",
                    "double",
                    new String[]{"java\\.lang\\.String", "double"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getDoubleExtra(String name, double defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getExtras",
                    "android\\.content\\.Intent\\.getExtras",
                    "android\\.os\\.Bundle",
                    new String[]{},
                    new String[]{},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getExtras()
            new TargetValueFromReturnValueAPI("Intent.getFloatArrayExtra",
                    "android\\.content\\.Intent\\.getFloatArrayExtra",
                    "float\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getFloatArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getFloatExtra",
                    "android\\.content\\.Intent\\.getFloatExtra",
                    "float",
                    new String[]{"java\\.lang\\.String", "float"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getFloatExtra(String name, float defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getIntArrayExtra",
                    "android\\.content\\.Intent\\.getIntArrayExtra",
                    "int\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getIntArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getIntExtra",
                    "android\\.content\\.Intent\\.getIntExtra",
                    "int",
                    new String[]{"java\\.lang\\.String", "int"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getIntExtra(String name, int defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getIntegerArrayListExtra",
                    "android\\.content\\.Intent\\.getIntegerArrayListExtra",
                    "java\\.util\\.ArrayList<java\\.lang\\.Integer>",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getIntegerArrayListExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getLongArrayExtra",
                    "android\\.content\\.Intent\\.getLongArrayExtra",
                    "long\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getLongArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getLongExtra",
                    "android\\.content\\.Intent\\.getLongExtra",
                    "long",
                    new String[]{"java\\.lang\\.String", "long"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getLongExtra(String name, long defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getParcelableArrayExtra",
                    "android\\.content\\.Intent\\.getParcelableArrayExtra",
                    "android\\.os\\.Parcelable\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getParcelableArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getParcelableArrayListExtra",
                    "android\\.content\\.Intent\\.getParcelableArrayListExtra",
                    "java\\.util\\.ArrayList<.*>",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getParcelableArrayListExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getParcelableExtra",
                    "android\\.content\\.Intent\\.getParcelableExtra",
                    ".*",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getParcelableExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getSerializableExtra",
                    "android\\.content\\.Intent\\.getSerializableExtra",
                    "java\\.io\\.Serializable",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getSerializableExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getShortArrayExtra",
                    "android\\.content\\.Intent\\.getShortArrayExtra",
                    "short\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getShortArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getShortExtra",
                    "android\\.content\\.Intent\\.getShortExtra",
                    "short",
                    new String[]{"java\\.lang\\.String", "short"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getShortExtra(String name, short defaultValue)
            new TargetValueFromReturnValueAPI("Intent.getStringArrayExtra",
                    "android\\.content\\.Intent\\.getStringArrayExtra",
                    "java\\.lang\\.String\\[\\]",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getStringArrayExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.getStringArrayListExtra",
                    "android\\.content\\.Intent\\.getStringArrayListExtra",
                    "java\\.util\\.ArrayList<java\\.lang\\.String>",
                    new String[]{"java\\.lang\\.String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.getStringArrayListExtra(String name)
            new TargetValueFromReturnValueAPI("Intent.toUri",
                    "android\\.content\\.Intent\\.toUri",
                    "java\\.lang\\.String",
                    new String[]{"int"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    CoconutAnnotationType.UserFileAnnotation,
                    new PersonalDataGroup[]{PersonalDataGroup.USER_FILE},
                    new UserFileAnnotationUtil(),
                    new ReturnValueTargetVariableTracker(), true, false
            ), //Method(s) handled: Intent.toUri(int flags)
            */

            /**
             * Items in the below section handles MediaRecorder APIs used to capture audios from the microphone and
             * videos from the camera
            */
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*FileDescriptor"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    null,
                    new PersonalDataGroup[]{},
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaRecorder.setOutputFile(FileDescriptor fd)
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*String"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    null,
                    new PersonalDataGroup[]{},
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaRecorder.setOutputFile(String path)
            new TargetVariableInMethodCallParameterAPI(
                    "MediaRecorder.setOutputFile",
                    "android\\.media\\.MediaRecorder\\.setOutputFile",
                    "void",
                    new String[]{".*File"},
                    new String[]{".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    null,
                    new PersonalDataGroup[]{},
                    new MediaRecorderAnnotationUtil(),
                    new PersonalDataInMethodCallParameterTargetVariableTracker(0),true, false
            ), //Method(s) handled: MediaRecorder.setOutputFile(File file)

            /**
             * Items in the below section handles startActivityForResult APIs used to capture a variety of personal data,
             * including camera data, microphone data, user files, etc.
            */
            new TargetVariableInIntentResultAPI(
                    "startActivityForResult",
                    ".*startActivityForResult",
                    "void",
                    new String[]{".*Intent", ".*"},
                    new String[]{".*", ".*"},
                    new AndroidPermission[][]{},
                    new AndroidPermission[][]{},
                    false, null,
                    null,
                    new PersonalDataGroup[]{},
                    new ActivityForResultAnnotationUtil(),
                    new StartActivityForResultTargetVariableTracker(),true, false
            ), //Method(s) handled: Activity.startActivityForResult(Intent intent, int requestCode) and Activity.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //and FragmentActivity.startActivityForResult(Intent intent, int requestCode) and FragmentActivity.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //and ActivityCompat.startActivityForResult(Intent intent, int requestCode) and ActivityCompat.startActivityForResult(Intent intent, int requestCode, Bundle options)
            //Intents for User Data should be - Intent.ACTION_PICK, Intent.ACTION_GET_CONTENT, Intent.ACTION_GET_DOCUMENT, Intent.ACTION_GET_DOCUMENT_TREE

    };

    static public PersonalDataAPI[] getAPIListByDataGroup(PersonalDataGroup group, boolean isThirdParty) {
        ArrayList<PersonalDataAPI> apiList = new ArrayList<>();
        for (PersonalDataAPI api : personalDataAPIs) {
            if (isThirdParty == api.isThirdPartyAPI && Arrays.asList(api.getGroups()).contains(group)) {
                apiList.add(api);
            }
        }
        return apiList.toArray(new PersonalDataAPI[0]);
    }

    static public PersonalDataAPI[] getAPIListByDataGroups(PersonalDataGroup [] groups, boolean isThirdParty) {
        ArrayList<PersonalDataAPI> apiList = new ArrayList<>();
        for (PersonalDataGroup group : groups) {
            for (PersonalDataAPI api : personalDataAPIs) {
                if (isThirdParty == api.isThirdPartyAPI && Arrays.asList(api.getGroups()).contains(group)) {
                    apiList.add(api);
                }
            }
        }
        return apiList.toArray(new PersonalDataAPI[0]);
    }

    public static PersonalDataAPI[] getAPIList(boolean isThirdParty) {
        ArrayList<PersonalDataAPI> apiList = new ArrayList<>();
        for (PersonalDataAPI api : personalDataAPIs) {
            if (isThirdParty == api.isThirdPartyAPI) {
                apiList.add(api);
            }
        }
        return apiList.toArray(new PersonalDataAPI[0]);
    }
}
