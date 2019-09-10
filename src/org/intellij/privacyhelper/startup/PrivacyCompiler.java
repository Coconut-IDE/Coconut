package org.intellij.privacyhelper.startup;

import com.intellij.codeInspection.*;
import com.intellij.codeInspection.ex.*;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.notification.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.profile.codeInspection.InspectionProjectProfileManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.intellij.privacyhelper.codeInspection.inspections.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.event.HyperlinkEvent;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class PrivacyCompiler implements StartupActivity {
    private static final NotificationGroup NOTIFICATION_GROUP = new NotificationGroup("Required Plugins", NotificationDisplayType.BALLOON, true);

    private final LocalInspectionTool[] privacyInspections = {
            new PersonalDataSourceAPIInspection(),
            new PersonalDataSinkAPIInspection(),
            new ThirdPartyAPIInspection(),
            new AnnotationConsistencyInspection(),
            new AnnotationPrivacySensitivityInspection(),
            new AnnotationTooltipInspection(),
    };

    private List<PsiFile> computeFilesWithErrors(Project project) {
        List<PsiFile> fileWithErrors = new LinkedList<>();
        try {
            fileWithErrors = ReadAction.compute(() -> {
                double filesProcessed = 0.0;
                List<PsiFile> errs = new LinkedList<PsiFile>();
                Collection<VirtualFile> projectFiles = getProjectFiles(project);

                for (final VirtualFile f : projectFiles) {
                    PsiFile fileToInspect = PsiManager.getInstance(project).findFile(f);

                    if (fileHasErrors(project, fileToInspect)) {
                        errs.add(fileToInspect);
                    }

                    double percentComplete = ((++filesProcessed) / projectFiles.size());
                    updateProgressBar(project, percentComplete);
                }

                return errs;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileWithErrors;
    }

    private class PrivacyCheckTask implements Runnable {
        private Project project;

        public PrivacyCheckTask(final Project project) { this.project = project; }

        public void run() {
            List<PsiFile> filesWithErrors = new LinkedList<PsiFile>();

            try {
                filesWithErrors = ProgressManager.getInstance()
                        .runProcessWithProgressSynchronously(
                                () -> computeFilesWithErrors(project),
                                "Checking for privacy violations",
                                true,
                                project
                        );
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            highlightFilesWithErrors(project, filesWithErrors);

            if(filesWithErrors.size() > 0) {
                notifyUserOfErrors(project);
            }
        }
    }

    public void runActivity(@NotNull Project project) {
        DumbService.getInstance(project).runWhenSmart(new PrivacyCheckTask(project));
    }

    private Collection<VirtualFile> getProjectFiles(@NotNull final Project project) {
        Collection<VirtualFile> allFiles = FileBasedIndex.getInstance()
                                                         .getContainingFiles(FileTypeIndex.NAME,
                                                                             JavaFileType.INSTANCE,
                                                                             GlobalSearchScope.projectScope(project));

        Collection<VirtualFile> projectFiles = new LinkedList<VirtualFile>();

        for(final VirtualFile file : allFiles) {
            if(!file.getName().equals("R.java") &&
               !file.getName().equals("BuildConfig.java")) {
                    projectFiles.add(file);
            }
        }

        return projectFiles;
    }

    private boolean fileHasErrors(@NotNull final Project project,
                                  final PsiFile file) {
        if (file != null) {
            InspectionProfileImpl profile = createInspectionProfile(project);
            GlobalInspectionContext context = createInspectionContext(project);

            for (final LocalInspectionTool inspection : privacyInspections) {
                InspectionToolWrapper wrapper = profile.getInspectionTool(inspection.getShortName(), project);

                if (wrapper != null) {
                    List<ProblemDescriptor> issues = InspectionEngine.runInspectionOnFile(file, wrapper, context);

                    if(!issues.isEmpty()) {
                        return true;
                    }
                }
            }

            return false;
        }

        return false;
    }

    private void highlightFilesWithErrors(@NotNull final Project project,
                                          final List<PsiFile> filesWithErrors) {
        for(final PsiFile problemFile : filesWithErrors) {
            Document document = PsiDocumentManager.getInstance(project).getDocument(problemFile);

            if(document != null) {

            }
        }
    }

    private InspectionProfileImpl createInspectionProfile(@NotNull final Project project) {
        final InspectionProfile profile = InspectionProjectProfileManager.getInstance(project).getInspectionProfile();
        return (InspectionProfileImpl)profile;
    }

    private GlobalInspectionContext createInspectionContext(@NotNull final Project project) {
        InspectionManagerEx inspectionManager = (InspectionManagerEx)InspectionManager.getInstance(project);
        return inspectionManager.createNewGlobalContext(false);
    }

    private void notifyUserOfErrors(@NotNull final Project project) {
        NOTIFICATION_GROUP
                .createNotification("Privacy Errors",
                        "Please check the tool window \"PrivacyChecker\" for unresolved privacy warnings/tasks.",
                        NotificationType.ERROR,
                        new NotificationListener() {
                            @Override
                            public void hyperlinkUpdate(@NotNull Notification notification, @NotNull HyperlinkEvent hyperlinkEvent) {

                            }
                        }).notify(project);
    }

    private void updateProgressBar(@NotNull final Project project,
                                   final double percentComplete) {
        ProgressManager.getInstance().getProgressIndicator().setFraction(percentComplete);
    }
}