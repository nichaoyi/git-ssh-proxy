package org.tshank.sshproxy;

import java.io.IOException;
import java.util.Scanner;

import org.tshank.sshproxy.ssh.FileKeyManager;
import org.tshank.sshproxy.ssh.IPublicKeyManager;

public class App implements Runnable {

  private static App _intance = null;
  private SshDaemon sshDaemon;
  
  public static synchronized App getInstance() {
    if (null == _intance) {
      _intance = new App();
    }
    return _intance;
  }
  
  private IPublicKeyManager publicKeymanager;
  
  private App() {
    publicKeymanager = new FileKeyManager();
  }
  
  public IPublicKeyManager getPublicKeyManager() {
    return publicKeymanager;
  }

  public static void main(String args[]) {
    new Thread(App.getInstance()).start();
  }

  @Override
  public void run() {
    sshDaemon = new SshDaemon();
    try {
      sshDaemon.start();
      
      while (true) {
        @SuppressWarnings("resource")
        int x= new Scanner(System.in).nextInt();
        if (x == 100) {
          break;
        }
      }
      
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
