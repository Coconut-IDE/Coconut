package org.intellij.privacyhelper.codeInspection.personalDataAPIAnnotationUtils;

import com.intellij.psi.*;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiElementFilter;
import com.intellij.psi.util.PsiTreeUtil;
import org.intellij.privacyhelper.codeInspection.personalDataEntityTrackers.TargetVariableTrackerUtil;

import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Created by tiffany on 2/21/19.
 */
public class QueryUtils {
    public static PsiElement getResolvedValue(PsiElement source) {
        PsiElement projectionElement = TargetVariableTrackerUtil.getResolvedVariable(source);

        PsiReference[] referenceCollections = ReferencesSearch.search(projectionElement).findAll().toArray(new PsiReference[0]);
        boolean beforeSource = false;
        for (int i = referenceCollections.length - 1; i >= 0; i--) {
            PsiElement psiElement = referenceCollections[i].getElement();

            if (!beforeSource && psiElement.equals(source)) {
                beforeSource = true;
                continue;
            }

            if (beforeSource) {
                PsiAssignmentExpression assignmentExpression = PsiTreeUtil.getParentOfType(psiElement, PsiAssignmentExpression.class);
                if (assignmentExpression != null) {
                    if (assignmentExpression.getRExpression() instanceof PsiNewExpression
                            || assignmentExpression.getRExpression() instanceof PsiPolyadicExpression) {
                        return assignmentExpression.getRExpression();
                    } else if (assignmentExpression.getRExpression() instanceof PsiReferenceExpression &&
                            !assignmentExpression.getRExpression().equals(source)) {
                        return getResolvedValue(assignmentExpression.getRExpression());
                    }
                }
            }
        }
        // Then we check the definition of the local variable/the field
        PsiExpression initValue = PsiTreeUtil.getChildOfType(projectionElement, PsiExpression.class);
        if (initValue instanceof PsiNewExpression || initValue instanceof PsiPolyadicExpression) {
            return initValue;
        } else if (initValue instanceof PsiReferenceExpression) {
            return getResolvedValue(initValue);
        }
        return null;
    }

    // TODO: make this globally accessible
    private static boolean matchCrossLines(String pattern, String target) {
        return Pattern.compile(pattern, Pattern.DOTALL).matcher(target).matches();
    }

    // TODO: also handle literal strings (e.g. "calendar_displayName" for "CalendarContract.Calendars.CALENDAR_DISPLAY_NAME")
    static void updateCalendarDataTypeSpeculationsFromStringList(PsiExpression[] array, ArrayList<String> dataTypeSpeculations) {
        for (PsiExpression item : array) {
            if (item == null) {
                continue;
            }
            String itemString = item.getText();
            if (itemString == null) {
                continue;
            }
            //CalendarsColumn
            if (matchCrossLines("(.*)CalendarContract\\.Calendars\\.CALENDAR_DISPLAY_NAME(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.CALENDAR_DISPLAY_NAME");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Calendars\\.OWNER_ACCOUNT(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.CALENDAR_OWNER");
            }

            //EventsColumn
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.TITLE(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_TITLE");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.CALENDAR_ID(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_CALENDAR_ID");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.DESCRIPTION(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_DESCRIPTION");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.LOCATION(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_LOCATION");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.ORGANIZER(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_ORGANIZER");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.DTSTART(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.DTEND(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.DURATION(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.ALL_DAY(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.RRULE(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.RDATE(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.EXRULE(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.EXDATE(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_TIME");
            }
            if (matchCrossLines("(.*)CalendarContract\\.Events\\.DTSTART(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.DTEND(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.DURATION(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Events\\.ALL_DAY(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.EVENT_TIME");
            }

            //AttendeesColumn
            if (matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE_EMAIL(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE_IDENTITY(.*)", itemString)
                    || matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE_NAME(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.ATTENDEE_INFO");
            }

            if (matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE_RELATIONSHIP(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.ATTENDEE_RELATIONSHIP");
            }

            if (matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE STATUS(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.ATTENDEE_STATUS");
            }

            if (matchCrossLines("(.*)CalendarContract\\.Attendees\\.ATTENDEE_TYPE(.*)", itemString)) {
                dataTypeSpeculations.add("CalendarDataType.ATTENDEE_TYPE");
            }
        }
    }

    // TODO: also handle literal strings (e.g. "calendar_displayName" for "CalendarContract.Calendars.CALENDAR_DISPLAY_NAME")
    static void updateContactsDataTypeSpeculationsFromStringList(PsiExpression[] array, ArrayList<String> dataTypeSpeculations) {
        for (PsiExpression item : array) {
            if (item == null) {
                continue;
            }
            String itemString = item.getText();
            if (itemString == null) {
                continue;
            }
            if (matchCrossLines("(.*)ContactsContract\\.(.*)RAW_CONTACT_ID(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.RAW_CONTACT_ID");
            }

            if (matchCrossLines("(.*)ContactsContract\\.(.*)\\._ID(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.ID");
            }

            if (matchCrossLines("(.*)ContactsContract\\.(.*)\\.LOOKUP_KEY(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.LOOKUP_KEY");
            }

            if (matchCrossLines("(.*)ContactsContract\\.(.*)PHOTO_(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.PHOTO");
            }

            if (matchCrossLines("(.*)ContactsContract\\.(.*)DISPLAY_NAME(.*)", itemString)
                    || matchCrossLines("(.*)ContactsContract\\.*\\.ACCOUNT_NAME(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.DISPLAY_NAME");
            }

            if (matchCrossLines("(.*)ContactsContract\\.(.*)\\.SOURCE_ID(.*)", itemString)) {
                dataTypeSpeculations.add("ContactsDataType.SOURCE_ID");
            }

        }
    }

    // TODO: also handle literal strings (e.g. "calendar_displayName" for "CalendarContract.Calendars.CALENDAR_DISPLAY_NAME")
    static void updateSmsDataTypeSpeculationsFromStringList(PsiExpression[] array, ArrayList<String> dataTypeSpeculations) {
        for (PsiExpression item : array) {
            if (item == null) {
                continue;
            }
            String itemString = item.getText();
            if (itemString == null) {
                continue;
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.CONTENT_LOCATION(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.CONTENT_LOCATION");
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.ADDRESS(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.ADDRESS");
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.CREATOR(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.CREATOR");
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.DATE(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.DELIVERY_TIME(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.DATE_AND_TIME");
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.MESSAGE_ID(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.MESSAGE_BOX(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.SUBJECT(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.RETRIEVE_TEXT(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.RESPONSE_TEXT(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.BODY(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.MESSAGES");
            }

            if (matchCrossLines("(.*)Telephony\\.(.*)\\.SEEN(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.READ(.*)", itemString)
                    || matchCrossLines("(.*)Telephony\\.(.*)\\.RESPONSE_STATUS(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.STATUS");
            }

            if (matchCrossLines("(.*)Telephony\\.Threads\\.(.*)", itemString)) {
                dataTypeSpeculations.add("SMSDataType.THREADS");
            }

        }
    }

    static void updateCallLogsDataTypeSpeculationsFromStringList(PsiExpression[] array, ArrayList<String> dataTypeSpeculations) {

        for (PsiExpression item : array) {
            if (item == null) {
                continue;
            }
            String itemString = item.getText();
            if (itemString == null) {
                continue;
            }

            if (matchCrossLines("(.*)Calls\\.CACHED(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.CACHED_DATA");
            }

            if (matchCrossLines("(.*)Calls\\.getLastOutgoingCall(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.LAST_OUTGOING_CALL");
            }

            if (matchCrossLines("(.*)Calls\\.DATE(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.DATE");
            }

            if (matchCrossLines("(.*)Calls\\.NUMBER(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.NUMBER");
            }

            if (matchCrossLines("(.*)Calls\\.DURATION(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.DURATION");
            }

            if (matchCrossLines("(.*)Calls\\.VOICEMAIL_URI(.*)", itemString) || matchCrossLines("(.*)Calls\\.TRANSCRIPTION(.*)", itemString)) {
                dataTypeSpeculations.add("CallLogsDataType.VOICEMAIL_DATA");
            }
        }
    }
}
