package org.intellij.privacyhelper.codeInspection.instances;

import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyhelper.codeInspection.inspections.PersonalDataSinkAPIInspection;
import org.intellij.privacyhelper.codeInspection.inspections.PersonalDataSourceAPIInspection;
import org.intellij.privacyhelper.codeInspection.inspections.ThirdPartyAPIInspection;
import org.intellij.privacyhelper.codeInspection.utils.CoconutAnnotationType;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by tianshi on 4/27/17.
 */
public class PersonalDataInstance {
    // TODO: (long-term) now we only record the source and destination of personal data, but later we will also record the instances that are along the data path propagating from the source to the destination
    public SmartPsiElementPointer psiElementPointer;
    public AnnotationMetaData[] annotationMetaDataList = null;
    public PersonalDataAPI personalDataAPI;
    @NotNull
    private String elementText = "";
    static private PersonalDataAPI[] sourceAPIList = PersonalDataSourceAPIList.getAPIList(false);
    static private PersonalDataAPI[] sinkAPIList = PersonalDataSinkAPIList.getAPIList();
    static private PersonalDataAPI[] thirdPartyAPIList = ThirdPartyAPIList.getAPIList();

    public PersonalDataInstance(SmartPsiElementPointer psiElementPointer, PersonalDataAPI personalDataAPI) {
        this.psiElementPointer = psiElementPointer;
        this.annotationMetaDataList = new AnnotationMetaData[0];
        this.personalDataAPI = personalDataAPI;
        if (psiElementPointer.getElement() != null) {
            this.elementText = psiElementPointer.getElement().getText();
        }
    }

    public PersonalDataInstance(SmartPsiElementPointer psiElementPointer,
                                SmartPsiElementPointer psiAnnotationPointer,
                                AnnotationHolder annotationSpeculation,
                                AnnotationHolder annotationInstance,
                                PersonalDataAPI personalDataAPI) {
        this.psiElementPointer = psiElementPointer;
        this.annotationMetaDataList = new AnnotationMetaData[] {new AnnotationMetaData(annotationInstance, annotationSpeculation, psiAnnotationPointer)};
        this.personalDataAPI = personalDataAPI;
        if (psiElementPointer.getElement() != null) {
            this.elementText = psiElementPointer.getElement().getText();
        }
    }

    public PersonalDataInstance(SmartPsiElementPointer psiElementPointer,
                                AnnotationMetaData [] metaData,
                                PersonalDataAPI personalDataAPI) {
        this.psiElementPointer = psiElementPointer;
        this.annotationMetaDataList = metaData;
        this.personalDataAPI = personalDataAPI;
        if (psiElementPointer.getElement() != null) {
            this.elementText = psiElementPointer.getElement().getText();
        }
    }


    public boolean isValid() {
        if (psiElementPointer.getElement() == null) {
            return false;
        } else {
            if (elementText.equals(psiElementPointer.getElement().getText())) {
                return true;
            } else {
                boolean findNewMatch = false;
                for (PersonalDataAPI api : sourceAPIList) {
                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
                        personalDataAPI = api;
                        elementText = psiElementPointer.getElement().getText();
                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
                            PersonalDataSourceAPIInspection.MyVisitMethodCallExpression(null,
                                    (PsiMethodCallExpression) psiElementPointer.getElement());
                        }
                        findNewMatch = true;
                    }
                }
                for (PersonalDataAPI api : sinkAPIList) {
                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
                        personalDataAPI = api;
                        elementText = psiElementPointer.getElement().getText();
                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
                            PersonalDataSinkAPIInspection.MyVisitMethodCallExpression(null,
                                    (PsiMethodCallExpression) psiElementPointer.getElement());
                        }
                        findNewMatch = true;
                    }
                }
                for (PersonalDataAPI api : thirdPartyAPIList) {
                    if (api.psiElementMethodCallMatched(psiElementPointer.getElement())) {
                        personalDataAPI = api;
                        elementText = psiElementPointer.getElement().getText();
                        if (psiElementPointer.getElement() instanceof PsiMethodCallExpression) {
                            ThirdPartyAPIInspection.MyVisitMethodCallExpression(null,
                                    (PsiMethodCallExpression) psiElementPointer.getElement());
                        }
                        findNewMatch = true;
                    }
                }
                return findNewMatch;
            }
        }
    }

    public SmartPsiElementPointer getPsiElementPointer() {
        return psiElementPointer;
    }

    public AnnotationMetaData[] getAnnotationMetaDataList() {
        return annotationMetaDataList;
    }

    public PersonalDataAPI getPersonalDataAPI() {
        return personalDataAPI;
    }

    // need to make sure that the API is in this group before calling this function
    public String getDescriptionByGroup(PersonalDataGroup group) {
        if (annotationMetaDataList == null) {
            return "";
        }
        if (!personalDataAPI.isThirdPartyAPI) {
            ArrayList<String> descriptionList = new ArrayList<>();
            for (AnnotationMetaData metaData : annotationMetaDataList) {
                if (metaData.annotationInstance != null) {
                    descriptionList.add(metaData.annotationInstance.getDescription());
                }
            }
            return String.join(";", descriptionList);
        } else {
            // Can have access to multiple groups
            ArrayList<String> descriptionList = new ArrayList<>();
            for (AnnotationMetaData metaData : annotationMetaDataList) {
                if (metaData.annotationInstance != null) {
                    descriptionList.add(metaData.annotationInstance.getDescriptionByGroup(group));
                }
            }
            return String.join(";", descriptionList);
        }
    }

    // need to make sure that the API use this permission (either optional or compulsory) before calling this function
    public String getDescriptionByPermission(AndroidPermission permission) {
        if (annotationMetaDataList == null) {
            return "";
        }
        if (!personalDataAPI.isThirdPartyAPI) {
            ArrayList<String> descriptionList = new ArrayList<>();
            for (AnnotationMetaData metaData : annotationMetaDataList) {
                if (metaData.annotationInstance != null) {
                    descriptionList.add(metaData.annotationInstance.getDescription());
                }
            }
            return String.join(";", descriptionList);
        } else {
            ArrayList<String> descriptionList = new ArrayList<>();
            for (AnnotationMetaData metaData : annotationMetaDataList) {
                if (metaData.annotationInstance != null) {
                    descriptionList.add(metaData.annotationInstance.getDescriptionByPermission(permission));
                }
            }
            return String.join(";", descriptionList);
        }
    }

    public void setAnnotationInfo(AnnotationHolder annotation, SmartPsiElementPointer newAnnotationSmartPointer, AnnotationHolder annotationSpeculation) {
        this.annotationMetaDataList = new AnnotationMetaData[] {new AnnotationMetaData(annotation, annotationSpeculation, newAnnotationSmartPointer)};
    }

    public void setAnnotationInfo(AnnotationMetaData [] annotationMetaDataList) {
        this.annotationMetaDataList = annotationMetaDataList;
    }

    public AnnotationMetaData getAnnotationMetadataByType(CoconutAnnotationType annotationType) {
        for (AnnotationMetaData metaData : annotationMetaDataList) {
            if (metaData.getAnnotationType() == annotationType) {
                return metaData;
            }
        }
        return null;
    }

    public boolean belongToPersonalDataGroup(PersonalDataGroup myGroup) {
        for (AnnotationMetaData annotationMetaData : annotationMetaDataList) {
            if (CodeInspectionUtil.annotationTypeToPersonalDataGroupMap.containsKey(annotationMetaData.getAnnotationType()) &&
                    CodeInspectionUtil.annotationTypeToPersonalDataGroupMap.get(annotationMetaData.getAnnotationType()) == myGroup) {
                return true;
            }
        }
        return false;
    }
}
