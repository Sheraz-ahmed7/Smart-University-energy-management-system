package GUI;

import Models.User;
import Models.Department;
import database.Queries;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {

    private final User currentUser;
    private JTable           table;
    private DefaultTableModel model;
    private JTextField       usernameField, searchField;
    private JPasswordField   passwordField, confirmField;
    private JComboBox<String>      roleCombo;
    private JComboBox<Department>  deptCombo;
    private JButton          addBtn, updateBtn, deleteBtn, clearBtn;
    private JLabel           formTitle;
    private int              editingUserId = -1;

    public UsersPanel(User currentUser) {
        this.currentUser = currentUser;
        if (!currentUser.isAdmin()) {
            setLayout(new BorderLayout());
            setOpaque(false);
            JLabel deny = Theme.label("⛔  Access denied — Admins only", Theme.FONT_HEADING, Theme.RED);
            deny.setHorizontalAlignment(SwingConstants.CENTER);
            add(deny, BorderLayout.CENTER);
            return;
        }
        setOpaque(false);
        setLayout(new BorderLayout(0,0));
        buildUI();
        loadUsers();
    }

    private void buildUI() {
        JPanel left = buildForm();
        JPanel right = buildTablePanel();

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setDividerLocation(310);
        split.setDividerSize(6);
        split.setOpaque(false);
        split.setBorder(null);
        split.setBackground(Theme.BG_DARK);
        add(split, BorderLayout.CENTER);
    }

    // ── Form ───────────────────────────────────────────────────
    private JPanel buildForm() {
        JPanel outer = Theme.darkPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16,16,16,8));

        JPanel card = Theme.card(null);
        card.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL; c.weightx=1; c.gridx=0;

        formTitle = Theme.label("Add New User", Theme.FONT_HEADING, Theme.ACCENT);
        c.gridy=0; c.insets=new Insets(0,0,16,0);
        card.add(formTitle, c);

        // Username
        c.gridy=1; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Username", Theme.FONT_BOLD, Theme.TEXT_SECONDARY), c);
        c.gridy=2; c.insets=new Insets(0,0,12,0);
        usernameField = Theme.field("Enter username");
        usernameField.setPreferredSize(new Dimension(260,38));
        card.add(usernameField, c);

        // Password
        c.gridy=3; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Password", Theme.FONT_BOLD, Theme.TEXT_SECONDARY), c);
        c.gridy=4; c.insets=new Insets(0,0,12,0);
        passwordField = Theme.passField("Enter password");
        passwordField.setPreferredSize(new Dimension(260,38));
        card.add(passwordField, c);

        // Confirm password
        c.gridy=5; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Confirm Password", Theme.FONT_BOLD, Theme.TEXT_SECONDARY), c);
        c.gridy=6; c.insets=new Insets(0,0,12,0);
        confirmField = Theme.passField("Confirm password");
        confirmField.setPreferredSize(new Dimension(260,38));
        card.add(confirmField, c);

        // Role
        c.gridy=7; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Role", Theme.FONT_BOLD, Theme.TEXT_SECONDARY), c);
        c.gridy=8; c.insets=new Insets(0,0,12,0);
        roleCombo = new JComboBox<>(new String[]{"staff","admin"});
        Theme.styleCombo(roleCombo);
        roleCombo.setPreferredSize(new Dimension(260,38));
        roleCombo.addActionListener(e -> deptCombo.setEnabled("staff".equals(roleCombo.getSelectedItem())));
        card.add(roleCombo, c);

        // Department
        c.gridy=9; c.insets=new Insets(0,0,5,0);
        card.add(Theme.label("Department (staff only)", Theme.FONT_BOLD, Theme.TEXT_SECONDARY), c);
        c.gridy=10; c.insets=new Insets(0,0,20,0);
        deptCombo = new JComboBox<>();
        Theme.styleCombo(deptCombo);
        deptCombo.setPreferredSize(new Dimension(260,38));
        loadDepartments();
        card.add(deptCombo, c);

        // Buttons
        JPanel btnRow = new JPanel(new GridLayout(2,2,8,8));
        btnRow.setOpaque(false);
        addBtn    = Theme.button("Add User",  Theme.ACCENT_DARK);
        updateBtn = Theme.button("Update",    new Color(160,100,0));
        deleteBtn = Theme.button("Delete",    new Color(140,30,30));
        clearBtn  = Theme.button("Clear",     new Color(50,70,54));
        updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        btnRow.add(addBtn); btnRow.add(updateBtn);
        btnRow.add(deleteBtn); btnRow.add(clearBtn);

        addBtn.addActionListener(e    -> addUser());
        updateBtn.addActionListener(e -> updateUser());
        deleteBtn.addActionListener(e -> deleteUser());
        clearBtn.addActionListener(e  -> clearForm());

        c.gridy=11; c.insets=new Insets(0,0,0,0);
        card.add(btnRow, c);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── Table ──────────────────────────────────────────────────
    private JPanel buildTablePanel() {
        JPanel outer = Theme.darkPanel(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(16,8,16,16));

        // Search bar
        JPanel topBar = new JPanel(new BorderLayout(10,0));
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(0,0,10,0));
        JLabel title = Theme.label("Registered Users", Theme.FONT_HEADING, Theme.TEXT_PRIMARY);
        searchField = Theme.field("🔍 Search users…");
        searchField.setPreferredSize(new Dimension(220,36));
        searchField.addActionListener(e -> filterTable(searchField.getText()));
        topBar.add(title,       BorderLayout.WEST);
        topBar.add(searchField, BorderLayout.EAST);

        // Table
        model = new DefaultTableModel(new String[]{"ID","Username","Role","Department"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if(!e.getValueIsAdjusting() && table.getSelectedRow()>=0) loadSelectedToForm();
        });

        JPanel card = Theme.card(null);
        card.setLayout(new BorderLayout(0,10));
        card.add(topBar,              BorderLayout.NORTH);
        card.add(Theme.scroll(table), BorderLayout.CENTER);

        outer.add(card, BorderLayout.CENTER);
        return outer;
    }

    // ── Data operations ────────────────────────────────────────
    private void loadDepartments() {
        deptCombo.removeAllItems();
        deptCombo.addItem(new Department(0,"— None (Admin) —",0,""));
        for (Department d : Queries.getAllDepartments()) deptCombo.addItem(d);
    }

    private void loadUsers() {
        model.setRowCount(0);
        for (User u : Queries.getAllUsers()) {
            String deptName = "—";
            if (u.getDepartmentId() != null && u.getDepartmentId() > 0) {
                Department d = Queries.getDepartmentById(u.getDepartmentId());
                if (d != null) deptName = d.getDeptName();
            }
            model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(), deptName});
        }
    }

    private void filterTable(String text) {
        model.setRowCount(0);
        for (User u : Queries.getAllUsers()) {
            if (text.isEmpty() || text.equals("🔍 Search users…") ||
                u.getUsername().toLowerCase().contains(text.toLowerCase()) ||
                u.getRole().toLowerCase().contains(text.toLowerCase())) {
                String deptName = "—";
                if (u.getDepartmentId() != null && u.getDepartmentId() > 0) {
                    Department d = Queries.getDepartmentById(u.getDepartmentId());
                    if (d != null) deptName = d.getDeptName();
                }
                model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getRole(), deptName});
            }
        }
    }

    private void loadSelectedToForm() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        editingUserId = (int) model.getValueAt(row, 0);
        String uname = (String) model.getValueAt(row, 1);
        String role  = (String) model.getValueAt(row, 2);

        usernameField.setText(uname); usernameField.setForeground(Theme.TEXT_PRIMARY);
        passwordField.setText(""); passwordField.setEchoChar('●');
        confirmField.setText(""); confirmField.setEchoChar('●');
        roleCombo.setSelectedItem(role);

        formTitle.setText("Edit User: " + uname);
        addBtn.setEnabled(false); updateBtn.setEnabled(true); deleteBtn.setEnabled(true);
    }

    private void clearForm() {
        editingUserId = -1;
        usernameField.setText("Enter username"); usernameField.setForeground(Theme.TEXT_SECONDARY);
        passwordField.setEchoChar((char)0); passwordField.setText("Enter password");
        confirmField.setEchoChar((char)0); confirmField.setText("Confirm password");
        roleCombo.setSelectedIndex(0);
        deptCombo.setSelectedIndex(0);
        formTitle.setText("Add New User");
        addBtn.setEnabled(true); updateBtn.setEnabled(false); deleteBtn.setEnabled(false);
        table.clearSelection();
    }

    private void addUser() {
        if (!validateForm(true)) return;
        String uname = usernameField.getText().trim();
        String pass  = new String(passwordField.getPassword());
        String role  = (String) roleCombo.getSelectedItem();
        Department dept = (Department) deptCombo.getSelectedItem();
        Integer deptId = (dept != null && dept.getDeptId() > 0) ? dept.getDeptId() : null;

        if (Queries.getUserByUsername(uname) != null) {
            JOptionPane.showMessageDialog(this,"Username already exists","Error",JOptionPane.ERROR_MESSAGE); return;
        }
        User u = new User(uname, hashPassword(pass), role, deptId);
        if (Queries.addUser(u)) {
            JOptionPane.showMessageDialog(this,"User added successfully!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadUsers(); clearForm();
        } else {
            JOptionPane.showMessageDialog(this,"Failed to add user","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateUser() {
        if (editingUserId < 0) return;
        if (!validateForm(false)) return;
        String uname = usernameField.getText().trim();
        String role  = (String) roleCombo.getSelectedItem();
        Department dept = (Department) deptCombo.getSelectedItem();
        Integer deptId = (dept != null && dept.getDeptId() > 0) ? dept.getDeptId() : null;
        String pass = new String(passwordField.getPassword());
        String newPass = (pass.isEmpty() || pass.equals("Enter password")) ? null : hashPassword(pass);

        if (Queries.updateUser(editingUserId, uname, newPass, role, deptId)) {
            JOptionPane.showMessageDialog(this,"User updated!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadUsers(); clearForm();
        } else {
            JOptionPane.showMessageDialog(this,"Failed to update user","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        if (editingUserId == currentUser.getUserId()) {
            JOptionPane.showMessageDialog(this,"Cannot delete your own account!","Warning",JOptionPane.WARNING_MESSAGE); return;
        }
        int r = JOptionPane.showConfirmDialog(this,"Delete this user?","Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if (r == JOptionPane.YES_OPTION && Queries.deleteUser(editingUserId)) {
            JOptionPane.showMessageDialog(this,"User deleted!","Success",JOptionPane.INFORMATION_MESSAGE);
            loadUsers(); clearForm();
        }
    }

    private boolean validateForm(boolean checkConfirm) {
        String uname = usernameField.getText().trim();
        if (uname.isEmpty() || uname.equals("Enter username")) {
            JOptionPane.showMessageDialog(this,"Username is required","Validation",JOptionPane.ERROR_MESSAGE); return false;
        }
        if (checkConfirm) {
            String pass = new String(passwordField.getPassword());
            String conf = new String(confirmField.getPassword());
            if (pass.isEmpty() || pass.equals("Enter password")) {
                JOptionPane.showMessageDialog(this,"Password is required","Validation",JOptionPane.ERROR_MESSAGE); return false;
            }
            if (pass.length() < 6) {
                JOptionPane.showMessageDialog(this,"Password must be at least 6 characters","Validation",JOptionPane.ERROR_MESSAGE); return false;
            }
            if (!pass.equals(conf)) {
                JOptionPane.showMessageDialog(this,"Passwords do not match","Validation",JOptionPane.ERROR_MESSAGE); return false;
            }
        }
        return true;
    }

    /** Simple SHA-256 hash */
    private String hashPassword(String plain) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return plain; }
    }
}