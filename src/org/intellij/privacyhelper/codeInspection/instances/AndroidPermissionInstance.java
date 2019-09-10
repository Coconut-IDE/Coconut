package org.intellij.privacyhelper.codeInspection.instances;

import com.intellij.psi.SmartPsiElementPointer;
import org.intellij.privacyhelper.codeInspection.utils.*;

/**
 * Created by tianshi on 4/27/17.
 */
public class AndroidPermissionInstance {
    public SmartPsiElementPointer psiElementPointer;
    public AndroidPermission permission;

    public AndroidPermissionInstance(SmartPsiElementPointer psiElementPointer,
                                     AndroidPermission permission) {
        this.psiElementPointer = psiElementPointer;
        this.permission = permission;
    }

    public boolean isValid() {
        // TODO (double-check) What's the right way to check the validity?
        return psiElementPointer.getElement() != null;
    }
}
