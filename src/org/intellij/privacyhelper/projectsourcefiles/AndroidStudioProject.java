package org.intellij.privacyhelper.projectsourcefiles;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Models a (for now)Java-based Android Studio project.
 *
 * Note: This class is not aware of changes that occur
 * in a project. You must manually handle file add/remove
 * events.
 * */
public class AndroidStudioProject {
    private static Map<Project, AndroidStudioProject> androidProjects;
    private List<SourceFile> sourceFiles;
    private Project intellijProject;

    private AndroidStudioProject(@NotNull Project intellijProject) {
        this.intellijProject = intellijProject;
        sourceFiles = new LinkedList<>();
        collectSourceFiles();
    }

    public static AndroidStudioProject getInstance(@NotNull Project project) {
        if(androidProjects == null) {
            androidProjects = new HashMap<Project, AndroidStudioProject>();
        }

        if(!androidProjects.containsKey(project)) {
            androidProjects.put(project, new AndroidStudioProject(project));
        }

        return androidProjects.get(project);
    }

    public List<SourceFile> getSourceFiles() {
        return sourceFiles;
    }

    public void addSourceFile(SourceFile file) {
        sourceFiles.add(file);
    }

    private void collectSourceFiles() {
        Collection<VirtualFile> allFiles = FileBasedIndex.getInstance()
                .getContainingFiles(FileTypeIndex.NAME,
                        JavaFileType.INSTANCE,
                        GlobalSearchScope.projectScope(intellijProject));

        for(final VirtualFile file : allFiles) {
            if(!file.getName().equals("R.java") &&
               !file.getName().equals("BuildConfig.java")) {
                PsiJavaFile javaFile = (PsiJavaFile)PsiManager.getInstance(intellijProject)
                                                              .findFile(file);

                sourceFiles.add(SourceFile.getInstance(javaFile));
            }
        }
    }
}