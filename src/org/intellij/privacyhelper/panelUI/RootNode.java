/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.privacyhelper.panelUI;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tianshi on 1/10/17.
 */
public final class RootNode extends BaseNode {

  public RootNode(Project project, Object value, AbstractTreeBuilder builder) {
    super(project, value, builder);
  }

  @Override
  @NotNull
  public Collection<AbstractTreeNode> getChildren() {
    ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<AbstractTreeNode>();
    if (myBuilder instanceof PersonalDataUsageOverviewTreeBuilder) {
      for (PersonalDataGroup group : PersonalDataGroup.values()) {
        childrenNodes.add(new PersonalDataGroupNode(myProject, group, myBuilder, ((PersonalDataUsageOverviewTreeBuilder) myBuilder).getMyPanelType()));
      }
    }
    return childrenNodes;
  }

  @Override
  public void update(PresentationData presentation) {
    presentation.setPresentableText("placeholder");
  }

  @Override
  public String getTestPresentation() {
    return "Root";
  }

}
