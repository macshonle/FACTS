package facts.views;

import static edu.utsa.eclipse.EclipseUtil.getShell;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import edu.utsa.dynamic.FieldReference;
import edu.utsa.eclipse.EclipseUIUtil;
import edu.utsa.eclipse.EclipseUtil;
import edu.utsa.exceptions.PluginError;
import edu.utsa.strings.StringUtil;
import facts.ast.Difference;

public class DifferenceView extends ViewPart
{
    // The ID of the view as specified by the extension.
    public static final String ID = "facts.views.DifferenceView";

    private static final int DIRECTORY_TEXT_AREA_STYLE = SWT.READ_ONLY
            | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL;

    private static final String PROJECT_A_TEXT = "DifferenceViewProjectA";
    private static final String PROJECT_B_TEXT = "DifferenceViewProjectB";
    private static final String FILE_SELECTED = "DifferenceViewFileSelected";

    private IMemento memento;

    public Text dirATextArea;
    public Text dirBTextArea;
    private Combo commonFileSelection;
    private Button compareButton;
    private Text outputTextArea;

    private Action clearAllAction;

    public DifferenceView() {
    }

    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }

    public Combo getcommonFileSelection()
    {
      return commonFileSelection;
    }
    // This is a callback that will allow us to create the viewer and initialize
    // it.
    public void createPartControl(Composite parent) {
        makeActions();
        IViewSite viewSite = getViewSite();
        IActionBars bars = viewSite.getActionBars();
        IMenuManager menuManager = bars.getMenuManager();
        menuManager.add(clearAllAction);

        layoutControls(parent);

        if (memento != null) {
            restoreState();
        }
    }

    private void layoutControls(Composite parent) {
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 2;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        parent.setLayoutData(EclipseUIUtil.fillGreedy());

        makeDirectoryInputRow(parent, "Select Project A:", new FieldReference(this, Text.class,
                "dirATextArea"));
        makeDirectoryInputRow(parent, "Select Project B:", new FieldReference(this, Text.class,
                "dirBTextArea"));

        makeFileSelectionRow(parent);

        this.outputTextArea = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        outputTextArea.setLayoutData(EclipseUIUtil.greedySpanColumnsRows(3, 1));

        clearAllAction.run();
    }

    private void makeDirectoryInputRow(Composite parent, String prompt,
            final FieldReference<Text> textAreaField) {
        Label label = new Label(parent, SWT.LEFT);
        label.setText(prompt);
        label.setLayoutData(EclipseUIUtil.rightAligned());

        final Text directoryTextArea = new Text(parent, DIRECTORY_TEXT_AREA_STYLE);
        directoryTextArea.setBackground(EclipseUIUtil.getGray());
        directoryTextArea.setLayoutData(EclipseUIUtil.fillHorizontalGreedy());
        textAreaField.setValue(directoryTextArea);

        Button browse = new Button(parent, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.OPEN);
                String dirOrNull = dialog.open();
                if (dirOrNull != null) {
                    String dir = dirOrNull;
                    textAreaField.getValue().setText(dir);
                }
                traverseFilesIfBothSet();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private void makeFileSelectionRow(Composite composite) {
        Composite parent = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        layout.marginWidth = 2;
        layout.marginHeight = 0;
        parent.setLayout(layout);
        parent.setLayoutData(EclipseUIUtil.greedySpanColumns(3));

        this.commonFileSelection = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.SINGLE);
        commonFileSelection.setLayoutData(EclipseUIUtil.fillHorizontalGreedy());

        this.compareButton = new Button(parent, SWT.PUSH);
        compareButton.setText("Compare different versions");
        compareButton.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                int i = commonFileSelection.getSelectionIndex();
                if (i == -1) {
                    EclipseUIUtil.notifyf("Please select a file from the drop down menu.");
                }
                else {
                    String selection = commonFileSelection.getItem(i);
                    String filenameA = String.format("%s%s", dirATextArea.getText(), selection);
                    String filenameB = String.format("%s%s", dirBTextArea.getText(), selection);
                    try {
                        final Difference diff = new Difference(filenameA, filenameB);
                        i = 2;
//                        EclipseUIUtil.guiExec(new Runnable() 
//                        {
//                            public void run() 
//                            {
                                outputTextArea.setText(diff.getResults());
//                            }
//                        });
                    }
                    catch (Exception e) {
                        throw new PluginError(e);
                    }
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });
    }

    private void makeActions() {
        this.clearAllAction = new Action() {
            public void run() {
                dirATextArea.setText("");
                dirBTextArea.setText("");
                commonFileSelection.removeAll();
                commonFileSelection.setEnabled(false);
                compareButton.setEnabled(false);
            }
        };
        clearAllAction.setText("Clear All");
        clearAllAction.setToolTipText("Reset both project directories to select new ones");
    }

    private void traverseFilesIfBothSet() {
        String dirA = dirATextArea.getText();
        String dirB = dirBTextArea.getText();
        if (!dirA.isEmpty() && !dirB.isEmpty()) {
            try {
                ProgressMonitorDialog dialog;
                TraverseDirectoriesOperation runnable;

                runnable = new TraverseDirectoriesOperation(dirA, dirB);
                dialog = new ProgressMonitorDialog(EclipseUtil.getShell());
                dialog.run(true, false, runnable);
            }
            catch (InvocationTargetException e) {
                EclipseUIUtil.notify(e);
                throw new PluginError(e);
            }
            catch (Exception e) {
                EclipseUIUtil.notify(e);
                throw new PluginError(e);
            }
        }
    }

    @Override
    public void setFocus() {
        outputTextArea.setFocus();
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        memento.putString(PROJECT_A_TEXT, dirATextArea.getText());
        memento.putString(PROJECT_B_TEXT, dirBTextArea.getText());
        int selectionIndex = commonFileSelection.getSelectionIndex();
        if (selectionIndex != -1) {
            memento.putString(FILE_SELECTED, commonFileSelection.getItem(selectionIndex));
        }
    }

    // Assumes memento != null.
    private void restoreState() {
        final String dirA = memento.getString(PROJECT_A_TEXT);
        final String dirB = memento.getString(PROJECT_B_TEXT);
        final String selectedFile = memento.getString(FILE_SELECTED);

        EclipseUIUtil.guiExec(new Runnable() {
            public void run() {
                boolean failed = false;
                try {
                    if (dirA != null) {
                        dirATextArea.setText(dirA);
                    }
                    if (dirB != null) {
                        dirBTextArea.setText(dirB);
                    }
                    traverseFilesIfBothSet();
                }
                catch (Exception e) {
                    // If it failed, let's pretend nothing happened
                    clearAllAction.run();
                    failed = true;
                }
                if (selectedFile != null && !failed) {
                    int index = commonFileSelection.indexOf(selectedFile);
                    if (index != -1) {
                        commonFileSelection.select(index);
                    }
                }
            }
        });
    }

    private class TraverseDirectoriesOperation implements IRunnableWithProgress
    {
        private String dirA;
        private String dirB;
        // the sets of files, with the dir name prefix removed
        private Set<String> dirAFiles;
        private Set<String> dirBFiles;

        public TraverseDirectoriesOperation(String dirA, String dirB) {
            this.dirA = dirA;
            this.dirB = dirB;
            this.dirAFiles = new TreeSet<String>();
            this.dirBFiles = new TreeSet<String>();
        }

        @Override
        public void run(IProgressMonitor monitor) throws InvocationTargetException,
                InterruptedException {
            try {
                scanDirectory(monitor, "Scanning Project A Directory", dirA, dirAFiles);
                scanDirectory(monitor, "Scanning Project B Directory", dirB, dirBFiles);
            }
            catch (Exception e) {
                EclipseUIUtil.notify(e.getMessage(), "Exception");
                throw new PluginError(e);
            }

            Set<String> onlyInA = new TreeSet(dirAFiles);
            onlyInA.removeAll(dirBFiles);

            Set<String> onlyInB = new TreeSet(dirBFiles);
            onlyInB.removeAll(dirAFiles);

            final Set<String> commonToBoth = new TreeSet(dirAFiles);
            commonToBoth.retainAll(dirBFiles);

            final StringBuilder buff = new StringBuilder();
            buff.append(String.format("Files found only in %s:%n", dirA));
            StringUtil.separate(buff, onlyInA, String.format("%n"), "[No differences]");

            buff.append(String.format("%n%nFiles found only in %s:%n", dirB));
            StringUtil.separate(buff, onlyInB, String.format("%n"), "[No differences]");

            buff.append(String.format("%n%nFiles found in both:%n"));
            StringUtil.separate(buff, commonToBoth, String.format("%n"));

            EclipseUIUtil.guiExec(new Runnable() {
                @Override
                public void run() {
                    commonFileSelection.removeAll();
                    for (String filename : commonToBoth) {
                        commonFileSelection.add(filename);
                    }
                    commonFileSelection.setEnabled(true);
                    compareButton.setEnabled(true);
                    DifferenceView.this.outputTextArea.setText(buff.toString());
                }
            });
        }

        private void scanDirectory(IProgressMonitor monitor, String message, String directory,
                Set<String> fileSet) throws IOException {
            monitor.beginTask(message, IProgressMonitor.UNKNOWN);
            File root = new File(directory);
            String commonPrefix = root.getCanonicalPath();
            scanDirectory(root, commonPrefix, fileSet, monitor);
        }

        private void scanDirectory(File root, String commonPrefix, Set<String> fileSet,
                IProgressMonitor monitor) throws IOException {
            if (root.isDirectory()) {
                monitor.subTask(root.getAbsolutePath());
                File[] files = root.listFiles();
                for (File file : files) {
                    if (file.isFile()) {
                        String name = file.getName();
                        if (name.endsWith(".java")) {
                            String path = file.getCanonicalPath();
                            if (!path.startsWith(commonPrefix)) {
                                throw new PluginError("Coding error: %s is not a prefix of %s",
                                        commonPrefix, path);
                            }
                            fileSet.add(path.substring(commonPrefix.length()));
                        }
                    }
                    else if (file.isDirectory()) {
                        scanDirectory(file, commonPrefix, fileSet, monitor);
                    }
                }
            }
        }
    }
}
