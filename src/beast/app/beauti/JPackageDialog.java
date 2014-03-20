package beast.app.beauti;

import beast.core.Description;
import beast.util.Package;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static beast.util.AddOnManager.*;

/**
 * dialog for managing Package.
 * List, install and uninstall Package
 *
 * modified by Walter Xie
 */
@Description("BEAUti package manager")
public class JPackageDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    JPanel panel;
    JCheckBox installAllDepCheckBox = new JCheckBox("install dependent packages", null, true);
    final JFrame frame;
    JTable dataTable = null;

    List<Package> packages = new ArrayList<Package>();

    public JPackageDialog(JFrame frame) {
        super(frame);
        this.frame = frame;
        frame.setCursor(new Cursor(Cursor.WAIT_CURSOR));
        setModal(true);

        panel = new JPanel();
        getContentPane().add(BorderLayout.CENTER, panel);
        setTitle("BEAST 2 Package Manager");

        Component packageListBox = createTable();
        panel.add(packageListBox);
        Box buttonBox = createButtonBox();
        getContentPane().add(buttonBox, BorderLayout.SOUTH);

        Dimension dim = panel.getPreferredSize();
        Dimension dim2 = buttonBox.getPreferredSize();
        setSize(dim.width + 20, dim.height + dim2.height + 30);
        Point frameLocation = frame.getLocation();
        Dimension frameSize = frame.getSize();
        setLocation(frameLocation.x + frameSize.width / 2 - dim.width / 2, frameLocation.y + frameSize.height / 2 - dim.height / 2);
        frame.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }


    private Component createTable() {
        Box box = Box.createVerticalBox();
        box.add(new JLabel("List of available packages for BEAST v" + beastVersion.getMajorVersion() + ".* in alphabetic order"));

        DataTableModel dataTableModel = new DataTableModel();
        dataTable = new JTable(dataTableModel);
        dataTable.setSelectionMode(DefaultListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dataTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Package selPackage = getSelectedPackage(dataTable.getSelectedRow());
                    showDetail(selPackage);
                }
            }
        });

        resetPackages();

        JScrollPane pane = new JScrollPane(dataTable);
        box.add(pane);
        return box;
    }

    private void resetPackages() {
        packages.clear();
        try {
            packages = getPackages();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (dataTable.getRowCount() > 0)
            dataTable.setRowSelectionInterval(0, 0);
    }

    private Package getSelectedPackage(int selectedRow) {
        if (packages.size() <= selectedRow)
            throw new IllegalArgumentException("Incorrect row " + selectedRow +
                    " is selected from package list, size = " + packages.size());
        return packages.get(selectedRow);
    }

    private void showDetail(Package aPackage) {
        //custom title, no icon
        JOptionPane.showMessageDialog(frame,
                "Eggs are not supposed to be green.",
                aPackage.packageName,
                JOptionPane.PLAIN_MESSAGE);
    }

    private Box createButtonBox() {
//        Box allBox = Box.createVerticalBox();
//        JPanel p = new JPanel();
//        p.add(BorderLayout.WEST, installAllDepCheckBox);
//        allBox.add(p);

        Box box = Box.createHorizontalBox();
        box.add(installAllDepCheckBox);
        box.add(Box.createGlue());
        JButton installButton = new JButton("Install/Upgrade");
        installButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = dataTable.getSelectedRows();
                for (int selRow : selectedRows) {
                    Package selPackage = getSelectedPackage(selRow);
                    if (selPackage != null) {
                        try {
                            if (selPackage.isInstalled()) {
                                //TODO upgrade version
                            } else {
                                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                if (installAllDepCheckBox.isSelected()) {
                                    installPackage(selPackage, false, null, packages);
                                } else {
                                    installPackage(selPackage, false, null, null);
                                }
                                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                            }
                            resetPackages();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Install failed because: " + ex.getMessage());
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }
            }
        });
        box.add(installButton);

        JButton uninstallButton = new JButton("Uninstall");
        uninstallButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = dataTable.getSelectedRows();

                boolean toDeleteFileExists = false;
                for (int selRow : selectedRows) {
                    Package selPackage = getSelectedPackage(selRow);
                    if (selPackage != null) {
                        try {
                            if (selPackage.isInstalled()) {
//                            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to uninstall " +
//                            AddOnManager.URL2PackageName(package.url) + "?", "Uninstall Add On",
//                                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                                setCursor(new Cursor(Cursor.WAIT_CURSOR));
                                uninstallPackage(selPackage, false, null);
                                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

                                File toDeleteFile = getToDeleteListFile();
                                if (toDeleteFile.exists()) {
                                    toDeleteFileExists = true;
                                }
//                            }
                            } else {
                                //TODO ?
                            }
                            resetPackages();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Uninstall failed because: " + ex.getMessage());
                            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        }
                    }
                }

                if (toDeleteFileExists) {
                    JOptionPane.showMessageDialog(null, "<html>To complete uninstalling the package, BEAUti need to be restarted<br><br>Exiting now.</html>");
                    System.exit(0);
                }

            }
        });
        box.add(uninstallButton);

        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        box.add(Box.createGlue());
        box.add(closeButton);
        box.add(Box.createGlue());

        JButton button = new JButton("?");
        button.setToolTipText(getPackageUserDir() + " " + getPackageAppDir());
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "<html>Package are installed in <br><br><em>" + getPackageUserDir() +
                        "</em><br><br> by you, and are available to you,<br>the user, only.<br>" +
                        "System wide packages are installed in <br><br><em>" + getPackageAppDir() +
                        "</em><br><br>and are available to all users." +
                        "<br>(just move the package there manually" +
                        "<br>to make it system wide available).</html>");
            }
        });
        box.add(button);
//        allBox.add(box);
        return box;
    }

    class DataTableModel extends AbstractTableModel {
        String[] columnNames = {"Name", "Status/Version", "Latest", "Dependencies", "Detail"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return packages.size();
        }

        public Object getValueAt(int row, int col) {
            Package aPackage = packages.get(row);
            switch (col) {
                case 0:
                    return aPackage.packageName;
                case 1:
                    return aPackage.getStatus();
                case 2:
                    return aPackage.getLatestVersion();
                case 3:
                    return aPackage.getDependenciesString();
                case 4:
                    return aPackage.description;
                default:
                    throw new IllegalArgumentException("unknown column, " + col);
            }
        }

        public String getColumnName(int column) {
            return columnNames[column];
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();

            buffer.append(getColumnName(0));
            for (int j = 1; j < getColumnCount(); j++) {
                buffer.append("\t");
                buffer.append(getColumnName(j));
            }
            buffer.append("\n");

            for (int i = 0; i < getRowCount(); i++) {
                buffer.append(getValueAt(i, 0));
                for (int j = 1; j < getColumnCount(); j++) {
                    buffer.append("\t");
                    buffer.append(getValueAt(i, j));
                }
                buffer.append("\n");
            }

            return buffer.toString();
        }
    }


}