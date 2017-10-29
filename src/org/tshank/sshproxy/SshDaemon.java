package org.tshank.sshproxy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.sshd.common.io.IoServiceFactoryFactory;
import org.apache.sshd.common.io.nio2.Nio2ServiceFactoryFactory;
import org.apache.sshd.common.util.SecurityUtils;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.CachingPublicKeyAuthenticator;
import org.bouncycastle.openssl.PEMWriter;
import org.eclipse.jgit.internal.JGitText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tshank.sshproxy.command.SshCommandFactory;
import org.tshank.sshproxy.command.WelcomeShell;
import org.tshank.sshproxy.manager.PubKeyAuthManagerImpl;
import org.tshank.sshproxy.ssh.DisabledFilesystemFactory;
import org.tshank.sshproxy.ssh.FileKeyManager;
import org.tshank.sshproxy.ssh.FileKeyPairProvider;
import org.tshank.sshproxy.ssh.NonForwardingFilter;
import org.tshank.sshproxy.ssh.SshKeyAuthenticator;
import org.tshank.sshproxy.ssh.SshServerSessionFactory;
import org.tshank.sshproxy.utils.IdGenerator;
import org.tshank.sshproxy.utils.JnaUtils;
import org.tshank.sshproxy.utils.WorkQueue;

import com.google.common.io.Files;

@SuppressWarnings("deprecation")
public class SshDaemon {

  private final Logger log = LoggerFactory.getLogger(SshDaemon.class);
  
  public static enum SshSessionBackend {
    MINA, NIO2
  }
  
  private final SshServer sshd;
  private final AtomicBoolean run;
  private WorkQueue workQueue;
  public SshDaemon() {
    
    IdGenerator idGenerator = new IdGenerator();
    workQueue = new WorkQueue(idGenerator, 20);
    
    SecurityUtils.setRegisterBouncyCastle(true);
    if (SecurityUtils.isBouncyCastleRegistered()) {
      
    }
    
    File rsaKeyStore = new File("E:/", "ssh-rsa-hostkey.pem");
    File dsaKeyStore = new File("E:/", "ssh-dsa-hostkey.pem");
    generateKeyPair(rsaKeyStore, "RSA", 2048);
    generateKeyPair(dsaKeyStore, "DSA", 0);
    FileKeyPairProvider hostKeyPairProvider = new FileKeyPairProvider();
    hostKeyPairProvider.setFiles(new String [] { rsaKeyStore.getPath(), dsaKeyStore.getPath(), dsaKeyStore.getPath() });

    // Configure the preferred SSHD backend
    System.setProperty(IoServiceFactoryFactory.class.getName(), Nio2ServiceFactoryFactory.class.getName());
    
    InetSocketAddress addr = new InetSocketAddress("localhost",29419);
    
    sshd = SshServer.setUpDefaultServer();
    sshd.setPort(addr.getPort());
    sshd.setHost(addr.getHostName());
    sshd.setKeyPairProvider(hostKeyPairProvider);
    
   
    SshKeyAuthenticator keyAuthenticator = new SshKeyAuthenticator(new FileKeyManager(), new PubKeyAuthManagerImpl());
    sshd.setPublickeyAuthenticator(new CachingPublicKeyAuthenticator(keyAuthenticator));
    
    sshd.setSessionFactory(new SshServerSessionFactory());
    sshd.setFileSystemFactory(new DisabledFilesystemFactory());
    sshd.setTcpipForwardingFilter(new NonForwardingFilter());
    sshd.setCommandFactory(new SshCommandFactory(workQueue));
    sshd.setShellFactory(new WelcomeShell(App.getInstance()));
    
    run = new AtomicBoolean(false);
  }
  
  /**
   * Start this daemon on a background thread.
   *
   * @throws IOException
   *             the server socket could not be opened.
   * @throws IllegalStateException
   *             the daemon is already running.
   */
  public synchronized void start() throws IOException {
      if (run.get()) {
          throw new IllegalStateException(JGitText.get().daemonAlreadyRunning);
      }

      sshd.start();
      run.set(true);

      String sshBackendStr = SshSessionBackend.NIO2.name();

      log.info(MessageFormat.format(
              "SSH Daemon ({0}) is listening on {1}:{2,number,0}",
              sshBackendStr, sshd.getHost(), sshd.getPort()));
  }

  /** @return true if this daemon is receiving connections. */
  public boolean isRunning() {
      return run.get();
  }

  /** Stop this daemon. */
  public synchronized void stop() {
      if (run.get()) {
          log.info("SSH Daemon stopping...");
          run.set(false);

          try {
              ((SshCommandFactory) sshd.getCommandFactory()).stop();
              sshd.stop();
          } catch (IOException e) {
              log.error("SSH Daemon stop interrupted", e);
          }
      }
  }
  
  private void generateKeyPair(File file, String algorithm, int keySize) {
    if (file.exists()) {
        return;
    }
    try {
        KeyPairGenerator generator = SecurityUtils.getKeyPairGenerator(algorithm);
        if (keySize != 0) {
            generator.initialize(keySize);
            //log.info("Generating {}-{} SSH host keypair...", algorithm, keySize);
        } else {
            //log.info("Generating {} SSH host keypair...", algorithm);
        }
        KeyPair kp = generator.generateKeyPair();

        // create an empty file and set the permissions
        Files.touch(file);
        try {
            JnaUtils.setFilemode(file, JnaUtils.S_IRUSR | JnaUtils.S_IWUSR);
        } catch (UnsatisfiedLinkError | UnsupportedOperationException e) {
            // Unexpected/Unsupported OS or Architecture
        }

        FileOutputStream os = new FileOutputStream(file);
        PEMWriter w = new PEMWriter(new OutputStreamWriter(os));
        w.writeObject(kp);
        w.flush();
        w.close();
    } catch (Exception e) {
        //log.warn(MessageFormat.format("Unable to generate {0} keypair", algorithm), e);
        return;
    }
}
}
