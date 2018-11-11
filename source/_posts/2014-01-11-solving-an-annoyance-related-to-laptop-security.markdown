---
layout: post
title: "Solving an annoyance related to laptop security"
date: 2014-01-11 00:50
comments: true
categories:  Work, Java, Security
---
So my job gives me a nifty HP Laptop running Windows 7.  It's imaged by the fabulous IT guys off in another building, and, although I have local Admin rights, the AD rights supercede me, and apply some basic security constraints on my PC, one of which is that the PC goes to sleep and locks the screen after 15 minutes if I'm not using it.  I'm on it all day non-stop when I'm in the office, so this is never an issue, but when I work from home, it annoys the crap out of me.  When I work at home, I sit at a desk with my laotop to one side, and my home PC to the other side.  If I need to send an email or login to a server to do work, I use my laptop, but when I watch a video or do some sort of training that isn't only available on the company Intranet, I use my home PC.  So, often my laptop sits ignored for more than 15 minutes.  I like to glance at it to see that no one has messaged me or sent me an email, so it'd be nice if the laptop didn't log me out for inactivity.  After all, it's in the security of my home office - nobody's gonna sneak up and do something evil when it's on my desk - I'm the only one around.
<!-- more -->

So I immediately thought of a solution - the Robot class in Java awt provides a mechanism for moving the mouse or clicking on the keyboard, ala `robot.mouseMove(100,100);`.  As much as I hate slapping semicolons on every line so the compiler knows I'm done with that thought, I fired up Netbeans to see if I could accomplish this wiggler.

Here's what I came up with (minus all the fluff):

    private void toggle_buttonActionPerformed(java.awt.event.ActionEvent evt) {                                              
        String current = state_label.getText();
        
        switch (current) {
            case "resting":
                state_label.setText("wiggling");
                do_wiggle = false;
                break;
            case "wiggling":
                state_label.setText("resting");
                do_wiggle = true;
                break;
        }
    }                                             

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainUIJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainUIJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainUIJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainUIJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainUIJFrame().setVisible(true);
                t = new Thread(new WiggleLoop());
                t.start();
            }
        });
    }

    private static class WiggleLoop
        implements Runnable {
        public void run() {
            System.out.println("getting ready...");
            try {
                while (true) {
                    if (do_wiggle == true) {
                        System.out.println("wiggling!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }
                    // Pause for 4 seconds
                    Thread.sleep(sleep_seconds);
                    System.out.println("running");
                    
                }
            } catch (InterruptedException e) {
                //don't wiggle
                System.out.println("not running");
                
            }
        }
    }

It works and I'm satisfied.  I put it in an executable jar.  Now when I use my laptop at home I just double click the wiggler.jar and it keeps my laptop logged in all day.  Profit. 
