package com.focusit.jsflight.player.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import org.json.JSONObject;

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
    private boolean pre = true;

    public StepScriptEditorDialog(UserScenario scenario, boolean pre)
    {
    	super(new JTextField());
    	setClickCountToStart(1);
    	this.scenario = scenario;
    	this.pre = pre;
    	
    	editorComponent = new JButton();
        editorComponent.setBackground(Color.white);
        editorComponent.setBorderPainted(false);
        editorComponent.setContentAreaFilled( false );

        // Make sure focus goes back to the table when the dialog is closed
        editorComponent.setFocusable( false );

    	dialog = new ScriptDialog();
    }

    public void cancelEdit()
    {
        fireEditingCanceled();
    }

    public void endEdit()
    {
        fireEditingStopped();
    }

    @Override
    public Object getCellEditorValue()
    {
        return newInput;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
    	oldValue = (String) table.getModel().getValueAt(row, column);
    	newInput = (String) table.getModel().getValueAt(row, column);
    	
    	final StepScriptEditorDialog editor = this;
    	
    	SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
            	dialog.setEditor(editor);
            	dialog.show();
            }
        });
    	
    	return editorComponent;
    }

}
