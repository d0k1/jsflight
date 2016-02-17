package com.focusit.jsflight.player.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.folding.FoldParserManager;
import org.fife.ui.rsyntaxtextarea.folding.JsonFoldParser;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.focusit.jmeter.JMeterRecorder;
import com.focusit.jsflight.player.controller.IUIController;
import com.focusit.jsflight.player.controller.InputController;
import com.focusit.jsflight.player.controller.JMeterController;
import com.focusit.jsflight.player.controller.OptionsController;
import com.focusit.jsflight.player.controller.PostProcessController;
import com.focusit.jsflight.player.controller.ScenarioController;
import com.focusit.jsflight.player.controller.WebLookupController;
import com.focusit.jsflight.player.input.FileInput;
import com.focusit.jsflight.player.scenario.UserScenario;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class MainFrame
{

    private static final String SET_ELEMENT_VISIBLE_JS = "var e = document.evaluate('%s' ,document, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null ).singleNodeValue; if(e!== null) {e.style.visibility='visible';};";
    private static final Logger log = LoggerFactory.getLogger(MainFrame.class);
    private UserScenario scenario = new UserScenario();
    private AbstractTableModel model;
    /*
        private List<JSONObject> events = new ArrayList<>();
        private int position = 0;
        private List<Boolean> checks;
        private Events rawevents;
    */
    private JFrame frmJsflightrecorderPlayer;
    private JTextField filenameField;
    private JTable table;
    private JTextArea contentPane;
    private RSyntaxTextArea eventContent;
    private JLabel statisticsLabel;
    private JTextField proxyHost;
    private JTextField proxyPort;
    private JTextField ffPath;
    private JTextField textField_1;
    private JTextField maxStepDelayField;
    private RSyntaxTextArea scriptArea;
    private JRadioButton usePhantomButton;
    private JRadioButton useFirefoxButton;
    private final ButtonGroup buttonGroup = new ButtonGroup();
    private JCheckBox makeShots;
    private JPanel jmeterPanel;
    private JTextField screenDirTextField;
    private JTextField scenarioTextField;
    private JTextField pjsPath;
    private JMeterRecorder jmeter = new JMeterRecorder();
    private JTextField textField;
    private JTextField checkPageJs;

    private HashMap<String, WebDriver> drivers = new HashMap<>();
    private HashMap<String, String> lastUrls = new HashMap<>();
    private HashMap<String, JSONObject> lastEvents = new HashMap<>();
    private HashMap<String, String> tabsWindow = new HashMap<>();

    private JTextField webDriverTag;

    /**
     * Create the application.
     * 
     * @throws IOException
     */
    public MainFrame() throws Exception
    {
        initialize();
        jmeter.init();
    }

    // I will use it, probably, another day
    // private Pattern urlPattern = Pattern.compile(
    // "^((http[s]?|ftp):\\/)?\\/?([^:\\/\\s]+)((\\/\\w+)*\\/)([\\w\\-\\.]+[^#?\\s]+)(.*)?(#[\\w\\-]+)?$");

    public AbstractTableModel createEventTableModel()
    {
        return new AbstractTableModel()
        {

            private static final long serialVersionUID = 1L;

            private String[] columns = { "*", "#", "eventId", "url", "type", "key", "target", "timestamp", "tag",
                    "Pre", "Post", "comment" };

            @Override
            public int getColumnCount()
            {
                return 11;
            }

            @Override
            public String getColumnName(int column)
            {
                return columns[column];
            }

            @Override
            public int getRowCount()
            {
                return scenario.getStepsCount();
            }

            @Override
            public Object getValueAt(int rowIndex, int columnIndex)
            {
                if (rowIndex == scenario.getPosition() && columnIndex == 0)
                {
                    return "*";
                }

                JSONObject event = scenario.getStepAt(rowIndex);

                switch (columnIndex)
                {
                case 1:
                    return rowIndex;
                case 2:
                    return event.get("eventId");
                case 3:
                    return event.get("hash");
                case 4:
                    String type = event.getString("type");
                    if (type.equals("mousedown"))
                    {
                        return "md";
                    }
                    if (type.equals("keypress"))
                    {
                        return "kp";
                    }
                    if (type.equals("keyup"))
                    {
                        return "ku";
                    }
                    if (type.equals("hashchange"))
                    {
                        return "hc";
                    }

                    return type;
                case 5:
                {
                    if (!event.has("charCode"))
                    {
                        return null;
                    }
                    int code = event.getInt("charCode");
                    char[] key = new char[1];
                    key[0] = (char)code;
                    return String.format("%d(%s)/%s", code, new String(key), event.has("button") ? event.get("button")
                            : "null");
                }
                case 6:
                    return getTargetForEvent(event);
                case 7:
                    return new Date(event.getBigDecimal("timestamp").longValue());
                case 8:
                    return getTagForEvent(event);
                case 9:
                    return event.has("pre") ? event.getString("pre") : "no";
                case 10:
                    return event.has("post") ? event.getString("post") : "no";
                }
                return null;
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                if (columnIndex == 6 || columnIndex == 9)
                {
                    return true;
                }
                return false;
            }
        };
    }

    /*
        public WebDriver getDriverForEvent(JSONObject event)
        {
            String tag = getTagForEvent(event);

            WebDriver driver = drivers.get(tag);

            try
            {
                if (driver != null)
                {
                    return driver;
                }

                FirefoxProfile profile = new FirefoxProfile();
                DesiredCapabilities cap = new DesiredCapabilities();
                if (proxyHost.getText().trim().length() > 0)
                {
                    String host = proxyHost.getText();
                    if (proxyPort.getText().trim().length() > 0)
                    {
                        host += ":" + proxyPort.getText();
                    }
                    org.openqa.selenium.Proxy proxy = new org.openqa.selenium.Proxy();
                    proxy.setHttpProxy(host).setFtpProxy(host).setSslProxy(host);
                    cap.setCapability(CapabilityType.PROXY, proxy);
                }
                if (useFirefoxButton.isSelected())
                {
                    if (ffPath.getText() != null && ffPath.getText().trim().length() > 0)
                    {
                        FirefoxBinary binary = new FirefoxBinary(new File(ffPath.getText()));
                        driver = new FirefoxDriver(binary, profile, cap);
                    }
                    else
                    {
                        driver = new FirefoxDriver(new FirefoxBinary(), profile, cap);
                    }
                }
                else if (usePhantomButton.isSelected())
                {
                    if (pjsPath.getText() != null && pjsPath.getText().trim().length() > 0)
                    {
                        cap.setCapability("phantomjs.binary.path", pjsPath.getText());
                        driver = new PhantomJSDriver(cap);
                    }
                    else
                    {
                        driver = new PhantomJSDriver(cap);
                    }
                }

                try
                {
                    Thread.sleep(7000);
                }
                catch (InterruptedException e)
                {
                    log.error(e.toString(), e);
                }

                drivers.put(tag, driver);
                return driver;
            }
            finally
            {
                String tabUuid = event.getString("tabuuid");
                String window = tabsWindow.get(tabUuid);
                if (window == null)
                {
                    ArrayList<String> tabs = new ArrayList<String>(driver.getWindowHandles());
                    if (tabsWindow.values().contains(tabs.get(0)))
                    {
                        String newTabKey = Keys.chord(Keys.CONTROL, "n");
                        driver.findElement(By.tagName("body")).sendKeys(newTabKey);
                        tabs = new ArrayList<String>(driver.getWindowHandles());
                    }
                    window = tabs.get(tabs.size() - 1);
                    tabsWindow.put(tabUuid, window);
                }
                driver.switchTo().window(window);
            }
        }
    */
    public JFrame getFrame()
    {
        return frmJsflightrecorderPlayer;
    }

    public String getLastUrl(JSONObject event)
    {
        String no_result = "";
        String result = lastUrls.get(getTagForEvent(event));
        if (result == null)
        {
            lastUrls.put(getTagForEvent(event), no_result);
            result = no_result;
        }

        return result;
    }

    public JSONObject getPrevEvent(JSONObject event)
    {
        return lastEvents.get(getTagForEvent(event));
    }

    public String getTagForEvent(JSONObject event)
    {
        String tag = "null";
        if (event.has(webDriverTag.getText()))
        {
            tag = event.getString(webDriverTag.getText());
        }

        return tag;
    }

    public String getTargetForEvent(JSONObject event)
    {
        JSONArray array = event.getJSONArray("target1");
        String target = array.getJSONObject(0).getString("getxp");
        return target;
    }

    public boolean isStepDuplicates(JSONObject event)
    {
        JSONObject prev = getPrevEvent(event);
        if (prev == null)
        {
            return false;
        }

        if (prev.getString("type").equals(event.getString("type"))
                && prev.getString("url").equals(event.getString("url"))
                && getTargetForEvent(prev).equals(getTargetForEvent(event)))
        {
            return true;
        }

        return false;
    }

    public void resetLastUrls()
    {
        lastUrls.clear();
    }

    public void setColumnWidths()
    {
        table.getColumnModel().getColumn(0).setMaxWidth(10);
        table.getColumnModel().getColumn(1).setMaxWidth(30);
        table.getColumnModel().getColumn(2).setMaxWidth(30);
        table.getColumnModel().getColumn(4).setMaxWidth(45);
        table.getColumnModel().getColumn(5).setMaxWidth(60);
        table.getColumnModel().getColumn(6).setPreferredWidth(400);
        table.getColumnModel().getColumn(7).setPreferredWidth(180);
        table.getColumnModel().getColumn(7).setMaxWidth(180);
        table.getColumnModel().getColumn(8).setPreferredWidth(140);
        table.getColumnModel().getColumn(8).setMaxWidth(140);
    }

    public void setLastEvent(JSONObject event)
    {
        lastEvents.put(getTagForEvent(event), event);
    }

    public void updateLastUrl(JSONObject event, String url)
    {
        lastUrls.put(getTagForEvent(event), url);
    }

    public void updatePrevEvent(JSONObject event)
    {
        lastEvents.put(getTagForEvent(event), event);
    }

    protected void copyCurrentStep()
    {
        scenario.copyStep(table.getSelectedRow());
        /*
        String event = rawevents.getEvents().get(table.getSelectedRow()).toString();
        JSONObject clone = new JSONObject(event);
        rawevents.getEvents().add(table.getSelectedRow(), clone);
        */
        model.fireTableRowsInserted(table.getSelectedRow(), table.getSelectedRow());
    }

    protected void createTableEditor()
    {
        final DefaultCellEditor editor = new DefaultCellEditor(new JTextField());
        editor.addCellEditorListener(new CellEditorListener()
        {
            @Override
            public void editingCanceled(ChangeEvent e)
            {
                ((JTextField)editor.getComponent()).getText();
            }

            @Override
            public void editingStopped(ChangeEvent e)
            {
            }
        });
        table.getColumnModel().getColumn(6).setCellEditor(editor);

        final DefaultCellEditor editor1 = new DefaultCellEditor(new JTextField());
        editor1.addCellEditorListener(new CellEditorListener()
        {
            @Override
            public void editingCanceled(ChangeEvent e)
            {
                ((JTextField)editor1.getComponent()).getText();
            }

            @Override
            public void editingStopped(ChangeEvent e)
            {
            }
        });
        table.getColumnModel().getColumn(9).setCellEditor(editor1);
    }

    protected void playTheScenario()
    {
        /*
        long begin = System.currentTimeMillis();

        log.info("playing the scenario");

        while (position < events.size())
        {
            if (position > 0)
            {
                JSONObject event = events.get(position - 1);
                long prev = event.getBigDecimal("timestamp").longValue();
                event = events.get(position);
                long now = event.getBigDecimal("timestamp").longValue();

                if ((now - prev) > 0)
                {
                    try
                    {
                        log.info("Emulate wait " + (now - prev) + "ms + 2000ms");
                        long sleepTime = now - prev;
                        int maxSleepTime = Integer.parseInt(maxStepDelayField.getText());
                        if (sleepTime > maxSleepTime * 1000)
                        {
                            sleepTime = maxSleepTime;
                        }
                        // if (sleepTime < 1000)
                        // {
                        // sleepTime = 1000;
                        // }
                        Thread.sleep(sleepTime);
                    }
                    catch (InterruptedException e)
                    {
                        log.error("interrupted", e);
                    }
                }
                log.info("Step " + position);
            }
            else
            {
                log.info("Step 0");
            }
            applyStep(position);
            checks.set(position, true);
            position++;
            if (position == events.size())
            {
                for (int i = 0; i < position; i++)
                {
                    checks.set(i, false);
                }
            }
        }
        log.info(String.format("Done(%d):playing", System.currentTimeMillis() - begin));
        position--;
        */
        scenario.play();
        model.fireTableDataChanged();
    }

    protected void resetCurrentEvent()
    {
        int index = table.getSelectedRow();
        if (index >= 0)
        {
            eventContent.setText(scenario.getStepAt(index).toString(3));
        }
    }

    protected void saveControlersOptions()
    {
        try
        {
            updateInputController();

            InputController.getInstance().store(IUIController.defaultConfig);
            JMeterController.getInstance().store(IUIController.defaultConfig);
            OptionsController.getInstance().store(IUIController.defaultConfig);
            PostProcessController.getInstance().store(IUIController.defaultConfig);
            ScenarioController.getInstance().store(IUIController.defaultConfig);
            WebLookupController.getInstance().store(IUIController.defaultConfig);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
    }

    protected void updateCurrentEvent()
    {
        /*
        List<JSONObject> events = rawevents.getEvents();
        events.set(table.getSelectedRow(), new JSONObject(eventContent.getText()));
        */
        scenario.updateStep(table.getSelectedRow(), new JSONObject(eventContent.getText()));
        model.fireTableRowsUpdated(table.getSelectedRow(), table.getSelectedRow());
    }

    private void applyStep(int position)
    {
        scenario.applyStep(position);
        /*
        JSONObject event = events.get(position);
        boolean error = false;
        try
        {
            if (isStepDuplicates(event))
            {
                return;
            }
            String eventType = event.getString("type");

            if (isEventIgnored(eventType) || isEventBad(event))
            {
                return;
            }

            openEventUrl(event);

            WebElement element = null;

            String target = getTargetForEvent(event);

            element = findTargetWebElement(event, target);

            processMouseEvent(event, element);

            processKeyboardEvent(event, element);

            makeAShot(event);
        }
        catch (Exception e)
        {
            error = true;
            throw e;
        }
        finally
        {
            if (!error)
            {
                updatePrevEvent(event);
            }
        }
        */
    }

    private void checkElement(int position)
    {
        /*
        JSONObject event = events.get(position);
        if (!event.has("target") || event.getString("type").equalsIgnoreCase("xhr"))
        {
            return;
        }
        getDriverForEvent(event).findElement(By.xpath(event.getString("target")));
        checks.set(position, true);
        */
        scenario.checkStep(position);
        model.fireTableDataChanged();
    }

    /*
        private WebElement findTargetWebElement(JSONObject event, String target)
        {
            makeElementVisibleByJS(event, target);
            return getDriverForEvent(event).findElement(By.xpath(target));
        }
    */
    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frmJsflightrecorderPlayer = new JFrame();
        frmJsflightrecorderPlayer.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                saveControlersOptions();
            }

            @Override
            public void windowDeactivated(WindowEvent e)
            {
                saveControlersOptions();
            }
        });
        frmJsflightrecorderPlayer.setTitle("JSFlightRecorder Player");
        frmJsflightrecorderPlayer.setBounds(100, 100, 1110, 850);
        frmJsflightrecorderPlayer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        frmJsflightrecorderPlayer.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel();
        tabbedPane.addTab("Input", null, inputPanel, null);
        GridBagLayout gbl_inputPanel = new GridBagLayout();
        gbl_inputPanel.columnWidths = new int[] { 0, 0, 0 };
        gbl_inputPanel.rowHeights = new int[] { 0, 0, 0, 0 };
        gbl_inputPanel.columnWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
        gbl_inputPanel.rowWeights = new double[] { 0.0, 0.0, 1.0, Double.MIN_VALUE };
        inputPanel.setLayout(gbl_inputPanel);

        JLabel lblFilename = new JLabel("Filename");
        GridBagConstraints gbc_lblFilename = new GridBagConstraints();
        gbc_lblFilename.insets = new Insets(0, 0, 5, 5);
        gbc_lblFilename.anchor = GridBagConstraints.NORTHEAST;
        gbc_lblFilename.gridx = 0;
        gbc_lblFilename.gridy = 0;
        inputPanel.add(lblFilename, gbc_lblFilename);

        filenameField = new JTextField();
        GridBagConstraints gbc_filenameField = new GridBagConstraints();
        gbc_filenameField.insets = new Insets(0, 0, 5, 0);
        gbc_filenameField.fill = GridBagConstraints.HORIZONTAL;
        gbc_filenameField.gridx = 1;
        gbc_filenameField.gridy = 0;
        inputPanel.add(filenameField, gbc_filenameField);
        filenameField.setColumns(10);
        filenameField.setText(InputController.getInstance().getFilename());

        JButton btnLoad = new JButton("Load");
        btnLoad.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                try
                {
                    loadScenario();
                }
                catch (IOException e1)
                {
                    throw new RuntimeException(e1);
                }
            }
        });
        GridBagConstraints gbc_btnLoad = new GridBagConstraints();
        gbc_btnLoad.insets = new Insets(0, 0, 5, 5);
        gbc_btnLoad.gridx = 0;
        gbc_btnLoad.gridy = 1;
        inputPanel.add(btnLoad, gbc_btnLoad);

        JButton btnBrowse = new JButton("Browse");
        btnBrowse.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION)
                {
                    String selectedFile = fileChooser.getSelectedFile().getAbsolutePath();
                    filenameField.setText(selectedFile);
                }
            }
        });
        GridBagConstraints gbc_btnBrowse = new GridBagConstraints();
        gbc_btnBrowse.insets = new Insets(0, 0, 5, 0);
        gbc_btnBrowse.gridx = 1;
        gbc_btnBrowse.gridy = 1;
        inputPanel.add(btnBrowse, gbc_btnBrowse);

        JLabel lblContent = new JLabel("Content");
        GridBagConstraints gbc_lblContent = new GridBagConstraints();
        gbc_lblContent.anchor = GridBagConstraints.NORTH;
        gbc_lblContent.insets = new Insets(0, 0, 0, 5);
        gbc_lblContent.gridx = 0;
        gbc_lblContent.gridy = 2;
        inputPanel.add(lblContent, gbc_lblContent);

        JScrollPane scrollPane_1 = new JScrollPane();
        GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
        gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
        gbc_scrollPane_1.gridx = 1;
        gbc_scrollPane_1.gridy = 2;
        inputPanel.add(scrollPane_1, gbc_scrollPane_1);

        contentPane = new JTextArea();
        scrollPane_1.setViewportView(contentPane);

        JPanel scenarioPanel = new JPanel();
        tabbedPane.addTab("Scenario", null, scenarioPanel, null);
        scenarioPanel.setLayout(new BorderLayout(0, 0));

        JPanel panel_4 = new JPanel();
        panel_4.setBorder(null);
        panel_4.setLayout(new BorderLayout(0, 0));

        JPanel panel_5 = new JPanel();
        panel_4.add(panel_5, BorderLayout.NORTH);
        GridBagLayout gbl_panel_5 = new GridBagLayout();
        gbl_panel_5.columnWidths = new int[] { 149, 122, 136, 86, 65, 66, 64, 93, 144, 0 };
        gbl_panel_5.rowHeights = new int[] { 25, 0 };
        gbl_panel_5.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
        gbl_panel_5.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        panel_5.setLayout(gbl_panel_5);

        JPanel panel_2 = new JPanel();
        GridBagConstraints gbc_panel_2 = new GridBagConstraints();
        gbc_panel_2.fill = GridBagConstraints.BOTH;
        gbc_panel_2.insets = new Insets(0, 0, 0, 5);
        gbc_panel_2.gridx = 0;
        gbc_panel_2.gridy = 0;
        panel_5.add(panel_2, gbc_panel_2);
        panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));

        JButton btnParse = new JButton("Parse");
        panel_2.add(btnParse);

        JButton btnSave_1 = new JButton("Save");
        btnSave_1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    scenario.saveScenario("test.json");
                    /*
                                        FileInput.saveEvents(events, "test.json");
                    */
                }
                catch (IOException e1)
                {
                    log.error(e1.toString(), e1);
                }
            }
        });
        panel_2.add(btnSave_1);

        JButton btnOpenBrowser = new JButton("Open browser");
        GridBagConstraints gbc_btnOpenBrowser = new GridBagConstraints();
        gbc_btnOpenBrowser.insets = new Insets(0, 0, 0, 5);
        gbc_btnOpenBrowser.gridx = 1;
        gbc_btnOpenBrowser.gridy = 0;
        panel_5.add(btnOpenBrowser, gbc_btnOpenBrowser);

        JButton btnCloseBrowser = new JButton("Close browsers");
        GridBagConstraints gbc_btnCloseBrowser = new GridBagConstraints();
        gbc_btnCloseBrowser.insets = new Insets(0, 0, 0, 5);
        gbc_btnCloseBrowser.gridx = 2;
        gbc_btnCloseBrowser.gridy = 0;
        panel_5.add(btnCloseBrowser, gbc_btnCloseBrowser);

        JButton btnRewind = new JButton("Rewind");
        GridBagConstraints gbc_btnRewind = new GridBagConstraints();
        gbc_btnRewind.insets = new Insets(0, 0, 0, 5);
        gbc_btnRewind.gridx = 3;
        gbc_btnRewind.gridy = 0;
        panel_5.add(btnRewind, gbc_btnRewind);

        JButton btnPrev = new JButton("Prev");
        GridBagConstraints gbc_btnPrev = new GridBagConstraints();
        gbc_btnPrev.insets = new Insets(0, 0, 0, 5);
        gbc_btnPrev.gridx = 4;
        gbc_btnPrev.gridy = 0;
        panel_5.add(btnPrev, gbc_btnPrev);

        JButton btnNext = new JButton("Next");
        GridBagConstraints gbc_btnNext = new GridBagConstraints();
        gbc_btnNext.insets = new Insets(0, 0, 0, 5);
        gbc_btnNext.gridx = 5;
        gbc_btnNext.gridy = 0;
        panel_5.add(btnNext, gbc_btnNext);

        JButton btnSkip = new JButton("Skip");
        GridBagConstraints gbc_btnSkip = new GridBagConstraints();
        gbc_btnSkip.insets = new Insets(0, 0, 0, 5);
        gbc_btnSkip.gridx = 6;
        gbc_btnSkip.gridy = 0;
        panel_5.add(btnSkip, gbc_btnSkip);

        JButton btnDel = new JButton("Del step");
        GridBagConstraints gbc_btnDel = new GridBagConstraints();
        gbc_btnDel.insets = new Insets(0, 0, 0, 5);
        gbc_btnDel.gridx = 7;
        gbc_btnDel.gridy = 0;
        panel_5.add(btnDel, gbc_btnDel);

        JPanel panel = new JPanel();
        GridBagConstraints gbc_panel = new GridBagConstraints();
        gbc_panel.fill = GridBagConstraints.BOTH;
        gbc_panel.gridx = 8;
        gbc_panel.gridy = 0;
        panel_5.add(panel, gbc_panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

        JButton CopyStepButton = new JButton("Copy step");
        CopyStepButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                copyCurrentStep();
            }
        });
        panel.add(CopyStepButton);

        JButton btnCheck = new JButton("Check");
        panel.add(btnCheck);
        btnCheck.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                checkElement(table.getSelectedRow());
            }
        });

        JButton btnPlay = new JButton("Play");
        btnPlay.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                playTheScenario();
            }
        });
        panel.add(btnPlay);
        btnDel.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                scenario.deleteStep(table.getSelectedRow());
                /*
                events.remove(table.getSelectedRow());
                */
                model.fireTableDataChanged();
            }
        });
        btnSkip.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                //position++;
                scenario.skip();
                model.fireTableDataChanged();
            }
        });
        btnNext.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                scenario.applyStep(scenario.getPosition());
                scenario.next();
                /*
                                position++;
                                if (position == events.size())
                                {
                                    for (int i = 0; i < position; i++)
                                    {
                                        checks.set(i, false);
                                    }
                                    position = 0;
                                }
                 */
                model.fireTableDataChanged();
            }
        });
        btnPrev.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                scenario.prev();
                /*
                if (position > 0)
                {
                    position--;
                }
                */
            }
        });
        btnRewind.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                scenario.rewind();
                /*
                for (int i = 0; i < events.size(); i++)
                {
                    checks.set(i, false);
                }
                position = 0;
                */
                resetLastUrls();
                model.fireTableDataChanged();
            }
        });
        btnCloseBrowser.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                for (WebDriver driver : drivers.values())
                {
                    if (driver != null)
                    {
                        driver.close();
                    }
                }
                drivers.clear();
            }
        });
        btnOpenBrowser.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                //                if (table.getSelectedRow() >= 0)
                //                {
                //                    JSONObject event = rawevents.getEvents().get(table.getSelectedRow());
                //                    getDriverForEvent(event);
                //                }
            }
        });
        btnParse.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                scenario.postProcessScenario();
                /*
                events = rawevents.getEvents();
                if (!scriptArea.getText().isEmpty())
                {
                    new Engine(scriptArea.getText()).postProcess(events);
                }
                checks = new ArrayList<>(events.size());
                for (int i = 0; i < events.size(); i++)
                {
                    checks.add(new Boolean(false));
                }

                long secs = 0;

                if (events.size() > 0)
                {
                    secs = events.get(events.size() - 1).getBigDecimal("timestamp").longValue()
                            - events.get(0).getBigDecimal("timestamp").longValue();
                }
                
                statisticsLabel.setText(String.format("Events %d, duration %f sec", events.size(), secs / 1000.0));
                */
                model = createEventTableModel();
                table.setModel(model);
                model.fireTableDataChanged();
                setColumnWidths();
                createTableEditor();
            }
        });

        JScrollPane scrollPane = new JScrollPane();
        panel_4.add(scrollPane, BorderLayout.CENTER);

        table = new JTable();
        table.setFont(new Font("Hack", Font.PLAIN, 14));
        table.setRowHeight(30);
        scrollPane.setViewportView(table);

        table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            @Override
            public void valueChanged(ListSelectionEvent e)
            {
                resetCurrentEvent();
            }
        });

        JSplitPane splitPane = new JSplitPane();
        splitPane.setContinuousLayout(true);
        splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        scenarioPanel.add(splitPane, BorderLayout.CENTER);

        eventContent = new RSyntaxTextArea();
        eventContent.setSyntaxEditingStyle(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JSON);
        eventContent.getFoldManager().setCodeFoldingEnabled(true);
        eventContent.setFont(new Font("Hack", Font.PLAIN, 14));
        eventContent.setRows(3);
        eventContent.setMarkOccurrences(true);
        eventContent.setLineWrap(true);
        eventContent.setWrapStyleWord(true);

        RTextScrollPane scrollPane_2 = new RTextScrollPane((Component)eventContent);
        scrollPane_2.setLineNumbersEnabled(true);
        scrollPane_2.setFoldIndicatorEnabled(true);
        splitPane.setLeftComponent(panel_4);
        splitPane.setRightComponent(scrollPane_2);

        FoldParserManager.get().addFoldParserMapping(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_JSON,
                new JsonFoldParser());

        JPanel panel_6 = new JPanel();
        panel_6.setBorder(null);
        scenarioPanel.add(panel_6, BorderLayout.SOUTH);
        GridBagLayout gbl_panel_6 = new GridBagLayout();
        gbl_panel_6.columnWidths = new int[] { 378, 0 };
        gbl_panel_6.rowHeights = new int[] { 0, 0 };
        gbl_panel_6.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
        gbl_panel_6.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
        panel_6.setLayout(gbl_panel_6);

        JPanel panel_1 = new JPanel();
        GridBagConstraints gbc_panel_1 = new GridBagConstraints();
        gbc_panel_1.anchor = GridBagConstraints.SOUTH;
        gbc_panel_1.fill = GridBagConstraints.HORIZONTAL;
        gbc_panel_1.gridx = 0;
        gbc_panel_1.gridy = 0;
        panel_6.add(panel_1, gbc_panel_1);
        panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

        JButton btnUpdate = new JButton("Update");
        btnUpdate.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                updateCurrentEvent();
            }
        });
        panel_1.add(btnUpdate);

        JButton btnReset_1 = new JButton("Reset");
        btnReset_1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                resetCurrentEvent();
            }
        });
        panel_1.add(btnReset_1);

        JLabel lblEvents = new JLabel("Statistics");
        panel_1.add(lblEvents);

        statisticsLabel = new JLabel("DATA");
        panel_1.add(statisticsLabel);

        JPanel postProcessPanel = new JPanel();
        tabbedPane.addTab("Post process", null, postProcessPanel, null);
        postProcessPanel.setLayout(new BorderLayout(0, 0));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        postProcessPanel.add(toolBar, BorderLayout.NORTH);

        JLabel lblScript = new JLabel("Script");
        toolBar.add(lblScript);

        textField_1 = new JTextField();
        toolBar.add(textField_1);
        textField_1.setColumns(15);

        JButton btnBrowse_1 = new JButton("Browse");
        toolBar.add(btnBrowse_1);

        JButton btnLoad_1 = new JButton("Load");
        btnLoad_1.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
            }
        });
        toolBar.add(btnLoad_1);

        JButton btnSave = new JButton("Save");
        toolBar.add(btnSave);

        JButton btnReset = new JButton("Reset");
        toolBar.add(btnReset);

        JButton btnRun = new JButton("Run");
        btnRun.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                /*
                Engine engine = new Engine(scriptArea.getText());
                List<JSONObject> events = new ArrayList<>();
                if (rawevents != null && rawevents.getEvents() != null)
                {
                    events = rawevents.getEvents();
                }
                engine.testPostProcess(events);
                */
                scenario.runPostProcessor(scriptArea.getText());
            }
        });
        toolBar.add(btnRun);

        RTextScrollPane scrollPane_3 = new RTextScrollPane();
        postProcessPanel.add(scrollPane_3, BorderLayout.CENTER);

        scriptArea = new RSyntaxTextArea();
        scriptArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        scriptArea.setCodeFoldingEnabled(true);
        scrollPane_3.setViewportView(scriptArea);

        JPanel webLookupPanel = new JPanel();
        tabbedPane.addTab("Web lookup", null, webLookupPanel, null);
        webLookupPanel.setLayout(new BorderLayout(0, 0));

        JToolBar toolBar_1 = new JToolBar();
        webLookupPanel.add(toolBar_1, BorderLayout.NORTH);

        JLabel label_1 = new JLabel("Script");
        toolBar_1.add(label_1);

        textField = new JTextField();
        textField.setColumns(15);
        toolBar_1.add(textField);

        JButton button = new JButton("Browse");
        toolBar_1.add(button);

        JButton button_1 = new JButton("Load");
        toolBar_1.add(button_1);

        JButton button_2 = new JButton("Save");
        toolBar_1.add(button_2);

        JButton button_3 = new JButton("Reset");
        toolBar_1.add(button_3);

        JButton button_4 = new JButton("Run");
        toolBar_1.add(button_4);

        JScrollPane scrollPane_4 = new JScrollPane();
        webLookupPanel.add(scrollPane_4, BorderLayout.CENTER);

        RSyntaxTextArea lookupScriptArea = new RSyntaxTextArea();
        scrollPane_4.setViewportView(lookupScriptArea);

        JPanel optionsPanel = new JPanel();
        tabbedPane.addTab("Options", null, optionsPanel, null);
        optionsPanel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
                new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

        JLabel lblFirefoxProxyHost = new JLabel("Proxy host");
        optionsPanel.add(lblFirefoxProxyHost, "2, 2, right, default");

        proxyHost = new JTextField();
        optionsPanel.add(proxyHost, "4, 2, fill, default");
        proxyHost.setColumns(10);

        JLabel lblFirefoxProxyPort = new JLabel("Proxy port");
        optionsPanel.add(lblFirefoxProxyPort, "2, 4, right, default");

        proxyPort = new JTextField();
        optionsPanel.add(proxyPort, "4, 4, fill, default");
        proxyPort.setColumns(10);

        JLabel lblFirefoxPath = new JLabel("Firefox path");
        optionsPanel.add(lblFirefoxPath, "2, 6, right, default");

        ffPath = new JTextField();
        ffPath.setText("/usr/bin/firefox");
        optionsPanel.add(ffPath, "4, 6, fill, default");
        ffPath.setColumns(10);

        JLabel lblPhantomJsPath = new JLabel("PhantomJs path");
        optionsPanel.add(lblPhantomJsPath, "2, 8, right, default");

        pjsPath = new JTextField();
        pjsPath.setText("phantomjs/phantomjs");
        pjsPath.setColumns(10);
        optionsPanel.add(pjsPath, "4, 8, fill, default");

        JLabel lblMaxDelayBetween = new JLabel("Max delay between steps, sec");
        optionsPanel.add(lblMaxDelayBetween, "2, 10, right, default");

        maxStepDelayField = new JTextField();
        maxStepDelayField.setText("30");
        optionsPanel.add(maxStepDelayField, "4, 10, fill, default");
        maxStepDelayField.setColumns(10);

        JLabel lblBrowser = new JLabel("Browser");
        optionsPanel.add(lblBrowser, "2, 12, right, default");

        JPanel panel_3 = new JPanel();
        optionsPanel.add(panel_3, "4, 12, fill, fill");
        panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));

        useFirefoxButton = new JRadioButton("Firefox");
        buttonGroup.add(useFirefoxButton);
        useFirefoxButton.setSelected(true);
        panel_3.add(useFirefoxButton);

        usePhantomButton = new JRadioButton("PhantomJs");
        buttonGroup.add(usePhantomButton);
        panel_3.add(usePhantomButton);

        JLabel label = new JLabel("Make screenshots");
        optionsPanel.add(label, "2, 14, right, default");

        makeShots = new JCheckBox("");
        makeShots.setSelected(true);
        optionsPanel.add(makeShots, "4, 14");

        JLabel lblScreenshotDirectory = new JLabel("Screenshot directory");
        optionsPanel.add(lblScreenshotDirectory, "2, 16, right, default");

        screenDirTextField = new JTextField();
        screenDirTextField.setText("shots");
        screenDirTextField.setColumns(10);
        optionsPanel.add(screenDirTextField, "4, 16, fill, default");

        JLabel lblCheckPageReady = new JLabel("Check page ready");
        optionsPanel.add(lblCheckPageReady, "2, 18, right, default");

        checkPageJs = new JTextField();
        checkPageJs.setText("return document.getElementById('state.context').getAttribute('value');");
        checkPageJs.setColumns(10);
        optionsPanel.add(checkPageJs, "4, 18, fill, default");

        JLabel lblWebdriverTag = new JLabel("WebDriver tag");
        optionsPanel.add(lblWebdriverTag, "2, 20, right, default");

        webDriverTag = new JTextField();
        webDriverTag.setText("uuid");
        webDriverTag.setColumns(10);
        optionsPanel.add(webDriverTag, "4, 20, fill, default");

        jmeterPanel = new JPanel();
        tabbedPane.addTab("JMeter", null, jmeterPanel, null);
        jmeterPanel.setLayout(new FormLayout(new ColumnSpec[] { FormSpecs.RELATED_GAP_COLSPEC,
                FormSpecs.DEFAULT_COLSPEC, FormSpecs.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"), },
                new RowSpec[] { FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC,
                        FormSpecs.DEFAULT_ROWSPEC, FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
                        FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC, }));

        JLabel lblJmeterRecorder_1 = new JLabel("JMeter recorder");
        jmeterPanel.add(lblJmeterRecorder_1, "2, 2");

        JPanel label_5 = new JPanel();
        jmeterPanel.add(label_5, "4, 2");
        label_5.setLayout(new BoxLayout(label_5, BoxLayout.X_AXIS));

        JButton startProxyButton = new JButton("Start proxy");
        startProxyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    jmeter.startRecording();
                }
                catch (IOException e1)
                {
                    log.error(e1.toString(), e1);
                }
            }
        });
        startProxyButton.setSelected(true);
        label_5.add(startProxyButton);

        JButton stopProxyButton = new JButton("Stop proxy");
        stopProxyButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                jmeter.stopRecording();
            }
        });
        stopProxyButton.setSelected(true);
        label_5.add(stopProxyButton);

        JButton saveRecordingButton = new JButton("Save recording");
        saveRecordingButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    jmeter.saveScenario(scenarioTextField.getText());
                }
                catch (IOException e1)
                {
                    log.error(e1.toString(), e1);
                }
            }
        });
        saveRecordingButton.setSelected(true);
        label_5.add(saveRecordingButton);

        JLabel lblJmeterRecorder = new JLabel("JMeter scenario");
        jmeterPanel.add(lblJmeterRecorder, "2, 4, right, default");

        scenarioTextField = new JTextField();
        scenarioTextField.setText("test.jmx");
        scenarioTextField.setColumns(10);
        jmeterPanel.add(scenarioTextField, "4, 4, fill, default");
    }

    private boolean isEventBad(JSONObject event)
    {
        return !event.has("target") || event.get("target") == null || event.get("target") == JSONObject.NULL;
    }

    private boolean isEventIgnored(String eventType)
    {
        return eventType.equalsIgnoreCase("xhr")
                || eventType.equalsIgnoreCase("hashchange")
                || (!eventType.equalsIgnoreCase("mousedown") && !eventType.equalsIgnoreCase("keypress") && !eventType
                        .equalsIgnoreCase("keyup"));
    }

    /**
     * @throws IOException 
     * 
     */
    private void loadScenario() throws IOException
    {
        String filename = filenameField.getText().trim();
        if (filename.length() == 0)
        {
            return;
        }
        try
        {
            /*
            rawevents = new Events();
            rawevents.parse(FileInput.getContent(filename));
            */
            scenario.parse(filename);
            contentPane.setText(FileInput.getContentInString(filename));
        }
        catch (IOException e1)
        {
            log.error(e1.toString(), e1);
            throw e1;
        }
    }

    private void updateInputController()
    {
        InputController.getInstance().setFilename(filenameField.getText());
    }
}
