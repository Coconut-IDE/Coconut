package org.intellij.privacyhelper.codeInspection.state;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import org.intellij.privacyhelper.codeInspection.instances.*;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.intellij.privacyhelper.codeInspection.annotations.AnnotationHolder;
import org.intellij.privacyhelper.codeInspection.annotations.PrivacyNoticeAnnotationHolder;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by tianshi on 4/26/17.
 */
public class PersonalDataHolder {

    private Vector<PersonalDataInstance> personalDataInstances = new Vector<>();
    private Vector<AndroidPermissionInstance> permissionInstances = new Vector<>();
    private HashMap<SmartPsiElementPointer, Boolean> annotationFieldIsComplete = new HashMap<>();

    private static Map<String, PersonalDataHolder> ourInstances = null;

    private Project openProject = null;

    static private void addProject(final Project project) {
        if(ourInstances == null) {
            ourInstances = new HashMap<String, PersonalDataHolder>();
        }

        final String key = getProjectKey(project);

        if(!ourInstances.containsKey(key)) {
            ourInstances.put(key, new PersonalDataHolder(project));
        }
    }

    static public PersonalDataHolder getInstance(final Project holderProject) {
        if (ourInstances == null) {
            ourInstances = new HashMap<String, PersonalDataHolder>();
        }

        addProject(holderProject);
        return ourInstances.get(getProjectKey(holderProject));
    }

    private static String getProjectKey(final Project project) {
        return project.getBasePath() + ":" + project.getName();
    }

    private PersonalDataHolder(final Project project){
        openProject = project;
    }

    private void cleanupInvalidAnnotationFieldIsCompleteInstance() {
        // FIXME: java.util.ConcurrentModificationException
//        annotationFieldIsComplete.entrySet().removeIf(entry -> entry.getKey().getElement() == null);
        annotationFieldIsComplete.entrySet().removeIf(entry -> entry.getKey() == null || entry.getKey().getElement() == null);

    }

    public void setAnnotationFieldIsComplete(PsiElement element, boolean errorSeverity) {
        cleanupInvalidAnnotationFieldIsCompleteInstance();
        SmartPsiElementPointer psiElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
        annotationFieldIsComplete.put(psiElementPointer, errorSeverity);
    }

    public boolean getAnnotationFieldIsComplete(PsiElement element) {
        cleanupInvalidAnnotationFieldIsCompleteInstance();
        SmartPsiElementPointer psiElementPointer = SmartPointerManager.getInstance(element.getProject()).createSmartPsiElementPointer(element);
        // If the element is not in the map, we consider it as the lowest severity
        return annotationFieldIsComplete.getOrDefault(psiElementPointer, true);
    }

    private void cleanupInvalidInstances() {
        // personal data API calls
        personalDataInstances.removeIf(personalDataAPICallInstance -> !personalDataAPICallInstance.isValid());
        // permissions
        permissionInstances.removeIf(permissionInstance -> !permissionInstance.isValid());
    }

    public Vector<PersonalDataInstance> getSourceAPICallInstances() {
        cleanupInvalidInstances();
        Vector<PersonalDataInstance> instances = new Vector<>();
        for (PersonalDataInstance instance : personalDataInstances) {
            if (instance.getPersonalDataAPI().isPersonalDataSource()) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public Vector<PersonalDataInstance> getSinkAPICallInstances() {
        cleanupInvalidInstances();
        Vector<PersonalDataInstance> instances = new Vector<>();
        for (PersonalDataInstance instance : personalDataInstances) {
            if (instance.getPersonalDataAPI().isPersonalDataSink()) {
                instances.add(instance);
            }
        }
        return instances;
    }

    public PersonalDataInstance addPersonalDataInstance(PsiElement expression, PersonalDataAPI api) {
        cleanupInvalidInstances();
        if (openProject == null) {
            // TODO: (long-term) support multiple projects
            openProject = expression.getProject();
        }
        SmartPsiElementPointer newAPISmartPointer =
                SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
        for (PersonalDataInstance instance : personalDataInstances) {
            if (instance.psiElementPointer.equals(newAPISmartPointer)) {
                return instance;
            }
        }
        PersonalDataInstance instance = new PersonalDataInstance(
                newAPISmartPointer, api);
        personalDataInstances.add(instance);
        return instance;
    }

    public PersonalDataInstance addPersonalDataInstance(PsiElement expression, PsiAnnotation psiAnnotation,
                                                        AnnotationHolder annotationSpeculatedFromAPICall, AnnotationHolder annotation,
                                                        PersonalDataAPI api) {
        cleanupInvalidInstances();
        if (openProject == null) {
            // TODO: (long-term) support multiple projects
            openProject = expression.getProject();
        }
        SmartPsiElementPointer newAPISmartPointer =
                SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
        SmartPsiElementPointer newAnnotationSmartPointer = null;
        if (psiAnnotation != null) {
            newAnnotationSmartPointer = SmartPointerManager.getInstance(psiAnnotation.getProject()).createSmartPsiElementPointer(psiAnnotation);
        }
        for (PersonalDataInstance instance : personalDataInstances) {
            if (instance.psiElementPointer.equals(newAPISmartPointer)) {
                // Already had the annotation, just update annotation content
                instance.setAnnotationInfo(annotation, newAnnotationSmartPointer, annotationSpeculatedFromAPICall);
                return instance;
            }
        }
        PersonalDataInstance instance = new PersonalDataInstance(
                newAPISmartPointer, newAnnotationSmartPointer, annotationSpeculatedFromAPICall,
                annotation, api);
        personalDataInstances.add(instance);
        return instance;
    }

    public PersonalDataInstance addPersonalDataInstance(PsiElement expression, PsiAnnotation [] psiAnnotations,
                                                        AnnotationHolder [] annotationSpeculatedFromAPICalls, AnnotationHolder [] annotations,
                                                        PersonalDataAPI api) {
        cleanupInvalidInstances();
        if (openProject == null) {
            // TODO: (long-term) support multiple projects
            openProject = expression.getProject();
        }
        assert (psiAnnotations.length == annotationSpeculatedFromAPICalls.length && psiAnnotations.length == annotations.length);
        ArrayList<AnnotationMetaData> metaDataArrayList = new ArrayList<>();
        for (int i = 0 ; i < psiAnnotations.length ; ++i) {
            SmartPsiElementPointer newAnnotationSmartPointer = null;
            if (psiAnnotations[i] != null) {
                newAnnotationSmartPointer = SmartPointerManager.getInstance(psiAnnotations[i].getProject()).createSmartPsiElementPointer(psiAnnotations[i]);
            }
            metaDataArrayList.add(new AnnotationMetaData(annotations[i], annotationSpeculatedFromAPICalls[i], newAnnotationSmartPointer));
        }
        SmartPsiElementPointer newAPISmartPointer =
                SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
        for (PersonalDataInstance instance : personalDataInstances) {
            if (instance.psiElementPointer.equals(newAPISmartPointer)) {
                // Already had the annotation, just update annotation content
                instance.setAnnotationInfo(metaDataArrayList.toArray(new AnnotationMetaData[0]));
                return instance;
            }
        }
        PersonalDataInstance instance = new PersonalDataInstance(
                newAPISmartPointer, metaDataArrayList.toArray(new AnnotationMetaData[0]),
                api);
        personalDataInstances.add(instance);

        return instance;
    }


    public void removePersonalDataInstance(PsiElement expression) {
        cleanupInvalidInstances();
        if (openProject == null) {
            // TODO: (long-term) support multiple projects
            openProject = expression.getProject();
        }
        SmartPsiElementPointer newAPISmartPointer =
                SmartPointerManager.getInstance(expression.getProject()).createSmartPsiElementPointer(expression);
        personalDataInstances.removeIf(p -> p.psiElementPointer.equals(newAPISmartPointer));
    }

    public void addPermissionInstance(AndroidPermission permission, PsiElement psiElement) {
        cleanupInvalidInstances();
        if (openProject == null) {
            openProject = psiElement.getProject();
        }
        SmartPsiElementPointer newPermissionSmartPointer =
                SmartPointerManager.getInstance(psiElement.getProject()).createSmartPsiElementPointer(psiElement);
        ArrayList<AndroidPermissionInstance> permissionInstancesToRemove = new ArrayList<>();
        for (AndroidPermissionInstance instance : permissionInstances) { // FIXME: java.util.ConcurrentModificationException
            if (instance.psiElementPointer.equals(newPermissionSmartPointer)) {
                permissionInstancesToRemove.add(instance);
            }
        }
        permissionInstances.removeAll(permissionInstancesToRemove);
        permissionInstances.add(new AndroidPermissionInstance(newPermissionSmartPointer, permission));
    }

    public boolean hasPermissionDeclared(AndroidPermission permission) {
        cleanupInvalidInstances();
        if (openProject != null) {
            PsiFile [] manifestFiles = FilenameIndex.getFilesByName(openProject, "AndroidManifest.xml", GlobalSearchScope.allScope(openProject));
            for (PsiFile manifestFile : manifestFiles) {
                if (Pattern.matches(".*app/src/main/AndroidManifest\\.xml", manifestFile.getVirtualFile().getPath())) {
                    manifestFile.accept(new PsiRecursiveElementVisitor() {
                        @Override
                        public void visitElement(PsiElement element) {
                            super.visitElement(element);
                            if (element instanceof XmlTag) {
                                XmlTag tag = (XmlTag) element;
                                if (tag.getAttributes().length == 0) {
                                    return;
                                }
                                String xmlAttribute = tag.getAttributes()[0].getDisplayValue();
                                if (xmlAttribute == null) {
                                    return;
                                }
                                for (AndroidPermission permission : AndroidPermission.values()) {
                                    if (xmlAttribute.equals("android.permission." + permission.toString())) {
                                        addPermissionInstance(permission, tag);
                                    }
                                }
                            }
                        }
                    });
                }
            }
            for (AndroidPermissionInstance instance : permissionInstances) {
                if (instance.permission.equals(permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    public PersonalDataInstance [] getAllPersonalDataInstances() {
        cleanupInvalidInstances();
        return personalDataInstances.toArray(new PersonalDataInstance[0]);
    }
}
