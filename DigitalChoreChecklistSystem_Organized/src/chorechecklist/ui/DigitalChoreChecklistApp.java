package chorechecklist.ui;

import chorechecklist.abstraction.Task;
import chorechecklist.inheritance.HouseholdTask;
import chorechecklist.inheritance.StudentTask;
import chorechecklist.encapsulation.TaskManager;
import chorechecklist.polymorphism.TaskFormatter;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Main entry point and Swing graphical user interface (sidebar dashboard layout). */
public class DigitalChoreChecklistApp extends JFrame {

    static {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
        catch (Exception ignored) { /* fall back to the default look and feel */ }
    }

    // ---------- Colour palette (restrained, corporate) ----------
    private static final Color SIDEBAR       = new Color(15, 23, 42);
    private static final Color SIDEBAR_SEL   = new Color(30, 41, 59);
    private static final Color SIDEBAR_TEXT  = new Color(203, 213, 225);
    private static final Color SIDEBAR_MUTED = new Color(100, 116, 139);
    private static final Color BG            = new Color(241, 245, 249);
    private static final Color CARD          = Color.WHITE;
    private static final Color BORDER        = new Color(226, 232, 240);
    private static final Color FIELD_BORDER  = new Color(203, 213, 225);
    private static final Color TEXT          = new Color(15, 23, 42);
    private static final Color BODY_TEXT     = new Color(51, 65, 85);
    private static final Color MUTED         = new Color(100, 116, 139);
    private static final Color PRIMARY       = new Color(37, 99, 235);
    private static final Color GREEN         = new Color(22, 163, 74);
    private static final Color AMBER         = new Color(217, 119, 6);
    private static final Color RED           = new Color(220, 38, 38);
    private static final Color SELECTED      = new Color(238, 243, 254);
    private static final Color ROW_LINE      = new Color(241, 245, 249);
    private static final Color HEADER_BG     = new Color(248, 250, 252);

    // Priority colours: red = High, yellow = Medium, green = Low
    private static final Color PRI_HIGH = new Color(220, 38, 38);
    private static final Color PRI_MED  = new Color(234, 179, 8);
    private static final Color PRI_LOW  = new Color(22, 163, 74);

    private static Color priorityColor(String p) {
        return Task.HIGH.equals(p) ? PRI_HIGH : Task.LOW.equals(p) ? PRI_LOW : PRI_MED;
    }
    private static Color priorityChipBg(String p) {
        return Task.HIGH.equals(p) ? new Color(254, 226, 226) : Task.LOW.equals(p) ? new Color(220, 252, 231) : new Color(254, 243, 199);
    }
    private static Color priorityChipFg(String p) {
        return Task.HIGH.equals(p) ? new Color(185, 28, 28) : Task.LOW.equals(p) ? new Color(21, 128, 61) : new Color(133, 77, 14);
    }
    private static Color priorityTint(String p) {
        return Task.HIGH.equals(p) ? new Color(254, 242, 242) : Task.LOW.equals(p) ? new Color(240, 253, 244) : new Color(254, 252, 232);
    }

    // Table column positions
    private static final int COL_ID = 0, COL_TASK = 1, COL_CAT = 2, COL_PRI = 3, COL_DUE = 4, COL_STATUS = 5, COL_DETAILS = 6;

    // ---------- Fonts ----------
    private static final String FAMILY = pickFont();
    private static final Font F_BODY  = new Font(FAMILY, Font.PLAIN, 13);
    private static final Font F_BOLD  = new Font(FAMILY, Font.BOLD, 13);
    private static final Font F_SMALL = new Font(FAMILY, Font.PLAIN, 12);
    private static final Font F_CAP   = new Font(FAMILY, Font.BOLD, 11);
    private static final Font F_BADGE = new Font(FAMILY, Font.BOLD, 11);

    private static String pickFont() {
        String[] wanted = {"Segoe UI", "Inter", "Helvetica Neue", "Open Sans"};
        String[] have = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String w : wanted) for (String h : have) if (h.equals(w)) return w;
        return "SansSerif";
    }

    // ---------- Application state ----------
    private final TaskManager manager = new TaskManager();
    private final TaskFormatter formatter = new TaskFormatter();
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new Object[]{"ID","Task","Category","Priority","Due Date","Status","Details"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
        @Override public Class<?> getColumnClass(int col) { return col == 0 ? Integer.class : Object.class; }
    };
    private final JTable table = new JTable(tableModel);
    private String categoryFilter = "All";
    private String priorityFilter = "All";
    private boolean priorityView = false;
    private final HintTextField search = new HintTextField("Search tasks...");

    private final NavItem navAll, navHousehold, navAcademic, navPriority;
    private final JLabel pageTitle = new JLabel("All Tasks");
    private final JLabel pageSubtitle = new JLabel();

    private final JLabel totalValue = new JLabel("0");
    private final JLabel doneValue = new JLabel("0");
    private final JLabel pendingValue = new JLabel("0");
    private final JLabel overdueValue = new JLabel("0");

    private final JLabel sideRate = new JLabel("0.0%");
    private final JLabel sideCaption = new JLabel("0 of 0 tasks completed");
    private final RateBar sideBar = new RateBar(new Color(30, 41, 59), new Color(59, 130, 246));

    private final CardLayout tableCards = new CardLayout();
    private final JPanel tableHolder = new JPanel(tableCards);
    private final JLabel emptyTitle = new JLabel("", SwingConstants.CENTER);
    private final JLabel emptyHint = new JLabel("", SwingConstants.CENTER);

    public DigitalChoreChecklistApp() {
        super("Digital Chore Checklist System");
        navAll = new NavItem("All Tasks", () -> selectCategory("All"));
        navHousehold = new NavItem("Household", () -> selectCategory("Household"));
        navAcademic = new NavItem("Academic", () -> selectCategory("Academic"));
        navPriority = new NavItem("Priority View", () -> selectPriorityView());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1020, 640));
        buildUi();
        setSize(1180, 720);
        setLocationRelativeTo(null);
        refreshTable();
    }

    // =====================================================================
    //  Layout
    // =====================================================================
    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        setContentPane(root);
        root.add(buildSidebar(), BorderLayout.WEST);

        JPanel main = new JPanel(new BorderLayout(0, 18));
        main.setBackground(BG);
        main.setBorder(new EmptyBorder(26, 30, 22, 30));

        JPanel top = new JPanel(new BorderLayout(0, 20));
        top.setOpaque(false);
        top.add(buildHeader(), BorderLayout.NORTH);
        top.add(buildStats(), BorderLayout.CENTER);
        main.add(top, BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildActions(), BorderLayout.NORTH);
        body.add(buildTableCard(), BorderLayout.CENTER);
        main.add(body, BorderLayout.CENTER);

        root.add(main, BorderLayout.CENTER);
        installShortcuts();
    }

    private JComponent buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(SIDEBAR);
        side.setPreferredSize(new Dimension(244, 100));

        // Brand
        JPanel brand = new JPanel(new BorderLayout(12, 0));
        brand.setOpaque(false);
        brand.setBorder(new EmptyBorder(26, 22, 22, 20));
        brand.add(new BrandMark(), BorderLayout.WEST);
        JLabel b1 = new JLabel("Digital Chore");
        b1.setFont(new Font(FAMILY, Font.BOLD, 15));
        b1.setForeground(Color.WHITE);
        JLabel b2 = new JLabel("Checklist System");
        b2.setFont(F_SMALL);
        b2.setForeground(SIDEBAR_MUTED);
        JPanel names = new JPanel(new GridLayout(2, 1, 0, 1));
        names.setOpaque(false);
        names.add(b1);
        names.add(b2);
        brand.add(names, BorderLayout.CENTER);

        // Navigation
        JPanel nav = new JPanel();
        nav.setOpaque(false);
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        JLabel section = new JLabel("TASKS");
        section.setFont(F_CAP);
        section.setForeground(SIDEBAR_MUTED);
        section.setBorder(new EmptyBorder(4, 26, 10, 0));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(section);
        for (NavItem n : new NavItem[]{navAll, navHousehold, navAcademic}) {
            n.setAlignmentX(Component.LEFT_ALIGNMENT);
            nav.add(n);
            nav.add(Box.createVerticalStrut(4));
        }
        JLabel viewsSection = new JLabel("VIEWS");
        viewsSection.setFont(F_CAP);
        viewsSection.setForeground(SIDEBAR_MUTED);
        viewsSection.setBorder(new EmptyBorder(16, 26, 10, 0));
        viewsSection.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(viewsSection);
        navPriority.setAlignmentX(Component.LEFT_ALIGNMENT);
        nav.add(navPriority);

        JPanel upper = new JPanel(new BorderLayout());
        upper.setOpaque(false);
        upper.add(brand, BorderLayout.NORTH);
        upper.add(nav, BorderLayout.CENTER);
        side.add(upper, BorderLayout.NORTH);

        // Progress block
        JPanel prog = new JPanel(new GridLayout(4, 1, 0, 6));
        prog.setOpaque(false);
        prog.setBorder(new EmptyBorder(0, 26, 26, 26));
        JLabel pc = new JLabel("OVERALL PROGRESS");
        pc.setFont(F_CAP);
        pc.setForeground(SIDEBAR_MUTED);
        sideRate.setFont(new Font(FAMILY, Font.BOLD, 26));
        sideRate.setForeground(Color.WHITE);
        sideCaption.setFont(F_SMALL);
        sideCaption.setForeground(SIDEBAR_MUTED);
        prog.add(pc);
        prog.add(sideRate);
        prog.add(sideBar);
        prog.add(sideCaption);
        side.add(prog, BorderLayout.SOUTH);
        return side;
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        pageTitle.setFont(new Font(FAMILY, Font.BOLD, 26));
        pageTitle.setForeground(TEXT);
        pageSubtitle.setFont(F_BODY);
        pageSubtitle.setForeground(MUTED);
        JPanel titles = new JPanel(new GridLayout(2, 1, 0, 3));
        titles.setOpaque(false);
        titles.add(pageTitle);
        titles.add(pageSubtitle);
        header.add(titles, BorderLayout.WEST);

        search.setPreferredSize(new Dimension(260, 38));
        search.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshTable(); }
            public void removeUpdate(DocumentEvent e) { refreshTable(); }
            public void changedUpdate(DocumentEvent e) { refreshTable(); }
        });
        RoundButton add = new RoundButton("+  Add Task", RoundButton.PRIMARY);
        add.addActionListener(e -> showTaskDialog(null));
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setOpaque(false);
        right.add(search);
        right.add(add);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JComponent buildStats() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.add(statCard("Total tasks", totalValue, PRIMARY));
        row.add(statCard("Completed", doneValue, GREEN));
        row.add(statCard("Pending", pendingValue, AMBER));
        row.add(statCard("Overdue", overdueValue, RED));
        return row;
    }

    private RoundedPanel statCard(String caption, JLabel value, Color dot) {
        RoundedPanel card = new RoundedPanel(new BorderLayout(0, 6), 10);
        card.setBorder(new EmptyBorder(14, 18, 14, 18));
        JLabel cap = new JLabel(caption, new DotIcon(dot), SwingConstants.LEFT);
        cap.setIconTextGap(8);
        cap.setFont(F_BOLD);
        cap.setForeground(MUTED);
        value.setFont(new Font(FAMILY, Font.BOLD, 30));
        value.setForeground(TEXT);
        card.add(cap, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JComponent buildActions() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);
        RoundButton edit = new RoundButton("Edit Selected", RoundButton.SECONDARY);
        RoundButton status = new RoundButton("Toggle Status", RoundButton.SECONDARY);
        RoundButton delete = new RoundButton("Delete", RoundButton.DANGER);
        RoundButton progress = new RoundButton("Progress Summary", RoundButton.SECONDARY);
        left.add(edit); left.add(status); left.add(delete); left.add(progress);
        bar.add(left, BorderLayout.WEST);
        left.setBorder(new EmptyBorder(0, -8, 0, 0));

        JLabel filterLabel = new JLabel("Priority");
        filterLabel.setFont(F_BOLD);
        filterLabel.setForeground(BODY_TEXT);
        final JComboBox<String> priorityCombo = new JComboBox<String>(new String[]{"All Priorities", "High", "Medium", "Low"});
        priorityCombo.setFont(F_BODY);
        priorityCombo.setBackground(Color.WHITE);
        priorityCombo.setPreferredSize(new Dimension(160, 36));
        priorityCombo.setRenderer(new PriorityListRenderer());
        priorityCombo.addActionListener(e -> {
            priorityFilter = priorityCombo.getSelectedIndex() == 0 ? "All" : (String) priorityCombo.getSelectedItem();
            refreshTable();
        });
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);
        right.add(filterLabel);
        right.add(priorityCombo);
        bar.add(right, BorderLayout.EAST);

        edit.addActionListener(e -> editSelected());
        status.addActionListener(e -> toggleSelected());
        delete.addActionListener(e -> deleteSelected());
        progress.addActionListener(e -> showProgressDialog());
        return bar;
    }

    private JComponent buildTableCard() {
        table.setRowHeight(46);
        table.setFont(F_BODY);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(CARD);
        table.setSelectionBackground(SELECTED);
        table.setSelectionForeground(TEXT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(newSorter());
        table.getTableHeader().setReorderingAllowed(false);
        int[] widths = {56, 190, 110, 115, 110, 115, 280};
        for (int i = 0; i < widths.length; i++) table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        TextCellRenderer text = new TextCellRenderer();
        BadgeRenderer badge = new BadgeRenderer();
        table.getColumnModel().getColumn(COL_ID).setCellRenderer(text);
        table.getColumnModel().getColumn(COL_TASK).setCellRenderer(text);
        table.getColumnModel().getColumn(COL_CAT).setCellRenderer(badge);
        table.getColumnModel().getColumn(COL_PRI).setCellRenderer(badge);
        table.getColumnModel().getColumn(COL_DUE).setCellRenderer(text);
        table.getColumnModel().getColumn(COL_STATUS).setCellRenderer(badge);
        table.getColumnModel().getColumn(COL_DETAILS).setCellRenderer(text);

        JTableHeader header = table.getTableHeader();
        header.setPreferredSize(new Dimension(100, 40));
        header.setDefaultRenderer(new HeaderRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(CARD);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel empty = new JPanel(new GridBagLayout());
        empty.setOpaque(false);
        JPanel emptyBox = new JPanel(new GridLayout(2, 1, 0, 6));
        emptyBox.setOpaque(false);
        emptyTitle.setFont(new Font(FAMILY, Font.BOLD, 17));
        emptyTitle.setForeground(TEXT);
        emptyHint.setFont(F_BODY);
        emptyHint.setForeground(MUTED);
        emptyBox.add(emptyTitle);
        emptyBox.add(emptyHint);
        empty.add(emptyBox);

        tableHolder.setOpaque(false);
        tableHolder.add(scroll, "table");
        tableHolder.add(empty, "empty");

        RoundedPanel card = new RoundedPanel(new BorderLayout(), 10);
        card.setBorder(new EmptyBorder(1, 1, 1, 1));
        card.add(tableHolder, BorderLayout.CENTER);

        table.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { maybePopup(e); }
            @Override public void mouseReleased(MouseEvent e) { maybePopup(e); }
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && table.rowAtPoint(e.getPoint()) >= 0)
                    editSelected();
            }
        });
        return card;
    }

    private void maybePopup(MouseEvent e) {
        if (!e.isPopupTrigger()) return;
        int row = table.rowAtPoint(e.getPoint());
        if (row < 0) return;
        table.setRowSelectionInterval(row, row);
        JPopupMenu menu = new JPopupMenu();
        JMenuItem edit = new JMenuItem("Edit Selected");
        JMenuItem toggle = new JMenuItem("Toggle Status");
        JMenuItem delete = new JMenuItem("Delete");
        edit.addActionListener(a -> editSelected());
        toggle.addActionListener(a -> toggleSelected());
        delete.addActionListener(a -> deleteSelected());
        menu.add(edit); menu.add(toggle); menu.addSeparator(); menu.add(delete);
        menu.show(table, e.getX(), e.getY());
    }

    private void installShortcuts() {
        JRootPane rp = getRootPane();
        rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK), "newTask");
        rp.getActionMap().put("newTask", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { showTaskDialog(null); }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteTask");
        table.getActionMap().put("deleteTask", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { deleteSelected(); }
        });
        table.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "editTask");
        table.getActionMap().put("editTask", new AbstractAction() {
            public void actionPerformed(ActionEvent e) { if (table.getSelectedRow() >= 0) editSelected(); }
        });
    }

    // =====================================================================
    //  Actions (same behaviour as the original application)
    // =====================================================================
    private void selectCategory(String category) {
        priorityView = false;
        categoryFilter = category;
        refreshTable();
    }

    /** Priority View: all tasks, High on top, Medium in the middle, Low at the bottom. */
    private void selectPriorityView() {
        priorityView = true;
        categoryFilter = "All";
        refreshTable();
    }

    /** Normal sorter: click a header to sort; the Priority column sorts High > Medium > Low. */
    private TableRowSorter<DefaultTableModel> newSorter() {
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<DefaultTableModel>(tableModel);
        sorter.setComparator(COL_PRI, (a, b) -> Integer.compare(priorityRank(String.valueOf(a)), priorityRank(String.valueOf(b))));
        return sorter;
    }

    private static int priorityRank(String p) {
        return Task.HIGH.equals(p) ? 0 : Task.MEDIUM.equals(p) ? 1 : 2;
    }

    private Task selectedTask() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a task first."); return null; }
        int modelRow = table.convertRowIndexToModel(row);
        return manager.findTaskById((Integer) tableModel.getValueAt(modelRow, 0));
    }

    private void editSelected() {
        Task t = selectedTask();
        if (t != null) showTaskDialog(t);
    }

    private void toggleSelected() {
        Task t = selectedTask();
        if (t == null) return;
        if ("Completed".equals(t.getStatus())) t.markAsPending(); else t.markAsCompleted();
        refreshTable();
    }

    private void deleteSelected() {
        Task t = selectedTask();
        if (t == null) return;
        int answer = JOptionPane.showConfirmDialog(this, "Delete '" + t.getTaskName() + "'?",
            "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) { manager.deleteTask(t.getTaskId()); refreshTable(); }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        String q = search.getText().trim().toLowerCase();
        // Priority View uses a fixed High > Medium > Low order, so header sorting is switched off there.
        if (priorityView) {
            if (table.getRowSorter() != null) table.setRowSorter(null);
        } else if (table.getRowSorter() == null) {
            table.setRowSorter(newSorter());
        }
        List<Task> tasks = priorityView
            ? manager.getTasksSortedByPriority(categoryFilter, priorityFilter)
            : manager.getFilteredTasks(categoryFilter, priorityFilter);
        for (Task t : tasks) {
            String details = formatter.formatSpecificDetails(t).replace("\n", ", ");
            if (!q.isEmpty()) {
                String hay = (t.getTaskName() + " " + t.getDescription() + " " + details + " "
                    + t.getCategory() + " " + t.getPriority() + " " + t.getStatus() + " " + t.getDueDate()).toLowerCase();
                if (!hay.contains(q)) continue;
            }
            tableModel.addRow(new Object[]{t.getTaskId(), t.getTaskName(), t.getCategory(), t.getPriority(), t.getDueDate(), t.getStatus(), details});
        }
        updateStats();
        if (tableModel.getRowCount() == 0) {
            if (manager.getTaskCount() == 0) {
                emptyTitle.setText("No tasks yet");
                emptyHint.setText("Click \"+ Add Task\" to create your first chore or assignment.");
            } else {
                emptyTitle.setText("No matching tasks");
                emptyHint.setText("Try a different search, category or priority.");
            }
            tableCards.show(tableHolder, "empty");
        } else {
            tableCards.show(tableHolder, "table");
        }
    }

    private int countOverdue() {
        int n = 0;
        LocalDate today = LocalDate.now();
        for (Task t : manager.getAllTasks())
            if (!"Completed".equals(t.getStatus()) && t.getDueDate().isBefore(today)) n++;
        return n;
    }

    private void updateStats() {
        totalValue.setText(String.valueOf(manager.getTaskCount()));
        doneValue.setText(String.valueOf(manager.getCompletedCount()));
        pendingValue.setText(String.valueOf(manager.getPendingCount()));
        overdueValue.setText(String.valueOf(countOverdue()));
        sideRate.setText(String.format("%.1f%%", manager.getCompletionRate()));
        sideBar.setValue(manager.getCompletionRate() / 100.0);
        sideCaption.setText(manager.getCompletedCount() + " of " + manager.getTaskCount() + " tasks completed");

        navAll.setCount(manager.getTasksByCategory("All").size());
        navHousehold.setCount(manager.getTasksByCategory("Household").size());
        navAcademic.setCount(manager.getTasksByCategory("Academic").size());
        navPriority.setCount(manager.getTaskCount());
        navAll.setSelected(!priorityView && "All".equals(categoryFilter));
        navHousehold.setSelected(!priorityView && "Household".equals(categoryFilter));
        navAcademic.setSelected(!priorityView && "Academic".equals(categoryFilter));
        navPriority.setSelected(priorityView);

        pageTitle.setText(priorityView ? "Priority View"
            : "All".equals(categoryFilter) ? "All Tasks" : categoryFilter + " Tasks");
        pageSubtitle.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
            + "   \u00B7   " + (priorityView ? "High \u2192 Medium \u2192 Low   \u00B7   " : "")
            + tableModel.getRowCount() + " shown");
    }

    // =====================================================================
    //  Dialogs
    // =====================================================================
    private void showProgressDialog() {
        final JDialog d = new JDialog(this, "Progress Summary", true);
        JPanel content = buildProgressPanel(() -> d.dispose());
        d.setContentPane(content);
        d.getRootPane().setDefaultButton((JButton) content.getClientProperty("defaultButton"));
        d.getRootPane().registerKeyboardAction(e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        d.setResizable(false);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setVisible(true);
    }

    private JPanel buildProgressPanel(final Runnable close) {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CARD);
        content.setPreferredSize(new Dimension(400, 400));

        JPanel body = new JPanel(new BorderLayout(0, 18));
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(26, 28, 20, 28));

        JLabel title = new JLabel("Progress Summary");
        title.setFont(new Font(FAMILY, Font.BOLD, 18));
        title.setForeground(TEXT);
        JLabel pct = new JLabel(String.format("%.1f%%", manager.getCompletionRate()));
        pct.setFont(new Font(FAMILY, Font.BOLD, 40));
        pct.setForeground(PRIMARY);
        JLabel cap = new JLabel("of all tasks are completed");
        cap.setFont(F_BODY);
        cap.setForeground(MUTED);
        JPanel top = new JPanel(new GridLayout(3, 1, 0, 2));
        top.setOpaque(false);
        top.add(title);
        top.add(pct);
        top.add(cap);

        RateBar bar = new RateBar(new Color(226, 232, 240), PRIMARY);
        bar.setValue(manager.getCompletionRate() / 100.0);

        JPanel rows = new JPanel(new GridLayout(4, 2, 0, 0));
        rows.setOpaque(false);
        addSummaryRow(rows, "Total tasks", String.valueOf(manager.getTaskCount()), TEXT);
        addSummaryRow(rows, "Completed", String.valueOf(manager.getCompletedCount()), GREEN);
        addSummaryRow(rows, "Pending", String.valueOf(manager.getPendingCount()), AMBER);
        addSummaryRow(rows, "Overdue", String.valueOf(countOverdue()), RED);

        JPanel middle = new JPanel(new BorderLayout(0, 16));
        middle.setOpaque(false);
        middle.add(bar, BorderLayout.NORTH);
        middle.add(rows, BorderLayout.CENTER);

        body.add(top, BorderLayout.NORTH);
        body.add(middle, BorderLayout.CENTER);

        RoundButton closeBtn = new RoundButton("Close", RoundButton.PRIMARY);
        closeBtn.addActionListener(e -> close.run());
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        foot.setBackground(HEADER_BG);
        foot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(14, 28, 14, 28)));
        foot.add(closeBtn);

        content.add(body, BorderLayout.CENTER);
        content.add(foot, BorderLayout.SOUTH);
        content.putClientProperty("defaultButton", closeBtn);
        return content;
    }

    private void addSummaryRow(JPanel p, String label, String value, Color valueColor) {
        JLabel l = new JLabel(label);
        l.setFont(F_BODY);
        l.setForeground(BODY_TEXT);
        JLabel v = new JLabel(value, SwingConstants.RIGHT);
        v.setFont(new Font(FAMILY, Font.BOLD, 15));
        v.setForeground(valueColor);
        Border line = BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_LINE);
        l.setBorder(line);
        v.setBorder(line);
        p.add(l);
        p.add(v);
    }

    private void showTaskDialog(Task existing) {
        final boolean editing = existing != null;
        final JDialog dialog = new JDialog(this, editing ? "Edit Task" : "Add New Task", true);
        JPanel content = buildTaskForm(existing, () -> dialog.dispose());
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton((JButton) content.getClientProperty("defaultButton"));
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.setResizable(false);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    private JPanel buildTaskForm(final Task existing, final Runnable close) {
        final boolean editing = existing != null;

        final JComboBox<String> category = new JComboBox<String>(new String[]{"Household", "Academic"});
        final JTextField name = field(), description = field(), due = field();
        final JTextField detail1 = field(), detail2 = field();
        final JComboBox<String> priority = new JComboBox<String>(new String[]{"High", "Medium", "Low"});
        priority.setSelectedItem("Medium");
        priority.setRenderer(new PriorityListRenderer());
        styleCombo(category);
        styleCombo(priority);

        final JLabel label1 = fieldLabel("Area");
        // Task type only applies to Academic tasks (Household tasks have no second detail field).
        final JPanel typeGroup = group(fieldLabel("Task type"), detail2);

        final JLabel error = new JLabel(" ");
        error.setFont(F_SMALL);
        error.setForeground(RED);

        category.addActionListener(e -> {
            boolean household = "Household".equals(category.getSelectedItem());
            label1.setText(household ? "Area" : "Subject");
            typeGroup.setVisible(!household);
        });

        if (editing) {
            category.setSelectedItem(existing.getCategory());
            category.setEnabled(false);
            name.setText(existing.getTaskName());
            description.setText(existing.getDescription());
            due.setText(existing.getDueDate().toString());
            priority.setSelectedItem(existing.getPriority());
            if (existing instanceof HouseholdTask) {
                HouseholdTask h = (HouseholdTask) existing;
                detail1.setText(h.getArea());
            } else {
                StudentTask s = (StudentTask) existing;
                detail1.setText(s.getSubject());
                detail2.setText(s.getType());
            }
        } else {
            due.setText(LocalDate.now().toString());
        }
        for (ActionListener al : category.getActionListeners()) al.actionPerformed(null);

        // ---- title ----
        JLabel ht = new JLabel(editing ? "Edit Task" : "Add New Task");
        ht.setFont(new Font(FAMILY, Font.BOLD, 19));
        ht.setForeground(TEXT);
        JLabel hs = new JLabel(editing ? "Update the task details below." : "Enter the details of the new task.");
        hs.setFont(F_BODY);
        hs.setForeground(MUTED);
        JPanel head = new JPanel(new GridLayout(2, 1, 0, 3));
        head.setBackground(CARD);
        head.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(22, 28, 16, 28)));
        head.add(ht);
        head.add(hs);

        // ---- form ----
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(new EmptyBorder(20, 28, 4, 28));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        c.gridx = 0; c.gridy = 0; c.insets = new Insets(0, 0, 14, 8);
        form.add(group(fieldLabel("Category"), category), c);
        c.gridx = 1; c.insets = new Insets(0, 8, 14, 0);
        form.add(group(fieldLabel("Due date (YYYY-MM-DD)"), due), c);

        c.gridx = 0; c.gridy = 1; c.gridwidth = 2; c.insets = new Insets(0, 0, 14, 0);
        form.add(group(fieldLabel("Task name"), name), c);
        c.gridy = 2;
        form.add(group(fieldLabel("Description"), description), c);

        c.gridy = 3; c.gridwidth = 1; c.insets = new Insets(0, 0, 4, 8);
        form.add(group(label1, detail1), c);
        c.gridx = 1; c.insets = new Insets(0, 8, 4, 0);
        form.add(group(fieldLabel("Priority"), priority), c);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 2; c.insets = new Insets(10, 0, 4, 0);
        form.add(typeGroup, c);

        // ---- footer ----
        RoundButton save = new RoundButton(editing ? "Save Changes" : "Add Task", RoundButton.PRIMARY);
        RoundButton cancel = new RoundButton("Cancel", RoundButton.SECONDARY);
        cancel.addActionListener(e -> close.run());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(cancel);
        buttons.add(save);
        JPanel foot = new JPanel(new BorderLayout(12, 0));
        foot.setBackground(HEADER_BG);
        foot.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER), new EmptyBorder(14, 28, 14, 18)));
        foot.add(error, BorderLayout.CENTER);
        foot.add(buttons, BorderLayout.EAST);

        save.addActionListener(e -> {
            try {
                String cat = (String) category.getSelectedItem();
                boolean household = "Household".equals(cat);
                if (name.getText().trim().isEmpty() || description.getText().trim().isEmpty() || detail1.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Please complete all required fields.");
                LocalDate date = LocalDate.parse(due.getText().trim());
                if (!household && detail2.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Task type is required.");

                if (editing) {
                    existing.setTaskName(name.getText().trim());
                    existing.setDescription(description.getText().trim());
                    existing.setDueDate(date);
                    existing.setPriority((String) priority.getSelectedItem());
                    if (existing instanceof HouseholdTask) {
                        HouseholdTask h = (HouseholdTask) existing;
                        h.setArea(detail1.getText().trim());
                    } else {
                        StudentTask s = (StudentTask) existing;
                        s.setSubject(detail1.getText().trim());
                        s.setType(detail2.getText().trim());
                    }
                } else {
                    int id = manager.getNextId();
                    Task task;
                    if (household)
                        task = new HouseholdTask(id, name.getText().trim(), description.getText().trim(), date,
                            detail1.getText().trim(), (String) priority.getSelectedItem());
                    else
                        task = new StudentTask(id, name.getText().trim(), description.getText().trim(), date,
                            detail1.getText().trim(), detail2.getText().trim(), (String) priority.getSelectedItem());
                    if (!manager.addTask(task)) throw new IllegalStateException("Task limit reached (100).");
                }
                close.run();
                refreshTable();
            } catch (DateTimeParseException ex) {
                error.setText("Invalid date. Use YYYY-MM-DD (example: 2026-11-23).");
            } catch (IllegalArgumentException | IllegalStateException ex) {
                error.setText(ex.getMessage());
            }
        });

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(CARD);
        content.setPreferredSize(new Dimension(540, 570));
        content.add(head, BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        content.add(foot, BorderLayout.SOUTH);
        content.putClientProperty("defaultButton", save);
        return content;
    }

    // =====================================================================
    //  Small helpers
    // =====================================================================
    private JPanel group(JLabel label, JComponent comp) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setOpaque(false);
        p.add(label, BorderLayout.NORTH);
        p.add(comp, BorderLayout.CENTER);
        return p;
    }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font(FAMILY, Font.BOLD, 12));
        l.setForeground(BODY_TEXT);
        return l;
    }

    private JTextField field() {
        final JTextField f = new JTextField();
        f.setFont(F_BODY);
        f.setForeground(TEXT);
        f.setPreferredSize(new Dimension(200, 38));
        f.setBorder(fieldBorder(false));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorder(true)); }
            @Override public void focusLost(FocusEvent e) { f.setBorder(fieldBorder(false)); }
        });
        return f;
    }

    private static Border fieldBorder(boolean focused) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(focused ? PRIMARY : FIELD_BORDER, focused ? 2 : 1),
            new EmptyBorder(0, focused ? 9 : 10, 0, 10));
    }

    private void styleCombo(JComboBox<String> c) {
        c.setFont(F_BODY);
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(200, 38));
    }

    private static Color shade(Color c, double amount) {
        return new Color((int) (c.getRed() * (1 - amount)), (int) (c.getGreen() * (1 - amount)), (int) (c.getBlue() * (1 - amount)));
    }

    private static void smooth(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    // =====================================================================
    //  Table renderers
    // =====================================================================
    private String statusAt(int viewRow) {
        int m = table.convertRowIndexToModel(viewRow);
        return String.valueOf(tableModel.getValueAt(m, COL_STATUS));
    }

    private String priorityAt(int viewRow) {
        int m = table.convertRowIndexToModel(viewRow);
        return String.valueOf(tableModel.getValueAt(m, COL_PRI));
    }

    private class TextCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            super.getTableCellRendererComponent(t, v, sel, false, row, col);
            Border inner = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, ROW_LINE), new EmptyBorder(0, col == 0 ? 13 : 14, 0, 12));
            String pri = priorityAt(row);
            // marker on the left of the row: blue when selected, priority colour in Priority View
            Color stripe = priorityView ? priorityColor(pri) : sel ? PRIMARY : CARD;
            setBorder(col == 0
                ? BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 4, 0, 0, stripe), inner)
                : inner);
            setBackground(sel ? SELECTED : priorityView ? priorityTint(pri) : CARD);
            boolean done = "Completed".equals(statusAt(row));
            Color fg = done ? MUTED : TEXT;
            Font f = F_BODY;
            if (col == COL_TASK) f = F_BOLD;
            if (col == COL_ID) fg = MUTED;
            if (col == COL_DETAILS) fg = done ? MUTED : BODY_TEXT;
            if (col == COL_DUE && !done && v instanceof LocalDate && ((LocalDate) v).isBefore(LocalDate.now())) {
                fg = RED;
                f = F_BOLD;
            }
            setForeground(fg);
            setFont(f);
            setToolTipText(col == 5 && v != null ? v.toString() : null);
            return this;
        }
    }

    /** Category = subtle tag, Status = coloured dot with text. */
    private class BadgeRenderer extends JLabel implements TableCellRenderer {
        private Color chipBg = Color.LIGHT_GRAY, chipFg = TEXT, rowBg = CARD, dot = MUTED;
        private boolean statusMode, priorityMode;

        BadgeRenderer() { setOpaque(false); }

        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            String s = v == null ? "" : v.toString();
            setText(s);
            rowBg = sel ? SELECTED : priorityView ? priorityTint(priorityAt(row)) : CARD;
            int modelCol = t.convertColumnIndexToModel(col);
            statusMode = modelCol == COL_STATUS;
            priorityMode = modelCol == COL_PRI;
            if (priorityMode) {
                dot = priorityColor(s);
                chipBg = priorityChipBg(s);
                chipFg = priorityChipFg(s);
            } else if (statusMode) {
                boolean done = "Completed".equals(s);
                dot = done ? GREEN : AMBER;
                chipFg = done ? new Color(21, 128, 61) : new Color(180, 83, 9);
            } else if ("Household".equals(s)) {
                chipBg = new Color(209, 250, 229); chipFg = new Color(4, 120, 87);
            } else {
                chipBg = new Color(209, 226, 253); chipFg = new Color(30, 64, 175);
            }
            return this;
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(rowBg);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(ROW_LINE);
            g2.fillRect(0, getHeight() - 1, getWidth(), 1);
            if (priorityMode) {
                g2.setFont(F_BADGE);
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(getText()) + 36, h = 24;
                int x = 14, y = (getHeight() - h) / 2;
                g2.setColor(chipBg);
                g2.fillRoundRect(x, y, w, h, h, h);
                g2.setColor(dot);
                g2.fillOval(x + 10, y + (h - 9) / 2, 9, 9);
                g2.setColor(chipFg);
                g2.drawString(getText(), x + 25, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            } else if (statusMode) {
                g2.setColor(dot);
                g2.fillOval(14, (getHeight() - 8) / 2, 8, 8);
                g2.setFont(F_BOLD);
                g2.setColor(chipFg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(), 30, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            } else {
                g2.setFont(F_BADGE);
                FontMetrics fm = g2.getFontMetrics();
                int w = fm.stringWidth(getText()) + 20, h = 22;
                int x = 14, y = (getHeight() - h) / 2;
                g2.setColor(chipBg);
                g2.fillRoundRect(x, y, w, h, 8, 8);
                g2.setColor(chipFg);
                g2.drawString(getText(), x + 10, y + (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    private class HeaderRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int row, int col) {
            String text = v == null ? "" : v.toString().toUpperCase();
            List<? extends RowSorter.SortKey> keys = t.getRowSorter() == null ? null : t.getRowSorter().getSortKeys();
            if (keys != null && !keys.isEmpty() && keys.get(0).getColumn() == t.convertColumnIndexToModel(col)) {
                SortOrder o = keys.get(0).getSortOrder();
                if (o == SortOrder.ASCENDING) text += "  \u25B2";
                else if (o == SortOrder.DESCENDING) text += "  \u25BC";
            }
            super.getTableCellRendererComponent(t, text, false, false, row, col);
            setOpaque(true);
            setBackground(HEADER_BG);
            setForeground(MUTED);
            setFont(F_CAP);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), new EmptyBorder(0, col == 0 ? 16 : 14, 0, 12)));
            return this;
        }
    }

    // =====================================================================
    //  Custom components
    // =====================================================================
    /** White card with a thin border and rounded corners. */
    private static class RoundedPanel extends JPanel {
        private final int radius;

        RoundedPanel(LayoutManager lm, int radius) {
            super(lm);
            this.radius = radius;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            RoundRectangle2D.Float rr = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(CARD);
            g2.fill(rr);
            g2.setColor(BORDER);
            g2.draw(rr);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small coloured circle used in the summary cards. */
    private static class DotIcon implements Icon {
        private final Color color;
        DotIcon(Color color) { this.color = color; }
        public int getIconWidth() { return 9; }
        public int getIconHeight() { return 9; }
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (color == null) return;
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(color);
            g2.fillOval(x, y, 9, 9);
            g2.dispose();
        }
    }

    /** Drop-down items with a red / yellow / green dot for High / Medium / Low. */
    private static class PriorityListRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean foc) {
            super.getListCellRendererComponent(list, value, index, sel, foc);
            String s = String.valueOf(value);
            boolean isPriority = Task.HIGH.equals(s) || Task.MEDIUM.equals(s) || Task.LOW.equals(s);
            setIcon(new DotIcon(isPriority ? priorityColor(s) : null));
            setIconTextGap(8);
            setBorder(new EmptyBorder(3, 8, 3, 8));
            return this;
        }
    }

    /** Logo tile shown at the top of the sidebar. */
    private static class BrandMark extends JComponent {
        BrandMark() { setPreferredSize(new Dimension(40, 40)); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            g2.setColor(PRIMARY);
            g2.fillRoundRect(0, 0, 40, 40, 12, 12);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawPolyline(new int[]{11, 18, 29}, new int[]{21, 28, 13}, 3);
            g2.dispose();
        }
    }

    /** Sidebar navigation entry with a task counter. */
    private static class NavItem extends JComponent {
        private final String label;
        private int count;
        private boolean selected, hover;

        NavItem(String label, final Runnable onClick) {
            this.label = label;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; repaint(); }
                @Override public void mouseClicked(MouseEvent e) { onClick.run(); }
            });
        }

        void setCount(int c) { this.count = c; repaint(); }
        void setSelected(boolean s) { this.selected = s; repaint(); }

        @Override public Dimension getPreferredSize() { return new Dimension(244, 42); }
        @Override public Dimension getMaximumSize() { return new Dimension(Integer.MAX_VALUE, 42); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth(), h = getHeight();
            if (selected || hover) {
                g2.setColor(selected ? SIDEBAR_SEL : new Color(22, 32, 54));
                g2.fillRoundRect(14, 1, w - 28, h - 2, 10, 10);
            }
            if (selected) {
                g2.setColor(PRIMARY);
                g2.fillRoundRect(14, 9, 4, h - 18, 4, 4);
            }
            g2.setFont(selected ? F_BOLD : F_BODY);
            g2.setColor(selected ? Color.WHITE : SIDEBAR_TEXT);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, 32, (h - fm.getHeight()) / 2 + fm.getAscent());

            g2.setFont(F_BADGE);
            fm = g2.getFontMetrics();
            String n = String.valueOf(count);
            int pw = Math.max(24, fm.stringWidth(n) + 14), ph = 20;
            int px = w - 14 - 12 - pw, py = (h - ph) / 2;
            g2.setColor(selected ? PRIMARY : new Color(30, 41, 59));
            g2.fillRoundRect(px, py, pw, ph, ph, ph);
            g2.setColor(selected ? Color.WHITE : SIDEBAR_TEXT);
            g2.drawString(n, px + (pw - fm.stringWidth(n)) / 2, py + (ph - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Button in three flavours: primary (filled), secondary (outline) and danger (red outline). */
    private static class RoundButton extends JButton {
        static final int PRIMARY = 0, SECONDARY = 1, DANGER = 2;
        private final int kind;
        private boolean hover, pressed;

        RoundButton(String text, int kind) {
            super(text);
            this.kind = kind;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setFont(F_BOLD);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 18, 10, 18));
            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(MouseEvent e) { hover = false; pressed = false; repaint(); }
                @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                @Override public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int w = getWidth() - 1, h = getHeight() - 1;
            Color fg;
            if (kind == PRIMARY) {
                g2.setColor(pressed ? shade(RoundedColors.P, 0.2) : hover ? shade(RoundedColors.P, 0.1) : RoundedColors.P);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                fg = Color.WHITE;
            } else {
                boolean danger = kind == DANGER;
                g2.setColor(hover ? (danger ? new Color(254, 242, 242) : HEADER_BG) : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
                g2.setColor(danger ? new Color(252, 165, 165) : FIELD_BORDER);
                g2.drawRoundRect(0, 0, w, h, 8, 8);
                fg = danger ? RED : BODY_TEXT;
            }
            g2.setFont(getFont());
            g2.setColor(fg);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g2.dispose();
        }
    }

    /** Holder so the nested button class can reach the primary colour. */
    private static final class RoundedColors { static final Color P = PRIMARY; }

    /** Thin rounded progress bar. */
    private static class RateBar extends JComponent {
        private final Color track, fill;
        private double value;

        RateBar(Color track, Color fill) {
            this.track = track;
            this.fill = fill;
            setPreferredSize(new Dimension(100, 8));
        }

        void setValue(double v) { this.value = Math.max(0, Math.min(1, v)); repaint(); }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            smooth(g2);
            int h = 8, y = (getHeight() - h) / 2;
            g2.setColor(track);
            g2.fillRoundRect(0, y, getWidth(), h, h, h);
            int w = (int) Math.round(getWidth() * value);
            if (w > 0) {
                g2.setColor(fill);
                g2.fillRoundRect(0, y, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }

    /** Text field that shows grey placeholder text while empty. */
    private static class HintTextField extends JTextField {
        private final String hint;

        HintTextField(String hint) {
            this.hint = hint;
            setFont(F_BODY);
            setForeground(TEXT);
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FIELD_BORDER), new EmptyBorder(0, 12, 0, 12)));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty()) {
                Graphics2D g2 = (Graphics2D) g.create();
                smooth(g2);
                g2.setColor(new Color(148, 163, 184));
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                Insets in = getInsets();
                g2.drawString(hint, in.left, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
                g2.dispose();
            }
        }
    }
}
