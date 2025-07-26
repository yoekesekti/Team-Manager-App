import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

public class TeamManagerUI extends JFrame {
    private Map<String, Set<String>> collabGraph = new HashMap<>();
    private JTabbedPane tabbedPane;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    
    private static final String USER_FILE = "data_user.txt";
    private static final String SKILL_FILE = "data_skill.txt";
    private static final String PROJECT_FILE = "data_project.txt";
    private static final String COLLAB_FILE = "data_kolaborasi.txt";
    
    public TeamManagerUI() {
        setTitle("Team Manager Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        
        initUI();
        loadInitialData();
    }
    
    private void initUI() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        JMenuBar menuBar = new JMenuBar();
        
        JLabel appNameLabel = new JLabel("  Team Manager App  ");
        appNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Atur font
        appNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 20)); // Padding kiri-kanan
        menuBar.add(appNameLabel);
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        
        setJMenuBar(menuBar);
        
        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        tabbedPane.addTab("Employees", createEmployeePanel());
        tabbedPane.addTab("Skills", createSkillPanel());
        tabbedPane.addTab("Projects", createProjectPanel());
        tabbedPane.addTab("Collaborations", createCollaborationPanel());
        tabbedPane.addTab("Team Formation", createTeamFormationPanel());
        
        mainPanel.add(tabbedPane, "main");
        add(mainPanel);
    }
    
    private void loadInitialData() {
        try {
            buildGraph();
        } catch (IOException e) {
            showError("Error loading initial data: " + e.getMessage());
        }
    }
    
    // ========== Employee Panel ==========
    private JPanel createEmployeePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Table for displaying employees
        JTable employeeTable = createEmployeeTable();
        JScrollPane scrollPane = new JScrollPane(employeeTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Employee");
        JButton editButton = new JButton("Edit Employee");
        JButton deleteButton = new JButton("Delete Employee");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddEmployeeDialog());
        editButton.addActionListener(e -> editEmployee(employeeTable));
        deleteButton.addActionListener(e -> deleteEmployee(employeeTable));
        refreshButton.addActionListener(e -> refreshTable(employeeTable, USER_FILE));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createEmployeeTable() {
        String[] columnNames = {"ID", "Name", "Age", "Skills", "Available", "Past Projects"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                model.addRow(parts);
            }
        } catch (IOException e) {
            showError("Error loading employee data: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        
        return table;
    }
    
    private void showAddEmployeeDialog() {
        JDialog dialog = new JDialog(this, "Add New Employee", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField ageField = new JTextField();
        JTextField skillsField = new JTextField();
        JCheckBox availableCheck = new JCheckBox();
        JTextField projectsField = new JTextField();
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Age:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("Skills (comma separated):"));
        formPanel.add(skillsField);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availableCheck);
        formPanel.add(new JLabel("Past Projects (comma separated):"));
        formPanel.add(projectsField);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String skills = skillsField.getText().trim();
                boolean available = availableCheck.isSelected();
                String projects = projectsField.getText().trim();

                if (id.isEmpty() || name.isEmpty()) {
                    showError("ID and Name cannot be empty!");
                    return;
                }

                String line = String.join("|", id, name, String.valueOf(age), skills,
                                  String.valueOf(available), projects);

                try (FileWriter fw = new FileWriter(USER_FILE, true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(line + "\n");
                }

                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(0)).getViewport().getView(), USER_FILE);
                dialog.dispose();
                showMessage("Employee added successfully!");

            } catch (NumberFormatException ex) {
                showError("Age must be a number!");
            } catch (IOException ex) {
                showError("Error saving employee: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editEmployee(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee to edit!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String id = (String) model.getValueAt(selectedRow, 0);
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\\|");
                if (parts[0].equals(id)) {
                    showEditEmployeeDialog(parts, i);
                    return;
                }
            }
            showError("Employee not found in file!");
        } catch (IOException e) {
            showError("Error reading employee data: " + e.getMessage());
        }
    }
    
    private void showEditEmployeeDialog(String[] employeeData, int lineNumber) {
        JDialog dialog = new JDialog(this, "Edit Employee", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField(employeeData[0]);
        JTextField nameField = new JTextField(employeeData[1]);
        JTextField ageField = new JTextField(employeeData[2]);
        JTextField skillsField = new JTextField(employeeData[3]);
        JCheckBox availableCheck = new JCheckBox();
        availableCheck.setSelected(Boolean.parseBoolean(employeeData[4]));
        JTextField projectsField = new JTextField(employeeData.length > 5 ? employeeData[5] : "");
        
        idField.setEditable(false);
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Age:"));
        formPanel.add(ageField);
        formPanel.add(new JLabel("Skills (comma separated):"));
        formPanel.add(skillsField);
        formPanel.add(new JLabel("Available:"));
        formPanel.add(availableCheck);
        formPanel.add(new JLabel("Past Projects (comma separated):"));
        formPanel.add(projectsField);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                int age = Integer.parseInt(ageField.getText().trim());
                String skills = skillsField.getText().trim();
                boolean available = availableCheck.isSelected();
                String projects = projectsField.getText().trim();
                
                if (name.isEmpty()) {
                    showError("Name cannot be empty!");
                    return;
                }
                
                String newLine = String.join("|", id, name, String.valueOf(age), skills, 
                                           String.valueOf(available), projects);
                
                List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
                lines.set(lineNumber, newLine);
                Files.write(Paths.get(USER_FILE), lines);
                
                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(0)).getViewport().getView(), USER_FILE);
                dialog.dispose();
                showMessage("Employee updated successfully!");
            } catch (NumberFormatException ex) {
                showError("Age must be a number!");
            } catch (IOException ex) {
                showError("Error updating employee: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteEmployee(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select an employee to delete!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String id = (String) model.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete employee " + id + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(USER_FILE));
                List<String> newLines = lines.stream()
                    .filter(line -> !line.startsWith(id + "|"))
                    .collect(Collectors.toList());
                
                Files.write(Paths.get(USER_FILE), newLines);
                refreshTable(table, USER_FILE);
                showMessage("Employee deleted successfully!");
            } catch (IOException e) {
                showError("Error deleting employee: " + e.getMessage());
            }
        }
    }
    
    // ========== Skill Panel ==========
    private JPanel createSkillPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Table for displaying skills
        JTable skillTable = createSkillTable();
        JScrollPane scrollPane = new JScrollPane(skillTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Skill");
        JButton editButton = new JButton("Edit Skill");
        JButton deleteButton = new JButton("Delete Skill");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddSkillDialog());
        editButton.addActionListener(e -> editSkill(skillTable));
        deleteButton.addActionListener(e -> deleteSkill(skillTable));
        refreshButton.addActionListener(e -> refreshTable(skillTable, SKILL_FILE));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createSkillTable() {
        String[] columnNames = {"Skill ID", "Name", "Category", "Level", "User ID"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try (BufferedReader reader = new BufferedReader(new FileReader(SKILL_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    model.addRow(Arrays.copyOf(parts, 5));
                }
            }
        } catch (IOException e) {
            showError("Error loading skill data: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        
        return table;
    }
    
    private void showAddSkillDialog() {
        JDialog dialog = new JDialog(this, "Add New Skill", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField categoryField = new JTextField();
        JTextField levelField = new JTextField();
        JTextField userIdField = new JTextField();
        
        formPanel.add(new JLabel("Skill ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Level:"));
        formPanel.add(levelField);
        formPanel.add(new JLabel("User ID:"));
        formPanel.add(userIdField);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String skillId = idField.getText().trim();
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();
                String level = levelField.getText().trim();
                String userId = userIdField.getText().trim();
                
                if (skillId.isEmpty() || name.isEmpty() || userId.isEmpty()) {
                    showError("Required fields cannot be empty!");
                    return;
                }
                
                String line = String.join("|", skillId, name, category, level, userId);
                
                try (FileWriter fw = new FileWriter(SKILL_FILE, true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(line + "\n");
                }
                
                // Update user's skill set
                updateUserSkill(userId, name);
                
                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(1)).getViewport().getView(), SKILL_FILE);
                dialog.dispose();
                showMessage("Skill added successfully!");
            } catch (IOException ex) {
                showError("Error saving skill: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editSkill(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a skill to edit!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String skillId = (String) model.getValueAt(selectedRow, 0);
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(SKILL_FILE));
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\\|");
                if (parts[0].equals(skillId)) {
                    showEditSkillDialog(parts, i);
                    return;
                }
            }
            showError("Skill not found in file!");
        } catch (IOException e) {
            showError("Error reading skill data: " + e.getMessage());
        }
    }
    
    private void showEditSkillDialog(String[] skillData, int lineNumber) {
        JDialog dialog = new JDialog(this, "Edit Skill", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField(skillData[0]);
        JTextField nameField = new JTextField(skillData[1]);
        JTextField categoryField = new JTextField(skillData[2]);
        JTextField levelField = new JTextField(skillData[3]);
        JTextField userIdField = new JTextField(skillData[4]);
        
        idField.setEditable(false);
        
        formPanel.add(new JLabel("Skill ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Level:"));
        formPanel.add(levelField);
        formPanel.add(new JLabel("User ID:"));
        formPanel.add(userIdField);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String skillId = idField.getText().trim();
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();
                String level = levelField.getText().trim();
                String userId = userIdField.getText().trim();
                
                if (name.isEmpty() || userId.isEmpty()) {
                    showError("Required fields cannot be empty!");
                    return;
                }
                
                String newLine = String.join("|", skillId, name, category, level, userId);
                
                List<String> lines = Files.readAllLines(Paths.get(SKILL_FILE));
                lines.set(lineNumber, newLine);
                Files.write(Paths.get(SKILL_FILE), lines);
                
                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(1)).getViewport().getView(), SKILL_FILE);
                dialog.dispose();
                showMessage("Skill updated successfully!");
            } catch (IOException ex) {
                showError("Error updating skill: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void deleteSkill(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a skill to delete!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String skillId = (String) model.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete skill " + skillId + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(SKILL_FILE));
                List<String> newLines = lines.stream()
                    .filter(line -> !line.startsWith(skillId + "|"))
                    .collect(Collectors.toList());
                
                Files.write(Paths.get(SKILL_FILE), newLines);
                refreshTable(table, SKILL_FILE);
                showMessage("Skill deleted successfully!");
            } catch (IOException e) {
                showError("Error deleting skill: " + e.getMessage());
            }
        }
    }
    
    // ========== Project Panel ==========
    private JPanel createProjectPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Table for displaying projects
        JTable projectTable = createProjectTable();
        JScrollPane scrollPane = new JScrollPane(projectTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Project");
        JButton editButton = new JButton("Edit Project");
        JButton deleteButton = new JButton("Delete Project");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddProjectDialog());
        editButton.addActionListener(e -> editProject(projectTable));
        deleteButton.addActionListener(e -> deleteProject(projectTable));
        refreshButton.addActionListener(e -> refreshTable(projectTable, PROJECT_FILE));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createProjectTable() {
        String[] columnNames = {"Project ID", "Required Skills", "Team Size", "Description", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try (BufferedReader reader = new BufferedReader(new FileReader(PROJECT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 5) {
                    model.addRow(Arrays.copyOf(parts, 5));
                }
            }
        } catch (IOException e) {
            showError("Error loading project data: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        
        return table;
    }
    
    private void showAddProjectDialog() {
        JDialog dialog = new JDialog(this, "Add New Project", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField skillsField = new JTextField();
        JTextField sizeField = new JTextField();
        JTextField descField = new JTextField();
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"not_started", "on_going", "completed"});
        
        formPanel.add(new JLabel("Project ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Required Skills (comma separated):"));
        formPanel.add(skillsField);
        formPanel.add(new JLabel("Team Size:"));
        formPanel.add(sizeField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String pid = idField.getText().trim();
                String skills = skillsField.getText().trim();
                int teamSize = Integer.parseInt(sizeField.getText().trim());
                String desc = descField.getText().trim();
                String status = (String) statusCombo.getSelectedItem();
                
                if (pid.isEmpty() || skills.isEmpty()) {
                    showError("Project ID and Required Skills cannot be empty!");
                    return;
                }
                
                String line = String.join("|", pid, skills, String.valueOf(teamSize), desc, status);
                
                try (FileWriter fw = new FileWriter(PROJECT_FILE, true);
                     BufferedWriter bw = new BufferedWriter(fw)) {
                    bw.write(line + "\n");
                }
                
                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(2)).getViewport().getView(), PROJECT_FILE);
                dialog.dispose();
                showMessage("Project added successfully!");
            } catch (NumberFormatException ex) {
                showError("Team Size must be a number!");
            } catch (IOException ex) {
                showError("Error saving project: " + ex.getMessage());
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editProject(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a project to edit!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String projectId = (String) model.getValueAt(selectedRow, 0);
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(PROJECT_FILE));
            for (int i = 0; i < lines.size(); i++) {
                String[] parts = lines.get(i).split("\\|");
                if (parts[0].equals(projectId)) {
                    showEditProjectDialog(parts, i);
                    return;
                }
            }
            showError("Project not found in file!");
        } catch (IOException e) {
            showError("Error reading project data: " + e.getMessage());
        }
    }
    
    private void showEditProjectDialog(String[] projectData, int lineNumber) {
        JDialog dialog = new JDialog(this, "Edit Project", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField(projectData[0]);
        JTextField skillsField = new JTextField(projectData[1]);
        JTextField sizeField = new JTextField(projectData[2]);
        JTextField descField = new JTextField(projectData[3]);
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{"not_started", "on_going", "completed"});
        statusCombo.setSelectedItem(projectData[4]);
        
        idField.setEditable(false);
        
        formPanel.add(new JLabel("Project ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Required Skills (comma separated):"));
        formPanel.add(skillsField);
        formPanel.add(new JLabel("Team Size:"));
        formPanel.add(sizeField);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(descField);
        formPanel.add(new JLabel("Status:"));
        formPanel.add(statusCombo);
        
        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                String pid = idField.getText().trim();
                String skills = skillsField.getText().trim();
                int teamSize = Integer.parseInt(sizeField.getText().trim());
                String desc = descField.getText().trim();
                String newStatus = (String) statusCombo.getSelectedItem();
                
                if (skills.isEmpty()) {
                    showError("Required Skills cannot be empty!");
                    return;
                }
                
                // Get current project data
                String[] currentProject = getProjectDetails(pid);
                if (currentProject == null) {
                    showError("Project not found in system!");
                    return;
                }
                
                String currentStatus = currentProject[4];
                
                // Validate status transitions
                if (currentStatus.equals("on_going") && !newStatus.equals("completed")) {
                    showError("Project is in progress! Can only change status to 'completed'");
                    return;
                }
                
                // If changing to completed, make team members available
                if (newStatus.equals("completed") && currentStatus.equals("on_going")) {
                    // Get team members from scoring data
                    List<String> teamMembers = getTeamMembersForProject(pid);
                    if (teamMembers != null && !teamMembers.isEmpty()) {
                        for (String userId : teamMembers) {
                            updateUserAvailability(userId, true);
                        }
                        showMessage("Project marked as completed. Team members are now available.");
                    }
                }
                // If changing from not_started to on_going, validate team exists
                else if (newStatus.equals("on_going") && currentStatus.equals("not_started")) {
                    List<String> teamMembers = getTeamMembersForProject(pid);
                    if (teamMembers == null || teamMembers.isEmpty()) {
                        showError("Cannot start project without a team! Form a team first.");
                        return;
                    }
                    
                    // Mark team members as unavailable
                    for (String userId : teamMembers) {
                        updateUserAvailability(userId, false);
                    }
                }
                
                String newLine = String.join("|", pid, skills, String.valueOf(teamSize), desc, newStatus);
                
                List<String> lines = Files.readAllLines(Paths.get(PROJECT_FILE));
                lines.set(lineNumber, newLine);
                Files.write(Paths.get(PROJECT_FILE), lines);
                
                refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(2)).getViewport().getView(), PROJECT_FILE);
                dialog.dispose();
                showMessage("Project updated successfully!");
                
                // If status changed to completed, refresh employee table
                if (newStatus.equals("completed")) {
                    refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(0)).getViewport().getView(), USER_FILE);
                }
            } catch (NumberFormatException ex) {
                showError("Team Size must be a number!");
            } catch (IOException ex) {
                showError("Error updating project: " + ex.getMessage());
            }
        });
      
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // Helper method to get team members for a project
        private List<String> getTeamMembersForProject(String projectId) throws IOException {
            File scoringFile = new File("data_scoring.txt");
            if (!scoringFile.exists()) {
                return null;
            }
            
            try (BufferedReader br = new BufferedReader(new FileReader(scoringFile))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\|");
                    if (parts.length >= 3 && parts[2].equals(projectId)) {
                        return Arrays.asList(parts[1].split(","));
                    }
                }
            }
            return null;
        }
    
    private void deleteProject(JTable table) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showError("Please select a project to delete!");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String projectId = (String) model.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete project " + projectId + "?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(PROJECT_FILE));
                List<String> newLines = lines.stream()
                    .filter(line -> !line.startsWith(projectId + "|"))
                    .collect(Collectors.toList());
                
                Files.write(Paths.get(PROJECT_FILE), newLines);
                refreshTable(table, PROJECT_FILE);
                showMessage("Project deleted successfully!");
            } catch (IOException e) {
                showError("Error deleting project: " + e.getMessage());
            }
        }
    }
    
    // ========== Collaboration Panel ==========
    private JPanel createCollaborationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Table for displaying collaborations
        JTable collabTable = createCollaborationTable();
        JScrollPane scrollPane = new JScrollPane(collabTable);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Collaboration");
        JButton editButton = new JButton("Edit Collaboration");
        JButton deleteButton = new JButton("Delete Collaboration");
        JButton refreshButton = new JButton("Refresh");
        
        addButton.addActionListener(e -> showAddCollaborationDialog());
        editButton.addActionListener(e -> editCollaboration(collabTable));
        deleteButton.addActionListener(e -> deleteCollaboration(collabTable));
        refreshButton.addActionListener(e -> refreshTable(collabTable, COLLAB_FILE));
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        
        panel.add(buttonPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTable createCollaborationTable() {
        String[] columnNames = {"Users", "Collab Count", "Success Rate", "Compatibility"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        try (BufferedReader reader = new BufferedReader(new FileReader(COLLAB_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {

                    model.addRow(Arrays.copyOf(parts, 4));
                }
            }
        } catch (IOException e) {
            showError("Error loading collaboration data: " + e.getMessage());
        }
        
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        
        return table;
    }
    
    private void showAddCollaborationDialog() {
    JDialog dialog = new JDialog(this, "Add New Collaboration", true);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(400, 270);
    dialog.setLocationRelativeTo(this);

    JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
    formPanel.setBorder(new EmptyBorder(15, 15, 10, 15));

    JTextField userAField = new JTextField();
    JTextField userBField = new JTextField();
    JTextField countField = new JTextField();
    JTextField rateField = new JTextField();
    JTextField compField = new JTextField();

    formPanel.add(new JLabel("User A:"));
    formPanel.add(userAField);
    formPanel.add(new JLabel("User B:"));
    formPanel.add(userBField);
    formPanel.add(new JLabel("Collab Count:"));
    formPanel.add(countField);
    formPanel.add(new JLabel("Success Rate:"));
    formPanel.add(rateField);
    formPanel.add(new JLabel("Compatibility:"));
    formPanel.add(compField);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");

    saveButton.addActionListener(e -> {
        try {
            String userA = userAField.getText().trim();
            String userB = userBField.getText().trim();
            int count = Integer.parseInt(countField.getText().trim());
            double rate = Double.parseDouble(rateField.getText().trim());
            double comp = Double.parseDouble(compField.getText().trim());

            if (userA.isEmpty() || userB.isEmpty()) {
                showError("User IDs cannot be empty!");
                return;
            }

            String line = String.join("|", userA + "," + userB,
                                      String.valueOf(count),
                                      String.valueOf(rate),
                                      String.valueOf(comp));

            try (FileWriter fw = new FileWriter(COLLAB_FILE, true);
                 BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(line + "\n");
            }

            collabGraph.computeIfAbsent(userA, k -> new HashSet<>()).add(userB);
            collabGraph.computeIfAbsent(userB, k -> new HashSet<>()).add(userA);

            refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(3)).getViewport().getView(), COLLAB_FILE);
            dialog.dispose();
            showMessage("Collaboration added successfully!");
        } catch (NumberFormatException ex) {
            showError("Count, Rate, and Compatibility must be numbers!");
        } catch (IOException ex) {
            showError("Error saving collaboration: " + ex.getMessage());
        }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
}

private void editCollaboration(JTable table) {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
        showError("Please select a collaboration to edit!");
        return;
    }
    
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    String users = (String) model.getValueAt(selectedRow, 0);
    
    try {
        List<String> lines = Files.readAllLines(Paths.get(COLLAB_FILE));
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\\|");
            if (parts[0].equals(users)) {
                showEditCollaborationDialog(parts, i);
                return;
            }
        }
        showError("Collaboration not found in file!");
    } catch (IOException e) {
        showError("Error reading collaboration data: " + e.getMessage());
    }
}

private void showEditCollaborationDialog(String[] collabData, int lineNumber) {
    JDialog dialog = new JDialog(this, "Edit Collaboration", true);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(400, 270);
    dialog.setLocationRelativeTo(this);

    JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
    formPanel.setBorder(new EmptyBorder(15, 15, 10, 15));

    String[] users = collabData[0].split(",");
    JTextField userAField = new JTextField(users[0]);
    JTextField userBField = new JTextField(users.length > 1 ? users[1] : "");
    JTextField countField = new JTextField(collabData[1]);
    JTextField rateField = new JTextField(collabData[2]);
    JTextField compField = new JTextField(collabData[3]);

    userAField.setEditable(false);
    userBField.setEditable(false);

    formPanel.add(new JLabel("User A:"));
    formPanel.add(userAField);
    formPanel.add(new JLabel("User B:"));
    formPanel.add(userBField);
    formPanel.add(new JLabel("Collab Count:"));
    formPanel.add(countField);
    formPanel.add(new JLabel("Success Rate:"));
    formPanel.add(rateField);
    formPanel.add(new JLabel("Compatibility:"));
    formPanel.add(compField);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    JButton saveButton = new JButton("Save");
    JButton cancelButton = new JButton("Cancel");

    saveButton.addActionListener(e -> {
        try {
            String userA = userAField.getText().trim();
            String userB = userBField.getText().trim();
            int count = Integer.parseInt(countField.getText().trim());
            double rate = Double.parseDouble(rateField.getText().trim());
            double comp = Double.parseDouble(compField.getText().trim());

            String newLine = String.join("|", userA + "," + userB,
                                         String.valueOf(count),
                                         String.valueOf(rate),
                                         String.valueOf(comp));

            List<String> lines = Files.readAllLines(Paths.get(COLLAB_FILE));
            lines.set(lineNumber, newLine);
            Files.write(Paths.get(COLLAB_FILE), lines);

            refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(3)).getViewport().getView(), COLLAB_FILE);
            dialog.dispose();
            showMessage("Collaboration updated successfully!");
        } catch (NumberFormatException ex) {
            showError("Count, Rate, and Compatibility must be numbers!");
        } catch (IOException ex) {
            showError("Error updating collaboration: " + ex.getMessage());
        }
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setVisible(true);
}


private void deleteCollaboration(JTable table) {
    int selectedRow = table.getSelectedRow();
    if (selectedRow == -1) {
        showError("Please select a collaboration to delete!");
        return;
    }
    
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    String users = (String) model.getValueAt(selectedRow, 0);
    
    int confirm = JOptionPane.showConfirmDialog(this, 
        "Are you sure you want to delete collaboration between " + users + "?", 
        "Confirm Delete", JOptionPane.YES_NO_OPTION);
    
    if (confirm == JOptionPane.YES_OPTION) {
        try {
            List<String> lines = Files.readAllLines(Paths.get(COLLAB_FILE));
            List<String> newLines = lines.stream()
                .filter(line -> !line.startsWith(users + "|"))
                .collect(Collectors.toList());
            
            Files.write(Paths.get(COLLAB_FILE), newLines);
            
            // Update collaboration graph
            String[] userPair = users.split(",");
            if (userPair.length == 2) {
                if (collabGraph.containsKey(userPair[0])) {
                    collabGraph.get(userPair[0]).remove(userPair[1]);
                }
                if (collabGraph.containsKey(userPair[1])) {
                    collabGraph.get(userPair[1]).remove(userPair[0]);
                }
            }
            
            refreshTable(table, COLLAB_FILE);
            showMessage("Collaboration deleted successfully!");
        } catch (IOException e) {
            showError("Error deleting collaboration: " + e.getMessage());
        }
    }
}

// ========== Team Formation Panel ==========
private JPanel createTeamFormationPanel() {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setBorder(new EmptyBorder(10, 10, 10, 10));
    
    // Project selection panel
    JPanel projectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel projectLabel = new JLabel("Select Project:");
    JComboBox<String> projectCombo = new JComboBox<>();
    JButton loadProjectsButton = new JButton("Load Projects");
    
    loadProjectsButton.addActionListener(e -> {
        projectCombo.removeAllItems();
        try (BufferedReader reader = new BufferedReader(new FileReader(PROJECT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length > 0) {
                    projectCombo.addItem(parts[0]);
                }
            }
        } catch (IOException ex) {
            showError("Error loading projects: " + ex.getMessage());
        }
    });
    
    projectPanel.add(projectLabel);
    projectPanel.add(projectCombo);
    projectPanel.add(loadProjectsButton);
    
    // Algorithm selection panel
    JPanel algoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel algoLabel = new JLabel("Algorithm:");
    ButtonGroup algoGroup = new ButtonGroup();
    JRadioButton dfsButton = new JRadioButton("DFS");
    JRadioButton bfsButton = new JRadioButton("BFS");
    JButton compareButton = new JButton("Compare Algorithms");
    
    algoGroup.add(dfsButton);
    algoGroup.add(bfsButton);
    dfsButton.setSelected(true);
    
    algoPanel.add(algoLabel);
    algoPanel.add(dfsButton);
    algoPanel.add(bfsButton);
    algoPanel.add(compareButton);
    
    // Team formation button
    JButton formTeamButton = new JButton("Form Team");
    
    // Results area
    JTextArea resultsArea = new JTextArea();
    resultsArea.setEditable(false);
    JScrollPane resultsScroll = new JScrollPane(resultsArea);
    
    // Team details panel
    JPanel teamDetailsPanel = new JPanel(new BorderLayout());
    JLabel teamLabel = new JLabel("Team Details:");
    JTextArea teamDetailsArea = new JTextArea();
    teamDetailsArea.setEditable(false);
    JScrollPane teamDetailsScroll = new JScrollPane(teamDetailsArea);
    
    teamDetailsPanel.add(teamLabel, BorderLayout.NORTH);
    teamDetailsPanel.add(teamDetailsScroll, BorderLayout.CENTER);
    
    // Score details panel
    JPanel scorePanel = new JPanel(new BorderLayout());
    JLabel scoreLabel = new JLabel("Score Details:");
    JTextArea scoreArea = new JTextArea();
    scoreArea.setEditable(false);
    JScrollPane scoreScroll = new JScrollPane(scoreArea);
    
    scorePanel.add(scoreLabel, BorderLayout.NORTH);
    scorePanel.add(scoreScroll, BorderLayout.CENTER);
    
    // Save button panel
    JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveTeamButton = new JButton("Save Team Recommendation");
    saveTeamButton.setEnabled(false);
    
    JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    splitPane.setTopComponent(resultsScroll);
    
    JSplitPane bottomSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    bottomSplit.setLeftComponent(teamDetailsPanel);
    bottomSplit.setRightComponent(scorePanel);
    splitPane.setBottomComponent(bottomSplit);
    
    splitPane.setDividerLocation(0.5);
    bottomSplit.setDividerLocation(0.5);
    
    // Form team action
    formTeamButton.addActionListener(e -> {
        String projectId = (String) projectCombo.getSelectedItem();
        if (projectId == null) {
            showError("Please select a project first!");
            return;
        }

        
        try {
            String[] project = getProjectDetails(projectId);
            if (project == null) {
                showError("Project not found!");
                return;
            }

             if (project[4].equals("on_going")) {
            showError("This project is already in progress and cannot have another team assigned!");
            return;
            }
            
            List<String> team;
            if (dfsButton.isSelected()) {
                team = rekomendasiTimDFS(projectId);
            } else {
                team = rekomendasiTimBFS(projectId);
            }
            
            if (team.isEmpty()) {
                resultsArea.setText("No suitable team found for this project.");
                saveTeamButton.setEnabled(false);
                return;
            }
            
            int requiredTeamSize = Integer.parseInt(project[2].trim());
            if (team.size() != requiredTeamSize) {
                resultsArea.setText(String.format(
                    "ERROR: Recommended team has %d members, but project requires %d members\n" +
                    "Please try another formation algorithm.",
                    team.size(), requiredTeamSize));
                saveTeamButton.setEnabled(false);
                return;
            }
            
            // Calculate scores
            Map<String, Double> userToProjectScores = new HashMap<>();
            Map<String, Double> userPairScores = new HashMap<>();
            List<String> pairScoresOutput = new ArrayList<>();
            
            // UserToProjectMatch scores
            for (String userId : team) {
                double skillMatchScore = hitungSkillMatchScore(userId, projectId);
                double availabilityBonus = hitungAvailabilityBonus(userId);
                double userToProjectMatch = (skillMatchScore * 0.9) + (availabilityBonus * 0.1);
                userToProjectScores.put(userId, userToProjectMatch);
            }
            
            // UserPairScore for all pairs
            for (int i = 0; i < team.size(); i++) {
                for (int j = i + 1; j < team.size(); j++) {
                    String user1 = team.get(i);
                    String user2 = team.get(j);
                    String pairKey = user1 + "-" + user2;
                    
                    double successRate = hitungSuccessRate(user1, user2);
                    double compatibility = hitungCompatibility(user1, user2);
                    double collabBonus = hitungCollabBonus(user1, user2);
                    
                    double userPairScore = (successRate * 0.5 + compatibility * 0.3 + collabBonus * 0.2);
                    userPairScores.put(pairKey, userPairScore);
                    
                    String pairScoreDetail = String.format(
                        "Pair %s-%s: SuccessRate=%.2f, Compatibility=%.2f, CollabBonus=%.2f -> Score=%.2f",
                        user1, user2, successRate, compatibility, collabBonus, userPairScore);
                    pairScoresOutput.add(pairScoreDetail);
                }
            }
            
            // CliqueScore (average of all UserPairScores)
            double cliqueScore = userPairScores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
            
            // Display results
            resultsArea.setText("=== TEAM FORMATION RESULTS ===\n");
            resultsArea.append("Algorithm: " + (dfsButton.isSelected() ? "DFS" : "BFS") + "\n");
            resultsArea.append("Project: " + projectId + " - " + project[3] + "\n");
            resultsArea.append("Required Skills: " + project[1] + "\n");
            resultsArea.append("Required Team Size: " + requiredTeamSize + "\n\n");
            resultsArea.append("Clique Score: " + String.format("%.2f", cliqueScore) + 
                            " (Average of all UserPair Scores)\n");
            
            // Team details
            teamDetailsArea.setText("=== TEAM MEMBERS ===\n");
            for (String userId : team) {
                String[] user = getUserDetails(userId);
                if (user != null) {
                    teamDetailsArea.append(String.format(
                        "- %s (%s)\n  Skills: %s\n  Available: %s\n  Past Projects: %s\n\n",
                        userId, user[1], user[3], user[4], user.length > 5 ? user[5] : ""));
                } else {
                    teamDetailsArea.append("- " + userId + " (Details not found)\n");
                }
            }
            
            // Score details
            scoreArea.setText("=== USER TO PROJECT MATCH SCORES ===\n");
            scoreArea.append("Formula: (SkillMatchScore x 0.9) + (AvailabilityBonus x 0.1)\n");
            for (Map.Entry<String, Double> entry : userToProjectScores.entrySet()) {
                scoreArea.append(entry.getKey() + ": " + String.format("%.2f", entry.getValue()) + "\n");
            }
            
            scoreArea.append("\n=== USER PAIR SCORES ===\n");
            scoreArea.append("Formula: (SuccessRate x 0.5 + Compatibility x 0.3 + CollabBonus x 0.2)\n");
            for (String detail : pairScoresOutput) {
                scoreArea.append(detail + "\n");
            }
            
            // Enable save button and set action
            saveTeamButton.setEnabled(true);
            saveTeamButton.addActionListener(ev -> {
                try {
                    // 1. Simpan data scoring terlebih dahulu
                    simpanDataScoring(projectId, team, userPairScores, cliqueScore);
                    
                    // 2. Update status proyek
                    if (!updateProjectStatus(projectId, "on_going")) {
                        showError("Gagal mengupdate status proyek");
                        return;
                    }
                    
                    // 3. Tampilkan pesan sukses
                    showMessage("Tim berhasil disimpan! Status proyek diubah menjadi on_going");
                    
                    // 4. Refresh tabel proyek
                    refreshTable((JTable)((JScrollPane)tabbedPane.getComponentAt(2))
                                .getViewport().getView(), PROJECT_FILE);
                    
                } catch (IOException ex) {
                    showError("Error: " + ex.getMessage());
                    saveTeamButton.setEnabled(false);
                }
        });
            
        } catch (IOException ex) {
            showError("Error forming team: " + ex.getMessage());
            saveTeamButton.setEnabled(false);
        }
    });
    
    // Compare algorithms action
    compareButton.addActionListener(e -> {
        String projectId = (String) projectCombo.getSelectedItem();
        if (projectId == null) {
            showError("Please select a project first!");
            return;
        }
        
        try {
            long startTime, endTime;
            
            // DFS Analysis
            startTime = System.nanoTime();
            List<String> dfsTeam = rekomendasiTimDFS(projectId);
            endTime = System.nanoTime();
            long dfsTime = endTime - startTime;
            
            // BFS Analysis
            startTime = System.nanoTime();
            List<String> bfsTeam = rekomendasiTimBFS(projectId);
            endTime = System.nanoTime();
            long bfsTime = endTime - startTime;
            
            double dfsTimeMs = dfsTime / 1_000_000.0;
            double bfsTimeMs = bfsTime / 1_000_000.0;
            
            resultsArea.setText("=== ALGORITHM COMPARISON ===\n");
            resultsArea.append("Project: " + projectId + "\n\n");
            resultsArea.append("DFS Results:\n");
            resultsArea.append("- Execution Time: " + String.format("%.3f", dfsTimeMs) + " ms\n");
            resultsArea.append("- Team Size: " + dfsTeam.size() + "\n");
            resultsArea.append("- Team Members: " + String.join(", ", dfsTeam) + "\n\n");
            
            resultsArea.append("BFS Results:\n");
            resultsArea.append("- Execution Time: " + String.format("%.3f", bfsTimeMs) + " ms\n");
            resultsArea.append("- Team Size: " + bfsTeam.size() + "\n");
            resultsArea.append("- Team Members: " + String.join(", ", bfsTeam) + "\n\n");
            
            resultsArea.append("Conclusion:\n");
            resultsArea.append("- DFS is " + (dfsTime < bfsTime ? "faster" : "slower") + " than BFS for this project\n");
            resultsArea.append("- DFS is better for: Teams with deep collaboration chains\n");
            resultsArea.append("- BFS is better for: Teams with direct and even collaboration\n");
            
            saveTeamButton.setEnabled(false);
            
        } catch (IOException ex) {
            showError("Error comparing algorithms: " + ex.getMessage());
            saveTeamButton.setEnabled(false);
        }
    });
    
    savePanel.add(saveTeamButton);
    
    JPanel controlPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    controlPanel.add(projectPanel);
    controlPanel.add(algoPanel);
    controlPanel.add(formTeamButton);
    
    panel.add(controlPanel, BorderLayout.NORTH);
    panel.add(splitPane, BorderLayout.CENTER);
    panel.add(savePanel, BorderLayout.SOUTH);
    
    return panel;
}

// ========== Helper Methods ==========
private void refreshTable(JTable table, String filename) {
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    model.setRowCount(0);
    
    try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|");
            model.addRow(parts);
        }
    } catch (IOException e) {
        showError("Error refreshing data: " + e.getMessage());
    }
}

private void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
}

private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
}

// ========== Data Processing Methods ==========
private void buildGraph() throws IOException {
    collabGraph.clear();
    File file = new File(COLLAB_FILE);
    if (!file.exists()) {
        return;
    }
    
    try (BufferedReader br = new BufferedReader(new FileReader(COLLAB_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            try {
                String[] parts = line.split("\\|");
                String[] pair = parts[0].split(",");
                collabGraph.computeIfAbsent(pair[0], k -> new HashSet<>()).add(pair[1]);
                collabGraph.computeIfAbsent(pair[1], k -> new HashSet<>()).add(pair[0]);
            } catch (Exception e) {
                continue;
            }
        }
    }
}

private Set<String> dfs(String start) {
    Set<String> visited = new HashSet<>();
    Stack<String> stack = new Stack<>();
    stack.push(start);
    
    while (!stack.isEmpty()) {
        String current = stack.pop();
        if (visited.contains(current)) continue;
        
        visited.add(current);
        for (String neighbor : collabGraph.getOrDefault(current, new HashSet<>())) {
            if (!visited.contains(neighbor)) {
                stack.push(neighbor);
            }
        }
    }
    return visited;
}

private Set<String> bfs(String start) {
    Set<String> visited = new HashSet<>();
    Queue<String> queue = new LinkedList<>();
    queue.offer(start);
    visited.add(start);
    
    while (!queue.isEmpty()) {
        String current = queue.poll();
        
        for (String neighbor : collabGraph.getOrDefault(current, new HashSet<>())) {
            if (!visited.contains(neighbor)) {
                visited.add(neighbor);
                queue.offer(neighbor);
            }
        }
    }
    return visited;
}

private String[] getProjectDetails(String pid) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(PROJECT_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            try {
                String[] parts = line.split("\\|");
                if (parts[0].equals(pid)) {
                    return parts;
                }
            } catch (Exception e) {
                continue;
            }
        }
    }
    return null;
}

private String[] getUserDetails(String uid) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            try {
                String[] parts = line.split("\\|");
                if (parts[0].equals(uid)) {
                    return parts;
                }
            } catch (Exception e) {
                continue;
            }
        }
    }
    return null;
}

private List<String[]> getAllUsers() throws IOException {
    List<String[]> users = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            try {
                users.add(line.split("\\|"));
            } catch (Exception e) {
                continue;
            }
        }
    }
    return users;
}

private List<String> rekomendasiTimDFS(String pid) throws IOException {
    String[] project = getProjectDetails(pid);
    if (project == null) {
        return new ArrayList<>();
    }
    
    String requiredSkills = project[1];
    int teamSize = Integer.parseInt(project[2]);
    
    List<String[]> users = getAllUsers();
    Set<String> reqSkillSet = new HashSet<>(Arrays.asList(requiredSkills.split(",")));
    
    if (collabGraph.isEmpty()) {
        buildGraph();
    }

    String startUser = findBestStartUser(users, reqSkillSet);
    if (startUser == null) {
        return new ArrayList<>();
    }

    Set<String> reachable = dfs(startUser);
    return selectTeamMembers(users, reachable, reqSkillSet, teamSize);
}

private List<String> rekomendasiTimBFS(String pid) throws IOException {
    String[] project = getProjectDetails(pid);
    if (project == null) {
        return new ArrayList<>();
    }
    
    String requiredSkills = project[1];
    int teamSize = Integer.parseInt(project[2]);
    
    List<String[]> users = getAllUsers();
    Set<String> reqSkillSet = new HashSet<>(Arrays.asList(requiredSkills.split(",")));
    
    if (collabGraph.isEmpty()) {
        buildGraph();
    }

    String startUser = findBestStartUser(users, reqSkillSet);
    if (startUser == null) {
        return new ArrayList<>();
    }

    Set<String> reachable = bfs(startUser);
    return selectTeamMembers(users, reachable, reqSkillSet, teamSize);
}

private String findBestStartUser(List<String[]> users, Set<String> reqSkillSet) {
    String bestUser = null;
    int maxMatches = 0;
    
    for (String[] user : users) {
        try {
            if (!Boolean.parseBoolean(user[4])) continue;
            
            Set<String> skills = new HashSet<>(Arrays.asList(user[3].split(",")));
            skills.retainAll(reqSkillSet);
            
            if (skills.size() > maxMatches) {
                maxMatches = skills.size();
                bestUser = user[0];
            }
        } catch (Exception e) {
            continue;
        }
    }
    return bestUser;
}

private List<String> selectTeamMembers(List<String[]> users, Set<String> reachable, 
                                 Set<String> reqSkillSet, int teamSize) {
    List<String> selected = new ArrayList<>();
    
    try {
        users.sort((a, b) -> {
            try {
                Set<String> aSkills = new HashSet<>(Arrays.asList(a[3].split(",")));
                Set<String> bSkills = new HashSet<>(Arrays.asList(b[3].split(",")));
                aSkills.retainAll(reqSkillSet);
                bSkills.retainAll(reqSkillSet);
                return Integer.compare(bSkills.size(), aSkills.size());
            } catch (Exception e) {
                return 0;
            }
        });
        
        for (String[] user : users) {
            if (selected.size() >= teamSize) break;
            try {
                if (!reachable.contains(user[0])) continue;
                if (!Boolean.parseBoolean(user[4])) continue;
                
                Set<String> skills = new HashSet<>(Arrays.asList(user[3].split(",")));
                skills.retainAll(reqSkillSet);
                if (!skills.isEmpty()) {
                    selected.add(user[0]);
                }
            } catch (Exception e) {
                continue;
            }
        }
    } catch (Exception e) {
        return new ArrayList<>();
    }
    return selected;
}

private double hitungSkillMatchScore(String userId, String projectId) throws IOException {
    String[] user = getUserDetails(userId);
    String[] project = getProjectDetails(projectId);
    
    if (user == null || project == null) {
        return 0.0;
    }
    
    Set<String> userSkills = new HashSet<>(Arrays.asList(user[3].split(",")));
    Set<String> requiredSkills = new HashSet<>(Arrays.asList(project[1].split(",")));
    
    userSkills.retainAll(requiredSkills);
    return (double) userSkills.size() / requiredSkills.size() * 10; 
}

private double hitungAvailabilityBonus(String userId) throws IOException {
    String[] user = getUserDetails(userId);
    if (user == null) {
        return 0.0;
    }
    return Boolean.parseBoolean(user[4]) ? 10.0 : 0.0; 
}

private double hitungSuccessRate(String user1, String user2) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(COLLAB_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            String[] users = parts[0].split(",");
            
            if ((users[0].equals(user1) && users[1].equals(user2)) || 
                (users[0].equals(user2) && users[1].equals(user1))) {
                return Double.parseDouble(parts[2]); 
            }
        }
    }
    return 0.5; 
}

private double hitungCompatibility(String user1, String user2) throws IOException {
    try (BufferedReader br = new BufferedReader(new FileReader(COLLAB_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            String[] users = parts[0].split(",");
            
            if ((users[0].equals(user1) && users[1].equals(user2)) || 
                (users[0].equals(user2) && users[1].equals(user1))) {
                return Double.parseDouble(parts[3]); 
            }
        }
    }
    return 0.5; 
}

private double hitungCollabBonus(String user1, String user2) throws IOException {
    double count = 0.0;
    try (BufferedReader br = new BufferedReader(new FileReader(COLLAB_FILE))) {
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length < 2) continue;

            String[] users = parts[0].split(",");

            if ((users[0].equals(user1) && users[1].equals(user2)) ||
                (users[0].equals(user2) && users[1].equals(user1))) {
                count += Double.parseDouble(parts[1].trim()); 
            }
        }
    }
    return Math.min(1.0, count * 0.2); 
}

private void simpanDataScoring(String projectId, List<String> team, 
                            Map<String, Double> userPairScores,
                            double cliqueScore) throws IOException {
    File file = new File("data_scoring.txt");
    
    int teamNumber = 1;
    if (file.exists()) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            while (br.readLine() != null) {
                teamNumber++;
            }
        }
    }
    
    String teamId = "T" + String.format("%02d", teamNumber);
    
    try (PrintWriter writer = new PrintWriter(new FileWriter(file, true))) {
        writer.printf("%s|%s|%s|%.2f%n",
            teamId,
            String.join(",", team),
            projectId,
            cliqueScore);
    }
    
    System.out.println("Data scoring saved for team " + teamId);
}

private void updateUserAvailability(String userId, boolean available) throws IOException {
    File inputFile = new File(USER_FILE);
    File tempFile = new File("temp.txt");

    try (
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
    ) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length >= 5 && parts[0].equals(userId)) {
                parts[4] = String.valueOf(available);
                line = String.join("|", parts);
            }
            writer.write(line + "\n");
        }
    }

    if (!inputFile.delete()) {
        throw new IOException("Failed to delete old user file");
    }
    if (!tempFile.renameTo(inputFile)) {
        throw new IOException("Failed to rename temp user file");
    }
}

private boolean updateProjectStatus(String pid, String newStatus) throws IOException {
    File inputFile = new File(PROJECT_FILE);
    if (!inputFile.exists()) {
        return false;
    }
    
    File tempFile = new File("temp.txt");
    boolean updated = false;
    
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
         BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {
        
        String line;
        while ((line = br.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length >= 5 && parts[0].equals(pid)) {
                // Validasi perubahan status
                if (parts[4].equals("completed")) {
                    showError("Proyek sudah selesai, tidak bisa diubah");
                    return false;
                }
                
                if (newStatus.equals("on_going") && !parts[4].equals("not_started")) {
                    showError("Hanya bisa ubah status dari not_started ke on_going");
                    return false;
                }
                
                parts[4] = newStatus;
                line = String.join("|", parts);
                updated = true;
            }
            bw.write(line + "\n");
        }
    }
    
    if (updated) {
        Files.move(tempFile.toPath(), inputFile.toPath(), 
                  StandardCopyOption.REPLACE_EXISTING);
        return true;
    }
    return false;
}

private void updateUserSkill(String userId, String newSkill) throws IOException {
    File inputFile = new File(USER_FILE);
    File tempFile = new File("temp.txt");

    try (
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
    ) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split("\\|");
            if (parts.length < 5) continue;

            if (parts[0].equals(userId)) {
                Set<String> updatedSkills = new HashSet<>(Arrays.asList(parts[3].split(",")));
                if (!updatedSkills.contains(newSkill)) {
                    updatedSkills.add(newSkill);
                }
                parts[3] = String.join(",", updatedSkills);
                line = String.join("|", parts); 
            }

            writer.write(line + "\n");
        }
    }

    if (!inputFile.delete()) {
        showError("Failed to delete old file.");
        return;
    }
    if (!tempFile.renameTo(inputFile)) {
        showError("Failed to rename temp file.");
    } else {
        showMessage("User skills updated successfully with new skill.");
    }
}

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            Font largerFont = new Font("Segoe UI", Font.PLAIN, 14); 

            UIManager.put("Label.font", largerFont);
            UIManager.put("Button.font", largerFont);
            UIManager.put("TextField.font", largerFont);
            UIManager.put("TextArea.font", largerFont);
            UIManager.put("Table.font", largerFont);
            UIManager.put("ComboBox.font", largerFont);
            UIManager.put("TabbedPane.font", largerFont);
            UIManager.put("RadioButton.font", largerFont);
            UIManager.put("CheckBox.font", largerFont);
            UIManager.put("TitledBorder.font", largerFont);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        TeamManagerUI app = new TeamManagerUI();
        app.setVisible(true);
    });
}
}
