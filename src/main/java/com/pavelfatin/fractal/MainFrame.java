/*
 * Copyright (C) 2012 Pavel Fatin <http://pavelfatin.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pavelfatin.fractal;

import com.pavelfatin.fractal.picture.PictureListener;
import com.pavelfatin.fractal.picture.Update;
import com.pavelfatin.fractal.renderer.Renderer;
import com.pavelfatin.fractal.renderer.RendererFactory;
import com.pavelfatin.fractal.renderer.RendererType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

class MainFrame extends JFrame {
    private static final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
    public static final String CONTROLS_FILE_NAME = "controls.html";

    private final PictureModel myModel = new PictureModel();
    private final RendererFactory myRendererFactory = new RendererFactory();
    private final RenderedPicture myPicture = new RenderedPicture(myModel, SCREEN_SIZE);

    private final JComboBox myRendererComboBox = new JComboBox();
    private final JCheckBox myIncrementalCheckBox = new JCheckBox("Incremental");
    private final JLabel myPositionLabel = new JLabel();
    private final JLabel myTimeLabel = new JLabel();
    private final JScrollBar myHorizontalBar = new JScrollBar(Adjustable.HORIZONTAL);
    private final JScrollBar myVerticalBar = new JScrollBar(Adjustable.VERTICAL);
    private final Controller myController;

    MainFrame() {
        final JRootPane root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0), "SHOW_CONTROLS");
        root.getActionMap().put("SHOW_CONTROLS", new ShowControlsAction());

        final DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(myRendererFactory.getAvailableRendererTypes());
        myRendererComboBox.setModel(comboBoxModel);
        myRendererComboBox.setSelectedItem(myRendererComboBox.getItemAt(myRendererComboBox.getItemCount() - 1));
        myRendererComboBox.setFocusable(false);
        myRendererComboBox.addActionListener(new MyRendererListener());

        final JLabel infoLabel = new JLabel();
        infoLabel.setForeground(Color.GRAY);

        if (comboBoxModel.getIndexOf(RendererType.OpenCL) == -1) {
            infoLabel.setText("(OpenCL is not available)");
        }

        myIncrementalCheckBox.setFocusable(false);
        myIncrementalCheckBox.setMnemonic('I');
        myIncrementalCheckBox.setSelected(myPicture.isIncremental());
        myIncrementalCheckBox.addActionListener(new MyIncrementalCheckBoxListener());

        final JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEADING));
        final JLabel label = new JLabel("Renderer:");
        label.setLabelFor(myRendererComboBox);
        label.setDisplayedMnemonic('R');
        toolbar.add(label);
        toolbar.add(myRendererComboBox);
        toolbar.add(infoLabel);

        final JPanel header = new JPanel(new BorderLayout());
        header.setBorder(new EmptyBorder(0, 0, 0, 3));
        header.add(toolbar, BorderLayout.WEST);
        header.add(myIncrementalCheckBox, BorderLayout.EAST);

        final Canvas canvas = new Canvas(myPicture);
        canvas.setFocusable(true);
        canvas.setPreferredSize(myModel.getViewSize());

        final JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(3, 3, 3, 3));
        footer.add(myPositionLabel, BorderLayout.WEST);
        footer.add(myTimeLabel, BorderLayout.EAST);

        final Container content = getContentPane();
        content.add(header, BorderLayout.NORTH);
        content.add(createScrollPane(canvas, myHorizontalBar, myVerticalBar), BorderLayout.CENTER);
        content.add(footer, BorderLayout.SOUTH);

        addWindowListener(new MyWindowListener());

        myController = new Controller(myModel, canvas, myHorizontalBar, myVerticalBar);

        myModel.addModelListener(new MyModelListener());
        myPicture.addPictureListener(new MyPictureListener());

        updateTitle();
        updatePosition();
        updateRenderer();
        updateScrollBars();

        myModel.refresh();
    }

    private static JPanel createScrollPane(final JComponent view, final JScrollBar horizontalBar, final JScrollBar verticalBar) {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new JScrollPane().getBorder());

        final GridBagConstraints constraints = new GridBagConstraints();

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        panel.add(horizontalBar, constraints);

        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.VERTICAL;
        panel.add(verticalBar, constraints);

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 1.0D;
        constraints.weighty = 1.0D;
        constraints.fill = GridBagConstraints.BOTH;
        panel.add(view, constraints);

        return panel;
    }

    private void updateRenderer() {
        final Renderer oldRenderer = myPicture.getRenderer();
        if (oldRenderer != null) {
            oldRenderer.dispose();
        }
        final RendererType type = (RendererType) myRendererComboBox.getSelectedItem();
        myPicture.setRenderer(myRendererFactory.createRenderer(type, SCREEN_SIZE.width, SCREEN_SIZE.height));
    }

    private void updatePosition() {
        myPositionLabel.setText(String.format("(%f, %f)", myModel.getRealPartCenter(), myModel.getImaginaryPartCenter()));
    }

    private void updateTitle() {
        final Dimension size = myModel.getViewSize();
        setTitle(String.format("Mandelbrot set (%dx%d)", size.width, size.height));
    }

    private void updateScrollBars() {
        final Dimension size = myModel.getFullSize();
        final Rectangle r = myModel.getViewBounds();

        myController.setProcessScrollEvents(false);

        final int unitIncrement = r.width / 10;
        final int blockIncrement = r.width / 2;

        myHorizontalBar.setValues(r.x, r.width, 0, size.width);
        myHorizontalBar.setUnitIncrement(unitIncrement);
        myHorizontalBar.setBlockIncrement(blockIncrement);

        myVerticalBar.setValues(r.y, r.height, 0, size.height);
        myVerticalBar.setUnitIncrement(unitIncrement);
        myVerticalBar.setBlockIncrement(blockIncrement);

        myController.setProcessScrollEvents(true);
    }

    private void showControls() {
        final String html = Utilities.load(getClass(), CONTROLS_FILE_NAME);
        JOptionPane.showMessageDialog(MainFrame.this, new JLabel(html), "Controls", JOptionPane.PLAIN_MESSAGE);
    }

    private class MyWindowListener extends WindowAdapter {
        @Override
        public void windowClosed(final WindowEvent e) {
            myPicture.getRenderer().dispose();
        }

        @Override
        public void windowOpened(WindowEvent e) {
            showControls();
        }
    }

    private class MyRendererListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            updateRenderer();
            myModel.refresh();
        }
    }

    private class MyIncrementalCheckBoxListener implements ActionListener {
        @Override
        public void actionPerformed(final ActionEvent e) {
            myPicture.setIncremental(myIncrementalCheckBox.isSelected());
        }
    }

    private class MyModelListener implements ModelListener {
        @Override
        public void onMove(final int dx, final int dy) {
            updateScrollBars();
            updatePosition();
        }

        @Override
        public void onChange() {
            updateScrollBars();
            updatePosition();
        }

        @Override
        public void onResize(final Dimension oldSize, final Dimension newSize) {
            updateTitle();
        }
    }

    private class MyPictureListener implements PictureListener {
        @Override
        public void onUpdate(final Update update, final long elapsedTime) {
            myTimeLabel.setText(String.format("%d ms", elapsedTime));
        }
    }

    private class ShowControlsAction extends AbstractAction {
        @Override
        public void actionPerformed(final ActionEvent e) {
            showControls();
        }
    }
}
