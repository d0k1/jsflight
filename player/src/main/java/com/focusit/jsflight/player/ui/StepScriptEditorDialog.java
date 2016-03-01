package com.focusit.jsflight.player.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import com.focusit.jsflight.player.scenario.UserScenario;

public class StepScriptEditorDialog extends DefaultCellEditor implements TableCellEditor
{
    private static final long serialVersionUID = 1L;

    static final String EDIT = "edit";
    String newInput;
    String oldValue;
    ScriptDialog dialog;
    private JButton editorComponent;
    UserScenario scenario;
    private String stepProperty = "";
    private int position = -1;

    public StepScriptEditorDialog(UserScenario scenario, boolean pre)
    {
        super(new JTextField());
        setClickCountToStart(1);
        this.scenario = scenario;

        if (pre)
        {
            stepProperty = "pre";
        }
        else
        {
            stepProperty = "post";
        }

        editorComponent = new JButton();
        editorComponent.setBackground(Color.white);
        editorComponent.setBorderPainted(false);
        editorComponent.setContentAreaFilled(false);

        // Make sure focus goes back to the table when the dialog is closed
        editorComponent.setFocusable(false);

        dialog = new ScriptDialog();
    }

    public void cancelEdit()
    {
        fireEditingCanceled();
    }

    public void endEdit()
    {
        newInput = dialog.getNewValue();

        if (newInput.trim().length() > 0)
        {
            scenario.getStepAt(position).put(stepProperty, newInput);
        }

        fireEditingStopped();
    }

    @Override
    public Object getCellEditorValue()
    {
        return null;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        oldValue = scenario.getStepAt(row).has(stepProperty)
                ? oldValue = scenario.getStepAt(row).getString(stepProperty) : "";

        newInput = oldValue;

        this.position = row;

        final StepScriptEditorDialog editor = this;
        dialog.setOldValue(oldValue);

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                dialog.setEditor(editor);
                dialog.show();
            }
        });

        return editorComponent;
    }

}
