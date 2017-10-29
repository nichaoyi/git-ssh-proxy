package org.tshank.sshproxy.test;

import java.net.InetSocketAddress;

public class Test {

  public static void main(String[] args) {
    // TODO Auto-generated method stub
    InetSocketAddress addr = new InetSocketAddress("localhost",29419);
    System.out.println(addr.getAddress());
    System.out.println(addr.getPort());
  }

}
