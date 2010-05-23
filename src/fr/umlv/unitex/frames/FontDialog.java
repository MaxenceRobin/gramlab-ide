/*
 * Unitex
 *
 * Copyright (C) 2001-2010 Université Paris-Est Marne-la-Vallée <unitex@univ-mlv.fr>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA.
 *
 */

package fr.umlv.unitex.frames;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import fr.umlv.unitex.FontInfo;

/**
 * This class describes a font selection dialog box.
 * 
 * @author Sébastien Paumier
 *  
 */
public class FontDialog extends JDialog {

	JTextField name = new JTextField(10);
	JTextField style = new JTextField(10);
	JTextField size = new JTextField("  ");
	JTextField script = new JTextField(10);
	JTextField example = new JTextField(6);
	JList fontList;
	JList styleList;
	JList sizeList;
	JList scriptList;
	Hashtable<String,Integer> styles;
	Hashtable<String,Integer> ranges;

	private static final int ASCII_BASE = 0x0000;
	private static final int GREEK_BASE = 0x0370;
	private static final int HEBREW_BASE = 0x0590;
	private static final int ARABIC_BASE = 0x0600;
	private static final int THAI_BASE = 0x0E00;
	private static final int GEORGIAN_BASE = 0x10A0;
	private static final int HIRAGANA_BASE = 0x3040;
	private static final int KATAKANA_BASE = 0x30A0;
	private static final int HANGUL_BASE = 0x3130;
	private static final int KANJI_BASE = 0x4E00;

	FontInfo info;
	
	/**
	 * Creates a new font dialog box.
	 * 
	 * @param in
	 *            indicates if we select an input or an output font for graphs.
	 */
	public FontDialog(FontInfo i) {
		super(UnitexFrame.mainFrame, "Font", true);
		init();
		configureFont(i);
		setContentPane(constructPanel());
		pack();
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				info=null;
			}
		});
		setLocationRelativeTo(UnitexFrame.mainFrame);
	}

	void configureFont(FontInfo i) {
		if (i==null) {
			throw new IllegalArgumentException("Cannot configure a null font info");
		}
		info=i.clone();
		name.setText(i.font.getName());
		style.setText(""+i.font.getStyle());
		size.setText(""+i.size);
		fontList.clearSelection();
		styleList.clearSelection();
		sizeList.clearSelection();
		refresh();
	}

	void refresh() {
		/* name */
		String s=(String)(fontList.getSelectedValue());
		if (s!=null) {
			name.setText(s);
		}/* style */
		int fontStyle=info.font.getStyle();
		/* We can use getSelectedIndex() instead of getSelectedValue() because
		 * style values are 0 1 2 3 
		 */
		int n=styleList.getSelectedIndex();
		if (n!=-1) {
			style.setText((String)styleList.getSelectedValue());
			fontStyle = n;
		}/* size */
		Integer j=(Integer)(sizeList.getSelectedValue());
		if (j!=null) {
			size.setText(j+"");
			info.size = j.intValue();
		}
		/* updating font */
		info.font = new Font(name.getText(), fontStyle, (int) (info.size / 0.72));
		example.setFont(info.font);
	}

	public FontInfo getFontInfo() {
		return info;
	}
	
	private JPanel constructPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(constructUpPanel(), BorderLayout.CENTER);
		panel.add(constructDownPanel(), BorderLayout.SOUTH);
		return panel;
	}

	private JPanel constructUpPanel() {
		JPanel upPanel = new JPanel(new BorderLayout());
		upPanel.add(constructFontPanel(), BorderLayout.WEST);
		upPanel.add(constructStylePanel(), BorderLayout.CENTER);
		upPanel.add(constructSizePanel(), BorderLayout.EAST);
		return upPanel;
	}

	private JPanel constructDownPanel() {
		JPanel downPanel = new JPanel(new BorderLayout());
		downPanel.add(constructScriptPanel(), BorderLayout.WEST);
		downPanel.add(constructExampleAndButtonPanel(), BorderLayout.CENTER);
		return downPanel;
	}

	private JPanel constructFontPanel() {
		JPanel fontPanel = new JPanel(new BorderLayout());
		fontPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		p.add(new JLabel(" Font: "));
		p.add(name);
		fontPanel.add(p, BorderLayout.NORTH);
		fontPanel.add(new JScrollPane(fontList), BorderLayout.CENTER);
		return fontPanel;
	}

	private JPanel constructStylePanel() {
		JPanel stylePanel = new JPanel(new BorderLayout());
		stylePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		p.add(new JLabel(" Style: "));
		p.add(style);
		stylePanel.add(p, BorderLayout.NORTH);
		stylePanel.add(new JScrollPane(styleList), BorderLayout.CENTER);
		return stylePanel;
	}

	private JPanel constructSizePanel() {
		JPanel sizePanel = new JPanel(new BorderLayout());
		sizePanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		p.add(new JLabel(" Size: "));
		p.add(size);
		sizePanel.add(p, BorderLayout.NORTH);
		sizePanel.add(new JScrollPane(sizeList), BorderLayout.CENTER);
		return sizePanel;
	}

	private JPanel constructScriptPanel() {
		JPanel scriptPanel = new JPanel(new BorderLayout());
		scriptPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		JPanel p = new JPanel();
		p.setLayout(new GridLayout(2, 1));
		p.add(new JLabel(" Script: "));
		p.add(script);
		scriptPanel.add(p, BorderLayout.NORTH);
		scriptPanel.add(new JScrollPane(scriptList), BorderLayout.CENTER);
		return scriptPanel;
	}

	private JPanel constructExampleAndButtonPanel() {
		JPanel exampleAndButtonPanel = new JPanel(new BorderLayout());
		exampleAndButtonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
		exampleAndButtonPanel.add(constructExamplePanel(), BorderLayout.CENTER);
		exampleAndButtonPanel.add(constructButtonPanel(), BorderLayout.SOUTH);
		return exampleAndButtonPanel;
	}

	private JPanel constructExamplePanel() {
		JPanel examplePanel = new JPanel(new BorderLayout());
		examplePanel.setBorder(new TitledBorder("Example"));
		examplePanel.add(new JPanel(), BorderLayout.WEST);
		examplePanel.add(example, BorderLayout.CENTER);
		examplePanel.add(new JPanel(), BorderLayout.EAST);
		return examplePanel;
	}

	private JPanel constructButtonPanel() {
		JPanel buttonPanel = new JPanel();
		Action okAction = new AbstractAction("OK") {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		};
		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				/* After cancelling, a call to getFont should
				 * return null 
				 */
				info=null;
				setVisible(false);
			}
		};
		JButton OK = new JButton(okAction);
		JButton CANCEL = new JButton(cancelAction);
		buttonPanel.add(OK);
		buttonPanel.add(CANCEL);
		return buttonPanel;
	}

	private void init() {
		// setting the components form
		name.setDisabledTextColor(Color.white);
		name.setBackground(Color.white);
		name.setText("");
		style.setEditable(false);
		style.setDisabledTextColor(Color.white);
		style.setBackground(Color.white);
		style.setText("");
		style.setEditable(false);
		size.setDisabledTextColor(Color.white);
		size.setBackground(Color.white);
		size.setText("");
		size.setEditable(false);
		script.setDisabledTextColor(Color.white);
		script.setBackground(Color.white);
		script.setText("");
		script.setEditable(false);
		// setting the components content
		styles = new Hashtable<String,Integer>();
		styles.put("Plain", new Integer(Font.PLAIN));
		styles.put("Bold", new Integer(Font.BOLD));
		styles.put("Italic", new Integer(Font.ITALIC));
		styles.put("Bold Italic", new Integer(Font.BOLD | Font.ITALIC));

		ranges = new Hashtable<String,Integer>();
		ranges.put("Occidental", new Integer(ASCII_BASE));
		ranges.put("Greek", new Integer(GREEK_BASE));
		ranges.put("Hebrew", new Integer(HEBREW_BASE));
		ranges.put("Arabic", new Integer(ARABIC_BASE));
		ranges.put("Thai", new Integer(THAI_BASE));
		ranges.put("Georgian", new Integer(GEORGIAN_BASE));
		ranges.put("Hiragana", new Integer(HIRAGANA_BASE));
		ranges.put("Katakana", new Integer(KATAKANA_BASE));
		ranges.put("Hangul", new Integer(HANGUL_BASE));
		ranges.put("Kanji", new Integer(KANJI_BASE));
		
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		fontList=new JList(ge.getAvailableFontFamilyNames());
		fontList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				refresh();
			}
		});

		styleList=new JList(new String[] {"Plain", "Bold", "Italic", "Bold Italic"});
		styleList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				refresh();
			}
		});

		sizeList=new JList(new Integer[] {
				Integer.valueOf(8),
				Integer.valueOf(9),
				Integer.valueOf(10),
				Integer.valueOf(11),
				Integer.valueOf(12),
				Integer.valueOf(14),
				Integer.valueOf(16),
				Integer.valueOf(18),
				Integer.valueOf(20),
				Integer.valueOf(22),
				Integer.valueOf(24),
				Integer.valueOf(28),
				Integer.valueOf(36),
				Integer.valueOf(48),
				Integer.valueOf(72)});
		sizeList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				refresh();
			}
		});

		scriptList=new JList(new String[] {"Occidental", "Greek", "Hebrew", "Arabic",
			"Thai", "Georgian", "Hiragana", "Katakana", "Hangul", "Kanji"});
		scriptList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				String s=(String)(scriptList.getSelectedValue());
				if (s==null) return;
				script.setText(s);
				switch (ranges.get(s).intValue()) {
				default: example.setText("AaBbCc"); break;
				case GREEK_BASE: example.setText("\u0391\u03b1\u0392\u03b2\u0393\u03b3"); break;
				case HEBREW_BASE: example.setText("\u05d0\u05d1\u05d2\u05d3\u05d4\u05d5"); break;
				case ARABIC_BASE: example.setText("\u0621\u0622\u0623\u0624\u0625\u0626"); break;
				case THAI_BASE: example.setText("\u0e01\u0e02\u0e03\u0e04\u0e05\u0e06"); break;
				case GEORGIAN_BASE: example.setText("\u10a0\u10a1\u10a2\u10a3\u10a4\u10a5"); break;
				case HIRAGANA_BASE: example.setText("\u3041\u3042\u3043\u3044\u3045\u3046"); break;
				case KATAKANA_BASE: example.setText("\u30a1\u30a2\u30a3\u30a4\u30a5\u30a6"); break;
				case HANGUL_BASE: example.setText("\u3131\u3132\u3133\u3134\u3135\u3136"); break;
				case KANJI_BASE: example.setText("\u4e07\u4e08\u4e09\u4e0a\u4e0b\u4e5f"); break;
				}
			}
		});
		scriptList.setSelectedIndex(0);
	}

}