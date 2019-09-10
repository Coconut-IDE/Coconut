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

import com.intellij.ide.*;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.ui.*;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.UIUtil;
import org.intellij.privacyhelper.codeInspection.instances.*;
import org.intellij.privacyhelper.codeInspection.utils.CoconutUIUtil;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;

/**
 * Created by tianshi on 1/9/17.
 */
public class PersonalDataUsageOverviewPanel extends SimpleToolWindowPanel implements OccurenceNavigator, DataProvider, Disposable {
  protected Project myProject;
  private final CoconutPanelType myPanelType;

  private final Tree myTree;
  private final MyTreeExpander myTreeExpander;
  protected final PersonalDataUsageOverviewTreeBuilder myPersonalDataUsageOverviewTreeBuilder;

  public PersonalDataUsageOverviewPanel(Project project, CoconutPanelType panelType) {
    super(false, true);

    myProject = project;
    myPanelType = panelType;

    DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
    myTree = new Tree(model);
    myTreeExpander = new MyTreeExpander();
    initUI();
    myPersonalDataUsageOverviewTreeBuilder = createTreeBuilder(myTree, model, myProject);
    Disposer.register(myProject, myPersonalDataUsageOverviewTreeBuilder);

    //myVisibilityWatcher = new MyVisibilityWatcher();
    //myVisibilityWatcher.install(this);
  }

  protected PersonalDataUsageOverviewTreeBuilder createTreeBuilder(JTree tree, DefaultTreeModel treeModel, Project project) {
    PersonalDataUsageOverviewTreeBuilder builder = new PersonalDataUsageOverviewTreeBuilder(tree, treeModel, project, myPanelType);
    builder.init();
    return builder;
  }

  private void initUI() {
    UIUtil.setLineStyleAngled(myTree);
    myTree.setShowsRootHandles(true);
    myTree.setRootVisible(false);
    myTree.setCellRenderer(new PrivacyCheckerCompositeRenderer());
    EditSourceOnDoubleClickHandler.install(myTree);
    new TreeSpeedSearch(myTree);

    setContent(createCenterComponent());

    myTree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(final TreeSelectionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().runWriteAction(() -> {
              TreePath treePath = myTree.getSelectionPath();
              if (treePath == null) {
                return;
              }
              final Object object = ((DefaultMutableTreeNode)treePath.getLastPathComponent()).getUserObject();
              if (!(object instanceof PersonalDataGroupItemNode)) {
                return;
              }
              final PersonalDataGroupItemNode personalDataGroupItemNode = (PersonalDataGroupItemNode)object;
              PersonalDataInstance apiCallInstance = ((PersonalDataInstance) personalDataGroupItemNode.getValue());
              SmartPsiElementPointer targetPointer = apiCallInstance.getPsiElementPointer();
              CoconutUIUtil.navigateMainEditorToPsiElement(targetPointer);
            });
          }
        });
      }
    });

    JPanel toolBarPanel = new JPanel(new GridLayout());
    DefaultActionGroup rightGroup = new DefaultActionGroup();
    AnAction expandAllAction = CommonActionsManager.getInstance().createExpandAllAction(myTreeExpander, this);
    rightGroup.add(expandAllAction);

    AnAction collapseAllAction = CommonActionsManager.getInstance().createCollapseAllAction(myTreeExpander, this);
    rightGroup.add(collapseAllAction);
    toolBarPanel.add(
      ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, rightGroup, false).getComponent());

    setToolbar(toolBarPanel);

  }

  protected JComponent createCenterComponent() {
    Splitter splitter = new OnePixelSplitter(false);
    splitter.setFirstComponent(ScrollPaneFactory.createScrollPane(myTree));
    return splitter;
  }


  @Override
  public boolean hasNextOccurence() {
    return false;
  }

  @Override
  public boolean hasPreviousOccurence() {
    return false;
  }

  @Override
  public OccurenceInfo goNextOccurence() {
    return null;
  }

  @Override
  public OccurenceInfo goPreviousOccurence() {
    return null;
  }

  @Override
  public String getNextOccurenceActionName() {
    return null;
  }

  @Override
  public String getPreviousOccurenceActionName() {
    return null;
  }

  @Override
  public void dispose() {

  }

  private final class MyTreeExpander implements TreeExpander {
    @Override
    public boolean canCollapse() {
      return true;
    }

    @Override
    public boolean canExpand() {
      return true;
    }

    @Override
    public void collapseAll() {
      myPersonalDataUsageOverviewTreeBuilder.collapseAll();
    }

    @Override
    public void expandAll() {
      myPersonalDataUsageOverviewTreeBuilder.expandAll(null);
    }
  }

  //private final class MyVisibilityWatcher extends VisibilityWatcher {
  //  @Override
  //  public void visibilityChanged() {
  //    if (myProject.isOpen()) {
  //      PsiDocumentManager.getInstance(myProject).commitAllDocuments();
  //      myPersonalDataUsageOverviewTreeBuilder.setUpdatable(isShowing());
  //    }
  //  }
  //}

}
