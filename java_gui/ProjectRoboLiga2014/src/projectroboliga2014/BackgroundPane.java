/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package projectroboliga2014;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author Tilen
 */
public class BackgroundPane extends javax.swing.JPanel {

    /**
     * Creates new form BackgroundPane
     */
    
    
    private String coordinatesToDraw = "";
    private boolean check = false;  
    private int firX = 0;
    private int firY = 0;
    
    
    public BufferedImage displayedImage = null;
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setOpaque(false);
        Graphics2D g2 = (Graphics2D) g;
        //getting size of monitor
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	GraphicsDevice[] gs = ge.getScreenDevices();
	double screenWidth = 50;
	double screenHeight = 50;
        for (GraphicsDevice g1 : gs) {
            DisplayMode dm = g1.getDisplayMode();
            screenWidth = dm.getWidth();
            screenHeight = dm.getHeight()-100;
        }
        //
        
        if (displayedImage != null) {
                //This is necessary to keep the image in the correct ratio
                //screenRatio = ratio of Height to Width
                double imageWidth = displayedImage.getWidth();
                double imageHeight = displayedImage.getHeight();
                double imageRatio = imageHeight/imageWidth;
                double screenRatio = screenHeight/screenWidth;

                //If ratios dont match, resize
                if (imageRatio != screenRatio) {
                    //If ratio is bellow 1, it's a widephoto and we adjust it to screen
                    if (imageRatio < 1) {
                        screenHeight = (int)(screenWidth*imageRatio);
                    //If ratio is above 1, it's a narrow photo and we adjust it to screen
                    } else if (imageRatio > 1) {
                        screenWidth = (int)(screenHeight*(1/imageRatio));
                    }
                }

                //Convert to int for drawing purpose
                int width = (int)(screenWidth);
                int height = (int)(screenHeight);


                setPreferredSize(new Dimension(displayedImage.getWidth(), displayedImage.getHeight()));
                revalidate();

                //If we didnt want to fit image to screen, we'd use commented code bellow
                //g.drawImage(displayedImage, 0, 0,null);

                //Sliko narišemo čez celoten zaslon oziroma glede na resolucijo zaslona in slike
                g.drawImage(displayedImage, 0, 0, width, height, null);    
                check = false;
        }
        
        //Drawing lines on image when clicking
        if (coordinatesToDraw.length() > 0) {
            check = false;
            int firstX = 0;
            int firstY = 0;
            int secondX = 0;
            int secondY = 0;

            String delim = "-";
            String[] tokens = coordinatesToDraw.split(delim);
            int maxLen = tokens.length;
            int x = Integer.parseInt(tokens[0]);
            int y = Integer.parseInt(tokens[1]);
            firstX = x;
            firstY = y;
            for (int i = 2; i < maxLen; i++) {
                if (i%2 == 0) {
                    secondX = Integer.parseInt(tokens[i]);
                } else if (i%2 == 1) {
                    secondY = Integer.parseInt(tokens[i]);
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(firstX, firstY, secondX, secondY);
                    g2.setColor(Color.blue);
                    g2.setStroke(new BasicStroke(6));
                    g2.drawLine(firstX, firstY, firstX, firstY);
                    g2.drawLine(secondX, secondY, secondX, secondY);  
                    firstX = secondX;
                    firstY = secondY;
                } else {
                    System.out.println("doesn't get here.");
                }
            } 
        }
        if (check) {
            g2.setColor(Color.blue);
            g2.setStroke(new BasicStroke(6));
            g2.drawLine(firX, firY, firX, firY);
        }
    }
    
    public void openFile(File f) {
        System.out.println("Opening file: "+ f.getName());
        try {
            BufferedImage photo = ImageIO.read(f);
            displayedImage = new BufferedImage(photo.getWidth(), photo.getHeight(), BufferedImage.TYPE_INT_RGB);
            
            for (int i = 0; i < photo.getWidth(); i++) {
                for (int j = 0; j < photo.getHeight(); j++) {
                    displayedImage.setRGB(i, j, photo.getRGB(i, j));
                }
            }
        } catch (IOException e) { 
        }
   
        repaint();
    }
    
    public void iLovePainting(String coordinates) {
        coordinatesToDraw = coordinates;
        repaint();
    }
    
    public void iAmSupposedToBeHere(int fX, int fY) {
        firX = fX;
        firY = fY;
        check = true;
        repaint();
    }
    
    
    
    
    public BackgroundPane() {
        setOpaque(false);
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setOpaque(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
