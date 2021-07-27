package gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.sun.java.swing.plaf.windows.resources.windows;

import javax.swing.JButton;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.GridBagLayout;
import javax.swing.JTextField;
import java.awt.GridBagConstraints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Color;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import java.awt.CardLayout;
import javax.swing.SpringLayout;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import net.miginfocom.swing.MigLayout;

public class Forum extends JFrame {

	private JPanel contentPane;
	int x = 270;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Forum frame = new Forum();
					frame.setVisible(true);
					frame.setLocationRelativeTo(null);
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Forum() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1399, 821);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);
		panel_2.setLayout(new GridLayout(0, 1, 0, 0));
		
		final JPanel panel = new JPanel();
		
		
		panel.setBackground(Color.GRAY);
		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
		
		
		
		JLabel lblNewLabel = new JLabel("   Your Friends   ");
		lblNewLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		panel.add(lblNewLabel);
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		JButton btnNewButton = new JButton("Add friend   +");
		btnNewButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				panel.add(Box.createRigidArea(new Dimension(0,5)));
				JLabel lblNewLabel_1 = new JLabel("Friend1");
				lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
				panel.add(lblNewLabel_1);
				JLabel lblNewLabel_2 = new JLabel("Online");
				lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 10));
				lblNewLabel_2.setForeground(Color.GREEN);
				panel.add(lblNewLabel_2);
				panel.revalidate();
			}
		});
		panel.add(btnNewButton);
		
		panel.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		JPanel panel_6 = new JPanel();
		panel_6.setBackground(Color.GRAY);
		panel_6.setLayout(new BoxLayout(panel_6,BoxLayout.Y_AXIS));
		final JScrollPane scrollPane = new JScrollPane(panel_6,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel.add(scrollPane);
		
		JButton btnNewButton_5 = new JButton("See all students");
		btnNewButton_5.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(btnNewButton_5);
		
		
		JLabel lblNewLabel_1 = new JLabel("Friend1");
		lblNewLabel_1.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_6.add(lblNewLabel_1);
		
		JLabel lblNewLabel_2 = new JLabel("Online");
		lblNewLabel_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_2.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblNewLabel_2.setForeground(Color.GREEN);
		panel_6.add(lblNewLabel_2);
		
		
		
		contentPane.add(panel, BorderLayout.WEST);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBackground(Color.GRAY);
		panel_1.setForeground(Color.BLACK);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.Y_AXIS));
		contentPane.add(panel_1, BorderLayout.EAST);
		
		JLabel lblNewLabel_3 = new JLabel("   Help Servers   ");
		lblNewLabel_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 20));
		panel_1.add(lblNewLabel_3);
		
		
		panel_1.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		JButton btnNewButton_2 = new JButton("Create New Server   +");
		btnNewButton_2.setAlignmentX(Component.CENTER_ALIGNMENT);
		btnNewButton_2.setFont(new Font("Tahoma", Font.PLAIN, 10));
		
		panel_1.add(btnNewButton_2);
		
		panel_1.add(Box.createRigidArea(new Dimension(0,5)));
		
		
		JPanel panel_3 = new JPanel();
		panel_3.setBackground(Color.GRAY);
		panel_3.setForeground(Color.BLACK);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.PAGE_AXIS));
		final JScrollPane scrollPane2 = new JScrollPane(panel_3,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel_1.add(scrollPane2);
		
		JLabel lblNewLabel_4 = new JLabel("Server1");
		lblNewLabel_4.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 16));
		panel_3.add(lblNewLabel_4);
		
		JLabel lblNewLabel_5 = new JLabel("Online 2");
		lblNewLabel_5.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_5.setFont(new Font("Tahoma", Font.BOLD, 10));
		lblNewLabel_5.setForeground(Color.GREEN);
		panel_3.add(lblNewLabel_5);
		
		JButton btnNewButton_4 = new JButton("Visit Main Server");
		btnNewButton_4.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_1.add(btnNewButton_4);
		
		JPanel panel_4 = new JPanel();
		panel_4.setBackground(Color.GRAY);
		contentPane.add(panel_4, BorderLayout.NORTH);
		
		JLabel lblNewLabel_6 = new JLabel("Welcome User!");
		lblNewLabel_6.setFont(new Font("Tahoma", Font.BOLD, 16));
		panel_4.add(lblNewLabel_6);
		panel_4.add(Box.createHorizontalStrut(300));
		
		JLabel lblNewLabel_7 = new JLabel("See What's New");
		lblNewLabel_7.setFont(new Font("Tahoma", Font.BOLD, 16));
		panel_4.add(lblNewLabel_7);
		
		JPanel panel_5 = new JPanel();
		panel_5.setBackground(Color.DARK_GRAY);
		contentPane.add(panel_5, BorderLayout.CENTER);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_8 = new JLabel("Here are all of the questions asked by the students:      ");
		lblNewLabel_8.setAlignmentX(Component.CENTER_ALIGNMENT);
		lblNewLabel_8.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_8.setOpaque(true);
		lblNewLabel_8.setAlignmentY(Component.TOP_ALIGNMENT);
		lblNewLabel_8.setBackground(new Color(255, 215, 0));
		lblNewLabel_8.setForeground(Color.BLACK);
		panel_5.add(lblNewLabel_8);
		
		panel_5.add(Box.createRigidArea(new Dimension(0,20)));
		
		JButton btnNewButton_3 = new JButton("Write your own question!");
		btnNewButton_3.setFont(new Font("Tahoma", Font.PLAIN, 16));
		btnNewButton_3.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_5.add(btnNewButton_3);
		
		panel_5.add(Box.createRigidArea(new Dimension(0,5)));
		
		final JPanel panel_7 = new JPanel();
		panel_7.setBackground(Color.DARK_GRAY);
		final JScrollPane scrollPane3 = new JScrollPane(panel_7,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		panel_5.add(scrollPane3);
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.Y_AXIS));
		
		JLabel lblNewLabel_10 = new JLabel("Andrew says: ");
		lblNewLabel_10.setBackground(Color.YELLOW);
		lblNewLabel_10.setFont(new Font("Tahoma", Font.PLAIN, 13));
		lblNewLabel_10.setOpaque(true);
		panel_7.add(lblNewLabel_10);
		
		JLabel lblNewLabel_9 = new JLabel("Can't seem to solve this math problem...  ");
		panel_7.add(lblNewLabel_9);
		lblNewLabel_9.setFont(new Font("Tahoma", Font.BOLD, 13));
		lblNewLabel_9.setBackground(new Color(154, 205, 50));
		lblNewLabel_9.setOpaque(true);
		
		JTextArea textArea = new JTextArea();
		textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		textArea.setText("Hi, I can't calculate the value of PI, please help, Thank you!");
		textArea.setSize(new Dimension(200,200));
		panel_7.add(textArea);
		
		JButton btnNewButton_1 = new JButton("Write answer");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				JLabel lblNewLabel_10 = new JLabel("Andrew says: ");
				lblNewLabel_10.setBackground(Color.YELLOW);
				lblNewLabel_10.setFont(new Font("Tahoma", Font.PLAIN, 13));
				lblNewLabel_10.setOpaque(true);
				panel_7.add(lblNewLabel_10);
				
				JLabel lblNewLabel_9 = new JLabel("Can't seem to solve this math problem...  ");
				panel_7.add(lblNewLabel_9);
				lblNewLabel_9.setFont(new Font("Tahoma", Font.BOLD, 13));
				lblNewLabel_9.setBackground(new Color(154, 205, 50));
				lblNewLabel_9.setOpaque(true);
				
				JTextArea textArea = new JTextArea();
				textArea.setAlignmentX(Component.LEFT_ALIGNMENT);
				textArea.setText("Hi, I can't calculate the value of PI, please help, Thank you!");
				textArea.setSize(new Dimension(200,200));
				panel_7.add(textArea);
				
				JButton btnNewButton_1 = new JButton("Write answer");
				panel_7.add(btnNewButton_1);
				
				panel_7.add(Box.createRigidArea(new Dimension(0,20)));
				
				panel_7.repaint();
			}
		});
		panel_7.add(btnNewButton_1);
		
		panel_7.add(Box.createRigidArea(new Dimension(0,20)));
		
		
		
	}
}
