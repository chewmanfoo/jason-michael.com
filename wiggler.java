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