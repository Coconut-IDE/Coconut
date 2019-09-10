package org.intellij.privacyhelper.startup;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.intellij.privacyhelper.codeInspection.state.PersonalDataHolder;
import org.jetbrains.annotations.NotNull;

public class ProjectInitializer implements StartupActivity {
    public void runActivity(@NotNull Project project) {
        PersonalDataHolder holder = PersonalDataHolder.getInstance(project);
        System.out.println("We got the holder: " + holder.getClass().getName());
    }
}
