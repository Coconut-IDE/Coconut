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
import org.intellij.privacyhelper.codeInspection.instances.*;
import org.intellij.privacyhelper.codeInspection.state.*;
import org.intellij.privacyhelper.codeInspection.utils.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

/**
 * Created by tianshi on 1/11/17.
 */
public class PersonalDataGroupNode extends BaseNode {
  private CoconutPanelType myPanelType;
  final String mySummaryText;
  private Project openProject;

  protected PersonalDataGroupNode(Project project, PersonalDataGroup group, AbstractTreeBuilder builder, CoconutPanelType myPanelType) {
    super(project, group, builder);
    mySummaryText = group.toString();
    this.myPanelType = myPanelType;
    openProject = project;
  }

  @NotNull
  @Override
  public Collection<? extends AbstractTreeNode> getChildren() {
    ArrayList<AbstractTreeNode> childrenNodes = new ArrayList<AbstractTreeNode>();
    PersonalDataGroup myGroup = (PersonalDataGroup) getEqualityObject();
    Vector<PersonalDataInstance> instances;
    if (myPanelType == CoconutPanelType.PersonalDataAccessOverview) {
      instances =
              PersonalDataHolder.getInstance(openProject).getSourceAPICallInstances();
    } else {
      // myPanelType == CoconutPanelType.PersonalDataLeakOverview
      instances =
              PersonalDataHolder.getInstance(openProject).getSinkAPICallInstances();
    }
    for (PersonalDataInstance instance : instances) {
      if (instance.belongToPersonalDataGroup(myGroup)) {
        childrenNodes.add(new PersonalDataGroupItemNode(myProject, instance, myBuilder, myGroup));
      }
    }
    return childrenNodes;
  }

  @Override
  protected void update(PresentationData presentation) {
    presentation.setPresentableText(mySummaryText);
  }
}
